package com.rashanstore.rashanstore;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginAs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_as);
        Button customer=findViewById(R.id.customer);
        Button shopkeeper=findViewById(R.id.shopkeeper);

        final Intent i=new Intent(LoginAs.this,Login.class);
        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i.putExtra("type","customer");
                startActivity(i);
            }
        });
        shopkeeper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i.putExtra("type","shopkeeper");
                startActivity(i);
            }
        });
        TextView register=findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(LoginAs.this, Register.class);
                startActivity(register);
            }
        });
    }
}
