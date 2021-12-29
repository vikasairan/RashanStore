package com.rashanstore.rashanstore;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class GroceryListAdapter extends ArrayAdapter {

    private ArrayList dataSet;
    Context mContext;
    private ArrayList<GroceryListModel> arraylist;

    private static class ViewHolder {
        TextView date;
        ImageView img;
    }

    public GroceryListAdapter(ArrayList data, Context context) {
        super(context, R.layout.activity_grocery_list, data);
        this.dataSet = data;
        this.mContext = context;
        this.arraylist = new ArrayList();
        this.arraylist.addAll(dataSet);
    }
    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public GroceryListModel getItem(int position) {
        return (GroceryListModel) dataSet.get(position);
    }


    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_grocery_list, parent, false);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.grocerylist);
            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        GroceryListModel item = getItem(position);
        viewHolder.date.setText(item.id);
        viewHolder.img.setImageBitmap(item.img);
        return result;
    }
}
