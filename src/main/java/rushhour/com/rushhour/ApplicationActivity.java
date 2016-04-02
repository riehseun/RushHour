package rushhour.com.rushhour;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.maps.model.LatLng;

public class ApplicationActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applicationactivity);

        findViewById(R.id.btn_simple).setOnClickListener(this);
        findViewById(R.id.btn_transit).setOnClickListener(this);
        findViewById(R.id.btn_alternative).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_simple) {
            goToSimpleDirection();
        } else if (id == R.id.btn_transit) {
            goToTransitDirection();
        } else if (id == R.id.btn_alternative) {
            goToAlternativeDirection();
        }
    }

    public void goToSimpleDirection() {
        openActivity(SimpleDirection.class);
    }

    public void goToTransitDirection() {
        openActivity(TransitDirection.class);
    }

    public void goToAlternativeDirection() {
        openActivity(AlternativeDirection.class);
    }

    public void openActivity(Class<?> cs) {
        startActivity(new Intent(this, cs));
    }
}