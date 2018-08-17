package in.keralafloodrelief;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import http.HttpRequest;

public class RecentRequests extends AppCompatActivity {


    final int MY_PERMISSIONS_REQUEST_1 = 100;
    RecyclerView mRecyclerView;
    SwipeRefreshLayout srlist;
    SingleShotLocationProvider.GPSCoordinates location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = findViewById(R.id.rv_list);
        srlist = findViewById(R.id.srlist);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getBaseContext());
        mRecyclerView.setLayoutManager(mLayoutManager);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(RecentRequests.this);
                builder1.setTitle("This is only for people in emergency");
                builder1.setMessage(" Are you sure ?");
                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(RecentRequests.this, SendRequest.class));
                            }
                        });
                builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder1.create().show();
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_1);
        } else {
            configureMyLocation();
        }


        FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Please add your STD code before this number", Snackbar.LENGTH_LONG)
                        .setAction("Continue", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:1077"));
                                startActivity(intent);
                            }
                        }).show();
            }
        });

        srlist.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                configureMyLocation();
            }
        });

//        configureMyLocation();
    }


    private void configureMyLocation() {
        SingleShotLocationProvider.requestSingleUpdate(RecentRequests.this,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override
                    public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates mlocation) {
                        RecentRequests.this.location = mlocation;
                        getRequests();
                    }

                    @Override
                    public void onDisabled() {
                        RecentRequests.this.location = new SingleShotLocationProvider.GPSCoordinates(-1, -1);
                        getRequests();
                        Toast.makeText(getBaseContext(), "GPS Disabled. Please enable it and reload.", Toast.LENGTH_LONG).show();
                    }
                });
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
                    Toast.makeText(RecentRequests.this, "Location Permission denied.", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
    }


    private void getRequests() {

        new AsyncTask<String, Void, String>() {
            JSONObject result = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                srlist.setRefreshing(true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                srlist.setRefreshing(false);


                if (result != null) {

                    try {
                        if (result.getInt("status") == 1) {
                            RecyclerView.Adapter mAdapter = new RequestShowAdapter(result.getJSONArray("requests"));
                            mRecyclerView.setAdapter(mAdapter);
                            mRecyclerView.setVisibility(View.VISIBLE);
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

                    data.put("key", BuildConfig.APIKey);
                    data.put("latitude", String.valueOf(location.latitude));
                    data.put("longitude", String.valueOf(location.longitude));
                    HttpRequest request = HttpRequest.post(getResources().getString(R.string.api_base_url) + "get_req");
                    request.form(data).created();
                    if (request.ok()) {
                        result = new JSONObject(request.body());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }


                return null;
            }
        }.execute();
    }

    public class RequestShowAdapter extends RecyclerView.Adapter<RequestShowAdapter.ViewHolder> {
        private JSONArray mDataset;

        public RequestShowAdapter(JSONArray myDataset) {
            mDataset = myDataset;
        }

        @Override
        public RequestShowAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_view_request, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                final JSONObject item = mDataset.getJSONObject(position);
                holder.title.setText(item.getString("name"));
                holder.phone.setText(item.getString("phone"));
                holder.added.setText(item.getString("added_on"));
                holder.priority.setText(item.getString("priority"));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(RecentRequests.this, ShowReuest.class);
                        intent.putExtra("item", item.toString());
                        startActivity(intent);

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public int getItemCount() {
            return mDataset.length();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title, added, priority, phone;

            public ViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.tv_name);
                added = v.findViewById(R.id.tv_added);
                priority = v.findViewById(R.id.tv_priority);
                phone = v.findViewById(R.id.tv_phone);
            }
        }
    }
}
