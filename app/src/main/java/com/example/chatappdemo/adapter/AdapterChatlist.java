package com.example.chatappdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.activity.ChatActivity;
import com.example.chatappdemo.activity.InforGroupActivity;
import com.example.chatappdemo.activity.MainActivity;
import com.example.chatappdemo.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
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

        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = RootRef.orderByChild("uid").equalTo(userList.get(position).getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String onlineStatus = ds.child("onlineStatus").getValue().toString();
                    if (onlineStatus.equals("online")) {
                        holder.onlineStatusIv.setVisibility(View.VISIBLE);
                    }else {
                        holder.onlineStatusIv.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("visit_user_id", hisUid);
                context.startActivity(intent);
            }
        });

        //delete
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("You won't be able to recover this chats!")
                        .setConfirmText("Delete!")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                String myUID = FirebaseAuth.getInstance().getUid();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chatlist");
                                ref.child(myUID).child(hisUid)
                                        .removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toasty.success(context, "Chat successfully deleted!", Toast.LENGTH_SHORT, true).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toasty.error(context, ""+e.getMessage(), Toast.LENGTH_SHORT, true).show();
                                            }
                                        });
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .show();
                return false;
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
        TextView nameTv, timeLastMess;
        EmojiconTextView lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            seenCv = itemView.findViewById(R.id.seenCv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
            timeLastMess = itemView.findViewById(R.id.timeLastMess);
        }
    }

}
