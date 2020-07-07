package com.example.chatappdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappdemo.R;
import com.example.chatappdemo.activity.GroupChatActivity;
import com.example.chatappdemo.model.GroupsList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.HolderGroupList>{
    private Context context;
    private ArrayList<GroupsList> groupsLists;

    public GroupListAdapter(Context context, ArrayList<GroupsList> groupsLists) {
        this.context = context;
        this.groupsLists = groupsLists;
    }

    @NonNull
    @Override
    public HolderGroupList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchats_list, parent, false);
        return new HolderGroupList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupList holder, int position) {
        GroupsList groupsListmodel = groupsLists.get(position);
        String groupId = groupsListmodel.getGroupId();
        String groupIcon = groupsListmodel.getGroupIcon();
        String groupTitle = groupsListmodel.getGroupTitle();

        holder.nameSenderTv.setText("");
        holder.timeGTv.setText("");
        holder.messageTv.setText("");

        //loadLast message and time
        loadLastMessage(groupsListmodel, holder);

        holder.groupTitleTv.setText(groupTitle);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.groupiv).into(holder.groupIconIv);
        } catch (Exception e) {
            holder.groupIconIv.setImageResource(R.drawable.groupiv);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(GroupsList groupsListmodel, HolderGroupList holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupsListmodel.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();
                            String messageType = ""+ds.child("type").getValue();

                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                            if (messageType.equals("image")) {
                                holder.messageTv.setText("Sent Photo");
                            } else {
                                holder.messageTv.setText(message);
                            }

                            holder.timeGTv.setText(dateTime);
                            //get infor sender
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds: snapshot.getChildren()) {
                                                String name = ""+ds.child("name").getValue();
                                                holder.nameSenderTv.setText(name);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupsLists.size();
    }

    class HolderGroupList extends RecyclerView.ViewHolder{
        private CircleImageView groupIconIv, imgDDuser1, imgDDuser2, imgDDuserN;
        private ImageButton moreGIv;
        private TextView groupTitleTv, nameSenderTv, messageTv, timeGTv;

        public HolderGroupList(@NonNull View itemView) {
            super(itemView);
            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            moreGIv = itemView.findViewById(R.id.moreG);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameSenderTv = itemView.findViewById(R.id.nameSenderTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeGTv = itemView.findViewById(R.id.timeGTv);
            imgDDuser1 = itemView.findViewById(R.id.imgDDuser1);
        }
    }
}
