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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResetPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Intent intent = getIntent();
        final String mobile = intent.getStringExtra("mobile");

        final EditText password=findViewById(R.id.password);

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

                        String pass=password.getText().toString().trim();
                        if(pass.length()<6)
                        {
                            password.setError("Password too short\nMinimum required length is 6");
                        }
                        else
                        {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("User");
                        DatabaseReference user=myRef.child(mobile);
                        user.child("password").setValue(pass);
                        Toast.makeText(ResetPassword.this, "Password Successfully changed", Toast.LENGTH_SHORT).show();

                        user.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.child("type").getValue().toString().equals("shopkeeper")) {
                                        startActivity(new Intent(ResetPassword.this, Home.class));
                                        finish();
                                    }
                                    else
                                    {
                                        startActivity(new Intent(ResetPassword.this, Customer.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
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
