package in.keralafloodrelief;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_requests);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = findViewById(R.id.rv_list);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getBaseContext());
        mRecyclerView.setLayoutManager(mLayoutManager);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This is only for people in emergency Are you sure ?", Snackbar.LENGTH_LONG)
                        .setAction("Yes", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(RecentRequests.this, SendRequest.class));
                            }
                        }).show();
            }
        });

        getRequests();


    }


    private void getRequests() {

        new AsyncTask<String, Void, String>() {
            JSONObject result = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);


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

                    data.put("key", getResources().getString(R.string.api_key));

                    HttpRequest request = HttpRequest.post(getResources().getString(R.string.api_base_url) + "get_req");
                    request.form(data).created();
//                    Log.e("======",request.body());
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
                holder.title.setText(mDataset.getJSONObject(position).getString("name"));
                holder.phone.setText(mDataset.getJSONObject(position).getString("phone"));
                holder.added.setText(mDataset.getJSONObject(position).getString("added_on"));
                holder.priority.setText(mDataset.getJSONObject(position).getString("priority"));
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
