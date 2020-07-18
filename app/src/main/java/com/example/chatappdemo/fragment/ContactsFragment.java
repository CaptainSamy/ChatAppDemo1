package com.example.chatappdemo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.chatappdemo.R;
import com.example.chatappdemo.activity.ChatActivity;
import com.example.chatappdemo.activity.ProfileActivity;
import com.example.chatappdemo.activity.SearchFriendActivity;
import com.example.chatappdemo.activity.UpdateProfileUserActivity;
import com.example.chatappdemo.activity.ViewProfileUserActivity;
import com.example.chatappdemo.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {
    private View contactsView;
    private ShimmerRecyclerView recyclerViewContacts;
    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private String userImage = "default_image";
    SwipeRefreshLayout swipeRefreshLayout;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        recyclerViewContacts = contactsView.findViewById(R.id.recycler_contact_list);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout = contactsView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //swipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadContacts();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadContacts();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadContacts() {
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
                                    final String imgAnhBia = dataSnapshot.child("imgAnhBia").getValue().toString();
                                    final String gioiTinh = dataSnapshot.child("gioiTinh").getValue().toString();
                                    final String phone = dataSnapshot.child("phone").getValue().toString();
                                    final String onlineStatus = dataSnapshot.child("onlineStatus").getValue().toString();
                                    final String typingTo = dataSnapshot.child("typingTo").getValue().toString();
                                    final String uid = dataSnapshot.child("uid").getValue().toString();

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("uid", uid);
                                    hashMap.put("name", userName);
                                    hashMap.put("status", userStatus);
                                    hashMap.put("phone", phone);
                                    hashMap.put("gioiTinh", gioiTinh);
                                    hashMap.put("onlineStatus", onlineStatus);
                                    hashMap.put("typingTo", typingTo);
                                    hashMap.put("imgAnhBia", imgAnhBia);
                                    hashMap.put("imgAnhDD", userImage);

                                    DatabaseReference refContacts = FirebaseDatabase.getInstance().getReference("Contacts");
                                    refContacts.child(currentUserId).child(userIds).updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toasty.error(getActivity(), ""+ e.getMessage(), Toast.LENGTH_SHORT, true).show();
                                                }
                                            });


                                    contactsViewHolder.tv_username.setText(userName);
                                    contactsViewHolder.tv_status_item.setText(userStatus);

                                    contactsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", userIds);
                                            startActivity(chatIntent);
                                        }
                                    });

                                    contactsViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            String visit_userId = getRef(i).getKey();
                                            Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                                            profileIntent.putExtra("visit_userId", visit_userId);
                                            startActivity(profileIntent);
                                            return false;
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
        swipeRefreshLayout.setRefreshing(false);
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
