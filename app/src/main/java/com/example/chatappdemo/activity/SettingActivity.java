package com.example.chatappdemo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.internet.MyApplication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class SettingActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private ImageButton btn_Light, btn_Dark;
    private TextView txtTaiKhoanSetting, txtTaiKhoanProfile, user_Name, tv_Logout, tv_DelAccout, tv_Information;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private String currentUserID;
    private DatabaseReference databaseReference;
    private CircleImageView userProfileImage, btn_Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        btn_Back = findViewById(R.id.back_setting);
        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_Light = findViewById(R.id.btn_light);
        btn_Light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                themeIdcurrent = R.style.AppTheme;
                SharedPreferences locationpref = getApplicationContext()
                        .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor spedit = locationpref.edit();
                spedit.putInt("themeid", themeIdcurrent);
                spedit.apply();
                restartApp();
            }
        });

        btn_Dark = findViewById(R.id.btn_dark);
        btn_Dark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                themeIdcurrent = R.style.DarkTheme;
                SharedPreferences locationpref = getApplicationContext()
                        .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor spedit = locationpref.edit();
                spedit.putInt("themeid", themeIdcurrent);
                spedit.apply();
                restartApp();
            }
        });

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(SettingActivity.this, Dangnhap_Dangky_Activity.class));
                    finish();
                }
            }
        };

        userProfileImage =(CircleImageView) findViewById(R.id.profile_image);
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentNextSelect = new Intent(SettingActivity.this, ViewProfileUserActivity.class);
                startActivity(intentNextSelect);
            }
        });
        user_Name = findViewById(R.id.user_Name);

        txtTaiKhoanSetting = findViewById(R.id.txt_taikhoan_setting);
        txtTaiKhoanSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, UpdateAccoutActivity.class));
            }
        });
        txtTaiKhoanProfile = findViewById(R.id.txt_tt_taikhoan);
        txtTaiKhoanProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, UpdateProfileUserActivity.class));
            }
        });

        tv_Logout = findViewById(R.id.tv_Logout);
        tv_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
            }
        });

        tv_DelAccout = findViewById(R.id.tv_DelAccout);
        tv_DelAccout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    progressDialog.setTitle("Delete Accout");
                    progressDialog.setMessage("Please Wait!");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toasty.info(SettingActivity.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT, true).show();
                                        progressDialog.dismiss();
                                        startActivity(new Intent(SettingActivity.this, Dangnhap_Dangky_Activity.class));
                                        finish();
                                    } else {
                                        Toasty.error(SettingActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT, true).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                }
            }
        });

        tv_Information = findViewById(R.id.tv_Information);
        tv_Information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingActivity.this, InformationAppActivity.class));
            }
        });

        UserInfor();
    }
    public void restartApp() {
        Intent i = getBaseContext().getPackageManager().
                getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void UserInfor() {
        databaseReference.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (((dataSnapshot.hasChild("imgAnhDD")) && (dataSnapshot.hasChild("name"))))) {
                            String retrieveImage = dataSnapshot.child("imgAnhDD").getValue().toString();
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();

                            user_Name.setText(retrieveUserName);
                            try {
                                Glide.with(SettingActivity.this).load(retrieveImage).placeholder(R.drawable.user_profile).into(userProfileImage);
                            }catch (Exception e) {
                                userProfileImage.setImageResource(R.drawable.user_profile);
                            }
                        }
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            user_Name.setText(retrieveUserName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
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

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}