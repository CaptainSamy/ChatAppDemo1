package com.example.chatappdemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatappdemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

public class ResetPasswordActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private Toolbar toolbar;
    private TextInputLayout inputEmail;
    private MaterialButton btnReset;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z][\\w-]+@([\\w]+\\.[\\w]+|[\\w]+\\.[\\w]{2,}\\.[\\w]{2,})$");
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        inputEmail =  findViewById(R.id.email);
        btnReset =  findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progressBar);
        firebaseAuth = FirebaseAuth.getInstance();

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateEmail()) {
                    return;
                } else {
                    email = inputEmail.getEditText().getText().toString().trim();
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toasty.info(ResetPasswordActivity.this, "We have sent you instructions to reset your password. Please check your email!", Toast.LENGTH_SHORT, true).show();
                                    } else {
                                        Toasty.error(ResetPasswordActivity.this, "Could not send request!", Toast.LENGTH_SHORT, true).show();
                                    }

                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });
    }

    private boolean validateEmail() {
        email = inputEmail.getEditText().getText().toString().trim();
        if (email.isEmpty()) {
            inputEmail.setError("Bạn không được để trống!");
            return false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            inputEmail.setError("Vui lòng nhập đúng định dạng email!");
            return false;
        } else {
            inputEmail.setError(null);
            return true;
        }
    }
}