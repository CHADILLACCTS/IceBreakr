package com.example.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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

    ImageView backBtn;
    EditText textBox;
    Intent intent;

    ChatAdapter chatAdapter;
    List<Chat> chatList;
    RecyclerView recyclerView;


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

        openPopupWindow();

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
        backBtn = findViewById(R.id.chatroom_back_btn);

        intent = getIntent();
        String receiverID = intent.getStringExtra("userid");



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

//                openPopupWindow();
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
                name.setText(user.getName());
                if(user.getImageURL().equals("default")){
                    profilePic.setImageResource(R.mipmap.ic_launcher);
                }
                // Read the message of current user (sent and received) from database
                readMessage(firebaseUser.getUid(), receiverID, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatRoom.this, ChatHomepage.class);
                startActivity(intent);
                finish();
            }
        });

    }

    // After the user hits send, the message is proceeded to be stored on database
    // Hashmap created the item in the database based on the given items (parameters)
    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        // All collected data will be under a shared key called "Chats"
        messageRef.child("Chats").push().setValue(hashMap);

        DatabaseReference chatListRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(sender)
                .child(receiver);

        chatListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatListRef.child("id").setValue(receiver);
                }
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
    private void readMessage(String senderId, String receiverId, String imageURL){
        chatList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

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

                    chatAdapter= new ChatAdapter(ChatRoom.this, chatList, imageURL);
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void openPopupWindow(){
//        LayoutInflater inflater = (LayoutInflater) ChatRoom.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = View.inflate(this, R.layout.chat_popup_question, null);
        TextView question = view.findViewById(R.id.chat_popup_question_text);
        Button readyButton = view.findViewById(R.id.chat_popup_ready_btn);
        Button anotherButton = view.findViewById(R.id.chat_popup_another_btn);

        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT,true);

        findViewById(R.id.chatroom).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(findViewById(R.id.chatroom), Gravity.CENTER, 0, 0);
            }
        });
//        popupWindow.showAtLocation(findViewById(R.id.chatroom), Gravity.CENTER, 0, 0);

        RelativeLayout background = view.findViewById(R.id.chat_popup_background);
        background.setVisibility(View.VISIBLE);

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
        String randomElement = questions.get(randomIndex);

        // Display the question
        question.setText(randomElement);

        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(ChatRoom.this, "Enjoy chatting! :)", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
                background.setVisibility(View.GONE);
                popupWindow.dismiss();
            }
        });

        anotherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                background.setVisibility(View.GONE);
                openPopupWindow();
                popupWindow.dismiss();
            }
        });
    }
}



