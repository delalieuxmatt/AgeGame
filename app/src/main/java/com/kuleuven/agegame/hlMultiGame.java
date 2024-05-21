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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class hlMultiGame extends AppCompatActivity {

    OkHttpClient client;
    private Button buttonYounger, buttonOlder, btnStartRound;
    private ImageView imgknown, imgunknown;
    private ImageButton btnHome;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String twoImgDB = "https://studev.groept.be/api/a23pt312/twoRandomImagesInfo";
    private final String guessPOST = "https://studev.groept.be/api/a23pt312/hlMultiplayerGuess_POST";
    private String gameID, userID, imageURLfirst, imageURLsecond, rounds, timeLimit, creator;
    private int agefirst, imageIDfirst, agesecond, imageIDsecond;
    private int roundsCtr = 0;
    private TextView txtAge, txtPersonIs;
    private LocalTime startTime;
    private boolean participating = true;
    private UserInfo userInfo;
    private int prevImageID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hl_multi_game);
        initView();
        client = new OkHttpClient();

        userInfo = new UserInfo(getApplicationContext());
        userID = userInfo.getID();

        Bundle extras = getIntent().getExtras();
        gameID = extras.getString("gameID");
        rounds = extras.getString("rounds");
        timeLimit = extras.getString("timeLimit");
        String time = extras.getString("startTime");
        System.out.println(time);
        creator = extras.getString("creator");
        startTime = LocalTime.parse(time, formatter);
        if(!userID.equals(creator)){btnStartRound.setVisibility(View.INVISIBLE);}
        //This will use the round created by the WaitMultiplayer class
        loadInitialImages();
    }



    private void loadInitialImages() {
        // Load the initial images for the first round
        loadImages();
        if(!participating){
            buttonOlder.setVisibility(View.INVISIBLE);
            buttonYounger.setVisibility(View.INVISIBLE);
        }
    }

    private void handleGuess(boolean guessedOlder) {
        boolean correctGuess = (guessedOlder && agesecond >= agefirst) || (!guessedOlder && agesecond <= agefirst);
        if (correctGuess) {
            Toast.makeText(this, "Correct!" + agesecond, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! The correct answer was " + agesecond, Toast.LENGTH_SHORT).show();
            participating = false;
        }
        recordGuess(correctGuess);
        buttonOlder.setVisibility(View.INVISIBLE);
        buttonYounger.setVisibility(View.INVISIBLE);
        //At the end of the round, we check if the current user is the one who created the
        //round, if so, they will generate the next round
        //This method will also trigger the start of the next round after a short delay

    }


    private void recordGuess(boolean correctGuess) {
        System.out.println("Apparently null:" + userID);
        String newmsg = agesecond + " years old";
        txtPersonIs.setText(newmsg);
        RequestBody requestBody = new FormBody.Builder()
                .add("gameid", gameID)
                .add("userid", userID)
                .add("correct", String.valueOf(correctGuess))
                .build();
        userInfo.enqPost(guessPOST, requestBody);
    }



    private void loadImages() {
        txtPersonIs.setText("This person is: ");
        RequestBody requestBody = new FormBody.Builder()
                .add("gameid", gameID)
                .build();
        Request request = new Request.Builder()
                .url(twoImgDB)
                .post(requestBody)
                .build();
        System.out.println(twoImgDB);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("loadSecondImage", "Failed to load second image: " + twoImgDB);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("loadSecondImage", "Unsuccessful image request");
                } else {
                    String responseData = response.body().string();
                    parseImages(responseData);
                }
            }
        });
    }

    //called when page loads
    private void parseImages(String responseData) {
        try {
            roundsCtr++;
            JSONArray jsonArray = new JSONArray(responseData);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            imageIDfirst = jsonObject.optInt("imageID1");
            imageURLfirst = jsonObject.optString("image1").replace("\\/", "/");
            agefirst = jsonObject.optInt("age1");
            imageIDsecond = jsonObject.optInt("imageID2");
            imageURLsecond = jsonObject.optString("image2").replace("\\/", "/");
            agesecond = jsonObject.optInt("age2");


            runOnUiThread(() -> {
                txtAge.setText(String.valueOf(agefirst));
                Glide.with(hlMultiGame.this)
                        .load(imageURLfirst)
                        .into(imgknown);
                Glide.with(hlMultiGame.this)
                        .load(imageURLsecond)
                        .into(imgunknown);
                Log.d("parseFirstImage", "First image loaded with age: " + agefirst);
                Log.d("parseSecondImage", "Second image loaded with age: " + agesecond);

                if(participating){
                    buttonOlder.setVisibility(View.VISIBLE);
                    buttonYounger.setVisibility(View.VISIBLE);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void generateRound(){
        String url = "https://studev.groept.be/api/a23pt312/newRandomImage";
        String hlMultiplayerRound = "https://studev.groept.be/api/a23pt312/hlMultiplayerRound_POST";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(url);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                if (!response.isSuccessful()) {
                    System.out.println("Unsuccessful on " + url);
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        int imageIDFirst = imageIDsecond;
                        int imageIDSecond = jsonObject.optInt("imageID");
                        //JSONObject jsonObject2 = jsonArray.getJSONObject(1);
                        //int imageIDSecond = jsonObject2.optInt("imageID");
                        System.out.println(imageIDFirst + " testing image IDs! " + imageIDSecond);
                        //Now we add the round to the rounds table!
                        RequestBody requestBody = new FormBody.Builder()
                                .add("gameid", gameID)
                                .add("imageidone", String.valueOf(imageIDFirst))
                                .add("imageidtwo", String.valueOf(imageIDSecond))
                                .build();
                        enqueuePost(hlMultiplayerRound, requestBody);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void enqueuePost(String url, RequestBody rb){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(rb)
                .build();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(hlMultiGame.this, "New round being loaded", Toast.LENGTH_SHORT).show();
            }
        });
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(url);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println("Unsuccessful on " + url);
                } else {
                    System.out.println("Success on " + url +  " with response: " + response.body().string());
                    new Handler(Looper.getMainLooper()).postDelayed(() -> loadImages(), 2000);


                }
            }
        });
    }

    public void roundStarter(){
        if(roundsCtr < Integer.parseInt(rounds)){
            generateRound();
        }
        else{
            Toast.makeText(this, "The game is over, connecting you to the results page!", Toast.LENGTH_SHORT).show();
            btnStartRound.setVisibility(View.INVISIBLE);
            redirect(ResultsMultiplayer.class);
        }
        System.out.println("Currently on round " + roundsCtr + "/" + rounds);
    }

    private void redirect(Class<?> nextLocation){
        Intent intent = new Intent(this, nextLocation);
        intent.putExtra("gameID",gameID);
        startActivity(intent);
    }

    private void initView() {
        imgknown = findViewById(R.id.imagefirst);
        imgunknown = findViewById(R.id.imagesecond);
        buttonOlder = findViewById(R.id.buttonOlder);
        buttonYounger = findViewById(R.id.buttonYounger);
        btnHome = findViewById(R.id.btnHome);
        txtAge = findViewById(R.id.txtAge);
        btnStartRound = findViewById(R.id.btnStartRound);
        txtPersonIs = findViewById(R.id.txtPersonIs);

        buttonOlder.setOnClickListener(v -> handleGuess(true));
        buttonYounger.setOnClickListener(v -> handleGuess(false));
        btnHome.setOnClickListener(v->redirect(MainActivity.class));
        btnStartRound.setOnClickListener(v->roundStarter());
    }



}