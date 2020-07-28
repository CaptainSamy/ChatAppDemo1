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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatappdemo.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class SigninActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private FirebaseAuth firebaseAuth;
    private MaterialButton login_Button, imgGoogle;
    private TextInputLayout login_Email, login_Password;
    private TextView login_forgetPassword, tv_register;
    private ProgressDialog loadingBar;
    public String email_signin, password_signin;
    private CircleImageView btn_Back;
    private DatabaseReference UserRef;
    private Boolean emailChecker;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z][\\w-]+@([\\w]+\\.[\\w]+|[\\w]+\\.[\\w]{2,}\\.[\\w]{2,})$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        firebaseAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        loadingBar = new ProgressDialog(this);

        login_Button = findViewById(R.id.login_button);
        login_Email = findViewById(R.id.login_email);
        login_Password = findViewById(R.id.login_password);
        login_forgetPassword = findViewById(R.id.forget_password_link);

        btn_Back = findViewById(R.id.back_signin);
        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_register = findViewById(R.id.tv_register);
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SigninActivity.this, SignupActivity.class));
            }
        });
        imgGoogle = findViewById(R.id.img_google);
        imgGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        login_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateEmail() | !validatePassword()) {
                    return;
                } else {
                    email_signin = login_Email.getEditText().getText().toString();
                    password_signin = login_Password.getEditText().getText().toString();
                    loadingBar.setTitle("Log In");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    firebaseAuth.signInWithEmailAndPassword(email_signin, password_signin)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        final String currentUserID = firebaseAuth.getCurrentUser().getUid();
                                        final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                        UserRef.child(currentUserID).child("device_token")
                                                .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
                                                    Toasty.success(SigninActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT, true).show();
                                                    loadingBar.dismiss();
                                                    VerifyEmailAddress();
                                                }
                                            }
                                        });


                                    } else {
                                        String message = task.getException().toString();
                                        Toasty.error(SigninActivity.this, "Error: " + message, Toast.LENGTH_SHORT, true).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(SigninActivity.this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toasty.error(SigninActivity.this, "Connection to Google Sign In false.", Toast.LENGTH_SHORT, true).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        login_forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_forget = new Intent(SigninActivity.this, ResetPasswordActivity.class);
                startActivity(intent_forget);
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            loadingBar.setTitle("Google Sign In");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toasty.info(SigninActivity.this, "Please wait, while we are getting your auth result.", Toast.LENGTH_SHORT, true).show();
            } else {
                Toasty.error(SigninActivity.this, "Can't get auth result!", Toast.LENGTH_SHORT, true).show();
                loadingBar.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final String currentUserID = firebaseAuth.getCurrentUser().getUid();
                            final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            UserRef.child(currentUserID).child("device_token")
                                    .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Intent intent1 = new Intent(SigninActivity.this, MainActivity.class);
                                        startActivity(intent1);
                                    } else {
                                        loadingBar.dismiss();
                                        String message = task.getException().toString();
                                        Toasty.error(SigninActivity.this, "Not authenticated: " + message, Toast.LENGTH_SHORT, true).show();
                                    }
                                }
                            });
                        }
                    }
                });
    }


    private void VerifyEmailAddress() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        emailChecker = user.isEmailVerified();
        if (emailChecker) {
            Intent intent1 = new Intent(SigninActivity.this, MainActivity.class);
            startActivity(intent1);
        } else {
            Toasty.info(SigninActivity.this, "Please verify your accout first!", Toast.LENGTH_SHORT, true).show();
            firebaseAuth.signOut();
        }
    }

    private boolean validateEmail() {
        email_signin = login_Email.getEditText().getText().toString().trim();
        if (email_signin.isEmpty()) {
            login_Email.setError("You must not leave blank!");
            return false;
        } else if (!EMAIL_PATTERN.matcher(email_signin).matches()) {
            login_Email.setError("Please enter the correct email format!");
            return false;
        } else {
            login_Email.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        password_signin = login_Password.getEditText().getText().toString().trim();
        if (password_signin.isEmpty()) {
            login_Password.setError("You must not leave blank!");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password_signin).matches()) {
            login_Password.setError("Password needs to have 1 uppercase letter and is longer than 6 characters including letters and numbers!");
            return false;
        } else {
            login_Password.setError(null);
            return true;
        }
    }
}