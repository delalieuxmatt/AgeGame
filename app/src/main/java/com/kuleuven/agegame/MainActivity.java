package com.kuleuven.agegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {
    OkHttpClient client;
    private Button btnStart, btnProfile;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        client = new OkHttpClient();
        UserInfo userInfo = new UserInfo(getApplicationContext());
        if(!userInfo.fileExist()){
            startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,singleGame.class);
                startActivity(intent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                UserInfo userInfo = new UserInfo(getApplicationContext());
                if(!userInfo.fileExist()){
                    //Indicates that the user info has been initialised
                    intent  = new Intent(MainActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                }
                else{
                    //Indicates that the user info has not been initialised
                    intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
    private void initView(){
        btnStart = findViewById(R.id.btnStart);
        btnProfile = findViewById(R.id.btnProfile);
    }

}
