package com.example.hayden.receipt_tracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
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

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Form extends AppCompatActivity {

    private String photoUrl;

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
    private LocationListener locationListener;
    private String provider;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 100;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 101;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 103;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Bundle extras = getIntent().getExtras();

        photoUrl = (String) extras.get("photoUrl");

        String[] tokens = photoUrl.split("/");
        String photoName = tokens[tokens.length - 1];


        imageView = (ImageView) findViewById(R.id.imageView);


        Bitmap photo = BitmapFactory.decodeFile(photoUrl);

        //print image

        int height = photo.getHeight();
        int width = photo.getWidth();
        float aspectRatio = (float)height/width;
        int scaledWidth = 1000;
        int scaledHeight = (int)(aspectRatio*scaledWidth);


        imageView.setImageBitmap(Bitmap.createScaledBitmap(photo,scaledWidth,scaledHeight,false));

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
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {}
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 2000, 10, locationListener);


        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);


        //OCR
        Bitmap bitmap = null;


        File imgFile = new  File(photoUrl);
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            {
                askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            {
                askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
//            if (imgFile.exists()) {
//                bitmap = BitmapFactory.decodeFile(url);
//                //imageView.setImageBitmap(bitmap);
//            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        String text = null;
        bitmap = photo;

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        SparseArray<TextBlock> items = textRecognizer.detect(frame);
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                Log.d("Processor", "Text detected! " + item.getValue());
                text = item.getValue();
            }
        }

        if(text != null)
        {

            String regex[] = new String[4];
            regex[0] = "Amount: ";
            regex[1] = "Description: ";
            regex[2] = "Category: ";
            regex[3] = "Project: ";

            for (int i = 0; i < regex.length; i++) {
                Pattern pattern = Pattern.compile(regex[i]);
                Matcher match = pattern.matcher(text);

                String inputText;

                while (match.find()) {
                    inputText = text.substring(match.end()).split("\n")[0];
                    switch (i) {
                        case 0: {
                            amountInput.setText(inputText);
                        }
                        case 1: {
                            descInput.setText(inputText);
                        }
                        case 2: {

                            categoryInput.setSelection(getSpinnerIndexByValue(categoryInput, inputText));
                        }
                        case 3: {
                            projectInput.setSelection(getSpinnerIndexByValue(projectInput, inputText));
                        }

                    }

                }
            }
        }

    }

    public void addReceiptButtonClicked(View view)
    {
        if (descInput.getText().toString().trim().equals("")) {
            descInput.setError("Description is required!");
            return;
        }

        if (amountInput.getText().toString().trim().equals("")) {
            amountInput.setError("Amount is required!");
            return;
        }

        //Location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                photoUrl,
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

    private int getSpinnerIndexByValue(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++)
        {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString))
            {
                index = i;
                break;
            }
        }
        return index;
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
