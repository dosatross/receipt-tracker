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
import android.util.Log;
import android.widget.ImageView;
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
* - handle if user denies permission
* - delete category/project
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
*
* Clean Up:
* - refactor (ESPECIALLY DBHandler)
* - search for TODOs
* - call activity classes activity
* - order receipts by date by default
*
* */

public class MainActivity extends AppCompatActivity{


    private static final String PHOTO_SUFFIX = ".jpg";

    //permissions
    private static final int ACTION_IMAGE_CAPTURE = 2;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 103;
    private static final int INTENT_REQUEST_CODE = 1;//TODO rename this

    private DBHandler dbHandler;
    private enum ListViewStatus {RECEIPTS,CATEGORY,PROJECT,FINYEAR}
    private String photoUrl;
    private File photoDir;

    private Toolbar toolbar;
    private ListView listView;
    private ArrayList<Receipt> receipts;
    private ReceiptAdapter receiptAdapter;
    private EditText editText;
    private ListViewStatus status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            askForPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            askForPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        listView = (ListView)  findViewById(R.id.listViewMain);
        editText = (EditText)  findViewById(R.id.editText);

        setSupportActionBar(toolbar);


        dbHandler = DBHandler.getInstance(this);

        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        dbHandler.resetDatabase(); isFirstRun = true; //reset system

        photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (isFirstRun)
        {
            populateDatabase();
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUN", false);
            editor.apply();
        }





        receipts = dbHandler.getReceipts();
        receiptAdapter = new ReceiptAdapter(this,receipts);
        viewReceiptList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                Intent intent = intent = new Intent(getApplicationContext(),ReceiptGroupDetail.class);
                Bundle extras = new Bundle();

                if(status == ListViewStatus.CATEGORY)
                {
                    String groupInstance = (String) parent.getItemAtPosition(position);
                    extras.putString("group","category");
                    extras.putString("category",groupInstance);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
                else if(status == ListViewStatus.PROJECT)
                {
                    String groupInstance = (String) parent.getItemAtPosition(position);
                    extras.putString("group","project");
                    extras.putString("project",groupInstance);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
                else if(status == ListViewStatus.FINYEAR)
                {
                    String groupInstance = (String) parent.getItemAtPosition(position);
                    extras.putString("group","finyear");
                    extras.putString("finyear",groupInstance.substring(groupInstance.lastIndexOf("-") + 1));
                    intent.putExtras(extras);
                    startActivity(intent);
                }
                else
                {
                    intent = new Intent(getApplicationContext(),ReceiptDetail.class);
                    Receipt receipt = (Receipt) parent.getItemAtPosition(position);
                    intent.putExtra("receipt-id",receipt.get_id());
                    intent.putExtras(extras);
                    startActivityForResult(intent,INTENT_REQUEST_CODE);
                }


            }
        });

    }

    void saveDrawableReceipt(int receiptId)
    {
        Bitmap bitmap = BitmapFactory.decodeResource( getResources(), receiptId);
        File photoFile = null;
        try {
            photoFile = createImageFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.hayden.receipt_tracker.fileprovider",
                        photoFile);
                FileOutputStream outStream = new FileOutputStream(photoFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

//        MenuItem item = menu.getItem();
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        searchView.setIconifiedByDefault(false);
        ImageView closeButton = (ImageView) findViewById(R.id.search_close_btn);




        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

            @Override
            public void onViewDetachedFromWindow(View view) {
                //search was closed
                viewReceiptList();
            }

            @Override
            public void onViewAttachedToWindow(View arg0) {
                // search was opened
            }
        });




        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        switch (id) {
            case R.id.action_settings:

                return true;
            case R.id.menuFilterReceipts:
                viewReceiptList();
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



    private void handleSearchIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //search
            receipts = dbHandler.searchKeyword(query);
            receiptAdapter = (ReceiptAdapter) listView.getAdapter();

            receiptAdapter.clear();
            receiptAdapter.notifyDataSetChanged();
            receiptAdapter.addAll(receipts);
            listView.setAdapter(receiptAdapter);


            status = ListViewStatus.RECEIPTS; //TODO change this

        }
    }



    public void onAddCategoryClicked(View view)
    {
        dbHandler.addCategory(editText.getText().toString());
    }

    public void onAddProjectClicked(View view)
    {
        dbHandler.addProject(editText.getText().toString());
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "RECEIPT_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                PHOTO_SUFFIX,         /* suffix */
                photoDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoUrl = image.getAbsolutePath();
        return image;
    }

    public void dispatchTakePictureIntent(View view) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
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

    //Get the photo and print to camera activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ACTION_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Intent formIntent = new Intent(this,Form.class);
            formIntent.putExtra("photoUrl",photoUrl);
            startActivityForResult(formIntent,INTENT_REQUEST_CODE); //start activity
        }
        if(requestCode == INTENT_REQUEST_CODE)
        {
            viewReceiptList();
        }
    }



    private void askForPermission(String permission, Integer requestCode) {
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


    public void refreshReceiptList()
    {
        receiptAdapter.clear();
        receiptAdapter.notifyDataSetChanged();

        receiptAdapter.addAll(receipts);
        listView.setAdapter(receiptAdapter);

        status = ListViewStatus.RECEIPTS;
    }

    public void viewReceiptList()
    {
        receipts = dbHandler.getReceipts();
        refreshReceiptList();
        status = ListViewStatus.RECEIPTS;
    }

    public void viewCategoryList()
    {
        viewStringList(dbHandler.getCategories());
        status = ListViewStatus.CATEGORY;
    }

    public void viewProjectList()
    {
        String[] strings =  dbHandler.getProjects();
        viewStringList(strings);
        status = ListViewStatus.PROJECT;
    }

    public void viewFinYearList()
    {
        viewStringList(dbHandler.getFinYears());
        status = ListViewStatus.FINYEAR;
    }

    public void viewStringList(String[] strings)
    {
        ArrayAdapter<String> stringAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,strings);
        listView.setAdapter(stringAdapter);
    }




    public void sortAmountAsc()
    {
        Collections.sort(receipts, Receipt.getAmountComparator());

        refreshReceiptList();
    }

    public void sortAmountDesc()
    {
        Collections.sort(receipts, Collections.<Receipt>reverseOrder(Receipt.getAmountComparator()));
        refreshReceiptList();
    }

    public void sortDateAsc()
    {
        Collections.sort(receipts, Receipt.getDateComparator());
        refreshReceiptList();
    }

    public void sortDateDesc()
    {
        Collections.sort(receipts, Collections.<Receipt>reverseOrder(Receipt.getDateComparator()));
        refreshReceiptList();
    }


    public void populateDatabase()
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
