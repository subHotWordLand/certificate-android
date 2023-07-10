package com.rongjingtaihegezheng.cert;

import static com.msprintsdk.UtilsTools.convertToBlackWhite;
import static com.msprintsdk.UtilsTools.getFromRaw;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Base64;
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
        Bitmap bitmap1 = rotateBitmap(bitmap, 180);
        imageView1.setImageBitmap(bitmap1);

    }
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null){

            Matrix m = new Matrix();

            m.postRotate(degress);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m,

                    true);
            return bitmap;
        }
        return bitmap;
    }


}
