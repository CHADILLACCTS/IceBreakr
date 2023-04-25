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
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(recyclerView);
        userList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        friendListRef = FirebaseDatabase.getInstance().getReference("FriendList").child(firebaseUser.getUid());
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        // Check for current Users (or Friends of the user)
        readFriends();


        return view;
    }

    // This method reads the database and retrieves the available users in the system
    // (At this moment, we assume that everyone in the database are "friends" to the other)
    // Only add those users whose ID is not matching the main user's
    // (To avoid unnecessary duplicate)
    // Finally, apply adapter to finalize the each row
    private void readFriends() {

        friendListRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {


                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                            friendID = dataSnapshot.getValue(String.class);
                            User user = snapshot1.child(friendID).getValue(User.class);
                            userList.add(user);

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

    User deletedItem = null;
    Boolean undo = false;

    ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();

            if (direction == ItemTouchHelper.LEFT) {
                deletedItem = userList.get(position);
                userList.remove(position);
                friendAdapter.notifyItemRemoved(position);
                int waitTime = 10000;

                Snackbar.make(recyclerView, deletedItem.getName(), waitTime)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                userList.add(position, deletedItem);
                                friendAdapter.notifyItemInserted(position);
                                undo = true;
                            }
                        }).show();

                new CountDownTimer(waitTime, 1000) {

                    @Override
                    public void onTick(long l) {
                        if (undo) {
                            cancel();
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (!undo) {
                            friendListRef.child(friendID).removeValue();
                        }
                    }
                }.start();

            }
        }

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

