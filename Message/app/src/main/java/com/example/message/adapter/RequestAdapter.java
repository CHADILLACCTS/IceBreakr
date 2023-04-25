package com.example.message.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.message.FriendRequest;
import com.example.message.R;
import com.example.message.model.Request;
import com.example.message.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    ArrayList<Request> requestList;
    Request request;
    private Context context;

    public RequestAdapter(Context context, ArrayList<Request> requestList){
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friendrequest_item, parent, false);
        RequestViewHolder requestViewHolder = new RequestViewHolder(view);
        return  requestViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        request = requestList.get(position);
        holder.request = request;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(request.getRequester());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                holder.name.setText(user.getName());
                holder.pic.setBackgroundResource(Integer.parseInt(user.getImageID()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        public TextView name;

        public ImageView pic;
        ImageButton accept, decline;
        Request request;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView;
            name = itemView.findViewById(R.id.friend_request_item_name);
            pic = itemView.findViewById(R.id.friend_request_item_profile_pic);
            accept = itemView.findViewById(R.id.friend_request_item_accept_btn);
            decline = itemView.findViewById(R.id.friend_request_item_decline_btn);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference requesteeFriendRef = FirebaseDatabase.getInstance().getReference("FriendList").child(request.getRequestee());
                    DatabaseReference requesterFriendRef = FirebaseDatabase.getInstance().getReference("FriendList").child(request.getRequester());
                    requesteeFriendRef.child(request.getRequester()).setValue(request.getRequester());
                    requesterFriendRef.child(request.getRequestee()).setValue(request.getRequestee());

                    DatabaseReference friendRequestRef = FirebaseDatabase.getInstance().getReference("FriendRequest");
                    friendRequestRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                String requester = dataSnapshot.child("requester").getValue(String.class);
                                String requestee = dataSnapshot.child("requestee").getValue(String.class);
                                if(requester.equals(request.getRequester()) && requestee.equals(request.getRequestee())){
                                    dataSnapshot.getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Toast.makeText(accept.getContext(), "Accept", Toast.LENGTH_SHORT).show();
                    Log.d("test","item clicked");

                }
            });
        }
    }
}

