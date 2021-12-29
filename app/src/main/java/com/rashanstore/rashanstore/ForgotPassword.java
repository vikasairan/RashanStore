package com.rashanstore.rashanstore;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForgotPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        final EditText phone = findViewById(R.id.phone);
        Button submit=findViewById(R.id.submit);

        submit.setOnTouchListener(new View.OnTouchListener() {

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
                        String mobile = phone.getText().toString();
                        if (mobile.startsWith("0")) {
                            mobile = mobile.substring(1);
                        }
                        if (mobile.startsWith("+91")) {
                            mobile = mobile.replace("+91", "");
                        }
                        final String finalMobile = mobile;
                        if (finalMobile.length() < 10) {
                            phone.setError("Invalid Phone Number");
                        }
                        else
                            {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference dbRef = database.getReference().child("User");

                            dbRef.addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    if (dataSnapshot.child(finalMobile).exists())
                                    {
                                        Intent intent = new Intent(ForgotPassword.this, Verify_mobile.class);
                                        intent.putExtra("mobile", finalMobile);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else
                                        {
                                        Toast.makeText(ForgotPassword.this, "Account with mobile number doesn't exist. Please Register.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Intent intent = new Intent(ForgotPassword.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }
                }
                    return true;
                }
            });
        }
}
