package com.example.message.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.message.R;
import com.example.message.adapter.FriendAdapter;
import com.example.message.model.ChatList;
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
    private List<User> userList;

    DatabaseReference userRef, chatListRef;

    // This method shows what happens everytime a chat fragment (tab) is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set the view to be displayed on the chat tab
        // recyclerView means the output is displayed in the list format
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = view.findViewById(R.id.fragment_chats_message_recycler); // link to layout
        recyclerView.setHasFixedSize(true);     // Each item has the same size
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Identify the user on database
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Create List to to (many) users and their info
        chatUserList = new ArrayList<>();

        // Create a reference to access database ("ChatList")
        // --> child = receiver id
        chatListRef = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());

        // Retrieve id of "receiver" in chat list format (id)
        // Traverse the data to search for an existing chat and store it in the list
        // Call readChatList() to match the ids in Chat List with the ids in actual Users list
        chatListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatUserList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String chatList = dataSnapshot.getValue(String.class);
                    chatUserList.add(chatList);        // sender in ChatList
                }
                getChatList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return view;
    }

    // Retrieve id of "receiver" in User format (id + name + pic)
    // Store to display
    private void getChatList() {
        userList = new ArrayList<>();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);

                    // Retrieve all users that have chatted with the main user
                    // Full info from User class
                    for(String chatList: chatUserList){
                        if(user.getId().equals(chatList)){
                            userList.add(user);     // receiver in Users
                        }
                    }
                }
                chatListAdapter = new FriendAdapter(getContext(), userList);
                recyclerView.setAdapter(chatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}