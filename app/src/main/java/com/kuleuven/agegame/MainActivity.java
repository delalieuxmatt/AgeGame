package com.kuleuven.agegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button btnStart;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        UserInfo userInfo = new UserInfo(getApplicationContext());
        if(!userInfo.fileExist()){
            startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    private void initView(){
        btnStart = findViewById(R.id.btnStart);
    }

}
