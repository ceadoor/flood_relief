package in.keralafloodrelief;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class ShowReuest extends AppCompatActivity {

    JSONObject item = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_reuest);
        getSupportActionBar().hide();

        Bundle extras = getIntent().getExtras();

        try {
            item = new JSONObject(getIntent().getStringExtra("item"));
            setupStats();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setupStats() throws JSONException {
        ((TextView) findViewById(R.id.textView)).setText(item.getString("name"));
        ((TextView) findViewById(R.id.textView2)).setText(item.getString("descr"));
        final String qstr = item.getString("latitude") + "," + item.getString("longitude");
        Picasso.get()
                .load("http://maps.googleapis.com/maps/api/staticmap?center=" + item.getString("latitude") + "," + item.getString("longitude") + "&size=400x400&sensor=true&zoom=15")
                .into((ImageView) findViewById(R.id.imageView));

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + qstr);
        final Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(mapIntent);
            }
        });

        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(mapIntent);
            }
        });

        findViewById(R.id.floatingActionButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                try {
                    intent.setData(Uri.parse("tel:" + item.getString("phone")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });


    }
}
