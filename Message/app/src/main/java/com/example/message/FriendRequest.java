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
    TextView name;
    RecyclerView recyclerView;
    ImageButton accept, decline;
    FirebaseUser firebaseUser;
    RequestAdapter requestAdapter;
    ArrayList<Request> requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        ImageButton backBtn = findViewById(friendrequest_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendRequest.this, ChatHomepage.class);
                startActivity(intent);
                finish();
            }
        });

        recyclerView = findViewById(R.id.friendrequest_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        friendRequestRef = FirebaseDatabase.getInstance().getReference("FriendRequest");
        requestList = new ArrayList<>();

        friendRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Request request = dataSnapshot.getValue(Request.class);
                    if(request.getRequestee().equals(firebaseUser.getUid())) {
                        requestList.add(request);
                    }
                }

                requestAdapter = new RequestAdapter(FriendRequest.this, requestList);
                recyclerView.setAdapter(requestAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}
