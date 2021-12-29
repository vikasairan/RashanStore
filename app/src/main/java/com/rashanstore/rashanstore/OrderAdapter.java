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

public class OrderAdapter extends ArrayAdapter {

    private ArrayList dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView name,qty,price,kg;
    }

    public OrderAdapter(ArrayList data, Context context) {
        super(context, R.layout.activity_order_list, data);
        this.dataSet = data;
        this.mContext = context;

    }
    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public OrderModel getItem(int position) {
        return (OrderModel) dataSet.get(position);
    }


    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder;
        final View result;

        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_order_list, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.qty = (TextView) convertView.findViewById(R.id.qty);
            viewHolder.price = (TextView) convertView.findViewById(R.id.price);
            viewHolder.kg = (TextView) convertView.findViewById(R.id.kg);
            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        OrderModel item = getItem(position);
        viewHolder.name.setText(item.name);
        viewHolder.qty.setText(item.qty);
        viewHolder.price.setText(item.price);

        if(item.category.equals("Loose Item"))
        {
            viewHolder.kg.setVisibility(View.VISIBLE);
        }
        else
        {
            viewHolder.kg.setVisibility(View.GONE);
        }

        return result;
    }

}
