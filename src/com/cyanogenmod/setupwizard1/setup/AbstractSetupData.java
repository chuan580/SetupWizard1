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

package com.cyanogenmod.setupwizard1.setup;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import com.cyanogenmod.setupwizard1.ui.SetupWizardActivity1;

public abstract class AbstractSetupData extends BroadcastReceiver implements SetupDataCallbacks {
boolean r=true;
    Runnable runnable;
    private static final String TAG = AbstractSetupData.class.getSimpleName();
    protected final Context mContext;
    private ArrayList<SetupDataCallbacks> mListeners = new ArrayList<SetupDataCallbacks>();
    private PageList mPageList;

    private int mCurrentPageIndex = 0;
    
    private int pageIndex = 0;// 我们自己定义索引，方便通过索引操作

    private boolean mIsResumed = false;

    private boolean mIsFinished = false;

    private SetupWizardActivity1 setupWizardActivity1;

    private OnResumeRunnable mOnResumeRunnable;
    Handler handler;
    public AbstractSetupData(Context context ,Handler handler ) {
        mContext = context;
        mPageList = onNewPageList();
        this.handler = handler;
    }
    protected abstract PageList onNewPageList();

    @Override
    public void onPageLoaded(Page page) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onPageLoaded(page);
        }
    }

    @Override
    public void onPageTreeChanged() {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onPageTreeChanged();
        }
    }

    @Override
    public void onFinish() {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onFinish();
        }
    }

    @Override
    public void finishSetup() {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).finishSetup();
        }
    }

    @Override
    public Page getPage(String key) {
        return mPageList.getPage(key);
    }

    @Override
    public Page getPage(int index) {
        return mPageList.getPage(index);
    }

    public Page getCurrentPage() {
        return mPageList.getPage(mCurrentPageIndex);
    }

    @Override
    public boolean isCurrentPage(Page page) {
        if (page == null) return false;
        return page.getKey().equals(getCurrentPage().getKey());
    }

    public boolean isFirstPage() {
        return mCurrentPageIndex == 0;
    }

    public boolean isLastPage() {
        return mCurrentPageIndex == mPageList.size() - 1;
    }

    @Override
    public void onNextPage() {
    	pageIndex++;
        runnable = new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(mContext,mCurrentPageIndex+"",Toast.LENGTH_SHORT).show();
                // ((Activity)mContext).runOnUiThread(new Runnable() {
                //     public void run() {
                //         Toast.makeText(mContext,String.valueOf(mCurrentPageIndex),Toast.LENGTH_SHORT).show();
                //     }
                // });
//            	System.out.println("s==--ssss---"+getCurrentPage().doNextAction()+"--ss--"+advanceToNextUnhidden());
                if (getCurrentPage().doNextAction() == false) {
                    if (advanceToNextUnhidden()) {
                        for (int i = 0; i < mListeners.size(); i++) {
                            mListeners.get(i).onNextPage();
                            System.out.println("aaaaaaaaa");
                        }
                    }
                }
            }
        };
    	doPreviousNext(runnable);
    	System.out.println("sss----wwww--"+pageIndex);

    	if(pageIndex == 2){
    	   Intent mIntent = new Intent();
    	   mIntent.putExtra("NotLogged", "123");
           mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           ComponentName comp = new ComponentName("com.example.startsetting", "com.example.startsetting.MainActivity");
           mIntent.setComponent(comp);
           mIntent.setAction("android.intent.action.VIEW");
           mContext.startActivity(mIntent);
    	}
    		

//        File file = new File(Environment.getExternalStorageDirectory().getPath()+ "/" + ".digdou");
//        if (mCurrentPageIndex==9){
//            file.delete();
//            doPreviousNext(runnable);
//        }
//        if (file.exists()){
//            Message mg = Message.obtain();
//            mg.obj = mCurrentPageIndex;
//            handler.sendMessage(mg);
//            doPreviousNext(runnable);
//            r=false;
//        }

