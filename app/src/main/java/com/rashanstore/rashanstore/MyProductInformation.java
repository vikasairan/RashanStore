package com.rashanstore.rashanstore;

import android.app.DatePickerDialog;
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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class MyProductInformation extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_product_information);
        Bundle extras = getIntent().getExtras();
        String id = null;
        if (extras != null) {
            id = extras.getString("id");
        }

        final CircleImageView img=findViewById(R.id.image);

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
            img.setImageBitmap(bitmap);
        }
        else
        {
            img.setBackgroundResource(R.drawable.default_image);
        }


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference dbRef = database.getReference().child("Products");
        final String finalId = id;
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                TextView productName=findViewById(R.id.productName);
                TextView companyName=findViewById(R.id.companyName);
                TextView category=findViewById(R.id.category);
                EditText mfg=findViewById(R.id.mfgDate);
                EditText exp=findViewById(R.id.expDate);
                EditText qty=findViewById(R.id.qty);
                EditText price=findViewById(R.id.price);
                EditText description=findViewById(R.id.description);


                productName.setText(String.valueOf(dataSnapshot.child(finalId).child("productname").getValue()));
                companyName.setText(String.valueOf(dataSnapshot.child(finalId).child("companyName").getValue()));
                mfg.setText(String.valueOf(dataSnapshot.child(finalId).child("mfgDate").getValue()));
                exp.setText(String.valueOf(dataSnapshot.child(finalId).child("expDate").getValue()));
                qty.setText(String.valueOf(dataSnapshot.child(finalId).child("qty").getValue()));
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


        final EditText mfg=findViewById(R.id.mfgDate);
        final EditText exp=findViewById(R.id.expDate);

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        Calendar newCalendar = Calendar.getInstance();


        final DatePickerDialog mfgDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                mfg.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        final DatePickerDialog expDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                exp.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));



        mfg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mfgDatePickerDialog.show();
            }
        });
        exp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expDatePickerDialog.show();
            }
        });





        final Button save=findViewById(R.id.save);

        save.setOnTouchListener(new View.OnTouchListener() {

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


                        if(validateForm()) {

                            EditText mfg=findViewById(R.id.mfgDate);
                            EditText exp=findViewById(R.id.expDate);
                            EditText qty=findViewById(R.id.qty);
                            EditText price=findViewById(R.id.price);
                            EditText description=findViewById(R.id.description);

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("Products");
                            DatabaseReference user = myRef.child(finalId);

                            user.child("mfgDate").setValue(mfg.getText().toString());
                            user.child("expDate").setValue(exp.getText().toString());
                            user.child("qty").setValue(qty.getText().toString());
                            user.child("price").setValue(price.getText().toString());
                            user.child("description").setValue(description.getText().toString());

                            finish();
                            overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);

                        }


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

                        finish();
                        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);

                        break;
                    }
                }
                return true;
            }
        });


        Button remove=findViewById(R.id.remove);
        remove.setOnTouchListener(new View.OnTouchListener() {

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
                        DatabaseReference myRef = database.getReference("Products");
                        DatabaseReference user = myRef.child(finalId);
                        user.setValue(null);
                        finish();
                        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);

                        break;
                    }
                }
                return true;
            }
        });
    }

    public boolean validateForm()
    {
        final EditText mfgDate=findViewById(R.id.mfgDate);

        final EditText expDate=findViewById(R.id.expDate);

        final EditText qty=findViewById(R.id.qty);

        final EditText price=findViewById(R.id.price);

        final EditText description=findViewById(R.id.description);

        boolean alldone=true;


        if(TextUtils.isEmpty(mfgDate.getText().toString().trim()))
        {
            mfgDate.setError("Enter MFG Date");
            return false;
        }else
        {
            alldone=true;
            mfgDate.setError(null);
        }
        if(TextUtils.isEmpty(expDate.getText().toString().trim()))
        {
            expDate.setError("Enter EXP Date");
            return false;
        }else
        {
            alldone=true;
            expDate.setError(null);
        }
        if(TextUtils.isEmpty(qty.getText().toString().trim()))
        {
            qty.setError("Enter Quantity");
            return false;
        }else
        {
            alldone=true;
            qty.setError(null);
        }
        if(TextUtils.isEmpty(price.getText().toString().trim()))
        {
            price.setError("Enter price");
            return false;
        }else
        {
            alldone=true;
            price.setError(null);
        }
        if(TextUtils.isEmpty(description.getText().toString().trim()))
        {
            description.setError("Enter description");
            return false;
        }else
        {
            alldone=true;
            description.setError(null);
        }
        return alldone;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);
    }

}
