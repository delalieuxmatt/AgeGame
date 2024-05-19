package com.kuleuven.agegame;

import static java.lang.Math.abs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;

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

public class singleGame extends AppCompatActivity {
    OkHttpClient client;
    public boolean flag = true;
    private String standardGuess_POST = "https://studev.groept.be/api/a23pt312/standardGuess_POST";
    private Button btnGuess, btnNewGame;
    private ImageButton btnHome;
    private EditText textGuess;
    private ImageView imgPerson;
    private int roundsWon;
    private String imageURL;
    private int age, imageID;
    private String imgDB = "https://studev.groept.be/api/a23pt312/randomImage";
    private String standardGame_POST = "https://studev.groept.be/api/a23pt312/standardGame_POST";
    private String gameIDGetter = "https://studev.groept.be/api/a23pt312/getGameID";
    private String correctMsg = "Well done, you got the age correct! You have won: " ;
    private String wrongMsg = "You got that one wrong, your streak was reset. The answer was: " ;
    private String nextMsg = "Next";
    private String guessMsg = "Guess";
    private String gameID, userID;
    private UserInfo userInfo;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_game);
        initView();
        client = new OkHttpClient();
        imageGetter();

        //Initiate the game!!!
        userInfo = new UserInfo(getApplicationContext());
        userID = userInfo.getID();
        RequestBody requestBody = new FormBody.Builder()
                .add("userid",userID)
                .build();
        userInfo.enqPost(standardGame_POST, requestBody);
        //We now look for the game id of the game we have just created so it can be used throughout the code
        gameIDGetter();

        btnGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!textGuess.getText().toString().isEmpty()){
                    int guess = Integer.parseInt(textGuess.getText().toString());
                    flag = false;
                    btnGuess.setText(nextMsg);
                    textGuess.setVisibility(View.INVISIBLE);
                    textGuess.setText("");
                    int difference = age-guess;
                    if (guess == age){
                        roundsWon++;
                        Toast.makeText(singleGame.this, correctMsg + roundsWon + " rounds", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(singleGame.this, wrongMsg + age, Toast.LENGTH_LONG).show();
                    }
                    System.out.println(gameID);
                    RequestBody requestBody = new FormBody.Builder()
                            .add("gameid", gameID)
                            .add("imageid",String.valueOf(imageID))
                            .add("diff",String.valueOf(difference))
                            .build();
                    Request request = new Request.Builder()
                            .url(standardGuess_POST)
                            .post(requestBody)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                            System.out.println(standardGuess_POST);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if(!response.isSuccessful()){
                                System.out.println("Unsuccessful");
                            }
                            else {
                                System.out.println(request);
                            }
                        }
                    });

                }
                else {
                    if(btnGuess.getText().equals(nextMsg)) {
                        btnGuess.setText(guessMsg);
                        textGuess.setVisibility(View.VISIBLE);
                        imageGetter();
                    }
                    else{
                        Toast.makeText(singleGame.this, "Please enter data before clicking next", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        //New game means we just start a new game, not much really happens only creating a new game which is handled
        //By an intent already. Simply call an intent

        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(singleGame.this,singleGame.class);
                startActivity(intent);
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(singleGame.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void imageGetter(){
        Request request = new Request.Builder()
                .url(imgDB)
                .get()
                .build();
        //Here we already switch the button to the guessing phase instead of starting a new round
        //The else will be executed next

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(imgDB);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    imageID = jsonObject.optInt("imageID");
                    imageURL = jsonObject.optString("image").replace("\\/", "/");
                    age = jsonObject.optInt("age");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(singleGame.this)
                                    .load(imageURL)
                                    .into(imgPerson);
                            System.out.println(imageURL);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void initView() {
        imgPerson = findViewById(R.id.imgPerson);
        btnGuess = findViewById(R.id.btnGuess);
        textGuess = findViewById(R.id.textGuess);
        btnNewGame = findViewById(R.id.btnNewGame);
        btnHome = findViewById(R.id.btnHomeWaitMulti);

    }

    private void gameIDGetter(){
        RequestBody requestBody = new FormBody.Builder()
                .add("userid", userID)
                .build();
        Request request = new Request.Builder()
                .url(gameIDGetter)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println("gameID is:" + gameIDGetter);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    gameID = jsonObject.optString("gameID");
                    // Now you have the gameID, you can use it as needed
                    // For example, you can pass it to another method or store it in a variable
                    System.out.println("gameID: " + gameID);

                } catch (JSONException e) {
                    // Handle JSON parsing error
                    e.printStackTrace();
                    System.out.println("Error parsing JSON response.");
                }
            }
        });
    }
}


