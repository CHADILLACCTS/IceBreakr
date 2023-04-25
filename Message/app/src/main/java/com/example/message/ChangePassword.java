package com.example.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangePassword extends AppCompatActivity {

    FirebaseUser firebaseUser;
    EditText oldPassword, newPassword, newPasswordMatch;

    Button confirm;

    ImageButton backBtn;

    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        oldPassword = findViewById(R.id.change_password_current);
        newPassword = findViewById(R.id.change_password_new);
        newPasswordMatch = findViewById(R.id.change_password_new_repeat);
        backBtn = findViewById(R.id.change_password_back_btn);
        confirm = findViewById(R.id.userprofile_change_password_btn);
        auth = FirebaseAuth.getInstance();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String old_password = oldPassword.getText().toString();
                String new_password = newPassword.getText().toString();
                String match_password = newPasswordMatch.getText().toString();
                if (TextUtils.isEmpty(old_password) || TextUtils.isEmpty(new_password) || TextUtils.isEmpty(match_password)) {
                    Toast.makeText(ChangePassword.this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(Objects.requireNonNull(firebaseUser.getEmail()), old_password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        if(new_password.equals(match_password))
                                        {
                                            firebaseUser.updatePassword(new_password);
                                            Toast.makeText(ChangePassword.this, "Password successfully updated", Toast.LENGTH_SHORT).show();

                                        }
                                        else
                                        {
                                            Toast.makeText(ChangePassword.this, "New Password does not match", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(ChangePassword.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangePassword.this, UserProfile.class);
                startActivity(intent);
                finish();
            }
        });
    }
}