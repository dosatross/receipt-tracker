package com.example.hayden.receipt_tracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Form extends AppCompatActivity {


    //Database
    private DBHandler dbHandler;

    //GUI
    private ImageView imageView;
    private Spinner projectInput;
    private Spinner categoryInput;
    private EditText amountInput;
    private EditText descInput;
    private CheckBox taxInput;
    private CheckBox reimburseInput;

    //GPS
    private LocationManager locationManager;
    private String provider;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 100;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Bundle extras = getIntent().getExtras();
        Bitmap photo = (Bitmap) extras.get("data");
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(photo);

        projectInput = (Spinner) findViewById(R.id.projectInput);
        categoryInput = (Spinner) findViewById(R.id.categoryInput);
        amountInput = (EditText) findViewById(R.id.amountInput);
        descInput = (EditText) findViewById(R.id.descInput);
        taxInput = (CheckBox) findViewById(R.id.taxInput);
        reimburseInput = (CheckBox) findViewById(R.id.reimburseInput);

        dbHandler = DBHandler.getInstance(this);
        //dbHandler.resetDatabase();
        dbHandler.addPresetCategories();


        // TODO factor this code out of onCreate
        ArrayAdapter<String> categoriesSpinnerArrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                dbHandler.getCategories()
        );
        categoriesSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryInput.setAdapter(categoriesSpinnerArrayAdapter);


        ArrayAdapter<String> projectsSpinnerArrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                dbHandler.getProjects()
        );
        projectsSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        projectInput.setAdapter(projectsSpinnerArrayAdapter);

        //GPS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
    }

    public void addReceiptButtonClicked(View view)
    {
        //Location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            askForPermission(Manifest.permission.ACCESS_COARSE_LOCATION, MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
        }
        Location location = locationManager.getLastKnownLocation(provider);
        float latitude = (float) location.getLatitude();
        float longitude = (float) location.getLongitude();

        //Date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(calendar.getTime());

        Receipt receipt = new Receipt(
                "path/to/photo",
                projectInput.getSelectedItem().toString(),
                categoryInput.getSelectedItem().toString(),
                formattedDate,
                Float.parseFloat(amountInput.getText().toString()),
                descInput.getText().toString(),
                latitude,
                longitude,
                taxInput.isChecked(),
                reimburseInput.isChecked()
        );
        dbHandler.addReceipt(receipt);
        finish();
    }

    public void onListButtonClicked(View view) {
        Intent intent = new Intent(this,Form.class);
        startActivity(intent);
    }


    //ask user for permission: used for gps
    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(Form.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Form.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(Form.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(Form.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }
}
