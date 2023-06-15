package com.rongjingtaihegezheng.cert;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiwinn.base.activity.BaseActivity;
import com.aiwinn.base.camera.AndroidCamera;
import com.aiwinn.base.camera.BaseCamera;
import com.aiwinn.base.camera.CameraConfig;
import com.aiwinn.base.camera.CameraUtil;
import com.aiwinn.base.camera.ICamera;
import com.aiwinn.base.camera.Size;
import com.aiwinn.base.log.LogUtils;
import com.aiwinn.base.util.ScreenUtils;
import com.aiwinn.base.util.StringUtils;
import com.aiwinn.base.util.TimeUtils;
import com.aiwinn.base.util.ToastUtils;
import com.aiwinn.base.widget.CameraSurfaceView;
import com.aiwinn.deblocks.utils.FeatureUtils;
import com.rongjingtaihegezheng.cert.adapter.DetectAdapter;
import com.rongjingtaihegezheng.cert.bean.DetectFaceBean;
import com.rongjingtaihegezheng.cert.utils.TTSUtils;
import com.aiwinn.facedetectsdk.FaceDetectManager;
import com.aiwinn.facedetectsdk.bean.DetectBean;
import com.aiwinn.facedetectsdk.bean.FaceBean;
import com.aiwinn.facedetectsdk.bean.UserBean;
import com.aiwinn.facedetectsdk.common.ConfigLib;
import com.aiwinn.facedetectsdk.common.Status;
import com.aiwinn.facedetectsdk.listener.ExtractFeatureListener;
import com.bumptech.glide.Glide;
import com.rongjingtaihegezheng.cert.common.AttConstants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * com.aiwinn.faceattendance.ui.m
 * SnapShot
 * 2018/08/24
 * Created by LeoLiu on User
 */

public class DetectActivity extends BaseActivity implements DetectView {

    private RelativeLayout mOther;
    private RecyclerView mRecyclerView;
    private MaskView mMaskView;
    private TextureView mSurfaceView1;
    private CameraSurfaceView mSurfaceView2;
    private TextView mTextView;
    private TextView mDebug;
    private ImageView mBack;
    private DetectPresenter mPresenter;
    private DetectAdapter mDetectAdapter;
    private ArrayList<DetectFaceBean> detectList;
    private DetectHandler mHandler;
    private int mPreviewWidth;
    private int mPreviewHeight;

    private final static int MSG_DETECT_NO_FACE = 0;
    private final static int MSG_DETECT_FAIL = 1;
    private final static int MSG_DETECT_DATA = 2;
    private final static int MSG_FACE = 3;

    private TextView mTvCompare;
    private TextView mTvCompareCancel;
    private TextView mTvCompareMsg;
    private ImageView mImCompareWait;
    private ImageView mImCompareSucc;

    boolean isAsyncDoing = false;
    private String mInfraredMsg = "No Face";
    private byte[] mCurrentPicData;
    private BaseCamera mRgbCamera;
    private BaseCamera mRedCamera;
    private int mInfraDataDegree;

    void doAsync(){
        isAsyncDoing = true;
    }

    void doAsyncDone(){
        isAsyncDoing = false;
    }

    ArrayList<Float> features = new ArrayList<>();

    private static class DetectHandler extends Handler {

        final WeakReference<DetectActivity> mActivity;

        public DetectHandler(DetectActivity detectActivity) {
            mActivity = new WeakReference<>(detectActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_DETECT_DATA:
                    mActivity.get().updateMessgae((String) msg.obj);
                    break;

                case MSG_DETECT_FAIL:
                    mActivity.get().updateMessgae(((Status) msg.obj).toString());
                    mActivity.get().mMaskView.clearRect();
                    break;

                case MSG_DETECT_NO_FACE:
                    mActivity.get().updateMessgae(mActivity.get().getResources().getString(R.string.no_face));
                    mActivity.get().mMaskView.clearRect();
                    break;

                case MSG_FACE:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    Glide.with(mActivity.get()).load(bitmap).into(mActivity.get().mImCompareSucc);
                    break;

            }
        }
    }

    void updateMessgae(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("w = " + mPreviewWidth + " h = " + mPreviewHeight + getInfraredMsg());
        stringBuilder.append("\n");
        stringBuilder.append(s);
        mTextView.setText(stringBuilder.toString());
    }

    @NonNull
    private String getInfraredMsg() {
        if (!ConfigLib.doubleCameraWithInfraredLiveness) return "";

        return " || Infrared " + mInfraredMsg;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_detect;
    }

