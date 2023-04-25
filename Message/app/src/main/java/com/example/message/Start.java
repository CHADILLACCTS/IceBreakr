package com.example.message;

import static com.example.message.Login.REMEMBER;
import static com.example.message.Login.SHARED_PREFS;

import androidx.appcompat.app.AppCompatActivity;

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

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        remember = sharedPreferences.getBoolean(REMEMBER, false);
        if(remember)
        {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        }
        // Check in the user has already logged in
        if (firebaseUser != null){
            startActivity(new Intent(Start.this, HomeScreen.class));
            finishAffinity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);



        loginBtn = findViewById(R.id.start_login_btn);
        registerBtn = findViewById(R.id.start_register_btn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Start.this, Login.class));
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Start.this, Register.class));
            }
        });
    }
}