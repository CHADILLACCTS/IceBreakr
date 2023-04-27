package com.example.message;

import static com.example.message.R.id.friendrequest_back_btn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.message.adapter.RequestAdapter;
import com.example.message.model.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendRequest extends AppCompatActivity {

    DatabaseReference friendRequestRef;
    RecyclerView recyclerView;
    FirebaseUser firebaseUser;
    RequestAdapter requestAdapter;
    ArrayList<Request> requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        // When the user clicks on the Back button, they will be redirected to the ChatHomepage activity
        ImageButton backBtn = findViewById(friendrequest_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendRequest.this, ChatHomepage.class);
                startActivity(intent);
                finish();
            }
        });

        // Set up recycler view
        recyclerView = findViewById(R.id.friendrequest_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        // Connect to Firebase (current user and "FriendRequest" node)
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        friendRequestRef = FirebaseDatabase.getInstance().getReference("FriendRequest");

        // Variable to store the list of friend request sent to current user
        requestList = new ArrayList<>();

        // Retrieve friend request from the "FriendRequest" Node in Firebase
        friendRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Clear (reset) the list
                requestList.clear();

                // Loop to traverse all the children of the snapshot
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){

                    // Only retrieve the request sent to the current user
                    Request request = dataSnapshot.getValue(Request.class);
                    if(request.getRequestee().equals(firebaseUser.getUid())) {
                        requestList.add(request);
                    }
                }

                // Apply the list to the recycler view using the adapter so it can display
                requestAdapter = new RequestAdapter(FriendRequest.this, requestList);
                recyclerView.setAdapter(requestAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}
