package com.example.chatappdemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatappdemo.R;
import com.example.chatappdemo.adapter.AdapterParticipantAdd;
import com.example.chatappdemo.model.Contact;
import com.example.chatappdemo.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class InforGroupActivity extends AppCompatActivity {
    private String groupId;
    private String myGroupRole = "";
    private ImageView groupIconIv;
    private CircleImageView backGroupInfor;
    private TextView groupTitleTv, groupRoleTv, descriptionTv, createByTv, editGroupTv, addParticipantTv, leaveGroupTv, participantsTv;
    private RecyclerView participantsRv;
    private ArrayList<Contact> contactList;
    private AdapterParticipantAdd adapterParticipantAdd;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infor_group);

        groupTitleTv = findViewById(R.id.groupTitleTv);
        groupRoleTv = findViewById(R.id.groupRoleTv);
        backGroupInfor = findViewById(R.id.backGroupInfor);
        groupIconIv = findViewById(R.id.groupIconIv);
        descriptionTv = findViewById(R.id.descriptionTv);
        createByTv = findViewById(R.id.createByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantsTv = findViewById(R.id.participantsTv);
        participantsRv = findViewById(R.id.participantsRv);

        groupId = getIntent().getStringExtra("groupId");
        firebaseAuth = FirebaseAuth.getInstance();

        backGroupInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loadGroupInfo();
        loadMyGroupRole();

        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InforGroupActivity.this, GroupParticipantAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InforGroupActivity.this, GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButtonTitle = "";
                if(myGroupRole.equals("Creator")) {
                    dialogTitle = "Delete Group";
                    dialogDescription = "Are you sure?";
                    positiveButtonTitle = "DELETE";
                }else {
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are you sure?";
                    positiveButtonTitle = "LEAVE";
                }
                new SweetAlertDialog(InforGroupActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(dialogTitle)
                        .setContentText(dialogDescription)
                        .setConfirmButton(positiveButtonTitle, new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                if (myGroupRole.equals("Creator")) {
                                    deleteGroup();
                                }else {
                                    leaveGroup();
                                }
                            }
                        })
                        .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .show();
            }
        });
    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(InforGroupActivity.this, "Group left successfully",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(InforGroupActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InforGroupActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(InforGroupActivity.this, "Group successfully deleted",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(InforGroupActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InforGroupActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createBy = ""+ds.child("createBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();

                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                    loadCreatorInfo(dateTime, createBy);
                    groupTitleTv.setText(groupTitle);
                    descriptionTv.setText(groupDescription);
                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.group_icon_bottom).into(groupIconIv);
                    }catch (Exception e){
                        groupIconIv.setImageResource(R.drawable.group_icon_bottom);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCreatorInfo(String dateTime, String createBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    createByTv.setText("Created by "+name +" on " + dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            groupRoleTv.setText(myGroupRole);
                            if (myGroupRole.equals("Participant")) {
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("Leave Group");
                            }else if (myGroupRole.equals("Admin")) {
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Leave Group");
                            }else if (myGroupRole.equals("Creator")) {
                                editGroupTv.setVisibility(View.VISIBLE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Delete Group");
                            }
                        }
                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadParticipants() {
        contactList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                contactList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    String uid = ""+ds.child("uid").getValue();
                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Contacts");
                    ref1.child(firebaseAuth.getCurrentUser().getUid()).orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds:snapshot.getChildren()){
                                Contact modelContact = ds.getValue(Contact.class);
                                contactList.add(modelContact);
                            }
                            adapterParticipantAdd = new AdapterParticipantAdd(InforGroupActivity.this, contactList, groupId, myGroupRole);
                            participantsRv.setAdapter(adapterParticipantAdd);
                            participantsTv.setText("Participants ("+contactList.size()+")");
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
}