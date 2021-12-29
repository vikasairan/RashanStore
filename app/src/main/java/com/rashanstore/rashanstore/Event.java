package com.rashanstore.rashanstore;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Event extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        final EditText eventdesc = (EditText) findViewById(R.id.event);
        final EditText date = (EditText) findViewById(R.id.date);
        final EditText location = (EditText) findViewById(R.id.location);
        final EditText contact = (EditText) findViewById(R.id.contact);
        final EditText name = (EditText) findViewById(R.id.name);


        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        Calendar newCalendar = Calendar.getInstance();


        final DatePickerDialog eventDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                date.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventDatePickerDialog.show();
            }
        });



        ImageView register = findViewById(R.id.register);

        register.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
                        if (validateForm()) {
                            if(isOnline()) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference dbRef = database.getReference().child("Event");
                                String id = generateString();
                                dbRef.child(id).child("event").setValue(eventdesc.getText().toString());
                                dbRef.child(id).child("date").setValue(date.getText().toString());
                                dbRef.child(id).child("location").setValue(location.getText().toString());
                                dbRef.child(id).child("contact").setValue(contact.getText().toString());
                                dbRef.child(id).child("name").setValue(name.getText().toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isFinishing()) {


                                            AlertDialog.Builder builder=new AlertDialog.Builder(Event.this);
                                            builder.setTitle("Successfully Applied").setMessage("Thanks for Trusting\n\nWe will contact you soon");
                                            builder.setIcon(R.mipmap.logo);
                                            builder.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    startActivity(new Intent(Event.this, Login.class));
                                                    finish();
                                                }
                                            });
                                            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                @Override
                                                public void onCancel(DialogInterface dialogInterface) {
                                                    startActivity(new Intent(Event.this, Login.class));
                                                    finish();

                                                }
                                            });
                                            builder.show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"No Internet Connection\nPlease Connect and Try again",Toast.LENGTH_LONG).show();
                            }
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
        final EditText event = (EditText) findViewById(R.id.event);
        final EditText date = (EditText) findViewById(R.id.date);
        final EditText location = (EditText) findViewById(R.id.location);
        final EditText contact = (EditText) findViewById(R.id.contact);
        final EditText name = (EditText) findViewById(R.id.name);

        boolean alldone=true;
        if(TextUtils.isEmpty(event.getText().toString().trim()))
        {
            event.setError("Enter Event Details");
            return false;
        }
        else
        {
            alldone=true;
            event.setError(null);
        }
        if(TextUtils.isEmpty(date.getText().toString().trim()))
        {
            date.setError("Enter Date of Event");
            return false;
        }
        else
        {
            alldone=true;
            date.setError(null);
        }
        if(TextUtils.isEmpty(location.getText().toString().trim()))
        {
            location.setError("Enter Location of Event");
            return false;
        }
        else
        {
            alldone=true;
            location.setError(null);
        }
        if(TextUtils.isEmpty(contact.getText().toString().trim())||contact.length()<10)
        {
            contact.setError("Enter Valid Contact Number");
            return false;
        }
        else
        {
            alldone=true;
            contact.setError(null);
        }
        if(TextUtils.isEmpty(name.getText().toString().trim()))
        {
            name.setError("Enter Name");
            return false;
        }
        else
        {
            alldone=true;
            name.setError(null);
        }
        return alldone;
    }
    private String generateString() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }
    public boolean isOnline() {
        ConnectivityManager connectivityManager;
        NetworkInfo wifiInfo, mobileInfo;
        boolean connected = false;
        try {
            connectivityManager = (ConnectivityManager) getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;

        } catch (Exception e) {
        }
        return connected;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(this,Customer.class));
            finish();
            super.onBackPressed();
        }
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.nav_home:
                Intent h = new Intent(Event.this, Customer.class);
                startActivity(h);
                finish();
                break;
            case R.id.nav_my_list:
                Intent m= new Intent(Event.this,MyGroceryList.class);
                startActivity(m);
                finish();
                break;
            case R.id.nav_bestoffers:
                Intent l = new Intent(Event.this, BestOffersCustomer.class);
                startActivity(l);
                finish();
                break;
            case R.id.nav_event:
                Intent e = new Intent(Event.this, Event.class);
                startActivity(e);
                finish();
                break;
            case R.id.nav_call:
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:+919214809000"));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return true;
                }
                startActivity(callIntent);
                break;
            case R.id.nav_chat:
                PackageManager pm = getPackageManager();
                try {

                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    Uri uri = Uri.parse("smsto:+919214809000");
                    Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                    i.setPackage("com.whatsapp");
                    startActivity(i);


                } catch (PackageManager.NameNotFoundException ex) {
                    Toast.makeText(Event.this, "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.logout:
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                Intent s = new Intent(this, Login.class);
                startActivity(s);
                finish();
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}