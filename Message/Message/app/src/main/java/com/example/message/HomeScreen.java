package com.example.message;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.example.message.model.User;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeScreen extends AppCompatActivity {

    TextView name;
    Button chatBtn;

    Button profileBtn;

    Button logoutBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        chatBtn = findViewById(R.id.Homescreen_chat_btn);
        profileBtn = findViewById(R.id.Homescreen_userprofile_btn);
        logoutBtn = findViewById(R.id.Homescreen_logout_btn);
        name = findViewById(R.id.Homescreen_welcome_name);
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeScreen.this, ChatHomepage.class);
                startActivity(intent);
                finish();
            }
        });


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");

        // Listen to (Record) any upcoming data
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve data from a particular snapshot
                // Store the data to the declared variables
                User user = snapshot.getValue(User.class);
                name.setText("Welcome back, " + user.getName() + "!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomeScreen.this, Start.class);
                startActivity(intent);
                finish();
            }
        });

    }


}