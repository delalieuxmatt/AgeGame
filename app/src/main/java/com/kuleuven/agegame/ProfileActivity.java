package com.kuleuven.agegame;


import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    Button btnProfileHome, btnProfileLogout;
    TextView txtFirstName, txtLastName, txtEmail, singlePlayed, singleAccuracy, hlPlayed;
    OkHttpClient client;
    String userID;
    int sgamesPlayed, stotalDifference, hlGamesPlayed, hltotalDifference;

    String dbURL = "https://studev.groept.be/api/a23pt312/standardStatsTotalUserID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        UserInfo userInfo = new UserInfo(getApplicationContext());
        String[] info = userInfo.readFile();
        userID = info[3];
        initView();
        singleStats();
        initProfile();
        btnProfileHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btnProfileLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfo userInfo = new UserInfo(getApplicationContext());
                System.out.println(userInfo.deleteFile());
                Intent intent = new Intent(ProfileActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }

    public void initView(){
        btnProfileHome = findViewById(R.id.btnProfileHome);
        btnProfileLogout = findViewById(R.id.btnProfileHome);
        txtFirstName = findViewById(R.id.txtProfileFirstName);
        txtLastName = findViewById(R.id.txtProfileLastName);
        txtEmail = findViewById(R.id.txtProfileEmail);
        singlePlayed = findViewById(R.id.singlePlayed);
        singleAccuracy = findViewById(R.id.singleAccuracy);
        hlPlayed = findViewById(R.id.hlPlayed);

    }

    public void initProfile(){
        UserInfo userInfo = new UserInfo(getApplicationContext());
        String[] info = userInfo.readFile();
        txtFirstName.setText(info[0]);
        txtLastName.setText(info[1]);
        txtEmail.setText(info[2]);
        singlePlayed.setText(String.valueOf(sgamesPlayed));
        int acc = sgamesPlayed + stotalDifference;
        singleAccuracy.setText(String.valueOf(acc));
        //hlPlayed.setText(String.valueOf(userID)); to check the user ID uncomment for now

    }

    public void singleStats(){
        System.out.println("WE ARE IN THE STATS NOW");
        client = new OkHttpClient();
        System.out.println(userID + "UserID");
        RequestBody requestBody = new FormBody.Builder()
                .add("userID", userID)
                .build();
        Request request = new Request.Builder()
                .url(dbURL)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(dbURL);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    sgamesPlayed = jsonObject.optInt("gamesPlayed");
                    stotalDifference = jsonObject.optInt("totalDifference");
                    System.out.println(sgamesPlayed + " games");
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                if (!response.isSuccessful()) {
                    System.out.println("Unsuccessful");
                }
            }
        });
    }

}

