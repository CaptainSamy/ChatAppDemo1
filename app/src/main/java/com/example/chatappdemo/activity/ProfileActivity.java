package com.example.chatappdemo.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.internet.MyApplication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private String receiverUserID, curent_state, sender_userId;
    private CircleImageView userProfileImage;
    private ImageButton userProfileImageBG;
    private TextView userProfileName, userProfilePhone, userProfileStatus, userProfileSex, tv_Back;
    private MaterialButton btnSendMess, btnDeclineRequest;
    private DatabaseReference userRef, requestRef, contactRef, notificationRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("visit_userId").toString();
        sender_userId = firebaseAuth.getCurrentUser().getUid();
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileImageBG = findViewById(R.id.imgBtnBG);
        userProfileName = findViewById(R.id.visit_username);
        userProfilePhone = findViewById(R.id.visit_phone);
        userProfileStatus = findViewById(R.id.visit_status);
        userProfileSex = findViewById(R.id.visit_userGioitinh);
        tv_Back = findViewById(R.id.tv_Back);
        tv_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSendMess = findViewById(R.id.btnSend_Mess);
        btnDeclineRequest = findViewById(R.id.btnDecline);
        curent_state = "new";

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && ((dataSnapshot.hasChild("imgAnhDD"))
                        && (dataSnapshot.hasChild("imgAnhBia"))
                        && (dataSnapshot.hasChild("name"))
                        && (dataSnapshot.hasChild("status"))
                        && (dataSnapshot.hasChild("gioiTinh"))
                        && (dataSnapshot.hasChild("phone")))) {
                    String userImage = dataSnapshot.child("imgAnhDD").getValue().toString();
                    String userImageBG = dataSnapshot.child("imgAnhBia").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userPhone = dataSnapshot.child("phone").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String userSex = dataSnapshot.child("gioiTinh").getValue().toString();

                    try {
                        Glide.with(ProfileActivity.this).load(userImage).placeholder(R.drawable.user_profile).into(userProfileImage);
                        Glide.with(ProfileActivity.this).load(userImageBG).placeholder(R.drawable.teabackground).into(userProfileImageBG);
                    }catch (Exception e) {

                    }
                    userProfileName.setText(userName);
                    userProfilePhone.setText(userPhone);
                    userProfileStatus.setText(userStatus);
                    if (userSex.equals("male")){
                        userProfileSex.setText("Nam");
                    }else {
                        userProfileSex.setText("Nữ");
                    }
                    ManageRequests();
                }
                String userName = dataSnapshot.child("name").getValue().toString();
                String userPhone = dataSnapshot.child("phone").getValue().toString();
                String userStatus = dataSnapshot.child("status").getValue().toString();
                String userSex = dataSnapshot.child("gioiTinh").getValue().toString();

                userProfileName.setText(userName);
                userProfilePhone.setText(userPhone);
                userProfileStatus.setText(userStatus);
                if (userSex.equals("male")){
                    userProfileSex.setText("Nam");
                }else {
                    userProfileSex.setText("Nữ");
                }

                ManageRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageRequests() {
        requestRef.child(sender_userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID)) {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                            if (request_type.equals("sent")) {
                                curent_state = "request_sent";
                                btnSendMess.setText("Cancel request");
                            } else if (request_type.equals("received")) {
                                curent_state = "request_received";
                                btnSendMess.setText("Accept Request");
                                btnDeclineRequest.setVisibility(View.VISIBLE);
                                btnDeclineRequest.setEnabled(true);
                                btnDeclineRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelRequest();
                                    }
                                });
                            }
                        } else {
                            contactRef.child(sender_userId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID)) {
                                                curent_state = "friends";
                                                btnSendMess.setText("Remove this contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        if (!sender_userId.equals(receiverUserID)) {
            btnSendMess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSendMess.setEnabled(false);
                    if (curent_state.equals("new")) {
                        SendRequest();
                    }
                    if (curent_state.equals("request_sent")) {
                        CancelRequest();
                    }
                    if (curent_state.equals("request_received")) {
                        AcceptRequest();
                    }
                    if (curent_state.equals("friends")) {
                        RemoveSpecificContact();
                    }
                }
            });
        } else {
            btnSendMess.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        contactRef.child(sender_userId).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(receiverUserID).child(sender_userId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btnSendMess.setEnabled(true);
                                                curent_state = "new";
                                                btnSendMess.setText("Send Request");

                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptRequest() {
        contactRef.child(sender_userId).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(receiverUserID).child(sender_userId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                requestRef.child(sender_userId).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    requestRef.child(receiverUserID).child(sender_userId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    btnSendMess.setEnabled(true);
                                                                                    curent_state = "friends";
                                                                                    btnSendMess.setText("Remove this contact");

                                                                                    btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                                                    btnDeclineRequest.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelRequest() {
        requestRef.child(sender_userId).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            requestRef.child(receiverUserID).child(sender_userId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btnSendMess.setEnabled(true);
                                                curent_state = "new";
                                                btnSendMess.setText("Send Request");

                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendRequest() {
        requestRef.child(sender_userId).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            requestRef.child(receiverUserID).child(sender_userId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", sender_userId);
                                                chatNotificationMap.put("type", "request");

                                                notificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    btnSendMess.setEnabled(true);
                                                                    curent_state = "request_sent";
                                                                    btnSendMess.setText("Cancel request");
                                                                }
                                                            }
                                                        });


                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void checkOnlineStatus(String status){
        try {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("onlineStatus", status);
            dbRef.updateChildren(hashMap);
        }catch (Exception e){

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.activityResumed();
        checkOnlineStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.activityPaused();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
    }
}