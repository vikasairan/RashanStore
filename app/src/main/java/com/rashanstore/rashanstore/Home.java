package com.rashanstore.rashanstore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.paytm.pgsdk.easypay.manager.PaytmAssist.getContext;
import static com.rashanstore.rashanstore.Login.getPermissions;
import static com.rashanstore.rashanstore.Login.hasPermissions;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ViewPagerEx.OnPageChangeListener, BaseSliderView.OnSliderClickListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar=null;
    ImageView addItem, cart, addNew;
    ArrayList items;
    GridView list;
    private productAdapter adapter;
    private static final int PERMISSION_ALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addItem =findViewById(R.id.addItem);
        addNew = findViewById(R.id.addNew);
        cart = findViewById(R.id.cart);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        String[] PERMISSIONS = new String[0];
        try {
            PERMISSIONS = getPermissions(getApplicationContext());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        final ImageView profile_image  = findViewById(R.id.profile_image);
        final TextView name=findViewById(R.id.name);
        TextView phone=findViewById(R.id.phone);

        final FirebaseAuth auth=FirebaseAuth.getInstance();
        String phone_user=auth.getCurrentUser().getPhoneNumber();
        if(phone_user.charAt(0)==0)
        {
            phone_user=phone_user.substring(1);
        }
        else if(phone_user.startsWith("+91"))
        {
            phone_user=phone_user.replace("+91","");
        }
        phone.setText(phone_user);
        FirebaseDatabase mydatabase = FirebaseDatabase.getInstance();
        DatabaseReference mydbRef = mydatabase.getReference().child("User").child(phone_user);
        mydbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name.setText(String.valueOf(dataSnapshot.child("name").getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        File dir_nav = Environment.getExternalStorageDirectory();
        final File folder_nav = new File(dir_nav, "Rashan Store");
        if (!folder_nav.exists()) {
            folder_nav.mkdirs();
        }
        File dir_profile = Environment.getExternalStorageDirectory();
        File folder_profile = new File(dir_profile, "Rashan Store/Profile Pictures");
        if (!folder_profile.exists()) {
            folder_profile.mkdirs();
        }
        final String finalPhone_user = phone_user;

        final File localFile = new File(folder_profile,phone_user + ".jpg");

        if(localFile.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;

            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath(),options);
            profile_image.setImageBitmap(bitmap);
        }
        else
        {
            StorageReference mStorageRef;
            File dir = Environment.getExternalStorageDirectory();
            final File folder = new File(dir, "Rashan Store/Profile Pictures");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            final File localFile_nav = new File(folder,finalPhone_user+".jpg");
            mStorageRef = FirebaseStorage.getInstance().getReference();
            mStorageRef.child("Profile Pictures/" + finalPhone_user + ".jpg").getFile(localFile_nav).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;

                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath(),options);
                    profile_image.setImageBitmap(bitmap);
                    }});
        }

        items = new ArrayList();

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
        progressBar.setVisibility(View.VISIBLE);
        findViewById(R.id.search_bar).setVisibility(View.GONE);
        if(!isOnline())
        {
            progressBar.setVisibility(View.GONE);
            findViewById(R.id.home).setVisibility(View.GONE);
            findViewById(R.id.internet).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.home).setVisibility(View.VISIBLE);
            findViewById(R.id.internet).setVisibility(View.GONE);
        }


        findViewById(R.id.shop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation bottomUp = AnimationUtils.loadAnimation(Home.this, R.anim.bottom_up);
                LinearLayout shoppanel=findViewById(R.id.shop_panel);
                shoppanel.startAnimation(bottomUp);
                shoppanel.setVisibility(View.VISIBLE);
                findViewById(R.id.home).setVisibility(View.GONE);
            }
        });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation bottomdown = AnimationUtils.loadAnimation(Home.this, R.anim.bottom_down);
                findViewById(R.id.shop_panel).startAnimation(bottomdown);
                findViewById(R.id.shop_panel).setVisibility(View.GONE);
                findViewById(R.id.home).setVisibility(View.VISIBLE);
            }
        });




        FirebaseDatabase database = FirebaseDatabase.getInstance();
        list = findViewById(R.id.list);
        final DatabaseReference dbRef = database.getReference().child("Products");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();
                for (DataSnapshot dsp : dataSnapshot.getChildren())
                {
                    String name = String.valueOf(dsp.child("productname").getValue());
                    String companyName = String.valueOf(dsp.child("companyName").getValue());
                    String qty= String.valueOf(dsp.child("qty").getValue());
                    String price = String.valueOf(dsp.child("price").getValue());
                    String description = String.valueOf(dsp.child("description").getValue());
                    if(String.valueOf(dsp.child("category").getValue()).equals("Loose Item"))
                    {
                        items.add(new productModel(name,companyName,description,"QTY: "+qty+" Kg","Rs "+price,dsp.getKey()));
                    }
                    else {

                        items.add(new productModel(name,companyName,description,"QTY: "+qty,"Rs "+price,dsp.getKey()));
                    }

                    File dir = Environment.getExternalStorageDirectory();
                    final File folder = new File(dir, "Rashan Store/Display Pictures");
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    final File localFile = new File(folder,dsp.getKey()+".jpg");
                    if(!localFile.exists()) {
                    final StorageReference mStorageRef;
                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    mStorageRef.child("Display Pictures/" + dsp.getKey() + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    }});}
                }
                Collections.shuffle(items, new Random(System.currentTimeMillis()));
                adapter = new productAdapter(items, getApplicationContext());
                list.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
                findViewById(R.id.search_bar).setVisibility(View.VISIBLE);
                if (items.isEmpty())
                {
                    findViewById(R.id.list).setVisibility(View.GONE);
                    findViewById(R.id.no_product).setVisibility(View.VISIBLE);
                }
                else
                {
                    findViewById(R.id.list).setVisibility(View.VISIBLE);
                    findViewById(R.id.no_product).setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                
            }

        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                productModel Model = (productModel) items.get(position);
                String product_id=Model.id;
                Bundle bundle = new Bundle();
                bundle.putString("id", product_id);
                Intent i=new Intent(Home.this,ProductInformation.class);
                i.putExtras(bundle);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_top,R.anim.slide_out_bottom);
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
                            findViewById(R.id.home).setVisibility(View.GONE);
                            findViewById(R.id.internet).setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            findViewById(R.id.home).setVisibility(View.VISIBLE);
                            findViewById(R.id.internet).setVisibility(View.GONE);
                        }

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        list = findViewById(R.id.list);
                        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
                        progressBar.setVisibility(View.VISIBLE);

                        items = new ArrayList();
                        final DatabaseReference dbRef = database.getReference().child("Products");
                        dbRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                items.clear();
                                for (DataSnapshot dsp : dataSnapshot.getChildren())
                                {
                                    String name = String.valueOf(dsp.child("productname").getValue());
                                    String companyName = String.valueOf(dsp.child("companyName").getValue());
                                    String qty= String.valueOf(dsp.child("qty").getValue());
                                    String price = String.valueOf(dsp.child("price").getValue());
                                    String description = String.valueOf(dsp.child("description").getValue());
                                    if(String.valueOf(dsp.child("category").getValue()).equals("Loose Item"))
                                    {
                                        items.add(new productModel(name,companyName,description,"QTY: "+qty+" Kg","Rs "+price,dsp.getKey()));
                                    }
                                    else {

                                        items.add(new productModel(name,companyName,description,"QTY: "+qty,"Rs "+price,dsp.getKey()));
                                    }
                                    File dir = Environment.getExternalStorageDirectory();
                                    final File folder = new File(dir, "Rashan Store/Display Pictures");
                                    if (!folder.exists()) {
                                        folder.mkdirs();
                                    }
                                    final File localFile = new File(folder,dsp.getKey()+".jpg");
                                    if(!localFile.exists()) {
                                        final StorageReference mStorageRef;
                                        mStorageRef = FirebaseStorage.getInstance().getReference();
                                        mStorageRef.child("Display Pictures/" + dsp.getKey() + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            }});}
                                    else
                                    {

                                    }
                                }
                                Collections.shuffle(items, new Random(System.currentTimeMillis()));
                                adapter = new productAdapter(items, getApplicationContext());
                                list.setAdapter(adapter);
                                progressBar.setVisibility(View.GONE);
                                if (items.isEmpty()) {
                                    findViewById(R.id.list).setVisibility(View.GONE);
                                    findViewById(R.id.no_product).setVisibility(View.VISIBLE);
                                } else {
                                    findViewById(R.id.list).setVisibility(View.VISIBLE);
                                    findViewById(R.id.no_product).setVisibility(View.GONE);
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

        final EditText search=findViewById(R.id.search);

        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                String text = search.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }
        });


        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        Intent addNew = new Intent(Home.this, BestOffers.class);
                        startActivity(addNew);
                        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

            }
        });
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent addItem = new Intent(Home.this, AddItem.class);
                startActivity(addItem);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

            }
        });
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent cart = new Intent(Home.this, Cart.class);
                startActivity(cart);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

            }
        });
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){

            case R.id.nav_home:
                Intent h= new Intent(Home.this,Home.class);
                startActivity(h);
                finish();
                break;
            case R.id.nav_my_products:
                Intent m= new Intent(Home.this,MyProducts.class);
                startActivity(m);
                finish();
                break;
            case R.id.nav_my_orders:
                Intent o= new Intent(this,MyOrders.class);
                startActivity(o);
                finish();
                break;
            case R.id.nav_about:
                Intent i= new Intent(Home.this,About.class);
                startActivity(i);
                finish();
                break;
            case R.id.nav_account:
                Intent g= new Intent(Home.this,profile.class);
                startActivity(g);
                finish();
                break;
            case R.id.nav_contact:
                Intent s= new Intent(Home.this,Contact.class);
                startActivity(s);
                finish();
                break;
            case R.id.nav_terms:
                Intent t= new Intent(Home.this,Terms.class);
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
