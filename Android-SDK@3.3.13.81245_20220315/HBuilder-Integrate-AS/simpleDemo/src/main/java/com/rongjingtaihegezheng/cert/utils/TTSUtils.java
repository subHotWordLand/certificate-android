package com.rongjingtaihegezheng.cert.utils;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

/**
 * 刘成  on 2018/6/1
 * Description:
 */
public final class TTSUtils {

    private static final String TAG = "TTSUtils";

    private static TextToSpeech mSpeech;
    private static int initStatus;

    /**
     * 建议在 Application 的 onCreate() 方法中初始化，这样就可以在任何界面中直接调用{@link#onStartSpeech(String,int)}方法
     *
     * @param context 上下文
     */
    public static void initSpeech(Context context) {

        mSpeech = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                initStatus = status;
            }
        });
        //初始化成功
        if (initStatus == TextToSpeech.SUCCESS) {
            Log.e(TAG, "TTS初始化成功");
            int result = mSpeech.setLanguage(Locale.CHINA);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS初始化失败：不支持中文");
            }
        }else {
            Log.e(TAG, "TTS初始化失败");
        }
    }

    /**
     * 播报语音，或打断之前的播放
     *
     * @param str 需要播报的字符串
     */
    private static void onStartSpeech(String str, int type) {
        if (null != mSpeech) {
            mSpeech.speak(str, type, null);
        } else {
            Log.e(TAG, "TTS还未初始化");
        }
    }

    private static String oldContent = "";

    public static void speech(String str) {
        if (TextUtils.isEmpty(str)) {
            return;
        }
        if (isSpeeching() && oldContent.equals(str)) {
            return;
        }
        oldContent = str;
        onStartSpeech(oldContent, TextToSpeech.QUEUE_FLUSH);
    }

    public static void speech_add(String str) {
        onStartSpeech(str, TextToSpeech.QUEUE_ADD);
    }


    /**
     * 当前是否正在播放语音
     *
     * @return ture:正在播放  false：没有播放
     */
    private static boolean isSpeeching() {
        return mSpeech != null && mSpeech.isSpeaking();
    }

    /**
     * 停止当前语音播报
     */
    public static void onStopSpeech() {
        if (null != mSpeech) {
            mSpeech.stop();
        }
    }

    /**
     * 释放资源操作
     * 调用此方法后，需要调用{@link #initSpeech(Context)}方法进行重新初始化
     */
    public static void onDestroy() {
        if (null != mSpeech) {
            mSpeech.stop();
            mSpeech.shutdown();
            mSpeech = null;
        }
    }
}
