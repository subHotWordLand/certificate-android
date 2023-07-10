package com.rongjingtaihegezheng.cert;

import static com.msprintsdk.PrintCmd.PrintDiskImagefile;
import static com.msprintsdk.UtilsTools.convertToBlackWhite;
import static com.msprintsdk.UtilsTools.getFromRaw;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.aiwinn.base.util.PermissionUtils;
import com.aiwinn.base.util.ToastUtils;
import com.aiwinn.facedetectsdk.FaceDetectManager;
import com.aiwinn.facedetectsdk.common.Constants;
import com.msprintsdk.PrintCmd;
import com.msprintsdk.UsbDriver;
import com.mtreader.MTReaderEngine;
import com.rongjingtaihegezheng.cert.common.AttConstants;
import com.rongjingtaihegezheng.cert.common.AttParams;
import com.rongjingtaihegezheng.cert.utils.TTSUtils;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import cpcl.PrinterHelper;
import rx.functions.Action1;


public class MainActivity extends CheckPermissionsActivity implements PermissionUtils.FullCallback {
    private Context thisCon = null;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler handler;
    private ProgressDialog dialog;
    public static String paper = "0";
    private ExecutorService executorService;
    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static String[] wifi_PERMISSIONS = {"android.permission.CHANGE_WIFI_STATE", "android.permission.ACCESS_WIFI_STATE"};
    private JSONObject prtInfo;
    private Callback printCallback;
    private Callback idcardCallback;
    private Callback faceCallback;
    private int btIntentReqCode = 23550;
    private Boolean isConnectPrint = false;
    private String printConnectType = "";
    static UsbDriver ptmUsbDriver;
    private UsbDevice ptmUsbDevice = null;
    private PendingIntent ptmPermissionIntent = null;
    private static final String ACTION_USB_PERMISSION = "com.HPRTSDKSample";

    // 身份证start
    //身份证识别实例
    private MTReaderEngine SDTAPI = null;
    private String mDefaultSerialPath = "/dev/ttyS3";
    private SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private UsbDevPermission mUsbDevPermission;
    private Set<String> chmodCmdString = new HashSet<String>();

    private boolean isReadingCard = false;
    private int m_device_fd = 0;
    private Context mContext;
    private long startTime;
    //身份证end

