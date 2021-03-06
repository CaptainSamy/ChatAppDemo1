package com.example.chatappdemo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.internet.MyApplication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class UpdateProfileUserActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private RadioGroup radioGroup;
    private TextView tv_Cancel;
    private RadioButton radioButtonOption, male_checkbox, female_checkbox;
    private TextInputLayout set_user_name, set_profile_status;
    private CountryCodePicker ccp;
    private EditText set_profile_phone;
    private ImageButton imgBtnBG;
    private MaterialButton update_button;
    private CircleImageView  imgBtnCamBG, imgBtnDD, imgBtnCamDD;
    private String imgAnhBia, imgAnhDD, name, status, gioiTinh, phone;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    String storagePath = "User_Profile_Cover_Imgs/";

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    String cameraPermissions[];
    String storagePermissions[];
    Uri image_uri;
    String profileOrCoverPhoto;
    SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_user);
        pDialog = new SweetAlertDialog(UpdateProfileUserActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#00c7bf"));
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        imgBtnBG = findViewById(R.id.imgBtnBG);
        imgBtnBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileOrCoverPhoto = "imgAnhBia";
                showImagePicDialog();
            }
        });

        imgBtnCamBG = findViewById(R.id.imgBtnCamBG);
        imgBtnCamBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileOrCoverPhoto = "imgAnhBia";
                showImagePicDialog();
            }
        });

        imgBtnDD = findViewById(R.id.imgBtnDD);
        imgBtnDD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileOrCoverPhoto = "imgAnhDD";
                showImagePicDialog();
            }
        });

        imgBtnCamDD = findViewById(R.id.imgBtnCamDD);
        imgBtnCamDD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileOrCoverPhoto = "imgAnhDD";
                showImagePicDialog();
            }
        });
        set_user_name = findViewById(R.id.set_user_name);
        set_profile_status = findViewById(R.id.set_profile_status);
        set_profile_phone = findViewById(R.id.set_profile_phone);
        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(set_profile_phone);
        update_button = findViewById(R.id.update_button);
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateProfileUser();
            }
        });

        tv_Cancel = findViewById(R.id.tv_Cancel);
        tv_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        radioGroup = findViewById(R.id.radio_group);
        male_checkbox = findViewById(R.id.male_checkbox);
        female_checkbox = findViewById(R.id.female_checkbox);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButtonOption = radioGroup.findViewById(checkedId);
                switch (checkedId) {
                    case R.id.male_checkbox:
                        gioiTinh = "male";
                        break;
                    case R.id.female_checkbox:
                        gioiTinh = "female";
                        break;
                    default:
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInformation();
    }

    private void loadInformation() {
        Query query = databaseReference.orderByChild("uid").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    String name = ds.child("name").getValue().toString();
                    String status = ds.child("status").getValue().toString();
                    String gioiTinh = ds.child("gioiTinh").getValue().toString();
                    String phone = ds.child("phone").getValue().toString();
                    String imgAnhDD = ds.child("imgAnhDD").getValue().toString();
                    String imgAnhBia = ds.child("imgAnhBia").getValue().toString();

                    if (gioiTinh.equals("male")){
                        male_checkbox.setChecked(true);
                        female_checkbox.setChecked(false);
                    }else {
                        female_checkbox.setChecked(true);
                        male_checkbox.setChecked(false);
                    }

                    //set data
                    set_user_name.getEditText().setText(name);
                    set_profile_status.getEditText().setText(status);
                    set_profile_phone.setText(phone);
                    try {
                        Glide.with(UpdateProfileUserActivity.this).load(imgAnhDD).placeholder(R.drawable.user_profile).into(imgBtnDD);
                    } catch (Exception e) {
                        imgBtnDD.setImageResource(R.drawable.user_profile);
                    }

                    try {
                        Glide.with(UpdateProfileUserActivity.this).load(imgAnhBia).placeholder(R.drawable.teabackground).into(imgBtnBG);
                    } catch (Exception e) {
                        imgBtnBG.setImageResource(R.drawable.teabackground);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void UpdateProfileUser() {
        SweetAlertDialog pDialog = new SweetAlertDialog(UpdateProfileUserActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Updating...");
        pDialog.setCancelable(false);
        pDialog.show();

        String edtUserName = set_user_name.getEditText().getText().toString().trim();
        String edtStatus = set_profile_status.getEditText().getText().toString().trim();
        String edtPhone = ccp.getFullNumberWithPlus().trim();
        if (!validateName() | !validateStatus() | !validatePhone() | !validateSex()) {
            pDialog.dismissWithAnimation();
            return;
        }else {
            updateProfile(""+edtUserName, ""+edtStatus, ""+edtPhone, ""+gioiTinh);
            pDialog.dismissWithAnimation();
        }
    }

    private void updateProfile(String edtUserName, String edtStatus, String edtPhone, String gioiTinh) {
        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("uid", user.getUid());
        profileMap.put("name", edtUserName);
        profileMap.put("status", edtStatus);
        profileMap.put("phone", edtPhone);
        profileMap.put("gioiTinh", gioiTinh);
        profileMap.put("onlineStatus", "online");
        profileMap.put("typingTo", "noOne");

        databaseReference.child(user.getUid()).updateChildren(profileMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toasty.success(UpdateProfileUserActivity.this, "Updated!", Toast.LENGTH_SHORT, true).show();
                        startActivity(new Intent(UpdateProfileUserActivity.this, ViewProfileUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(UpdateProfileUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                    }
                });
    }

    private void showImagePicDialog() {
        //option show dialog
        String options[] = {"Camera", "Gallery"};
        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }else if (which == 1) {
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri image_uri) {
        SweetAlertDialog pDialog = new SweetAlertDialog(UpdateProfileUserActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Uploading...");
        pDialog.setCancelable(false);
        pDialog.show();

        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto, downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pDialog.dismiss();
                                            Toasty.success(UpdateProfileUserActivity.this, "Image Updated!", Toast.LENGTH_SHORT, true).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pDialog.dismiss();
                                            Toasty.error(UpdateProfileUserActivity.this, "Error Updating Image!", Toast.LENGTH_SHORT, true).show();
                                        }
                                    });
                        } else {
                            pDialog.dismiss();
                            Toasty.error(UpdateProfileUserActivity.this, "Some error occured!", Toast.LENGTH_SHORT, true).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(UpdateProfileUserActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT, true).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toasty.info(this, "Please enable permission!", Toast.LENGTH_SHORT, true).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        Toasty.info(this, "Please enable permission!", Toast.LENGTH_SHORT, true).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean validateName() {
        String edtUserName = set_user_name.getEditText().getText().toString().trim();
        if (edtUserName.isEmpty()) {
            set_user_name.setError("Please write your name!");
            return false;
        } else {
            set_user_name.setError(null);
            return true;
        }
    }

    private boolean validateStatus() {
        String edtStatus = set_profile_status.getEditText().getText().toString().trim();
        if (edtStatus.isEmpty()) {
            set_profile_status.setError("Please write your status!");
            return false;
        } else {
            set_profile_status.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String edtPhone = ccp.getFullNumberWithPlus();
        if (edtPhone.isEmpty()) {
            set_profile_phone.setError("Please phone your number!");
            return false;
        } else {
            set_profile_phone.setError(null);
            return true;
        }
    }

    private boolean validateSex() {
        int isSelecter = radioGroup.getCheckedRadioButtonId();
        if (isSelecter == -1) {
            Toasty.error(this, "Please select a gender!", Toast.LENGTH_SHORT, true).show();
            return false;
        }
        return true;
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
}
