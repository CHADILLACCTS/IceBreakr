package com.example.message.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.message.R;
import com.example.message.adapter.FriendAdapter;
import com.example.message.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FriendAdapter chatListAdapter;
    private List<String> chatUserList;

    DatabaseReference userRef, chatListRef;

    // This method shows what happens everytime a "Chat" fragment (tab) is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set up the layout and recycler view to display the list of chats
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = view.findViewById(R.id.fragment_chats_message_recycler); // link to layout
        recyclerView.setHasFixedSize(true);     // Each item has the same size
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Identify the user on database
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Create List to store chats
        chatUserList = new ArrayList<>();

        // Create a reference to access database ("ChatList") --> current user
        chatListRef = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());

        // Retrieve id of chats in the chat list
        chatListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear (reset) list
                chatUserList.clear();

                // Loop to retrieve the children of the snapshot
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String chatList = dataSnapshot.getValue(String.class);
                    chatUserList.add(chatList);
                }

                // Retrieve the existing chats of current user
                getChatList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return view;
    }



    // List of existing chats in which the current user is involved
    private void getChatList() {

        // Create a list to store user
        List<User> userList = new ArrayList<>();

        // Connect (reference) to database under the node "Users"
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        // Retrieve users who have sent or received a message from the current user
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear (reset)
                userList.clear();

                // Loop to retrieve children of snapshot
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);

                    // Retrieve all users who have sent or received a message from the current user
                    // from the list of existing chats
                    // Add user to the list
                    for(String chatList: chatUserList){
                        if(user.getId().equals(chatList)){
                            userList.add(user);     // receiver in Users
                        }
                    }
                }

                // Apply the adapter so that the list is displayed on the recycler view
                chatListAdapter = new FriendAdapter(getContext(), userList);
                recyclerView.setAdapter(chatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}