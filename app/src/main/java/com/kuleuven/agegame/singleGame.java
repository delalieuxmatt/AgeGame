package com.kuleuven.agegame;

import static java.lang.Math.abs;

import android.content.Intent;
import android.os.Bundle;
import android.view.PixelCopy;
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

    private Button btnGuess, btnNewGame;
    private ImageButton btnHome;
    private EditText textGuess;
    private ImageView imgPerson;
    private int roundsPlayed,roundsWon,totalDifference;
    private String imageURL;
    private int age = 9;
    private int guess;
    private String imgDB = "https://studev.groept.be/api/a23pt312/img";
    private String statsDB = "https://studev.groept.be/api/a23pt312/Stats";
    private String gamesDB = "https://studev.groept.be/api/a23pt312/Games_POST";
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
        imageGetter();
        btnGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!textGuess.getText().toString().isEmpty()){
                    int guess = Integer.parseInt(textGuess.getText().toString());
                    flag = false;
                    btnGuess.setText(nextMsg);
                    textGuess.setText("");
                    if (guess == age){
                        roundsWon++;
                        Toast.makeText(singleGame.this, correctMsg + roundsWon + " rounds", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(singleGame.this, wrongMsg + age, Toast.LENGTH_LONG).show();
                    }
                    totalDifference += abs(age-guess);

                }
                else {
                    btnGuess.setText(guessMsg);
                    roundsPlayed++;
                    imageGetter();
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
                        .add("w", String.valueOf(roundsWon))
                        .add("pl", String.valueOf(roundsPlayed))
                        .add("diff", String.valueOf(totalDifference))
                        .add("email", email)
                        .build();
                System.out.println(String.valueOf(roundsWon));
                System.out.println(String.valueOf(roundsPlayed));
                System.out.println(String.valueOf(totalDifference));
                System.out.println(email);
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
                    imageURL = jsonObject.optString("image").replace("\\/", "/");;
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
        btnHome = findViewById(R.id.btnHome);

    }
}


