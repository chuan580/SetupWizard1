/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.setupwizard1.ui;

import android.animation.Animator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.ThemeManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cyanogenmod.setupwizard1.R;
import com.cyanogenmod.setupwizard1.SetupWizardApp;
import com.cyanogenmod.setupwizard1.cmstats.SetupStats;
import com.cyanogenmod.setupwizard1.setup.CMSetupWizardData;
import com.cyanogenmod.setupwizard1.setup.Page;
import com.cyanogenmod.setupwizard1.setup.SetupDataCallbacks;
import com.cyanogenmod.setupwizard1.util.EnableAccessibilityController;
import com.cyanogenmod.setupwizard1.util.SetupWizardUtils;

import java.io.File;
import java.util.ArrayList;


public class SetupWizardActivity1 extends Activity implements SetupDataCallbacks,
        ThemeManager.ThemeChangeListener {

    private static final String TAG = "abc";//SetupWizardActivity.class.getSimpleName();

    private static final int UI_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    private View mRootView;
    private View mButtonBar;
    private Button mNextButton;
    private Button mPrevButton;
    private ImageView mReveal;
    private ProgressBar mFinishingProgressBar;

    private EnableAccessibilityController mEnableAccessibilityController;

    private CMSetupWizardData mSetupData;

    private final Handler mHandler = new Handler();

    private volatile boolean mIsFinishing = false;

    private static long sLaunchTime = 0;

    private final ArrayList<Runnable> mFinishRunnables = new ArrayList<Runnable>();

    public void onCreate(Bundle savedInstanceState) {
    	System.out.println("3333333333333");
    	Toast.makeText(this, "aaaaxxxwww", Toast.LENGTH_LONG).show();
        super.onCreate(savedInstanceState);
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(UI_FLAGS);
        decorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(UI_FLAGS);
                        }
                    }
                });
        if (sLaunchTime == 0) {
            SetupStats.addEvent(SetupStats.Categories.APP_LAUNCH, TAG);
            sLaunchTime = System.nanoTime();
        }
        setContentView(R.layout.setup_main);
        mRootView = findViewById(R.id.root);
        mRootView.setSystemUiVisibility(UI_FLAGS);
        mReveal = (ImageView)mRootView.findViewById(R.id.reveal);
        mButtonBar = findViewById(R.id.button_bar);
        mFinishingProgressBar = (ProgressBar)findViewById(R.id.finishing_bar);
        ((SetupWizardApp)getApplicationContext()).disableStatusBar();
        mSetupData = (CMSetupWizardData)getLastNonConfigurationInstance();
        if (mSetupData == null) {
            mSetupData = new CMSetupWizardData(getApplicationContext(),handler);
        }
        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);
        if (mSetupData.isFinished()) {
            mNextButton.setVisibility(View.INVISIBLE);
            mPrevButton.setVisibility(View.INVISIBLE);
        }
        mSetupData.registerListener(this);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableButtonBar(false);
                mSetupData.onNextPage();
            }
        });
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableButtonBar(false);
                mSetupData.onPreviousPage();
            }
        });
        if (savedInstanceState == null) {
            Page page = mSetupData.getCurrentPage();
            page.doLoadAction(getFragmentManager(), Page.ACTION_NEXT);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("data")) {
            mSetupData.load(savedInstanceState.getBundle("data"));
        }
        mEnableAccessibilityController =
                EnableAccessibilityController.getInstance(getApplicationContext());
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean consumeIntercept = mEnableAccessibilityController.onInterceptTouchEvent(event);
                boolean consumeTouch = mEnableAccessibilityController.onTouchEvent(event);
                return consumeIntercept && consumeTouch;
            }
        });
        registerReceiver(mSetupData, mSetupData.getIntentFilter());

    }

    @Override
    protected void onResume() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(UI_FLAGS);
        super.onResume();
        if (mSetupData.isFinished()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishSetup();
                }
            }, 500);
        }  else {
            mSetupData.onResume();
            onPageTreeChanged();
            enableButtonBar(true);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        File file = new File(Environment.getExternalStorageDirectory().getPath()+ "/" + ".digdou");
        if (file.exists()){
            mSetupData.onNextPage();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSetupData.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSetupData.onDestroy();
        mSetupData.unregisterListener(this);
        unregisterReceiver(mSetupData);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mSetupData;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("data", mSetupData.save());
    }

    @Override
    public void onBackPressed() {
        if (!mSetupData.isFirstPage()) {
            mSetupData.onPreviousPage();
        }
    }

    @Override
    public void onNextPage() {
        Page page = mSetupData.getCurrentPage();
        if (!isFinishing()) {
            page.doLoadAction(getFragmentManager(), Page.ACTION_NEXT);
        }
    }

    @Override
    public void onPreviousPage() {
        Page page = mSetupData.getCurrentPage();
        if (!isFinishing()) {
            page.doLoadAction(getFragmentManager(), Page.ACTION_PREVIOUS);
        }
    }

    @Override
    public void onPageLoaded(Page page) {
        updateButtonBar();
        enableButtonBar(true);
    }

    @Override
    public void onPageTreeChanged() {
        updateButtonBar();
    }

    private void enableButtonBar(boolean enabled) {
        mNextButton.setEnabled(true);
        mPrevButton.setEnabled(true);
    }

    private void updateButtonBar() {
        Page page = mSetupData.getCurrentPage();
        mNextButton.setText(page.getNextButtonTitleResId());
        if (page.getPrevButtonTitleResId() != -1) {
            mPrevButton.setText(page.getPrevButtonTitleResId());
        } else {
            mPrevButton.setText("");
        }
        if (mSetupData.isFirstPage()) {
            mPrevButton.setCompoundDrawables(null, null, null, null);
            mPrevButton.setVisibility(SetupWizardUtils.hasTelephony(this) ?
                    View.VISIBLE : View.INVISIBLE);
        } else {
            mPrevButton.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(R.drawable.ic_chevron_left_dark),
                    null, null, null);
        }
        final Resources resources = getResources();
        if (mSetupData.isLastPage()) {
            mButtonBar.setBackgroundResource(R.color.primary);
            mNextButton.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    getDrawable(R.drawable.ic_chevron_right_wht), null);
            mNextButton.setTextColor(resources.getColor(R.color.white));
            mPrevButton.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(R.drawable.ic_chevron_left_wht), null,
                    null, null);
            mPrevButton.setTextColor(resources.getColor(R.color.white));
        } else {
            mButtonBar.setBackgroundResource(R.color.button_bar_background);
            mNextButton.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    getDrawable(R.drawable.ic_chevron_right_dark), null);
            mNextButton.setTextColor(resources.getColorStateList(R.color.button_bar_text));
            mPrevButton.setTextColor(resources.getColorStateList(R.color.button_bar_text));
        }
    }

    @Override
    public Page getPage(String key) {
        return mSetupData.getPage(key);
    }

    @Override
    public Page getPage(int key) {
        return mSetupData.getPage(key);
    }

    @Override
    public boolean isCurrentPage(Page page) {
        return mSetupData.isCurrentPage(page);
    }

    @Override
    public void addFinishRunnable(Runnable runnable) {
        mFinishRunnables.add(runnable);
    }

    @Override
    public void onFinish() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        mNextButton.startAnimation(fadeOut);
        mNextButton.setVisibility(View.INVISIBLE);
        mPrevButton.startAnimation(fadeOut);
        mPrevButton.setVisibility(View.INVISIBLE);
        final SetupWizardApp setupWizardApp = (SetupWizardApp)getApplication();
        setupWizardApp.enableStatusBar();
        setupWizardApp.enableCaptivePortalDetection();
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        mFinishingProgressBar.setVisibility(View.VISIBLE);
        mFinishingProgressBar.setIndeterminate(true);
        mFinishingProgressBar.startAnimation(fadeIn);
