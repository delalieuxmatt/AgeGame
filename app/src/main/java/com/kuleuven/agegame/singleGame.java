package com.kuleuven.agegame;

import static java.lang.Math.abs;

import android.content.Intent;
import android.os.Bundle;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

public class singleGame extends AppCompatActivity {
    OkHttpClient client;
    public boolean flag;

    private Button btnGuess, btnNewGame;
    private EditText textGuess;
    private ImageView imgPerson;
    private int roundsPlayed,roundsWon,totalDifference;
    private String imageURL;
    private int age;
    private String imgDB = "";
    private String statsDB = "";
    private String gamesDB = "";
    private String correctMsg = "Well done, you got the age correct! You have won: " ;
    private String wrongMsg = "You got that one wrong, your streak was reset. The answer was: " ;
    private String failUpload = "failed to upload to games database";
    private String nextMsg = "Next";
    private String guessMsg = "Guess";
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_game);
        initView();
        client = new OkHttpClient();
        btnGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int guess = Integer.parseInt(textGuess.getText().toString());
                if(!flag) {
                    //We are now in the mind of starting a new game, an image is pulled from the db
                    //Meaning that the text on the button should now be "Guess" instead of "Next"
                    btnGuess.setText(guessMsg);
                    roundsPlayed++;
                    Request request = new Request.Builder()
                            .url(imgDB)
                            .get()
                            .build();
                    //Here we already switch the button to the guessing phase instead of starting a new round
                    //The else will be executed next
                    flag = !flag;
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
                                imageURL = jsonObject.optString("imageURL");
                                age = jsonObject.optInt("age");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                //This is the code that is executed when the new round of the game has already started
                //It will ensure that when text is entered, compared to the answer, that there is a response and saving of the data
                else {
                    btnGuess.setText(nextMsg);
                    if (guess == age){
                        roundsWon++;
                        Toast.makeText(singleGame.this, correctMsg + roundsWon + " rounds", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(singleGame.this, wrongMsg + age, Toast.LENGTH_LONG).show();
                    }
                    totalDifference += abs(age-guess);
                }

            }
        });

        //New game means reset of the scores of the game after uploading the scores into the database

        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfo userInfo = new UserInfo(getApplicationContext());
                String email = userInfo.getEmail();
                RequestBody requestBody = new FormBody.Builder()
                        .add("played",String.valueOf(roundsPlayed))
                        .add("won",String.valueOf(roundsWon))
                        .add("totDifference",String.valueOf(totalDifference))
                        .add("email",email)
                        .build();

                Request request = new Request.Builder()
                        .url(gamesDB)
                        .post(requestBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        System.out.println(gamesDB);
                        Toast.makeText(singleGame.this, failUpload, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        if (!response.isSuccessful()) {
                            System.out.println("Unsuccessful");
                        }
                    }
                });
                Intent intent = new Intent(singleGame.this,singleGame.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        imgPerson = findViewById(R.id.imgPerson);
        btnGuess = findViewById(R.id.btnGuess);
        textGuess = findViewById(R.id.textGuess);
        btnNewGame = findViewById(R.id.btnNewGame);

    }
}


