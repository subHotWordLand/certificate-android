package com.rongjingtaihegezheng.cert;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.aiwinn.base.module.log.LogUtils;
import com.aiwinn.base.util.ScreenUtils;
import com.aiwinn.facedetectsdk.common.ConfigLib;
import com.rongjingtaihegezheng.cert.common.AttConstants;
import com.rongjingtaihegezheng.cert.DetectView;
import com.aiwinn.facedetectsdk.FaceDetectManager;
import com.aiwinn.facedetectsdk.bean.DetectBean;
import com.aiwinn.facedetectsdk.bean.FaceBean;
import com.aiwinn.facedetectsdk.bean.LivenessBean;
import com.aiwinn.facedetectsdk.bean.UserBean;
import com.aiwinn.facedetectsdk.common.Status;
import com.aiwinn.facedetectsdk.listener.DebugRecognizeListener;
import com.aiwinn.facedetectsdk.listener.InfraredLiveListener;
import com.aiwinn.facedetectsdk.listener.RecognizeListener;
import com.rongjingtaihegezheng.cert.utils.FaceUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * com.aiwinn.faceattendance.ui.p
 * SDK_ATT
 * 2018/08/24
 * Created by LeoLiu on User
 */

public class DetectPresenterImpl implements DetectPresenter {

    public static final String HEAD = "ATT_DETECT";

    private DetectView mDetectView;

    public DetectPresenterImpl(DetectView detectView) {
        mDetectView = detectView;
        mLandScape = AttConstants.PORTRAIT_LANDSCAPE || ScreenUtils.isLandscape();
    }

    private boolean mLandScape;

