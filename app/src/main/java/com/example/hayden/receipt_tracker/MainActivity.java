package com.example.hayden.receipt_tracker;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.BaseAdapter;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v4.app.Fragment;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import static com.example.hayden.receipt_tracker.R.id.textView;



/*
* TODO
* Features:
* - Auto categories: if desc and coord matches with database then category = "blah"
*   - onEnterListener on EditText
* - Internal camera
*
*
* Implement:
* - onclicklistener for group receipt details
* - save photo location
* - photo on receipt details
* - refresh listview on delete
* - location listener so that location doesnt have null
* - view all receipts on search close
* - clean up and styalise gui / item layout
* - on details page: tags modify db entry
* - searchview: focus on action icon click
*
*
* Possible changes:
* - JOIN for category/project?
* - new tables: settings and finyear?
*
* Bugs:
*
* Clean Up:
* - remove preset projects
* - remove receipt entries
* - refactor (ESPECIALLY DBHandler)
* - search for TODOs
*
* */

public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private DBHandler dbHandler;
    private enum ListViewStatus {ALL,CATEGORY,PROJECT,FINYEAR}

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        listView = (ListView)  findViewById(R.id.listViewMain);
        editText = (EditText)  findViewById(R.id.editText);

        setSupportActionBar(toolbar);


        dbHandler = DBHandler.getInstance(this);

        //set up preset categories
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        dbHandler.resetDatabase(); isFirstRun = true; //reset system

        if (isFirstRun)
        {
            dbHandler.addPresetCategories();
            dbHandler.addPresetProjects();
            Receipt receipt1 = new Receipt("fwefef","Project 1","Category 1","2017-06-01",(float)50.0,"Receipt 1",(float)-37.867470,(float)145.101451,false,false);
            Receipt receipt2 = new Receipt("fwefef","Project 3","Category 2","2017-07-01",(float)90.0,"Receipt 2",(float)-37.865742,(float)145.101354,true,false);
            Receipt receipt3 = new Receipt("fwefef","Project 2","Category 1","2017-04-01",(float)430.0,"Receipt 3",(float)-37.807495,(float)144.981010,false,false);
            Receipt receipt4 = new Receipt("fwefef","Project 1","Category 3","2016-02-01",(float)20.0,"Receipt 4",(float)-37.819032,(float)144.953472,true,true);
            Receipt receipt5 = new Receipt("fwefef","Project 1","Category 1","2014-06-01",(float)250.0,"Receipt 5",(float)-37.860070,(float)145.101051,false,false);
            Receipt receipt6 = new Receipt("fwefef","Project 3","Category 2","2017-07-03",(float)190.0,"Receipt 6",(float)-37.865742,(float)145.101954,true,false);
            Receipt receipt7 = new Receipt("fwefef","Project 2","Category 1","2012-04-01",(float)340.0,"Receipt 7",(float)-37.803495,(float)144.984810,false,false);
            Receipt receipt8 = new Receipt("fwefef","Project 1","Category 3","2016-02-01",(float)205.0,"Receipt 8",(float)-37.813032,(float)144.955472,true,true);
            dbHandler.addReceipt(receipt1);
            dbHandler.addReceipt(receipt2);
            dbHandler.addReceipt(receipt3);
            dbHandler.addReceipt(receipt4);
            dbHandler.addReceipt(receipt5);
            dbHandler.addReceipt(receipt6);
            dbHandler.addReceipt(receipt7);
            dbHandler.addReceipt(receipt8);

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
                }
                else if(status == ListViewStatus.PROJECT)
                {
                    String groupInstance = (String) parent.getItemAtPosition(position);
                    extras.putString("group","project");
                    extras.putString("project",groupInstance);
                }
                else if(status == ListViewStatus.FINYEAR)
                {
                    String groupInstance = (String) parent.getItemAtPosition(position);
                    extras.putString("group","finyear");
                    extras.putString("finyear",groupInstance.substring(groupInstance.lastIndexOf("-") + 1));
                }
                else
                {
                    intent = new Intent(getApplicationContext(),ReceiptDetail.class);
                    Receipt receipt = (Receipt) parent.getItemAtPosition(position);
                    intent.putExtra("receipt-id",receipt.get_id());
                }
                intent.putExtras(extras);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        viewReceiptList();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        switch (id) {
            case R.id.action_settings:

                return true;
            case R.id.menuFilterAll:
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


            status = ListViewStatus.ALL; //TODO change this

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

    public void launchCamera(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Take a picture and pass results along to onActivityResult
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    //Get the photo and print to camera activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Intent formIntent = new Intent(this,Form.class);

            //unpack photo from intent data
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            formIntent.putExtra("data",photo);

            startActivity(formIntent); //start activity
        }
    }



    public void viewReceiptList()
    {
        //receipts = dbHandler.getReceipts();
        //receiptAdapter = new ReceiptAdapter(this,receipts);

        listView.setAdapter(receiptAdapter);
        status = ListViewStatus.ALL;
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
        viewReceiptList();
    }

    public void sortAmountDesc()
    {
        Collections.sort(receipts, Collections.<Receipt>reverseOrder(Receipt.getAmountComparator()));
        viewReceiptList();
    }

    public void sortDateAsc()
    {
        Collections.sort(receipts, Receipt.getDateComparator());
        viewReceiptList();
    }

    public void sortDateDesc()
    {
        Collections.sort(receipts, Collections.<Receipt>reverseOrder(Receipt.getDateComparator()));
        viewReceiptList();
    }


}
