package com.example.message;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.message.databinding.ActivityResetPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

    EditText email;
    Button sendBtn;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        email = findViewById(R.id.email);
        sendBtn = findViewById(R.id.send_btn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String l_email = email.getText().toString();
                if (TextUtils.isEmpty(l_email) | TextUtils.isEmpty(l_email)) {
                    Toast.makeText(ResetPassword.this, "Email entry required", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ResetPassword.this, "Email sent", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().sendPasswordResetEmail(l_email);
                    Intent intent = new Intent(ResetPassword.this, Login.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}