    @Override
    public void detectFaceData(final byte[] data, final int w, final int h) {
        FaceDetectManager.recognizeFace(AttConstants.DETECT_DEFAULT ? "" : AttConstants.EXDB, data, w, h, new RecognizeListener() {
            @Override
            public void onDetectFace(List<FaceBean> faceBeanList) {
                //探测人脸结果回调，人脸框位置、活体状态、识别成功后跟踪识别信息

                //faceBean.mDetectBean.id           人脸跟踪id，跟踪id改变说明人脸跟丢
                //faceBean.mDetectBean.x0           人脸在原图左上角x坐标
                //faceBean.mDetectBean.y0           人脸在原图左上角y坐标
                //faceBean.mDetectBean.x1           人脸在原图右下角x坐标
                //faceBean.mDetectBean.y1           人脸在原图右下角y坐标
                //faceBean.mDetectBean.coverStatus  0戴口罩  1不戴口罩

                //faceBean.mLiveBean.livenessTag    0未知  1活体  2非活体

                //faceBean.mUserBean.name           识别成功name跟踪


                if (faceBeanList.size() > 0) {
                    // 使用 map 方法对 List 中的元素进行操作
                    List<DetectBean> detectBeanList = faceBeanList.stream()
                            .map(new Function<FaceBean, DetectBean>() {
                                public DetectBean apply(FaceBean faceBean) {
                                    // 在这里定义对每个元素进行的操作
                                    return faceBean.mDetectBean; // 例如将每个元素乘以 2
                                }
                            })
                            .collect(Collectors.toList());
                    DetectBean maxFace = FaceUtils.findMaxFace(detectBeanList);
                    com.aiwinn.base.log.LogUtils.d(HEAD, "blur = " + maxFace.blur + " light = " + maxFace.light);
                    if (!checkFaceIsRight(maxFace, w, h)) {
                        //没有居中
                        return;
                    }
                    if (maxFace.blur > ConfigLib.blurRecognizeThreshold && maxFace.light > ConfigLib.minRegisterBrightness) {
                        Bitmap bitmap = FaceDetectManager.yuvToBitmap(maxFace.faceBigPicData, w, h);
                        if (bitmap == null) {
//                            mView.onError(Status.Nv21ToBitmapFail);
                        } else {
                            mDetectView.detectFace(faceBeanList, maxFace, bitmap);
//                            mView.faceInfo(bitmap, maxFace);
                        }
                    } else {
//                        dealFaceInfoFinish();
                    }

                    LogUtils.d(DetectPresenterImpl.HEAD, "faceBeanList_mDetectBean_id=" + faceBeanList.get(0).mDetectBean.id);
                    //coverStatus == 0 戴口罩  coverStatus == 1 不戴口罩
                    LogUtils.d(HEAD, "detect mask = " + faceBeanList.get(0).mDetectBean.coverStatus);
                } else {
                    mDetectView.detectNoFace();
                }
            }

            boolean checkFaceIsRight(DetectBean detectBean, int width, int height) {
                if (!mLandScape) {
                    int bak = width;
                    width = height;
                    height = bak;
                }
                float x0 = detectBean.x0;
                float y0 = detectBean.y0;
                float x1 = detectBean.x1;
                float y1 = detectBean.y1;
                float w = x1 - x0;
                float h = y1 - y0;
                if (w > width
                        || h > height
                        || x0 < 0
                        || y0 < 0
                        || x1 > width
                        || y1 > height) {
                    com.aiwinn.base.log.LogUtils.d(HEAD, "detectBean.x1-detectBean.x0 = " + w);
                    com.aiwinn.base.log.LogUtils.d(HEAD, "detectBean.y1-detectBean.y0 = " + h);
                    com.aiwinn.base.log.LogUtils.d(HEAD, "detectBean.x1 = " + x1);
                    com.aiwinn.base.log.LogUtils.d(HEAD, "detectBean.y1 = " + y1);
                    com.aiwinn.base.log.LogUtils.d(HEAD, "image.getWidth() = " + width);
                    com.aiwinn.base.log.LogUtils.d(HEAD, "image.getHeight() = " + height);
                    com.aiwinn.base.log.LogUtils.d(HEAD, "check face fail : not center in pic");
                    return false;
                }
                return true;
            }

            @Override
            public void onLiveness(LivenessBean livenessBean) {
                //活体状态回调，不需要处理
                //livenessBean.mLiveBean.livenessTag == livenessBean.mLiveBean.LIVE     活体
                //livenessBean.mLiveBean.livenessTag == livenessBean.mLiveBean.FAKE     非活体
                //livenessBean.mLiveBean.livenessTag == livenessBean.mLiveBean.UNKNOWN  未知

            }

            @Override
            public void onRecognize(UserBean userBean, DetectBean detectBean) {
                //识别匹配人脸库结果回调

                //userBean.name             注册人员name(为空时没有识别到注册人员)
                //userBean.userId           注册人员userId(为空时没有识别到注册人员)
                //userBean.localImagePath   注册人员注册是保存的图片本地路径(为空时没有识别到注册人员)
                //userBean.compareScore     匹配注册人员库最大的比对值
                //userBean.features         提取当前识别人员的特征值
                //userBean.headImage        当前识别抓拍人脸头像图bitmap

                //detectBean.faceBigPicData 原始图像byte[]数据
                //detectBean.isMask()       true 戴口罩 false 不带口罩
                LogUtils.d(HEAD, "recognize mask status = " + detectBean.coverStatus);
                if (TextUtils.isEmpty(userBean.name)) {
                    mDetectView.recognizeFaceNotMatch(userBean);
                } else {
                    mDetectView.recognizeFace(userBean);
                }
            }

            @Override
            public void onError(Status status) {
                mDetectView.detectFail(status);
            }
        }, new DebugRecognizeListener() {
            @Override
            public void onRemove(FaceBean faceBean) {
                mDetectView.debug(faceBean);
            }
        });
    }

    @Override
    public void detectInfraredLiveData(byte[] data, int w, int h, int degree) {

        long start = System.currentTimeMillis();
        // 红外摄像头数据调用探测活体接口，注意相机数据degree需要正确，不同设备角度有差异
        // 能够稳定的探测到人脸degree即为正确
        FaceDetectManager.detectInfraredLiveness(data, w, h, degree, new InfraredLiveListener() {
            @Override
            public void onDetectFace(List<FaceBean> faceBeanList) {
                LogUtils.d(HEAD, "onDetectLiveness size = " + faceBeanList.size());
                if (faceBeanList.size() == 0) {
                    mDetectView.detectInfraredInfo("No Face");
                } else {
                    mDetectView.detectInfraredInfo("Find Face " + faceBeanList.size());
                }
            }

            @Override
            public void onDetectLiveness(LivenessBean liveBean, float value) {
                mDetectView.detectInfraredInfo("Live Scor: " + value);
                LogUtils.d(HEAD, "onDetectLiveness " + value);
            }

            @Override
            public void onError(int code, String msg) {
                mDetectView.detectInfraredInfo(msg);
                LogUtils.e(HEAD, "onDetectLiveness error" + msg);
            }
        });
        LogUtils.d(HEAD, "detectInfraredLiveData time " + (System.currentTimeMillis() - start));
    }
}