//        if(mCurrentPageIndex==1) {
////            if (isNetworkAvailable(mContext)) {
//            Intent mIntent = new Intent();
//            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            ComponentName comp = new ComponentName("com.example.startsetting", "com.example.startsetting.MainActivity");
//            mIntent.setComponent(comp);
//            mIntent.setAction("android.intent.action.VIEW");
//            mContext.startActivity(mIntent);
//            // ((Activity)mContext).runOnUiThread(new Runnable() {
//            //     public void run() {next_button
//            //         Toast.makeText(mContext,"准备启 动云服务",Toast.LENGTH_SHORT).show();
//            //     }
//            // });
////            }
//            System.out.println("aaaaaaa------"+mCurrentPageIndex);
//            onPreviousPage();
////            doPreviousNext(mOnResumeRunnable);
//        }else if(r){
//        	System.out.println("bbbbbb------");
//          
//        }
    	
    }
    @Override
    public void onPreviousPage() {
    	if(pageIndex > 0)
    		pageIndex--;
    	System.out.println("zzzzzz----wwww--"+pageIndex);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (getCurrentPage().doPreviousAction() == false) {
                    if (advanceToPreviousUnhidden()) {
                        for (int i = 0; i < mListeners.size(); i++) {
                            mListeners.get(i).onPreviousPage();
                        }
                    }
                }
            }
        };
        if (r)
        doPreviousNext(runnable);
    }

    private boolean advanceToNextUnhidden() {
        while (mCurrentPageIndex < mPageList.size()-1) {
            mCurrentPageIndex++;
            if (!getCurrentPage().isHidden()) {
                return true;
            }
        }
        return false;
    }

    private boolean advanceToPreviousUnhidden() {
        while (mCurrentPageIndex > 0) {
            mCurrentPageIndex--;
            if (!getCurrentPage().isHidden()) {
                return true;
            }
        }
        return false;
    }

    public void load(Bundle savedValues) {
        for (String key : savedValues.keySet()) {
            Page page = mPageList.getPage(key);
            if (page != null) {
                page.resetData(savedValues.getBundle(key));
            }
        }
    }

    private void doPreviousNext(Runnable runnable) {
        if (mIsResumed) {
            runnable.run();
        } else {
            mOnResumeRunnable = new OnResumeRunnable(runnable, this);
        }
    }

    public void onDestroy() {
        mOnResumeRunnable = null;
    }

    public void onPause() {
        mIsResumed = false;
    }

    public void onResume() {
        mIsResumed = true;
        if (mOnResumeRunnable != null) {
            mOnResumeRunnable.run();
        }
    }

    public void finishPages() {
        mIsFinished = true;
        for (Page page : mPageList.values()) {
            page.onFinishSetup();
        }
    }

    @Override
    public void addFinishRunnable(Runnable runnable) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).addFinishRunnable(runnable);
        }
    }

    public boolean isFinished() {
        return mIsFinished;
    }

    public Bundle save() {
        Bundle bundle = new Bundle();
        for (Page page : mPageList.values()) {
            bundle.putBundle(page.getKey(), page.getData());
        }
        return bundle;
    }

    public void registerListener(SetupDataCallbacks listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(SetupDataCallbacks listener) {
        mListeners.remove(listener);
    }

    private static class OnResumeRunnable implements Runnable {

        private final AbstractSetupData mAbstractSetupData;
        private final Runnable mRunnable;

        private OnResumeRunnable(Runnable runnable, AbstractSetupData abstractSetupData) {
            mAbstractSetupData = abstractSetupData;
            mRunnable = runnable;
        }

        @Override
        public void run() {
            mRunnable.run();
            mAbstractSetupData.mOnResumeRunnable = null;
        }
    }
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
//                System.out.println("当前网络是连接的");
                if (info.getState() == NetworkInfo.State.CONNECTED) {
//                    System.out.println("当前所连接的网络可用");
                    return true;
                } else {
//                    Toast.makeText(this, "请检查网络，网络不可用", Toast.LENGTH_SHORT).show();
                }
            } else {
//                    Toast.makeText(this, "请检查网络，网络未连接", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }
}
