package com.example.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.message.adapter.ChatAdapter;
import com.example.message.model.Chat;
import com.example.message.model.User;
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

// This is the Chat Room, where the conversation will take place
public class ChatRoom extends AppCompatActivity {

    ImageView profilePic;
    TextView name;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    ImageButton sendBtn;
    EditText textBox;
    Intent intent;

    ChatAdapter chatAdapter;
    List<Chat> chatList;
    RecyclerView recyclerView;
    String nameString, receiverID, question;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);    // The layout connected to this method

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.chatroom_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ImageButton backBtn = findViewById(R.id.chatroom_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatRoom.this, ChatHomepage.class);
                startActivity(intent);
                finish();
            }
        });

        recyclerView = findViewById(R.id.chatroom_recycler_view);
        // The next line indicates that the children of recyclerView
        //      all have the same size (width, height)
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Link variable to the corresponding element in the layout (.xml)
        profilePic = findViewById(R.id.chatroom_profile_pic);
        name = findViewById(R.id.chatroom_name);
        textBox = findViewById(R.id.chatroom_text_box);
        sendBtn = findViewById(R.id.chatroom_send_btn);


        // Retrieve parameters passed by ChatHomepage during Random Chat
        intent = getIntent();
        Bundle extras = intent.getExtras();
        receiverID = extras.getString("userid");
        question = extras.getString("question");

        // Question window only pops up for Random Chat
        if(question != null){
            openQuestionPopupWindow(question);
        }

        // If the user hits the Send Button, the message will be saved to the database
        // Only proceed to send message if the input is not empty
        // The text box is set back to Empty (no character) for the next input
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textBox.getText().toString();

                if(!message.isEmpty()){
                    sendMessage(firebaseUser.getUid(),receiverID,message);
                }
                textBox.setText("");
            }
        });

        // Connect to Firebase from the current user's perspective
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(receiverID);

        // Listen to (aka Record) any upcoming data
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve data from a particular snapshot
                // Store the data to the declared variables
                User user = snapshot.getValue(User.class);

                nameString = user.getName();
                name.setText(nameString);

                profilePic.setImageResource(Integer.parseInt(user.getImageID()));

                // Read the message of current user (sent and received) from database
                readMessage(firebaseUser.getUid(), receiverID, Integer.parseInt(user.getImageID()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.chat_room_friend_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat_room_add_friend:
                addFriend();
                return true;

            case R.id.chat_room_do_not_connect_again:
                doNotConnectAgain();
                return (true);

            case R.id.chat_room_report_user:
                openReportWindow(receiverID);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }


    // After the user hits send, the message is proceeded to be stored on database
    // Hashmap created the item in the database based on the given items (parameters)
    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        // All collected data will be under a shared key called "ChatHistory"
        messageRef.child("ChatHistory").push().setValue(hashMap);

        DatabaseReference chatListRef = FirebaseDatabase.getInstance().getReference("ChatList");

        chatListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatListRef.child(receiver).child(sender).setValue(sender);
                chatListRef.child(sender).child(receiver).setValue(receiver);
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // This method read all the messages from the database that are under the key "Chats"
    // In every run, the list of chats is cleared to avoid duplicates, since the Message data
    //      will be read from the very first one in the database (traversal)
    // During the traversal, compare the receiver ID and sender ID with the existing ones on database
    //      to ensure the correct conversation is retrieved
    // Apply the Adapter function to finalize the change
    private void readMessage(String senderId, String receiverId, int imageID){
        chatList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatHistory");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId) ||
                            chat.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)){
                        chatList.add(chat);
                    }

                    chatAdapter= new ChatAdapter(ChatRoom.this, chatList, imageID);
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void openReportWindow(String reportedId){
        View view = View.inflate(this, R.layout.report_window, null);
        EditText report = view.findViewById(R.id.report_text);
        Button sendBtn = view.findViewById(R.id.send_report_btn);

        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT,true);

        findViewById(R.id.chatroom).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(findViewById(R.id.chatroom), Gravity.CENTER, 0, 0);
            }
        });

        RelativeLayout background = view.findViewById(R.id.report_window_layout);
        background.setVisibility(View.VISIBLE);

        // Display the question



        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reportText = report.getText().toString();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Reports").child(reportedId).child(firebaseUser.getUid());
                ref.child("Report").setValue(reportText);
                Toast.makeText(ChatRoom.this, "Report Sent", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });
    }

    public void openQuestionPopupWindow(String randomQuestion){
        View view = View.inflate(this, R.layout.chat_popup_question, null);
        TextView question = view.findViewById(R.id.chat_popup_question_text);
        Button readyButton = view.findViewById(R.id.chat_popup_ready_btn);

        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT,true);

        findViewById(R.id.chatroom).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(findViewById(R.id.chatroom), Gravity.CENTER, 0, 0);
            }
        });

        RelativeLayout background = view.findViewById(R.id.chat_popup_question_layout);
        background.setVisibility(View.VISIBLE);

        // Display the question
        question.setText(randomQuestion);

        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }

    public void addFriend(){
        View view = View.inflate(this, R.layout.chat_popup_add_friend, null);
        DatabaseReference friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        Button yesBtn = view.findViewById(R.id.chat_popup_add_friend_yes_btn);
        Button noBtn = view.findViewById(R.id.chat_popup_add_friend_no_btn);
        TextView text = view.findViewById(R.id.chat_popup_add_friend_text);
        text.setText("Add " + nameString + "\nto your friend list?");

        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT,true);

        findViewById(R.id.chatroom).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(findViewById(R.id.chatroom), Gravity.CENTER, 0, 0);
            }
        });

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friendRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("requester", firebaseUser.getUid());
                        hashMap.put("requestee", receiverID);
                        friendRequestRef.push().setValue(hashMap);
                        Toast.makeText(ChatRoom.this, "Your friend request has been sent!", Toast.LENGTH_SHORT).show();
                        popupWindow.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

    }

    public void doNotConnectAgain() {
        View view = View.inflate(this, R.layout.chat_popup_other, null);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DoNotConnect");
        Button yesBtn = view.findViewById(R.id.chat_popup_other_yes_btn);
        Button noBtn = view.findViewById(R.id.chat_popup_other_no_btn);
        TextView text = view.findViewById(R.id.chat_popup_other_text);
        TextView note = view.findViewById(R.id.chat_popup_other_note);

        text.setText("Do not connect with\n" + nameString + " again?");
        note.setText("You and " + nameString + " will not\nbe matched again in the future");

        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT, true);

        findViewById(R.id.chatroom).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(findViewById(R.id.chatroom), Gravity.CENTER, 0, 0);
            }
        });

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        ref.child(firebaseUser.getUid()).child(receiverID).setValue(receiverID);
                        ref.child(receiverID).child(firebaseUser.getUid()).setValue(firebaseUser.getUid());

                        Toast.makeText(ChatRoom.this, "You two will not be matched again!", Toast.LENGTH_SHORT).show();
                        popupWindow.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

    }
}



