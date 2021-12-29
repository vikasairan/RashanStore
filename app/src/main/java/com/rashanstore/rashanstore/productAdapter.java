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
import java.util.Locale;

public class productAdapter extends ArrayAdapter {

    private ArrayList dataSet;
    Context mContext;
    private ArrayList<productModel> arraylist;

    private static class ViewHolder {
        TextView name,qty,price;
        LinearLayout img;


    }

    public productAdapter(ArrayList data, Context context) {
        super(context, R.layout.activity_product_list, data);
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
    public productModel getItem(int position) {
        return (productModel) dataSet.get(position);
    }


    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_product_list, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.qty = (TextView) convertView.findViewById(R.id.quantity);
            viewHolder.price = (TextView) convertView.findViewById(R.id.price);
            viewHolder.img=(LinearLayout) convertView.findViewById(R.id.image);
            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        productModel item = getItem(position);
        viewHolder.name.setText(item.name);
        viewHolder.qty.setText(item.qty);
        viewHolder.price.setText(item.price);


        File dir = Environment.getExternalStorageDirectory();
        final File folder = new File(dir, "Rashan Store/Display Pictures");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        final File localFile = new File(folder,item.id+".jpg");

        if(localFile.exists())
        {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath(),options);
            BitmapDrawable background=new BitmapDrawable(mContext.getResources(), bitmap);
            viewHolder.img.setBackground(background);
        }
        else
        {
            viewHolder.img.setBackgroundResource(R.drawable.default_image);
        }

        return result;
    }
    public void filter(String charText)
    {
        charText = charText.toLowerCase(Locale.getDefault());
        dataSet.clear();
        if (charText.length() == 0) {
            dataSet.addAll(arraylist);
        } else {
            for (productModel d : arraylist) {
                if (d.getName().toLowerCase(Locale.getDefault()).contains(charText)||d.getCompanyName().toLowerCase(Locale.getDefault()).contains(charText)||d.getDescription().toLowerCase(Locale.getDefault()).contains(charText)) {
                    dataSet.add(d);
                }
            }
        }
        notifyDataSetChanged();
    }
}
