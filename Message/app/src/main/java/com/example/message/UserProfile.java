package com.example.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.message.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfile extends AppCompatActivity {

    TextView name, email;

    ImageView profilePic;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    int drawableResourceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());


        name = findViewById(R.id.userprofile_name);
        email = findViewById(R.id.userprofile_email);
        profilePic = findViewById(R.id.userprofile_profile_pic);
        Button changePictureBtn = findViewById(R.id.userprofile_change_profile_pic_btn);
        Button changePasswordBtn = findViewById(R.id.userprofile_change_password_btn);

        changePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfile.this, ChangePicture.class);
                startActivity(intent);
                finish();
            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfile.this, ChangePassword.class);
                startActivity(intent);
                finish();
            }
        });

        ImageButton backBtn = findViewById(R.id.userprofile_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfile.this, HomeScreen.class);
                startActivity(intent);
                finish();
            }
        });
        // Listen to (Record) any upcoming data
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve data from a particular snapshot
                // Store the data to the declared variables
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    profilePic.setBackgroundResource(Integer.parseInt(user.getImageID()));
                    name.setText(user.getName());
                    email.setText(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }


}