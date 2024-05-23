package com.kuleuven.agegame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

public class ResultsMultiplayer extends AppCompatActivity {
    private TextView txtFirst, txtSecond, txtThird, scoreFirst, scoreSecond, scoreThird;
    private String gameID, first, second, third, score1, score2, score3;
    private String placements = "https://studev.groept.be/api/a23pt312/hlMultiplayerStatistics";
    private OkHttpClient client;
    private ImageButton btnHome;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_results);
        initView();
        client = new OkHttpClient();
        Bundle extras = getIntent().getExtras();
        gameID = extras.getString("gameID");
        System.out.println(gameID);
        getPlacements();

    }
    public void initView(){
        txtFirst = findViewById(R.id.txtFirst);
        txtSecond = findViewById(R.id.txtSecond);
        txtThird = findViewById(R.id.txtThird);
        btnHome = findViewById(R.id.btnHome);
        scoreFirst = findViewById(R.id.scoreFirst);
        scoreSecond = findViewById(R.id.scoreSecond);
        scoreThird = findViewById(R.id.scoreThird);

        btnHome.setOnClickListener(v->redirect(MainActivity.class));
    }

    private void redirect(Class<?> nextLocation){
        Intent intent = new Intent(this, nextLocation);
        startActivity(intent);
    }

    private void getPlacements(){
        RequestBody requestBody = new FormBody.Builder()
                .add("gameid", gameID)
                .add("gameid", gameID)
                .build();
        Request request = new Request.Builder()
                .url(placements)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("loadSecondImage", "Unsuccessful image request");
                } else {
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        first = jsonObject.optString("FullName");
                        score1 = jsonObject.optString("correct_count");
                        if(jsonArray.length() > 1){
                            jsonObject = jsonArray.getJSONObject(1);
                            second = jsonObject.optString("FullName");
                            score2 = jsonObject.optString("correct_count");
                        }
                        if(jsonArray.length()>2){
                            jsonObject = jsonArray.getJSONObject(2);
                            third = jsonObject.optString("FullName");
                            score3 = jsonObject.optString("correct_count");
                        }
                        runOnUiThread(() -> {
                            if(first!=null){txtFirst.setText(first); scoreFirst.setText(score1);}
                            if(second!=null){txtSecond.setText(second); scoreSecond.setText(score2);}
                            if(third!=null){txtThird.setText(third); scoreThird.setText(score3);}
                        });

                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
