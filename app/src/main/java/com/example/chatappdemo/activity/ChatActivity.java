package com.example.chatappdemo.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private String messReceiverId, messReceiverImage, messReceiverName, messSenderId;
    private CircleImageView imgMore, imgGif, imgProfileFriend, back_user_chat, imgSmile, onlineStatusIv;
    private LinearLayout bottom_linear, sendImage, sendFile, sendGif, sendLocation;
    private TextView name_user_chat, userLastSeen;
    private EmojiconEditText messageInput;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference RootRef;
    private ValueEventListener seenListerner;
    private DatabaseReference userRefForSeen;

    private List<Messages> messagesList;
    private RecyclerView userMessageList;
    private AdapterMessage adapterMessage;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static final int TOTAL_ITEM_TO_LOAD = 15;
    private int mCurrentPage = 1;

    //Solution for descending list on refresh
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    private RequestQueue requestQueue;
    private boolean notify = false;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;

    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;
    private static final int FILE_PICK_CODE = 3000;

    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri myUri = null;

    private View rootView;
    private EmojIconActions emojIconActions;
    private EmoticonGIFKeyboardFragment mEmoticonGIFKeyboardFragment;
    private FrameLayout keyboard_container;

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

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        back_user_chat = findViewById(R.id.back_user_chat);
        back_user_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgProfileFriend = findViewById(R.id.image_user_chat);
        name_user_chat = findViewById(R.id.name_user_chat);
        sendImage = findViewById(R.id.sendImage);
        sendFile = findViewById(R.id.sendFile);
        onlineStatusIv = findViewById(R.id.onlineStatusIv);
        userLastSeen = findViewById(R.id.user_last_seen);
        userMessageList = (RecyclerView) findViewById(R.id.messager_list_of_users);

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
                        Picasso.get().load(messReceiverImage).placeholder(R.drawable.user_profile).into(imgProfileFriend);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.user_profile).into(imgProfileFriend);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkOnlineStatus("online");

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

        cameraPermission = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageImportDialog();
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

        readMessages();
        seenMessage();
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

    private void showImageImportDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if (!checkCameraPermission()) {
                                requestCameraPermission();
                            } else {
                                pickCamera();
                            }
                        } else {
                            if (!checkStoragePermission()) {
                                requestStoragePermission();
                            } else {
                                pickGallery();
                            }
                        }
                    }
                }).show();
    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "ImageTitle");

        myUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, myUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toasty.info(this, "Camera & Storage permissions are required.", Toast.LENGTH_SHORT, true).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toasty.info(this, "Storage permissions are required.", Toast.LENGTH_SHORT, true).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                myUri = data.getData();
                sendImageMessage(myUri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                sendImageMessage(myUri);
            }
            if (requestCode == FILE_PICK_CODE) {
                myUri = data.getData();
                sendFileMessage(myUri);
            }
        }
    }

    private void sendFileMessage(Uri file_uri) {
        notify = true;
        SweetAlertDialog pDialog = new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2cb9b0"));
        pDialog.setTitleText("Sending File...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filenamePath = "ChatFiles/" + "" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filenamePath);
        storageReference.putFile(file_uri)
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

    private void sendImageMessage(Uri image_uri) {
        notify = true;
        SweetAlertDialog pDialog = new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2cb9b0"));
        pDialog.setTitleText("Sending Image...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filenamePath = "ChatImages/" + "" + System.currentTimeMillis();

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filenamePath);
        storageReference.putBytes(data)
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
                    userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void sendMessage(final String messageText) {
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

    @Override
    protected void onStart() {
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListerner);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        checkOnlineStatus("offline");
        super.onDestroy();
    }
}
