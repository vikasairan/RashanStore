package com.rashanstore.rashanstore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

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

import static android.view.View.GONE;

public class ProductInformation extends AppCompatActivity {
    int minteger = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_information);
        Bundle extras = getIntent().getExtras();
        String id = null;
        if (extras != null) {
            id = extras.getString("id");
        }


        final LinearLayout img=findViewById(R.id.image);

        File dir = Environment.getExternalStorageDirectory();
        final File folder = new File(dir, "Rashan Store/Display Pictures");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        final File localFile = new File(folder,id+".jpg");

        if(localFile.exists()){

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;

            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath(),options);
            BitmapDrawable background=new BitmapDrawable(getApplicationContext().getResources(), bitmap);
            img.setBackground(background);
        }
        else
        {
            img.setBackgroundResource(R.drawable.default_image);
        }

        final Button add_cart=findViewById(R.id.add_cart);

        final CardView qty_select=findViewById(R.id.qty_select);
        qty_select.setVisibility(GONE);
        final LinearLayout add_layout=findViewById(R.id.add_layout);


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference dbRef = database.getReference().child("Products");
        final String finalId = id;
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                TextView productName=findViewById(R.id.productName);
                TextView companyName=findViewById(R.id.companyName);
                TextView mfg=findViewById(R.id.mfgDate);
                TextView exp=findViewById(R.id.expDate);
                TextView qty=findViewById(R.id.qty);
                TextView price=findViewById(R.id.price);
                TextView description=findViewById(R.id.description);
                TextView category=findViewById(R.id.category);

                productName.setText(String.valueOf(dataSnapshot.child(finalId).child("productname").getValue()));
                companyName.setText(String.valueOf(dataSnapshot.child(finalId).child("companyName").getValue()));
                mfg.setText(String.valueOf(dataSnapshot.child(finalId).child("mfgDate").getValue()));
                exp.setText(String.valueOf(dataSnapshot.child(finalId).child("expDate").getValue()));
                qty.setText(String.valueOf(dataSnapshot.child(finalId).child("qty").getValue()));
                if(String.valueOf(dataSnapshot.child(finalId).child("qty").getValue()).equals("0"))
                {
                    add_layout.setVisibility(GONE);
                }

                if(String.valueOf(dataSnapshot.child(finalId).child("category").getValue()).equals("Loose Item"))
                {
                    findViewById(R.id.kg).setVisibility(View.VISIBLE);
                    findViewById(R.id.perkg).setVisibility(View.VISIBLE);
                }
                else {
                    findViewById(R.id.kg).setVisibility(GONE);
                    findViewById(R.id.perkg).setVisibility(GONE);
                }
                price.setText(String.valueOf(dataSnapshot.child(finalId).child("price").getValue()));
                description.setText(String.valueOf(dataSnapshot.child(finalId).child("description").getValue()));
                category.setText(String.valueOf(dataSnapshot.child(finalId).child("category").getValue()));

                TextView amount=findViewById(R.id.amount);
                amount.setText(price.getText());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        add_cart.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                Button view = (Button) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x50000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        add_layout.setVisibility(GONE);
                        qty_select.setVisibility(View.VISIBLE);
                        break;
                    }
                }
                return true;
            }
        });
        Button cancel=findViewById(R.id.cancel);
        cancel.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                Button view = (Button) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x50000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        qty_select.setVisibility(GONE);
                        add_layout.setVisibility(View.VISIBLE);
                        break;
                    }
                }
                return true;
            }
        });
        TextView decrease=findViewById(R.id.decrease);
        TextView increase=findViewById(R.id.increase);

        decrease.setOnTouchListener(new View.OnTouchListener() {

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
                        if(minteger>1) {
                        minteger = minteger - 1;
                        EditText qty_add = findViewById(R.id.qty_add);
                        qty_add.setText("" + minteger);
                }
                break;
            }
        }
        return true;
    }
});
        increase.setOnTouchListener(new View.OnTouchListener() {

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
                        TextView qty=findViewById(R.id.qty);
                       if(minteger<Integer.valueOf(qty.getText().toString())) {
                          minteger = minteger + 1;
                          EditText qty_add = findViewById(R.id.qty_add);
                          qty_add.setText("" + minteger); }
                        break;
                    }
                }
                return true;
            }
        });
        final TextView amount=findViewById(R.id.amount);
        final TextView price=findViewById(R.id.price);
        final EditText qty_add = findViewById(R.id.qty_add);
        qty_add.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()!=0) {
                    if (!price.getText().equals("")) {
                        amount.setText(String.valueOf(Integer.valueOf(String.valueOf(charSequence)) * Integer.valueOf(price.getText().toString())));
                    }
                    TextView qty = findViewById(R.id.qty);

                    if (!qty.getText().equals("") && Integer.valueOf(String.valueOf(charSequence)) > Integer.valueOf(qty.getText().toString())) {
                        qty_add.setText(qty.getText());
                    }

                }
                else
                {
                    amount.setText("0");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(qty_add.getText().toString().equals("0")||qty_add.getText().toString().equals(""))
                {
                    qty_add.setText("1");
                    amount.setText(price.getText().toString());
                }
                minteger=Integer.parseInt(qty_add.getText().toString());
            }
        });

        Button add=findViewById(R.id.add);
        add.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                TextView view= (TextView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x50000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("Cart");
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
                        DatabaseReference cart=myRef.child(mobile);
                        EditText qty_add = findViewById(R.id.qty_add);
                        cart.child(finalId).child("qty").setValue(qty_add.getText().toString());
                        cart.child(finalId).child("price").setValue(price.getText().toString());
                        startActivity(new Intent(ProductInformation.this, Cart.class));
                        finish();
                        break;
            }
        }
        return true;
    }
});
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);
    }

}
