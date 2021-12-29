package com.rashanstore.rashanstore;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);
    final EditText name = (EditText) findViewById(R.id.name);
    final EditText mobilenumber = (EditText) findViewById(R.id.mobileNumber);
    final EditText gstNumber = (EditText) findViewById(R.id.gstNumber);
    final EditText panNumber = (EditText) findViewById(R.id.panNumber);
    final EditText shopName = (EditText) findViewById(R.id.shopName);
    final EditText state = (EditText) findViewById(R.id.state);
    final EditText address = (EditText) findViewById(R.id.address);
    final EditText password = (EditText) findViewById(R.id.password);
    Button register = (Button) findViewById(R.id.register);

    final RadioButton customer=findViewById(R.id.radioCustomer);
    final RadioButton shopkeeper=findViewById(R.id.radioShopkeeper);

    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiocs);
    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(customer.isChecked())
        {
          gstNumber.setVisibility(View.GONE);
          panNumber.setVisibility(View.GONE);
          shopName.setVisibility(View.GONE);
          state.setVisibility(View.GONE);
        }
        else
        {
          gstNumber.setVisibility(View.VISIBLE);
          panNumber.setVisibility(View.VISIBLE);
          shopName.setVisibility(View.VISIBLE);
          state.setVisibility(View.VISIBLE);
        }
      }
    });


    register.setOnTouchListener(new View.OnTouchListener() {

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
            view.invalidate();{
              String mobile = mobilenumber.getText().toString().trim();
              if (mobile.startsWith("0")) {
                mobile = mobile.substring(1);
              }
              if (mobile.startsWith("+91")) {
                mobile = mobile.replace("+91", "");
              }
              if (mobile.isEmpty() || mobile.length() < 10) {
                mobilenumber.setError("Enter a valid mobile");
                mobilenumber.requestFocus();
                return false;
              }
              final String finalMobile = mobile;
              FirebaseDatabase database = FirebaseDatabase.getInstance();
              DatabaseReference dbRef = database.getReference().child("User");
              dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                  if (!dataSnapshot.child(finalMobile).exists()) {
                    Intent intent = new Intent(Register.this, phone_verification.class);
                    if(shopkeeper.isChecked()&&validateFormShopkeeper()) {
                      intent.putExtra("mobile", finalMobile);
                      intent.putExtra("name", name.getText().toString().trim());
                      intent.putExtra("gstNumber", gstNumber.getText().toString().trim());
                      intent.putExtra("panNumber", panNumber.getText().toString().trim());
                      intent.putExtra("shopName", shopName.getText().toString().trim());
                      intent.putExtra("state", state.getText().toString().trim());
                      intent.putExtra("address", address.getText().toString().trim());
                      intent.putExtra("password", password.getText().toString().trim());
                      intent.putExtra("type", "shopkeeper");
                      startActivity(intent);
                      finish();
                    }
                    if(customer.isChecked()&&validateFormCustomer())
                    {
                      intent.putExtra("mobile", finalMobile);
                      intent.putExtra("name", name.getText().toString().trim());
                      intent.putExtra("address", address.getText().toString().trim());
                      intent.putExtra("password", password.getText().toString().trim());
                      intent.putExtra("type", "customer");
                      startActivity(intent);
                      finish();
                    }

                  } else {
                    Toast.makeText(Register.this, "Account with Mobile number already exist. Please Log In", Toast.LENGTH_SHORT).show();
                    mobilenumber.setText("");
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

  public boolean validateFormShopkeeper()
  {
    final EditText name=(EditText)findViewById(R.id.name);
    final EditText mobilenumber=(EditText)findViewById(R.id.mobileNumber);
    final EditText gstNumber=(EditText)findViewById(R.id.gstNumber);
    final EditText panNumber=(EditText)findViewById(R.id.panNumber);
    final EditText shopName=(EditText)findViewById(R.id.shopName);
    final EditText state=(EditText)findViewById(R.id.state);
    final EditText address=(EditText)findViewById(R.id.address);
    final EditText password=(EditText)findViewById(R.id.password);

    Pattern patternPAN = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");
    Matcher matcherPAN = patternPAN.matcher(panNumber.getText().toString().trim());
    Pattern patternGST1 = Pattern.compile("[0][1-9]"+panNumber.getText().toString().trim()+"[1-9a-zA-Z]{1}[zZ]{1}[0-9a-zA-Z]{1}");
    Matcher matcherGST1 = patternGST1.matcher(gstNumber.getText().toString().trim());
    Pattern patternGST2 = Pattern.compile("[1-2][0-9]"+panNumber.getText().toString().trim()+"[1-9a-zA-Z]{1}[zZ]{1}[0-9a-zA-Z]{1}");
    Matcher matcherGST2 = patternGST2.matcher(gstNumber.getText().toString().trim());
    Pattern patternGST3 = Pattern.compile("[3][0-7]"+panNumber.getText().toString().trim()+"[1-9a-zA-Z]{1}[zZ]{1}[0-9a-zA-Z]{1}");
    Matcher matcherGST3 = patternGST3.matcher(gstNumber.getText().toString().trim());


    boolean alldone=true;
    if(TextUtils.isEmpty(name.getText().toString().trim()))
    {
      name.setError("Enter Name");
      return false;
    }
    else
    {
      alldone=true;
      name.setError(null);
    }
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
    if(TextUtils.isEmpty(panNumber.getText().toString().trim())||matcherPAN.matches())
    {
      alldone=true;
      panNumber.setError(null);
    }
    else
    {
      panNumber.setError("Enter Valid PAN NUMBER");
      return false;
    }
    if(TextUtils.isEmpty(gstNumber.getText().toString().trim())||matcherGST1.matches()||matcherGST2.matches()||matcherGST3.matches())
    {
      alldone=true;
      gstNumber.setError(null);
    }
    else
    {
       gstNumber.setError("Enter Valid GST Number");
      return false;
    }
    if(TextUtils.isEmpty(shopName.getText().toString().trim()))
    {
      shopName.setError("Enter Shop Name");
      return false;
    }
    else
    {
      alldone=true;
      shopName.setError(null);
    }
    if(TextUtils.isEmpty(state.getText().toString().trim()))
    {
      state.setError("Enter State");
      return false;
    }
    else
    {
      alldone=true;
      state.setError(null);
    }
    if(TextUtils.isEmpty(address.getText().toString().trim()))
    {
      address.setError("Enter Address");
      return false;
    }
    else
    {
      alldone=true;
      address.setError(null);
    }
    if(TextUtils.isEmpty(password.getText().toString().trim()))
    {
      address.setError("Enter Password");
      return false;
    }
    else
    {
      alldone=true;
      address.setError(null);
    }
    return alldone;
  }
  public boolean validateFormCustomer()
  {
    final EditText name=(EditText)findViewById(R.id.name);
    final EditText mobilenumber=(EditText)findViewById(R.id.mobileNumber);
    final EditText address=(EditText)findViewById(R.id.address);
    final EditText password=(EditText)findViewById(R.id.password);

    boolean alldone=true;
    if(TextUtils.isEmpty(name.getText().toString().trim()))
    {
      name.setError("Enter Name");
      return false;
    }
    else
    {
      alldone=true;
      name.setError(null);
    }
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
    if(TextUtils.isEmpty(address.getText().toString().trim()))
    {
      address.setError("Enter Address");
      return false;
    }
    else
    {
      alldone=true;
      address.setError(null);
    }
    if(TextUtils.isEmpty(password.getText().toString().trim())||password.getText().toString().length()<6)
    {
      password.setError("Enter Valid Password of minimum 6 characters");
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