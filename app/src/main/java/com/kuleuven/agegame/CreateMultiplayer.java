package com.kuleuven.agegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateMultiplayer extends AppCompatActivity {
    private Button btnCreateGame, btnJoinGame;
    private ImageButton btnHomeCreateMulti;
    private EditText edtTimeLimit, edtRounds;
    public String gameID, creatorID, formattedTime;
    private OkHttpClient client;

    private String gameIDGetter = "https://studev.groept.be/api/a23pt312/getMultiGameID";
    private String hlMultiplayerGames_POST = "https://studev.groept.be/api/a23pt312/hlMultiplayerGames_POST";
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_create);
        UserInfo userInfo = new UserInfo(getApplicationContext());
        initView();
        btnJoinGame.setVisibility(View.INVISIBLE);
        creatorID = userInfo.getID();
        client = new OkHttpClient();
        btnCreateGame.setOnClickListener(v->createGame());
        btnHomeCreateMulti.setOnClickListener(v->redirect(MainActivity.class));
        btnJoinGame.setOnClickListener(v->redirect2(WaitMultiplayer.class));
    }
    private void createGame(){
        String timeLimit = edtTimeLimit.getText().toString();
        String rounds = edtRounds.getText().toString();
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        formattedTime = time.format(formatter);
        RequestBody requestBody = new FormBody.Builder()
                .add("timelimit", timeLimit)
                .add("creatorid", creatorID)
                .add("rounds",rounds)
                .add("starttime", formattedTime)
                .build();
        Request request = new Request.Builder()
                .url(hlMultiplayerGames_POST)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(hlMultiplayerGames_POST);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful()){
                    System.out.println("Unsuccessful");
                }
                else {
                    gameIDGetter();
                    System.out.println(hlMultiplayerGames_POST);
                }
            }
        });
        btnCreateGame.setVisibility(View.INVISIBLE);
        btnJoinGame.setVisibility(View.VISIBLE);
    }
    //We have created the game, if it is successful you get sent to the gamePlay page




    private void gameIDGetter(){
        RequestBody requestBody = new FormBody.Builder()
                .add("creatorid", creatorID)
                .build();
        Request request = new Request.Builder()
                .url(gameIDGetter)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(gameIDGetter);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    //JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray jsonArray = new JSONArray(responseData);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    gameID = jsonObject.optString("gameID");

                    // Now you have the gameID, you can use it as needed
                    System.out.println("gameID: " + gameID);

                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println("Error parsing JSON response.");
                }
            }
        });
    }

    public void redirect2(Class<?> nextLocation){
        Intent intent = new Intent(this, nextLocation);
        //Here we make sure that if you are the one that created the game, that the gameID gets transferred over!
        System.out.println("Testing GAME ID: " + gameID);
        intent.putExtra("gameID",gameID);
        intent.putExtra("creator", creatorID);
        startActivity(intent);
    }

    public void redirect(Class<?> nextLocation){
        Intent intent = new Intent(this, nextLocation);
        startActivity(intent);
    }
    private void initView(){
        btnCreateGame = findViewById(R.id.btnCreateGame);
        edtTimeLimit = findViewById(R.id.edtTimeLimit);
        btnHomeCreateMulti = findViewById(R.id.btnHomeCreateMulti);
        btnJoinGame = findViewById(R.id.btnJoinGame);
        edtRounds = findViewById(R.id.edtRounds);
    }
}
