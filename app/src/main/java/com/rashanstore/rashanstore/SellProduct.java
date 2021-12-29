package com.rashanstore.rashanstore;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SellProduct extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_product);
        ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        clearDim(root);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        final EditText productname=findViewById(R.id.productName);

        final EditText companyName=findViewById(R.id.companyName);

        final EditText mfgDate=findViewById(R.id.mfgDate);

        final EditText expDate=findViewById(R.id.expDate);

        final EditText qty=findViewById(R.id.qty);

        final EditText price=findViewById(R.id.price);

        final EditText description=findViewById(R.id.description);



        Intent intent = getIntent();
        String category = intent.getStringExtra("category");
        if(category.equals("Loose Item"))
        {
            qty.setHint("Quantity (in Kg)");
            price.setHint("Price (per Kg)");
        }


        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        Calendar newCalendar = Calendar.getInstance();


        final DatePickerDialog mfgDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                mfgDate.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        final DatePickerDialog expDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                expDate.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));



        mfgDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mfgDatePickerDialog.show();
            }
        });
        expDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expDatePickerDialog.show();
            }
        });



        TextView add=findViewById(R.id.add_sell);
        final FirebaseAuth auth=FirebaseAuth.getInstance();

        add.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                TextView view = (TextView) v;
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
                            String mobile = auth.getCurrentUser().getPhoneNumber();
                            if (mobile.startsWith("0")) {
                                mobile = mobile.substring(1);
                            }
                            if (mobile.startsWith("+91")) {
                                mobile = mobile.replace("+91", "");
                            }
                            final String finalMobile = mobile;
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference dbRef = database.getReference().child("User").child(finalMobile);
                            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child("gstNumber").exists()&&dataSnapshot.child("panNumber").exists()&&!String.valueOf(dataSnapshot.child("gstNumber").getValue()).equals("") && !String.valueOf(dataSnapshot.child("panNumber").getValue()).equals("")) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference myRef = database.getReference("Products");
                                        String id = companyName.getText().toString() + "_" + productname.getText().toString() + "_" + price.getText().toString();
                                        id = id.replace(" ", "").replace(".", "").replace("#", "").replace("$", "").replace("[", "").replace("]", "");
                                        DatabaseReference user = myRef.child(id);
                                        user.child("productname").setValue(productname.getText().toString());
                                        user.child("companyName").setValue(companyName.getText().toString());
                                        user.child("mfgDate").setValue(mfgDate.getText().toString());
                                        user.child("expDate").setValue(expDate.getText().toString());
                                        user.child("qty").setValue(qty.getText().toString());
                                        int x=Integer.valueOf(price.getText().toString());
                                        x= (int) (x+Math.ceil(x*0.02));
                                        user.child("price").setValue(String.valueOf(x));
                                        user.child("description").setValue(description.getText().toString());
                                        Intent intent = getIntent();
                                        String category = intent.getStringExtra("category");
                                        user.child("category").setValue(category);
                                        FirebaseAuth auth = FirebaseAuth.getInstance();
                                        user.child("phone").setValue(auth.getCurrentUser().getPhoneNumber());
                                        Intent i = new Intent(SellProduct.this, UploadImage.class);
                                        i.putExtra("path", "Display Pictures/" + id + ".jpg");
                                        startActivity(i);
                                        finish();
                                    } else {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!isFinishing()) {

                                                    final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
                                                    applyDim(root, 0.8f);
                                                    LayoutInflater layoutInflater = (LayoutInflater) SellProduct.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                    View customView = layoutInflater.inflate(R.layout.gst_pan_verification, null);
                                                    Button cancel = (Button) customView.findViewById(R.id.cancel);
                                                    Button submit = (Button) customView.findViewById(R.id.submit);

                                                    final PopupWindow popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                                                    LinearLayout layout = findViewById(R.id.sell);
                                                    Display display = getWindowManager().getDefaultDisplay();
                                                    Point size = new Point();
                                                    display.getSize(size);
                                                    int width = size.x;
                                                    popupWindow.setWidth(width - 50);

                                                    popupWindow.setOutsideTouchable(true);
                                                    popupWindow.update();

                                                    final EditText edit_gst = customView.findViewById(R.id.edit_gst);
                                                    final EditText edit_pan = customView.findViewById(R.id.edit_pan);


                                                    edit_gst.setText(String.valueOf(dataSnapshot.child("gstNumber").getValue()));
                                                    edit_pan.setText(String.valueOf(dataSnapshot.child("panNumber").getValue()));

                                                    popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
                                                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                                        @Override
                                                        public void onDismiss() {
                                                            popupWindow.dismiss();
                                                            clearDim(root);
                                                        }
                                                    });

                                                    cancel.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            popupWindow.dismiss();
                                                            clearDim(root);
                                                        }
                                                    });

                                                    submit.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Pattern patternPAN = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");
                                                            Matcher matcherPAN = patternPAN.matcher(edit_pan.getText().toString().trim());
                                                            Pattern patternGST1 = Pattern.compile("[0][1-9][A-Z]{5}[0-9]{4}[A-Z]{1}[1-9a-zA-Z]{1}[zZ]{1}[0-9a-zA-Z]{1}");
                                                            Matcher matcherGST1 = patternGST1.matcher(edit_gst.getText().toString().trim());
                                                            Pattern patternGST2 = Pattern.compile("[1-2][0-9][A-Z]{5}[0-9]{4}[A-Z]{1}[1-9a-zA-Z]{1}[zZ]{1}[0-9a-zA-Z]{1}");
                                                            Matcher matcherGST2 = patternGST2.matcher(edit_gst.getText().toString().trim());
                                                            Pattern patternGST3 = Pattern.compile("[3][0-7][A-Z]{5}[0-9]{4}[A-Z]{1}[1-9a-zA-Z]{1}[zZ]{1}[0-9a-zA-Z]{1}");
                                                            Matcher matcherGST3 = patternGST3.matcher(edit_gst.getText().toString().trim());

                                                            if (matcherPAN.matches()) {
                                                                if (matcherGST1.matches() || matcherGST2.matches() || matcherGST3.matches()) {
                                                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                    DatabaseReference dbRef = database.getReference().child("User").child(finalMobile);
                                                                    dbRef.child("gstNumber").setValue(edit_gst.getText().toString());
                                                                    dbRef.child("panNumber").setValue(edit_pan.getText().toString());

                                                                    DatabaseReference myRef = database.getReference("Products");
                                                                    String id = companyName.getText().toString() + "_" + productname.getText().toString() + "_" + price.getText().toString();
                                                                    id = id.replace(" ", "").replace(".", "").replace("#", "").replace("$", "").replace("[", "").replace("]", "");
                                                                    DatabaseReference user = myRef.child(id);
                                                                    user.child("productname").setValue(productname.getText().toString());
                                                                    user.child("companyName").setValue(companyName.getText().toString());
                                                                    user.child("mfgDate").setValue(mfgDate.getText().toString());
                                                                    user.child("expDate").setValue(expDate.getText().toString());
                                                                    user.child("qty").setValue(qty.getText().toString());
                                                                    int x=Integer.valueOf(price.getText().toString());
                                                                    x= (int) (x+Math.ceil(x*0.02));
                                                                    user.child("price").setValue(String.valueOf(x));
                                                                    user.child("description").setValue(description.getText().toString());
                                                                    Intent intent = getIntent();
                                                                    String category = intent.getStringExtra("category");
                                                                    user.child("category").setValue(category);
                                                                    FirebaseAuth auth = FirebaseAuth.getInstance();
                                                                    user.child("phone").setValue(auth.getCurrentUser().getPhoneNumber());
                                                                    Intent i = new Intent(SellProduct.this, UploadImage.class);
                                                                    i.putExtra("path", "Display Pictures/" + id + ".jpg");
                                                                    startActivity(i);
                                                                    finish();

                                                                    popupWindow.dismiss();
                                                                    clearDim(root);
                                                                } else {
                                                                    Toast.makeText(getApplicationContext(), "Invalid GST Number", Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                Toast.makeText(getApplicationContext(), "Invalid PAN Number", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }}});
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                    break;
                }
            }
                return true;
        }
    });
    }
    public boolean validateForm()
    {
        final EditText productname=findViewById(R.id.productName);

        final EditText companyName=findViewById(R.id.companyName);

        final EditText mfgDate=findViewById(R.id.mfgDate);

        final EditText expDate=findViewById(R.id.expDate);

        final EditText qty=findViewById(R.id.qty);

        final EditText price=findViewById(R.id.price);

        final EditText description=findViewById(R.id.description);

        boolean alldone=true;
        if(TextUtils.isEmpty(productname.getText().toString().trim()))
        {
            productname.setError("Enter product name");
            return false;
        }else
        {
            alldone=true;
            productname.setError(null);
        }
        if(TextUtils.isEmpty(companyName.getText().toString().trim()))
        {
            companyName.setError("Enter company name");
            return false;
        }else
        {
            alldone=true;
            companyName.setError(null);
        }
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        //here is the main place where we need to work on.
        int id=item.getItemId();
        switch (id){

            case R.id.nav_home:
                Intent h= new Intent(this,Home.class);
                startActivity(h);
                finish();
                break;
            case R.id.nav_my_products:
                Intent m= new Intent(this,MyProducts.class);
                startActivity(m);
                finish();
                break;
            case R.id.nav_about:
                Intent i= new Intent(this, About.class);
                startActivity(i);
                finish();
                break;
            case R.id.nav_account:
                Intent g= new Intent(this,profile.class);
                startActivity(g);
                finish();
                break;
            case R.id.nav_contact:
                Intent s= new Intent(this,Contact.class);
                startActivity(s);
                finish();
                break;
            case R.id.nav_terms:
                Intent t= new Intent(this,Terms.class);
                startActivity(t);
                finish();
                break;
            case R.id.logout:
                FirebaseAuth auth=FirebaseAuth.getInstance();
                auth.signOut();
                Intent l= new Intent(this,Login.class);
                startActivity(l);
                finish();
                break;
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public static void applyDim(@NonNull ViewGroup parent, float dimAmount){
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    public static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

}