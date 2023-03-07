package com.example.message;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

//    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://message-98022-default-rtdb.firebaseio.com/");

    EditText name, password, email;
    Button registerBtn;
    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.register_name);
        password = findViewById(R.id.register_password);
        email = findViewById(R.id.register_email);
        registerBtn = findViewById(R.id.register_btn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String r_name = name.getText().toString();
                String r_password= password.getText().toString();
                String r_email = email.getText().toString();

                if(TextUtils.isEmpty(r_name) | TextUtils.isEmpty(r_password) | TextUtils.isEmpty(r_email)){
                    Toast.makeText(Register.this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
                }
                else{
                    register(r_name, r_password, r_email);
                }
            }
        });

        auth = FirebaseAuth.getInstance();
    }
    private void register(String name, String password, String email){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        String userID = firebaseUser.getUid();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", userID);
                        hashMap.put("name", name);
                        hashMap.put("imageURL", "default");

                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Intent intent = new Intent(Register.this,HomeScreen.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(Register.this,"ERROR: Register failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
    }



}