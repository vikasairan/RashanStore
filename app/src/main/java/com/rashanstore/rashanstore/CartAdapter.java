package com.rashanstore.rashanstore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;

public class CartAdapter extends ArrayAdapter {

    private ArrayList dataSet;
    Context mContext;
    private static class ViewHolder {
        ImageView img,remove;
        TextView productName,companyName,increase,decrease,price,qty_add;
    }

    public CartAdapter(ArrayList data, Context context) {
        super(context, R.layout.activity_cart_list, data);
        this.dataSet = data;
        this.mContext = context;
    }
    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public CartModel getItem(int position) {
        return (CartModel) dataSet.get(position);
    }


    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent)
    {

        final ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_cart_list, parent, false);

            viewHolder.img = convertView.findViewById(R.id.image);
            viewHolder.productName =  convertView.findViewById(R.id.productName);
            viewHolder.companyName =convertView.findViewById(R.id.companyName);
            viewHolder.price =  convertView.findViewById(R.id.amount);
            viewHolder.qty_add = convertView.findViewById(R.id.qty_add);
            viewHolder.decrease=convertView.findViewById(R.id.decrease);
            viewHolder.increase=convertView.findViewById(R.id.increase);
            viewHolder.remove=convertView.findViewById(R.id.remove);

            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }
        final CartModel item = getItem(position);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth=FirebaseAuth.getInstance();
        String mobile=auth.getCurrentUser().getPhoneNumber();
        if(mobile.startsWith("0"))
        {
            mobile=mobile.substring(1);
        }
        if(mobile.startsWith("+91"))
        {
            mobile=mobile.replace("+91","");
        }
        final String finalMobile = mobile;

        DatabaseReference myRef = database.getReference().child("Cart").child(finalMobile);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot)
            {

                        if (dataSnapshot.child(item.id).exists()) {
                            viewHolder.qty_add.setText(String.valueOf(dataSnapshot.child(item.id).child("qty").getValue()));
                            if(!String.valueOf(viewHolder.qty_add.getText()).equals("null")&&!String.valueOf(dataSnapshot.child(item.id).child("price").getValue()).equals("null")) {
                                int price = Integer.valueOf(String.valueOf(viewHolder.qty_add.getText())) * Integer.valueOf(String.valueOf(dataSnapshot.child(item.id).child("price").getValue()));
                                viewHolder.price.setText(String.valueOf(price));
                            }
                    }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


            myRef = database.getReference().child("Products").child(item.id);
                myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    viewHolder.productName.setText(String.valueOf(dataSnapshot.child("productname").getValue()));
                    viewHolder.companyName.setText(String.valueOf(dataSnapshot.child("companyName").getValue()));

                    String qty=viewHolder.qty_add.getText().toString();
                    if(!qty.equals("")&&!qty.equals("null")&&!dataSnapshot.child("qty").getValue().toString().equals(""))
                    {
                        if (Integer.valueOf(qty)> Integer.valueOf(String.valueOf(dataSnapshot.child("qty").getValue())))
                        {
                            viewHolder.qty_add.setText(String.valueOf(dataSnapshot.child("qty").getValue()));
                            DatabaseReference myRef = database.getReference("Cart");
                            DatabaseReference cart=myRef.child(finalMobile);
                            cart.child(item.id).child("qty").setValue(String.valueOf(dataSnapshot.child("qty").getValue()));
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
        });

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
            viewHolder.img.setImageBitmap(bitmap);
        }
        else
        {
            viewHolder.img.setImageResource(R.drawable.default_image);
        }



        viewHolder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dataSet.remove(position);
                notifyDataSetChanged();
                DatabaseReference myRef = database.getReference().child("Cart").child(finalMobile);
                myRef.child(item.id).setValue(null);
            }
        });



        viewHolder.decrease.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                TextView view= (TextView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x80ffffff, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        int x=Integer.valueOf(String.valueOf(viewHolder.qty_add.getText()));
                if( x>1) {
                    x=x-1;
                    viewHolder.qty_add.setText(String.valueOf(x));
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("Cart");
                    DatabaseReference cart=myRef.child(finalMobile);
                    cart.child(item.id).child("qty").setValue(String.valueOf(viewHolder.qty_add.getText()));
                }
                break;
            }
        }
        return true;
        }
        });
        viewHolder.increase.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                TextView view= (TextView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x80ffffff, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        DatabaseReference myRef = database.getReference().child("Products").child(item.id);
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int x=Integer.valueOf(String.valueOf(viewHolder.qty_add.getText()));
                        int max=Integer.valueOf(dataSnapshot.child("qty").getValue().toString());
                        if(x <max) {
                            x=x+1;
                            viewHolder.qty_add.setText(String.valueOf(x));
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("Cart");
                            DatabaseReference cart=myRef.child(finalMobile);
                            cart.child(item.id).child("qty").setValue(String.valueOf(viewHolder.qty_add.getText()));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                        break;
                    }
                }
                return true;
            }
        });
        return result;
    }

}
