package com.example.chatappdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.activity.ChatActivity;
import com.example.chatappdemo.model.User;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {
    Context context;
    List<User> userList;
    private HashMap<String, String> lastMessageMap;
    private HashMap<String, String> seenMessageMap;

    public AdapterChatlist(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
        seenMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImgAnhDD();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(hisUid);
        String seenMessage = seenMessageMap.get(hisUid);


        //set data
        holder.nameTv.setText(userName);
        if (lastMessage == null || lastMessage.equals("default")) {
            holder.lastMessageTv.setVisibility(View.GONE);
        } else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }

        if (seenMessage == null || seenMessage.equals("default")){
            holder.seenCv.setVisibility(View.GONE);
        }else if(seenMessage != null) {
            if (seenMessage.equals("true")){
                holder.seenCv.setVisibility(View.GONE);
            }else if (seenMessage.equals("false")){
                holder.seenCv.setVisibility(View.VISIBLE);
            }
        }

        try {
            Glide.with(context).load(userImage).placeholder(R.drawable.user_profile).into(holder.profileIv);
        } catch (Exception e) {
            holder.profileIv.setImageResource(R.drawable.user_profile);
        }

        if (userList.get(position).getOnlineStatus().equals("online")) {
            holder.onlineStatusIv.setVisibility(View.VISIBLE);
        } else {
            holder.onlineStatusIv.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("visit_user_id", hisUid);
                context.startActivity(intent);
            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId, lastMessage);
    }

    public void setSeenMessageMap(String userId, String seen) {
        seenMessageMap.put(userId, seen);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView profileIv, onlineStatusIv, seenCv;
        TextView nameTv;
        EmojiconTextView lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            seenCv = itemView.findViewById(R.id.seenCv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }

}
