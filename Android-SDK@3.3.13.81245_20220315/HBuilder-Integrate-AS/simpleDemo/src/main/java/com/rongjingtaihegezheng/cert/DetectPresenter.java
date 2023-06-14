package com.rongjingtaihegezheng.cert;

/**
 * com.aiwinn.faceattendance.ui.p
 * SDK_ATT
 * 2018/08/24
 * Created by LeoLiu on User
 */

public interface DetectPresenter {

    void detectFaceData(byte[] data, int w, int h);
    void detectInfraredLiveData(byte[] data, int w, int h, int degree);
}
