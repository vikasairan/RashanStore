package com.rashanstore.rashanstore;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class AddItem extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar=null;
    CircleImageView looseItem, packedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        looseItem = findViewById(R.id.looseItem);
        packedItem =findViewById(R.id.packedItem);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navHeader = navigationView.getHeaderView(0);


        final Button select=findViewById(R.id.select);
        select.getBackground().setColorFilter(0x90ffffff, PorterDuff.Mode.SRC_ATOP);
        select.invalidate();
        final Intent item = new Intent(AddItem.this, SellProduct.class);
        looseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                looseItem.setColorFilter(0x60000000, PorterDuff.Mode.SRC_ATOP);
                looseItem.invalidate();
                packedItem.clearColorFilter();
                packedItem.invalidate();
                select.getBackground().clearColorFilter();
                select.invalidate();

                item.putExtra("category","Loose Item");
            }
        });

        packedItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packedItem.setColorFilter(0x60000000, PorterDuff.Mode.SRC_ATOP);
                packedItem.invalidate();
                looseItem.clearColorFilter();
                looseItem.invalidate();
                select.getBackground().clearColorFilter();
                select.invalidate();
                item.putExtra("category","Packed Item");
            }
        });

        select.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if(item.getExtras()!=null&&item.getExtras().getString("category")!=null) {
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
                        startActivity(item);
                        break;
                    }
                }
                }
                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_top,R.anim.slide_out_bottom);

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
}
