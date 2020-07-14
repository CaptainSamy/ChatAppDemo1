package com.example.chatappdemo.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.chatappdemo.R;
import com.example.chatappdemo.activity.MainActivity;
import com.example.chatappdemo.adapter.AdapterChatlist;
import com.example.chatappdemo.helper.MyButtonClickListener;
import com.example.chatappdemo.helper.MySwipeHelper;
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
    AdapterChatlist adapterChatlist;
    String mUID;
    SwipeRefreshLayout swipeRefreshLayout;

    public ChatsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);
        chatlistList = new ArrayList<>();
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //swipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadChats();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        MySwipeHelper swipeHelper = new MySwipeHelper(getActivity(), recyclerView, 200) {

            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(getActivity(),
                        0,
                        "Delete", 35,
                        Color.parseColor("#FF3C30"),
                        new MyButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                Toast.makeText(getActivity(), "Delete click", Toast.LENGTH_LONG).show();
                            }
                        }));
            }
        };

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
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadChats() {
        swipeRefreshLayout.setRefreshing(true);
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
                    adapterChatlist = new AdapterChatlist(getContext(), userList);
                    recyclerView.setAdapter(adapterChatlist);
                    //set last message
                    for (int i=0; i<userList.size(); i++) {
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(),""+error.getMessage(),Toast.LENGTH_SHORT).show();
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
                String seen = "default";
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

                    if (messages.getTo().equals(mUID) && messages.getFrom().equals(userId)){
                        if (messages.getType().equals("image")) {
                            theLastMessage = "Sent a photo";
                        }else if (messages.getType().equals("file")) {
                            theLastMessage = "Sent a file";
                        }else {
                            theLastMessage = messages.getMessage();
                        }
                        seen = ""+messages.isSeen();
                    } else if (messages.getTo().equals(userId) && messages.getFrom().equals(mUID)){
                        if (messages.getType().equals("image")) {
                            theLastMessage = "You sent a photo";
                        }else if (messages.getType().equals("file")) {
                            theLastMessage = "You sent a file";
                        }else {
                            theLastMessage = "You: " + messages.getMessage();
                        }
                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.setSeenMessageMap(userId, seen);
                adapterChatlist.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
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
