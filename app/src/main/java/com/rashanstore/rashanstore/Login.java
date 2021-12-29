package com.rashanstore.rashanstore;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private static final int PERMISSION_ALL = 1;
    private Button login;
    private TextView register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FirebaseAuth auth=FirebaseAuth.getInstance();
        String[] PERMISSIONS = new String[0];
        if(auth.getCurrentUser()!=null)
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
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("type").getValue().toString().equals("shopkeeper")) {
                        startActivity(new Intent(Login.this, Home.class));
                        finish();
                    }
                    else
                        {
                        startActivity(new Intent(Login.this, Customer.class));
                        finish();
                       }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }
        else {

            setContentView(R.layout.activity_login);
            ChangeBounds bounds = new ChangeBounds();
            bounds.setDuration(1500);
            getWindow().setSharedElementEnterTransition(bounds);
            try {
                PERMISSIONS = getPermissions(getApplicationContext());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }


            login = findViewById(R.id.login);
            register = findViewById(R.id.register);

            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent register = new Intent(Login.this, Register.class);
                    startActivity(register);
                }
            });

            TextView forgotpassword = findViewById(R.id.forgotpassword);

            forgotpassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent f = new Intent(Login.this, ForgotPassword.class);
                    startActivity(f);
                }
            });

            final EditText mobilenumber = findViewById(R.id.phone);
            final EditText pass = findViewById(R.id.password);

            login.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
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

                            if(validateForm()) {
                                String mobile = mobilenumber.getText().toString().trim();
                                final String password = pass.getText().toString().trim();

                                if (mobile.isEmpty() || mobile.length() < 10) {
                                    mobilenumber.setError("Enter a valid phone number");
                                    mobilenumber.requestFocus();
                                    return false;
                                }
                                if (mobile.startsWith("0")) {
                                    mobile = mobile.substring(1);
                                }
                                if (mobile.startsWith("+91")) {
                                    mobile = mobile.replace("+91", "");
                                }
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference dbRef = database.getReference().child("User");
                                Intent i=getIntent();
                                final String type=i.getStringExtra("type");
                                final String finalMobile = mobile;
                                dbRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.child(finalMobile).exists() && dataSnapshot.child(finalMobile).child("type").getValue().toString().equals(type)) {

                                            if(dataSnapshot.child(finalMobile).child("password").getValue().toString().equals(password)) {

                                                Intent intent = new Intent(Login.this, LoginPhoneVerification.class);
                                                intent.putExtra("mobile",finalMobile);
                                                intent.putExtra("type",dataSnapshot.child(finalMobile).child("type").getValue().toString());
                                                startActivity(intent);
                                                finish();
                                            }
                                            else
                                            {
                                                Toast.makeText(Login.this, "Invalid Phone Number or Password", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else
                                            {
                                            Toast.makeText(Login.this," Account with mobile number doesn't exist. Please register.", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                            break;
                        }
                    }
                    return true;
                }
            });
        }
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

    public static String[] getPermissions(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
        return info.requestedPermissions;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean validateForm()
    {
        final EditText mobilenumber=(EditText)findViewById(R.id.phone);
        final EditText password=(EditText)findViewById(R.id.password);

        boolean alldone=true;
        if(TextUtils.isEmpty(mobilenumber.getText().toString().trim())||mobilenumber.length()<10)
        {
            mobilenumber.setError("Enter Valid Mobile Number");
            return false;
        }
        else
        {
            alldone=true;
            mobilenumber.setError(null);
        }
        if(TextUtils.isEmpty(password.getText().toString().trim()))
        {
            password.setError("Invalid Password");
            return false;
        }
        else
        {
            alldone=true;
            password.setError(null);
        }
        return alldone;
    }
}