package com.example.chatappdemo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappdemo.R;
import com.example.chatappdemo.activity.MainActivity;
import com.example.chatappdemo.adapter.ChatlistAdapter;
import com.example.chatappdemo.model.Chatlist;
import com.example.chatappdemo.model.Messages;
import com.example.chatappdemo.model.User;
import com.example.chatappdemo.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;



/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<Chatlist> chatlistList;
    List<User> userList;
    ChatlistAdapter chatlistAdapter;
    String mUID;

    public ChatsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);
        chatlistList = new ArrayList<>();

        updateToken(FirebaseInstanceId.getInstance().getToken());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser fUser = firebaseAuth.getCurrentUser();
        if (fUser != null){
            mUID = fUser.getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(mUID);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatlistList.clear();
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Chatlist chatlist = ds.getValue(Chatlist.class);
                        chatlistList.add(chatlist);
                    }
                    loadChats();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else{
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }

    }

    private void loadChats() {
        userList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    for (Chatlist chatlist: chatlistList) {
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())) {
                            userList.add(user);
                            break;
                        }
                    }
                    chatlistAdapter = new ChatlistAdapter(getContext(), userList);
                    recyclerView.setAdapter(chatlistAdapter);
                    //set last message
                    for (int i=0; i<userList.size(); i++) {
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {
        FirebaseUser fUser = firebaseAuth.getCurrentUser();
        mUID = fUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Messages messages = ds.getValue(Messages.class);
                    if (messages == null) {
                        continue;
                    }
                    String sender = messages.getFrom();
                    String receiver = messages.getTo();
                    if (sender == null || receiver == null) {
                        continue;
                    }
                    if (messages.getTo().equals(mUID) && messages.getFrom().equals(userId) ||
                            messages.getTo().equals(userId) && messages.getFrom().equals(mUID)) {
                        theLastMessage = messages.getMessage();
                    }
                }
                chatlistAdapter.setLastMessageMap(userId, theLastMessage);
                chatlistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateToken(String token) {
        FirebaseUser fUser = firebaseAuth.getCurrentUser();
        if (fUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
            Token token1 = new Token(token);
            reference.child(fUser.getUid()).setValue(token1);
        }
    }
}
