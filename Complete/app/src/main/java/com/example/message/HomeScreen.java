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
    Button chatBtn, profileBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Link the variables to their corresponding element in the layout
        chatBtn = findViewById(R.id.Homescreen_chat_btn);
        profileBtn = findViewById(R.id.Homescreen_userprofile_btn);
        logoutBtn = findViewById(R.id.Homescreen_logout_btn);
        name = findViewById(R.id.Homescreen_welcome_name);

        // -----------------------------------------------------------------------------------------
        // --------------------------------------- FIREBASE ----------------------------------------

        // Connect to the database of current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Connect (refer) to the "Users" node in the Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        // Access the data in "Users" node
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve data from a particular snapshot
                // Store the data to the declared variables (name)
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    name.setText("Welcome back, " + user.getName() + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // -----------------------------------------------------------------------------------------
        // ----------------------------------------- BUTTON ----------------------------------------

        // When the user clicks on "Chat" button, they are redirected to ChatHomepage activity
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeScreen.this, ChatHomepage.class);
                startActivity(intent);
                finish();
            }
        });

        // When the user clicks on "Profile" button, they are redirected to UserProfile activity
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeScreen.this, UserProfile.class);
                startActivity(intent);
                finish();
            }
        });

        // When the user clicks on "Logout" button
        // They are logged out of the system (database) and redirected to Start activity
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