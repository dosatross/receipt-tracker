package com.example.hayden.receipt_tracker;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;



public class ReceiptAdapter extends ArrayAdapter<Receipt> {


    //View lookup cache
    private static class ViewHolder {
        TextView desc;
        TextView category;
        TextView amount;
        TextView date;

    }


    public ReceiptAdapter(Context context, ArrayList<Receipt> receipts) {
        super(context, 0, receipts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Receipt receipt = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
            viewHolder.desc = (TextView) convertView.findViewById(R.id.desc);
            viewHolder.category = (TextView) convertView.findViewById(R.id.category);
            viewHolder.amount = (TextView) convertView.findViewById(R.id.amount);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);

        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        viewHolder.desc.setText(receipt.get_desc());
        viewHolder.category.setText(receipt.get_category());
        viewHolder.amount.setText(String.valueOf(receipt.get_amount()));
        viewHolder.date.setText(String.valueOf(receipt.get_date()));

        // Return the completed view to render on screen
        return convertView;
    }
}
