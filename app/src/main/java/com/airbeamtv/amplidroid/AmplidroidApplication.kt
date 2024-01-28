package com.airbeamtv.amplidroid

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.AWSDataStorePlugin

class AmplidroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            Amplify.addPlugin(AWSDataStorePlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext);
            Log.i("Tutorial", "Initialized Amplify");
        } catch (error: AmplifyException) {
            Log.e("Tutorial", "Could not initialize Amplify", error);
        }
    }
}