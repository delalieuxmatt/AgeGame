package com.kuleuven.agegame;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class RegistrationActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    OkHttpClient client;
    Button btnRegister, btnLogin, btnChooseImage;
    EditText edtTxtFirstName, edtTxtSecondName, edtTxtEmail, edtPassWord, edtPassWordAgain;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    String postURL = "https://studev.groept.be/api/a23pt312/Registration_POST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AmplifyAge.initializeAmplify(getApplicationContext());
        setContentView(R.layout.activity_registration);
        client = new OkHttpClient();
        initView();

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    uploadImage(inputStream);
                } catch (FileNotFoundException e) {
                    Log.e("RegistrationActivity", "File not found", e);
                }
            }
        });

        btnChooseImage.setOnClickListener(v -> openFileChooser());
        btnRegister.setOnClickListener(v -> registerUser());

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    private void initView() {
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        edtTxtFirstName = findViewById(R.id.edtTxtFirstName);
        edtTxtSecondName = findViewById(R.id.edtTxtSecondName);
        edtTxtEmail = findViewById(R.id.edtTxtEmail);
        edtPassWord = findViewById(R.id.edtPassWord);
        edtPassWordAgain = findViewById(R.id.edtPassWordAgain); // Initialize registrationStatus TextView
    }

    private void registerUser(){
        String fName = edtTxtFirstName.getText().toString();
        String lName = edtTxtSecondName.getText().toString();
        String email = edtTxtEmail.getText().toString();
        String password = edtPassWord.getText().toString();
        String passwordagain = edtPassWordAgain.getText().toString();
        String errorMsg = "Passwords do not match";
        String errorMsg2 = "Please fill in all fields";

        if (!fName.isEmpty() && !lName.isEmpty() && !email.isEmpty() && !password.isEmpty() && !passwordagain.isEmpty() &&
                passwordagain.equals(password)) {
            RequestBody requestBody = new FormBody.Builder()
                    .add("fn", fName)
                    .add("ln", lName)
                    .add("mail", email)
                    .add("pw",password)
                    .build();

            Request request = new Request.Builder()
                    .url(postURL)
                    .post(requestBody)
                    .build();

            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);

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

        } else {
            if (!passwordagain.equals(password)) {
                Toast.makeText(RegistrationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(RegistrationActivity.this, errorMsg2, Toast.LENGTH_LONG).show();
            }
        }
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
                    "profile.JPG", // Change to the desired key
                    file,
                    result -> {
                        Log.i("RegistrationActivity", "Successfully uploaded: " + result.getKey());
                        // You can do something with the uploaded file if needed
                        // Delete the temporary file after uploading
                        boolean deleted = file.delete();
                        if (!deleted) {
                            Log.e("RegistrationActivity", "Failed to delete temporary file");
                        }
                    },
                    storageFailure -> {
                        Log.e("RegistrationActivity", "Upload failed", storageFailure);
                        // Delete the temporary file in case of failure
                        boolean deleted = file.delete();
                        if (!deleted) {
                            Log.e("RegistrationActivity", "Failed to delete temporary file");
                        }
                    }
            );
        } catch (IOException e) {
            Log.e("RegistrationActivity", "Error creating temporary file", e);
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

}
