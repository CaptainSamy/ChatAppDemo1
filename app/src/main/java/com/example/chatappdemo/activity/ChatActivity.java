package com.example.chatappdemo.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatappdemo.R;
import com.example.chatappdemo.adapter.MessageAdapter;
import com.example.chatappdemo.model.Messages;
import com.example.chatappdemo.model.User;
import com.example.chatappdemo.notifications.Data;
import com.example.chatappdemo.notifications.Sender;
import com.example.chatappdemo.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private String messReceiverId, messReceiverImage, messReceiverName, messSenderId;
    private CircleImageView imgMore, imgProfileFriend, back_user_chat, imgSmile, onlineStatusIv;
    private LinearLayout bottom_linear, sendImage, sendFile, sendGif, sendLocation;
    private TextView name_user_chat, userLastSeen;
    private EditText messageInput;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference RootRef;
    private ValueEventListener seenListerner;
    private DatabaseReference userRefForSeen;

    private List<Messages> messagesList;
    private RecyclerView userMessageList;
    private MessageAdapter messageAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static final int TOTAL_ITEM_TO_LOAD = 12;
    private int mCurrentPage = 1;

    //Solution for descending list on refresh
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";


    private RequestQueue requestQueue;
    private boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid", R.style.AppTheme);
        setTheme(themeIdcurrent);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        back_user_chat = findViewById(R.id.back_user_chat);
        back_user_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgProfileFriend = findViewById(R.id.image_user_chat);
        name_user_chat = findViewById(R.id.name_user_chat);
        onlineStatusIv = findViewById(R.id.onlineStatusIv);
        userLastSeen = findViewById(R.id.user_last_seen);
        userMessageList = (RecyclerView) findViewById(R.id.messager_list_of_users);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);

        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itemPos = 0;
                mCurrentPage++;
                loadMoreMessages();

            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference("Users");
        messSenderId = firebaseAuth.getCurrentUser().getUid();
        messReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        Query query = RootRef.orderByChild("uid").equalTo(messReceiverId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    messReceiverName = ds.child("name").getValue().toString();
                    messReceiverImage = ds.child("imgAnhDD").getValue().toString();
                    String typingStatus = ds.child("typingTo").getValue().toString();
                    if (typingStatus.equals(messSenderId)) {
                        userLastSeen.setText("typing...");
                    } else {
                        String onlineStatus = ds.child("onlineStatus").getValue().toString();
                        if (onlineStatus.equals("online")) {
                            userLastSeen.setText(onlineStatus);
                            onlineStatusIv.setVisibility(View.VISIBLE);
                        } else {
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            userLastSeen.setText("Last seen at: " + dateTime);
                            onlineStatusIv.setVisibility(View.INVISIBLE);
                        }
                    }

                    name_user_chat.setText(messReceiverName);
                    try {
                        Picasso.get().load(messReceiverImage).placeholder(R.drawable.user_profile).into(imgProfileFriend);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.user_profile).into(imgProfileFriend);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        imgSmile = findViewById(R.id.img_smile);
        bottom_linear = findViewById(R.id.bottom_linear);
        messageInput = findViewById(R.id.input_message);
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (messageInput.getText().toString().equals("")) {
                    imgSmile.setImageResource(R.drawable.smile);
                    checkTypingStatus("noOne");
                } else {
                    imgSmile.setImageResource(R.drawable.send);
                    checkTypingStatus(messReceiverId);
                    imgSmile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            notify = true;
                            String messageText = messageInput.getText().toString().trim();
                            if (TextUtils.isEmpty(messageText)) {
                                Toast.makeText(ChatActivity.this, "Cannot send the empty message", Toast.LENGTH_SHORT).show();
                            } else {
                                sendMessage(messageText);
                            }
                            messageInput.setText("");
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        imgMore = findViewById(R.id.imgMore);
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotation);
        final Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.anim_rotation2);
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottom_linear.getVisibility() == View.GONE) {
                    bottom_linear.setVisibility(View.VISIBLE);
                    imgMore.startAnimation(animation);
                } else if (bottom_linear.getVisibility() == View.VISIBLE) {
                    bottom_linear.setVisibility(View.GONE);
                    imgMore.startAnimation(animation2);
                }
            }
        });

        readMessages();
        seenMessage();
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListerner = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Messages messages = ds.getValue(Messages.class);
                    if (messages.getTo().equals(messSenderId) && messages.getFrom().equals(messReceiverId)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        messagesList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");

        Query messageQuery = dbRef.limitToLast(mCurrentPage * TOTAL_ITEM_TO_LOAD);

        messageQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();


                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Messages messages = ds.getValue(Messages.class);
                    if (messages.getTo().equals(messSenderId) && messages.getFrom().equals(messReceiverId) ||
                            messages.getTo().equals(messReceiverId) && messages.getFrom().equals(messSenderId)) {

                        itemPos++;

                        if (itemPos == 1) {
                            String mMessageKey = dataSnapshot.getKey();

                            mLastKey = mMessageKey;
                            mPrevKey = mMessageKey;
                        }

                        messagesList.add(messages);
                    }
                    messageAdapter = new MessageAdapter(messagesList, ChatActivity.this, messReceiverImage);
                    messageAdapter.notifyDataSetChanged();
                    userMessageList.setAdapter(messageAdapter);
                    userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void loadMoreMessages(){
        messagesList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query messageQuery = dbRef.limitToLast(mCurrentPage*TOTAL_ITEM_TO_LOAD);

        messageQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Messages messages = ds.getValue(Messages.class);
                    if (messages.getTo().equals(messSenderId) && messages.getFrom().equals(messReceiverId) ||
                            messages.getTo().equals(messReceiverId) && messages.getFrom().equals(messSenderId)) {

                        String messageKey = snapshot.getKey();


                        if(!mPrevKey.equals(messageKey)){
                            messagesList.add(itemPos++,messages);

                        }
                        else{
                            mPrevKey = mLastKey;
                        }

                        if(itemPos == 1){
                            String mMessageKey = snapshot.getKey();
                            mLastKey = mMessageKey;
                        }
                        messagesList.add(messages);
                    }
                    messageAdapter = new MessageAdapter(messagesList, ChatActivity.this, messReceiverImage);
                    messageAdapter.notifyDataSetChanged();
                    userMessageList.setAdapter(messageAdapter);
                    userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void sendMessage(final String messageText) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("from", messSenderId);
        hashMap.put("message", messageText);
        hashMap.put("type", "text");
        hashMap.put("to", messReceiverId);
        hashMap.put("timeStamp", timestamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(hashMap);


        //create chatlist
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(messSenderId)
                .child(messReceiverId);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef1.child("id").setValue(messReceiverId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(messReceiverId)
                .child(messSenderId);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef2.child("id").setValue(messSenderId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // notification
        final String msg = messageText;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(messSenderId);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify) {
                    senNotification(messReceiverId, user.getName(), messageText);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // end notification
    }

    private void senNotification(final String messReceiverId, final String name, final String messageText) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(messReceiverId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(messSenderId, R.drawable.logo, name + " :" + messageText, "New Message", messReceiverId);
                    Sender sender = new Sender(data,token.getToken());

                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("JSON_RESPONSE", "onResponse: " + response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: " + error.toString());
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAALr5_rao:APA91bFqmtMv_eK5ARooDx7iGyBLPuZzMvhy1IiAm82G17gb-umttVleSgkIS4j67s6xTUVfLzUsoD2rabroAcLfvYv7DxNFn1HgeljgMjG2FzJTJ6hIOQEAqaqYa9PavKWfgw_7y4QD");

                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(messSenderId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(messSenderId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkOnlineStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListerner);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
