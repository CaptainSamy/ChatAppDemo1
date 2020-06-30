package com.example.chatappdemo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappdemo.R;
import com.example.chatappdemo.activity.ChatActivity;
import com.example.chatappdemo.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {
    private View contactsView;
    private RecyclerView recyclerViewContacts;
    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private String userImage = "default_image";

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        recyclerViewContacts = contactsView.findViewById(R.id.recycler_contact_list);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(contactsRef, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull User user) {

                        final String userIds = getRef(i).getKey();

                        userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {


                                    if (dataSnapshot.hasChild("imgAnhDD")) {
                                        userImage = dataSnapshot.child("imgAnhDD").getValue().toString();
                                        Picasso.get().load(userImage)
                                                .placeholder(R.drawable.user_profile).into(contactsViewHolder.profileImage);
                                    }

                                    final String userName = dataSnapshot.child("name").getValue().toString();
                                    final String userStatus = dataSnapshot.child("status").getValue().toString();

                                    contactsViewHolder.tv_username.setText(userName);
                                    contactsViewHolder.tv_status_item.setText(userStatus);

//                                    if (dataSnapshot.child("userState").hasChild("state")){
//                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
//                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
//                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();
//                                        if (state.equals("online")){
//                                            contactsViewHolder.onlineIcon.setVisibility(View.VISIBLE);
//                                            contactsViewHolder.tv_status_item.setText("online");
//
//                                        }else if (state.equals("offline")){
//                                            contactsViewHolder.onlineIcon.setVisibility(View.INVISIBLE);
//                                            contactsViewHolder.tv_status_item.setText("Last seen: "  +time  +" "+ date);
//
//                                        }
//
//
//                                    }else {
//                                        contactsViewHolder.tv_status_item.setText("offline");
//                                        contactsViewHolder.onlineIcon.setVisibility(View.INVISIBLE);
//
//                                    }

                                    contactsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", userIds);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_chats, parent, false);
                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerViewContacts.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView tv_username, tv_status_item;
        CircleImageView profileImage,onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_username = itemView.findViewById(R.id.tv_username_chat);
            tv_status_item = itemView.findViewById(R.id.tv_status_chat);
            profileImage = itemView.findViewById(R.id.user_profile_chat);
            onlineIcon= itemView.findViewById(R.id.user_on_off_chat);
        }
    }
}