    //人脸识别 start
    private int faceIntentReqCode = 23560;
    private boolean mFaceIsGranted;
    private String[] facePermissions = new String[]{
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_PHONE_STATE,

            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    //人脸识别 end
    private String TAG = "rongjingtai";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisCon = this;
        mContext = this;
        //打印机 start
        ptmUsbDriver = new UsbDriver((UsbManager) getSystemService(Context.USB_SERVICE), this);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        ptmUsbDriver.setPermissionIntent(permissionIntent);
        //
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        this.registerReceiver(ptmUsbReceiver, filter);
        //打印机 end

        //idcard start
        SDTAPI = MTReaderEngine.getInstance();
        mUsbDevPermission = new UsbDevPermission(thisCon);
        initCmdList();
        List<String> serialLists = new ArrayList<String>();
        String[] serials = null;
        try {
            serials = mSerialPortFinder.getAllDevicesPath();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        if (serials != null) {
            for (int i = 0; i < serials.length; i++) {
                String chmodSerial = "chmod 777 " + serials[i] + ";";
                serialLists.add(serials[i]);
                chmodCmdString.add(chmodSerial);
            }
        } else {
            serials = new String[1];
            serials[0] = mDefaultSerialPath;
            serialLists.add(mDefaultSerialPath);
        }

        RootCmd.chmodShell(chmodCmdString);
        FileUtils.getInstance(mContext).copyAssetsToSD("wltlib", "wltlib");
        //人脸识别start
        mFaceIsGranted = true;
        for (int i = 0; i < facePermissions.length; i++) {
            if (!PermissionUtils.isGranted(facePermissions[i])) {
                mFaceIsGranted = false;
                break;
            }
        }
        //人脸识别end
//        getSystemMsg();
        //idcard end
//        EnableBluetooth();
        // 开启子线程
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(12000); // 等待
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                // 切换到主线程执行任务
//                Handler handler = new Handler(Looper.getMainLooper());
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        // 在这里执行需要在主线程中处理的任务
//                        Intent intent = new Intent(thisCon, DetectActivity.class);
//                        startActivity(intent);
//                    }
//                });
//            }
//        }).start();

    }

    /**
     * 获取usb权限
     */
    public int usbDriverCheck() {
        int iResult = -1;
        try {

            if (!ptmUsbDriver.isUsbPermission()) {
                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                ptmUsbDevice = null;
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
                            || (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
                        ptmUsbDevice = device;
                        showMessage("DeviceClass:" + device.getDeviceClass() + ";DeviceName:" + device.getDeviceName(), 1);
                    }
                }
                if (ptmUsbDevice != null) {
                    iResult = 1;
                    if (ptmUsbDriver.usbAttached(ptmUsbDevice)) {
                        if (ptmUsbDriver.openUsbDevice(ptmUsbDevice))
                            iResult = 0;
                    }
                }
            } else {
                if (!ptmUsbDriver.isConnected()) {
                    if (ptmUsbDriver.openUsbDevice(ptmUsbDevice))
                        iResult = 0;
                } else {
                    iResult = 0;
                }
            }
        } catch (Exception e) {

            Log.e(TAG, "usbDriverCheck:" + e.getMessage());
        }

        return iResult;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFaceIsGranted) {
            if (!AttConstants.INIT_STATE) {
                com.aiwinn.base.util.FileUtils.createOrExistsDir(Constants.PATH_LIVE_SAVE);
                com.aiwinn.base.util.FileUtils.createOrExistsDir(AttConstants.PATH_AIWINN);
                com.aiwinn.base.util.FileUtils.createOrExistsDir(AttConstants.PATH_ATTENDANCE);
                com.aiwinn.base.util.FileUtils.createOrExistsDir(AttConstants.PATH_BULK_REGISTRATION);
                com.aiwinn.base.util.FileUtils.createOrExistsDir(AttConstants.PATH_CARD);
                int cameraCount = Camera.getNumberOfCameras();
                if (cameraCount == 0) {
                    ToastUtils.showShort("设备没有摄像头");
                }

                AttParams.initSpParams();

                TTSUtils.initSpeech(mContext);

                /**
                 * 授权方式分为"定制渠道号授权"和"序列号授权"两种方式，请联系FAE或者商务了解你们的授权方式
                 * 1.定制渠道号授权参考"授权-渠道号授权"
                 * 2.序列号授权参考“授权-序列号单个授权”、“授权-序列号批量授权”
                 */
                //获取是否为定制的渠道号授权
                int faceAuthType = AttApp.sp.getInt(AttConstants.PREFS_AUTH_FACE_TYPE, 0);
                String faceAContent = AttApp.sp.getString(AttConstants.PREFS_AUTH_FACE_SERIAL_ID, "");
                if (faceAuthType == 0) {
                    //定制渠道号授权，此处不需要额外处理
                } else if (faceAuthType == 1) {
                    //设置序列号到SDK
                    //请输入购买的单个授权序列号
                    FaceDetectManager.setSerialId(faceAContent);
                } else if (faceAuthType == 2) {
                    //设置序列号到SDK
                    //请输入购买的批量授权序列号
                    FaceDetectManager.setSerialGroup(faceAContent);
                }

                if (!FaceDetectManager.isLicenseVersion()) {//SDK版本分测试有效期和网络授权版本,授权版本客户按业务处理授权时机
                    //测试有效期版本,直接初始化
                    AttApp.initSDK();
                } else {
                    //网络授权版本
                    //1、第一次授权, 业务需要直接初始化并完成授权可不用做判断直接初始化
                    //AttApp.initSDK();

                    //2、第一次授权, 业务需要例如有授权按钮需求的,可以将初始化放到点击授权处处理, demo使用这种处理方式
                    if (FaceDetectManager.isLicensed(getApplicationContext())) {
                        //已授权,直接初始化
                        AttApp.initSDK();
                    }
                }
                AttConstants.Detect_Exception = false;
            }
            Log.d("onResume", "MainActivity -> onResume > Detect_Exception " + AttConstants.Detect_Exception + " INIT_STATE " + AttConstants.INIT_STATE);
//            if(AttConstants.Detect_Exception && checkInitState()) {
//                mIntent.setClass(MainActivity.this, DetectActivity.class);
//                startActivity(mIntent);
//            }
        } else {
            PermissionUtils.permission(facePermissions).callback(this).request();
        }
    }

    boolean checkInitState() {
        if (FaceDetectManager.isLicenseVersion() && !FaceDetectManager.isLicensed(mContext)) {
            ToastUtils.showLong(getResources().getString(R.string.authorization_not_had));
            return false;
        }
        if (!AttConstants.INIT_STATE) {
            ToastUtils.showLong(getResources().getString(R.string.init_fail) + " : " + AttConstants.INIT_STATE_ERROR);
            return false;
        } else {
            return true;
        }
    }

    private void getSystemMsg() {
        try {
            showMessage("手机厂商:" + SystemUtil.getDeviceBrand(), 1);
            showMessage("手机型号:" + SystemUtil.getSystemModel(), 1);
            showMessage("系统语言:" + SystemUtil.getSystemLanguage(), 1);
            showMessage("系统版本号:" + SystemUtil.getSystemVersion(), 1);
            showMessage("SELinuxMode:" + RootCmd.getSELinuxMode(), 1);
        } catch (Exception ex) {
            ex.getLocalizedMessage();
        }
    }

    public void connectdevIdcard() {
        //USB
        m_device_fd = mUsbDevPermission.getUsbFileDescriptor(0x0400, 0xC35A);
        if (m_device_fd <= 0) {
            m_device_fd = mUsbDevPermission.getUsbFileDescriptor(0x0483, 0x5652);
        }
        if (m_device_fd <= 0) {
            showMessage("获取设备描述符失败,fd=" + m_device_fd, 1);
            return;
        }
        //Selinux处于强制模式下:Enforcing
        //Selinux处于宽容模式下:Permissive
        int ret = 0;

        ret = SDTAPI.OpenReader(m_device_fd);

        if (ret == 0x90) {
            showMessage("打开设备成功 开始进行读卡", 1);
            startReadCard();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    /**
                     * 延时执行的代码
                     */
                    connectdevIdcard();
                }
            }, 1000); // 延时1秒
            showMessage("打开设备失败,ret=" + ret + "进行重试", 1);
        }
    }

    public void startReadCard() {
        if (!isReadingCard) {
            isReadingCard = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    while (isReadingCard) {
                        byte pucCHMsg[] = new byte[256];
                        byte pucPHMsg[] = new byte[1024];
                        byte pucFPMsg[] = new byte[1024];

                        int puiCHMsgLen[] = new int[1];
                        int puiPHMsgLen[] = new int[1];
                        int puiFPMsgLen[] = new int[1];

                        int ret = SDTAPI.ReadIDCardAll(pucCHMsg, puiCHMsgLen, pucPHMsg, puiPHMsgLen, pucFPMsg, puiFPMsgLen);
                        if (ret == 0x90) {
                            try {
                                showIDCardInfo(pucCHMsg, pucPHMsg, pucFPMsg, puiFPMsgLen);
                            } catch (UnsupportedEncodingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            isReadingCard = false;
                            break;
                        }
                    }
                }
            }).start();
        }
    }

    public void closeIdcardPort() {
        startTime = System.currentTimeMillis();
        int ret = SDTAPI.CloseReader();
        if (ret == 0x90) {
            showMessage("关闭设备成功", 1);
            mUsbDevPermission.unRegisterReceiver();
        } else {
            showMessage("关闭设备失败,ret=" + ret, 1);
        }
        showMessage("用时:" + (System.currentTimeMillis() - startTime) + "ms", 1);
    }

    private void showIDCardInfo(byte[] pucCHMsg, byte[] pucPHMsg,
                                byte[] pucFPMsg, int[] puiFPMsgLen)
            throws UnsupportedEncodingException {
        int ret;
        String StrBmpFilePath;
        HashMap<Integer, String> resultData = new HashMap<>();
        resultData = AnysizeIDCMsg.AnysizeData(pucCHMsg);
        if (resultData.size() == 0) {
            showMessage("卡片数据解析出错！", 0);
            return;
        }
        Map<Object, Object> tresultData = resultData.entrySet().stream().collect(Collectors.toMap(
                entry -> String.valueOf(entry.getKey()),
                entry -> entry.getValue()
        ));
        JSONObject r = new JSONObject(tresultData);
        String rstr = r.toString();
        if (idcardCallback != null) idcardCallback.result(1, rstr);
        /*
        if (resultData.get(AnysizeIDCMsg.CARDTYPE_INDEX).equals("GAT")) {
            showMessage(resultData.get(AnysizeIDCMsg.NAME_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.SEX_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.BIRTH_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.ADDR_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.IDNUM_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.IDTYPE_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.TXZHM_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.QFCS_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.DEPART_INDEX), 1);

            showMessage(resultData.get(AnysizeIDCMsg.STATIME_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.ENDTIME_INDEX), 1);

        }
        else if (resultData.get(AnysizeIDCMsg.CARDTYPE_INDEX).equals("FGR")) {

            showMessage(resultData.get(AnysizeIDCMsg.FGRNAME_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.NAME_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.SEX_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.BIRTH_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.COUNTRY_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.IDNUM_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.DEPART_INDEX), 1);

            showMessage(resultData.get(AnysizeIDCMsg.VERSION_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.IDTYPE_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.SLJGCODE_INDEX), 1);

            showMessage(resultData.get(AnysizeIDCMsg.STATIME_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.ENDTIME_INDEX), 1);

        }
        else {
            showMessage(resultData.get(AnysizeIDCMsg.NAME_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.SEX_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.NATION_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.BIRTH_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.ADDR_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.IDNUM_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.DEPART_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.STATIME_INDEX), 1);
            showMessage(resultData.get(AnysizeIDCMsg.ENDTIME_INDEX), 1);
        }
        showMessage("指纹信息:\r\n" + bytesToHexString(pucFPMsg, puiFPMsgLen[0]), 1);

        if (IDCReaderSDK.GetLoadSoState()) {
            ret = IDCReaderSDK.Init();
            if (ret == 0) {
                int t = IDCReaderSDK.unpack(pucPHMsg, AnysizeIDCMsg.byLicData);
                if (t == 1) {
                    StrBmpFilePath = Environment.getExternalStorageDirectory() + "/wltlib/zp.bmp";
                    FileInputStream fis;
                    try {
                        fis = new FileInputStream(StrBmpFilePath);
                        fis.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    showMessage(StrBmpFilePath, 2); // 显示照片
                } else {
                    showMessage("身份证照片解码库解码失败,ret = " + t, 0);
                }
            } else {
                showMessage("身份证照片解码库初始化失败,请检查< " + Environment.getExternalStorageDirectory() + "/wltlib/ > 目录下是否有照片解码库授权文件!", 0);
            }
        } else {
            showMessage("未找到身份证照片解码库libwltdecode.so!", 0);
        }
         */
    }

    public static final String bytesToHexString(byte[] bArray, int bArrayLen) {
        StringBuffer sb = new StringBuffer(bArrayLen);
        String sTemp;
        for (int i = 0; i < bArrayLen; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /*
     * str:要显示的信息
     * Flag：1时为文本显示，2时为图像显示
     */
    protected void showMessage(String str, int Flag) {
        Message msg = new Message();
        msg.obj = str;
        msg.what = Flag;
        mhandler.sendMessage(msg);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String str = (String) msg.obj;
            Log.d("idcard handleMessage", str);
            Toast.makeText(thisCon, str, Toast.LENGTH_SHORT).show();
        }
    };

    private void initCmdList() {
        chmodCmdString.add("chmod 777 /dev/;");
        chmodCmdString.add("chmod 777 /dev/bus/;");
        chmodCmdString.add("chmod 777 /dev/bus/usb/;");
        chmodCmdString.add("chmod 777 /dev/bus/usb/0*;");
        chmodCmdString.add("chmod 777 /dev/bus/usb/001/*;");
        chmodCmdString.add("chmod 777 /dev/bus/usb/002/*;");
        chmodCmdString.add("chmod 777 /dev/bus/usb/003/*;");
        chmodCmdString.add("chmod 777 /dev/bus/usb/004/*;");
        chmodCmdString.add("chmod 777 /dev/bus/usb/005/*;");
    }

    public void naviBtSelected() {
        try {
            //获取蓝牙动态权限
            RxPermissions rxPermissions = new RxPermissions((Activity) thisCon);
            rxPermissions.request(android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.BLUETOOTH, android.Manifest.permission.ACCESS_FINE_LOCATION).subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    if (aBoolean) {
                        printConnectType = "Bluetooth";
                        Intent intent = new Intent(thisCon, BTActivity.class);
                        intent.putExtra("TAG", 0);
                        startActivityForResult(intent, btIntentReqCode);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("rongjingtai", (new StringBuilder("Activity_Main --> onClickConnect " + printConnectType)).append(e.getMessage()).toString());
        }
    }

    /**
     * 启动人脸识别for js call
     */
    public void startFaceRecFromJs(String data, Callback callback) throws JSONException {
        JSONObject result = new JSONObject(data);
        String method = result.getString("method");
        JSONObject dataObj = result.getJSONObject("data");
        prtInfo = dataObj;
        Log.e("rongjingtai", "dataObj is " + dataObj);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(thisCon, DetectActivity.class);
                            startActivityForResult(intent, faceIntentReqCode);
                        }
                    });
                } catch (Exception e) {
                }
            }
        }.start();
        this.faceCallback = callback;
    }

    /**
     * 启动打印for js call
     */
    public void startPrintFromJs(String data, Callback callback) throws JSONException {
        JSONObject result = new JSONObject(data);
        String method = result.getString("method");
        JSONObject dataObj = result.getJSONObject("data");
        prtInfo = dataObj;
        Log.e("rongjingtai", "dataObj is " + dataObj);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //如果已经连接了的情况下直接打印
                            if (isConnectPrint) {
                                printResult();
                                return;
                            }
                            //在这里写你要跳转的界面
                            naviBtSelected();
                        }
                    });
                } catch (Exception e) {
                }
            }
        }.start();
        this.printCallback = callback;
    }

    /**
     * 关闭打印for js call
     */
    public void closePrintPort(String data) throws JSONException {
        JSONObject result = new JSONObject(data);
        String method = result.getString("method");
        JSONObject dataObj = result.getJSONObject("data");
        Log.e("rongjingtai", "dataObj is " + dataObj);
        if (!checkClick.isClickEvent()) return;
        try {
            PrinterHelper.portClose();
            isConnectPrint = false;
            return;
        } catch (Exception e) {
            Log.e("rongjingtai", (new StringBuilder("Activity_Main --> closePrintPort ")).append(e.getMessage()).toString());
        }
    }

    /**
     * 启动usb打印for js call
     */
    public void startUsbPrintFromJs(String data, Callback callback) throws JSONException {
        JSONObject result = new JSONObject(data);
        String method = result.getString("method");
        JSONObject dataObj = result.getJSONObject("data");
        prtInfo = dataObj;
        Log.e("rongjingtai", "dataObj is " + dataObj);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int iDriverCheck = usbDriverCheck();
                            if (iDriverCheck == -1) {
                                showMessage("打印机没有连接!", 1);
                                return;
                            }

                            if (iDriverCheck == 1) {
                                showMessage("打印机没有授权认证!", 1);
                                return;
                            }
                            printResult();
                        }
                    });
                } catch (Exception e) {
                    showMessage(e.getMessage(), 1);
                }
            }
        }.start();
        this.printCallback = callback;
    }

    /*
     *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
     *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
     */
    BroadcastReceiver ptmUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
                        || (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
                    ptmUsbDriver.closeUsbDevice(device);
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) synchronized (this) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
                            || (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
                        //赋权限以后的操作
                    }
                } else {
//                    Toast.makeText(MainActivity.this, "permission denied for device",
                    showMessage("permission denied for device", 1);
                }
            }

        }
    };

    /**
     * 启动身份证识别for js call
     */
    public void startIdcardCheckFromJs(String data, Callback callback) throws JSONException {
        JSONObject result = new JSONObject(data);
        String method = result.getString("method");
        JSONObject dataObj = result.getJSONObject("data");
        Log.e("rongjingtai", "dataObj is " + dataObj);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectdevIdcard();
                        }
                    });
                } catch (Exception e) {
                }
            }
        }.start();
        if (callback != null) this.idcardCallback = callback;
    }

    /**
     * 身份证放置监听for js call
     */
    public void cardListeningFromJs(String data) throws JSONException {
        JSONObject result = new JSONObject(data);
        String method = result.getString("method");
        JSONObject dataObj = result.getJSONObject("data");
        Log.e("rongjingtai", "dataObj is " + dataObj);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startReadCard();
                        }
                    });
                } catch (Exception e) {
                }
            }
        }.start();
    }

    /**
     * 身份证放置停止监听for js call
     */
    public void cardListeningStopFromJs(String data) throws JSONException {
        JSONObject result = new JSONObject(data);
        String method = result.getString("method");
        JSONObject dataObj = result.getJSONObject("data");
        Log.e("rongjingtai", "dataObj is " + dataObj);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isReadingCard = false;
                        }
                    });
                } catch (Exception e) {
                }
            }
        }.start();
    }

    /**
     * 关闭身份证识别读卡
     */
    public void closeIdcardCheckFromJs(String data, Callback callback) throws JSONException {
        JSONObject result = new JSONObject(data);
        String method = result.getString("method");
        JSONObject dataObj = result.getJSONObject("data");
        Log.e("rongjingtai", "dataObj is " + dataObj);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeIdcardPort();
                        }
                    });
                } catch (Exception e) {
                }
            }
        }.start();
        if (callback != null) this.idcardCallback = callback;
    }

    //call back by scan bluetooth printer
