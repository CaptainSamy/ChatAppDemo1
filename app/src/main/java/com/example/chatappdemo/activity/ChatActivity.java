package com.example.chatappdemo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.adapter.AdapterMessage;
import com.example.chatappdemo.fragment.EmoticonGIFKeyboardFragment;
import com.example.chatappdemo.gifs.Gif;
import com.example.chatappdemo.gifs.GifSelectListener;
import com.example.chatappdemo.giphy.GiphyGifProvider;
import com.example.chatappdemo.internet.MyApplication;
import com.example.chatappdemo.model.Messages;
import com.example.chatappdemo.model.User;
import com.example.chatappdemo.notifications.Data;
import com.example.chatappdemo.notifications.Sender;
import com.example.chatappdemo.notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import gun0912.tedbottompicker.TedBottomPicker;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;

public class ChatActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";

    private static TextView internet_status_on, internet_status_off;
    private static int SPLASH_TIME_CONNECTED = 3000;
    private String messReceiverId, messReceiverImage, messReceiverName, messSenderId;
    private CircleImageView imgMore, imgGif, imgProfileFriend, back_user_chat, imgSmile, onlineStatusIv, startbtn, stopbtn;
    private LinearLayout bottom_linear, ln_chatInput, sendImage, sendFile, sendAudio, sendLocation, liner_record;
    private TextView name_user_chat, userLastSeen, mRecordLabel, tv_Block_UnBlock, tv_View_Block_Unblock;
    private EmojiconEditText messageInput;
    private CardView cv_blockUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference RootRef;
    private ValueEventListener seenListerner;
    private DatabaseReference userRefForSeen;

    private List<Messages> messagesList;
    private RecyclerView userMessageList;
    private AdapterMessage adapterMessage;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static final int TOTAL_ITEM_TO_LOAD = 12;
    private int mCurrentPage = 1;

    //Solution for descending list on refresh
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    private RequestQueue requestQueue;
    private boolean notify = false;
    private boolean isBlocked = false;

    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int FILE_PICK_CODE = 3000;
    private String[] storagePermission;

    private Uri myUri = null;
    private List<Uri> selectedUriList;
    private int upload_count = 0;

    private View rootView;
    private EmojIconActions emojIconActions;
    private EmoticonGIFKeyboardFragment mEmoticonGIFKeyboardFragment;
    private FrameLayout keyboard_container;

    private MediaRecorder mRecorder;
    private Chronometer chronometer;
    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 111;

    public static void toggleKeyboardVisibility(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null)
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid", R.style.AppTheme);
        setTheme(themeIdcurrent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        internet_status_on = findViewById(R.id.internet_status_on);
        internet_status_off = findViewById(R.id.internet_status_off);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            changeTextStatus(true);
        } else {
            changeTextStatus(false);
        }

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        back_user_chat = findViewById(R.id.back_user_chat);
        back_user_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_Block_UnBlock = findViewById(R.id.tv_Block_UnBlock);
        cv_blockUser = findViewById(R.id.cv_blockUser);
        tv_View_Block_Unblock = findViewById(R.id.tv_View_Block_Unblock);
        tv_Block_UnBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBlocked){
                    unBlockUser();
                }else {
                    blockUser();
                }
            }
        });

        imgProfileFriend = findViewById(R.id.image_user_chat);
        name_user_chat = findViewById(R.id.name_user_chat);
        sendImage = findViewById(R.id.sendImage);
        sendFile = findViewById(R.id.sendFile);
        onlineStatusIv = findViewById(R.id.onlineStatusIv);
        userLastSeen = findViewById(R.id.user_last_seen);
        userMessageList = (RecyclerView) findViewById(R.id.messager_list_of_users);
        ln_chatInput = findViewById(R.id.ln_chatInput);

        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itemPos = 0;
                mCurrentPage++;
                loadMoreMessages();

            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference("Users");
        messSenderId = firebaseAuth.getCurrentUser().getUid();
        messReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        Query query = RootRef.orderByChild("uid").equalTo(messReceiverId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    messReceiverName = ds.child("name").getValue().toString();
                    messReceiverImage = ds.child("imgAnhDD").getValue().toString();
                    String typingStatus = ds.child("typingTo").getValue().toString();
                    if (typingStatus.equals(messSenderId)) {
                        userLastSeen.setText("typing...");
                    } else {
                        String onlineStatus = ds.child("onlineStatus").getValue().toString();
                        if (onlineStatus.equals("online")) {
                            userLastSeen.setText(onlineStatus);
                            onlineStatusIv.setVisibility(View.VISIBLE);
                        } else {
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            userLastSeen.setText("Last seen: " + dateTime);
                            onlineStatusIv.setVisibility(View.INVISIBLE);
                        }
                    }

                    name_user_chat.setText(messReceiverName);
                    try {
                        Glide.with(ChatActivity.this).load(messReceiverImage).placeholder(R.drawable.user_profile).into(imgProfileFriend);
                    } catch (Exception e) {
                        imgProfileFriend.setImageResource(R.drawable.user_profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        imgMore = findViewById(R.id.imgMore);
        imgSmile = findViewById(R.id.img_smile);
        bottom_linear = findViewById(R.id.bottom_linear);
        messageInput = findViewById(R.id.input_message);
        final Animation animation = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.anim_rotation);
        final Animation animation2 = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.anim_rotation2);
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottom_linear.getVisibility() == View.GONE) {
                    bottom_linear.setVisibility(View.VISIBLE);
                    imgMore.startAnimation(animation);
                } else if (bottom_linear.getVisibility() == View.VISIBLE) {
                    bottom_linear.setVisibility(View.GONE);
                    liner_record.setVisibility(View.GONE);
                    imgMore.startAnimation(animation2);
                }
            }
        });

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (messageInput.getText().toString().equals("")) {
                    imgMore.setImageResource(R.drawable.add);
                    final Animation animation = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.anim_rotation);
                    final Animation animation2 = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.anim_rotation2);
                    imgMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (bottom_linear.getVisibility() == View.GONE) {
                                bottom_linear.setVisibility(View.VISIBLE);
                                imgMore.startAnimation(animation);
                            } else if (bottom_linear.getVisibility() == View.VISIBLE) {
                                bottom_linear.setVisibility(View.GONE);
                                liner_record.setVisibility(View.GONE);
                                imgMore.startAnimation(animation2);
                            }
                        }
                    });
                    checkTypingStatus("noOne");
                } else {
                    imgMore.setImageResource(R.drawable.send);
                    checkTypingStatus(messReceiverId);
                    imgMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            notify = true;
                            String messageText = messageInput.getText().toString().trim();
                            if (TextUtils.isEmpty(messageText)) {
                                Toasty.error(ChatActivity.this, "Cannot send the empty message", Toast.LENGTH_SHORT, true).show();
                            } else {
                                sendMessage(messageText);
                            }
                            messageInput.setText("");
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        rootView = findViewById(R.id.root_view);
        emojIconActions = new EmojIconActions(this, rootView, messageInput, imgSmile, "#00bdb5","#e8e8e8","#f4f4f4");
        emojIconActions.ShowEmojIcon();
        emojIconActions.setIconsIds(R.drawable.ic_keyboad, R.drawable.smile);
        emojIconActions.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
            }

            @Override
            public void onKeyboardClose() {
            }
        });

        imgGif = findViewById(R.id.imgGif);
        EmoticonGIFKeyboardFragment.GIFConfig gifConfig = new EmoticonGIFKeyboardFragment
                .GIFConfig(GiphyGifProvider.create(this, "564ce7370bf347f2b7c0e4746593c179"))

                .setGifSelectListener(new GifSelectListener() {
                    @Override
                    public void onGifSelected(@NonNull Gif gif) {
                        //Do something with the selected GIF.
                        String urlGif = gif.getGifUrl();
                        sendImageGifMessage(urlGif);
                    }

                    @Override
                    public void onBackSpace() {
                    }
                });

        keyboard_container = findViewById(R.id.keyboard_container);
        imgGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (keyboard_container.getVisibility() == View.GONE) {
                    keyboard_container.setVisibility(View.VISIBLE);
                    mEmoticonGIFKeyboardFragment = EmoticonGIFKeyboardFragment
                            .getNewInstance(findViewById(R.id.keyboard_container), gifConfig);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.keyboard_container, mEmoticonGIFKeyboardFragment)
                            .commit();
                    mEmoticonGIFKeyboardFragment.open();
                }else if (keyboard_container.getVisibility() == View.VISIBLE) {
                    keyboard_container.setVisibility(View.GONE);
                    mEmoticonGIFKeyboardFragment.toggle();
                    toggleKeyboardVisibility(ChatActivity.this);
                }
            }
        });

        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionListener permissionlistener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {

                        TedBottomPicker.with(ChatActivity.this)
                                .setPeekHeight(1600)
                                .showTitle(false)
                                .setCompleteButtonText("Done")
                                .setEmptySelectionText("No Select")
                                .setSelectedUriList(selectedUriList)
                                .showMultiImage(uriList -> {
                                    selectedUriList = uriList;
                                        sendSingle_MultiImageMessage(uriList);
                                });
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(ChatActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    }
                };

                checkPermission(permissionlistener);
            }
        });

        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFile();
                }
            }
        });

        sendAudio = findViewById(R.id.sendAudio);
        liner_record = findViewById(R.id.liner_record);
        sendAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (liner_record.getVisibility() == View.GONE){
                    liner_record.setVisibility(View.VISIBLE);
                } else if (liner_record.getVisibility() == View.VISIBLE) {
                    liner_record.setVisibility(View.GONE);
                }
            }
        });
        mRecordLabel = findViewById(R.id.mRecordLabel);
        startbtn = findViewById(R.id.btnRecord);
        stopbtn = findViewById(R.id.btnStop);
        chronometer = (Chronometer) findViewById(R.id.chronometerTimer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/AudioRecording.3gp";
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckPermissionsAudio()) {
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    stopbtn.setVisibility(View.VISIBLE);
                    startbtn.setVisibility(View.GONE);
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mRecorder.setOutputFile(mFileName);
                    try {
                        mRecorder.prepare();
                    } catch (IOException e) {
                    }
                    mRecorder.start();
                    mRecordLabel.setText("Recording Started");
                }
                else
                {
                    RequestPermissionsAudio();
                }
            }
        });
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                stopbtn.setVisibility(View.GONE);
                startbtn.setVisibility(View.VISIBLE);
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                mRecordLabel.setText("Recording Stopped");
                Uri uriAudio = Uri.fromFile(new File(mFileName));
                new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Send file recording?")
                        .setConfirmText("Sent")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sendFileAudio(uriAudio);
                                sDialog.dismissWithAnimation();
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

        checkIsBlocked();
        readMessages();
        seenMessage();
    }

    private void checkHisBlocked() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Contacts");
        ref.child(messReceiverId).child(messSenderId).child("BlockedUsers").orderByChild("uid").equalTo(messSenderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                ln_chatInput.setVisibility(View.GONE);
                                cv_blockUser.setVisibility(View.VISIBLE);
                                tv_View_Block_Unblock.setText(messReceiverName + " has blocked you");
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void unBlockUser() {
        new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Unblock " + messReceiverName + "?")
                .setConfirmText("Unblock!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Contacts");
                        ref.child(messSenderId).child(messReceiverId).child("BlockedUsers").orderByChild("uid").equalTo(messReceiverId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds: snapshot.getChildren()) {
                                            if (ds.exists()){
                                                ds.getRef().removeValue()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                tv_Block_UnBlock.setText("Block");
                                                                ln_chatInput.setVisibility(View.VISIBLE);
                                                                cv_blockUser.setVisibility(View.GONE);
                                                                Toasty.success(ChatActivity.this, "Unblocked Successfully!", Toast.LENGTH_SHORT, true).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toasty.error(ChatActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT, true).show();
                                                            }
                                                        });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        sDialog.dismissWithAnimation();
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

    private void blockUser() {
        new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Block " + messReceiverName + "?")
                .setContentText("You will no longer receive messages from this person!")
                .setConfirmText("Block")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("uid",messReceiverId);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Contacts");
                        ref.child(messSenderId).child(messReceiverId).child("BlockedUsers").child(messReceiverId).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        tv_Block_UnBlock.setText("Unblock");
                                        ln_chatInput.setVisibility(View.GONE);
                                        cv_blockUser.setVisibility(View.VISIBLE);
                                        tv_View_Block_Unblock.setText("You blocked " + messReceiverName);
                                        Toasty.success(ChatActivity.this, "Blocked Successfully!", Toast.LENGTH_SHORT, true).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toasty.error(ChatActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT, true).show();
                                    }
                                });
                        sDialog.dismissWithAnimation();
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

    private void checkIsBlocked() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Contacts");
        ref.child(messSenderId).child(messReceiverId).child("BlockedUsers").orderByChild("uid").equalTo(messReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                ln_chatInput.setVisibility(View.GONE);
                                cv_blockUser.setVisibility(View.VISIBLE);
                                tv_Block_UnBlock.setText("Unblock");
                                tv_View_Block_Unblock.setText("You blocked " + messReceiverName);
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toasty.success(getApplicationContext(), "Permission Granted!", Toast.LENGTH_SHORT, true).show();
                    } else {
                        Toasty.error(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT, true).show();
                    }
                }
                break;
        }
    }

    private void RequestPermissionsAudio() {
        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    private boolean CheckPermissionsAudio() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }


    // Method to change the text status
    public void changeTextStatus(boolean isConnected) {
        // Change status according to boolean value
        if (isConnected) {
            internet_status_on.setVisibility(View.VISIBLE);
            internet_status_off.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    internet_status_on.setVisibility(View.GONE);
                }
            }, SPLASH_TIME_CONNECTED);
        } else {
            internet_status_off.setVisibility(View.VISIBLE);
            internet_status_on.setVisibility(GONE);
        }
    }

    private void sendSingle_MultiImageMessage(List<Uri> uriList) {
        notify = true;
        SweetAlertDialog pDialog = new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2cb9b0"));
        pDialog.setTitleText("Sending " + uriList.size() + " Image...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filenamePath = "ChatImages/" + "" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filenamePath);
        for (upload_count = 0; upload_count < uriList.size(); upload_count++){
            myUri = uriList.get(upload_count);
            StorageReference childImg = storageReference.child(myUri.getLastPathSegment());
            childImg.putFile(myUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                String timestamp = "" + System.currentTimeMillis();
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("from", messSenderId);
                                hashMap.put("message", downloadUri);
                                hashMap.put("type", "image");
                                hashMap.put("to", messReceiverId);
                                hashMap.put("timeStamp", timestamp);
                                hashMap.put("isSeen", false);
                                databaseReference.child("Chats").push().setValue(hashMap);

                                //create chatlist
                                DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                        .child(messSenderId)
                                        .child(messReceiverId);
                                chatRef1.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (!snapshot.exists()) {
                                            chatRef1.child("id").setValue(messReceiverId);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                        .child(messReceiverId)
                                        .child(messSenderId);
                                chatRef2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (!snapshot.exists()) {
                                            chatRef2.child("id").setValue(messSenderId);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                //notification
                                DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(messSenderId);
                                database.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User user = snapshot.getValue(User.class);
                                        if (notify) {
                                            senNotification(messReceiverId, user.getName(), "Sent you a photo");
                                        }
                                        notify = false;
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            pDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pDialog.dismiss();
                            Toasty.error(ChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                        }
                    });
        }
    }

    private void sendImageGifMessage(String urlGif) {
        notify = true;

        String timestamp = "" + System.currentTimeMillis();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("from", messSenderId);
        hashMap.put("message", urlGif);
        hashMap.put("type", "image_gif");
        hashMap.put("to", messReceiverId);
        hashMap.put("timeStamp", timestamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(hashMap);

        //create chatlist
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(messSenderId)
                .child(messReceiverId);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef1.child("id").setValue(messReceiverId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(messReceiverId)
                .child(messSenderId);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef2.child("id").setValue(messSenderId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //notification
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(messSenderId);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify) {
                    senNotification(messReceiverId, user.getName(), "Sent you a gif");
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mEmoticonGIFKeyboardFragment == null || !mEmoticonGIFKeyboardFragment.handleBackPressed())
            super.onBackPressed();
    }

    private void pickFile() {
        Intent intentFile = new Intent();
        intentFile.setType("application/*");
        intentFile.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentFile, "Select File"), FILE_PICK_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == FILE_PICK_CODE) {
                myUri = data.getData();
                sendFileMessage(myUri);
            }
        }
    }

    private void sendFileMessage(Uri fileUri) {
        notify = true;

        SweetAlertDialog pDialog = new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2cb9b0"));
        pDialog.setTitleText("Sending File...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filenamePath = "ChatFiles/" + "" + fileUri.getLastPathSegment();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filenamePath);
        storageReference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            String timestamp = "" + System.currentTimeMillis();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("from", messSenderId);
                            hashMap.put("message", downloadUri);
                            hashMap.put("type", "file");
                            hashMap.put("to", messReceiverId);
                            hashMap.put("timeStamp", timestamp);
                            hashMap.put("nameFile", ""+fileUri.getLastPathSegment());
                            hashMap.put("isSeen", false);
                            databaseReference.child("Chats").push().setValue(hashMap);

                            //create chatlist
                            DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(messSenderId)
                                    .child(messReceiverId);
                            chatRef1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        chatRef1.child("id").setValue(messReceiverId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(messReceiverId)
                                    .child(messSenderId);
                            chatRef2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        chatRef2.child("id").setValue(messSenderId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //notification
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(messSenderId);
                            database.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);
                                    if (notify) {
                                        senNotification(messReceiverId, user.getName(), "Sent you a file");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        pDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pDialog.dismiss();
                        Toasty.error(ChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        pDialog.setTitleText("Uploaded: " + (int) progress + "%");
                    }
                });

    }

    private void sendFileAudio(Uri uriAudio) {
        notify = true;

        SweetAlertDialog pDialog = new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2cb9b0"));
        pDialog.setTitleText("Sending Voice...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filenamePath = "ChatFiles/" + "" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filenamePath);
        storageReference.putFile(uriAudio)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            String timestamp = "" + System.currentTimeMillis();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("from", messSenderId);
                            hashMap.put("message", downloadUri);
                            hashMap.put("type", "audio");
                            hashMap.put("to", messReceiverId);
                            hashMap.put("timeStamp", timestamp);
                            hashMap.put("nameFile", ""+uriAudio.getLastPathSegment());
                            hashMap.put("isSeen", false);
                            databaseReference.child("Chats").push().setValue(hashMap);

                            //create chatlist
                            DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(messSenderId)
                                    .child(messReceiverId);
                            chatRef1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        chatRef1.child("id").setValue(messReceiverId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(messReceiverId)
                                    .child(messSenderId);
                            chatRef2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        chatRef2.child("id").setValue(messSenderId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //notification
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(messSenderId);
                            database.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);
                                    if (notify) {
                                        senNotification(messReceiverId, user.getName(), "Sent you a voice.");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        pDialog.dismiss();
                        mRecordLabel.setText("Uploading Finished");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pDialog.dismiss();
                        Toasty.error(ChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                    }
                });
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListerner = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Messages messages = ds.getValue(Messages.class);
                    if (messages.getTo().equals(messSenderId) && messages.getFrom().equals(messReceiverId)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        messagesList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query messageQuery = dbRef.limitToLast(mCurrentPage * TOTAL_ITEM_TO_LOAD);
        messageQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Messages messages = ds.getValue(Messages.class);
                    if (messages.getTo().equals(messSenderId) && messages.getFrom().equals(messReceiverId) ||
                            messages.getTo().equals(messReceiverId) && messages.getFrom().equals(messSenderId)) {

                        itemPos++;

                        if (itemPos == 1) {
                            String mMessageKey = dataSnapshot.getKey();

                            mLastKey = mMessageKey;
                            mPrevKey = mMessageKey;
                        }

                        messagesList.add(messages);
                    }
                    adapterMessage = new AdapterMessage(messagesList, ChatActivity.this, messReceiverImage);
                    adapterMessage.notifyDataSetChanged();
                    userMessageList.setAdapter(adapterMessage);
                    userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void loadMoreMessages() {
        messagesList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query messageQuery = dbRef.limitToLast(mCurrentPage * TOTAL_ITEM_TO_LOAD);

        messageQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Messages messages = ds.getValue(Messages.class);
                    if (messages.getTo().equals(messSenderId) && messages.getFrom().equals(messReceiverId) ||
                            messages.getTo().equals(messReceiverId) && messages.getFrom().equals(messSenderId)) {

                        String messageKey = snapshot.getKey();


                        if (!mPrevKey.equals(messageKey)) {
                            messagesList.add(itemPos++, messages);

                        } else {
                            mPrevKey = mLastKey;
                        }

                        if (itemPos == 1) {
                            String mMessageKey = snapshot.getKey();
                            mLastKey = mMessageKey;
                        }
                        messagesList.add(messages);
                    }
                    adapterMessage = new AdapterMessage(messagesList, ChatActivity.this, messReceiverImage);
                    adapterMessage.notifyDataSetChanged();
                    userMessageList.setAdapter(adapterMessage);
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void sendMessage(final String messageText) {
        notify = true;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("from", messSenderId);
        hashMap.put("message", messageText);
        hashMap.put("type", "text");
        hashMap.put("to", messReceiverId);
        hashMap.put("timeStamp", timestamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(hashMap);


        //create chatlist
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(messSenderId)
                .child(messReceiverId);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef1.child("id").setValue(messReceiverId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(messReceiverId)
                .child(messSenderId);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef2.child("id").setValue(messSenderId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // notification
        final String msg = messageText;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(messSenderId);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify) {
                    senNotification(messReceiverId, user.getName(), messageText);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // end notification
    }

    private void senNotification(final String messReceiverId, final String name, final String messageText) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(messReceiverId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(messSenderId, R.drawable.logo, name + " :" + messageText, "New Message", messReceiverId);
                    Sender sender = new Sender(data, token.getToken());

                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("JSON_RESPONSE", "onResponse: " + response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: " + error.toString());
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAALr5_rao:APA91bFqmtMv_eK5ARooDx7iGyBLPuZzMvhy1IiAm82G17gb-umttVleSgkIS4j67s6xTUVfLzUsoD2rabroAcLfvYv7DxNFn1HgeljgMjG2FzJTJ6hIOQEAqaqYa9PavKWfgw_7y4QD");

                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(messSenderId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(messSenderId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        dbRef.updateChildren(hashMap);
    }

    private void checkPermission(PermissionListener permissionlistener) {
        TedPermission.with(ChatActivity.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkHisBlocked();
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
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListerner);
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
    }
}
