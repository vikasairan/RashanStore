package com.rashanstore.rashanstore;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import static android.view.View.GONE;
import static com.rashanstore.rashanstore.Login.getPermissions;
import static com.rashanstore.rashanstore.Login.hasPermissions;

public class Customer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar = null;

    private static final int PERMISSION_ALL = 1;
    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri filePath;
    private ImageView attachmentlist;
    private LinearLayout upload;
    private StorageReference storageReference;
    ArrayList items;
    ListView list;
    private BestOffersAdapter adapter;
    LinearLayout camera;
    ImageView check,cross;
    LinearLayout advertisementlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        clearDim(root);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        attachmentlist = findViewById(R.id.attachmentlist);
        upload=findViewById(R.id.upload);
        camera = findViewById(R.id.camera);
        check=findViewById(R.id.check);
        cross=findViewById(R.id.cross);
        advertisementlist=findViewById(R.id.advertisementlist);

        final LinearLayout edit_password = findViewById(R.id.resetpassword);
        edit_password.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {

                                                 runOnUiThread(new Runnable() {
                                                     @Override
                                                     public void run() {
                                                         if (!isFinishing()) {

                                                             applyDim(root, 0.8f);
                                                             LayoutInflater layoutInflater = (LayoutInflater) Customer.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


                                                             View customView = layoutInflater.inflate(R.layout.edit_password, null);


                                                             Button cancel = (Button) customView.findViewById(R.id.cancel);
                                                             Button submit = (Button) customView.findViewById(R.id.submit);

                                                             final PopupWindow popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                                                             LinearLayout layout = findViewById(R.id.customer);


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

                                                                     if (String.valueOf(edit_password.getText()).length() >= 6) {

                                                                         FirebaseAuth auth = FirebaseAuth.getInstance();

                                                                         String phone = auth.getCurrentUser().getPhoneNumber();
                                                                         if (phone.charAt(0) == 0) {
                                                                             phone = phone.substring(1);
                                                                         } else if (phone.startsWith("+91")) {
                                                                             phone = phone.replace("+91", "");
                                                                         }

                                                                         dbRef.child(phone).child("password").setValue(edit_password.getText().toString().trim());
                                                                         popupWindow.dismiss();
                                                                         clearDim(root);
                                                                         Toast.makeText(Customer.this, "Password successfully changed", Toast.LENGTH_SHORT).show();

                                                                     } else {
                                                                         popupWindow.dismiss();
                                                                         clearDim(root);
                                                                         Toast.makeText(Customer.this, "Password too short. Minimum length is 6", Toast.LENGTH_SHORT).show();

                                                                     }
                                                                 }
                                                             });

                                                         }

                                                     }
                                                 });
                                             }
                                         }

        );


        ImageView attachment = findViewById(R.id.attachment);
        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filePath != null) {
                    final ProgressDialog progressDialog = new ProgressDialog(Customer.this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String mobile = auth.getCurrentUser().getPhoneNumber();

                    if (mobile.startsWith("0")) {
                        mobile = mobile.substring(1);
                    }
                    if (mobile.startsWith("+91")) {
                        mobile = mobile.replace("+91", "");
                    }
                    SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
                    final String format = s.format(new Date());
                    String path = "GroceryList/" + mobile + "/" + format + ".jpg";

                    storageReference = FirebaseStorage.getInstance().getReference();

                    StorageReference ref = storageReference.child(path);
                    final String finalMobile = mobile;
                    ref.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("GroceryList").child(finalMobile);
                                    myRef.child(format).setValue(1);
                                    progressDialog.dismiss();
                                    Toast.makeText(Customer.this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                                    upload.setVisibility(GONE);
                                    attachmentlist.setImageResource(android.R.color.transparent);
                                    camera.setVisibility(View.VISIBLE);
                                    advertisementlist.setVisibility(View.VISIBLE);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(Customer.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                }
                            });
                } else {
                    Toast.makeText(Customer.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload.setVisibility(GONE);
                attachmentlist.setImageResource(android.R.color.transparent);
                camera.setVisibility(View.VISIBLE);
                advertisementlist.setVisibility(View.VISIBLE);
            }
        });
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
                for (final DataSnapshot dsp : dataSnapshot.getChildren()) {

                    final File localFile = new File(folder, dsp.getKey() + ".jpg");
                    if (!localFile.exists()) {
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

        list = (ListView) findViewById(R.id.list);

        myRef = database.getReference().child("Products");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    String name = String.valueOf(dsp.child("productname").getValue());
                    String companyName = String.valueOf(dsp.child("companyName").getValue());
                    String description = String.valueOf(dsp.child("description").getValue());

                    File localFile = new File(folder, dsp.getKey() + ".jpg");
                    if (localFile.exists()) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        items.add(new BestOffersModel(BitmapFactory.decodeFile(localFile.getAbsolutePath(), options), dsp.getKey(), name, companyName, description));
                    }
                }
                Collections.shuffle(items, new Random(System.currentTimeMillis()));
                adapter = new BestOffersAdapter(items, getApplicationContext());
                list.setAdapter(adapter);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                attachmentlist.setImageBitmap(bitmap);
                upload.setVisibility(View.VISIBLE);
                camera.setVisibility(GONE);
                advertisementlist.setVisibility(GONE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void applyDim(@NonNull ViewGroup parent, float dimAmount) {
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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
                Intent h = new Intent(Customer.this, Customer.class);
                startActivity(h);
                finish();
                break;
            case R.id.nav_my_list:
                Intent m= new Intent(Customer.this,MyGroceryList.class);
                startActivity(m);
                finish();
                break;
            case R.id.nav_bestoffers:
                Intent l = new Intent(Customer.this, BestOffersCustomer.class);
                startActivity(l);
                finish();
                break;
            case R.id.nav_event:
                Intent e = new Intent(Customer.this, Event.class);
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
                    Toast.makeText(Customer.this, "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
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