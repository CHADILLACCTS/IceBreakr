package com.example.message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.example.message.fragment.ChatsFragment;
import com.example.message.fragment.FriendsFragment;
import com.example.message.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


// This is the Homepage of Chat, where the lists of Existing Chats and Friends are displayed
public class ChatHomepage extends AppCompatActivity {

    ImageView profilePic;
    TextView name;
    Button RandomBtn;
    ImageButton friendRequestBtn;
    FirebaseUser firebaseUser;
    String nameString;
    DatabaseReference userReference, randomReference, connectReference;
    static int numUsers, numRandomUsers;
    private List<String> userIDs, randomUserIDs, doNotConnectList;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        userIDs = new ArrayList<String>();
        randomUserIDs = new ArrayList<String>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_homepage);    // The layout connected to this file

        // When the user clicks on the Back button, they will be redirected to the HomeScreen activity
        ImageButton backBtn = findViewById(R.id.chathome_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatHomepage.this, HomeScreen.class);
                startActivity(intent);
                finish();
            }
        });

        // Link variables to the corresponding element in the layout (.xml)
        profilePic = findViewById(R.id.chathome_profile_pic);
        name = findViewById(R.id.chathome_name);
        RandomBtn = findViewById(R.id.chathome_random_chat_btn);
        friendRequestBtn = findViewById(R.id.chathome_friend_request_btn);

        // Connect to Firebase and some specific nodes
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        randomReference = FirebaseDatabase.getInstance().getReference("RandomChat");
        connectReference = FirebaseDatabase.getInstance().getReference("ChatToBeConnected");


        // -----------------------------------------------------------------------------------------
        // -------------------------------- ACCESS "USERS" DATABASE --------------------------------

        // Retrieve data from a particular snapshot of current user in the "Users" node
        // and store to the variables (name, profilePic)
        userReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                nameString = user.getName();
                name.setText(nameString);
                profilePic.setBackgroundResource(Integer.parseInt(user.getImageID()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Retrieve data from a particular snapshot of "Users" node
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the count of children of the node
                numUsers = (int) snapshot.getChildrenCount();

                // Clear (reset) the list of user IDs
                userIDs.clear();

                // Loop to traverse the list of all children of the node
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    assert firebaseUser != null;

                    // Retrieve all users from Firebase (except the user themself)
                    // Store to the list of user IDs
                    if(!user.getId().equals(firebaseUser.getUid())) {
                        userIDs.add(user.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // -----------------------------------------------------------------------------------------
        // ----------------------------------------- BUTTON ----------------------------------------

        // When user clicks on "FriendRequest" button (waving hand)
        // They are redirected to the FriendRequest activity
        friendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatHomepage.this, FriendRequest.class);
                startActivity(intent);
                finish();
            }
        });

        // When user clicks on "RandomChat" button ...
        RandomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                randomReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // The user's ID is added to the "RandomChat" node in the Database
                        randomReference.child(firebaseUser.getUid()).child("id").setValue(firebaseUser.getUid());

                        // Run the startRandomSelect() method to initiate the random chat
                        startRandomSelect();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        // -----------------------------------------------------------------------------------------
        // ---------------------------------------- FRAGMENT ---------------------------------------

        // TabLayout creates the layout of multiple tabs
        // ViewPager displays each corresponding tab
        // Link both to their corresponding element in the layout
        TabLayout tabLayout = findViewById(R.id.chathome_tab_layout);
        ViewPager viewPager = findViewById(R.id.chathome_view_pager);

        // ViewPagerAdapter manages the viewPager
        // Create two tabs (fragments) virtually: Chats and Friends
        // "Chats" include all existing chats
        // "Friends" include all current friends of the user, even if their chat history is empty
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
        viewPagerAdapter.addFragment(new FriendsFragment(), "Friends");

        // Apply changes to the app
        // This initiates the creation of the two tabs and links them to the actual app physically
        // The layout of the tab bar is updated to show new tabs, and each tab has each own page
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }


    // -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------
    // --------------------------------- ViewPagerAdapter CLASS --------------------------------
    // -----------------------------------------------------------------------------------------

    // This class creates and manages the view (appearance) of the tab (fragment)
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fm, String title) {
            fragments.add(fm);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

    }


    // -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------
    // -------------------------------- startRandomSelect METHOD -------------------------------
    // -----------------------------------------------------------------------------------------

    // This method manages the random chat feature
    // It allows two users to be connected if the condition is valid
    private void startRandomSelect() {

        // -----------------------------------------------------------------------------------------
        // -------------------------------- "DO NOT CONNECT" DATABASE ------------------------------

        // Create a list of IDs which must not be connected to the current user
        doNotConnectList = new ArrayList<>();

        // Connect (refer) to the "DoNotConnect" node in the Database
        DatabaseReference doNotConnectRef = FirebaseDatabase.getInstance().getReference("DoNotConnect")
                .child(firebaseUser.getUid());

        // Retrieve from database and store IDs of users whom must not be connected to current user
        doNotConnectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear (reset) list
                doNotConnectList.clear();

                // Loop to traverse the list of all children of the node
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    // Retrieve id of children of each snapshot and add to list
                    String id = dataSnapshot.getValue(String.class);
                    doNotConnectList.add(id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // -----------------------------------------------------------------------------------------
        // ------------------------------ WAITING SCREEN - POPUP WINDOW ----------------------------

        // Display the loading (waiting) screen as a popup window
        View view = View.inflate(this, R.layout.chat_popup_search, null);
        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT,true);

        findViewById(R.id.chat_homepage).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(findViewById(R.id.chat_homepage), Gravity.CENTER, 0, 0);
            }
        });

        RelativeLayout background = view.findViewById(R.id.chat_popup_search_layout);
        background.setVisibility(View.VISIBLE);

        // User can wait for max 60 seconds before being disconnected from Random Match Database
        // due to no available match
        TextView timer = view.findViewById(R.id.chat_popup_search_progress_timer);
        ImageButton cancelBtn = view.findViewById(R.id.chat_popup_search_cancel_btn);


        // -----------------------------------------------------------------------------------------
        // ---------------------------------- WAITING SCREEN - TIMER -------------------------------

        int maxTime = 60000;    // Max wait time = 60 secs
        int minTime = 55000;    // Min wait time = 55 secs

        // Create a new count-down timer, with interval of 1 sec
        new CountDownTimer(maxTime,1000){

            // Initialize the matched boolean to false as
            // the user did not get any match the moment they starts
            boolean matched = false;

            // Variable to store the random Icebreaker question
            String question;

            // -------------------------------------------------------------------------------------
            // -------------------------- WAITING SCREEN - TIMER - ON TICK -------------------------

            // On each tick (1 sec) of the timer ...
            public void onTick(long millisUntilFinished) {

                // Display the remaining seconds on the screen
                timer.setText(""+ millisUntilFinished/1000);

                // When the user clicks on the "Cancel" button, the popup window is closed
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        background.setVisibility(View.GONE);
                        popupWindow.dismiss();

                        // Remove the user from "RandomChat" database
                        randomReference.child(firebaseUser.getUid()).removeValue();

                        // Cancel the timer
                        cancel();
                    }
                });

                // Retrieve all the IDs of users waiting in the "RandomChat" database
                randomReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        randomUserIDs.clear();

                        // Loop to traverse the list of all children of the node
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            // Retrieve id of children of each snapshot
                            // (except the current user themself) and add to list
                            String user = dataSnapshot.child("id").getValue(String.class);
                            if(!user.equals(firebaseUser.getUid())) {
                                randomUserIDs.add(user);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                numRandomUsers = randomUserIDs.size();

                // Check if the user is still not matched and the timer passed the min time
                // Min time ensures that the user will get to the loading screen for at least
                // 5 seconds (maxTime - minTime) before any match is attempted
                if(matched == false  && millisUntilFinished <= minTime){

                    // First, check if the current user has been connected with any other user
                    // (that "other user" is waiting on current user)
                    // Since the code can only control one user at a time

                    // If user A is matched with user B, user A will be put in the chat room with user B
                    // Database will store information indicating that user A is waiting for user B
                    // ---> ("ChatToBeConnected" node - connectReference)
                    // User B will then have to go look for user A, then manually go to the chat room with user A


                    // Retrieve data from the "ChatToBeConnected" database
                    connectReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                String searcher = dataSnapshot.child("searcher").getValue(String.class);
                                String waiter = dataSnapshot.child("waiter").getValue(String.class);
                                String question = dataSnapshot.child("question").getValue(String.class);

                                // If the user finds someone already waiting on them
                                // Match with that waiting user
                                if(searcher.equals(firebaseUser.getUid())) {
                                    matched = true;
                                    connectReference.child(waiter).removeValue();

                                    // Call the pairSuccessfully() method to finalize the match
                                    pairSuccessfully(background, popupWindow, waiter, question);

                                    // Cancel the timer
                                    cancel();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                    // The finding process only starts if there exists other users using the Random feature
                    // (other than the current user)
                    if(matched == false && numRandomUsers >= 1) {

                        // Generate a random user ID from the current list
                        Random rand = new Random();
                        int randomIndex = rand.nextInt(numRandomUsers);
                        String userMatch = randomUserIDs.get(randomIndex);

                        // Ensure that the potential match is not in the DoNotConnect list
                        if (!doNotConnectList.contains(userMatch)) {

                            // This boolean value ensure that the user will only be matched once
                            matched = true;

                            // Find a random question from the given list
                            question = findRandomQuestion();

                            // Add information to DB so that the user #2 can find the current user and connect
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("searcher", userMatch);
                            hashMap.put("waiter", firebaseUser.getUid());
                            hashMap.put("question", question);
                            connectReference.child(firebaseUser.getUid()).setValue(hashMap);

                            // Call the pairSuccessfully() method to finalize the match
                            pairSuccessfully(background, popupWindow, userMatch, question);

                            // Cancel the timer
                            cancel();
                        }
                    }

                }
            }

            // -------------------------------------------------------------------------------------
            // ------------------------- WAITING SCREEN - TIMER - ON FINISH ------------------------

            // If the time is up without a match
            // Prompt a message and close the popup window
            // Remove the user from "RandomChat" database
            public void onFinish() {
                Toast.makeText(ChatHomepage.this, "Sorry, no available match at the moment.",Toast.LENGTH_LONG).show();
                background.setVisibility(View.GONE);
                popupWindow.dismiss();
                randomReference.child(firebaseUser.getUid()).removeValue();
            }
        }.start();
    }


    // -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------
    // -------------------------------- pairSuccessfully METHOD --------------------------------
    // -----------------------------------------------------------------------------------------

    // This method finalizes the random match by closing the waiting window,
    // open the Chatroom and modify the database
    public void pairSuccessfully(RelativeLayout background, PopupWindow popupWindow, String userMatch, String question){

        // Close the waiting screen - popup window
        background.setVisibility(View.GONE);
        popupWindow.dismiss();

        // Pass information (user and question) to the chat room
        // Redirect the user to ChatRoom activity
        Intent intent = new Intent(ChatHomepage.this, ChatRoom.class);
        Bundle extras = new Bundle();
        extras.putString("userid", userMatch);
        extras.putString("question", question);
        intent.putExtras(extras);
        startActivity(intent);
        finish();

        // Remove the user from "RandomChat" database
        randomReference.child(firebaseUser.getUid()).removeValue();
    }


    // -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------
    // ------------------------------- findRandomQuestion METHOD -------------------------------
    // -----------------------------------------------------------------------------------------

    // This method returns the random question from a list
    private String findRandomQuestion(){
        // Get random question from a text file

        // Get the file of questions
        InputStream inputStream = this.getResources().openRawResource(R.raw.sampleqs);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> questions = new ArrayList<String>();

        // Read the file and add each question (line) to the list
        String strData = "";
        int i = 0;
        if(inputStream != null){
            try{
                while ((strData = bufferedReader.readLine()) != null){
                    questions.add(strData);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        // Randomly pick a question from the list
        Random rand = new Random();
        int randomIndex = rand.nextInt(questions.size());
        return questions.get(randomIndex);

    }

}