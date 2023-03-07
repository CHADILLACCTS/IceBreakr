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

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;
    private List<User> userList;

    // This method shows what happens everytime a chat fragment (tab) is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set the view to be displayed on the chat tab
        // recyclerView means the output is displayed in the list format
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        recyclerView = view.findViewById(R.id.fragment_friends_message_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();

        // Check for current Users (or Friends of the user)
        readFriends();
        return view;
    }

    // This method reads the database and retrieves the available users in the system
    // (At this moment, we assume that everyone in the database are "friends" to the other)
    // Only add those users whose ID is not matching the main user's
    // (To avoid unnecessary duplicate)
    // Finally, apply adapter to finalize the each row
    private void readFriends(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);

                    assert user != null;
                    assert firebaseUser != null;

                    // Retrieve all users from Firebase (except the user themself)
                    if(!user.getId().equals(firebaseUser.getUid())) {
                        userList.add(user);
                    }
                }

                friendAdapter = new FriendAdapter(getContext(), userList);
                recyclerView.setAdapter(friendAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}