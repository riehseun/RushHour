package rushhour.com.rushhour;

/**
 * Created by user on 2016-04-02.
 */
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SimpleDirection extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DirectionCallback {
    private Button btnRequestDirection;
    private GoogleMap googleMap;
    private String serverKey = "AIzaSyALQ8AUHj3Y5PeSwUVjE-aN3KnaLqlnK-w";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simpledirection);

        btnRequestDirection = (Button) findViewById(R.id.btn_request_direction);
        btnRequestDirection.setOnClickListener(this);

        currentLat = 43.6532;
        currentLon = -79.3832;
        /*
        startLat = 43.6532;
        startLon = -79.3832;
        endLat = 43.6552;
        endLon = -79.3836;
        */
        camera = new LatLng(currentLat, currentLon);
        origin = new LatLng(startLat, startLon);
        destination = new LatLng(endLat, endLon);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                //getLatLng();
                List<Double> list = new ArrayList<Double>();
                list = getLatLng();
                startLat = list.get(0);
                startLon = list.get(1);
                endLat = list.get(2);
                endLon = list.get(3);

                System.out.println(startLat);
                System.out.println(startLon);
                System.out.println(endLat);
                System.out.println(endLon);

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
        {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 15));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_request_direction) {
            requestDirection();
        }
    }

    public void requestDirection() {
        Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        if (direction.isOK()) {
            googleMap.addMarker(new MarkerOptions().position(origin));
            googleMap.addMarker(new MarkerOptions().position(destination));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));

            btnRequestDirection.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Snackbar.make(btnRequestDirection, t.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address != null && address.size() > 0) {
                Address location = address.get(0);
                p1 = new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (p1 == null) {
            p1 = new LatLng(19.111258, 72.908313);
        }
        return p1;
    }

    public List<Double> getLatLng() {
        from = (EditText)findViewById(R.id.from);
        to = (EditText)findViewById(R.id.to);
        fromString = from.getText().toString();
        toString = to.getText().toString();

        //System.out.println(fromString);
        //System.out.println(toString);

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
}