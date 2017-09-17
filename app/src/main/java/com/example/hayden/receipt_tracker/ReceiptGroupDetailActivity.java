package com.example.hayden.receipt_tracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class ReceiptGroupDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final float PADDING_MULTIPLIER = (float) 0.20;

    private DBHandler dbHandler;
    private String group;
    private String groupInstance;
    private TextView title;
    private ArrayList<Receipt> receipts;
    private LatLng coordinates;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_group_detail);

        title = (TextView) findViewById(R.id.title);
        listView = (ListView)  findViewById(R.id.listView);


        dbHandler = DBHandler.getInstance(this);

        parseIntent();
        title.setText(groupInstance);
        viewReceiptList();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),ReceiptDetailActivity.class);
                Receipt receipt = (Receipt) parent.getItemAtPosition(position);
                intent.putExtra("receipt-id",receipt.get_id());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        List<MarkerOptions> markers = new ArrayList<MarkerOptions>();
        for(int i = 0; i < receipts.size();i++)
        {
            coordinates = new LatLng(receipts.get(i).get_xcoord(),receipts.get(i).get_ycoord());

            markers.add(new MarkerOptions().position(coordinates).title(receipts.get(i).get_desc()));
            googleMap.addMarker(markers.get(i));
            CameraUpdateFactory.newLatLng(coordinates);

        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng position;
        for(int i = 0; i < markers.size(); i++){
            position = markers.get(i).getPosition();
            builder.include(new LatLng(position.latitude, position.longitude));
        }
        LatLngBounds bounds = builder.build();

        final int zoomWidth = getResources().getDisplayMetrics().widthPixels;
        final int zoomHeight = getResources().getDisplayMetrics().heightPixels;
        final int zoomPadding = (int) (zoomWidth * PADDING_MULTIPLIER);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, zoomWidth, zoomHeight, zoomPadding));
    }

    protected void viewReceiptList()
    {
        if(group.equals("category"))
        {
            receipts = dbHandler.getReceiptsByCategory(groupInstance);
        }
        else if (group.equals("project"))
        {
            receipts = dbHandler.getReceiptsByProject(groupInstance);
        }
        else if (group.equals("finyear"))
        {
            receipts = dbHandler.getReceiptsByFinYear(groupInstance);
        }
        ReceiptAdapter adapter = new ReceiptAdapter(this,receipts);
        listView.setAdapter(adapter);
    }

    protected void parseIntent()
    {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        group = extras.getString("group");
        if(group.equals("category"))
        {
            groupInstance = extras.getString("category");
        }
        else if (group.equals("project"))
        {
            groupInstance = extras.getString("project");
        }
        else if (group.equals("finyear"))
        {
            groupInstance = extras.getString("finyear");
        }
    }

    protected void onDeleteButtonClicked(View view)
    {
        for(int i = 0; i < receipts.size();i++)
        {
            dbHandler.deleteReceipt(receipts.get(i).get_id());
        }

        if(group.equals("category"))
        {
            dbHandler.deleteCategory(groupInstance);
        }
        else if (group.equals("project"))
        {
            dbHandler.deleteProject(groupInstance);
        }

        Intent resultIntent = new Intent();

        resultIntent.putExtra("refresh", true);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
