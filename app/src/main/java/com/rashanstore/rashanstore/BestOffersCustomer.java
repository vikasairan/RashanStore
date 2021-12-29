package com.rashanstore.rashanstore;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class BestOffersCustomer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar = null;

    ArrayList items;
    ListView list;
    private BestOffersAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best_offers_customer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        File dir = Environment.getExternalStorageDirectory();
        final File folder = new File(dir, "Rashan Store/Advertisements");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("Products");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot dsp : dataSnapshot.getChildren())
                {

                    final File localFile = new File(folder,dsp.getKey()+".jpg");
                    if(!localFile.exists()) {
                        final StorageReference mStorageRef;
                        mStorageRef = FirebaseStorage.getInstance().getReference();

                        mStorageRef.child("Advertisements/" + dsp.getKey() + ".jpeg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                mStorageRef.child("Advertisements/" + dsp.getKey() + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        mStorageRef.child("Advertisements/" + dsp.getKey() + ".png").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {

                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        items = new ArrayList();

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
        progressBar.setVisibility(View.VISIBLE);

        if(!isOnline())
        {
            progressBar.setVisibility(View.GONE);
            findViewById(R.id.internet).setVisibility(View.VISIBLE);
            findViewById(R.id.bestoffers).setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.internet).setVisibility(View.GONE);
            findViewById(R.id.bestoffers).setVisibility(View.VISIBLE);
            if (items.isEmpty()) {
                findViewById(R.id.bestoffers).setVisibility(View.GONE);
                findViewById(R.id.no_product).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.bestoffers).setVisibility(View.VISIBLE);
                findViewById(R.id.no_product).setVisibility(View.GONE);
            }
        }



        list = (ListView) findViewById(R.id.list);

        myRef = database.getReference().child("Products");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                items.clear();
                for (DataSnapshot dsp : dataSnapshot.getChildren())
                {
                    String name = String.valueOf(dsp.child("productname").getValue());
                    String companyName = String.valueOf(dsp.child("companyName").getValue());
                    String description = String.valueOf(dsp.child("description").getValue());

                    File localFile = new File(folder,dsp.getKey()+".jpg");
                    if(localFile.exists())
                    {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        items.add(new BestOffersModel(BitmapFactory.decodeFile(localFile.getAbsolutePath(),options),dsp.getKey(),name,companyName,description));
                    }
                }
                Collections.shuffle(items, new Random(System.currentTimeMillis()));
                adapter = new BestOffersAdapter(items, getApplicationContext());
                list.setAdapter(adapter);
                progressBar.setVisibility(View.INVISIBLE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
                progressBar.setLayoutParams(params);
                if (items.isEmpty()) {
                    findViewById(R.id.bestoffers).setVisibility(View.GONE);
                    findViewById(R.id.no_product).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.bestoffers).setVisibility(View.VISIBLE);
                    findViewById(R.id.no_product).setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });



        final SwipeRefreshLayout swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeView.setColorScheme(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_green_light);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(!isOnline())
                        {
                            progressBar.setVisibility(View.GONE);
                            findViewById(R.id.internet).setVisibility(View.VISIBLE);
                            findViewById(R.id.bestoffers).setVisibility(View.GONE);
                        }
                        else
                        {
                            findViewById(R.id.internet).setVisibility(View.GONE);
                            findViewById(R.id.bestoffers).setVisibility(View.VISIBLE);
                        }
                        File dir = Environment.getExternalStorageDirectory();
                        final File folder = new File(dir, "Rashan Store/Advertisements");
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference().child("Products");
                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (final DataSnapshot dsp : dataSnapshot.getChildren())
                                {

                                    final File localFile = new File(folder,dsp.getKey()+".jpg");
                                    if(!localFile.exists()) {
                                        final StorageReference mStorageRef;
                                        mStorageRef = FirebaseStorage.getInstance().getReference();

                                        mStorageRef.child("Advertisements/" + dsp.getKey() + ".jpeg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                mStorageRef.child("Advertisements/" + dsp.getKey() + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        mStorageRef.child("Advertisements/" + dsp.getKey() + ".png").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {

                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });

                        list = (ListView) findViewById(R.id.list);
                        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
                        progressBar.setVisibility(View.VISIBLE);

                        items = new ArrayList();
                        myRef = database.getReference().child("Products");
                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                items.clear();
                                for (DataSnapshot dsp : dataSnapshot.getChildren())
                                {
                                    String name = String.valueOf(dsp.child("productname").getValue());
                                    String companyName = String.valueOf(dsp.child("companyName").getValue());
                                    String description = String.valueOf(dsp.child("description").getValue());

                                    File localFile = new File(folder,dsp.getKey()+".jpg");
                                    if(localFile.exists())
                                    {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inSampleSize = 2;

                                        items.add(new BestOffersModel(BitmapFactory.decodeFile(localFile.getAbsolutePath(),options),dsp.getKey(),name,companyName,description));
                                    }
                                }
                                Collections.shuffle(items, new Random(System.currentTimeMillis()));
                                adapter = new BestOffersAdapter(items, getApplicationContext());
                                list.setAdapter(adapter);
                                progressBar.setVisibility(View.INVISIBLE);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
                                progressBar.setLayoutParams(params);
                                if (items.isEmpty()) {
                                    findViewById(R.id.bestoffers).setVisibility(View.GONE);
                                    findViewById(R.id.no_product).setVisibility(View.VISIBLE);
                                } else {
                                    findViewById(R.id.bestoffers).setVisibility(View.VISIBLE);
                                    findViewById(R.id.no_product).setVisibility(View.GONE);
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
                            progressBar.setVisibility(View.GONE);
                            findViewById(R.id.internet).setVisibility(View.VISIBLE);
                            findViewById(R.id.bestoffers).setVisibility(View.GONE);
                        }
                        else
                        {
                            findViewById(R.id.internet).setVisibility(View.GONE);
                            findViewById(R.id.bestoffers).setVisibility(View.VISIBLE);
                        }

                        File dir = Environment.getExternalStorageDirectory();
                        final File folder = new File(dir, "Rashan Store/Advertisements");
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference().child("Products");
                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (final DataSnapshot dsp : dataSnapshot.getChildren())
                                {

                                    final File localFile = new File(folder,dsp.getKey()+".jpg");
                                    if(!localFile.exists()) {
                                        final StorageReference mStorageRef;
                                        mStorageRef = FirebaseStorage.getInstance().getReference();

                                        mStorageRef.child("Advertisements/" + dsp.getKey() + ".jpeg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                mStorageRef.child("Advertisements/" + dsp.getKey() + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        mStorageRef.child("Advertisements/" + dsp.getKey() + ".png").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {

                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });

                        list = (ListView) findViewById(R.id.list);
                        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
                        progressBar.setVisibility(View.VISIBLE);

                        items = new ArrayList();
                        myRef = database.getReference().child("Products");
                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                items.clear();
                                for (DataSnapshot dsp : dataSnapshot.getChildren())
                                {
                                    String name = String.valueOf(dsp.child("productname").getValue());
                                    String companyName = String.valueOf(dsp.child("companyName").getValue());
                                    String description = String.valueOf(dsp.child("description").getValue());

                                    File localFile = new File(folder,dsp.getKey()+".jpg");
                                    if(localFile.exists())
                                    {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inSampleSize = 2;

                                        items.add(new BestOffersModel(BitmapFactory.decodeFile(localFile.getAbsolutePath(),options),dsp.getKey(),name,companyName,description));
                                    }
                                }
                                Collections.shuffle(items, new Random(System.currentTimeMillis()));
                                adapter = new BestOffersAdapter(items, getApplicationContext());
                                list.setAdapter(adapter);
                                progressBar.setVisibility(View.INVISIBLE);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
                                progressBar.setLayoutParams(params);
                                if (items.isEmpty()) {
                                    findViewById(R.id.bestoffers).setVisibility(View.GONE);
                                    findViewById(R.id.no_product).setVisibility(View.VISIBLE);
                                } else {
                                    findViewById(R.id.bestoffers).setVisibility(View.VISIBLE);
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
    public boolean isOnline() {
        ConnectivityManager connectivityManager;
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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.nav_home:
                Intent h = new Intent(BestOffersCustomer.this, Customer.class);
                startActivity(h);
                finish();
                break;
            case R.id.nav_my_list:
                Intent m= new Intent(BestOffersCustomer.this,MyGroceryList.class);
                startActivity(m);
                finish();
                break;
            case R.id.nav_bestoffers:
                Intent l = new Intent(BestOffersCustomer.this, BestOffersCustomer.class);
                startActivity(l);
                finish();
                break;
            case R.id.nav_event:
                Intent e = new Intent(BestOffersCustomer.this, Event.class);
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
                    Toast.makeText(BestOffersCustomer.this, "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
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