//    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == btIntentReqCode) {
            try {
                String strIsConnected;
                if (data == null) return;
                switch (resultCode) {
                    case RESULT_CANCELED:
                        connectBT(data.getStringExtra("SelectedBDAddress"));
                        break;
                }
            } catch (Exception e) {
                Log.e("rongjingtai", (new StringBuilder("Activity_Main --> onActivityResult ")).append(e.getMessage()).toString());
            }
            return;
        } else if (requestCode == faceIntentReqCode) {
            if (data == null) return;
            if (faceCallback != null) {
                String imgBase64 = null;
                byte[] idata = data.getByteArrayExtra("imageData");
                if (idata != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(idata, 0, idata.length);
                    imgBase64 = bitmapToBase64(bitmap);
                    //通知uniapp
                    HashMap<String, Object> info = new HashMap<String, Object>();
                    info.put("imgBase64", imgBase64);
                    JSONObject r = new JSONObject(info);
                    String rstr = r.toString();
                    faceCallback.result(1, rstr);
                }
            }
            return;
        }
        //测试如果是js通知 这句加上会让onActivityResult执行两次导致打印机连接异常 查询了一下貌似是fragment引起的 https://www.jianshu.com/p/cbef02d0765d
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void connectBT(final String selectedBDAddress) {
        if (TextUtils.isEmpty(selectedBDAddress)) return;
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("连接");
        progressDialog.show();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    final int result = PrinterHelper.portOpenBT(getApplicationContext(), selectedBDAddress);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result == 0) {
                                Log.d("rongjingtai", "连接成功！");
                                Toast.makeText(thisCon, "连接成功！", Toast.LENGTH_SHORT).show();
                                isConnectPrint = true;
                                printResult();
                            } else {
                                Log.d("rongjingtai", "连接失败" + result);
                                Toast.makeText(thisCon, "连接失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    progressDialog.dismiss();
                } catch (Exception e) {
                    progressDialog.dismiss();
                }
            }
        }.start();
    }

    @SuppressLint("MissingPermission")
    private boolean EnableBluetooth() {
        boolean bRet = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) return true;
            mBluetoothAdapter.enable();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!mBluetoothAdapter.isEnabled()) {
                bRet = true;
                Log.d("PRTLIB", "BTO_EnableBluetooth --> Open OK");
            }
        } else {
            Log.d("rongjingtai", (new StringBuilder("Activity_Main --> EnableBluetooth ").append("Bluetooth Adapter is null.")).toString());
        }
        return bRet;
    }

    private void printResult() {
        if (prtInfo != null) {
            try {
                ptmUsbDriver.write(PrintCmd.tuizhi(70));
                Bitmap bitmap = null;
                try {
                    int width, heigh;
                    InputStream txt = getResources().openRawResource(R.raw.txt);
                    String base64Data = getFromRaw(txt);
                    byte[] bytes = Base64.decode(base64Data.split(",")[1], Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    bitmap = convertToBlackWhite(bitmap);

                    width = bitmap.getWidth();
                    heigh = bitmap.getHeight();
                    int iDataLen = width * heigh;
                    int[] pixels = new int[iDataLen];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, heigh);
                    int[] data1 = pixels;
                    ptmUsbDriver.write(PrintCmd.SetLeftmargin(34));
                    ptmUsbDriver.write(PrintDiskImagefile(data1, width,269));
                    ptmUsbDriver.write(PrintCmd.SetRotate(1));
                    ptmUsbDriver.write(PrintCmd.SetDirection(1));
//                    ptmUsbDriver.write(PrintCmd.PrintFeedline(6));
                    ptmUsbDriver.write(PrintCmd.PrintMarkpositioncut());
                    ptmUsbDriver.write(PrintCmd.PrintCutpaper(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e("rongjingtai", (new StringBuilder("Activity_Main --> printResult ")).append(e.getMessage()).toString());
            }
        }
    }

    //人脸权限通知
    @Override
    public void onGranted(List<String> list) {
        mFaceIsGranted = true;
    }

    @Override
    public void onDenied(List<String> list, List<String> list1) {
        mFaceIsGranted = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //人脸usb监听
        mUsbDevPermission.unRegisterReceiver();
        //打印机usb监听
        try {
            PrinterHelper.portClose();
            if (ptmUsbReceiver != null) {
                unregisterReceiver(ptmUsbReceiver);
            }
//            if (ptmReceiver != null) {
//                unregisterReceiver(mReceiver);
//            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}