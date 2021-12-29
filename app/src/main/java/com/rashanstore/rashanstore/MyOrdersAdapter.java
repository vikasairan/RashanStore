package com.rashanstore.rashanstore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class MyOrdersAdapter extends ArrayAdapter {

    private ArrayList dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView orderid,date,total,payment,address;
    }

    public MyOrdersAdapter(ArrayList data, Context context) {
        super(context, R.layout.activity_my_orders_list, data);
        this.dataSet = data;
        this.mContext = context;

    }
    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public MyOrdersModel getItem(int position) {
        return (MyOrdersModel) dataSet.get(position);
    }


    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder;
        final View result;

        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_my_orders_list, parent, false);
            viewHolder.orderid = (TextView) convertView.findViewById(R.id.orderid);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.total = (TextView) convertView.findViewById(R.id.total);
            viewHolder.payment=(TextView) convertView.findViewById(R.id.payment);
            viewHolder.address=(TextView)convertView.findViewById(R.id.address);
            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        MyOrdersModel item = getItem(position);
        viewHolder.orderid.setText(item.orderid);
        viewHolder.date.setText(item.date);
        viewHolder.total.setText(item.total);
        viewHolder.payment.setText(item.payment);
        viewHolder.address.setText(item.address);

        return result;
    }

}
