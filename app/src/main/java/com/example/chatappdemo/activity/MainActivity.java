package com.example.chatappdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import cn.pedant.SweetAlert.SweetAlertDialog;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.fragment.ChatsFragment;
import com.example.chatappdemo.fragment.ContactsFragment;
import com.example.chatappdemo.fragment.GroupsFragment;
import com.example.chatappdemo.fragment.RequestFragment;
import com.example.chatappdemo.internet.MyApplication;
import com.example.chatappdemo.model.Messages;
import com.example.chatappdemo.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.badge.BadgeDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private static TextView internetStatus;
    private static int SPLASH_TIME_CONNECTED = 3000;
    private BottomNavigationView bottomNavigationView;
    private CircleImageView profile_image, user_on_off_chat;
    private ImageButton btnSearch, btnCreate;
    private TextView txtTitle;

    private FirebaseAuth firebaseAuth;
    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtTitle = findViewById(R.id.txt_Title);
        user_on_off_chat = findViewById(R.id.user_on_off_chat);
        internetStatus = findViewById(R.id.internet_status);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            changeTextStatus(true);
        } else {
            changeTextStatus(false);
        }
        firebaseAuth = FirebaseAuth.getInstance();

        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(itemSelectedListener);
        Fragment fragment = new ChatsFragment();
        loadFragment(fragment);

        //Anh dai dien
        profile_image = findViewById(R.id.profile_image);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_imgUser = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent_imgUser);
            }
        });

        //Create
        btnCreate = findViewById(R.id.btnCreate);

        checkUserStatus();
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    // Method to change the text status
    public void changeTextStatus(boolean isConnected) {
        // Change status according to boolean value
        if (isConnected) {
            internetStatus.setVisibility(View.VISIBLE);
            internetStatus.setText("Connected");
            internetStatus.setTextColor(Color.parseColor("#7ED321"));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    internetStatus.setVisibility(View.GONE);
                }
            }, SPLASH_TIME_CONNECTED);
        } else {
            internetStatus.setVisibility(View.VISIBLE);
            internetStatus.setText("Disconnected");
            internetStatus.setTextColor(Color.parseColor("#ff0000"));
        }
    }

    private void BadgeChats() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unread = 0;
                for (DataSnapshot ds: snapshot.getChildren()) {
                    int menuItemId = bottomNavigationView.getMenu().getItem(0).getItemId();
                    BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(menuItemId);
                    Messages mess = ds.getValue(Messages.class);
                    if (mess.getTo() != null && mess.getTo().equals(firebaseAuth.getCurrentUser().getUid()) && !mess.isSeen()){
                        unread++;
                    }
                    if (unread == 0) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.add(R.id.frame_container, new ChatsFragment(), null);
                        badgeDrawable.setVisible(false);

                    } else {
                        badgeDrawable.setVisible(true);
                        badgeDrawable.setNumber(unread);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener itemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment fragment;
            switch (menuItem.getItemId()) {
                case R.id.navigation_chat:
                    fragment = new ChatsFragment();
                    loadFragment(fragment);
                    btnCreate.setVisibility(GONE);
                    return true;
                case R.id.navigation_contact:
                    fragment = new ContactsFragment();
                    loadFragment(fragment);
                    btnCreate.setVisibility(View.VISIBLE);
                    btnCreate.setImageResource(R.drawable.add_friend);
                    btnCreate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(MainActivity.this, SearchFriendActivity.class));
                        }
                    });
                    return true;
                case R.id.navigation_group:
                    fragment = new GroupsFragment();
                    loadFragment(fragment);
                    btnCreate.setVisibility(View.VISIBLE);
                    btnCreate.setImageResource(R.drawable.create_group);
                    btnCreate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(MainActivity.this, GroupCreateActivity.class));
                        }
                    });
                    return true;
                case R.id.navigation_request:
                    fragment = new RequestFragment();
                    loadFragment(fragment);
                    btnCreate.setVisibility(GONE);
                    return true;
            }
            return false;
        }
    };

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        if (user == null) {
            Intent loginIntent = new Intent(MainActivity.this, Dangnhap_Dangky_Activity.class);
            startActivity(loginIntent);
        } else {
            mUID = user.getUid();
            databaseReference.child(mUID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if ((dataSnapshot.child("name").exists())) {
                        String userImage = dataSnapshot.child("imgAnhDD").getValue().toString();
                        String userName = dataSnapshot.child("name").getValue().toString();
                        txtTitle.setText(userName);
                        try {
                            Glide.with(MainActivity.this).load(userImage).placeholder(R.drawable.user_profile).into(profile_image);
                            //pDialog.dismiss();
                            user_on_off_chat.setVisibility(View.VISIBLE);
                        }catch (Exception e) {
                            profile_image.setImageResource(R.drawable.user_profile);
                            //pDialog.dismiss();
                            user_on_off_chat.setVisibility(View.VISIBLE);
                        }
                    } else {
                        //pDialog.dismiss();
                        Intent intent = new Intent(MainActivity.this, CreateUserActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //pDialog.dismiss();
                    Toasty.error(MainActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT, true).show();
                }
            });
            BadgeChats();
        }
    }

    // phan notification
    public void updateToken(String token) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
            Token mToken = new Token(token);
            ref.child(user.getUid()).setValue(mToken);

            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", user.getUid());
            editor.apply();
        }
    }
    // end notification

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.activityPaused();// On Pause notify the Application
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.activityResumed();// On Resume notify the Application
    }

}
