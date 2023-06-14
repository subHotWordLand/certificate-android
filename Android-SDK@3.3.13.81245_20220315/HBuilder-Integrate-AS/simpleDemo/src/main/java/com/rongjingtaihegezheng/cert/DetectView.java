package com.rongjingtaihegezheng.cert;

import com.aiwinn.facedetectsdk.bean.FaceBean;
import com.aiwinn.facedetectsdk.bean.UserBean;
import com.aiwinn.facedetectsdk.common.Status;

import java.util.List;

/**
 * com.aiwinn.faceattendance.ui.v
 * SDK_ATT
 * 2018/08/24
 * Created by LeoLiu on User
 */

public interface DetectView {

    void recognizeFace(UserBean userBean);

    void recognizeFaceNotMatch(UserBean userBean);

    void detectNoFace();

    void detectFail(Status status);

    void detectFace(List<FaceBean> faceBeans);

    void debug(FaceBean faceBean);

    void detectInfraredInfo(String faceId);
}
