package com.kuleuven.agegame;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserInfo {
    public String db = "https://studev.groept.be/api/a23pt312/getUserID";

    String responseData;
    /*
    this class contain several methods to write, read or initialize user ID.
     */

    private Context context;
    private File file;

    public UserInfo(Context context){
        this.context = context;
        this.file = new File(context.getFilesDir(), "ID");
    }

    public boolean fileExist(){
        return file.exists();
    }


    public boolean initInfo(){
        if(!file.exists()){
            System.out.println("file doesn't exist");
            try{
                System.out.println("file already exists");
                return file.createNewFile();
            }
            catch(IOException e){
                e.printStackTrace();
                return false;
            }
        }
        else return false;

    }

    public boolean writeInfo(String firstName, String lastName, String email, int dbID){
        if(!file.exists()){
            return false;
        }
        else{
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);

                try {
                    FileWriter writer = new FileWriter(fos.getFD());
                    writer.write(firstName + '\n');
                    writer.write(lastName  + '\n');
                    writer.write(email  + '\n');
                    writer.write(String.valueOf(dbID) + '\n');
                    writer.close();
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    String[] readFile() {
        String[] userInfo = new String[4];
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            Scanner scanner = new Scanner(fis);
            int i = 0;
            while (scanner.hasNextLine() && i < 4) {
                userInfo[i] = scanner.nextLine();
                i++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return userInfo;
    }

    public String getFirstName(){
        return readFile()[0];
    }
    public String getLastName(){
        return readFile()[1];
    }
    public String getEmail(){
        return readFile()[2];
    }
    public String getID(){return readFile()[3];}

    public boolean deleteFile(){
        return file.delete();
    }

}

