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

public class BestOffersAdapter extends ArrayAdapter {

    private ArrayList dataSet;
    Context mContext;
    private ArrayList<BestOffersModel> arraylist;

    private static class ViewHolder {
        ImageView img;

    }

    public BestOffersAdapter(ArrayList data, Context context) {
        super(context, R.layout.activity_best_offers_list, data);
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
    public BestOffersModel getItem(int position) {
        return (BestOffersModel) dataSet.get(position);
    }


    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_best_offers_list, parent, false);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.advertisement);
            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        BestOffersModel item = getItem(position);
        viewHolder.img.setImageBitmap(item.img);
        return result;
    }
    public void filter(String charText)
    {
        charText = charText.toLowerCase(Locale.getDefault());
        dataSet.clear();
        if (charText.length() == 0) {
            dataSet.addAll(arraylist);
        } else {
            for (BestOffersModel d : arraylist) {
                if (d.getName().toLowerCase(Locale.getDefault()).contains(charText)||d.getCompanyName().toLowerCase(Locale.getDefault()).contains(charText)||d.getDescription().toLowerCase(Locale.getDefault()).contains(charText)) {
                    dataSet.add(d);
                }
            }
        }
        notifyDataSetChanged();
    }
}
