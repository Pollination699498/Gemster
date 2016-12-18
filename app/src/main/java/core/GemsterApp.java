package core;

import android.app.Application;

/**
 * Created by WONSEOK OH on 2016-12-17.
 */

public class GemsterApp extends Application {
    private GoogleApiHelper mGoogleApiHelper;

    private static GemsterApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized GemsterApp getInstance() {
        return mInstance;
    }

    public void setClient(GoogleApiHelper client) {
        mGoogleApiHelper = client;
    }

    public GoogleApiHelper getClient() {
        return mGoogleApiHelper;
    }
}