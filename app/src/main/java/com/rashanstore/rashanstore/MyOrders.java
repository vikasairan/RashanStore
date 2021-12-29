package com.rashanstore.rashanstore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.transition.ChangeBounds;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.paytm.pgsdk.easypay.OnSwipeTouchListener;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class MyOrders extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ViewPagerEx.OnPageChangeListener, BaseSliderView.OnSliderClickListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    ArrayList items;
    GridView list;
    private MyOrdersAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        ChangeBounds bounds = new ChangeBounds();
        bounds.setDuration(1500);
        getWindow().setSharedElementEnterTransition(bounds);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        items = new ArrayList();

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
        progressBar.setVisibility(View.VISIBLE);


        if(!isOnline())
        {
            progressBar.setVisibility(View.GONE);
            findViewById(R.id.MyOrders).setVisibility(View.GONE);
            findViewById(R.id.internet).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.MyOrders).setVisibility(View.VISIBLE);
            findViewById(R.id.internet).setVisibility(View.GONE);
            if (items.isEmpty()) {
                findViewById(R.id.MyOrders).setVisibility(View.GONE);
                findViewById(R.id.no_product).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.MyOrders).setVisibility(View.VISIBLE);
                findViewById(R.id.no_product).setVisibility(View.GONE);
            }
        }


        final FirebaseDatabase database=FirebaseDatabase.getInstance();

        list = findViewById(R.id.list);

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


        final DatabaseReference dbRef = database.getReference().child("OrdersReceived");
        final String finalMobile1 = mobile;
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();
                for (DataSnapshot dsp : dataSnapshot.getChildren())
                {
                    if(dsp.child("mobile").exists()&&String.valueOf(dsp.child("mobile").getValue()).equals(finalMobile1)) {
                        String date = String.valueOf(dsp.child("TXNDATE").getValue());
                        String total = String.valueOf(dsp.child("TXNAMOUNT").getValue());
                        String payment = String.valueOf(dsp.child("PAYMENTMODE").getValue());
                        String address = String.valueOf(dsp.child("address").getValue());
                        if(!payment.equals("Cash on Delivery"))
                        {
                            payment="Paytm";
                        }
                        items.add(new MyOrdersModel(dsp.getKey(),date,"Rs "+total,payment,address));
                    }
                }
                adapter = new MyOrdersAdapter(items, getApplicationContext());
                list.setAdapter(adapter);
                progressBar.setVisibility(View.INVISIBLE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
                progressBar.setLayoutParams(params);
                if(isOnline()) {
                    if (items.isEmpty()) {
                        findViewById(R.id.MyOrders).setVisibility(View.GONE);
                        findViewById(R.id.no_product).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.MyOrders).setVisibility(View.VISIBLE);
                        findViewById(R.id.no_product).setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                MyOrdersModel Model = (MyOrdersModel) items.get(position);
                String orderid=Model.orderid;
                Bundle bundle = new Bundle();
                bundle.putString("orderid", orderid);
                Intent i=new Intent(MyOrders.this,OrderSummary.class);
                i.putExtras(bundle);
                startActivity(i);
            }

        });



        final SwipeRefreshLayout swipeView1 = findViewById(R.id.swipe1);
        swipeView1.setColorScheme(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_green_light);
        swipeView1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView1.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(!isOnline())
                        {
                            progressBar.setVisibility(View.GONE);
                            findViewById(R.id.MyOrders).setVisibility(View.GONE);
                            findViewById(R.id.internet).setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            findViewById(R.id.MyOrders).setVisibility(View.VISIBLE);
                            findViewById(R.id.internet).setVisibility(View.GONE);
                        }

                        list = findViewById(R.id.list);
                        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
                        progressBar.setVisibility(View.VISIBLE);

                        items = new ArrayList();
                        final DatabaseReference dbRef = database.getReference().child("OrdersReceived");
                        dbRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                items.clear();
                                for (DataSnapshot dsp : dataSnapshot.getChildren())
                                {
                                    if(dsp.child("mobile").exists()&&String.valueOf(dsp.child("mobile").getValue()).equals(finalMobile1)) {
                                        String date = String.valueOf(dsp.child("TXNDATE").getValue());
                                        String total = String.valueOf(dsp.child("TXNAMOUNT").getValue());
                                        String payment = String.valueOf(dsp.child("PAYMENTMODE").getValue());
                                        String address = String.valueOf(dsp.child("address").getValue());
                                        if(!payment.equals("Cash on Delivery"))
                                        {
                                            payment="Paytm";
                                        }
                                        items.add(new MyOrdersModel(dsp.getKey(),date,"Rs "+total,payment,address));
                                    }
                                }
                                adapter = new MyOrdersAdapter(items, getApplicationContext());
                                list.setAdapter(adapter);
                                progressBar.setVisibility(View.INVISIBLE);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
                                progressBar.setLayoutParams(params);
                                if(isOnline()) {
                                    if (items.isEmpty()) {
                                        findViewById(R.id.MyOrders).setVisibility(View.GONE);
                                        findViewById(R.id.no_product).setVisibility(View.VISIBLE);
                                    } else {
                                        findViewById(R.id.MyOrders).setVisibility(View.VISIBLE);
                                        findViewById(R.id.no_product).setVisibility(View.GONE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });



                        swipeView1.setRefreshing(false);

                    }
                }, 3000);
            }
        });

        final SwipeRefreshLayout swipeView2 = findViewById(R.id.swipe2);
        swipeView2.setColorScheme(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_green_light);
        swipeView2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView2.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        if(!isOnline())
                        {
                            progressBar.setVisibility(View.GONE);
                            findViewById(R.id.MyOrders).setVisibility(View.GONE);
                            findViewById(R.id.internet).setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            findViewById(R.id.MyOrders).setVisibility(View.VISIBLE);
                            findViewById(R.id.internet).setVisibility(View.GONE);
                        }

                        list = findViewById(R.id.list);
                        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
                        progressBar.setVisibility(View.VISIBLE);

                        items = new ArrayList();
                        final DatabaseReference dbRef = database.getReference().child("OrdersReceived");
                        dbRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                items.clear();
                                for (DataSnapshot dsp : dataSnapshot.getChildren())
                                {
                                    if(dsp.child("mobile").exists()&&String.valueOf(dsp.child("mobile").getValue()).equals(finalMobile1)) {
                                        String date = String.valueOf(dsp.child("TXNDATE").getValue());
                                        String total = String.valueOf(dsp.child("TXNAMOUNT").getValue());
                                        String payment = String.valueOf(dsp.child("PAYMENTMODE").getValue());
                                        String address = String.valueOf(dsp.child("address").getValue());
                                        if(!payment.equals("Cash on Delivery"))
                                        {
                                            payment="Paytm";
                                        }
                                        items.add(new MyOrdersModel(dsp.getKey(),date,"Rs "+total,payment,address));
                                    }
                                }
                                adapter = new MyOrdersAdapter(items, getApplicationContext());
                                list.setAdapter(adapter);
                                progressBar.setVisibility(View.INVISIBLE);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
                                progressBar.setLayoutParams(params);
                                if(isOnline()) {
                                    if (items.isEmpty()) {
                                        findViewById(R.id.MyOrders).setVisibility(View.GONE);
                                        findViewById(R.id.no_product).setVisibility(View.VISIBLE);
                                    } else {
                                        findViewById(R.id.MyOrders).setVisibility(View.VISIBLE);
                                        findViewById(R.id.no_product).setVisibility(View.GONE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });
                        swipeView2.setRefreshing(false);

                    }
                }, 3000);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(this,Home.class));
            finish();
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){

            case R.id.nav_home:
                Intent h= new Intent(MyOrders.this,Home.class);
                startActivity(h);
                finish();
                break;
            case R.id.nav_my_products:
                Intent m= new Intent(MyOrders.this,MyProducts.class);
                startActivity(m);
                finish();
                break;
            case R.id.nav_about:
                Intent i= new Intent(MyOrders.this,About.class);
                startActivity(i);
                finish();
                break;
            case R.id.nav_account:
                Intent g= new Intent(MyOrders.this,profile.class);
                startActivity(g);
                finish();
                break;
            case R.id.nav_contact:
                Intent s= new Intent(MyOrders.this,Contact.class);
                startActivity(s);
                finish();
                break;
            case R.id.nav_terms:
                Intent t= new Intent(MyOrders.this,Terms.class);
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

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
}
