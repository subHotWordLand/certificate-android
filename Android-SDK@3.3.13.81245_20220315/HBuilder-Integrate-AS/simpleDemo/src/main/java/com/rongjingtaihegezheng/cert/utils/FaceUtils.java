package com.rongjingtaihegezheng.cert.utils;

import android.graphics.Bitmap;

import com.aiwinn.facedetectsdk.bean.DetectBean;

import java.util.List;

/**
 * com.aiwinn.faceattendance.utils
 * SDK_ATT
 * 2018/08/29
 * Created by LeoLiu on User
 */

public class FaceUtils {

    public static DetectBean findMaxFace(List<DetectBean> detectBeanList) {

        float s = 0.0f;

        DetectBean maxFace = null;

        for (DetectBean detectBean : detectBeanList) {
            float temp = Math.abs(detectBean.x0 - detectBean.x1) * Math.abs(detectBean.y0 - detectBean.y1);
            if (temp > s) {
                s = temp;
                maxFace = detectBean;
            }
        }

        return maxFace;
    }

    public static Bitmap createBitmapfromDetectBean(DetectBean detectBean, Bitmap image) {

        Bitmap headScalePic;

        if (image == null) return null;

        int x0 = (int) detectBean.x0;
        int x1 = (int) detectBean.x1;
        int y0 = (int) detectBean.y0;
        int y1 = (int) detectBean.y1;
        int image_width = (int) (x1 - x0);
        int image_height = (int) (y1 - y0);
        x0 = x0 - image_width / 4;
        x1 = x1 + image_width / 4;
        y0 = y0 - image_height / 4;
        y1 = y1 + image_height / 4;

        if (x1 >= image.getWidth()) {
            x1 = image.getWidth() - 1;
        }
        if (y1 >= image.getHeight()) {
            y1 = image.getHeight() - 1;
        }
        if (x0 < 0) {
            x0 = 1;
        }
        if (y0 < 0) {
            y0 = 1;
        }

        image_width = (int) (x1 - x0);
        image_height = (int) (y1 - y0);

        headScalePic = Bitmap.createBitmap(image, x0, y0, image_width, image_height);

        return headScalePic;
    }

}
