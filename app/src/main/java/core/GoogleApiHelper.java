package core;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.Player;

/**
 * Created by WONSEOK OH on 2016-12-16.
 */

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = GoogleApiHelper.class.getSimpleName();

    private Activity mActivity;

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // request codes we use when invoking an external activity
    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

    public GoogleApiHelper(Activity activity) {
        mActivity = activity;

        // Create the Google API Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
    }

    public void connect() {
        Log.d(TAG, "onStart(): connecting");
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        Log.d(TAG, "onStop(): disconnecting");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void signOut() {
        Log.d(TAG, "signOut");
        Games.signOut(mGoogleApiClient);
        disconnect();
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    public boolean handleActivityResult(int requestCode, int resultCode) {
        if (requestCode == RC_SIGN_IN) {
            mResolvingConnectionFailure = false;
            if (resultCode == Activity.RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                showActivityResultError(mActivity, requestCode, resultCode, "signin_other_error");
            }
            return true;
        }
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): connected to Google APIs");

        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
        }

        Log.d(TAG, "player name = " + displayName);

        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
            Toast.makeText(mActivity, "your_progress_will_be_uploaded",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");

        mResolvingConnectionFailure = true;
        if (!resolveConnectionFailure(mActivity, mGoogleApiClient, connectionResult,
                RC_SIGN_IN, "sign_in_other_error")) {
            mResolvingConnectionFailure = false;
        }
    }

    private boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    void pushAccomplishments() {
        if (!isSignedIn()) {
            // can't push to the cloud, so save locally
            mOutbox.saveLocal(GemsterApp.getInstance());
            return;
        }
        if (mOutbox.mPrimeAchievement) {
            //Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_prime));
            //mOutbox.mPrimeAchievement = false;
        }
        mOutbox.saveLocal(GemsterApp.getInstance());
    }

    class AccomplishmentsOutbox {
        boolean mPrimeAchievement = false;
        boolean mHumbleAchievement = false;
        boolean mLeetAchievement = false;
        boolean mArrogantAchievement = false;
        int mBoredSteps = 0;
        int mEasyModeScore = -1;
        int mHardModeScore = -1;

        boolean isEmpty() {
            return !mPrimeAchievement && !mHumbleAchievement && !mLeetAchievement &&
                    !mArrogantAchievement && mBoredSteps == 0 && mEasyModeScore < 0 &&
                    mHardModeScore < 0;
        }

        public void saveLocal(Context ctx) {
            /* TODO: This is left as an exercise. To make it more difficult to cheat,
             * this data should be stored in an encrypted file! And remember not to
             * expose your encryption key (obfuscate it by building it from bits and
             * pieces and/or XORing with another string, for instance). */
        }

        public void loadLocal(Context ctx) {
            /* TODO: This is left as an exercise. Write code here that loads data
             * from the file you wrote in saveLocal(). */
        }
    }

    /**
     * Resolve a connection failure from
     * {@link com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener#onConnectionFailed(com.google.android.gms.common.ConnectionResult)}
     *
     * @param activity             the Activity trying to resolve the connection failure.
     * @param client               the GoogleAPIClient instance of the Activity.
     * @param result               the ConnectionResult received by the Activity.
     * @param requestCode          a request code which the calling Activity can use to identify the result
     *                             of this resolution in onActivityResult.
     * @param fallbackErrorMessage a generic error message to display if the failure cannot be resolved.
     * @return true if the connection failure is resolved, false otherwise.
     */
    public boolean resolveConnectionFailure(Activity activity,
                                            GoogleApiClient client, ConnectionResult result, int requestCode,
                                            String fallbackErrorMessage) {

        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, requestCode);
                return true;
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                client.connect();
                return false;
            }
        } else {
            // not resolvable... so show an error message
            int errorCode = result.getErrorCode();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                    activity, requestCode);
            if (dialog != null) {
                dialog.show();
            } else {
                // no built-in dialog: show the fallback error message
                Log.d(TAG, fallbackErrorMessage);
            }
            return false;
        }
    }

    /**
     * Show a {@link android.app.Dialog} with the correct message for a connection error.
     *
     * @param activity         the Activity in which the Dialog should be displayed.
     * @param requestCode      the request code from onActivityResult.
     * @param actResp          the response code from onActivityResult.
     * @param errorDescription the resource id of a String for a generic error message.
     */
    public static void showActivityResultError(Activity activity, int requestCode, int actResp, String errorDescription) {
        if (activity == null) {
            Log.e("BaseGameUtils", "*** No Activity. Can't show failure dialog!");
            return;
        }
        Dialog errorDialog;

        switch (actResp) {
            case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                errorDialog = makeSimpleDialog(activity, "app_misconfigured");
                break;
            case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                errorDialog = makeSimpleDialog(activity, "sign_in_failed");
                break;
            case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
                errorDialog = makeSimpleDialog(activity, "license_failed");
                break;
            default:
                // No meaningful Activity response code, so generate default Google
                // Play services dialog
                final int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
                errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                        activity, requestCode, null);
                if (errorDialog == null) {
                    // get fallback dialog
                    Log.e("BaseGamesUtils",
                            "No standard error dialog available. Making fallback dialog.");
                    errorDialog = makeSimpleDialog(activity, errorDescription);
                }
        }

        errorDialog.show();
    }

    /**
     * Create a simple {@link Dialog} with an 'OK' button and a message.
     *
     * @param activity the Activity in which the Dialog should be displayed.
     * @param text     the message to display on the Dialog.
     * @return an instance of {@link android.app.AlertDialog}
     */
    public static Dialog makeSimpleDialog(Activity activity, String text) {
        return (new AlertDialog.Builder(activity)).setMessage(text)
                .setNeutralButton(android.R.string.ok, null).create();
    }
}
