package com.example.chatappdemo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappdemo.R;
import com.example.chatappdemo.model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.MessageViewHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private List<Messages> userMessagesList;
    private FirebaseUser firebaseUser;
    private Context context;
    String imageUrl;

    public AdapterMessage(List<Messages> userMessagesList, Context context, String imageUrl) {
        this.userMessagesList = userMessagesList;
        this.context = context;
        this.imageUrl = imageUrl;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView MessageText,  timeTv, isSeenTv;
        public CircleImageView ProfileImage;
        public RelativeLayout messageLayout;
        private ImageView message_image;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            MessageText = (TextView) itemView.findViewById(R.id.message_text);
            ProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            message_image = itemView.findViewById(R.id.messageIv);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MessageViewHolder(view);
        }

    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int i) {
        String message = userMessagesList.get(i).getMessage();
        String timeStamp = userMessagesList.get(i).getTimeStamp();
        String type = userMessagesList.get(i).getType();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (type.equals("image")) {
            messageViewHolder.MessageText.setVisibility(View.GONE);
            messageViewHolder.message_image.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(message).placeholder(R.drawable.image_iv).into(messageViewHolder.message_image);
            } catch (Exception e) {
                messageViewHolder.message_image.setImageResource(R.drawable.image_iv);
            }
        }else {
            messageViewHolder.MessageText.setVisibility(View.VISIBLE);
            messageViewHolder.message_image.setVisibility(View.GONE);
            messageViewHolder.MessageText.setText(message);
        }


        messageViewHolder.timeTv.setText(dateTime);
        try{
            Picasso.get().load(imageUrl).placeholder(R.drawable.user_profile).into(messageViewHolder.ProfileImage);
        } catch (Exception e) {

        }
        //click to show time
        messageViewHolder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageViewHolder.timeTv.getVisibility() == View.GONE) {
                    messageViewHolder.timeTv.setVisibility(View.VISIBLE);
                } else {
                    messageViewHolder.timeTv.setVisibility(View.GONE);
                }
            }
        });

        // delete dialog
        messageViewHolder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(i);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });

        if (i == userMessagesList.size()-1) {
            if (userMessagesList.get(i).isSeen()) {
                messageViewHolder.isSeenTv.setText("Seen");
            } else {
                messageViewHolder.isSeenTv.setText("Delivered");
            }
        } else {
            messageViewHolder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int i) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String msgTimeStamp = userMessagesList.get(i).getTimeStamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (ds.child("from").getValue().equals(myUID)) {
                        //remove from chats
                        ds.getRef().removeValue();
                        //set value
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context,"Message deleted...",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,"You can delete only your messages...",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (userMessagesList.get(position).getFrom().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
