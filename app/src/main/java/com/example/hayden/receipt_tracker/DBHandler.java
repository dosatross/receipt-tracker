package com.example.hayden.receipt_tracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    private static DBHandler dbInstance; // singleton DBHandler

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "receipt.db";

    private static final String TABLE_RECEIPT = "receipt";
    private static final String COLUMN_RID = "_rid";
    private static final String COLUMN_PHOTO = "_photo";
    private static final String COLUMN_CATEGORY = "_category";
    private static final String COLUMN_PROJECT = "_project";
    private static final String COLUMN_DATE = "_date";
    private static final String COLUMN_AMOUNT = "_amount";
    private static final String COLUMN_DESC = "_desc";
    private static final String COLUMN_XCOORD = "_xcoord";
    private static final String COLUMN_YCOORD = "_ycoord";
    private static final String COLUMN_TAX = "_tax";
    private static final String COLUMN_REIMBURSE = "_reimburse";

    private static final String TABLE_PROJECT = "project";
    private static final String COLUMN_PID = "_pid";
    private static final String COLUMN_PNAME = "_name";

    private static final String TABLE_CATEGORY = "category";
    private static final String COLUMN_CID = "_cid";
    private static final String COLUMN_CNAME = "_name";

    private static final String[] PRESET_CATEGORIES = {"Petrol", "Durable Tooling", "Perishable Equipment", "Sub-contracting", "Travel", "Work Clothing", "Training", "Machine Repairs", "Utilities", "Equipment", "Phone", "Union Fees"};
    private static final String[] PRESET_PROJECTS = {"PRM Elevator Installation", "Rand Escalator Repair", "Decommision PRM Elevator"};

    private DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    //create tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_PROJECT + "(" +
                COLUMN_PID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PNAME + " TEXT " +
                ");";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_CATEGORY + "(" +
                COLUMN_CID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CNAME + " TEXT " +
                ");";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_RECEIPT + "(" +
                COLUMN_RID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PHOTO + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_AMOUNT + " DECIMAL,  " +
                COLUMN_DESC + " TEXT, " +
                COLUMN_PROJECT + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_XCOORD + " DECIMAL, " +
                COLUMN_YCOORD + " DECIMAL, " +
                COLUMN_TAX + " BOOLEAN, " +
                COLUMN_REIMBURSE + " BOOLEAN, " +
                "FOREIGN KEY(" + COLUMN_PROJECT + ") REFERENCES " + TABLE_PROJECT + "(" + COLUMN_PID + ")," +
                "FOREIGN KEY(" + COLUMN_CATEGORY + ") REFERENCES " + TABLE_CATEGORY + "(" + COLUMN_CID + ")" +
                ");";
        db.execSQL(query);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECEIPT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        onCreate(db);
    }

    public static synchronized DBHandler getInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new DBHandler(context.getApplicationContext(),null,null,1);
        }
        return dbInstance;
    }

    public void addPresetCategories() {
        for(int i = 0; i < PRESET_CATEGORIES.length;i++) {
            addCategory(PRESET_CATEGORIES[i]);
        }
    }

    public void addPresetProjects() {
        for(int i = 0; i < PRESET_PROJECTS.length;i++) {
            addProject(PRESET_PROJECTS[i]);
        }
    }

    public void updateColumnTax(int id,boolean value)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TAX,value);
        db.update(TABLE_RECEIPT, values, COLUMN_RID + "=" + id, null);
    }
    public void updateColumnReimburse(int id,boolean value)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REIMBURSE,value);
        db.update(TABLE_RECEIPT, values, COLUMN_RID + "=" + id, null);
    }


    public void addReceipt(Receipt receipt) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTO,receipt.get_photo());
        values.put(COLUMN_CATEGORY,receipt.get_category());
        values.put(COLUMN_PROJECT,receipt.get_project());
        values.put(COLUMN_DATE,receipt.get_date());
        values.put(COLUMN_AMOUNT,receipt.get_amount());
        values.put(COLUMN_DESC,receipt.get_desc());
        values.put(COLUMN_XCOORD,receipt.get_xcoord());
        values.put(COLUMN_YCOORD,receipt.get_ycoord());
        values.put(COLUMN_TAX,receipt.is_tax());
        values.put(COLUMN_REIMBURSE,receipt.is_reimburse());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_RECEIPT,null,values);

        db.close();
    }

    public void addCategory(String category) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CNAME,category);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_CATEGORY,null,values);
        db.close();
    }

    public void addProject(String project) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PNAME,project);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_PROJECT,null,values);
        db.close();
    }

    public void deleteReceipt(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_RECEIPT + " WHERE " + COLUMN_RID + "=\"" + id + "\";");
    }

    public void deleteCategory(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CATEGORY + " WHERE " + COLUMN_CID + "=\"" + id + "\";");
    }

    public void deleteProject(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PROJECT + " WHERE " + COLUMN_PID + "=\"" + id + "\";");
    }

    public String receiptTableToString() {
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_RECEIPT;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        //Position after the last row means the end of the results
        while (!cursor.isAfterLast()) {
            // null could happen if we used our empty constructor
            if (cursor.getString(cursor.getColumnIndex(COLUMN_DESC)) != null) {
                dbString += cursor.getString(cursor.getColumnIndex(COLUMN_DESC)) + ": " + cursor.getString(cursor.getColumnIndex(COLUMN_AMOUNT)) + "\n";
            }
            cursor.moveToNext();
        }

        db.close();
        return dbString;
    }

    public String[] getFinYears()
    {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT DISTINCT " +
                " CASE WHEN strftime('%m'," + COLUMN_DATE + ")>=7 THEN " +
                "          (strftime('%Y'," + COLUMN_DATE + ") || '-' || (strftime('%Y'," + COLUMN_DATE + ")+1)) " +
                "   ELSE (strftime('%Y'," + COLUMN_DATE + ")-1) || '-' || strftime('%Y'," + COLUMN_DATE + ") END AS financial_year " +
                "FROM " + TABLE_RECEIPT;
        Cursor cursor = db.rawQuery(query, null);
        String[] finYears = new String[cursor.getCount()];
        cursor.moveToFirst();

        int i = 0;
        while (!cursor.isAfterLast())
        {
            if (cursor.getString(0) != null)
            {
                finYears[i] = cursor.getString(0);
            }
            cursor.moveToNext();
            i++;
        }
        db.close();
        return finYears;
    }

    public String[] getProjects()
    {
        return getColumn(TABLE_PROJECT, COLUMN_PNAME);
    }

    public String[] getCategories()
    {
        return getColumn(TABLE_CATEGORY, COLUMN_CNAME);
    }

    public String[] getColumn(String table, String column)
    {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT " + column + " FROM " + table;
        Cursor cursor = db.rawQuery(query, null);
        String[] receiptDescriptions = new String[cursor.getCount()];
        cursor.moveToFirst();

        int i = 0;
        while (!cursor.isAfterLast())
        {
            if (cursor.getString(cursor.getColumnIndex(column)) != null)
            {
                receiptDescriptions[i] = cursor.getString(cursor.getColumnIndex(column));
            }
            cursor.moveToNext();
            i++;
        }

        db.close();
        return receiptDescriptions;
    }

    public ArrayList<Receipt> getReceipts()
    {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_RECEIPT;
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Receipt> receipts = new ArrayList<Receipt>();
        cursor.moveToFirst();

        int i = 0;
        while (!cursor.isAfterLast())
        {
            if (cursor.getString(cursor.getColumnIndex(COLUMN_RID)) != null)
            {
                Receipt receipt = new Receipt();

                receipt.set_id(cursor.getInt(cursor.getColumnIndex(COLUMN_RID)));
                receipt.set_desc(cursor.getString(cursor.getColumnIndex(COLUMN_DESC)));
                receipt.set_category(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
                receipt.set_project(cursor.getString(cursor.getColumnIndex(COLUMN_PROJECT)));
                receipt.set_amount(cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT)));
                receipt.set_date(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                receipt.set_photo(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO)));
                receipt.set_xcoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_XCOORD)));
                receipt.set_ycoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_YCOORD)));
                receipt.set_tax(cursor.getInt(cursor.getColumnIndex(COLUMN_TAX)) > 0);
                receipt.set_reimburse(cursor.getInt(cursor.getColumnIndex(COLUMN_REIMBURSE)) > 0);
                receipts.add(receipt);
            }
            cursor.moveToNext();
            i++;
        }

        db.close();
        return receipts;
    }


    public ArrayList<Receipt> getReceiptsByCategory(String category)
    {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_RECEIPT + " WHERE " + COLUMN_CATEGORY + "=\"" + category + "\"";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Receipt> receipts = new ArrayList<Receipt>();
        cursor.moveToFirst();

        int i = 0;
        while (!cursor.isAfterLast())
        {
            if (cursor.getString(cursor.getColumnIndex(COLUMN_RID)) != null)
            {
                Receipt receipt = new Receipt();

                receipt.set_id(cursor.getInt(cursor.getColumnIndex(COLUMN_RID)));
                receipt.set_desc(cursor.getString(cursor.getColumnIndex(COLUMN_DESC)));
                receipt.set_category(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
                receipt.set_project(cursor.getString(cursor.getColumnIndex(COLUMN_PROJECT)));
                receipt.set_amount(cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT)));
                receipt.set_date(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                receipt.set_photo(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO)));
                receipt.set_xcoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_XCOORD)));
                receipt.set_ycoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_YCOORD)));
                receipt.set_tax(cursor.getInt(cursor.getColumnIndex(COLUMN_TAX)) > 0);
                receipt.set_reimburse(cursor.getInt(cursor.getColumnIndex(COLUMN_REIMBURSE)) > 0);
                receipts.add(receipt);
            }
            cursor.moveToNext();
            i++;
        }

        db.close();
        return receipts;
    }

    public ArrayList<Receipt> getReceiptsByProject(String project)
    {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_RECEIPT + " WHERE " + COLUMN_PROJECT + "=\"" + project + "\"";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Receipt> receipts = new ArrayList<Receipt>();
        cursor.moveToFirst();

        int i = 0;
        while (!cursor.isAfterLast())
        {
            if (cursor.getString(cursor.getColumnIndex(COLUMN_RID)) != null)
            {
                Receipt receipt = new Receipt();

                receipt.set_id(cursor.getInt(cursor.getColumnIndex(COLUMN_RID)));
                receipt.set_desc(cursor.getString(cursor.getColumnIndex(COLUMN_DESC)));
                receipt.set_category(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
                receipt.set_project(cursor.getString(cursor.getColumnIndex(COLUMN_PROJECT)));
                receipt.set_amount(cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT)));
                receipt.set_date(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                receipt.set_photo(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO)));
                receipt.set_xcoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_XCOORD)));
                receipt.set_ycoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_YCOORD)));
                receipt.set_tax(cursor.getInt(cursor.getColumnIndex(COLUMN_TAX)) > 0);
                receipt.set_reimburse(cursor.getInt(cursor.getColumnIndex(COLUMN_REIMBURSE)) > 0);
                receipts.add(receipt);
            }
            cursor.moveToNext();
            i++;
        }

        db.close();
        return receipts;
    }

    public ArrayList<Receipt> getReceiptsByFinYear(String endDate)
    {
        SQLiteDatabase db = getWritableDatabase();

        int startDateInt = Integer.parseInt(endDate);
        String startDate = String.valueOf(startDateInt - 1);
        String query = "SELECT * FROM " + TABLE_RECEIPT + " WHERE " + COLUMN_DATE + ">\"" + startDate + "\"" + " AND " + COLUMN_DATE + "<\"" + endDate + "\"";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Receipt> receipts = new ArrayList<Receipt>();
        cursor.moveToFirst();

        int i = 0;
        while (!cursor.isAfterLast())
        {
            if (cursor.getString(cursor.getColumnIndex(COLUMN_RID)) != null)
            {
                Receipt receipt = new Receipt();

                receipt.set_id(cursor.getInt(cursor.getColumnIndex(COLUMN_RID)));
                receipt.set_desc(cursor.getString(cursor.getColumnIndex(COLUMN_DESC)));
                receipt.set_category(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
                receipt.set_project(cursor.getString(cursor.getColumnIndex(COLUMN_PROJECT)));
                receipt.set_amount(cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT)));
                receipt.set_date(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                receipt.set_photo(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO)));
                receipt.set_xcoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_XCOORD)));
                receipt.set_ycoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_YCOORD)));
                receipt.set_tax(cursor.getInt(cursor.getColumnIndex(COLUMN_TAX)) > 0);
                receipt.set_reimburse(cursor.getInt(cursor.getColumnIndex(COLUMN_REIMBURSE)) > 0);
                receipts.add(receipt);
            }
            cursor.moveToNext();
            i++;
        }

        db.close();
        return receipts;
    }


    public Receipt getReceiptById(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_RECEIPT + " WHERE " + COLUMN_RID + "=" + id;
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        Receipt receipt = new Receipt();

        if (cursor.getString(cursor.getColumnIndex(COLUMN_RID)) != null)
        {
            receipt.set_id(cursor.getInt(cursor.getColumnIndex(COLUMN_RID)));
            receipt.set_desc(cursor.getString(cursor.getColumnIndex(COLUMN_DESC)));
            receipt.set_category(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
            receipt.set_project(cursor.getString(cursor.getColumnIndex(COLUMN_PROJECT)));
            receipt.set_amount(cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT)));
            receipt.set_date(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
            receipt.set_photo(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO)));
            receipt.set_xcoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_XCOORD)));
            receipt.set_ycoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_YCOORD)));
            receipt.set_tax(cursor.getInt(cursor.getColumnIndex(COLUMN_TAX)) > 0);
            receipt.set_reimburse(cursor.getInt(cursor.getColumnIndex(COLUMN_REIMBURSE)) > 0);
        }

        db.close();
        return receipt;
    }

    public ArrayList<Receipt> searchKeyword(String keyword)
    {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_RECEIPT + " WHERE " + COLUMN_DESC + " LIKE \"%" + keyword + "%\"";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Receipt> receipts = new ArrayList<Receipt>();
        cursor.moveToFirst();

        int i = 0;
        while (!cursor.isAfterLast())
        {
            if (cursor.getString(cursor.getColumnIndex(COLUMN_RID)) != null)
            {
                Receipt receipt = new Receipt();

                receipt.set_id(cursor.getInt(cursor.getColumnIndex(COLUMN_RID)));
                receipt.set_desc(cursor.getString(cursor.getColumnIndex(COLUMN_DESC)));
                receipt.set_category(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
                receipt.set_project(cursor.getString(cursor.getColumnIndex(COLUMN_PROJECT)));
                receipt.set_amount(cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT)));
                receipt.set_date(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                receipt.set_photo(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO)));
                receipt.set_xcoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_XCOORD)));
                receipt.set_ycoord(cursor.getFloat(cursor.getColumnIndex(COLUMN_YCOORD)));
                receipt.set_tax(cursor.getInt(cursor.getColumnIndex(COLUMN_TAX)) > 0);
                receipt.set_reimburse(cursor.getInt(cursor.getColumnIndex(COLUMN_REIMBURSE)) > 0);
                receipts.add(receipt);
            }
            cursor.moveToNext();
            i++;
        }

        db.close();
        return receipts;
    }



    public void resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        onUpgrade(db,DATABASE_VERSION,DATABASE_VERSION);
    }


}
