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
import android.support.v7.widget.CardView;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class Checkout extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Bundle extras = getIntent().getExtras();
        final String amount = extras.getString("amount");

        TextView total=findViewById(R.id.total);
        total.setText(amount);

        final TextView address=findViewById(R.id.address);

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


        final DatabaseReference userRef = database.getReference().child("User").child(finalMobile);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               address.setText(String.valueOf(dataSnapshot.child("address").getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();


        ImageView edit=findViewById(R.id.edit_add);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      if (!isFinishing()) {

                                          applyDim(root, 0.8f);
                                          LayoutInflater layoutInflater = (LayoutInflater) Checkout.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                          View customView = layoutInflater.inflate(R.layout.edit_address_popup, null);
                                          Button cancel = (Button) customView.findViewById(R.id.cancel);
                                          Button submit = (Button) customView.findViewById(R.id.submit);

                                          final PopupWindow popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                                          LinearLayout layout = findViewById(R.id.checkout);
                                          Display display = getWindowManager().getDefaultDisplay();
                                          Point size = new Point();
                                          display.getSize(size);
                                          int width = size.x;
                                          popupWindow.setWidth(width - 50);

                                          popupWindow.setOutsideTouchable(true);
                                          popupWindow.update();


                                          final EditText edit_address = customView.findViewById(R.id.edit_address);

                                          FirebaseDatabase database = FirebaseDatabase.getInstance();
                                          final DatabaseReference dbRef = database.getReference().child("User");

                                          dbRef.addValueEventListener(new ValueEventListener() {
                                              @Override
                                              public void onDataChange(DataSnapshot dataSnapshot) {
                                                  if (!String.valueOf(dataSnapshot.child(finalMobile).child("address").getValue()).equals(""))
                                                  {
                                                      edit_address.setText(String.valueOf(dataSnapshot.child(finalMobile).child("address").getValue()));
                                                  }
                                              }

                                              @Override
                                              public void onCancelled(DatabaseError databaseError) {

                                              }
                                          });

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
                                                  if (!String.valueOf(edit_address.getText()).equals("")) {
                                                      dbRef.child(finalMobile).child("address").setValue(edit_address.getText().toString().trim());
                                                      popupWindow.dismiss();
                                                      clearDim(root);
                                                  } else {
                                                      Toast.makeText(getApplicationContext(), "Invalid Address", Toast.LENGTH_SHORT).show();
                                                  }
                                              }
                                          });

                                      }
                                  }
                              }
                );
            }
        });

        findViewById(R.id.cod).setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                CardView view= (CardView) v;
                switch (event.getAction())
                    {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x50000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();

                        FirebaseAuth auth=FirebaseAuth.getInstance();
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
                        final String finalMobile = mobile;
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String orderid=generateString();
                                final DatabaseReference myRef = database.getReference().child("Orders").child(orderid);
                                DatabaseReference dbRef = database.getReference().child("Cart").child(finalMobile);
                                myRef.child("TXNAMOUNT").setValue(amount);

                                DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                                String date = df.format(Calendar.getInstance().getTime());

                                myRef.child("TXNDATE").setValue(date);

                                myRef.child("PAYMENTMODE").setValue("Cash on Delivery");

                                myRef.child("mobile").setValue(finalMobile);

                                final DatabaseReference userRef = database.getReference().child("User").child(finalMobile);
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        myRef.child("address").setValue(String.valueOf(dataSnapshot.child("address").getValue()));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }

                                });

                                for (final DataSnapshot dsp : dataSnapshot.getChildren()) {
                                    if(!String.valueOf(dsp.child("qty").getValue()).equals("0")) {
                                        myRef.child("Products").child(dsp.getKey()).child("qty").setValue(dsp.child("qty").getValue());
                                        myRef.child("Products").child(dsp.getKey()).child("price").setValue(dsp.child("price").getValue());

                                        final DatabaseReference Ref = database.getReference().child("Products").child(dsp.getKey());
                                        Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                myRef.child("Products").child(dsp.getKey()).child("productName").setValue(dataSnapshot.child("productname").getValue().toString());
                                                myRef.child("Products").child(dsp.getKey()).child("category").setValue(dataSnapshot.child("category").getValue().toString());
                                                Ref.child("qty").setValue(String.valueOf(Integer.parseInt(String.valueOf(dataSnapshot.child("qty").getValue())) - Integer.parseInt(String.valueOf(dsp.child("qty").getValue()))));
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }

                                        });

                                        dbRef.child(dsp.getKey()).setValue(null);
                                    }

                                }

                                DatabaseReference Ref = database.getReference().child("Orders").child(orderid);
                                Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        DatabaseReference Orders = database.getReference().child("OrdersReceived").child(orderid);
                                        Orders.setValue(dataSnapshot.getValue());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }

                                });

                                Toast.makeText(Checkout.this, "Order Successfully Placed", Toast.LENGTH_LONG).show();
                                Intent i=new Intent(Checkout.this,OrderSummary.class);
                                i.putExtra("orderid",orderid);
                                startActivity(i);
                                finish();
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
        findViewById(R.id.paytm).setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                CardView view= (CardView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x50000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        startActivity(new Intent(Checkout.this,Paytm.class));

                        break;
                    }
                }
                return true;
            }
        });
        findViewById(R.id.gpay).setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                CardView view= (CardView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x50000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        startActivity(new Intent(Checkout.this,GPay.class));

                        break;
                    }
                }
                return true;
            }
        });
        findViewById(R.id.idfc).setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                CardView view= (CardView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x50000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        startActivity(new Intent(Checkout.this,IDFC.class));

                        break;
                    }
                }
                return true;
            }
        });
        findViewById(R.id.phonepe).setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                CardView view= (CardView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x50000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        startActivity(new Intent(Checkout.this,PhonePe.class));

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
        startActivity(new Intent(Checkout.this,Cart.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
    private String generateString() {
        Random rnd = new Random();
        int n1 = 10000000 + rnd.nextInt(90000000);
        int n2 = 10000000 + rnd.nextInt(90000000);
        return String.valueOf(n1)+String.valueOf(n2);
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