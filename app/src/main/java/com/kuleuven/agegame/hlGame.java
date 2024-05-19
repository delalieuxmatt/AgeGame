package com.kuleuven.agegame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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

public class hlGame extends AppCompatActivity {

    OkHttpClient client;
    private Button buttonYounger, buttonOlder;
    private ImageView imgknown, imgunknown;
    private ImageButton btnHome;
    private String imgDB = "https://studev.groept.be/api/a23pt312/randomImage";
    private String guessPOST = "https://studev.groept.be/api/a23pt312/hlGuess_POST";
    private String hlgamePost = "https://studev.groept.be/api/a23pt312/hlGame_POST";
    private String gameIDGetter = "https://studev.groept.be/api/a23pt312/getGameID";
    private String gameID, userID;
    private int agefirst, imageIDfirst;
    private int agesecond, imageIDsecond;
    private String imageURLfirst, imageURLsecond;
    private TextView txtAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hl_game);
        initView();
        client = new OkHttpClient();

        UserInfo userInfo = new UserInfo(getApplicationContext());
        userID = userInfo.getID();
        createGame();
        getGameID();
    }

    private void createGame() {
        RequestBody requestBody = new FormBody.Builder()
                .add("userid", userID)
                .build();
        Request request = new Request.Builder()
                .url(hlgamePost)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(hlgamePost);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println("Unsuccessful");
                } else {
                    System.out.println("Game created: " + response.body().string());
                }
            }
        });
    }

    private void getGameID() {
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
                System.out.println("gameIDGetter failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    gameID = jsonObject.optString("gameID");
                    System.out.println("gameID: " + gameID);
                    loadInitialImages();
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println("Error parsing JSON response.");
                }
            }
        });
    }

    private void loadInitialImages() {
        // Load the initial images for the first round
        loadFirstImage();
    }

    private void loadFirstImage() {
        Request request = new Request.Builder()
                .url(imgDB)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(imgDB);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println("Unsuccessful image request");
                } else {
                    String responseData = response.body().string();
                    parseFirstImage(responseData);
                }
            }
        });
    }

    private void parseFirstImage(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            imageIDfirst = jsonObject.optInt("imageID");
            imageURLfirst = jsonObject.optString("image").replace("\\/", "/");
            agefirst = jsonObject.optInt("age");

            runOnUiThread(() -> {
                txtAge.setText(String.valueOf(agefirst));
                Glide.with(hlGame.this)
                        .load(imageURLfirst)
                        .into(imgknown);

                // Load the second image after the first one is displayed
                loadSecondImage();
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void loadSecondImage() {
        Request request = new Request.Builder()
                .url(imgDB)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("loadSecondImage", "Failed to load second image: " + imgDB);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("loadSecondImage", "Unsuccessful image request");
                } else {
                    String responseData = response.body().string();
                    parseSecondImage(responseData);
                }
            }
        });
    }

    private void parseSecondImage(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            imageIDsecond = jsonObject.optInt("imageID");
            imageURLsecond = jsonObject.optString("image").replace("\\/", "/");
            agesecond = jsonObject.optInt("age");

            runOnUiThread(() -> {
                Glide.with(hlGame.this)
                        .load(imageURLsecond)
                        .into(imgunknown);
                Log.d("parseSecondImage", "Second image loaded with age: " + agesecond);
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void handleGuess(boolean guessedOlder) {
        boolean correctGuess = (guessedOlder && agesecond >= agefirst) || (!guessedOlder && agesecond <= agefirst);
        if (correctGuess) {
            Toast.makeText(this, "Correct!" + agesecond, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! The correct answer was " + agesecond, Toast.LENGTH_SHORT).show();
        }

        recordGuess(correctGuess);

        // Start the next round after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(this::startNextRound, 2000);
    }

    private void startNextRound() {
        // Set the second image of the current round as the first image of the next round
        System.out.println("Image IDs were:" + imageIDfirst + " " +  imageIDsecond);
        imageIDfirst = imageIDsecond;
        imageURLfirst = imageURLsecond;
        agefirst = agesecond;

        // Display the known image
        Log.d("parseFirstImage", "Setting age: " + agefirst);
        txtAge.setText(String.valueOf(agefirst));

        Glide.with(hlGame.this)
                .load(imageURLfirst)
                .into(imgknown);

        // Load a new second image
        loadSecondImage();
    }

    private void recordGuess(boolean correctGuess) {
        RequestBody requestBody = new FormBody.Builder()
                .add("gameid", gameID)
                .add("imageidone", String.valueOf(imageIDfirst))
                .add("imageidtwo", String.valueOf(imageIDsecond))
                .add("correct", String.valueOf(correctGuess))
                .build();
        Request request = new Request.Builder()
                .url(guessPOST)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(guessPOST);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println("Unsuccessful guess submission");
                } else {
                    System.out.println("Guess submitted: " + response.body().string());
                    //System.out.println("Image IDs were:" + imageIDfirst + " " +  imageIDsecond);
                }
            }
        });
    }
    public void redirect(Class<?> nextLocation){
        Intent intent = new Intent(this, nextLocation);
        startActivity(intent);
    }

    private void initView() {
        imgknown = findViewById(R.id.imagefirst);
        imgunknown = findViewById(R.id.imagesecond);
        buttonOlder = findViewById(R.id.buttonOlder);
        buttonYounger = findViewById(R.id.buttonYounger);
        btnHome = findViewById(R.id.btnHome);
        txtAge = findViewById(R.id.txtAge);

        buttonOlder.setOnClickListener(v -> handleGuess(true));
        buttonYounger.setOnClickListener(v -> handleGuess(false));
        btnHome.setOnClickListener(v->redirect(MainActivity.class));
    }
}