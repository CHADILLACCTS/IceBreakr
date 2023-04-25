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
    DatabaseReference reference, userReference, randomReference, connectReference;

    static int numUsers, numRandomUsers;

    private List<String> userIDs, randomUserIDs, doNotConnectList;






    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        userIDs = new ArrayList<String>();
        randomUserIDs = new ArrayList<String>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_homepage);    // The layout connected to this file

        ImageButton backBtn = findViewById(R.id.chathome_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatHomepage.this, HomeScreen.class);
                startActivity(intent);
                finish();
            }
        });

        // Link variable to the corresponding element in the layout (.xml)
        profilePic = findViewById(R.id.chathome_profile_pic);
        name = findViewById(R.id.chathome_name);
        RandomBtn = findViewById(R.id.chathome_random_chat_btn);
        friendRequestBtn = findViewById(R.id.chathome_friend_request_btn);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        randomReference = FirebaseDatabase.getInstance().getReference("RandomChat");
//        connectReference = FirebaseDatabase.getInstance().getReference("ChatToBeConnected");

        // Listen to (Record) any upcoming data
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve data from a particular snapshot
                // Store the data to the declared variables
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

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numUsers = (int) snapshot.getChildrenCount();
                userIDs.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);

                    assert user != null;
                    assert firebaseUser != null;

                    // Retrieve all users from Firebase (except the user themself)
                    if(!user.getId().equals(firebaseUser.getUid())) {
                        userIDs.add(user.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        String userID = firebaseUser.getUid();

        friendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatHomepage.this, FriendRequest.class);
                startActivity(intent);
                finish();
            }
        });

        RandomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                randomReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        randomReference.child(userID).child("id").setValue(userID);

                        startRandomSelect();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });



        // TabLayout creates the layout of multiple tabs
        // ViewPager displays each corresponding tab
        // Link both to their corresponding layout
        TabLayout tabLayout = findViewById(R.id.chathome_tab_layout);
        ViewPager viewPager = findViewById(R.id.chathome_view_pager);

        // ViewPagerAdapter manages the viewPager
        // Create two tabs (fragments) virtually: Chats and Friends
        // "Chats" include all existing chats
        // "Friends" include all current friends of the user, even if they have never had a chat
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
        viewPagerAdapter.addFragment(new FriendsFragment(), "Friends");

        // Apply changes to the app
        // This initiates the creation of the two tabs and links them to the actual app physically
        // The layout of the tab bar is updated to show new tabs, and each tab has each own page
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }


    // This method manages the view (appearance) of the tab
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

    private void startRandomSelect() {

        // Display the loading (waiting) screen
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

        // User can wait for max 60 seconds before being disconnected from Random Match DB
        // due to no available match
        TextView timer = view.findViewById(R.id.chat_popup_search_progress_timer);
        ImageButton cancelBtn = view.findViewById(R.id.chat_popup_search_cancel_btn);

        doNotConnectList = new ArrayList<>();
        DatabaseReference doNotConnectRef = FirebaseDatabase.getInstance().getReference("DoNotConnect")
                .child(firebaseUser.getUid());

        doNotConnectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doNotConnectList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = dataSnapshot.getValue(String.class);
                    doNotConnectList.add(id);
                    // Log.d("*****", id + " --> size = " + doNotConnectList.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        int maxTime = 30000;
        int minTime = 25000;
        new CountDownTimer(maxTime,1000){
            boolean matched = false;
            String question;

            // Loading screen helps the transition look more smooth
            public void onTick(long millisUntilFinished) {
                timer.setText(""+ millisUntilFinished/1000);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        background.setVisibility(View.GONE);
                        popupWindow.dismiss();
                        stopRandomSelect();
                        cancel();
                    }
                });

                randomReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        randomUserIDs.clear();

                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
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

                if(matched == false  && millisUntilFinished <= minTime){
                    // First, check if the current user has been connected with any other user
                    // Since the code can only control one user at a time
                    // If the current user is matched with user X, the current user will be put in the chat room with user X
                    // Database will store information indicating that current user is waiting for user X
                    // User X will then have to go look for the current user, then manually go to the chat room with the current user
                    connectReference = FirebaseDatabase.getInstance().getReference("ChatToBeConnected");
                    connectReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                String searcher = dataSnapshot.child("searcher").getValue(String.class);
                                String waiter = dataSnapshot.child("waiter").getValue(String.class);
                                String question = dataSnapshot.child("question").getValue(String.class);
                                if(searcher.equals(firebaseUser.getUid())) {
                                    matched = true;
                                    connectReference.child(waiter).removeValue();
                                    Intent intent = new Intent(ChatHomepage.this,ChatRoom.class);
                                    Bundle extras = new Bundle();
                                    extras.putString("userid", waiter);
                                    extras.putString("question", question);
                                    intent.putExtras(extras);
                                    startActivity(intent);
                                    finish();
                                    stopRandomSelect();
                                    cancel();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    // If not, find a user to match in the given list
                    // The finding process only starts if there exists other users using the Random feature (other than the current user)
                    if(matched == false && numRandomUsers >= 1) {
                        matched = true;       // This boolean value ensure that the user will only be matched once

                        // Generate a random user ID from the current list
                        Random rand = new Random();
                        int randomIndex = rand.nextInt(numRandomUsers);
                        String userMatch = randomUserIDs.get(randomIndex);

                        if (!doNotConnectList.contains(userMatch)) {
                            // Find a random question from the given list
                            question = findRandomQuestion();

                            // Add information to DB so that the user #2 can find the current user and connect
                            // Purpose: reconcile(?)
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("searcher", userMatch);
                            hashMap.put("waiter", firebaseUser.getUid());
                            hashMap.put("question", question);
                            connectReference.child(firebaseUser.getUid()).setValue(hashMap);

                            // Pass information (user and question) to the chat room
                            Intent intent = new Intent(ChatHomepage.this, ChatRoom.class);
                            Bundle extras = new Bundle();
                            extras.putString("userid", userMatch);
                            extras.putString("question", question);
                            intent.putExtras(extras);
                            startActivity(intent);
                            finish();
                            stopRandomSelect();
                            cancel();
                        }
                    }

                }

            }
            public void onFinish() {
                Toast.makeText(ChatHomepage.this, "Sorry, no available match at the moment.",Toast.LENGTH_LONG).show();
                background.setVisibility(View.GONE);
                popupWindow.dismiss();
                stopRandomSelect();
            }
        }.start();
    }

    private void stopRandomSelect(){
        // Remove user from RandomChat DB when they are paired successfully
        randomReference.child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
//                    Toast.makeText(ChatHomepage.this, "DELETING FROM DB",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(ChatHomepage.this, "FAILED!!!!!!!!!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChatHomepage.this, HomeScreen.class);
        startActivity(intent);
    }

    private String findRandomQuestion(){
        // Get random question from a text file
        // 1. Get the file
        InputStream inputStream = this.getResources().openRawResource(R.raw.sampleqs);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> questions = new ArrayList<String>();
        // 2 . Read the file
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
        // 3. Randomly pick a question from the list
        Random rand = new Random();
        int randomIndex = rand.nextInt(questions.size());
        return questions.get(randomIndex);

    }

}