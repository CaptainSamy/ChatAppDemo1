package com.example.chatappdemo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.model.FindFriend;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class SearchFriendActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private CircleImageView imgBtnBack;
    private EditText txtSearch;
    private ImageView imgNoSearch;
    private RecyclerView recycler_find_friend;
    private DatabaseReference userRef;
    private String stringSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        imgBtnBack = findViewById(R.id.back_search);
        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgNoSearch = findViewById(R.id.img_no_search_result);
        imgNoSearch.setVisibility(View.VISIBLE);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        txtSearch = findViewById(R.id.txt_search);
        txtSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (txtSearch.getRight() - txtSearch.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        txtSearch.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtSearch.getText().toString().equals("")) {
                    Toasty.info(SearchFriendActivity.this, "Please write name to search!", Toast.LENGTH_SHORT, true).show();
                } else {
                    stringSearch = s.toString();
                    SearchFriend(stringSearch);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recycler_find_friend = findViewById(R.id.recycler_find_friend);
        recycler_find_friend.setLayoutManager(new LinearLayoutManager(this));
    }

    private void SearchFriend(String stringSearch) {
        FirebaseRecyclerOptions<FindFriend> options = null;
        if (stringSearch.equals("")) {
            options =
                    new FirebaseRecyclerOptions.Builder<FindFriend>()
                            .setQuery(userRef, FindFriend.class)
                            .build();
        } else {
            options =
                    new FirebaseRecyclerOptions.Builder<FindFriend>()
                            .setQuery(userRef.orderByChild("name").startAt(stringSearch).endAt(stringSearch + "\uf8ff"), FindFriend.class)
                            .build();
        }
        imgNoSearch.setVisibility(View.GONE);
        FirebaseRecyclerAdapter<FindFriend, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<FindFriend, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder findFriendViewHolder, final int i, @NonNull FindFriend findFriend) {
                        findFriendViewHolder.tv_username.setText(findFriend.getName());
                        Glide.with(SearchFriendActivity.this).load(findFriend.getImgAnhDD()).placeholder(R.drawable.user_profile).into(findFriendViewHolder.profileImage);

                        findFriendViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_userId = getRef(i).getKey();
                                Intent profileIntent = new Intent(SearchFriendActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_userId", visit_userId);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_search, parent, false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                        return viewHolder;
                    }
                };
        recycler_find_friend.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView tv_username;
        CircleImageView profileImage;
        CircleImageView img_On_Off;
        public FindFriendViewHolder(View itemView) {
            super(itemView);
            tv_username = itemView.findViewById(R.id.tv_user_name);
            profileImage = (CircleImageView) itemView.findViewById(R.id.user_profile);
            img_On_Off = (CircleImageView) itemView.findViewById(R.id.user_on_off);
        }
    }
}