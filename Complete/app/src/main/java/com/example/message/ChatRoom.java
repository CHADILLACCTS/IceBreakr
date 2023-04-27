package com.example.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// This is the Chat Room, where the conversation will take place
public class ChatRoom extends AppCompatActivity {

    ImageView profilePic;
    TextView name;
    FirebaseUser firebaseUser;
    ImageButton sendBtn;
    EditText textBox;
    Intent intent;

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

        // When the user clicks on the Back button, they will be redirected to the ChatHomepage activity
        ImageButton backBtn = findViewById(R.id.chatroom_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatRoom.this, ChatHomepage.class);
                startActivity(intent);
                finish();
            }
        });

        // Link variable to the corresponding element in the layout (.xml)
        recyclerView = findViewById(R.id.chatroom_recycler_view);
        profilePic = findViewById(R.id.chatroom_profile_pic);
        name = findViewById(R.id.chatroom_name);
        textBox = findViewById(R.id.chatroom_text_box);
        sendBtn = findViewById(R.id.chatroom_send_btn);

        // Set up the recycler view
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);  // message is displayed starting from the bottom
        recyclerView.setLayoutManager(linearLayoutManager);

        // Retrieve parameters passed by ChatHomepage during Random Chat
        intent = getIntent();
        Bundle extras = intent.getExtras();
        receiverID = extras.getString("userid");
        question = extras.getString("question");

        // Question window only pops up for Random Chat
        if (question != null) {
            openQuestionPopupWindow(question);
        }

        // -----------------------------------------------------------------------------------------
        // -------------------------------------- SEND BUTTON --------------------------------------

        // If the user hits the Send Button, the message will be saved to the database
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve message from user's input
                String message = textBox.getText().toString();

                // Only proceed to send message if the input is not empty
                if (!message.isEmpty()) {
                    sendMessage(firebaseUser.getUid(), receiverID, message);
                }

                // The text box is set back to Empty (no character) for the next input
                textBox.setText("");
            }
        });

        // -----------------------------------------------------------------------------------------
        // ----------------------------------- SET UP CHAT ROOM ------------------------------------

        // Connect to Firebase from the current user's perspective
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(receiverID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve data from a particular snapshot and store to variables
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


    // ---------------------------------------------------------------------------------------------
    // ------------------------------- openQuestionPopupWindow() METHOD ----------------------------

    // This method displays a popup window showing the random Icebreaker question
    // The window only appears if it was a random chat
    public void openQuestionPopupWindow(String randomQuestion) {

        // Retrieve layout and link variable to the corresponding element in the layout
        View view = View.inflate(this, R.layout.chat_popup_question, null);
        TextView question = view.findViewById(R.id.chat_popup_question_text);
        Button readyButton = view.findViewById(R.id.chat_popup_ready_btn);

        // Create a new popup window
        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, true);

        // Display the popup window
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

        // When the user clicks on the "Ready" button, the popup window will close
        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }



    // ---------------------------------------------------------------------------------------------
    // ----------------------------------- readMessage() METHOD ------------------------------------

    // This method read all the messages from the database that are under the key "ChatHistory"
    // This happens upon the creation of every chat room
    private void readMessage(String senderId, String receiverId, int imageID) {

        // Create a list to store history of chat
        List<Chat> chatHistoryList = new ArrayList<>();

        // Connect to Firebase under "ChatHistory" node
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatHistory");

        // Modify data under "ChatHistory" node
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // CLear (reset) list
                chatHistoryList.clear();

                // Loop to retrieve all children of the snapshot
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    // Retrieve and store the chat history to the list
                    // if the chat belongs to both users presenting in the chat room
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId)
                            || chat.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)) {
                        chatHistoryList.add(chat);
                    }

                    // Apply the list to the recycler view using the adapter so it can display
                    ChatAdapter chatAdapter = new ChatAdapter(ChatRoom.this, chatHistoryList, imageID);
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }



    // ---------------------------------------------------------------------------------------------
    // ----------------------------------- sendMessage() METHOD ------------------------------------

    // This method saves the message to the database under the key "ChatHistory" and "ChatList"
    // This happens after the user hits the "Send" button
    private void sendMessage(String sender, String receiver, String message) {

        // Connect to Firebase under the "ChatHistory" node
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("ChatHistory");

        // Create a hashmap and store data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        // All collected data (message that was sent) will be
        // stored under a shared ID, which is under "ChatHistory"
        messageRef.push().setValue(hashMap);

        // Connect to Firebase under the "ChatList" node
        DatabaseReference chatListRef = FirebaseDatabase.getInstance().getReference("ChatList");

        // If user A sends user B a message (with or without a message sent back by user B)
        // Store data to the database to indicate a conversation exists between two users
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



    // -----------------------------------------------------------------------------------------
    // ------------------------------- TOP-RIGHT-CORNER OPTIONS --------------------------------
    public boolean onCreateOptionsMenu(Menu menu) {
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
                report(receiverID);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }



    // ---------------------------------------------------------------------------------------------
    // ----------------------------- OPTION #1 ---- addFriend() METHOD -----------------------------

    // This method adds the user to the FriendRequest database
    // The other user will see the request from their side
    public void addFriend() {

        // Retrieve layout and link variable to the corresponding element in the layout
        View view = View.inflate(this, R.layout.chat_popup_other, null);
        Button yesBtn = view.findViewById(R.id.chat_popup_other_yes_btn);
        Button noBtn = view.findViewById(R.id.chat_popup_other_no_btn);
        TextView text = view.findViewById(R.id.chat_popup_other_text);
        TextView note = view.findViewById(R.id.chat_popup_other_note);

        // Set the text to be displayed
        text.setText("Add " + nameString + "\nto your friend list?");
        note.setText("A friend request will be \nsent to " + nameString);

        // Create a new popup window
        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, true);

        // Display the popup window
        findViewById(R.id.chatroom).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(findViewById(R.id.chatroom), Gravity.CENTER, 0, 0);
            }
        });

        // If the user clicks on the "Yes" button...
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Connect (reference) to database under the "FriendRequest" node
                DatabaseReference friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
                friendRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Create a new hashmap and store information of a friend quest to it
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("requester", firebaseUser.getUid());
                        hashMap.put("requestee", receiverID);

                        // The hashmap with the friend request is pushed to the database
                        friendRequestRef.push().setValue(hashMap);

                        // Prompt a message to tell the user that the request has been sent
                        Toast.makeText(ChatRoom.this, "Your friend request has been sent!", Toast.LENGTH_SHORT).show();

                        // Close the popup window
                        popupWindow.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        // If the user clicks on "No", close the popup window
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }



    // ---------------------------------------------------------------------------------------------
    // ------------------------- OPTION #2 ---- doNotConnectAgain() METHOD -------------------------

    // This method adds the user to the DoNotConnect database
    // Those users that are blocked by either one will not
    // be connected while using Random Chat feature
    public void doNotConnectAgain() {

        // Retrieve layout and link variable to the corresponding element in the layout
        View view = View.inflate(this, R.layout.chat_popup_other, null);
        Button yesBtn = view.findViewById(R.id.chat_popup_other_yes_btn);
        Button noBtn = view.findViewById(R.id.chat_popup_other_no_btn);
        TextView text = view.findViewById(R.id.chat_popup_other_text);
        TextView note = view.findViewById(R.id.chat_popup_other_note);

        // Set the text to be displayed
        text.setText("Do not connect with\n" + nameString + " again?");
        note.setText("You and " + nameString + " will not\nbe matched again in the future");

        // Create a new popup window
        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT, true);

        // Display the popup window
        findViewById(R.id.chatroom).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(findViewById(R.id.chatroom), Gravity.CENTER, 0, 0);
            }
        });

        // If the user clicks on "Yes" button ...
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Connect (reference) to the database under the "DoNotConnect" node
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DoNotConnect");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // Retrieve and store the information of both users (under a shared unique ID)
                        // to the "DoNotConnect" database
                        ref.child(firebaseUser.getUid()).child(receiverID).setValue(receiverID);
                        ref.child(receiverID).child(firebaseUser.getUid()).setValue(firebaseUser.getUid());

                        // Prompt a message to tell the user that they will not be connected again
                        Toast.makeText(ChatRoom.this, "You two will not be matched again!", Toast.LENGTH_SHORT).show();

                        // Close the popup window
                        popupWindow.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        // If the user clicks on "No", close the popup window
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }



    // ---------------------------------------------------------------------------------------------
    // ------------------------------ OPTION #3 ---- report() METHOD -------------------------------

    // This method creates the layout and let the user fills in a report
    public void report(String reportedId) {

        // Retrieve layout and link variable to the corresponding element in the layout
        View view = View.inflate(this, R.layout.chat_popup_report, null);
        EditText report = view.findViewById(R.id.report_text);
        Button sendBtn = view.findViewById(R.id.chat_popup_report_send_btn);
        Button cancelBtn = view.findViewById(R.id.chat_popup_report_cancel_btn);

        // Create a new popup window
        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, true);

        // Display the popup window
        findViewById(R.id.chatroom).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(findViewById(R.id.chatroom), Gravity.CENTER, 0, 0);
            }
        });

        RelativeLayout background = view.findViewById(R.id.report_window_layout);
        background.setVisibility(View.VISIBLE);

        // When the user clicks on the "Send" button ...
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Retrieve the text of the report from the user's input
                String reportText = report.getText().toString();

                // Connect (reference) to the database under "Reports" node --> receiver ID --> current user ID
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Reports").child(reportedId).child(firebaseUser.getUid());

                // Store the text of report to the database
                ref.child("Report").setValue(reportText);

                // Prompt a message telling the user that the report was sent
                Toast.makeText(ChatRoom.this, "Report Sent", Toast.LENGTH_SHORT).show();

                // Close the popup window
                popupWindow.dismiss();
            }
        });

        // When the user clicks on the "Cancel" button, close the popup window
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }
}
