package com.kuleuven.agegame;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;

public class AmplifyAge extends Application {
    public static void initializeAmplify(Context context) {
        try {
            // Add plugins
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSS3StoragePlugin());

            // Configure Amplify
            Amplify.configure(context);

            Log.i("AmplifyInitializer", "Initialized Amplify");
        } catch (AmplifyException error) {
            Log.e("AmplifyInitializer", "Could not initialize Amplify", error);
        }
    }

}