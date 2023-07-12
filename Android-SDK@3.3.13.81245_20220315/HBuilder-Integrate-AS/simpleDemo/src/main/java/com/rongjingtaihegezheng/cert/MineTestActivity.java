package com.rongjingtaihegezheng.cert;

import static com.msprintsdk.UtilsTools.convertToBlackWhite;
import static com.msprintsdk.UtilsTools.getFromRaw;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class MineTestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mt);

        final ImageView imageView = findViewById(R.id.imageView);
        final ImageView imageView1 = findViewById(R.id.imageView1);

        //
        Bitmap bitmap = null;
        InputStream txt = getResources().openRawResource(R.raw.txt);
        String base64Data = getFromRaw(txt);
        byte[] bytes = Base64.decode(base64Data.split(",")[1], Base64.DEFAULT);
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        bitmap = convertToBlackWhite(bitmap);
        imageView.setImageBitmap(bitmap);
        //反面
        Bitmap emptyBp = Bitmap.createBitmap(bitmap.getWidth(), 150, Bitmap.Config.ARGB_8888);
        emptyBp.eraseColor(Color.parseColor("#000000"));
        Bitmap rb = addBitmap(bitmap, emptyBp);
        Bitmap bitmap1 = rotateBitmap(rb, 180);
        imageView1.setImageBitmap(bitmap1);
    }

    /**
     * 纵向拼接
     * <功能详细描述>
     *
     * @param first
     * @param second
     * @return
     */
    private Bitmap addBitmap(Bitmap first, Bitmap second) {
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
