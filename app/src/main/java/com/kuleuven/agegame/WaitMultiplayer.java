package com.kuleuven.agegame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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

public class WaitMultiplayer extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private OkHttpClient client;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private String gameID, creator, status, userID, rounds, timeLimit;
    private Button btnStartGame, btnJoin;
    private LocalTime startTime;
    private ImageButton btnHomeWaitMulti;
    private EditText edtGameID;
    public boolean isCreator = false, isFirst = true;
    private String getStatus = "https://studev.groept.be/api/a23pt312/getMultiGameStatus";

    private final String setStatus = "https://studev.groept.be/api/a23pt312/setMultiGameStatus/";
    private UserInfo userInfo;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_waiting);
        userInfo = new UserInfo(getApplicationContext());
        userID = userInfo.getID();
        initView();
        client = new OkHttpClient();
        Bundle extras = getIntent().getExtras();
        //We check if there are extras, if there are this means that this person
        //Is the creator, isCreator is set as true
        //We get the gameID from the extras and hide the UI components that are
        //not necessary, retrieve the creator's user ID
        System.out.println(extras);
        if (extras != null) {
            isCreator = true;
            hide();
            gameID = extras.getString("gameID");
            creator = extras.getString("creator");
            System.out.println("Creator is: " + creator);
            System.out.println("Game ID is: " + gameID);
            String msg = "The gameID is: " + gameID + ". Inform your friends! Once you are ready, press start game ";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
        else{
            btnStartGame.setVisibility(View.INVISIBLE);
        }
        btnJoin.setOnClickListener(v->joinRoutine());

        btnStartGame.setOnClickListener(v->startGame()); //fix this method

        //Ensure that if you did not create the game, you cannot start it by hiding the button!
        btnHomeWaitMulti.setOnClickListener(v->redirect(MainActivity.class));
    }

    public void joinRoutine(){
        if(!isCreator){
            gameID = edtGameID.getText().toString();
        }
        if(!isFirst){
            String msg = "The game has not started yet, we will check again in 5s";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
        hide();
        RequestBody requestBody = new FormBody.Builder()
                .add("gameid",gameID)
                .build();
        Request request = new Request.Builder()
                .url(getStatus)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    status = jsonObject.optString("started");
                    String dbTime = jsonObject.optString("startTime");
                    startTime = LocalTime.parse(dbTime, formatter);
                    rounds = jsonObject.optString("rounds");
                    timeLimit = jsonObject.optString("timeLimit");
                    if(status.equals("1")){
                        Intent intent = new Intent(WaitMultiplayer.this, hlMultiGame.class);
                        //Here we make sure that if you are the one that created the game, that the gameID gets transferred over!
                        System.out.println("Testing GAME ID: " + gameID);
                        intent.putExtra("gameID",gameID);
                        intent.putExtra("rounds", rounds);
                        intent.putExtra("timeLimit", timeLimit);
                        intent.putExtra("startTime", startTime);
                        intent.putExtra("creator", creator);
                        startActivity(intent);
                    } else {
                        isFirst = false;
                        // Schedule the next check after 5 seconds
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                joinRoutine();
                            }
                        }, 5000);
                    }

                    // Now you have the gameID, you can use it as needed
                    System.out.println("The game status is (0/not started, 1/started: " + status);

                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println("Error parsing JSON response.");
                }
            }
        });
    }

    public void startGame(){
        RequestBody requestBody = new FormBody.Builder()
                .add("gameid", gameID)
                .add("started", "1")
                .build();
        userInfo.enqPost(setStatus, requestBody);
    }

    public void hide(){
        edtGameID.setVisibility(View.INVISIBLE);
        btnJoin.setVisibility(View.INVISIBLE);
    }
    public void redirect(Class<?> nextLocation){
        Intent intent = new Intent(this, nextLocation);
        startActivity(intent);
    }

    public void initView(){
        btnStartGame = findViewById(R.id.btnStartGame);
        edtGameID = findViewById(R.id.edtGameID);
        btnHomeWaitMulti = findViewById(R.id.btnHomeWaitMulti);
        btnJoin = findViewById(R.id.btnJoin);
    }
}
