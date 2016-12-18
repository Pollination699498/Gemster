package ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import pollinationp.gemster.R;

public class MainActivity extends Activity {

    private final String TAG = "MainActivity";

    private MainManager mMainManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainManager = new MainManager(this);
    }

    @Override
    protected void onUserLeaveHint() {
        mMainManager.onUserLeaveHint();
        super.onUserLeaveHint();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMainManager.handleStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMainManager.handleStop();
    }

    @Override
    protected void onResume() {
        mMainManager.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMainManager.pause();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mMainManager.handleActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onBackPressed() {
        if (mMainManager.backPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
