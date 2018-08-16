package in.keralafloodrelief;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SendRequest extends AppCompatActivity {

    final int MY_PERMISSIONS_REQUEST_1 = 100;

    EditText name, phone, descr;
    Spinner prio;
    Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_1);
        } else {
            configureMyLocation();
        }

        name = findViewById(R.id.et_name);
        phone = findViewById(R.id.et_phone);
        descr = findViewById(R.id.et_decr);
        prio = findViewById(R.id.sp_prio);
        send = findViewById(R.id.btn_send);

        send.setVisibility(View.GONE);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configureMyLocation();
                    return;
                } else {
                    Toast.makeText(SendRequest.this, "Location Permission denied.", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
    }


    private void configureMyLocation() {
        SingleShotLocationProvider.requestSingleUpdate(SendRequest.this,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override
                    public void onNewLocationAvailable(final SingleShotLocationProvider.GPSCoordinates location) {
                        send.setVisibility(View.VISIBLE);


                    }
                });
    }

}
