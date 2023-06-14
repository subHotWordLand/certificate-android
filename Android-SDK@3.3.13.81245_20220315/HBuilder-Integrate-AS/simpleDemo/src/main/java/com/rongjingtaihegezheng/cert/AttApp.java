package com.rongjingtaihegezheng.cert;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.aiwinn.base.AiwinnManager;
import com.aiwinn.facedetectsdk.FaceDetectManager;
import com.aiwinn.facedetectsdk.common.Status;
import com.aiwinn.facedetectsdk.listener.InitSDKCallback;
import com.rongjingtaihegezheng.cert.common.AttConstants;

/**
 * com.aiwinn.faceattendance
 * SDK_ATT
 * 2018/08/24
 * Created by LeoLiu on User
 */

public class AttApp extends io.dcloud.application.DCloudApplication {

    public static Context mContext;
    public static SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        sp = getSharedPreferences(AttConstants.PREFS, 0);
        AiwinnManager.getInstance().init(this);
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
    }

    public static void initSDK() {
        AiwinnManager.getInstance().setDebug(AttConstants.DEBUG);
        FaceDetectManager.setDebug(AttConstants.DEBUG);

        Log.d("AttApp", "start init sdk");
        //String PATH = getContext().getExternalFilesDir(null).getPath();
        //FaceDetectManager.init(mContext, PATH,new InitSDKCallback() {
            FaceDetectManager.init(mContext, new InitSDKCallback() {
            @Override
            public void onSuccess() {
                AttConstants.INIT_STATE = true;
                Log.d("AttApp", "onSuccess: ");
                if (!FaceDetectManager.initDb(AttConstants.EXDB)) {//生成自定义多库, demo需要, 非必须
                    Log.e("AttApp", "init ex db fail");
                }
            }

            @Override
            public void onFailed(Status status, String msg) {
                Log.d("AttApp", "onFailed: " + msg);
                AttConstants.INIT_STATE = false;
                AttConstants.INIT_STATE_ERROR = status;
            }
        });
    }

    public static Context getContext() {
        return mContext;
    }

    public static void hideBottomUIMenu(Activity activity) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
