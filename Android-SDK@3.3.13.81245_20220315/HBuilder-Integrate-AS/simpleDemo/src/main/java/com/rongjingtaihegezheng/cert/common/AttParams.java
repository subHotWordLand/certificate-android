package com.rongjingtaihegezheng.cert.common;

import com.aiwinn.facedetectsdk.common.ConfigLib;
import com.aiwinn.facedetectsdk.common.Constants;
import com.rongjingtaihegezheng.cert.AttApp;

public class AttParams {

    public static void initSpParams() {
        //探测界面人脸框位置左右镜像时设置
        AttConstants.LEFT_RIGHT = AttApp.sp.getBoolean(AttConstants.PREFS_LR, AttConstants.LEFT_RIGHT);
        //探测界面人脸框位置上下镜像时设置
        AttConstants.TOP_BOTTOM = AttApp.sp.getBoolean(AttConstants.PREFS_TB, AttConstants.TOP_BOTTOM);
        //设备竖屏预览横屏相机画面
        //摄像头安装角度为0或180度时，全屏预览成像上下拉伸变形时，调整预览view宽高比例
        AttConstants.PORTRAIT_LANDSCAPE = AttApp.sp.getBoolean(AttConstants.PREFS_PORTRAIT_LANDSCAPE, AttConstants.PORTRAIT_LANDSCAPE);

        //是否支持戴口罩识别注册人员，需要开启该功能后注册的人员才支持
        ConfigLib.isMaskMode = AttApp.sp.getBoolean(AttConstants.PREFS_SET_MASK_RECOGNIZE, ConfigLib.isMaskMode);
        //RGB活体识别
        ConfigLib.detectWithLiveness = AttApp.sp.getBoolean(AttConstants.PREFS_LIVENESS, ConfigLib.detectWithLiveness);
        //RGB活体阈值
        ConfigLib.livenessAwRGBThreshold1 = AttApp.sp.getFloat(AttConstants.PREFS_LIVENESST, ConfigLib.livenessAwRGBThreshold1);
        //识别匹配注册人员阈值
        ConfigLib.featureThreshold = AttApp.sp.getFloat(AttConstants.PREFS_UNLOCK, ConfigLib.featureThreshold);
        //多帧活体判断活体帧数，活体策略：活体多帧探测结果为活体计数达到阈值才认为是活体，非活体同理
        ConfigLib.livenessLiveNum = AttApp.sp.getInt(AttConstants.PREFS_LIVECOUNT, ConfigLib.livenessLiveNum);
        //多帧活体判断非活体帧数
        ConfigLib.livenessFakeNum = AttApp.sp.getInt(AttConstants.PREFS_FAKECOUNT, ConfigLib.livenessFakeNum);
        //默认注册库/扩展库
        AttConstants.REGISTER_DEFAULT = AttApp.sp.getBoolean(AttConstants.PREFS_REGISTER_DEFAULT, AttConstants.REGISTER_DEFAULT);
        //默认识别库/扩展库
        AttConstants.DETECT_DEFAULT = AttApp.sp.getBoolean(AttConstants.PREFS_DETECT_DEFAULT, AttConstants.DETECT_DEFAULT);
        //双目活体开关
        ConfigLib.doubleCameraWithInfraredLiveness = AttApp.sp.getBoolean(AttConstants.PREFS_OPEN_DOUBLE_CAMERA, ConfigLib.doubleCameraWithInfraredLiveness);
        //记录是否双目活体功能初始化
        //初始化前未设置开启双目功能参数，应用内切换设置开启需要重启生效
        AttConstants.DUL_CAMERA_IR_INIT = ConfigLib.doubleCameraWithInfraredLiveness;
        //双目探测人脸重合度阈值
        ConfigLib.overlayAreaThreshold = AttApp.sp.getFloat(AttConstants.PREFS_FACE_OVERLAY, ConfigLib.overlayAreaThreshold);
        //双目活体阈值，活体策略：
        //活体值 > Threshold1                 1帧直接判定为活体
        //Threshold1 >= 活体值 >= Threshold2  多帧判断活体结果
        //Threshold2 > 活体值                 多帧判断非活体结果
        ConfigLib.livenessInfraredThreshold1 = AttApp.sp.getFloat(AttConstants.PREFS_LIVENESS_INFRAREDT1, ConfigLib.livenessInfraredThreshold1);
        ConfigLib.livenessInfraredThreshold2 = AttApp.sp.getFloat(AttConstants.PREFS_LIVENESS_INFRAREDT2, ConfigLib.livenessInfraredThreshold2);
        //戴口罩时活体阈值
        ConfigLib.livenessInfraredMaskThreshold1 = AttApp.sp.getFloat(AttConstants.PREFS_LIVENESS_INFRAREDT_MASK1, ConfigLib.livenessInfraredMaskThreshold1);
        ConfigLib.livenessInfraredMaskThreshold2 = AttApp.sp.getFloat(AttConstants.PREFS_LIVENESS_INFRAREDT_MASK2, ConfigLib.livenessInfraredMaskThreshold2);


        //是否使用开源UVC Camera库开启摄像头，部分平台设备驱动不支持使用原生Camera API打开外接USB摄像头
        AttConstants.UVC_MODE = AttApp.sp.getBoolean(AttConstants.PREFS_UVC, AttConstants.UVC_MODE);
        //相机打开分辨率
        AttConstants.CAMERA_PREVIEW_HEIGHT = AttApp.sp.getInt(AttConstants.PREFS_CAMERA_PREVIEW_SIZE, AttConstants.CAMERA_PREVIEW_HEIGHT);
        //RGB相机ID
        AttConstants.CAMERA_ID = AttApp.sp.getInt(AttConstants.PREFS_CAMERA_ID, AttConstants.CAMERA_ID);
        //是否手动设置RGB相机数据角度和预览角度
        //标准设备可根据公式计算相机数据角度和显示预览角度，非标准设备根据设备手动设置
        AttConstants.SET_RGB_CAM_PARAMS = AttApp.sp.getBoolean(AttConstants.PREFS_SET_RGB_CAM_PARAMS, AttConstants.SET_RGB_CAM_PARAMS);
        //RGB相机数据角度
        AttConstants.CAMERA_DEGREE = AttApp.sp.getInt(AttConstants.PREFS_CAMERA_DEGREE, AttConstants.CAMERA_DEGREE);
        //RGB相机预览角度
        AttConstants.PREVIEW_DEGREE = AttApp.sp.getInt(AttConstants.PREFS_PREVIEW_DEGREE, AttConstants.PREVIEW_DEGREE);
        //IR相机ID
        AttConstants.CAMERA_ID_INFRARED = AttApp.sp.getInt(AttConstants.PREFS_CAMERA_ID_INFRARED, AttConstants.CAMERA_ID_INFRARED);
        //是否手动设置IR相机数据角度和预览角度
        AttConstants.SET_IR_CAM_PARAMS = AttApp.sp.getBoolean(AttConstants.PREFS_SET_IR_CAM_PARAMS, AttConstants.SET_IR_CAM_PARAMS);
        //IR相机数据角度
        AttConstants.CAMERA_DEGREE_INFRARED = AttApp.sp.getInt(AttConstants.PREFS_CAMERA_DEGREE_INFRARED, AttConstants.CAMERA_DEGREE_INFRARED);
        //IR相机预览角度
        AttConstants.PREVIEW_DEGREE_INFRARED = AttApp.sp.getInt(AttConstants.PREFS_PREVIEW_DEGREE_INFRARED, AttConstants.PREVIEW_DEGREE_INFRARED);
        //界面显示预览是否镜像
        AttConstants.PREVIEW_MIRROR = AttApp.sp.getBoolean(AttConstants.PREFS_SET_PREVIEW_MIRROR, AttConstants.PREVIEW_MIRROR);


        //debug activity settings
        AttConstants.DEBUG = AttApp.sp.getBoolean(AttConstants.PREFS_DEBUG, AttConstants.DEBUG);
        //保存rgb拒真图
        Constants.DEBUG_SAVE_FAKE = AttApp.sp.getBoolean(AttConstants.PREFS_FAKE, Constants.DEBUG_SAVE_FAKE);
        //保存rgb攻破图
        Constants.DEBUG_SAVE_LIVE = AttApp.sp.getBoolean(AttConstants.PREFS_LIVE, Constants.DEBUG_SAVE_LIVE);
        //保存ir拒真图
        Constants.DEBUG_SAVE_INFRARED_FAKE = AttApp.sp.getBoolean(AttConstants.PREFS_SAVEINFRAREDFAKE, Constants.DEBUG_SAVE_INFRARED_FAKE);
        //保存ir攻破图
        Constants.DEBUG_SAVE_INFRARED_LIVE = AttApp.sp.getBoolean(AttConstants.PREFS_SAVEINFRAREDLIVE, Constants.DEBUG_SAVE_INFRARED_FAKE);
        //保存无人脸图
        Constants.DEBUG_SAVE_NOFACE = AttApp.sp.getBoolean(AttConstants.PREFS_SAVENOFACEDATA, Constants.DEBUG_SAVE_NOFACE);
        //保存识别对比图
        Constants.DEBUG_SAVE_SIMILARITY_SMALL = AttApp.sp.getBoolean(AttConstants.PREFS_SAVESSDATA, Constants.DEBUG_SAVE_SIMILARITY_SMALL);
    }
}
