package rushhour.com.rushhour;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rushhour.com.rushhour.place.GooglePlacesReadTask;
import rushhour.com.rushhour.util.DirectionsJSONParser;
import rushhour.com.rushhour.location.GPSTracker;

public class ApplicationActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    public static final String NAME = ApplicationActivity.class.getSimpleName();

    /* Google Direction API */
    private GoogleMap googleMap;
    private String serverKey = "AIzaSyALQ8AUHj3Y5PeSwUVjE-aN3KnaLqlnK-w";
    //private String serverKey = "AIzaSyDW6B_geqVgT2YaEYyn-FxxG8JqFIkIXmE";
    private LatLng camera;
    private LatLng origin;
    private LatLng destination;
    private double currentLat;
    private double currentLon;
    private double startLat;
    private double startLon;
    private double endLat;
    private double endLon;

    private EditText from;
    private EditText to;
    private String fromString;
    private String toString;
    private LatLng fromLatLng;
    private LatLng toLatLng;
    private double fromLat;
    private double fromLng;
    private double toLat;
    private double toLng;

    private Button button;

    private String[] colors = {"#7fff7272", "#7f31c7c5", "#7fff8a00"};

    private TextView duration1;
    private TextView duration2;
    private TextView duration3;
    private TextView duration4;

    private static int time = 0;

    private int PROXIMITY_RADIUS = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applicationactivity);

        duration1 = (TextView) findViewById(R.id.duration1);
        duration2 = (TextView) findViewById(R.id.duration2);
        duration3 = (TextView) findViewById(R.id.duration3);
        duration4 = (TextView) findViewById(R.id.duration4);

        from = (EditText)findViewById(R.id.from);
        to = (EditText)findViewById(R.id.to);
        from.setText("1 yonge street toronto ontario canada");
        to.setText("2373 yonge street toronto ontario canada");

        GPSTracker gpsTracker = new GPSTracker(this);
        currentLat = gpsTracker.getLatitude();
        currentLon = gpsTracker.getLongitude();

        camera = new LatLng(currentLat, currentLon);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                googleMap.clear();

                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                List<Double> list;
                list = getLatLng();
                startLat = list.get(0);
                startLon = list.get(1);
                endLat = list.get(2);
                endLon = list.get(3);

                origin = new LatLng(startLat, startLon);
                destination = new LatLng(endLat, endLon);

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 15));

                googleMap.addMarker(new MarkerOptions()
                        .position(destination)
                        .title("End")).showInfoWindow();

                googleMap.addMarker(new MarkerOptions()
                        .position(origin)
                        .title("Start")).showInfoWindow();

                requestDirection();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setMyLocationEnabled(true);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 15));
    }

    @Override
    public void onClick(View v) {

    }

    public void requestDirection() {
        Snackbar.make(button, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        /*
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.WALKING)
                .alternativeRoute(false)
                .execute(this);
        */

        DownloadTask downloadTask = new DownloadTask();
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, destination, time);
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address = null;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 1); // only return 1 address
            if (address != null && address.size() > 0) {
                Address location = address.get(0);
                p1 = new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (p1 == null) {
            p1 = new LatLng(currentLat, currentLon);
        }

        return p1;
    }

    public List<Double> getLatLng() {
        fromString = from.getText().toString();
        toString = to.getText().toString();

        fromLatLng = getLocationFromAddress(fromString);
        toLatLng = getLocationFromAddress(toString);

        fromLat = fromLatLng.latitude;
        fromLng = fromLatLng.longitude;
        toLat = toLatLng.latitude;
        toLng = toLatLng.longitude;

        List<Double> list = new ArrayList<Double>();
        list.add(fromLat);
        list.add(fromLng);
        list.add(toLat);
        list.add(toLng);

        return list;
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest, int secondsToAdd) {
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        long seconds = System.currentTimeMillis() / 1000;
        System.out.println("seconds in UTC: " + seconds);
        seconds += secondsToAdd;
        String secondsString = String.valueOf(seconds);
        System.out.println("new seconds in UTC: " + secondsString);

        // Departure Time
        String departureTime = "departure_time="+secondsString;

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+departureTime;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while ( ( line = br.readLine())  != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        } catch(Exception e) {
            //Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if(result.size() < 1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){	// Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.BLUE);
            }

            //Toast.makeText(getBaseContext(), "Time:"+time + ", Duration:"+duration, Toast.LENGTH_LONG).show();
            //System.out.println("TIME: " + time);

            // Drawing polyline in the Google Map for the i-th route
            googleMap.addPolyline(lineOptions);

            if (time == 0) {
                duration1.setText("Now: " + duration);
            }
            else if (time == 1800) {
                duration2.setText("In 30 mins: " + duration);
            }
            else if (time == 3600) {
                duration3.setText("In 1 hour: " + duration);
            }
            else if (time == 5400) {
                duration4.setText("In 90 mins: " + duration);
            }

            if (time < 5400) {
                DownloadTask downloadTask = new DownloadTask();
                time += 1800; // increament by 30 minutes
                String url = getDirectionsUrl(origin, destination, time);
                downloadTask.execute(url);
            }
            else {
                time = 0;
                /*
                currentLat = 43.6532;
                currentLon = -79.3832;
                */
                StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                String type = "cafe";
                googlePlacesUrl.append("location=" + currentLat + "," + currentLon);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&types=" + type);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + serverKey);

                GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
                Object[] toPass = new Object[2];
                toPass[0] = googleMap;
                toPass[1] = googlePlacesUrl.toString();
                googlePlacesReadTask.execute(toPass);
            }
        }
    }
}