    @Override
    public void initViews() {
        AttApp.hideBottomUIMenu(this);
        mOther = findViewById(R.id.other);
        mTvCompare = findViewById(R.id.tvcompare);
        mTvCompareCancel = findViewById(R.id.tvcomparecancel);
        mTvCompareMsg = findViewById(R.id.tvcomparemsg);
        mImCompareWait = findViewById(R.id.imcomparewait);
        mImCompareSucc = findViewById(R.id.imcomparesucc);
        mSurfaceView1 = findViewById(R.id.sv);
        if (AttConstants.PREVIEW_MIRROR){
            mSurfaceView1.setScaleX(-mSurfaceView1.getScaleX());
        }
        mSurfaceView2 = findViewById(R.id.sv_2);
        mSurfaceView2.setZOrderMediaOverlay(true);
        mRecyclerView = findViewById(R.id.rv);
        mBack = findViewById(R.id.back);
        mTextView = findViewById(R.id.message);
        mDebug = findViewById(R.id.debugmessage);
        mMaskView = findViewById(R.id.kcfmv);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    public void initData() {
        mHandler = new DetectHandler(this);
        mPresenter = new DetectPresenterImpl(this);
        LinearLayoutManager detectRvManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(detectRvManager);
        detectList = new ArrayList<>();
        detectList.clear();
        mDetectAdapter = new DetectAdapter(detectList);
        mRecyclerView.setAdapter(mDetectAdapter);
        if (!ScreenUtils.isLandscape()) {
            mOther.setVisibility(View.GONE);
        }

        setupDegree();

        int[] size = getSize();
        mPreviewWidth = size[0];
        mPreviewHeight = size[1];
        reLayoutSurfaceView();
        setupCamera();
    }

    private void setupDegree() {
        if (!AttConstants.SET_RGB_CAM_PARAMS) {
            //标准设备可根据公式计算相机数据角度和显示预览角度，非标准设备根据设备手动设置
            //将自动匹配的数据角度写到sp
            AttConstants.CAMERA_DEGREE = CameraUtil.getCameraDataDegree(this, AttConstants.CAMERA_ID);
            AttApp.sp.edit().putInt(AttConstants.PREFS_CAMERA_DEGREE, AttConstants.CAMERA_DEGREE).commit();
            //将自动匹配的预览角度写到sp
            AttConstants.PREVIEW_DEGREE = CameraUtil.getDisplayOrientation(this, AttConstants.CAMERA_ID);
            AttApp.sp.edit().putInt(AttConstants.PREFS_PREVIEW_DEGREE, AttConstants.PREVIEW_DEGREE).commit();
        }
        FaceDetectManager.setDegree(AttConstants.CAMERA_DEGREE);

        if (ConfigLib.doubleCameraWithInfraredLiveness && !AttConstants.SET_IR_CAM_PARAMS) {
            AttConstants.CAMERA_DEGREE_INFRARED = CameraUtil.getCameraDataDegree(this, AttConstants.CAMERA_ID_INFRARED);
            AttApp.sp.edit().putInt(AttConstants.PREFS_CAMERA_DEGREE_INFRARED, AttConstants.CAMERA_DEGREE_INFRARED).commit();
            //将自动匹配的预览角度写到sp
            AttConstants.PREVIEW_DEGREE_INFRARED = CameraUtil.getDisplayOrientation(this, AttConstants.CAMERA_ID_INFRARED);
            AttApp.sp.edit().putInt(AttConstants.PREFS_PREVIEW_DEGREE_INFRARED, AttConstants.PREVIEW_DEGREE_INFRARED).commit();
        }
        mInfraDataDegree = AttConstants.CAMERA_DEGREE_INFRARED;
    }

    public void setupCamera() {
        if (checkSupportSize()) return;

        CameraConfig rgbConfig = new CameraConfig.Builder()
                .bindDisplayView(mSurfaceView1)
                .setCameraId(AttConstants.CAMERA_ID)
                .setDisplayOrientation(AttConstants.PREVIEW_DEGREE)
                .setPreviewSize(mPreviewWidth, mPreviewHeight)
                .setDataCallback(mCallBack)
                .build();
        mRgbCamera = new AndroidCamera(rgbConfig);

        if (ConfigLib.doubleCameraWithInfraredLiveness) {
            CameraConfig redConfig = new CameraConfig.Builder()
                    .bindDisplayView(mSurfaceView2)
                    .setCameraId(AttConstants.CAMERA_ID_INFRARED)
                    .setDisplayOrientation(AttConstants.PREVIEW_DEGREE_INFRARED)
                    .setPreviewSize(mPreviewWidth, mPreviewHeight)
                    .setDataCallback(mCallBack)
                    .build();
            mRedCamera = new AndroidCamera(redConfig);
        }
    }

    private boolean checkSupportSize() {
        if (!CameraUtil.getSupportPreviewSizes(AttConstants.CAMERA_ID).contains(new Size(mPreviewWidth, mPreviewHeight))) {
            ToastUtils.showShort("RGB相机不支持分辨率 " + mPreviewWidth + "x" + mPreviewHeight);
            return true;
        }
        if (ConfigLib.doubleCameraWithInfraredLiveness) {
            if (!CameraUtil.getSupportPreviewSizes(AttConstants.CAMERA_ID_INFRARED).contains(new Size(mPreviewWidth, mPreviewHeight))) {
                ToastUtils.showShort("Ir相机不支持分辨率 " + mPreviewWidth + "x" + mPreviewHeight);
                return true;
            }
        }
        return false;
    }
    /**
     * 处理预览显示不变形，显示view显示比例需要和打开相机分辨率一致
     */
    public void reLayoutSurfaceView() {
        int screenWidth = ScreenUtils.getScreenWidth();
        int screenHeight = ScreenUtils.getScreenHeight();
        //屏幕h/w比例
        float screenRatio = screenWidth > screenHeight ? (float) screenWidth / screenHeight : (float) screenHeight / screenWidth;
        //相机数据输出比例 mPreviewWidth > mPreviewHeight
        float cameraDataRatio = (float) mPreviewWidth / mPreviewHeight;
        int viewWidth = 0, viewHeight = 0, marginY = 0, marginX = 0;
        boolean fullWidth = false;
        if (ScreenUtils.isPortrait()) {
            //竖屏
            if (AttConstants.CAMERA_DEGREE == 90 || AttConstants.CAMERA_DEGREE == 270) {
                //摄像头相机数据 宽 < 高
                fullWidth = screenRatio < cameraDataRatio;
                if (screenRatio - cameraDataRatio > 0.3) {
                    //640*480和屏幕比例相差比较大设备
                    fullWidth = true;
                }
                if (fullWidth) {
                    //预览view宽度和屏幕一致，高度按相机分辨率比例计算
                    viewWidth = screenWidth;
                    viewHeight = (int) (screenWidth * cameraDataRatio);
                    //预览view距离上下边距，大于0内缩，小于0外扩超出屏幕距离
                    marginY = (screenHeight - viewHeight) / 2;
                } else {
                    //预览view高度和屏幕一致，宽度按相机分辨率比例计算
                    viewHeight = screenHeight;
                    viewWidth = (int) (screenHeight / cameraDataRatio);
                    //预览view距离左右边距，大于0内缩，小于0外扩超出屏幕距离
                    marginX = (screenWidth - viewWidth) / 2;
                }
            } else {
                //摄像头相机数据 宽 > 高
                //预览view宽和屏幕保持一致，高度按相机数据比例计算
                fullWidth = true;
                viewWidth = screenWidth;
                viewHeight = (int) (screenWidth / cameraDataRatio);
                marginY = (screenHeight - viewHeight) / 2;
            }
        } else {
            //横屏
            if (AttConstants.CAMERA_DEGREE == 0 || AttConstants.CAMERA_DEGREE == 180) {
                //摄像头相机数据 宽 < 高
                fullWidth = screenRatio > cameraDataRatio;
                if (screenRatio - cameraDataRatio > 0.3) {
                    //640*480和屏幕比例相差比较大设备
                    fullWidth = false;
                }
                if (fullWidth) {
                    //预览view宽度和屏幕一致，高度按相机分辨率比例计算
                    viewWidth = screenWidth;
                    viewHeight = (int) (screenWidth / cameraDataRatio);
                    //预览view距离上下边距，大于0内缩，小于0外扩超出屏幕距离
                    marginY = (screenHeight - viewHeight) / 2;
                } else {
                    //预览view高度和屏幕一致，宽度按相机分辨率比例计算
                    viewHeight = screenHeight;
                    viewWidth = (int) (screenHeight * cameraDataRatio);
                    //预览view距离左右边距，大于0内缩，小于0外扩超出屏幕距离
                    marginX = (screenWidth - viewWidth) / 2;
                }
            } else {
                //摄像头相机数据 宽 > 高
                //预览view宽和屏幕保持一致，高度按相机数据比例计算
                viewHeight = screenHeight;
                viewWidth = (int) (screenHeight / cameraDataRatio);
                marginX = (screenWidth - viewWidth) / 2;
            }
        }
        LogUtils.i(TAG, "reLayoutSurfaceView: isPortrait= " + ScreenUtils.isPortrait() + ", displayDegree=" + AttConstants.PREVIEW_DEGREE + ", cameraDataDegree=" + AttConstants.CAMERA_DEGREE);
        LogUtils.i(TAG, "reLayoutSurfaceView: screenWidth=" + screenWidth + ", screenHeight=" + screenHeight);
        LogUtils.i(TAG, "reLayoutSurfaceView: mPreviewWidth=" + mPreviewWidth + ", mPreviewHeight=" + mPreviewHeight);
        LogUtils.i(TAG, "reLayoutSurfaceView: screenRatio=" + screenRatio + ", cameraDataRatio=" + cameraDataRatio + ", fullWidth=" + fullWidth);
        LogUtils.i(TAG, "reLayoutSurfaceView: viewWidth=" + viewWidth + ", viewHeight=" + viewHeight);
        LogUtils.i(TAG, "reLayoutSurfaceView: marginX=" + marginX + ", marginY=" + marginY);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSurfaceView1.getLayoutParams();
        layoutParams.width = viewWidth;
        layoutParams.height = viewHeight;
        layoutParams.topMargin = marginY;
        layoutParams.bottomMargin = marginY;
        layoutParams.leftMargin = marginX;
        layoutParams.rightMargin = marginX;
        mSurfaceView1.setLayoutParams(layoutParams);
        mMaskView.setLayoutParams(layoutParams);

        if (ConfigLib.doubleCameraWithInfraredLiveness) {
            //红外显示左下角
            mSurfaceView2.setVisibility(View.VISIBLE);
            final RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) mSurfaceView2.getLayoutParams();
            layoutParams2.width = viewWidth / 4;
            layoutParams2.height = viewHeight / 4;
            layoutParams2.leftMargin = Math.max(marginX, 0);
            layoutParams2.bottomMargin = Math.max(marginY, 0);
            mSurfaceView2.setLayoutParams(layoutParams2);
        }
    }

