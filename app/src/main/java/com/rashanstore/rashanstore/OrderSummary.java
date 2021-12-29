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

public class OrderSummary extends AppCompatActivity {

    DrawerLayout drawer;
    NavigationView navigationView;
    ArrayList items;
    GridView list;
    private OrderAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);


        final TextView total=findViewById(R.id.total);
        final TextView id=findViewById(R.id.orderid);
        final TextView date=findViewById(R.id.date);
        final TextView payment=findViewById(R.id.payment);
        final TextView address=findViewById(R.id.address);

        items = new ArrayList();

        final FirebaseDatabase database=FirebaseDatabase.getInstance();

        list = findViewById(R.id.list);

        Bundle extras = getIntent().getExtras();
        String orderid = extras.getString("orderid");
        id.setText(orderid);

        final DatabaseReference dbRef = database.getReference().child("OrdersReceived").child(orderid);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                total.setText(String.valueOf(dataSnapshot.child("TXNAMOUNT").getValue()));
                date.setText(String.valueOf(dataSnapshot.child("TXNDATE").getValue()));
                address.setText(String.valueOf(dataSnapshot.child("address").getValue()));

                if(!String.valueOf(dataSnapshot.child("PAYMENTMODE").getValue()).equals("Cash on Delivery")) {
                   payment.setText("Paytm");
                }
                else
                {
                    payment.setText(dataSnapshot.child("PAYMENTMODE").getValue().toString());
                }
                items.clear();
                for (DataSnapshot dsp : dataSnapshot.child("Products").getChildren())
                {
                        String name = String.valueOf(dsp.child("productName").getValue());
                        String category = String.valueOf(dsp.child("category").getValue());
                        String qty = String.valueOf(dsp.child("qty").getValue());
                        String price = String.valueOf(dsp.child("price").getValue());
                        String amount=String.valueOf(Integer.valueOf(qty)*Integer.valueOf(price));
                        items.add(new OrderModel(name,qty,amount,category));
                }
                adapter = new OrderAdapter(items, getApplicationContext());
                list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(OrderSummary.this,MyOrders.class));
        finish();
    }
}
