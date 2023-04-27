package com.example.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText email, password;
    TextView goToRegister;
    TextView forgotPassword;
    Button loginBtn;
    FirebaseAuth auth;
    CheckBox rememberMe;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String REMEMBER = "remember";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Link variables to their corresponding element in the layout
        password = findViewById(R.id.login_password);
        email = findViewById(R.id.login_email);
        loginBtn = findViewById(R.id.login_btn);
        goToRegister = findViewById(R.id.login_go_to_register);
        rememberMe = (CheckBox) findViewById(R.id.login_remember_me_check_box);
        forgotPassword = findViewById(R.id.login_forget_password);

        // Connect to Firebase
        auth = FirebaseAuth.getInstance();

        // When the user clicks on "Login" button ...
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save the input from the user
                String l_password = password.getText().toString();
                String l_email = email.getText().toString();

                // Ensure that all fields are filled out
                // If not, prompt warning message
                if (TextUtils.isEmpty(l_password) | TextUtils.isEmpty(l_email)) {
                    Toast.makeText(Login.this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Log in to Firebase with the given email and password
                    auth.signInWithEmailAndPassword(l_email, l_password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Check and save the information whether the user checked the "Remember Me" box
                                        // Pass the information to saveData() method
                                        saveData(rememberMe.isChecked());

                                        // Login successful - redirect user to HomeScreen activity
                                        Intent intent = new Intent(Login.this, HomeScreen.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Login fail - prompt warning message
                                        Toast.makeText(Login.this, "Login failed! Please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        // Clickable textview "Register" - redirect user to Register activity
        goToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
                finish();
            }
        });

        // Clickable textview "Forgot password" - redirect user to ResetPassword activity
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, ResetPassword.class);
                startActivity(intent);
                finish();
            }
        });
    }



    // Save and process the information of whether the user checked the "Remember Me" box
    public void saveData(boolean checked) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(REMEMBER, checked);
        editor.apply();
    }
}