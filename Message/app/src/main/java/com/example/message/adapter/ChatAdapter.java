package com.example.message.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.message.ChatRoom;
import com.example.message.R;
import com.example.message.model.Chat;
import com.example.message.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int message_rcv = 0;
    public static final int message_send = 1;
    private Context context;
    private List<Chat> chatList;
    private String imageURL;

    FirebaseUser firebaseUser;

    // Declaration of ChatAdapter
    public ChatAdapter(Context context, List<Chat> chatList, String imageURL){
        this.context = context;
        this.chatList = chatList;
        this.imageURL = imageURL;
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

        holder.message.setText(chat.getMessage());

        if(imageURL.equals("default")){
            holder.profilePic.setImageResource(R.mipmap.ic_launcher);
        }
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

            message = itemView.findViewById(R.id.chat_item_text);
            profilePic = itemView.findViewById(R.id.chat_item_image);
        }
    }

    @Override
    public int getItemViewType (int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return message_send;
        }
        else{
            return message_rcv;
        }
    }

}