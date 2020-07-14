package com.example.chatappdemo.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatappdemo.R;
import com.example.chatappdemo.adapter.AdapterGroupChat;
import com.example.chatappdemo.model.GroupChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class GroupChatActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private CircleImageView backGroupChat, groupIconIv, imgMore, img_smile;
    private LinearLayout bottom_linear, sendImage, sendFile, sendGif, sendLocation;
    private ImageButton ibAddParticipant, ibInformationGroup;
    private TextView groupTitleTv;
    private EmojiconEditText messageEt;
    private RecyclerView groupchatRv;

    private FirebaseAuth firebaseAuth;
    private String groupId, myGroupRole;

    private ArrayList<GroupChat> groupChatList;
    private AdapterGroupChat adapterGroupChat;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;

    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;
    private static final int FILE_PICK_CODE = 3000;

    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri image_uri = null;
    private Uri file_uri = null;

    private View rootView;
    private EmojIconActions emojIconActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid", R.style.AppTheme);
        setTheme(themeIdcurrent);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

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
                finish();
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

        loadGroupInfo();
        loadGroupMessages();
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
        contentValues.put(MediaStore.Images.Media.TITLE, "GroupImageTitle");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "GroupImageDescription");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupChatList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            GroupChat model = ds.getValue(GroupChat.class);
                            groupChatList.add(model);
                        }
                        adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatList);
                        groupchatRv.setAdapter(adapterGroupChat);
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
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                Picasso.get().load(groupIcon).placeholder(R.drawable.group_icon_bottom).into(groupIconIv);
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
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                sendImageMessage();
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                sendImageMessage();
            }
            if (requestCode == FILE_PICK_CODE) {
                file_uri = data.getData();
                sendFileMessage(file_uri);
            }
        }
    }

    private void sendFileMessage(Uri file_uri) {
        SweetAlertDialog pDialog = new SweetAlertDialog(GroupChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2cb9b0"));
        pDialog.setTitleText("Sending File...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filenamePath = "ChatFiles/" + "" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
        //upload image
        storageReference.putFile(file_uri)
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
                                            Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            pDialog.dismiss();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void sendImageMessage() {
        SweetAlertDialog pDialog = new SweetAlertDialog(GroupChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#2cb9b0"));
        pDialog.setTitleText("Sending Image...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filenamePath = "ChatImages/" + "" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
        //upload image
        storageReference.putFile(image_uri)
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
                                            Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            pDialog.dismiss();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        pDialog.dismiss();
                    }
                });
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
                        Toast.makeText(this, "Camera & Storage permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, "Storage permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}