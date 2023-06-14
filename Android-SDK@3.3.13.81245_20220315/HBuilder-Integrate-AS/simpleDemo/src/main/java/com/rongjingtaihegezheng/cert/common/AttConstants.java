package com.rongjingtaihegezheng.cert.common;

import android.os.Environment;

import com.aiwinn.facedetectsdk.common.Status;

import java.io.File;

/**
 * com.aiwinn.faceattendance.common
 * SDK_ATT
 * 2018/08/24
 * Created by LeoLiu on User
 */

public class AttConstants {
    public static final String PREFS = "ATT_SP";
    public static final String EXDB = "EX";
    public static boolean REGISTER_DEFAULT = true;
    public static boolean DETECT_DEFAULT = true;

    public static boolean DEBUG = true;
    public static boolean DEBUG_SHOW_FACEINFO = false;
    public static boolean INIT_STATE = false;
    public static Status INIT_STATE_ERROR = null;

    //是否使用开源UVC Camera库开启摄像头，部分平台设备驱动不支持使用原生Camera API打开外接USB摄像头
    public static boolean UVC_MODE = false;

    //无法正确匹配角度参数，使用手动设置的相机参数
    public static boolean SET_RGB_CAM_PARAMS = false;
    public static boolean SET_IR_CAM_PARAMS = false;

    public static boolean LEFT_RIGHT = false;
    public static boolean TOP_BOTTOM = false;
    //竖屏时摄像头预览为横向画面
    public static boolean PORTRAIT_LANDSCAPE = false;

    //预览镜像
    public static boolean PREVIEW_MIRROR = false;
    //双目活体功能需初始化前设置生效，如果初始化时关闭在打开中设置保存后需要重启应用初始化
    public static boolean DUL_CAMERA_IR_INIT = false;

    public static int CAMERA_ID_INFRARED = 1;
    public static int CAMERA_DEGREE_INFRARED = 0;
    public static int PREVIEW_DEGREE_INFRARED = 0;

    public static int CAMERA_ID = 0;
    public static int CAMERA_DEGREE = 0;
    public static int PREVIEW_DEGREE = 0;

    public static int CAMERA_PREVIEW_HEIGHT = 0;

    public static int DETECT_LIST_SIZE = 10;

    public static boolean Detect_Exception = false;

    public static String SD_CARD = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static String PATH_AIWINN = SD_CARD+ File.separator+"aiwinn";

    public static String PATH_ATTENDANCE = PATH_AIWINN+ File.separator+"attendance";

    public static String PATH_BULK_REGISTRATION = PATH_ATTENDANCE+ File.separator+"bulkregistration";
    public static String PATH_CARD = PATH_ATTENDANCE+ File.separator+"testslotcard";

    public static final String PREFS_CAMERA_ID = "CAMERA_ID";
    public static final String PREFS_CAMERA_DEGREE = "CAMERA_DEGREE";
    public static final String PREFS_PREVIEW_DEGREE = "PREVIEW_DEGREE";
    public static final String PREFS_CAMERA_PREVIEW_SIZE = "CAMERA_PREVIEW_SIZE";
    public static final String PREFS_DETECT_DEFAULT = "DETECT_DEFAULT";
    public static final String PREFS_REGISTER_DEFAULT = "REGISTER_DEFAULT";
    public static final String PREFS_LIVENESS = "LIVENESS";
    public static final String PREFS_UNLOCK = "UNLOCK";
    public static final String PREFS_LIVENESST = "LIVENESST";
    public static final String PREFS_LIVECOUNT = "LIVECOUNT";
    public static final String PREFS_FAKECOUNT = "FAKECOUNT";
    public static final String PREFS_LR = "LR";
    public static final String PREFS_TB = "TB";
    public static final String PREFS_OPEN_DOUBLE_CAMERA = "OPEN_DOUBLE_CAMERA";
    public static final String PREFS_FACE_OVERLAY = "FACE_OVERLAY";
    public static final String PREFS_CAMERA_ID_INFRARED = "CAMERA_ID_INFRARED";
    public static final String PREFS_CAMERA_DEGREE_INFRARED = "CAMERA_DEGREE_INFRARED";
    public static final String PREFS_PREVIEW_DEGREE_INFRARED = "PREVIEW_DEGREE_INFRARED";
    public static final String PREFS_PORTRAIT_LANDSCAPE = "PORTRAIT_LANDSCAPE";
    public static final String PREFS_UVC = "UVC";
    public static final String PREFS_LIVENESS_INFRAREDT1 = "LIVENESS_INFRAREDT1";
    public static final String PREFS_LIVENESS_INFRAREDT2 = "LIVENESS_INFRAREDT2";
    public static final String PREFS_LIVENESS_INFRAREDT_MASK1 = "LIVENESS_INFRAREDT_MASK1";
    public static final String PREFS_LIVENESS_INFRAREDT_MASK2 = "LIVENESS_INFRAREDT_MASK2";
    public static final String PREFS_SET_RGB_CAM_PARAMS = "SET_RGB_CAM_PARAMS";
    public static final String PREFS_SET_IR_CAM_PARAMS = "SET_IR_CAM_PARAMS";
    public static final String PREFS_SET_PREVIEW_MIRROR= "SET_PREVIEW_MIRROR";
    public static final String PREFS_SET_MASK_RECOGNIZE = "SET_MASK_RECOGNIZE";

    public static final String PREFS_USEAWRGBLIVEVTYPE = "USEAWRGBLIVEVTYPE";
    public static final String PREFS_USEAWRGBLIVEVERSION = "USEAWRGBLIVEVERSION";
    public static final String PREFS_DEBUG = "DEBUG";
    public static final String PREFS_LIVE = "LIVE";
    public static final String PREFS_FAKE = "FAKE";
    public static final String PREFS_SAVEINFRAREDLIVE = "SAVEINFRAREDLIVE";
    public static final String PREFS_SAVEINFRAREDFAKE = "SAVEINFRAREDFAKE";
    public static final String PREFS_SAVENOFACEDATA = "SAVENOFACEDATA";
    public static final String PREFS_SAVESSDATA = "SAVESSDATA";

    public static final String PREFS_AUTH_FACE_TYPE = "AUTH_FACE_TYPE";
    public static final String PREFS_AUTH_FACE_SERIAL_ID = "AUTH_FACE_SERIAL_ID";
}
