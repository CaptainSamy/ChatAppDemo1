package com.example.chatappdemo.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chatappdemo.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

class View_Update_Profile_User_Activity extends AppCompatActivity {
    private ImageButton imgBtnBG;
    private CircleImageView imgBtnDD;
    private TextView tv_userName, tv_Phone, tv_Status, tv_Gioitinh, tv_Done;
    private FloatingActionButton fab;
    private ProgressDialog progressDialog;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_update_profile_user_2);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        tv_Done = findViewById(R.id.tv_Done);
        tv_Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBackMain = new Intent(View_Update_Profile_User_Activity.this, MainActivity.class);
                //intentBackMain.putExtra("imageDD", ImageDD);
                startActivity(intentBackMain);
            }
        });
        imgBtnBG = findViewById(R.id.imgBtnBG);
        imgBtnDD = findViewById(R.id.imgBtnDD);
        tv_userName = findViewById(R.id.tv_userName);
        tv_Phone = findViewById(R.id.tv_Phone);
        tv_Status = findViewById(R.id.tv_Status);
        tv_Gioitinh = findViewById(R.id.tv_Gioitinh);
        fab = findViewById(R.id.fab);
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
                    tv_userName.setText(name);
                    tv_Status.setText(status);
                    tv_Gioitinh.setText(gioiTinh);
                    tv_Phone.setText(phone);
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

        //fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
        DisplayProfile();
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

    private void showEditProfileDialog() {
        //option show dialog
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Status", "Edit Sex", "Edit Phone"};
        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    progressDialog.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "imgAnhDD";
                    showImagePicDialog();
                }else if (which == 1) {
                    progressDialog.setMessage("Updating Background Picture");
                    profileOrCoverPhoto = "imgAnhBia";
                    showImagePicDialog();
                }else if (which == 2) {
                    progressDialog.setMessage("Updating Profile Name");
                    showNameStatusSexPhoneUpdateDialog("name");
                }else if (which == 3) {
                    progressDialog.setMessage("Updating Profile Status");
                    showNameStatusSexPhoneUpdateDialog("status");
                }else if (which == 4) {
                    progressDialog.setMessage("Updating Profile Sex");
                    showNameStatusSexPhoneUpdateDialog("gioiTinh");
                }else if (which == 5){
                    progressDialog.setMessage("Updating Profile Phone");
                    showNameStatusSexPhoneUpdateDialog("phone");
                }
            }
        });
        builder.create().show();
    }

    private void showNameStatusSexPhoneUpdateDialog(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update " + key);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(this);
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)) {
                    progressDialog.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(View_Update_Profile_User_Activity.this, "Updated...",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(View_Update_Profile_User_Activity.this, "" + e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(View_Update_Profile_User_Activity.this, "Please enter " + key,Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
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
                                            Toast.makeText(View_Update_Profile_User_Activity.this, "Image Updated...",Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(View_Update_Profile_User_Activity.this, "Error Updating Image...",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(View_Update_Profile_User_Activity.this, "Some error occured",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(View_Update_Profile_User_Activity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
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

    private void DisplayProfile() {
        databaseReference.child("Users").child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && ((dataSnapshot.hasChild("imgAnhDD"))
                                && (dataSnapshot.hasChild("imgAnhBia"))
                                && (dataSnapshot.hasChild("name"))
                                && (dataSnapshot.hasChild("status"))
                                && (dataSnapshot.hasChild("gioiTinh"))
                                && (dataSnapshot.hasChild("phone")))) {
                            String ImageDD = dataSnapshot.child("imgAnhDD").getValue().toString();
                            String ImageBG = dataSnapshot.child("imgAnhBia").getValue().toString();
                            String UserName = dataSnapshot.child("name").getValue().toString();
                            String Status = dataSnapshot.child("status").getValue().toString();
                            String Phone = dataSnapshot.child("phone").getValue().toString();
                            String GioiTinh = dataSnapshot.child("gioiTinh").getValue().toString();


                            tv_userName.setText(UserName);
                            tv_Status.setText(Status);
                            tv_Phone.setText(Phone);
                            tv_Gioitinh.setText(GioiTinh);
                            Picasso.get().load(ImageDD).placeholder(R.drawable.user_profile).into(imgBtnDD);
                            Picasso.get().load(ImageBG).placeholder(R.drawable.teabackground).into(imgBtnBG);
                        } else if ((dataSnapshot.exists()) && ((dataSnapshot.hasChild("name"))
                                && (dataSnapshot.hasChild("status"))
                                && (dataSnapshot.hasChild("gioiTinh"))
                                && (dataSnapshot.hasChild("phone")))) {

                            String UserName = dataSnapshot.child("name").getValue().toString();
                            String Status = dataSnapshot.child("status").getValue().toString();
                            String Phone = dataSnapshot.child("phone").getValue().toString();
                            String GioiTinh = dataSnapshot.child("gioiTinh").getValue().toString();

                            tv_userName.setText(UserName);
                            tv_Status.setText(Status);
                            tv_Phone.setText(Phone);
                            tv_Gioitinh.setText(GioiTinh);

                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}