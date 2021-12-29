package com.rashanstore.rashanstore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.math.MathContext;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.math.MathUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Cart extends AppCompatActivity{

    GridView list;
    ArrayList items;
    CartAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        clearDim(root);

        list = findViewById(R.id.list);
        items = new ArrayList();
        final FirebaseAuth auth=FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        String mobile=auth.getCurrentUser().getPhoneNumber();
        if(mobile.startsWith("0"))
        {
            mobile=mobile.substring(1);
        }
        if(mobile.startsWith("+91"))
        {
            mobile=mobile.replace("+91","");
        }


        DatabaseReference myRef = database.getReference().child("Cart").child(mobile);
        final DatabaseReference finalMyRef1 = myRef;
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();
                for (DataSnapshot dsp : dataSnapshot.getChildren())
                {
                    if(!String.valueOf(dsp.child("qty").getValue()).equals("0"))
                    {
                        items.add(new CartModel(dsp.getKey(), String.valueOf(dsp.child("qty").getValue()),String.valueOf(dsp.child("price").getValue())));
                    }
                    else
                    {
                        finalMyRef1.child(dsp.getKey()).setValue(null);
                    }
                }
                adapter = new CartAdapter(items, getApplicationContext());
                list.setAdapter(adapter);
                if(items.isEmpty())
                {
                    findViewById(R.id.cart).setVisibility(View.GONE);
                    findViewById(R.id.empty_cart).setVisibility(View.VISIBLE);
                }
                else
                {
                    findViewById(R.id.cart).setVisibility(View.VISIBLE);
                    findViewById(R.id.empty_cart).setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        final TextView total_amount = findViewById(R.id.total_amount);

        myRef = database.getReference().child("Cart").child(mobile);
        final DatabaseReference finalMyRef = myRef;
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        for (final DataSnapshot dsp : dataSnapshot.getChildren()) {

                            final DatabaseReference dbRef = database.getReference().child("Products").child(dsp.getKey());
                            dbRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    finalMyRef.child(dsp.getKey()).child("price").setValue(String.valueOf(dataSnapshot.child("price").getValue()));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }

                            });

                        }
                        total_amount.setText("0");

                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            if (!String.valueOf(total_amount.getText()).equals("") && !String.valueOf(dsp.child("qty").getValue()).equals("null") && !String.valueOf(dsp.child("price").getValue()).equals("null")) {
                                int amount = Integer.valueOf(total_amount.getText().toString());
                                total_amount.setText(String.valueOf(amount + Integer.valueOf(String.valueOf(dsp.child("qty").getValue())) * Integer.valueOf(String.valueOf(dsp.child("price").getValue()))));
                            }
                        }
                    }});
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


        if(!isOnline())
        {
            findViewById(R.id.internet).setVisibility(View.VISIBLE);
            findViewById(R.id.cart).setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.internet).setVisibility(View.GONE);
            findViewById(R.id.cart).setVisibility(View.VISIBLE);
            if(items.isEmpty())
            {
                findViewById(R.id.cart).setVisibility(View.GONE);
                findViewById(R.id.empty_cart).setVisibility(View.VISIBLE);
            }
            else
            {
                findViewById(R.id.cart).setVisibility(View.VISIBLE);
                findViewById(R.id.empty_cart).setVisibility(View.GONE);
            }
        }

        final SwipeRefreshLayout swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeView.setColorScheme(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_green_light);
        final String finalMobile = mobile;
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(!isOnline())
                        {
                            findViewById(R.id.internet).setVisibility(View.VISIBLE);
                            findViewById(R.id.cart).setVisibility(View.GONE);
                        }
                        else
                        {
                            findViewById(R.id.internet).setVisibility(View.GONE);
                            findViewById(R.id.cart).setVisibility(View.VISIBLE);
                        }

                        final DatabaseReference myRef = database.getReference().child("Cart").child(finalMobile);
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                items.clear();
                                for (final DataSnapshot dsp : dataSnapshot.getChildren()) {
                                    if(!String.valueOf(dsp.child("qty").getValue()).equals("0")) {
                                        items.add(new CartModel(dsp.getKey(), dsp.child("qty").getValue().toString(),dsp.child("price").getValue().toString()));
                                    }
                                    else
                                    {
                                        myRef.child(dsp.getKey()).setValue(null);
                                    }
                                }
                                adapter = new CartAdapter(items, getApplicationContext());
                                list.setAdapter(adapter);
                                if(items.isEmpty())
                                {
                                    findViewById(R.id.cart).setVisibility(View.GONE);
                                    findViewById(R.id.empty_cart).setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    findViewById(R.id.cart).setVisibility(View.VISIBLE);
                                    findViewById(R.id.empty_cart).setVisibility(View.GONE);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        swipeView.setRefreshing(false);
                    }
                }, 3000);
            }
        });
        final SwipeRefreshLayout swipeView1 = (SwipeRefreshLayout) findViewById(R.id.swipe1);
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
                            findViewById(R.id.internet).setVisibility(View.VISIBLE);
                            findViewById(R.id.cart).setVisibility(View.GONE);
                        }
                        else
                        {
                            findViewById(R.id.internet).setVisibility(View.GONE);
                            findViewById(R.id.cart).setVisibility(View.VISIBLE);
                        }

                        final DatabaseReference myRef = database.getReference().child("Cart").child(finalMobile);
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                items.clear();
                                for (final DataSnapshot dsp : dataSnapshot.getChildren()) {
                                    if(!String.valueOf(dsp.child("qty").getValue()).equals("0")) {
                                        items.add(new CartModel(dsp.getKey(), dsp.child("qty").getValue().toString(),dsp.child("price").getValue().toString()));
                                    }
                                    else
                                    {
                                        myRef.child(dsp.getKey()).setValue(null);
                                    }
                                }
                                adapter = new CartAdapter(items, getApplicationContext());
                                list.setAdapter(adapter);
                                if(items.isEmpty())
                                {
                                    findViewById(R.id.cart).setVisibility(View.GONE);
                                    findViewById(R.id.empty_cart).setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    findViewById(R.id.empty_cart).setVisibility(View.GONE);
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



        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                CartModel Model = (CartModel) items.get(position);
                String product_id=Model.id;
                Bundle bundle = new Bundle();
                bundle.putString("id", product_id);
                Intent i=new Intent(Cart.this,ProductInformation.class);
                i.putExtras(bundle);
                startActivity(i);
            }

        });
        findViewById(R.id.pay).setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                TextView view= (TextView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x50ffffff, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        if(!String.valueOf(total_amount.getText()).equals("0"))
                        {

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

                                        Bundle bundle = new Bundle();
                                        bundle.putString("amount", String.valueOf(total_amount.getText()));
                                        Intent i = new Intent(Cart.this, Checkout.class);
                                        i.putExtras(bundle);
                                        startActivity(i);

                                    } else {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!isFinishing()) {

                                                    final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
                                                    applyDim(root, 0.8f);
                                                    LayoutInflater layoutInflater = (LayoutInflater) Cart.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                    View customView = layoutInflater.inflate(R.layout.gst_pan_verification, null);
                                                    Button cancel = (Button) customView.findViewById(R.id.cancel);
                                                    Button submit = (Button) customView.findViewById(R.id.submit);

                                                    final PopupWindow popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                                                    LinearLayout layout = findViewById(R.id.cartscreen);
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

                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putString("amount", String.valueOf(total_amount.getText()));
                                                                    Intent i = new Intent(Cart.this, Checkout.class);
                                                                    i.putExtras(bundle);
                                                                    startActivity(i);

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
                                                }
                                            }
                                        }
                                        );

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });




                        }
                        else
                        {
                            Toast.makeText(Cart.this, "Cart Empty. Add Products and Continue.", Toast.LENGTH_SHORT).show();
                        }
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

        startActivity(new Intent(Cart.this,Home.class));

        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        finish();
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