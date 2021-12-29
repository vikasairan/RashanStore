package com.rashanstore.rashanstore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.EditText;
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

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;


public class profile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        clearDim(root);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView name=findViewById(R.id.name);
        final TextView contact=findViewById(R.id.contact);
        final ImageView img=findViewById(R.id.image);

        FirebaseAuth auth=FirebaseAuth.getInstance();
        String phone=auth.getCurrentUser().getPhoneNumber();
        if(phone.charAt(0)==0)
        {
            phone=phone.substring(1);
        }
        else if(phone.startsWith("+91"))
        {
            phone=phone.replace("+91","");
        }
        contact.setText(phone);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference().child("User").child(phone);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                name.setText(String.valueOf(dataSnapshot.child("name").getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }});


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


                        FirebaseAuth auth=FirebaseAuth.getInstance();
                        String phone=auth.getCurrentUser().getPhoneNumber();
                        if(phone.charAt(0)==0)
                        {
                            phone=phone.substring(1);
                        }
                        else if(phone.startsWith("+91"))
                        {
                            phone=phone.replace("+91","");
                        }
                        contact.setText(phone);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference dbRef = database.getReference().child("User").child(phone);
                        dbRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                name.setText(String.valueOf(dataSnapshot.child("name").getValue()));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }});



                        swipeView.setRefreshing(false);

                    }
                }, 3000);
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
        final File localFile = new File(folder_profile,phone + ".jpg");

        if(localFile.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;

            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath(),options);
            img.setImageBitmap(bitmap);
        }
        else
        {
            StorageReference mStorageRef;
            File dir = Environment.getExternalStorageDirectory();
            final File folder = new File(dir, "Rashan Store/Profile Pictures");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            final File localFile_nav = new File(folder,phone+".jpg");
            mStorageRef = FirebaseStorage.getInstance().getReference();
            mStorageRef.child("Profile Pictures/" + phone + ".jpg").getFile(localFile_nav).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;

                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath(),options);
                    img.setImageBitmap(bitmap);
                }});
        }

        final String finalPhone = phone;
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(profile.this, UploadImage.class);
                i.putExtra("path","Profile Pictures/" + finalPhone + ".jpg");
                startActivity(i);
            }
        });


        final LinearLayout edit=findViewById(R.id.edit_profile);
        edit.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!isFinishing()) {


                                                    applyDim(root, 0.8f);
                                                    LayoutInflater layoutInflater = (LayoutInflater) profile.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                    View customView = layoutInflater.inflate(R.layout.edit_popup, null);
                                                    Button cancel = (Button) customView.findViewById(R.id.cancel);
                                                    Button submit = (Button) customView.findViewById(R.id.submit);

                                                    final PopupWindow popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                                                    LinearLayout layout = findViewById(R.id.profile);
                                                    Display display = getWindowManager().getDefaultDisplay();
                                                    Point size = new Point();
                                                    display.getSize(size);
                                                    int width = size.x;
                                                    popupWindow.setWidth(width - 50);

                                                    popupWindow.setOutsideTouchable(true);
                                                    popupWindow.update();


                                                    final EditText edit_name = customView.findViewById(R.id.edit_name);
                                                    final EditText edit_phone = customView.findViewById(R.id.edit_phone);

                                                    edit_name.setText(name.getText().toString());
                                                    edit_phone.setText(contact.getText().toString());

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

                                                            if (!edit_phone.getText().toString().equals(contact.getText().toString())) {
                                                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                final DatabaseReference dbRef = database.getReference().child("User");
                                                                dbRef.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        if (!dataSnapshot.child(edit_phone.getText().toString().trim()).exists()) {
                                                                            Intent intent = new Intent(profile.this, edit_phone_verification.class);
                                                                            intent.putExtra("mobile", edit_phone.getText().toString().trim());
                                                                            intent.putExtra("oldmobile", contact.getText().toString().trim());
                                                                            intent.putExtra("name", edit_name.getText().toString().trim());
                                                                            intent.putExtra("gstNumber", String.valueOf(dataSnapshot.child(contact.getText().toString()).child("gstNumber").getValue()));
                                                                            intent.putExtra("panNumber", String.valueOf(dataSnapshot.child(contact.getText().toString()).child("panNumber").getValue()));
                                                                            intent.putExtra("shopName", String.valueOf(dataSnapshot.child(contact.getText().toString()).child("shopName").getValue()));
                                                                            intent.putExtra("state", String.valueOf(dataSnapshot.child(contact.getText().toString()).child("state").getValue()));
                                                                            intent.putExtra("address", String.valueOf(dataSnapshot.child(contact.getText().toString()).child("address").getValue()));
                                                                            intent.putExtra("password", String.valueOf(dataSnapshot.child(contact.getText().toString()).child("password").getValue()));
                                                                            intent.putExtra("type", String.valueOf(dataSnapshot.child(contact.getText().toString()).child("type").getValue()));

                                                                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                                                            mAuth.signOut();
                                                                            startActivity(intent);
                                                                            finish();
                                                                        } else {
                                                                            Toast.makeText(profile.this, "Account with Mobile number already exist. Please try different number", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });

                                                            } else
                                                                {
                                                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                DatabaseReference dbRef = database.getReference().child("User").child(contact.getText().toString());
                                                                dbRef.child("name").setValue(edit_name.getText().toString());
                                                            }
                                                            popupWindow.dismiss();
                                                            clearDim(root);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
            }
        );

        final LinearLayout edit_address=findViewById(R.id.address);
        edit_address.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (!isFinishing()) {

                                                            applyDim(root, 0.8f);
                                                            LayoutInflater layoutInflater = (LayoutInflater) profile.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


                                                            View customView = layoutInflater.inflate(R.layout.edit_address_popup, null);


                                                            Button cancel = (Button) customView.findViewById(R.id.cancel);
                                                            Button submit = (Button) customView.findViewById(R.id.submit);

                                                            final PopupWindow popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                                                            LinearLayout layout = findViewById(R.id.profile);


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
                                                                    FirebaseAuth auth=FirebaseAuth.getInstance();
                                                                    String phone=auth.getCurrentUser().getPhoneNumber();
                                                                    if(phone.charAt(0)==0)
                                                                    {
                                                                        phone=phone.substring(1);
                                                                    }
                                                                    else if(phone.startsWith("+91"))
                                                                    {
                                                                        phone=phone.replace("+91","");
                                                                    }

                                                                    if (!String.valueOf(dataSnapshot.child(phone).child("address").getValue()).equals(""))
                                                                    {
                                                                        edit_address.setText(String.valueOf(dataSnapshot.child(phone).child("address").getValue()));
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
                                                                        dbRef.child(contact.getText().toString()).child("address").setValue(edit_address.getText().toString().trim());
                                                                        popupWindow.dismiss();
                                                                        clearDim(root);
                                                                    } else {
                                                                        Toast.makeText(getApplicationContext(), "Invalid Address", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }
                                                });
                                            }
                                        }

        );

        final LinearLayout edit_password=findViewById(R.id.resetpassword);
        edit_password.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (!isFinishing()) {

                                                            applyDim(root, 0.8f);
                                                            LayoutInflater layoutInflater = (LayoutInflater) profile.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


                                                            View customView = layoutInflater.inflate(R.layout.edit_password, null);


                                                            Button cancel = (Button) customView.findViewById(R.id.cancel);
                                                            Button submit = (Button) customView.findViewById(R.id.submit);

                                                            final PopupWindow popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                                                            LinearLayout layout = findViewById(R.id.profile);


                                                            Display display = getWindowManager().getDefaultDisplay();
                                                            Point size = new Point();
                                                            display.getSize(size);
                                                            int width = size.x;
                                                            popupWindow.setWidth(width - 50);

                                                            popupWindow.setOutsideTouchable(true);
                                                            popupWindow.update();


                                                            final EditText edit_password = customView.findViewById(R.id.edit_password);

                                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                            final DatabaseReference dbRef = database.getReference().child("User");


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

                                                                    if (String.valueOf(edit_password.getText()).length()>=6) {

                                                                        FirebaseAuth auth=FirebaseAuth.getInstance();

                                                                        String phone=auth.getCurrentUser().getPhoneNumber();
                                                                        if(phone.charAt(0)==0)
                                                                        {
                                                                            phone=phone.substring(1);
                                                                        }
                                                                        else if(phone.startsWith("+91"))
                                                                        {
                                                                            phone=phone.replace("+91","");
                                                                        }

                                                                        dbRef.child(phone).child("password").setValue(edit_password.getText().toString().trim());
                                                                        popupWindow.dismiss();
                                                                        clearDim(root);
                                                                        Toast.makeText(profile.this, "Password successfully changed", Toast.LENGTH_SHORT).show();

                                                                    } else {
                                                                        popupWindow.dismiss();
                                                                        clearDim(root);
                                                                        Toast.makeText(profile.this, "Password too short. Minimum length is 6", Toast.LENGTH_SHORT).show();

                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }
                                                });
                                            }
                                        }

        );


        LinearLayout logout=findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            FirebaseAuth auth=FirebaseAuth.getInstance();
            auth.signOut();
            startActivity(new Intent(profile.this, Login.class));
            finish();
            }});

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){

            case R.id.nav_home:
                Intent h= new Intent(profile.this,Home.class);
                startActivity(h);
                finish();
                break;
            case R.id.nav_my_products:
                Intent m= new Intent(this,MyProducts.class);
                startActivity(m);
                finish();
                break;
            case R.id.nav_my_orders:
                Intent o= new Intent(this,MyOrders.class);
                startActivity(o);
                finish();
                break;
            case R.id.nav_about:
                Intent i= new Intent(profile.this,About.class);
                startActivity(i);
                finish();
                break;
            case R.id.nav_account:
                Intent g= new Intent(profile.this,profile.class);
                startActivity(g);
                finish();
                break;
            case R.id.nav_contact:
                Intent s= new Intent(profile.this,Contact.class);
                startActivity(s);
                finish();
                break;
            case R.id.nav_terms:
                Intent t= new Intent(profile.this,Terms.class);
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
