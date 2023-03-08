package com.example.message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.example.message.fragment.ChatsFragment;
import com.example.message.fragment.FriendsFragment;
import com.example.message.model.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


// This is the Homepage of Chat, where the lists of Existing Chats and Friends are displayed
public class ChatHomepage extends AppCompatActivity {

    ImageView profilePic;
    TextView name;
    Button logoutBtn;

    Button RandomBtn;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    DatabaseReference userReference;

    static int numUsers;

    int max;

    int rand;



    private List<String> userIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        userIDs = new ArrayList<String>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_homepage);    // The layout connected to this file

        // Link variable to the corresponding element in the layout (.xml)
        profilePic = findViewById(R.id.chathome_profile_pic);
        name = findViewById(R.id.chathome_name);
        logoutBtn = findViewById(R.id.chathome_logout_btn);
        RandomBtn = findViewById(R.id.chathome_random_chat_btn);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        // Listen to (Record) any upcoming data
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve data from a particular snapshot
                // Store the data to the declared variables
                User user = snapshot.getValue(User.class);
                name.setText(user.getName());
                if (user.getImageURL().equals("default")) {
                    profilePic.setImageResource(R.mipmap.ic_launcher);
                }
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

        RandomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                randomSelect();
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

        // If click on "Logout" button, user is logged out from the "database's perspective"
        // User will then be directed to the Initial page of the app
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChatHomepage.this, Start.class));
                finish();
            }
        });
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

    private void randomSelect() {
        String user2id;
        int randomIndex = (int) ((Math.random() * (numUsers)-2));
        user2id = userIDs.get(randomIndex);
        Intent intent = new Intent(ChatHomepage.this,ChatRoom.class);
        intent.putExtra("userid", user2id);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChatHomepage.this, HomeScreen.class);
        startActivity(intent);
    }

}