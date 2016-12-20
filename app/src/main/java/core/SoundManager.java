package core;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import pollinationp.gemster.R;

/**
 * Created by WONSEOK OH on 2016-12-11.
 */

public class SoundManager {

    private static MediaPlayer mBGM;
    private static SoundPool mSoundPool;
    private static int mSoundIdClick;

    public enum SoundEnum {CLICK}

    public static void init(Activity activity) {
        mBGM = MediaPlayer.create(activity, R.raw.background_sound);
        mBGM.setVolume(0.5f, 0.5f);
        mBGM.setLooping(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .build();
        } else {
            mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
        }
        mSoundIdClick = mSoundPool.load(activity, R.raw.sm_click, 1);
    }

    public static void startBGM() {
        if ((boolean) Common.getPrefData(Common.DISABLE_BGM)) return;
        mBGM.start();
    }

    public static void pauseBGM() {
        mBGM.pause();
    }

    public static void stopBGM() {
        mBGM.stop();
    }

    public static void startSound(SoundEnum sound) {
        if ((boolean) Common.getPrefData(Common.DISABLE_EFFECT_SOUND)) return;
        if (SoundEnum.CLICK.equals(sound)) {
            mSoundPool.play(mSoundIdClick, 1, 1, 1, 0, 1);
        }
    }

    public static void setEnabledBGM(boolean enable) {
        Common.setPrefData(Common.DISABLE_BGM, String.valueOf(!enable));
        if (enable) {
            startBGM();
        } else {
            pauseBGM();
        }
    }

    public static void setEnabledEffectSound(boolean enable) {
        Common.setPrefData(Common.DISABLE_EFFECT_SOUND, String.valueOf(!enable));
    }

}
