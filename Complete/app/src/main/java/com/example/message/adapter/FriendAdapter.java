package com.example.message.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.message.ChatRoom;
import com.example.message.R;
import com.example.message.model.User;

import java.util.List;

//
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    public FriendAdapter(Context context, List<User> userList){
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_item, parent, false);
        return new FriendAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = userList.get(position);

        // Set the name and profile pic of each user
        holder.name.setText(user.getName());
        holder.profilePic.setImageResource(Integer.parseInt(user.getImageID()));

        // When the user clicks on an item on the recycler view, they will be redirected
        // to the chat room with the other user whom they have selected
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatRoom.class);
                intent.putExtra("userid", user.getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView name;
        public ImageView profilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link variables to the corresponding element in the layout
            name = itemView.findViewById(R.id.friend_name);
            profilePic = itemView.findViewById(R.id.friend_profile_pic);
        }
    }

}
