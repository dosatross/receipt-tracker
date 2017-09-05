package com.example.hayden.receipt_tracker;


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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class ReceiptGroupDetail extends AppCompatActivity implements OnMapReadyCallback {
    private DBHandler dbHandler;
    private String group;
    private String groupInstance;
    private TextView textView;
    private ArrayList<Receipt> receipts;
    private LatLng coordinates;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_group_detail);

        textView = (TextView) findViewById(R.id.textView);
        listView = (ListView)  findViewById(R.id.listView);

        dbHandler = DBHandler.getInstance(this);

        parseIntent();
        textView.setText(groupInstance);
        viewReceiptList();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),ReceiptDetail.class);
                Receipt receipt = (Receipt) parent.getItemAtPosition(position);
                intent.putExtra("receipt-id",receipt.get_id());
                startActivity(intent);
            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        for(int i = 0; i < receipts.size();i++)
        {
            coordinates = new LatLng(receipts.get(i).get_xcoord(),receipts.get(i).get_ycoord());
            googleMap.addMarker(new MarkerOptions().position(coordinates).title("Receipt Location"));
            CameraUpdateFactory.newLatLng(coordinates);
        }

        //googleMap.moveCamera(center);
        //googleMap.animateCamera(zoom);
    }

    public void viewReceiptList()
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

    public void parseIntent()
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
}
