package com.rongjingtaihegezheng.cert;

import static com.aiwinn.facedetectsdk.common.ConfigLib.enableEyeDectect;
import static com.aiwinn.facedetectsdk.common.ConfigLib.enableGAADectect;
import static com.aiwinn.facedetectsdk.common.ConfigLib.enableHelmet;
import static com.aiwinn.facedetectsdk.common.ConfigLib.enableOtherATTR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.aiwinn.base.log.LogUtils;
import com.aiwinn.base.util.ScreenUtils;
import com.aiwinn.facedetectsdk.bean.FaceBean;
import com.aiwinn.facedetectsdk.common.ConfigLib;
import com.rongjingtaihegezheng.cert.common.AttConstants;

import java.util.List;


/**
 * com.aiwinn.facelock.widget.camera
 * 1217/08/05
 * Created by LeoLiu on User.
 */

@SuppressLint("AppCompatCustomView")
public class MaskView extends ImageView {

    List<FaceBean> mFaceBeans;
    boolean mDraw = false;
    boolean mLandScape = true;
    int width, height;
    int widthPreview, heightPreview;
    Paint mPaint;
    TextPaint mTextPaint;
    Rect rect = new Rect();
    private int green;
    private int red;
    private int blue;

    public MaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        initPaint();
        mDraw = false;
        mLandScape = ScreenUtils.isLandscape();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private void initPaint() {

        green = getResources().getColor(R.color.rect_green);
        red = getResources().getColor(R.color.rect_red);
        blue = getResources().getColor(R.color.rect_blue);

       mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(blue);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6f);
        mPaint.setAlpha(180);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(blue);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(80);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

    }

    public void setLandScape(){
        mLandScape = AttConstants.PORTRAIT_LANDSCAPE || ScreenUtils.isLandscape();
    }

    public void drawRect(List<FaceBean> faceInfoExes, int width, int height) {
        widthPreview = width;
        heightPreview = height;
        mFaceBeans = faceInfoExes;
        mDraw = true;
        postInvalidate();
    }

    public void clearRect() {
        if (mDraw) {
            mDraw = false;
        }
        this.invalidate();
    }

    boolean isShowMask = true;
    boolean isShowHelmet = enableHelmet;
    boolean isShowEyeStatus = enableEyeDectect;
    boolean isShowHeadAngle = true;
    boolean isShowGlass = enableOtherATTR;
    boolean isShowHat = enableOtherATTR;
    boolean isShowGAA = enableGAADectect;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDraw) {
            for (FaceBean faceBean : mFaceBeans) {
                if (faceBean != null) {
                    LogUtils.d(DetectPresenterImpl.HEAD, "RecognitionFace face [ MaskView ] name = " + faceBean.mUserBean.name + ", flag = " + faceBean.mDetectBean.flag + ", live tag = " + faceBean.mLiveBean.livenessTag);

                    //人脸在原始图片上的坐标，左上角[x0,y0]，右下角[y0,y1]
                    float _x0 = faceBean.mDetectBean.x0;
                    float _y0 = faceBean.mDetectBean.y0;
                    float _x1 = faceBean.mDetectBean.x1;
                    float _y1 = faceBean.mDetectBean.y1;

                    double scale_x;
                    double scale_y;
                    if (AttConstants.CAMERA_DEGREE == 0 || AttConstants.CAMERA_DEGREE == 180) {
                        scale_x = ((double) width / widthPreview);
                        scale_y = ((double) height / heightPreview);
                    } else {
                        // 90 || 270
                        scale_x = ((double) width / heightPreview);
                        scale_y = ((double) height / widthPreview);
                    }
                    int x0 = (int) (_x0 * scale_x);
                    int y0 = (int) (_y0 * scale_y);
                    int x1 = (int) (_x1 * scale_x);
                    int y1 = (int) (_y1 * scale_y);

                    if (AttConstants.LEFT_RIGHT) {
                        int x0b = width - x1;
                        int x1b = width - x0;
                        x0 = x0b;
                        x1 = x1b;
                    }

                    if (AttConstants.TOP_BOTTOM) {
                        int y0b = height - y1;
                        int y1b = height - y0;
                        y0 = y0b;
                        y1 = y1b;
                    }
                    //原始图片人脸坐标映射到MaskView上的坐标位置
                    rect.set(x0, y0, x1, y1);

                    if (ConfigLib.detectWithLiveness || ConfigLib.detectWithInfraredLiveness || ConfigLib.doubleCameraWithInfraredLiveness) {
                        if (faceBean.mLiveBean.livenessTag == faceBean.mLiveBean.FAKE) {
                            mPaint.setColor(red);
                            mTextPaint.setColor(red);
                            LogUtils.d(DetectPresenterImpl.HEAD, "draw [ maskview ] RED");
                        }else if (faceBean.mLiveBean.livenessTag == faceBean.mLiveBean.LIVE){
                            mPaint.setColor(green);
                            mTextPaint.setColor(green);
                            LogUtils.d(DetectPresenterImpl.HEAD, "draw [ maskview ] GREEN");
                        }else{
                            mPaint.setColor(blue);
                            mTextPaint.setColor(blue);
                            LogUtils.d(DetectPresenterImpl.HEAD, "draw [ maskview ] UNKNOWN");
                        }
                    }else {
                        mPaint.setColor(blue);
                        mTextPaint.setColor(blue);
                        LogUtils.d(DetectPresenterImpl.HEAD, "draw [ maskview ] BLUE");
                    }

                    //人脸框
                    drawRectLine(canvas);

                    mTextPaint.setTextSize(40);
//                    int baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2;

                    String liveString="";
                    if(faceBean.mLiveBean != null && (ConfigLib.detectWithLiveness || ConfigLib.detectWithInfraredLiveness || ConfigLib.doubleCameraWithInfraredLiveness)){

                        if (faceBean.mLiveBean.livenessTag == faceBean.mLiveBean.FAKE) {
                            liveString = "假人";
                        }else if (faceBean.mLiveBean.livenessTag == faceBean.mLiveBean.LIVE) {
                            liveString = "真人";
                        }else{
                            liveString="活体检测中...";
                        }
                    }

                    String maskString="";
                    if (faceBean.mDetectBean.coverStatus == 1) {
                        maskString = "未戴口罩";
                    }else if (faceBean.mDetectBean.coverStatus == 0) {
                        maskString = "戴口罩";
                    }else{
                        maskString = "口罩状态检测中...";
                    }


                    String helmetString="";
                    if (faceBean.mDetectBean.helmet == 0) {
                        helmetString = "未戴安全帽";
                    }else if (faceBean.mDetectBean.helmet == 1) {
                        helmetString = "戴安全帽";
                    }else{
                        helmetString = "安全帽检测中...";
                    }

                    String eyeStatusString="";
                    if (faceBean.mDetectBean.eyeStatus == 0) {
                        eyeStatusString = "闭眼";
                    }else if (faceBean.mDetectBean.eyeStatus == 1) {
                        eyeStatusString = "睁眼";
                    }else{
                        eyeStatusString = "眼睛状态检测中...";
                    }

                    String headAngleString="";
                    if (faceBean.mDetectBean.yaw != 1000) {
                        if(faceBean.mDetectBean.pitch > 0){
                            headAngleString = "侧脸:"+ (int)Math.abs(faceBean.mDetectBean.yaw) + "° 抬头:"+ (int)Math.abs(faceBean.mDetectBean.pitch) + "° 歪头:" + (int)Math.abs(faceBean.mDetectBean.roll) + "°";
                        }else{
                            headAngleString = "侧脸:"+ (int)Math.abs(faceBean.mDetectBean.yaw) + "° 低头:"+ (int)Math.abs(faceBean.mDetectBean.pitch) + "° 歪头:" + (int)Math.abs(faceBean.mDetectBean.roll) + "°";
                        }
                    }else{
                        headAngleString = "人脸角度检测中...";
                    }

                    String glassStatusString="";
                    if (faceBean.mDetectBean.glass == 0) {
                        glassStatusString = "未戴眼镜";
                    }else if (faceBean.mDetectBean.glass == 1) {
                        glassStatusString = "戴眼镜";
                    }else{
                        glassStatusString = "眼镜状态检测中...";
                    }

                    String hatStatusString="";
                    if (faceBean.mDetectBean.hat == 0) {
                        hatStatusString = "未戴帽子";
                    }else if (faceBean.mDetectBean.hat == 1) {
                        hatStatusString = "戴帽子";
                    }else{
                        hatStatusString = "帽子状态检测中...";
                    }


                    //show age:
                    //0~9: 1
                    //10~19:2
                    //20~29:3
                    //30~39:4
                    //40~49:5
                    //50~59:6
                    //60~:7
                    String ageString;
                    if(faceBean.mDetectBean.age == 1){
                        ageString = "年龄:0~9岁";
                    }else if(faceBean.mDetectBean.age == 2){
                        ageString = "年龄:10~19岁";
                    }else if(faceBean.mDetectBean.age == 3){
                        ageString = "年龄:20~29岁";
                    }else if(faceBean.mDetectBean.age == 4){
                        ageString = "年龄:30~39岁";
                    }else if(faceBean.mDetectBean.age == 5){
                        ageString = "年龄:40~49岁";
                    }else if(faceBean.mDetectBean.age == 6){
                        ageString = "年龄:50~59岁";
                    }else if(faceBean.mDetectBean.age == 7){
                        ageString = "年龄:60岁以上";
                    }else{
                        ageString = "年龄检测中...";
                    }

                    String genderString;
                    if(faceBean.mDetectBean.gender == 0){
                        genderString = "女-";
                    }else if(faceBean.mDetectBean.gender == 1){
                        genderString = "男-";
                    }else{
                        genderString ="性别检测中...";
                    }

                    int topY = 20;
                    int padding = 40;
                    String name = faceBean.mUserBean.name;
                    if (!TextUtils.isEmpty(name)) {
                        canvas.drawText(name, rect.centerX(), rect.top - topY, mTextPaint);
                        topY += padding;
                    }

                    if (!TextUtils.isEmpty(liveString)) {
                        canvas.drawText(liveString, rect.centerX(), rect.top - topY, mTextPaint);
                        topY += padding;
                    }
                    if (isShowMask && !TextUtils.isEmpty(maskString)) {
                        canvas.drawText(maskString, rect.centerX(), rect.top - topY, mTextPaint);
                        topY += padding;
                    }
                    if (isShowHelmet && !TextUtils.isEmpty(helmetString)) {
                        canvas.drawText(helmetString, rect.centerX(), rect.top - topY, mTextPaint);
                        topY += padding;
                    }
                    if (isShowEyeStatus && !TextUtils.isEmpty(eyeStatusString)) {
                        canvas.drawText(eyeStatusString, rect.centerX(), rect.top - topY, mTextPaint);
                        topY += padding;
                    }
                    if (isShowHeadAngle && !TextUtils.isEmpty(headAngleString)) {
                        canvas.drawText(headAngleString, rect.centerX(), rect.top - topY, mTextPaint);
                        topY += padding;
                    }
                    if (isShowGlass && !TextUtils.isEmpty(glassStatusString)) {
                        canvas.drawText(glassStatusString, rect.centerX(), rect.top - topY, mTextPaint);
                        topY += padding;
                    }
                    if (isShowHat && !TextUtils.isEmpty(hatStatusString)) {
                        canvas.drawText(hatStatusString, rect.centerX(), rect.top - topY, mTextPaint);
                        topY += padding;
                    }
                    if (isShowGAA && !TextUtils.isEmpty(genderString)) {
                        canvas.drawText(genderString, rect.centerX(), rect.top - topY, mTextPaint);
                        topY += padding;
                    }
                    if (isShowGAA && !TextUtils.isEmpty(ageString)) {
                        canvas.drawText(ageString, rect.centerX(), rect.top - topY, mTextPaint);
                        topY += padding;
                    }

                    if(AttConstants.DEBUG_SHOW_FACEINFO){
                        mTextPaint.setTextSize(30);
                        String f = "";
                        if (faceBean.mDetectBean != null) {
                            f += "ID : "+faceBean.mDetectBean.id;
                            f += getResources().getString(R.string.rect)+faceBean.mDetectBean.getFaceWidth()+"~"+ faceBean.mDetectBean.getFaceHeight();
                            f += "\r\n"+getResources().getString(R.string.blur)+faceBean.mDetectBean.blur + " NetBlur:" + faceBean.mDetectBean.netBlur;
                            f += "  Cs : "+faceBean.mDetectBean.coverStatus;
                            f +=" ";
                            f += "\r\n" + getResources().getString(R.string.light)+faceBean.mDetectBean.light;
                            f += "fs : "+ faceBean.mDetectBean.fdScore;
//                            f += "\r\n" + " QFBlur ："+faceBean.mDetectBean.QFBlur;
                        }

                        if (faceBean.mUserBean != null) {
                            f += "\r\n"+getResources().getString(R.string.score)+faceBean.mUserBean.compareScore;
                        }

                        if (faceBean.mLiveBean != null && (ConfigLib.detectWithLiveness || ConfigLib.detectWithInfraredLiveness || ConfigLib.doubleCameraWithInfraredLiveness)) {
                            String tag = "UNKNOWN";
                            if (faceBean.mLiveBean.livenessTag == faceBean.mLiveBean.FAKE) {
                                tag = "FAKE";
                            }
                            if (faceBean.mLiveBean.livenessTag == faceBean.mLiveBean.LIVE) {
                                tag = "LIVE";
                            }
                            if (faceBean.mLiveBean.livenessTag == faceBean.mLiveBean.UNKNOWN) {
                                tag = "UNKNOWN";
                            }
                            f+= "\r\n"+"TAG : "+tag;
                            f+= " -> FC : "+faceBean.mLiveBean.fakeCount+" | "+"LC : "+faceBean.mLiveBean.liveCount;
                            f+= "\r\n"+"LS : "+faceBean.mLiveBean.livenessScore;
                            f+= "\r\n"+"LS1 : "+faceBean.mLiveBean.livenessScore1;
                            if((ConfigLib.enhanceMode) && (ConfigLib.isAttaMode)){
                                f+= "\r\n"+"LT : "+ConfigLib.attackModeLiveThreshold;
                            }else {
                                f+= "\r\n"+"LT : "+ConfigLib.livenessThreshold;
                            }

                        }
                        StaticLayout layout = new StaticLayout(f, mTextPaint, canvas.getWidth(),
                                Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
                        canvas.save();
                        canvas.translate(rect.centerX(), rect.bottom+10);
                        layout.draw(canvas);
                        canvas.restore();
                    }
                }
            }
        }
    }

    private void drawRectLine(Canvas canvas) {
        //左上角
        int height = rect.height() / 4;
        int width = rect.width() / 4;
        Path path1 = new Path();
        path1.moveTo(rect.left, rect.top + height);
        path1.lineTo(rect.left, rect.top);
        path1.lineTo(rect.left + width, rect.top);
        canvas.drawPath(path1, mPaint);

        //右上角
        Path path2 = new Path();
        path2.moveTo(rect.right - width, rect.top);
        path2.lineTo(rect.right, rect.top);
        path2.lineTo(rect.right, rect.top + height);
        canvas.drawPath(path2, mPaint);

        //右下角
        Path path3 = new Path();
        path3.moveTo(rect.right, rect.bottom - height);
        path3.lineTo(rect.right, rect.bottom);
        path3.lineTo(rect.right - width, rect.bottom);
        canvas.drawPath(path3, mPaint);

        //左下角
        Path path4 = new Path();
        path4.moveTo(rect.left + width, rect.bottom);
        path4.lineTo(rect.left, rect.bottom);
        path4.lineTo(rect.left, rect.bottom - height);
        canvas.drawPath(path4, mPaint);
    }
}
