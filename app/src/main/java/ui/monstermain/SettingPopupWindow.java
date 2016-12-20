package ui.monstermain;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;

import core.Common;
import core.CustomOnTouchListener;
import core.GemsterApp;
import core.SoundManager;
import pollinationp.gemster.R;
import ui.EffectManager;

/**
 * Created by WONSEOK OH on 2016-12-16.
 */

public class SettingPopupWindow extends PopupWindow {

    private Activity mActivity;

    private EffectManager mEffectManager;

    private ImageButton mImageButtonClose;
    private CheckBox mCheckboxBGSound;
    private CheckBox mCheckboxEffectSound;
    private SignInButton mSignButton;
    private ImageButton mImageButtonCredit;

    private CustomOnTouchListener mOnTouchListener;
    private View.OnClickListener mOnSignClickListener;

    public enum EventList {EVENT_SIGN}

    private EventListener mListener;

    public interface EventListener {
        void onSettingEvent(EventList event);
    }

    public void setEventListener(EventListener listener) {
        mListener = listener;
    }

    public SettingPopupWindow(final Activity activity, final EffectManager effectManager) {
        super(activity);
        mActivity = activity;
        mEffectManager = effectManager;

        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_setting_popup_window, null, false);

        this.setContentView(layout);
        this.setFocusable(true);
        this.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.PopupWindowAnimationStyle);
        this.setBackgroundDrawable(null);

        initView();
        initListener();
        setListener();
    }

    private void initView() {
        mImageButtonClose = (ImageButton) getContentView().findViewById(R.id.spw_imageButton_close);
        mCheckboxBGSound = (CheckBox) getContentView().findViewById(R.id.spw_checkBox_bgSound);
        mCheckboxEffectSound = (CheckBox) getContentView().findViewById(R.id.spw_checkBox_effectSound);
        mSignButton = (SignInButton) getContentView().findViewById(R.id.spw_signInButton);
        mImageButtonCredit = (ImageButton) getContentView().findViewById(R.id.spw_imageButton_credit);
    }

    private void initListener() {
        mOnTouchListener = new CustomOnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    SoundManager.startSound(SoundManager.SoundEnum.CLICK);
                    setRectAndIgnore(view);
                    startClickScaleAnimation(view);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (this.getIgnore()) return false;
                    endClickScaleAnimation(view);
                    processActionUp(view);
                } else if (event.getAction() == MotionEvent.ACTION_MOVE && this.isOutOfBounds(view, event)) {
                    if (this.getIgnore()) return false;
                    endClickScaleAnimation(view);
                    this.setIgnore(true);
                }
                return true;
            }
        };

        mOnSignClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundManager.startSound(SoundManager.SoundEnum.CLICK);
                endClickScaleAnimation(view);
                handleSign();
                mListener.onSettingEvent(EventList.EVENT_SIGN);
            }
        };
    }

    private void setListener() {
        mImageButtonClose.setOnTouchListener(mOnTouchListener);
        mCheckboxBGSound.setOnTouchListener(mOnTouchListener);
        mCheckboxEffectSound.setOnTouchListener(mOnTouchListener);
        mImageButtonCredit.setOnTouchListener(mOnTouchListener);

        mSignButton.setOnClickListener(mOnSignClickListener);
    }

    public void show(View parent) {
        setSignButtonText();

        this.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    private void setSignButtonText() {
        String desc;

        mSignButton.setEnabled(Common.isNetworkConnected());

        if (GemsterApp.getInstance().getClient().isConnected()) {
            desc = "change account".toUpperCase();
        } else {
            desc = "sign in".toUpperCase();
        }

        for (int child = 0; child < mSignButton.getChildCount(); child++) {
            View view = mSignButton.getChildAt(child);
            if (view instanceof TextView) {
                ((TextView) view).setText(desc);
            }
        }
    }

    private void handleSign() {
        if (GemsterApp.getInstance().getClient().isConnected()) {
            GemsterApp.getInstance().getClient().savedGamesUpdate(true);
            setSignButtonText();
        } else {
            GemsterApp.getInstance().getClient().connect();
        }
    }

    private void startClickScaleAnimation(View view) {
        mEffectManager.startClickScaleAnimation(view);
    }

    private void endClickScaleAnimation(View view) {
        mEffectManager.endClickScaleAnimation(view);
    }

    private void processActionUp(View view) {
        if (view == null) return;
        if (view.equals(mImageButtonClose)) {
            dismiss();
        } else if (view.equals(mImageButtonCredit)) {
        }
    }
}
