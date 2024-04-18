package com.kuleuven.agegame;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.core.Amplify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AmplifyTestPage extends AppCompatActivity {
    ImageView testImg;
    EditText enterAge, enterFileName;
    Button btnImg;
    ImageButton btnHome;
    OkHttpClient client;
    String fileName, age;
    String errorMsg = "Please enter a file name and an age";
    String postURL = "https://studev.groept.be/api/a23pt312/image_POST";
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AmplifyAge.initializeAmplify(getApplicationContext());
        setContentView(R.layout.activity_amplifytest);
        client = new OkHttpClient();
        initView();
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                try {
                    age = enterAge.getText().toString();
                    fileName = enterFileName.getText().toString();
                    if(!age.isEmpty() && !fileName.isEmpty()){
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        uploadImage(inputStream);
                    }
                    else{
                        Toast.makeText(AmplifyTestPage.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (FileNotFoundException e) {
                    Log.e("RegistrationActivity", "File not found", e);
                }
            }
        });
        btnImg.setOnClickListener(v -> openFileChooser());
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AmplifyTestPage.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(intent); // Launch the image picker
    }

    private void uploadImage(InputStream inputStream) {
        try {
            // Create a temporary file
            File file = createTempFile(inputStream);

            // Upload the file to Amplify Storage
            Amplify.Storage.uploadFile(
                    fileName, // Change to the desired key
                    file,
                    result -> {
                        String imageURL = "https://buckettest7106f-dev.s3.eu-west-2.amazonaws.com/"+result.getKey();
                        System.out.println(imageURL);
                        addToDB(imageURL);
                        Log.i("Amplify", "Successfully uploaded: " + result.getKey());
                        // You can do something with the uploaded file if needed
                        // Delete the temporary file after uploading
                        boolean deleted = file.delete();
                        if (!deleted) {
                            Log.e("Amplify", "Failed to delete temporary file");
                        }
                    },
                    storageFailure -> {
                        Log.e("Amplify", "Upload failed", storageFailure);
                        // Delete the temporary file in case of failure
                        boolean deleted = file.delete();
                        if (!deleted) {
                            Log.e("Amplify", "Failed to delete temporary file");
                        }
                    }
            );
        } catch (IOException e) {
            Log.e("Amplify", "Error creating temporary file", e);
        }  finally {
        try {
            // Close the input stream
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e("Amplify", "Error closing input stream", e);
        }
    }
    }

    private void addToDB(String imageURL){
        String age = enterAge.getText().toString();
        if(!age.isEmpty() && !imageURL.isEmpty()){
            System.out.println("Not empty");
            RequestBody requestBody = new FormBody.Builder()
                    .add("url", imageURL)
                    .add("age", age)
                    .build();
            System.out.println(requestBody);
            Request request = new Request.Builder()
                    .url(postURL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    System.out.println(postURL);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        System.out.println("Unsuccessful");
                    }
                }
            });
        }


    }

    private File createTempFile(InputStream inputStream) throws IOException {
        // Create a temporary file
        File file = File.createTempFile("temp_image", ".JPG", getCacheDir());

        // Write the InputStream data to the file
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();

        return file;
    }

    private void initView(){
        testImg = findViewById(R.id.testImg);
        btnImg = findViewById(R.id.btnImg);
        enterAge = findViewById(R.id.enterAge);
        btnHome = findViewById(R.id.btnHome);
        enterFileName = findViewById(R.id.enterFileName);
    }

}
