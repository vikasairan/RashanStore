package com.rashanstore.rashanstore;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;

public class UploadImage extends AppCompatActivity{

    private static final int PICK_IMAGE_REQUEST = 234;
    private TextView buttonChoose;
    private TextView buttonUpload;
    private ImageView imageView;

    private Uri filePath;

    private StorageReference storageReference;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        buttonChoose = findViewById(R.id.buttonChoose);
        buttonUpload = findViewById(R.id.buttonUpload);
        imageView =    findViewById(R.id.imageView);
        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);

        Intent intent = getIntent();
        final String path = intent.getStringExtra("path");

        File dir = Environment.getExternalStorageDirectory();
        File localFile = new File(dir,"Rashan Store/"+path);


        if(localFile.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath(),options);
            imageView.setImageBitmap(bitmap);
        }


        buttonChoose.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                TextView view = (TextView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getBackground().setColorFilter(0x50000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                        break;
                    }
                }
                return true;
            }
        });
         buttonUpload.setOnTouchListener(new View.OnTouchListener() {

        public boolean onTouch(View v, MotionEvent event) {
            TextView view = (TextView) v;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    view.getBackground().setColorFilter(0x50000000, PorterDuff.Mode.SRC_ATOP);
                    view.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    view.getBackground().clearColorFilter();
                    view.invalidate();
                    if (filePath != null) {

                        File dir = Environment.getExternalStorageDirectory();
                        File folder = new File(dir, "Rashan Store");
                        File localFile = new File(folder,path);
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(localFile.getPath());
                            Bitmap bm=((BitmapDrawable)imageView.getDrawable()).getBitmap();
                            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        final ProgressDialog progressDialog = new ProgressDialog(UploadImage.this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();
                StorageReference ref = storageReference.child(path);
                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(UploadImage.this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                                Intent i=new Intent(UploadImage.this,Home.class);
                                startActivity(i);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(UploadImage.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Uploaded "+(int)progress+"%");
                            }
                        });

            }
            else
                    {
                        Toast.makeText(UploadImage.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
            return true;
        }
         });
        }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    }
