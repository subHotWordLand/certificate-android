package com.rongjingtaihegezheng.cert.bean;

import com.aiwinn.facedetectsdk.bean.UserBean;

/**
 * com.aiwinn.faceattendance
 * SDK_ATT
 * 2018/08/25
 * Created by LeoLiu on User
 */

public class DetectFaceBean {

    UserBean mUserBean;
    String time;

    public DetectFaceBean(UserBean userBean, String time) {
        this.mUserBean = userBean;
        this.time = time;
    }

    public UserBean getUserBean() {
        return mUserBean;
    }

    public void setUserBean(UserBean userBean) {
        mUserBean = userBean;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