    private final ICamera.CameraCallBack mCallBack = new ICamera.CameraCallBack() {
        @Override
        public void cameraData(byte[] data, CameraConfig config) {
            if (config.getCameraId() == AttConstants.CAMERA_ID) {
                if (mCurrentPicData == null){
                    mCurrentPicData = new byte[data.length];
                }
                System.arraycopy(data, 0, mCurrentPicData, 0, mCurrentPicData.length);
                LogUtils.d(DetectPresenterImpl.HEAD, "Begin -> ( w = " + config.getPreviewWidth() + " h = " + config.getPreviewHeight() + " size = " + data.length + " )" + Thread.currentThread().getName());
                if (check()) {
                    mPresenter.detectFaceData(data, config.getPreviewWidth(), config.getPreviewHeight());
                }
            } else {
                LogUtils.d(DetectPresenterImpl.HEAD, "Begin2 -> ( w2 = " + config.getPreviewWidth() + " h2 = " + config.getPreviewHeight() + " size2 = " + data.length + " )" + Thread.currentThread().getName());
                mPresenter.detectInfraredLiveData(data, config.getPreviewWidth(), config.getPreviewHeight(), mInfraDataDegree);
            }
        }
    };

    private int[] getSize(){
        switch (AttConstants.CAMERA_PREVIEW_HEIGHT) {
            case 640:
                return new int[]{480, 640};
            case 720:
                return new int[]{1280, 720};
            case 1280:
                return new int[]{720, 1280};
            case 1080:
                return new int[]{1920, 1080};
            case 1920:
                return new int[]{1080, 1920};
            case 960:
                return new int[]{1280, 960};
            case 1280 * 2:
                return new int[]{960, 1280};
            default:
                return new int[]{640, 480};
        }
    }

