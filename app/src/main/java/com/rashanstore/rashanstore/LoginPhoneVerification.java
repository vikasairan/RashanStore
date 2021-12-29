package com.rashanstore.rashanstore;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mukesh.OtpView;

import java.util.concurrent.TimeUnit;

public class LoginPhoneVerification extends AppCompatActivity {

    private String mVerificationId;
    private OtpView editTextCode;
    private FirebaseAuth mAuth;
    private CountDownTimer countDownTimer;
    private boolean timerHasStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_verification);

        mAuth = FirebaseAuth.getInstance();

        editTextCode = findViewById(R.id.editTextCode);
        final TextView timer = findViewById(R.id.timer);
        Intent intent = getIntent();
        final String mobile = intent.getStringExtra("mobile");
        findViewById(R.id.resendcode).setVisibility(View.GONE);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                timer.setText("Please wait "+l/1000+" sec");
            }

            @Override
            public void onFinish() {
                timer.setVisibility(View.GONE);
                findViewById(R.id.resendcode).setVisibility(View.VISIBLE);
                timerHasStarted=false;
            }
        };
        if (!timerHasStarted) {
            countDownTimer.start();
            timerHasStarted = true;
        }
        sendVerificationCode(mobile);

        ImageView next=findViewById(R.id.buttonSignIn);
        next.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                ImageView view=(ImageView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        view.getDrawable().clearColorFilter();
                        view.invalidate();

                        String code = editTextCode.getOTP();
                        if (code.isEmpty() || code.length() < 6)
                        {
                            Toast.makeText(LoginPhoneVerification.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                            editTextCode.requestFocus();
                            return true;
                        }
                        verifyVerificationCode(code);
                        break;
                    }
                }
                return true;
            }
        });
        findViewById(R.id.resendcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode(mobile);
                findViewById(R.id.resendcode).setVisibility(View.GONE);
                timer.setText("Please wait 60 sec");
                timer.setVisibility(View.VISIBLE);
                Toast.makeText(LoginPhoneVerification.this, "Verification Code Sent", Toast.LENGTH_SHORT).show();
                countDownTimer = new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long l) {
                        timer.setText("Please wait "+l/1000+" sec");
                    }

                    @Override
                    public void onFinish() {
                        timer.setVisibility(View.GONE);
                        findViewById(R.id.resendcode).setVisibility(View.VISIBLE);
                        timerHasStarted=false;
                    }
                };
                if (!timerHasStarted) {
                    countDownTimer.start();
                    timerHasStarted = true;
                }

            }
        });

    }
    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                editTextCode.setOTP(code);
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e)
        {
            Toast.makeText(LoginPhoneVerification.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken)
        {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
        }
    };


    private void verifyVerificationCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginPhoneVerification.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = getIntent();
                            String type =intent.getStringExtra("type");
                            if(type.equals("shopkeeper")) {
                                intent = new Intent(LoginPhoneVerification.this, Home.class);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                intent = new Intent(LoginPhoneVerification.this, Customer.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            String message = "Somthing is wrong, we will fix it soon";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered";
                            }
                            Toast.makeText(LoginPhoneVerification.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}