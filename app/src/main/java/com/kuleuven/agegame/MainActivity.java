package com.kuleuven.agegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    OkHttpClient client;
    private Button btnStart, btnProfile;
    private ImageView imgEasy, imgHard;
    private String db = "https://studev.groept.be/api/a23pt312/";
    private String imgHardURL = db + "imgEasy";
    private String imgEasyURL = db + "imgHard";
    private String imageURL;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        client = new OkHttpClient();
        UserInfo userInfo = new UserInfo(getApplicationContext());
        if(!userInfo.fileExist()){
            startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
        }

        //Changed how images are loaded, because we need to load two images
        //Concurrently we use this code instead!
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<String> easyImageFuture = executorService.submit(() -> imageGetter(imgEasyURL));
        Future<String> hardImageFuture = executorService.submit(() -> imageGetter(imgHardURL));

        executorService.shutdown();

        try {
            String easyImage = easyImageFuture.get();
            String hardImage = hardImageFuture.get();

            Glide.with(MainActivity.this)
                    .load(easyImage)
                    .override(250, 250)
                    .into(imgEasy);

            Glide.with(MainActivity.this)
                    .load(hardImage)
                    .override(250, 250)
                    .into(imgHard);
        } catch (Exception e) {
            e.printStackTrace();
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

    private String imageGetter(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            return parseImageURL(responseData);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String parseImageURL(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            return jsonObject.optString("image").replace("\\/", "/");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void initView(){
        btnStart = findViewById(R.id.btnStart);
        btnProfile = findViewById(R.id.btnProfile);
        imgEasy = findViewById(R.id.imgEasy);
        imgHard = findViewById(R.id.imgHard);
    }

}
