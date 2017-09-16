package com.example.hayden.receipt_tracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ReceiptDetail extends AppCompatActivity implements OnMapReadyCallback {

    private DBHandler dbHandler;
    private ImageView photoView;
    private TextView desc;
    private TextView amount;
    private TextView date;
    private TextView category;
    private TextView project;
    private CheckBox tax;
    private CheckBox reimburse;
    private int id;
    private LatLng coordinates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_detail);

        dbHandler = DBHandler.getInstance(this);

        photoView =  (ImageView) findViewById(R.id.photo);
        desc = (TextView) findViewById(R.id.desc);
        amount = (TextView) findViewById(R.id.amount);
        date = (TextView) findViewById(R.id.date);
        category = (TextView) findViewById(R.id.category);
        project = (TextView) findViewById(R.id.project);
        tax = (CheckBox) findViewById(R.id.tax);
        reimburse = (CheckBox) findViewById(R.id.reimburse);

        Intent intent = getIntent();
        id = intent.getIntExtra("receipt-id",0);
        Receipt receipt = dbHandler.getReceiptById(id);


        Bitmap photo = BitmapFactory.decodeFile(receipt.get_photo());

        int height = photo.getHeight();
        int width = photo.getWidth();
        float aspectRatio = (float)height/width;
        int scaledWidth = 500;
        int scaledHeight = (int)(aspectRatio*scaledWidth);


        photoView.setImageBitmap(Bitmap.createScaledBitmap(photo,scaledWidth,scaledHeight,false));
        desc.setText(receipt.get_desc());
        amount.setText(String.valueOf(receipt.get_amount()));
        date.setText(receipt.get_date());
        category.setText(receipt.get_category());
        project.setText(receipt.get_project());
        reimburse.setChecked(receipt.is_reimburse());
        tax.setChecked(receipt.is_tax());
        coordinates = new LatLng(receipt.get_xcoord(),receipt.get_ycoord());



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(coordinates).title("Receipt Location"));
        CameraUpdate center = CameraUpdateFactory.newLatLng(coordinates);
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(12);
        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);
    }

    public void onDeleteButtonClicked(View view)
    {
        dbHandler.deleteReceipt(id);
        Intent resultIntent = new Intent();

        resultIntent.putExtra("refresh", true);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


}
