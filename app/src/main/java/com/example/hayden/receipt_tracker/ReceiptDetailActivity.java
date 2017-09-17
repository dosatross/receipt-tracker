package com.example.hayden.receipt_tracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ReceiptDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String DOLLAR_SIGN = "$";
    private static final int ZOOM = 12;
    private static final int SCALED_WIDTH = 500;

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

        populateGuiElements();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        //render map
        MarkerOptions markerOptions = new MarkerOptions().position(coordinates).title((String) desc.getText());
        googleMap.addMarker(markerOptions);
        CameraUpdate center = CameraUpdateFactory.newLatLng(coordinates);
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZOOM);
        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);
    }

    protected void populateGuiElements()
    {
        Receipt receipt = dbHandler.getReceiptById(id);


        Bitmap photo = BitmapFactory.decodeFile(receipt.get_photo());

        int height = photo.getHeight();
        int width = photo.getWidth();
        float aspectRatio = (float)height/width;
        int scaledWidth = SCALED_WIDTH;
        int scaledHeight = (int)(aspectRatio*scaledWidth);


        photoView.setImageBitmap(Bitmap.createScaledBitmap(photo,scaledWidth,scaledHeight,false));
        desc.setText(receipt.get_desc());
        String amountStr = DOLLAR_SIGN + String.valueOf(receipt.get_amount());
        amount.setText(amountStr);
        date.setText(receipt.get_date());
        category.setText(receipt.get_category());
        project.setText(receipt.get_project());
        reimburse.setChecked(receipt.is_reimburse());
        tax.setChecked(receipt.is_tax());
        coordinates = new LatLng(receipt.get_xcoord(),receipt.get_ycoord());
    }

    protected void onDeleteButtonClicked(View view)
    {
        dbHandler.deleteReceipt(id);
        Intent resultIntent = new Intent();

        resultIntent.putExtra("refresh", true);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


    @Override
    public void onStop()
    {
        super.onStop();
        dbHandler.updateColumnTax(id,tax.isChecked());
        dbHandler.updateColumnReimburse(id,reimburse.isChecked());

    }

}
