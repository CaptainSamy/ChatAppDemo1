package com.example.chatappdemo.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.adapter.AdapterGroupChat;
import com.example.chatappdemo.fragment.EmoticonGIFKeyboardFragment;
import com.example.chatappdemo.gifs.Gif;
import com.example.chatappdemo.gifs.GifSelectListener;
import com.example.chatappdemo.giphy.GiphyGifProvider;
import com.example.chatappdemo.internet.MyApplication;
import com.example.chatappdemo.model.GroupChat;
import com.example.chatappdemo.model.User;
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
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import gun0912.tedbottompicker.TedBottomPicker;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class GroupChatActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private static TextView internetStatus;
    private static int SPLASH_TIME_CONNECTED = 3000;
    private CircleImageView backGroupChat, imgGif, groupIconIv, imgMore, img_smile, startbtn, stopbtn, ibAddParticipant, ibInformationGroup;
    private LinearLayout bottom_linear, sendImage, sendFile, sendAudio, sendLocation, liner_record;
    private TextView groupTitleTv, mRecordLabel;
    private EmojiconEditText messageEt;
    private RecyclerView groupchatRv;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static final int TOTAL_ITEM_TO_LOAD = 12;
    private int mCurrentPage = 1;

    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    private FirebaseAuth firebaseAuth;
    private String groupId, myGroupRole;

    private ArrayList<GroupChat> groupChatList;
    private AdapterGroupChat adapterGroupChat;

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
        setContentView(R.layout.activity_group_chat);

        internetStatus = findViewById(R.id.internet_status);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            changeTextStatus(true);
        } else {
            changeTextStatus(false);
        }

        backGroupChat = findViewById(R.id.backGroupChat);
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        ibAddParticipant = findViewById(R.id.ibAddParticipant);
        ibInformationGroup = findViewById(R.id.ibInformationGroup);
        imgMore = findViewById(R.id.imgMore);
        bottom_linear = findViewById(R.id.bottom_linear);
        sendImage = findViewById(R.id.sendImage);
        sendFile = findViewById(R.id.sendFile);
        img_smile = findViewById(R.id.img_smile);
        messageEt = findViewById(R.id.messageEt);
        groupchatRv = findViewById(R.id.groupchatRv);

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        backGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupChatActivity.this, MainActivity.class));
                finish();
            }
        });

        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itemPos = 0;
                mCurrentPage++;
                loadGroupMessages();

            }
        });

        ibInformationGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentInforGroup = new Intent(GroupChatActivity.this, InforGroupActivity.class);
                intentInforGroup.putExtra("groupId", groupId);
                startActivity(intentInforGroup);
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

                        TedBottomPicker.with(GroupChatActivity.this)
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
                        Toast.makeText(GroupChatActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
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

        loadGroupInfo();
        readMessage();
        loadMyGroupRole();

        rootView = findViewById(R.id.root_view);
        emojIconActions = new EmojIconActions(this, rootView, messageEt, img_smile, "#00bdb5","#e8e8e8","#f4f4f4");
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

        final Animation animation = AnimationUtils.loadAnimation(GroupChatActivity.this, R.anim.anim_rotation);
        final Animation animation2 = AnimationUtils.loadAnimation(GroupChatActivity.this, R.anim.anim_rotation2);
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

        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String message = messageEt.getText().toString().trim();
                if (messageEt.getText().toString().equals("")) {
                    imgMore.setImageResource(R.drawable.add);
                    final Animation animation = AnimationUtils.loadAnimation(GroupChatActivity.this, R.anim.anim_rotation);
                    final Animation animation2 = AnimationUtils.loadAnimation(GroupChatActivity.this, R.anim.anim_rotation2);
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
                } else {
                    imgMore.setImageResource(R.drawable.send);
                    imgMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendMessage(message);
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
                    toggleKeyboardVisibility(GroupChatActivity.this);
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
                new SweetAlertDialog(GroupChatActivity.this, SweetAlertDialog.NORMAL_TYPE)
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
        ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
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

    private void readMessage() {
        groupChatList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Messages");
        Query messageQuery = ref.limitToLast(mCurrentPage * TOTAL_ITEM_TO_LOAD);
        messageQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    GroupChat model = ds.getValue(GroupChat.class);
                    itemPos++;
                    if (itemPos == 1){
                        String mMessageKey = snapshot.getKey();
                        mLastKey = mMessageKey;
                        mPrevKey = mMessageKey;
                    }
                    groupChatList.add(model);
                }
                adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatList);
                adapterGroupChat.notifyDataSetChanged();
                groupchatRv.setAdapter(adapterGroupChat);
                groupchatRv.smoothScrollToPosition(groupchatRv.getAdapter().getItemCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendSingle_MultiImageMessage(List<Uri> uriList) {
        SweetAlertDialog pDialog = new SweetAlertDialog(GroupChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2cb9b0"));
        pDialog.setTitleText("Sending " + uriList.size() + " Image...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filenamePath = "ChatImages/" + "" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
        for (upload_count = 0; upload_count < uriList.size(); upload_count++){
            myUri = uriList.get(upload_count);
            StorageReference childImg = storageReference.child(myUri.getLastPathSegment());
            childImg.putFile(myUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!p_uriTask.isSuccessful()) ;
                            Uri p_downloadUri = p_uriTask.getResult();
                            if (p_uriTask.isSuccessful()) {
                                String timestamp = "" + System.currentTimeMillis();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", "" + firebaseAuth.getUid());
                                hashMap.put("message", "" + p_downloadUri);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("type", "" + "image");
                                // add in db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                                ref.child(groupId).child("Messages").child(timestamp)
                                        .setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                messageEt.setText("");
                                                pDialog.dismiss();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toasty.error(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                                                pDialog.dismiss();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pDialog.dismiss();
                            Toasty.error(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                        }
                    });
        }
    }

    private void checkPermission(PermissionListener permissionlistener) {
        TedPermission.with(GroupChatActivity.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    private void sendImageGifMessage(String urlGif) {
        String timestamp = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", "" + urlGif);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("type", "" + "image_gif");
        // add in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        messageEt.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                    }
                });
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

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            myGroupRole = "" + ds.child("role").getValue();

                            if (myGroupRole.equals("Creator") || myGroupRole.equals("Admin")) {
                                ibAddParticipant.setVisibility(View.VISIBLE);
                                ibAddParticipant.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent1 = new Intent(GroupChatActivity.this, GroupParticipantAddActivity.class);
                                        intent1.putExtra("groupId", groupId);
                                        startActivity(intent1);
                                    }
                                });
                            } else {
                                ibAddParticipant.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadGroupMessages() {
        groupChatList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Messages");
        Query messageQuery = ref.limitToLast(mCurrentPage * TOTAL_ITEM_TO_LOAD);
        messageQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupChatList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            GroupChat model = ds.getValue(GroupChat.class);
                            String messageKey = snapshot.getKey();
                            if (!mPrevKey.equals(messageKey)) {
                                groupChatList.add(itemPos++, model);
                            }else {
                                mPrevKey = mLastKey;
                            }

                            if (itemPos == 1){
                                String mMessageKey = snapshot.getKey();
                                mLastKey = mMessageKey;
                            }
                            groupChatList.add(model);
                        }
                        adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatList);
                        adapterGroupChat.notifyDataSetChanged();
                        groupchatRv.setAdapter(adapterGroupChat);
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendMessage(String message) {
        String timestamp = "" + System.currentTimeMillis();
        //setup data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", "" + message);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("type", "" + "text");

        //add in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        messageEt.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
            }
        });

    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String groupDescription = "" + ds.child("groupDescription").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String createBy = "" + ds.child("createBy").getValue();

                            groupTitleTv.setText(groupTitle);
                            try {
                                Glide.with(GroupChatActivity.this).load(groupIcon).placeholder(R.drawable.group_icon_bottom).into(groupIconIv);
                            } catch (Exception e) {
                                groupIconIv.setImageResource(R.drawable.group_icon_bottom);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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

    private void sendFileAudio(Uri uriAudio) {
        SweetAlertDialog pDialog = new SweetAlertDialog(GroupChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2cb9b0"));
        pDialog.setTitleText("Sending Voice...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filenamePath = "ChatFiles/" + "" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
        //upload image
        storageReference.putFile(uriAudio)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!p_uriTask.isSuccessful()) ;
                        Uri p_downloadUri = p_uriTask.getResult();
                        if (p_uriTask.isSuccessful()) {
                            String timestamp = "" + System.currentTimeMillis();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", "" + firebaseAuth.getUid());
                            hashMap.put("message", "" + p_downloadUri);
                            hashMap.put("timestamp", "" + timestamp);
                            hashMap.put("type", "" + "audio");
                            hashMap.put("nameFile", ""+uriAudio.getLastPathSegment());
                            // add in db
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).child("Messages").child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            messageEt.setText("");
                                            pDialog.dismiss();
                                            mRecordLabel.setText("Uploading Finished");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                                            pDialog.dismiss();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                        pDialog.dismiss();
                    }
                });
    }

    private void sendFileMessage(Uri fileUri) {
        SweetAlertDialog pDialog = new SweetAlertDialog(GroupChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2cb9b0"));
        pDialog.setTitleText("Sending File...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filenamePath = "ChatFiles/" + "" + fileUri.getLastPathSegment();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
        //upload image
        storageReference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!p_uriTask.isSuccessful()) ;
                        Uri p_downloadUri = p_uriTask.getResult();
                        if (p_uriTask.isSuccessful()) {
                            String timestamp = "" + System.currentTimeMillis();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", "" + firebaseAuth.getUid());
                            hashMap.put("message", "" + p_downloadUri);
                            hashMap.put("timestamp", "" + timestamp);
                            hashMap.put("type", "" + "file");
                            hashMap.put("nameFile", ""+fileUri.getLastPathSegment());
                            // add in db
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).child("Messages").child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            messageEt.setText("");
                                            pDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                                            pDialog.dismiss();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                        pDialog.dismiss();
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