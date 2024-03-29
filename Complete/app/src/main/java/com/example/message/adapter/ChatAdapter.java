package com.example.message.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.message.R;
import com.example.message.model.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int message_rcv = 0;
    public static final int message_send = 1;
    private Context context;
    private List<Chat> chatList;
    private int imageID;

    FirebaseUser firebaseUser;

    // Declaration of ChatAdapter
    public ChatAdapter(Context context, List<Chat> chatList, int imageID){
        this.context = context;
        this.chatList = chatList;
        this.imageID = imageID;
    }

    // This method decides how the layout of the Chat will look like
    //      depending on the user's Point of View (sender or Receiver)
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == message_send){
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_send, parent, false);
        }
        else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_receive, parent, false);
        }
        return new ViewHolder(view);
    }

    // This method assign the value to message (content) and profile picture as necessary
    //      to show for each chat (each bubble chat)
    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        // Set the message and profile pic of each message
        holder.message.setText(chat.getMessage());
        holder.profilePic.setImageResource(imageID);

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView message;
        public ImageView profilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link variables to corresponding elements in the layout
            message = itemView.findViewById(R.id.chat_item_text);
            profilePic = itemView.findViewById(R.id.chat_item_image);
        }
    }

    @Override
    public int getItemViewType (int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the current user is the sender in the Chat list
        if(chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return message_send;
        }

        // Else, the current user is the receiver
        else{
            return message_rcv;
        }
    }

}