package com.example.hayden.receipt_tracker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;


public class Receipt {
    private int _id;
    private String _photo;
    private String _project;
    private String _category;
    private String _date;
    private float _amount;
    private String _desc;
    private float _xcoord;
    private float _ycoord;
    private boolean _tax;
    private boolean _reimburse;

    static Comparator<Receipt> getDateComparator() {
        return new Comparator<Receipt>() {
            public int compare(Receipt one, Receipt two) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date dateOne = null;
                Date dateTwo = null;
                try {
                    dateOne = df.parse(one.get_date());
                    dateTwo = df.parse(two.get_date());
                }catch (ParseException e) {
                    e.printStackTrace();
                }
                int dateCompare = dateOne.compareTo(dateTwo);
                if(dateCompare > 0)
                {
                    return 1;
                }
                else if(dateCompare < 0)
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        };
    }

    static Comparator<Receipt> getAmountComparator() {
        return new Comparator<Receipt>() {
            public int compare(Receipt one, Receipt two) {

                float amountOne = one.get_amount();
                float amountTwo = two.get_amount();
                if(amountOne > amountTwo)
                {
                    return 1;
                }
                else if(amountOne < amountTwo)
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        };
    }

    public Receipt() {

    }

    public Receipt(String _photo, String _project, String _category, String _date, float _amount, String _desc, float _xcoord, float _ycoord, boolean _tax, boolean _reimburse) {
        this._photo = _photo;
        this._project = _project;
        this._category = _category;
        this._date = _date;
        this._amount = _amount;
        this._desc = _desc;
        this._xcoord = _xcoord;
        this._ycoord = _ycoord;
        this._tax = _tax;
        this._reimburse = _reimburse;
    }

    public int get_id() {
        return _id;
    }

    public String get_photo() {
        return _photo;
    }

    public String get_project() {
        return _project;
    }

    public String get_category() {
        return _category;
    }

    public String get_date() {
        return _date;
    }

    public float get_amount() {
        return _amount;
    }

    public String get_desc() {
        return _desc;
    }

    public float get_xcoord() {
        return _xcoord;
    }

    public float get_ycoord() {
        return _ycoord;
    }

    public boolean is_tax() {
        return _tax;
    }

    public boolean is_reimburse() {
        return _reimburse;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void set_photo(String _photo) {
        this._photo = _photo;
    }

    public void set_project(String _project) {
        this._project = _project;
    }

    public void set_category(String _category) {
        this._category = _category;
    }

    public void set_date(String _date) {
        this._date = _date;
    }

    public void set_amount(float _amount) {
        this._amount = _amount;
    }

    public void set_desc(String _desc) {
        this._desc = _desc;
    }

    public void set_xcoord(float _xcoord) {
        this._xcoord = _xcoord;
    }

    public void set_ycoord(float _ycoord) {
        this._ycoord = _ycoord;
    }

    public void set_tax(boolean _tax) {
        this._tax = _tax;
    }

    public void set_reimburse(boolean _reimburse) {
        this._reimburse = _reimburse;
    }
}

