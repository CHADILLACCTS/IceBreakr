package com.example.message;

import static com.example.message.Login.REMEMBER;
import static com.example.message.Login.SHARED_PREFS;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Start extends AppCompatActivity {

    Button loginBtn, registerBtn;
    FirebaseUser firebaseUser;
    Boolean remember;

    @Override
    protected void onStart() {
        super.onStart();

        // Retrieve the values for Remember Me
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        remember = sharedPreferences.getBoolean(REMEMBER, false);

        // If the user selected Remember Me, their login will be saved
        // and their information will be retrieved
        if(remember)
        {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        // If the user's login information exists, log in automatically
        if (firebaseUser != null){
            startActivity(new Intent(Start.this, HomeScreen.class));
            finishAffinity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        loginBtn = findViewById(R.id.start_login_btn);
        registerBtn = findViewById(R.id.start_register_btn);

        // Log in button - redirect user to Login activity
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Start.this, Login.class));
            }
        });

        // Register button - redirect user to Register activity
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Start.this, Register.class));
            }
        });
    }
}