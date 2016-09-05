package com.cyanogenmod.setupwizard1.ui;

import java.io.File;
import java.util.ArrayList;

import com.cyanogenmod.setupwizard1.R;
import com.cyanogenmod.setupwizard1.SetupWizardApp;
import com.cyanogenmod.setupwizard1.cmstats.SetupStats;
import com.cyanogenmod.setupwizard1.setup.CMSetupWizardData;
import com.cyanogenmod.setupwizard1.setup.Page;
import com.cyanogenmod.setupwizard1.setup.SetupDataCallbacks;
import com.cyanogenmod.setupwizard1.util.EnableAccessibilityController;
import com.cyanogenmod.setupwizard1.util.SetupWizardUtils;

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

public class aaa extends Activity {

//	private static final String TAG = SetupWizardActivity.class.getSimpleName();

	private static final int UI_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			| View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
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

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
	}

	
}
