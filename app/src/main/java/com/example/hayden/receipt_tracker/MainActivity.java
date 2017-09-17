package com.example.hayden.receipt_tracker;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


/*
* TODO
* Features:
* - Internal camera
* - settings page: reset database to
*
* Implement:
* - category and project spinner first value
* - db: check if group already exists
* - change photos directory to pictures/receipts not pictures
* - group lists in order
*
*
* Possible changes:
* - JOIN for category/project?
* - new tables: settings and finyear?
* - manual enter of location if they enter receipt away from store location
* - max words description
*
* Bugs:
* - crashes when searching on category list
* - crashes when delete all receipts from a project then view the project
* - some individual receipt map markers are off centre
*
* Clean Up:
* - refactor DBHandler
*
*
* */

public class MainActivity extends AppCompatActivity{

    //constants
    private static final String PHOTO_SUFFIX = ".jpg";

    //permission request codes
    private static final int PERMISSIONS_REQUEST_CODE_FINE_LOCATION = 101;
    private static final int PERMISSIONS_REQUEST_CODE_COARSE_LOCATION = 102;
    private static final int PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 103;
    private static final int PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE = 104;


    //intent request codes
    private static final int REFRESH_ON_INTENT_RESULT_REQUEST_CODE = 1;
    private static final int ACTION_IMAGE_CAPTURE = 2;

    private DBHandler dbHandler;
    private enum ListViewState {RECEIPTS,CATEGORY,PROJECT,FINYEAR}
    private String photoUrl;
    private File photoDir;

    private Toolbar toolbar;
    private ListView listView;
    private ArrayList<Receipt> receipts;
    private ReceiptAdapter receiptAdapter;
    private EditText editText;
    private ListViewState listState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions(); //ask user for permissions

        //UI init
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        listView = (ListView)  findViewById(R.id.listViewMain);
        editText = (EditText)  findViewById(R.id.editText);
        setSupportActionBar(toolbar);

        photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); //initialise receipt photos directory

        dbHandler = DBHandler.getInstance(this); //get db
        setUpData(); //reset/populate database

        setUpListView(); //view receipt list and set up onClickListener
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        setUpSearchBar(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //parses menu items selected in action bar
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:

                return true;
            case R.id.menuFilterReceipts:
                viewReceiptList();
                sortDateDesc();
                return true;
            case R.id.menuFilterCategory:
                viewCategoryList();
                return true;
            case R.id.menuFilterProject:
                viewProjectList();
                return true;
            case R.id.menuFilterFinYear:
                viewFinYearList();
                return true;
            case R.id.menuSortAmountAsc:
                sortAmountAsc();
                return true;
            case R.id.menuSortAmountDesc:
                sortAmountDesc();
                return true;
            case R.id.menuSortDateAsc:
                sortDateAsc();
                return true;
            case R.id.menuSortDateDesc:
                sortDateDesc();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onNewIntent(Intent intent){
        setIntent(intent);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            handleSearchIntent(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ACTION_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Intent formIntent = new Intent(this,FormActivity.class);
            formIntent.putExtra("photoUrl",photoUrl);
            startActivityForResult(formIntent,REFRESH_ON_INTENT_RESULT_REQUEST_CODE); //start activity
        }
        if(requestCode == REFRESH_ON_INTENT_RESULT_REQUEST_CODE)
        {
            viewReceiptList();
            sortDateDesc();
        }
    }

    protected void setUpData()
    {
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        dbHandler.resetDatabase(); isFirstRun = true; //reset system

        if (isFirstRun)
        {
            populateDatabase();
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUN", false);
            editor.apply();
        }
    }

    protected void setUpListView()
    {
        receipts = dbHandler.getReceipts();
        receiptAdapter = new ReceiptAdapter(this,receipts);
        viewReceiptList();
        sortDateDesc();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                Intent intent = intent = new Intent(getApplicationContext(),ReceiptGroupDetailActivity.class);
                Bundle extras = new Bundle();

                if(listState == ListViewState.CATEGORY)
                {
                    String groupInstance = (String) parent.getItemAtPosition(position);
                    extras.putString("group","category");
                    extras.putString("category",groupInstance);
                }
                else if(listState == ListViewState.PROJECT)
                {
                    String groupInstance = (String) parent.getItemAtPosition(position);
                    extras.putString("group","project");
                    extras.putString("project",groupInstance);
                }
                else if(listState == ListViewState.FINYEAR)
                {
                    String groupInstance = (String) parent.getItemAtPosition(position);
                    extras.putString("group","finyear");
                    extras.putString("finyear",groupInstance.substring(groupInstance.lastIndexOf("-") + 1));
                }
                else
                {
                    intent = new Intent(getApplicationContext(),ReceiptDetailActivity.class);
                    Receipt receipt = (Receipt) parent.getItemAtPosition(position);
                    intent.putExtra("receipt-id",receipt.get_id());
                }
                intent.putExtras(extras);
                startActivityForResult(intent,REFRESH_ON_INTENT_RESULT_REQUEST_CODE);
            }
        });
    }

    protected void setUpSearchBar(Menu menu)
    {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); //focus on field automatically

        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener()
        {

            @Override
            public void onViewDetachedFromWindow(View view)
            {
                viewReceiptList();
                sortDateDesc(); //view receipt list when search bar is closed
            }

            @Override
            public void onViewAttachedToWindow(View view)
            {
                // search bar opened
            }
        });
    }

    protected void saveDrawableReceipt(int receiptId)
    {
        Bitmap bitmap = BitmapFactory.decodeResource( getResources(), receiptId);
        File photoFile;
        try
        {
            photoFile = createImageFile(); //photo container for camera activity
            // Continue only if the File was successfully created
            if (photoFile != null)
            {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.hayden.receipt_tracker.fileprovider",
                        photoFile);
                FileOutputStream outStream = new FileOutputStream(photoFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }



    protected void handleSearchIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //search
            receipts = dbHandler.searchKeyword(query);
            receiptAdapter = (ReceiptAdapter) listView.getAdapter();

            refreshReceiptList();

        }
    }

    protected void onAddCategoryClicked(View view)
    {
        dbHandler.addCategory(editText.getText().toString());
    }

    protected void onAddProjectClicked(View view)
    {
        dbHandler.addProject(editText.getText().toString());
    }


    protected void dispatchTakePictureIntent(View view) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.hayden.receipt_tracker.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, ACTION_IMAGE_CAPTURE);
            }
        }
    }

    protected File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "RECEIPT_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  // prefix
                PHOTO_SUFFIX,  // suffix
                photoDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoUrl = image.getAbsolutePath();
        return image;
    }

    protected void refreshReceiptList()
    {
        //renders receipts
        receiptAdapter.clear();
        receiptAdapter.notifyDataSetChanged();
        receiptAdapter.addAll(receipts);
        listView.setAdapter(receiptAdapter);

        listState = ListViewState.RECEIPTS;
    }

    public void viewReceiptList()
    {
        receipts = dbHandler.getReceipts();
        refreshReceiptList();
        listState = ListViewState.RECEIPTS;
    }

    protected void viewCategoryList()
    {
        viewStringList(dbHandler.getCategories());
        listState = ListViewState.CATEGORY;
    }

    protected void viewProjectList()
    {
        String[] strings =  dbHandler.getProjects();
        viewStringList(strings);
        listState = ListViewState.PROJECT;
    }

    protected void viewFinYearList()
    {
        viewStringList(dbHandler.getFinYears());
        listState = ListViewState.FINYEAR;
    }

    protected void viewStringList(String[] strings)
    {
        ArrayAdapter<String> stringAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,strings);
        listView.setAdapter(stringAdapter);
    }

    protected void sortAmountAsc()
    {
        Collections.sort(receipts, Receipt.getAmountComparator());
        refreshReceiptList();
    }

    protected void sortAmountDesc()
    {
        Collections.sort(receipts, Collections.<Receipt>reverseOrder(Receipt.getAmountComparator()));
        refreshReceiptList();
    }

    protected void sortDateAsc()
    {
        Collections.sort(receipts, Receipt.getDateComparator());
        refreshReceiptList();
    }

    protected void sortDateDesc()
    {
        Collections.sort(receipts, Collections.<Receipt>reverseOrder(Receipt.getDateComparator()));
        refreshReceiptList();
    }

    protected void checkPermissions()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            askForPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            finish();
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            askForPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            finish();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            askForPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_CODE_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            finish();
        }

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            askForPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION, PERMISSIONS_REQUEST_CODE_COARSE_LOCATION);
        }
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            finish();
        }
    }



    protected void askForPermission(String permission, Integer requestCode)
    {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }


    protected void populateDatabase()
    {
        dbHandler.addPresetCategories();
        dbHandler.addPresetProjects();

        saveDrawableReceipt(R.drawable.receipt_1);
        Receipt receipt1 = new Receipt(photoUrl,"PRM Elevator Installation","Durable Tooling","2017-06-01",(float)365.0,"2pc Cordless Drill Kit",(float)-37.867470,(float)145.101451,true,false);
        saveDrawableReceipt(R.drawable.receipt_2);
        Receipt receipt2 = new Receipt(photoUrl,"PRM Elevator Installation","Durable Tooling","2017-07-01",(float)60.0,"Vernier Caliper 150mm",(float)-37.865742,(float)145.101354,true,false);
        saveDrawableReceipt(R.drawable.receipt_3);
        Receipt receipt3 = new Receipt(photoUrl,"PRM Elevator Installation","Durable Tooling","2017-04-01",(float)100.0,"Platform Trolley",(float)-37.885580,(float)144.998978,false,false);
        saveDrawableReceipt(R.drawable.receipt_4);
        Receipt receipt4 = new Receipt(photoUrl,"PRM Elevator Installation","Durable Tooling","2016-02-01",(float)120.0,"Cordless Palm Orbital Sander",(float)-37.885580,(float)144.998978,true,false);
        saveDrawableReceipt(R.drawable.receipt_5);
        Receipt receipt5 = new Receipt(photoUrl,"PRM Elevator Installation","Perishable Equipment","2014-06-01",(float)250.0,"10G x 50mm Stainless Steel Screws",(float)-37.809253,(float)144.989702,false,false);
        saveDrawableReceipt(R.drawable.receipt_6);
        Receipt receipt6 = new Receipt(photoUrl,"Decommision PRM Elevator","Petrol","2017-07-03",(float)43.0,"Petrol",(float)-37.865742,(float)145.101954,true,false);
        saveDrawableReceipt(R.drawable.receipt_7);
        Receipt receipt7 = new Receipt(photoUrl,"Rand Escalator Repair","Sub-contracting","2012-04-01",(float)550.0,"Myer's Repair",(float)-37.803495,(float)144.984810,false,false);
        saveDrawableReceipt(R.drawable.receipt_8);
        Receipt receipt8 = new Receipt(photoUrl,"Decommision PRM Elevator","Work Clothing","2016-02-01",(float)37.0,"Visibility Jacket",(float)-37.813032,(float)144.955472,true,true);
        dbHandler.addReceipt(receipt1);
        dbHandler.addReceipt(receipt2);
        dbHandler.addReceipt(receipt3);
        dbHandler.addReceipt(receipt4);
        dbHandler.addReceipt(receipt5);
        dbHandler.addReceipt(receipt6);
        dbHandler.addReceipt(receipt7);
        dbHandler.addReceipt(receipt8);
    }


}
