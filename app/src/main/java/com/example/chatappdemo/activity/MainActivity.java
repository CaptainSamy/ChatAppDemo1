package com.example.chatappdemo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.chatappdemo.R;
import com.example.chatappdemo.fragment.ChatsFragment;
import com.example.chatappdemo.fragment.ContactsFragment;
import com.example.chatappdemo.fragment.GroupsFragment;
import com.example.chatappdemo.fragment.RequestFragment;
import com.example.chatappdemo.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private BottomNavigationView bottomNavigationView;
    private CircleImageView profile_image;
    private ImageButton btnSearch;
    private TextView txtTitle;
    private View notificationBadge;

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtTitle = findViewById(R.id.txt_Title);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //loadFragment(new ChatsFragment());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.frame_container, new ChatsFragment(), null);
        transaction.commit();

        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(itemSelectedListener);

        //Anh dai dien
        profile_image = findViewById(R.id.profile_image);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_imgUser = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent_imgUser);
            }
        });

        //Search
        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_search = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent_search);
            }
        });

        addBadgeView();
        //checkUserStatus();
        //phan notification
//        updateToken(String.valueOf(FirebaseInstanceId.getInstance().getToken()));
    }

    private void addBadgeView() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(0);

        notificationBadge = LayoutInflater.from(this).inflate(R.layout.custom_badge_layout, menuView, false);

        itemView.addView(notificationBadge);
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
                    refreshBadgeView();
                    return true;
                case R.id.navigation_contact:
                    fragment = new ContactsFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_group:
                    fragment = new GroupsFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_request:
                    fragment = new RequestFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    private void refreshBadgeView() {
        boolean badgeIsVisible = notificationBadge.getVisibility() != VISIBLE;
        notificationBadge.setVisibility(badgeIsVisible ? VISIBLE : GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void checkUserStatus() {
        if (currentUser == null) {
            Intent loginIntent = new Intent(MainActivity.this, Dangnhap_Dangky_Activity.class);
            startActivity(loginIntent);
        } else {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
            databaseReference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if ((dataSnapshot.child("name").exists())) {
//                        Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_LONG).show();
                        String userImage = dataSnapshot.child("imgAnhDD").getValue().toString();
                        String userName = dataSnapshot.child("name").getValue().toString();
                        txtTitle.setText(userName);

                        Picasso.get().load(userImage).into(profile_image);
                    } else {
                        Intent intent = new Intent(MainActivity.this, UpdateProfileUserActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //notification
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", currentUserId);
            editor.apply();
            //end notification
        }
    }

    // phan notification
    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(currentUserId).setValue(mToken);
    }
    // end notification
}
