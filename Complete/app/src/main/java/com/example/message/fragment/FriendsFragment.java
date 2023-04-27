package com.example.message.fragment;

import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.message.R;
import com.example.message.adapter.FriendAdapter;
import com.example.message.model.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;
    private FirebaseUser firebaseUser;
    private DatabaseReference friendListRef, userRef;
    private List<User> userList;
    private String friendID;

    // This method shows what happens everytime a "Friend" fragment (tab) is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set up the layout and recycler view
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        recyclerView = view.findViewById(R.id.fragment_friends_message_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(recyclerView);

        // Create a list to store user
        userList = new ArrayList<>();

        // Connect to Firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        friendListRef = FirebaseDatabase.getInstance().getReference("FriendList").child(firebaseUser.getUid());
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        // Check for current Users (or Friends of the user)
        readFriends();


        return view;
    }



   // Read from the database all those who are friends with the current user
    private void readFriends() {

        // For every ID of friend in the "FriendList" database,
        // retrieve the full information of the friend from the "Users" database
        friendListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear (reset) the list
                userList.clear();

                // Loop to retrieve all children of the snapshot
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {

                            // Retrieve and store data of snapshot to the list
                            friendID = dataSnapshot.getValue(String.class);
                            User user = snapshot1.child(friendID).getValue(User.class);
                            userList.add(user);

                            // Apply adapter to display the list on the recycler view
                            friendAdapter = new FriendAdapter(getContext(), userList);
                            recyclerView.setAdapter(friendAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    // Variable to store most recent deleted item (friend)
    User deletedItem = null;

    // Variable to check whether the Undo option has been used
    Boolean undo = false;

    // Delete friends by "Swiping Left" on the friend's name
    ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();

            // Ensure that the swipe is to the left
            if (direction == ItemTouchHelper.LEFT) {

                // Store the most recent deleted item (friend) and
                // (temporarily) remove it from the friend list
                deletedItem = userList.get(position);
                userList.remove(position);
                friendAdapter.notifyItemRemoved(position);

                // Time limit of the Undo option
                int waitTime = 10000;   // waitTime = 10 secs

                // A bar is displayed with the "Undo" option
                Snackbar.make(recyclerView, deletedItem.getName(), waitTime)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                userList.add(position, deletedItem);
                                friendAdapter.notifyItemInserted(position);
                                undo = true;
                            }
                        }).show();

                // User has 10 seconds to "Undo" their deletion
                new CountDownTimer(waitTime, 1000) {

                    @Override
                    public void onTick(long l) {
                        // If "Undo" button is hit anytime before the time is up,
                        // the Delete action is canceled and no change to the database
                        if (undo) {
                            cancel();
                        }
                    }

                    @Override
                    public void onFinish() {
                        // If the "Undo" button is not hit when the time is up
                        // the Deletion is permanently performed on the Database
                        if (!undo) {
                            friendListRef.child(friendID).removeValue();
                        }
                    }
                }.start();

            }
        }

        // Decorate the "Swipe Left" action
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX / 2, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(R.color.Blue_4)
                    .addSwipeLeftLabel("Delete")
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX / 2, dY, actionState, isCurrentlyActive);
        }
    };
}

