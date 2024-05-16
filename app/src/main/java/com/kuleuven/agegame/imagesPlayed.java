package com.kuleuven.agegame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class imagesPlayed extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageButton btnHome3;
    private ImageAdapter adapter;
    private final String url = "https://studev.groept.be/api/a23pt312/selectImageURL";
    private ArrayList<Image> imageList;
    private ArrayList<Integer> ageList;
    private ArrayList<Integer> yourGuessList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagesplayed);
        imageList = new ArrayList<>();
        ageList = new ArrayList<>();
        yourGuessList = new ArrayList<>();
        initView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ImageAdapter(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()); // Initialize with an empty list
        recyclerView.setAdapter(adapter);

        // Load images from URLs
        getImagesPlayed();

        btnHome3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(imagesPlayed.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private synchronized void loadImages(List<String> imageURLs) {
        List<Bitmap> images = new ArrayList<>();

        // Use Glide to load images asynchronously from URLs
        for (String imageURL : imageURLs) {
            Glide.with(this)
                    .asBitmap()
                    .load(imageURL)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            images.add(resource);
                            // Update RecyclerView once all images are loaded
                            if (images.size() == imageURLs.size()) {
                                updateRecyclerView(images);
                            }
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            // Handle placeholder cleanup if needed
                        }
                    });
        }
    }

    private void updateRecyclerView(List<Bitmap> images) {
        ImageAdapter adapter = new ImageAdapter(images, ageList, yourGuessList);
        recyclerView.setAdapter(adapter);
    }

    private void getImagesPlayed() {
        OkHttpClient client = new OkHttpClient();
        UserInfo userInfo = new UserInfo(getApplicationContext());
        String userID = userInfo.getID();
        RequestBody requestBody = new FormBody.Builder()
                .add("userid", userID)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
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
                System.out.println(responseData);
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                        String imageID = jsonObject.optString("imageid");
                        String gameID = jsonObject.optString("gameid");
                        int age = jsonObject.optInt("age");
                        System.out.println(age);
                        String URL = jsonObject.optString("url");
                        int difference = jsonObject.optInt("difference");
                        Image image = new Image(imageID, difference, gameID, age, URL);
                        imageList.add(image);
                        yourGuessList.add(age+difference);
                        ageList.add(age);

                    }
                    runOnUiThread(() -> {
                        List<String> imageURLs = getImageURLs();
                        loadImages(imageURLs);
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private List<String> getImageURLs() {
        // Dummy method to provide a list of image URLs
        List<String> imageURLs = new ArrayList<>();
        for (Image i : imageList) {
            imageURLs.add(i.getURL());
        }
        return imageURLs;
    }

    private void initView(){
        btnHome3 = findViewById(R.id.btnHome3);
        recyclerView = findViewById(R.id.imagesPlayed);
    }
}