//        final ThemeManager tm = (ThemeManager) getSystemService(Context.THEME_SERVICE);
//        tm.addClient(this);
        mSetupData.finishPages();
    }

    @Override
    public void onFinish(boolean isSuccess) {
        if (isResumed()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    finishSetup();
                }
            });
        }
    }

    @Override
    public void onProgress(int progress) {
        if (progress > 0) {
            mFinishingProgressBar.setIndeterminate(false);
            mFinishingProgressBar.setProgress(progress);
        }
    }

    @Override
    public void finishSetup() {
        if (!mIsFinishing) {
            SetupStats.addEvent(SetupStats.Categories.APP_FINISHED, TAG,
                    SetupStats.Label.TOTAL_TIME, String.valueOf(
                            System.nanoTime() - sLaunchTime));
            final SetupWizardApp setupWizardApp = (SetupWizardApp)getApplication();
            setupWizardApp.sendStickyBroadcastAsUser(
                    new Intent(SetupWizardApp.ACTION_FINISHED),
                    UserHandle.getCallingUserHandle());
            mIsFinishing = true;
            setupRevealImage();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.translucent_enter, R.anim.translucent_exit);
    }

    private void setupRevealImage() {
        mFinishingProgressBar.setProgress(100);
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        mFinishingProgressBar.startAnimation(fadeOut);
        mFinishingProgressBar.setVisibility(View.INVISIBLE);

        final Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        final WallpaperManager wallpaperManager =
                WallpaperManager.getInstance(SetupWizardActivity1.this);
        wallpaperManager.forgetLoadedWallpaper();
        final Bitmap wallpaper = wallpaperManager.getBitmap();
        Bitmap cropped = null;
        if (wallpaper != null) {
            cropped = Bitmap.createBitmap(wallpaper, 0,
                    0, Math.min(p.x, wallpaper.getWidth()),
                    Math.min(p.y, wallpaper.getHeight()));
        }
        if (cropped != null) {
            mReveal.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mReveal.setImageBitmap(cropped);
        } else {
            mReveal.setBackground(wallpaperManager
                    .getBuiltInDrawable(p.x, p.y, false, 0, 0));
        }
        animateOut();
    }

    private void animateOut() {
        int cx = (mReveal.getLeft() + mReveal.getRight()) / 2;
        int cy = (mReveal.getTop() + mReveal.getBottom()) / 2;
        int finalRadius = Math.max(mReveal.getWidth(), mReveal.getHeight());
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mReveal, cx, cy, 0, finalRadius);
        anim.setDuration(900);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mReveal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        finalizeSetup();
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        anim.start();
    }

    private void finalizeSetup() {
        mFinishRunnables.add(new Runnable() {
            @Override
            public void run() {
                Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
                Settings.Secure.putInt(getContentResolver(),
                        Settings.Secure.USER_SETUP_COMPLETE, 1);
                if (mEnableAccessibilityController != null) {
                    mEnableAccessibilityController.onDestroy();
                }
                final ThemeManager tm =
                        (ThemeManager) SetupWizardActivity1.this.getSystemService(THEME_SERVICE);
                tm.removeClient(SetupWizardActivity1.this);
                SetupStats.sendEvents(SetupWizardActivity1.this);
                SetupWizardUtils.disableGMSSetupWizard(SetupWizardActivity1.this);
                final WallpaperManager wallpaperManager =
                        WallpaperManager.getInstance(SetupWizardActivity1.this);
                wallpaperManager.forgetLoadedWallpaper();
            }
        });
        new FinishTask(this, mFinishRunnables).execute();
    }

    private static class FinishTask extends AsyncTask<Void, Void, Boolean> {

        private final SetupWizardActivity1 mActivity;
        private final ArrayList<Runnable> mFinishRunnables;

        public FinishTask(SetupWizardActivity1 activity,
                ArrayList<Runnable> finishRunnables) {
            mActivity = activity;
            mFinishRunnables = finishRunnables;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (Runnable runnable : mFinishRunnables) {
                runnable.run();
            }
            SetupWizardUtils.disableSetupWizard(mActivity);
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            mActivity.startActivity(intent);
            mActivity.finish();
        }
    }
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mPrevButton.setEnabled(false);
            mPrevButton.setVisibility(View.INVISIBLE);
            
        }
    };

        public boolean onKeyDown(int keyCode,KeyEvent event) {
            File file = new File(Environment.getExternalStorageDirectory().getPath()+ "/" + ".digdou");
            if (file.exists()){
            switch (keyCode) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    return true;
                case KeyEvent.KEYCODE_CALL:
                    return true;
                case KeyEvent.KEYCODE_SYM:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return true;
                case KeyEvent.KEYCODE_STAR:
                    return true;
             }
            }
        else if(keyCode == KeyEvent.KEYCODE_HOME)
        {
            //由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
            return false;
        }
            return super.onKeyDown(keyCode, event);
        }
}
