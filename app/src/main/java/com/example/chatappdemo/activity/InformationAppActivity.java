package com.example.chatappdemo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.example.chatappdemo.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class InformationAppActivity extends AppCompatActivity {
    int themeIdcurrent;
    String SHARED_PREFS = "codeTheme";
    private CircleImageView back_informationApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences locationpref = getApplicationContext()
                .getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        themeIdcurrent = locationpref.getInt("themeid",R.style.AppTheme);
        setTheme(themeIdcurrent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_app);
        back_informationApp = findViewById(R.id.back_informationApp);
        back_informationApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}