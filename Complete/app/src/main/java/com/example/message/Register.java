package com.example.message;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    EditText name, password, email;
    TextView goToLogin;
    Button registerBtn;
    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Link variables to their corresponding element in the layout
        name = findViewById(R.id.register_name);
        password = findViewById(R.id.register_password);
        email = findViewById(R.id.register_email);
        registerBtn = findViewById(R.id.register_btn);
        goToLogin = findViewById(R.id.register_go_to_login);

        // When the user clicks on the "Register" button ...
        registerBtn.setOnClickListener(view -> {
            // Save the input from the user
            String r_name = name.getText().toString();
            String r_password= password.getText().toString();
            String r_email = email.getText().toString();

            // Ensure that all fields are filled out
            // If not, prompt warning message
            if(TextUtils.isEmpty(r_name) | TextUtils.isEmpty(r_password) | TextUtils.isEmpty(r_email)){
                Toast.makeText(Register.this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
            }
            // Pass the inputs to the register() method
            else{
                register(r_name, r_password, r_email);
            }
        });

        // Connect to Firebase
        auth = FirebaseAuth.getInstance();

        // Clickable textview "Login" - redirect user to Login activity
        goToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this,Login.class);
            startActivity(intent);
            finish();
        });
    }



    // Register the user to the Database with the given input
    private void register(String name, String password, String email){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    // Create a new data for the new user
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userID = firebaseUser.getUid();

                    // Connect (refer) to the "Users" node in the Database
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

                    // Insert the input to the hashmap under the "Users" node
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userID);
                    hashMap.put("name", name);
                    hashMap.put("email", email);
                    hashMap.put("imageID", String.valueOf(this.getResources().getIdentifier("icondefault", "drawable", this.getPackageName())));

                    reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                        // If insert successfully, redirect user to HomeScreen activity
                        if(task1.isSuccessful()) {
                            Intent intent = new Intent(Register.this,HomeScreen.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(Register.this,"ERROR: Register failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                });
    }
}