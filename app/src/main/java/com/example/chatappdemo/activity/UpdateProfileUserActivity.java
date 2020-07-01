package com.example.chatappdemo.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import com.example.chatappdemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileUserActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private RadioGroup radioGroup;
    private TextView tv_Cancel;
    private RadioButton radioButtonOption;
    private TextInputLayout set_user_name, set_profile_status;
    private CountryCodePicker ccp;
    private EditText set_profile_phone;
    private ImageButton imgBtnBG;
    private CircleImageView update_button, imgBtnCamBG, imgBtnDD, imgBtnCamDD;
    private ProgressDialog progressDialog;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_user);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        progressDialog = new ProgressDialog(this);

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

                    //set data
                    set_user_name.getEditText().setText(name);
                    set_profile_status.getEditText().setText(status);
                    //tv_Gioitinh.setText(gioiTinh);
                    set_profile_phone.setText(phone);
                    try {
                        Picasso.get().load(imgAnhDD).into(imgBtnDD);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.user_profile).into(imgBtnDD);
                    }
                    try {
                        Picasso.get().load(imgAnhBia).into(imgBtnBG);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.teabackground).into(imgBtnBG);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        DisplayProfile();

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
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButtonOption = radioGroup.findViewById(checkedId);
                switch (checkedId) {
                    case R.id.male_checkbox:
                        gioiTinh = radioButtonOption.getText().toString();
                        break;
                    case R.id.female_checkbox:
                        gioiTinh = radioButtonOption.getText().toString();
                        break;
                    default:
                }
            }
        });

    }
//    private void DisplayProfile() {
//        databaseReference.child("Users").child(user.getUid())
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if ((dataSnapshot.exists()) && ((dataSnapshot.hasChild("imgAnhDD"))
//                                && (dataSnapshot.hasChild("imgAnhBia"))
//                                && (dataSnapshot.hasChild("name"))
//                                && (dataSnapshot.hasChild("status"))
//                                && (dataSnapshot.hasChild("gioiTinh"))
//                                && (dataSnapshot.hasChild("phone")))) {
//                            String ImageDD = dataSnapshot.child("imgAnhDD").getValue().toString();
//                            String ImageBG = dataSnapshot.child("imgAnhBia").getValue().toString();
//                            String UserName = dataSnapshot.child("name").getValue().toString();
//                            String Status = dataSnapshot.child("status").getValue().toString();
//                            String Phone = dataSnapshot.child("phone").getValue().toString();
//                            String GioiTinh = dataSnapshot.child("gioiTinh").getValue().toString();
//
//
//                            set_user_name.getEditText().setText(UserName);
//                            set_profile_status.getEditText().setText(Status);
//                            set_profile_phone.setText(Phone);
//                            Picasso.get().load(ImageDD).placeholder(R.drawable.user_profile).into(imgBtnDD);
//                            Picasso.get().load(ImageBG).placeholder(R.drawable.teabackground).into(imgBtnBG);
//                        } else if ((dataSnapshot.exists()) && ((dataSnapshot.hasChild("name"))
//                                && (dataSnapshot.hasChild("status"))
//                                && (dataSnapshot.hasChild("gioiTinh"))
//                                && (dataSnapshot.hasChild("phone")))) {
//
//                            String UserName = dataSnapshot.child("name").getValue().toString();
//                            String Status = dataSnapshot.child("status").getValue().toString();
//                            String Phone = dataSnapshot.child("phone").getValue().toString();
//                            String GioiTinh = dataSnapshot.child("gioiTinh").getValue().toString();
//
//                            set_user_name.getEditText().setText(UserName);
//                            set_profile_status.getEditText().setText(Status);
//                            set_profile_phone.setText(Phone);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//    }

    private void UpdateProfileUser() {
        String edtUserName = set_user_name.getEditText().getText().toString().trim();
        String edtStatus = set_profile_status.getEditText().getText().toString().trim();
        String edtPhone = ccp.getFullNumberWithPlus().trim();
        if (!validateName() | !validateStatus() | !validatePhone() | !validateSex()) {
            return;
        } else {

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
                            progressDialog.dismiss();
                            update_button.setImageResource(R.drawable.tick);
                            Toast.makeText(UpdateProfileUserActivity.this, "Updated...",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UpdateProfileUserActivity.this, ViewProfileUserActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UpdateProfileUserActivity.this, "" + e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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

    private void uploadProfileCoverPhoto(Uri uri) {
        progressDialog.show();
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
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
                                            progressDialog.dismiss();
                                            Toast.makeText(UpdateProfileUserActivity.this, "Image Updated...",Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(UpdateProfileUserActivity.this, "Error Updating Image...",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(UpdateProfileUserActivity.this, "Some error occured",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(UpdateProfileUserActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Please enable permission",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please enable permission",Toast.LENGTH_SHORT).show();
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
            set_user_name.setError("Hay dien ten ban muon hien thi!");
            return false;
        } else {
            set_user_name.setError(null);
            return true;
        }
    }

    private boolean validateStatus() {
        String edtStatus = set_profile_status.getEditText().getText().toString().trim();
        if (edtStatus.isEmpty()) {
            set_profile_status.setError("Hay them loi gioi thieu ve ban!");
            return false;
        } else {
            set_profile_status.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String edtPhone = ccp.getFullNumberWithPlus();
        if (edtPhone.isEmpty()) {
            set_profile_phone.setError("Bạn không được để trống!");
            return false;
        } else {
            set_profile_phone.setError(null);
            return true;
        }
    }

    private boolean validateSex() {
        int isSelecter = radioGroup.getCheckedRadioButtonId();
        if (isSelecter == -1) {
            Toast.makeText(this, "Bạn chưa chọn giới tính!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
