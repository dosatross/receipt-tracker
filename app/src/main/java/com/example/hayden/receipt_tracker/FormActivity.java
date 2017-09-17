package com.example.hayden.receipt_tracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FormActivity extends AppCompatActivity {

    private static final int SCALED_WIDTH = 1000;
    private static final String END_LINE = "\n";

    private String photoUrl;
    private Bitmap photo;

    //DB
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Bundle extras = getIntent().getExtras();
        photoUrl = (String) extras.get("photoUrl");
        photo = BitmapFactory.decodeFile(photoUrl);

        imageView = (ImageView) findViewById(R.id.imageView);
        projectInput = (Spinner) findViewById(R.id.projectInput);
        categoryInput = (Spinner) findViewById(R.id.categoryInput);
        amountInput = (EditText) findViewById(R.id.amountInput);
        descInput = (EditText) findViewById(R.id.descInput);
        taxInput = (CheckBox) findViewById(R.id.taxInput);
        reimburseInput = (CheckBox) findViewById(R.id.reimburseInput);

        dbHandler = DBHandler.getInstance(this);

        renderPhoto();
        renderSpinners();
        setUpLocationService();
        parsePhoto();
    }

    protected void parsePhoto()
    {
        //recognises text in photo and renders to gui elements
        //cannot parse real receipts - only demonstration receipts
        String text = null;

        Frame frame = new Frame.Builder().setBitmap(photo).build();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        SparseArray<TextBlock> items = textRecognizer.detect(frame);
        for (int i = 0; i < items.size(); ++i)
        {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null)
            {
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
                    inputText = text.substring(match.end()).split(END_LINE)[0];
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

    protected void setUpLocationService()
    {
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {} //redundant (already checked in ActivityMain); must check again for location
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
    }

    protected void renderPhoto()
    {
        int height = photo.getHeight();
        int width = photo.getWidth();
        float aspectRatio = (float) height/width;
        int scaledHeight = (int)(aspectRatio*SCALED_WIDTH);
        imageView.setImageBitmap(Bitmap.createScaledBitmap(photo,SCALED_WIDTH,scaledHeight,false));
    }

    protected void renderSpinners()
    {
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
    }

    protected void addReceiptButtonClicked(View view)
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {} //redundant (already checked in ActivityMain); must check again for location
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

    protected int getSpinnerIndexByValue(Spinner spinner, String myString)
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
}