    @Override
    public void initListeners() {
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttConstants.Detect_Exception = false;
                LogUtils.d(TAG, "DetectActivity_mBack_setOnClickListener");
                DetectActivity.this.finish();
            }
        });
        mTvCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAsyncDoing) {
                    return;
                }
                extractFeature();
            }
        });
        mTvCompareCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCompare();
            }
        });
    }

    //模拟刷卡读取身份证头像并提取人脸特征值
    private void extractFeature(){
        doAsync();
        if (mCurrentPicData == null){
            doAsyncDone();
            return;
        }
        Bitmap bitmap = FaceDetectManager.yuvToBitmap(mCurrentPicData, mPreviewWidth, mPreviewHeight);
        resetCompare();
        showSlotCardState(getResources().getString(R.string.parse_begin));
        Glide.with(DetectActivity.this).load(bitmap).into(mImCompareWait);
        LogUtils.d(DetectPresenterImpl.HEAD,"extractFeatureASync Parsing");
        FaceDetectManager.extractFeatureASync(bitmap, new ExtractFeatureListener() {

            @Override
            public void onSuccess(DetectBean detectBean, ArrayList<Float> floats) {
                LogUtils.d(DetectPresenterImpl.HEAD,"extractFeatureASync success");
                showSlotCardState(getResources().getString(R.string.parse_success));
                features.clear();
                features.addAll(floats);
                doAsyncDone();
            }

            @Override
            public void onError(final Status code) {
                LogUtils.d(DetectPresenterImpl.HEAD,"extractFeatureASync fail : "+code.toString());
                showSlotCardState(getResources().getString(R.string.parse_fail)+" : "+code.toString());
                doAsyncDone();
            }
        });
    }

    private void resetCompare(){
        mImCompareWait.setImageDrawable(null);
        mImCompareSucc.setImageDrawable(null);
        mTvCompareMsg.setText("");
        features.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AttConstants.Detect_Exception = true;
        if (mRgbCamera != null){
            showDialog(getResources().getString(R.string.load_camera));
            mRgbCamera.onResume();
        }
        if (ConfigLib.doubleCameraWithInfraredLiveness){
            if (mRedCamera != null) mRedCamera.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRgbCamera != null) mRgbCamera.onPause();
        if (ConfigLib.doubleCameraWithInfraredLiveness) {
            if (mRedCamera != null) mRedCamera.onPause();
        }
        DetectActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onResume","DetectActivity -> onDestroy > Detect_Exception "+AttConstants.Detect_Exception +" INIT_STATE "+AttConstants.INIT_STATE);
        detectList.clear();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mPresenter = null;
        mSurfaceView1 = null;
        ConfigLib.detectionFirstInitFlag = true;
        ConfigLib.detectionInfraredFirstInitFlag = true;
        LogUtils.d(DetectPresenterImpl.HEAD, "RecognitionFace face destroy " + detectList.size());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ToastUtils.showShort(getResources().getString(R.string.reopen_activity));
        DetectActivity.this.finish();
    }

    int initFrame = 1;
    int detectFrame = 4;
    int nowFrame = initFrame;
    boolean checkFrame = false;
    boolean check(){
        if(!checkFrame){
            nowFrame++;
            dissmisDialog();
            if (nowFrame >= detectFrame) {
                nowFrame = initFrame;
                checkFrame = true;
            }
        }
        return checkFrame;
    }

    @Override
    public void recognizeFace(final UserBean userBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (userBean) {
                    TTSUtils.speech(getString(R.string.recognize) + userBean.name);
                    String time = TimeUtils.millis2String(System.currentTimeMillis());
                    DetectFaceBean detectFaceBean = new DetectFaceBean(userBean, time);
                    if (detectList.size() != 0 && StringUtils.equals(detectList.get(0).getUserBean().name, userBean.name)) {
                        LogUtils.d(DetectPresenterImpl.HEAD, "RecognitionFace face set new + " + detectList.size());
                        detectList.set(0, detectFaceBean);
                    } else {
                        LogUtils.d(DetectPresenterImpl.HEAD, "RecognitionFace face add new + " + detectList.size());
                        detectList.add(0, detectFaceBean);
                    }
                    if(detectList.size() > AttConstants.DETECT_LIST_SIZE){
                        List<DetectFaceBean> list = new ArrayList<>();
                        list.clear();
                        list.addAll(detectList);
                        detectList.clear();
                        detectList.addAll(list.subList(0,AttConstants.DETECT_LIST_SIZE));
                    }
                    mDetectAdapter.replaceData(detectList);
                }
            }
        });
    }

    @Override
    public void recognizeFaceNotMatch(UserBean userBean) {
        if (features.size() > 0) {
            compareSlotCard(userBean);
        }
    }

    //比对人证相似度
    private void compareSlotCard(UserBean userBean) {
        LogUtils.d(DetectPresenterImpl.HEAD,"extractFeatureASync find recognizeFaceNotMatch");
        ArrayList<Float> floatArrayList = new ArrayList<>();
        floatArrayList.clear();
        floatArrayList.addAll(features);
        final float compare = FaceDetectManager.compareFeature(FeatureUtils.arrayListToFloat(floatArrayList), FeatureUtils.arrayListToFloat(userBean.features));
        LogUtils.d(DetectPresenterImpl.HEAD,"extractFeatureASync find recognizeFaceNotMatch : "+compare);
        if (compare > ConfigLib.featureThreshold) {
            Message message = Message.obtain();
            message.obj = userBean.headImage;
            message.what = MSG_FACE;
            mHandler.sendMessage(message);
            features.clear();
            showSlotCardState(getResources().getString(R.string.match_success)+" : "+compare);
        }else {
            showSlotCardState(getResources().getString(R.string.match_fail)+" : "+compare);
        }
    }

    private void showSlotCardState(final String state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvCompareMsg.setText(state);
            }
        });
    }

    @Override
    public void detectNoFace() {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_DETECT_NO_FACE);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lastDebugText = System.currentTimeMillis();
                mDebug.setText("");
            }
        });
    }

    @Override
    public void detectFail(Status status) {
        Message message = Message.obtain();
        message.what = MSG_DETECT_FAIL;
        message.obj = status;
        if (mHandler != null) {
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void detectFace(final List<FaceBean> faceBeans) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (faceBeans) {
                    List<FaceBean> faceBeanList = new ArrayList<>();
                    faceBeanList.clear();
                    faceBeanList.addAll(faceBeans);
                    StringBuilder stringBuilder = new StringBuilder();
                    try {
                        for (int i = 0; i < faceBeanList.size(); i++) {
                            FaceBean bean = faceBeanList.get(i);
                            if (bean.mUserBean != null && !StringUtils.isEmpty(bean.mUserBean.name)) {
                                String name = bean.mUserBean.name;
                                stringBuilder.append("< Find " + name + " >");
                                stringBuilder.append("\n");
                            } else {
                                String find = "";
                                if (ConfigLib.detectWithLiveness || ConfigLib.detectWithInfraredLiveness || ConfigLib.doubleCameraWithInfraredLiveness) {
                                    if (bean.mLiveBean != null && bean.mLiveBean.livenessTag == bean.mLiveBean.UNKNOWN) {
                                        find = "UNKNOWN";
                                    }else if (bean.mLiveBean != null && bean.mLiveBean.livenessTag == bean.mLiveBean.FAKE) {
                                        find = "FAKE";
                                    }else {
                                        find = bean.mUserBean.compareScore+"";
                                        Bitmap bitmap = bean.mUserBean.headImage;
                                    }
                                }else {
                                    if(bean.mUserBean != null){
                                        find = bean.mUserBean.compareScore+"";
                                        Bitmap bitmap = bean.mUserBean.headImage;
                                    }
                                }
                                stringBuilder.append("< Find " + find + " >");
                                stringBuilder.append("\n");
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    sendDebugMessage(stringBuilder.toString());
                    mMaskView.drawRect(faceBeanList, mPreviewWidth, mPreviewHeight);
                    if (System.currentTimeMillis() - lastDebugText > 1000) {
                        mDebug.setText("");
                    }
                }
            }
        });
    }

    long lastDebugText = 0;
    @Override
    public void debug(final FaceBean faceBean) {
        if (faceBean != null && faceBean.mDetectBean != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastDebugText = System.currentTimeMillis();
                    mDebug.setText("Remove :"+"\n"+"ID = "+faceBean.mDetectBean.id+"\n"+"Reason = "+faceBean.mDetectBean.faceState);
                }
            });
        }
    }

    @Override
    public void detectInfraredInfo(String faceMsg) {
        mInfraredMsg = faceMsg;
    }

    public void sendDebugMessage(String s) {
        Message message = Message.obtain();
        message.what = MSG_DETECT_DATA;
        message.obj = s;
        mHandler.sendMessage(message);
    }

    // 退出按键
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AttConstants.Detect_Exception = false;
        LogUtils.d(TAG, "DetectActivity_onBackPressed");

    }
}
