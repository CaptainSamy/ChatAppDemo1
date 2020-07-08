
package com.example.chatappdemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.example.chatappdemo.R;
import com.example.chatappdemo.adapter.AdapterParticipantAdd;
import com.example.chatappdemo.model.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupParticipantAddActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";

    private RecyclerView usersRv;
    private String groupId, myGroupRole;
    private CircleImageView groupIconIv;
    private TextView groupTitleTv;
    private ArrayList<Contact> contactList;
    private AdapterParticipantAdd adapterParticipantAdd;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);
        firebaseAuth = FirebaseAuth.getInstance();

        usersRv = findViewById(R.id.usersRv);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        groupIconIv = findViewById(R.id.groupIconIv);
        groupId = getIntent().getStringExtra("groupId");

        checkUserState();
    }

    private void checkUserState() {
        if (firebaseAuth.getCurrentUser() != null) {
            loadGroupInfor();
        }
    }

    private void getAllContacts() {
        contactList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseAuth.getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Contact modelContact = ds.getValue(Contact.class);
                    if (!firebaseAuth.getUid().equals(modelContact.getUid())) {
                        contactList.add(modelContact);
                    }
                }
                adapterParticipantAdd = new AdapterParticipantAdd(GroupParticipantAddActivity.this, contactList, ""+groupId, ""+myGroupRole);
                usersRv.setAdapter(adapterParticipantAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupInfor() {
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String createBy = ""+ds.child("createBy").getValue();

                    ref1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        myGroupRole = ""+snapshot.child("role").getValue();
                                        groupTitleTv.setText(groupTitle + "-" + myGroupRole);
                                        try {
                                            Picasso.get().load(groupIcon).placeholder(R.drawable.group_dark_mode).into(groupIconIv);
                                        }catch (Exception e){
                                            groupIconIv.setImageResource(R.drawable.group_dark_mode);
                                        }
                                        getAllContacts();
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
}