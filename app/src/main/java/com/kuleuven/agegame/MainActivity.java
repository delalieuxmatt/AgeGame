package com.kuleuven.agegame;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    OkHttpClient client;
    private Button btnStart, btnProfile, uploadImg, btnCreateMultiHL,btnWaitMultiHL;
    private ImageView imgEasy, imgHard;
    private String db = "https://studev.groept.be/api/a23pt312/randomImage";
    private String imgHardURL = db;
    private String imgEasyURL = db;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        client = new OkHttpClient();

        //configPerms(); does not work yet, for some reason can't click the thing?

        UserInfo userInfo = new UserInfo(getApplicationContext());
        if(!userInfo.fileExist()){
            startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
        }

        //Changed how images are loaded, because we need to load two images
        //Concurrently we use this code instead!

        //create a thread pool with 2 threads
        //ExecutorService implements asynchronous execution

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

        uploadImg.setOnClickListener(v->redirect(AmplifyTestPage.class));

        btnStart.setOnClickListener(v->redirect(singleGame.class));

        btnProfile.setOnClickListener(v->profileLink());

        btnCreateMultiHL.setOnClickListener(v->redirect(CreateMultiplayer.class));

        btnWaitMultiHL.setOnClickListener(v->redirect(WaitMultiplayer.class));
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

    public void redirect(Class<?> nextLocation){
        Intent intent = new Intent(this, nextLocation);
        startActivity(intent);
    }

    public void configPerms(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // If the permission for storage usage was already granted, nothing should happen
            } else {
                //Otherwise an intent is opened to manage settings for app permissions
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    public void profileLink(){
        UserInfo userInfo = new UserInfo(getApplicationContext());
        if(!userInfo.fileExist()){
            redirect(RegistrationActivity.class);
            //Indicates that the user info has been initialised
        }
        else{
            //Indicates that the user info has not been initialised
            redirect(ProfileActivity.class);
        }
    }

    private void initView(){
        btnStart = findViewById(R.id.btnStart);
        btnProfile = findViewById(R.id.btnProfile);
        imgEasy = findViewById(R.id.imgEasy);
        imgHard = findViewById(R.id.imgHard);
        uploadImg = findViewById(R.id.uploadImg);
        btnCreateMultiHL = findViewById(R.id.btnCreateMultiHL);
        btnWaitMultiHL = findViewById(R.id.btnWaitMultiHL);
    }

}
