package com.kuleuven.agegame;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;

public class PlayMultiplayer extends AppCompatActivity {
    private OkHttpClient client;
    private String gameID, creator;
    private Button startGame;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_play);
        UserInfo userInfo = new UserInfo(getApplicationContext());
        initView();
        client = new OkHttpClient();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            creator = extras.getString("creator");
        }
        if(!userInfo.getID().equals(creator)){
           startGame.setVisibility(View.INVISIBLE);
        }
        //Ensure that if you did not create the game, you cannot start it!
    }

    public void initView(){
        startGame = findViewById(R.id.startGame);
    }

    public void getGameID(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            gameID = extras.getString("gameID");
        } else {
            gameID = "";
        }
    }
}
