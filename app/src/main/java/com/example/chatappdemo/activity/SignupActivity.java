package com.example.chatappdemo.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import com.example.chatappdemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class SignupActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private CircleImageView btn_Back;
    private FirebaseAuth firebaseAuth;
    private TextView tv_login;
    private DatabaseReference databaseReference;
    private MaterialButton register_button;
    private TextInputLayout register_Email, register_Password, register_ConfirmPassword;
    private ProgressDialog loadingBar;
    private String email_signup, password_signup, rePassword_signup;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z][\\w-]+@([\\w]+\\.[\\w]+|[\\w]+\\.[\\w]{2,}\\.[\\w]{2,})$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        btn_Back = findViewById(R.id.back_signup);
        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_login = findViewById(R.id.tv_login);
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, SigninActivity.class));
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        loadingBar = new ProgressDialog(SignupActivity.this);
        register_button = findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        register_Email = findViewById(R.id.register_email);
        register_Password = findViewById(R.id.register_password);
        register_ConfirmPassword = findViewById(R.id.register_confirm_password);
    }

    public void registerUser(){
        email_signup = register_Email.getEditText().getText().toString();
        password_signup = register_Password.getEditText().getText().toString();
        rePassword_signup = register_ConfirmPassword.getEditText().getText().toString();

        if (!validateEmail() | !validatePassword() | !validateRePassword()) {
            return;
        } else {
            loadingBar.setTitle("Creating account");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            firebaseAuth.createUserWithEmailAndPassword(email_signup, password_signup)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                SendEmailVerificationMessage();
                            } else {
                                String message = task.getException().toString();
                                Toasty.error(SignupActivity.this, "Error: " + message, Toast.LENGTH_SHORT, true).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendEmailVerificationMessage() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        new SweetAlertDialog(SignupActivity.this, SweetAlertDialog.NORMAL_TYPE)
                                .setTitleText("Notification")
                                .setContentText("We have sent an email to you. Please check your email!")
                                .setConfirmText("OK!")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        onBackPressed();
                                    }
                                })
                                .show();
                        firebaseAuth.signOut();
                    } else {
                        String error = task.getException().getMessage();
                        Toasty.error(SignupActivity.this, "Error: " + error, Toast.LENGTH_SHORT, true).show();
                        firebaseAuth.signOut();
                    }
                }
            });
        }
    }

    private boolean validateEmail() {
        email_signup = register_Email.getEditText().getText().toString().trim();
        if (email_signup.isEmpty()) {
            register_Email.setError("Bạn không được để trống!");
            return false;
        } else if (!EMAIL_PATTERN.matcher(email_signup).matches()) {
            register_Email.setError("Vui lòng nhập đúng định dạng email!");
            return false;
        } else {
            register_Email.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        password_signup = register_Password.getEditText().getText().toString().trim();
        if (password_signup.isEmpty()) {
            register_Password.setError("You must not leave blank!");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password_signup).matches()) {
            register_Password.setError("Password needs to have 1 uppercase letter and is longer than 6 characters including letters and numbers!");
            return false;
        } else {
            register_Password.setError(null);
            return true;
        }
    }

    private boolean validateRePassword() {
        rePassword_signup = register_ConfirmPassword.getEditText().getText().toString().trim();
        password_signup = register_Password.getEditText().getText().toString().trim();
        if (rePassword_signup.isEmpty()) {
            register_ConfirmPassword.setError("You must not leave blank!");
            return false;
        } else if (!rePassword_signup.equals(password_signup)) {
            register_ConfirmPassword.setError("The password is different from the password above!");
            return false;
        } else {
            register_ConfirmPassword.setError(null);
            return true;
        }
    }
}