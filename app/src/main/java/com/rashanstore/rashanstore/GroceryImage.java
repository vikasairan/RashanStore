package com.rashanstore.rashanstore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

public class GroceryImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_image);
        Bundle extras = getIntent().getExtras();
        String id = null;
        if (extras != null) {
            id = extras.getString("id");
        }


        final ImageView img=findViewById(R.id.img);

        File dir = Environment.getExternalStorageDirectory();
        final File folder = new File(dir, "Rashan Store/GroceryList");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        final File localFile = new File(folder,id+".jpg");

        if(localFile.exists()) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath(), options);
            img.setImageBitmap(bitmap);
        }

    }
}
