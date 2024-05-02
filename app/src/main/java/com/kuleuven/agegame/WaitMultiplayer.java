package com.kuleuven.agegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WaitMultiplayer extends AppCompatActivity {
    private OkHttpClient client;
    private String gameID, creator, userID;
    private Button startGame, btnJoin;
    private ImageButton btnHomeWaitMulti;
    private EditText edtGameID;
    public boolean isCreator = false;
    private String hlMultiplayerPlayer_POST = "https://studev.groept.be/api/a23pt312/hlMultiplayerGuess_POST";
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_waiting);
        UserInfo userInfo = new UserInfo(getApplicationContext());
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
        btnJoin.setOnClickListener(v->joinRoutine());

        //Ensure that if you did not create the game, you cannot start it by hiding the button!
        btnHomeWaitMulti.setOnClickListener(v->redirect(MainActivity.class));
    }
    public void joinRoutine(){
        if(!isCreator){
            gameID = edtGameID.getText().toString();
        }
        edtGameID.setVisibility(View.INVISIBLE);
        RequestBody requestBody = new FormBody.Builder()
                .add("gameid",gameID)
                .add("userid",userID)
                .build();
        Request request = new Request.Builder()
                .url(hlMultiplayerPlayer_POST)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(hlMultiplayerPlayer_POST);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful()){
                    System.out.println("Unsuccessful");
                }
                else {
                    redirect(hlGame.class);

                }
            }
        });
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
        //startGame = findViewById(R.id.startGame);
        edtGameID = findViewById(R.id.edtGameID);
        btnHomeWaitMulti = findViewById(R.id.btnHomeWaitMulti);
        btnJoin = findViewById(R.id.btnJoin);
    }
}
