package com.kuleuven.agegame;

import androidx.annotation.NonNull;

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

public class Image {
    private String dbURL = "https://studev.groept.be/api/a23pt312/selectImageURL/";
    private String URL;
    private int age;
    private String gameID;
    private int difference;
    private String imageID;
    public Image(String imageID, int difference, String gameID, int age, String URL){
        this.difference = difference;
        this.imageID = imageID;
        this.gameID = gameID;
        this.age = age;
        this.URL = URL;

        //getURL();
    }
    public String getImageID() {
        return imageID;
    }

    public int getDifference() {
        return difference;
    }

    public String getURL(){
        return URL;
    }

    public int getAge(){return age;}

    public String getGameID(){return gameID;}
}
