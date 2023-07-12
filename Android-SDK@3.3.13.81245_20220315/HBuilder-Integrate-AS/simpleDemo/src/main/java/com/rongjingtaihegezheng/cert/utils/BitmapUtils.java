package com.rongjingtaihegezheng.cert.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

public class BitmapUtils {
    /**
     * 纵向拼接
     * <功能详细描述>
     *
     * @param first
     * @param second
     * @return
     */
    public static Bitmap addBitmap(Bitmap first, Bitmap second) {
        int width = Math.max(first.getWidth(), second.getWidth());
        int height = first.getHeight() + second.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(first, 0, 0, null);
        canvas.drawBitmap(second, 0, first.getHeight(), null);
        return result;
    }


    /**
     * 裁剪一定高度保留下面
     *
     * @param srcBitmap
     * @param needHeight
     * @return
     */
    public static Bitmap cropBitmapTop(Bitmap srcBitmap, int needHeight) {
        Log.d("danxx", "cropBitmapBottom before h : " + srcBitmap.getHeight());
        /**裁剪保留上部分的第一个像素的Y坐标*/
        int needY = 0;
        /**裁剪关键步骤*/
        Bitmap cropBitmap = Bitmap.createBitmap(srcBitmap, 0, needY, srcBitmap.getWidth(), needHeight);
        Log.d("danxx", "cropBitmapBottom after h : " + cropBitmap.getHeight());
        return cropBitmap;
    }

    /**
     * 旋转
     *
     * @param bitmap
     * @param degress
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m,
                    true);
            return bitmap;
        }
        return bitmap;
    }
}
