package in.keralafloodrelief;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import http.HttpRequest;

public class SendRequest extends AppCompatActivity implements View.OnClickListener {

    final int MY_PERMISSIONS_REQUEST_1 = 100;

    EditText name, phone, descr;
    Spinner prio;
    Button btn_send;
    SingleShotLocationProvider.GPSCoordinates location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        btn_send = findViewById(R.id.btn_send);

        btn_send.setVisibility(View.GONE);


        btn_send.setVisibility(View.VISIBLE);
        btn_send.setOnClickListener(SendRequest.this);
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
                    public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates mlocation) {
                        SendRequest.this.location = mlocation;

                    }

                    @Override
                    public void onDisabled() {
                        location = new SingleShotLocationProvider.GPSCoordinates(-1, -1);
                        Toast.makeText(getBaseContext(), "GPS Disabled. Unable to determine location.", Toast.LENGTH_LONG).show();
                        new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                                    configureMyLocation();
                                }
                            }
                        };

                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_send) {
            SendThis();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void SendThis() {

        new AsyncTask<String, Void, String>() {
            JSONObject result = null;
            ProgressDialog pd;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd = new ProgressDialog(SendRequest.this);
                pd.setMessage("Sending..");
                pd.setCancelable(false);
                pd.show();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                pd.dismiss();


                if (result != null) {

                    try {
                        if (result.getInt("status") == 1) {

                            Toast.makeText(getBaseContext(), result.getString("msg"), Toast.LENGTH_LONG).show();
                            finish();

                        } else {
                            Toast.makeText(getBaseContext(), result.getString("msg"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), "Data error", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Network error", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(String... strings) {

                try {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("latitude", String.valueOf(location.latitude));
                    data.put("longitude", String.valueOf(location.longitude));
                    data.put("name", name.getText().toString());
                    data.put("phone", phone.getText().toString());
                    data.put("priority", String.valueOf(getResources().getIntArray(R.array.level)[prio.getSelectedItemPosition()]));
                    data.put("descr", descr.getText().toString());
                    data.put("key", BuildConfig.APIKey);

                    HttpRequest request = HttpRequest.post(getResources().getString(R.string.api_base_url) + "send_request");
                    request.form(data).created();

                    if (request.ok()) {
                        result = new JSONObject(request.body());
                    }
                } catch (Exception e) {
                    return null;
                }


                return null;
            }
        }.execute();
    }


}

