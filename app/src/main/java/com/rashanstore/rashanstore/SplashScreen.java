package com.rashanstore.rashanstore;

import android.app.Activity;
import android.app.ActivityOptions;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

import static android.app.ActivityOptions.makeSceneTransitionAnimation;

public class SplashScreen extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);

        final ImageView logo=findViewById(R.id.logo);
        logo.setVisibility(View.VISIBLE);
        logo.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.splash_screen_animation));
        TextView txt=findViewById(R.id.text);
        txt.setVisibility(View.VISIBLE);
        txt.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.zoom_in));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        FirebaseAuth auth=FirebaseAuth.getInstance();
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
                                        startActivity(new Intent(SplashScreen.this, Home.class));
                                        finish();
                                    }
                                    else
                                    {
                                        startActivity(new Intent(SplashScreen.this, Customer.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                        else
                         {
                            Intent intent = new Intent(SplashScreen.this, LoginAs.class);
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(SplashScreen.this, logo, "logo_transition");
                            startActivity(intent, options.toBundle());
                            finish();
                        }
                    }
                });
            }
        }, 3000);
    }

}