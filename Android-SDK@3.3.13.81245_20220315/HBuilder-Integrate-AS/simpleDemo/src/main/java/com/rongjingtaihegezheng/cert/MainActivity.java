package com.rongjingtaihegezheng.cert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.telecom.Connection;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.r01lib.WTR01;
import com.ivsign.android.IDCReader.IDCReaderSDK;
import com.mtreader.MTReaderEngine;
import com.rongjingtaihegezheng.network.BaseNetApi;
import com.rongjingtaihegezheng.network.Forground;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import cpcl.PrinterHelper;
import io.dcloud.PandoraEntryActivity;
import io.dcloud.common.adapter.util.Logger;
import rx.functions.Action1;


public class MainActivity extends CheckPermissionsActivity {
    private Context thisCon = null;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler handler;
    private PendingIntent mPermissionIntent = null;
    private ProgressDialog dialog;
    public static String paper = "0";
    private String ConnectType = "";
    private ExecutorService executorService;
    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static String[] wifi_PERMISSIONS = {"android.permission.CHANGE_WIFI_STATE", "android.permission.ACCESS_WIFI_STATE"};
    private JSONObject prtInfo;
    private Callback printCallback;
    private Callback idcardCallback;
    private int btIntentReqCode = 23550;
    private Boolean isConnectPrint = false;
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisCon = this;
        mContext = this;
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
//        getSystemMsg();
        //idcard end
//        EnableBluetooth();
        // 开启子线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(6000); // 等待
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 切换到主线程执行任务
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 在这里执行需要在主线程中处理的任务
                        Intent intent = new Intent(thisCon, DetectActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }).start();
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
                        ConnectType = "Bluetooth";
                        Intent intent = new Intent(thisCon, BTActivity.class);
                        intent.putExtra("TAG", 0);
                        startActivityForResult(intent, btIntentReqCode);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("rongjingtai", (new StringBuilder("Activity_Main --> onClickConnect " + ConnectType)).append(e.getMessage()).toString());
        }
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
        }
        //测试如果是js通知 这句加上会让onActivityResult执行两次导致打印机连接异常 查询了一下貌似是fragment引起的 https://www.jianshu.com/p/cbef02d0765d
        super.onActivityResult(requestCode, resultCode, data);
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
                String face = prtInfo.getString("face");
                int facei = Integer.parseInt(face);
                int top = 40;//反面
                if (facei == 0) {
                    //正面
                    top = 0;
                }
                int marginv = 0;
                int lineh = 30;
                int left = 25;
                int marginl = 150;
                int font = 8;
                String qrcode = prtInfo.getString("qrcode");
                JSONArray list = prtInfo.getJSONArray("list");
                String printCount = prtInfo.getString("printCount");
                int _temptop = top;
                for (int i = 0; i < list.length(); i++) {
                    _temptop += lineh;
                    _temptop += marginv;
                }
                //PoPrint旋转180度打印的话 相当于print打印是区域Y的最大值作为poprint打印的开始值(向上扩张最后的定位作为开始值也就是y为0)
                if (facei == 0) _temptop += 30;
                PrinterHelper.printAreaSize("0", "200", "200", String.valueOf(_temptop), printCount);
                for (int i = 0; i < list.length(); i++) {
                    JSONObject jsonObject = list.getJSONObject(i);
                    String name = jsonObject.getString("name");
                    String value = jsonObject.getString("value");
                    PrinterHelper.Align(PrinterHelper.LEFT);
                    PrinterHelper.Text(PrinterHelper.TEXT, String.valueOf(font), "0", String.valueOf(left), String.valueOf(top), name + ":");
                    //
                    PrinterHelper.Align(PrinterHelper.LEFT);
                    PrinterHelper.Text(PrinterHelper.TEXT, String.valueOf(font), "0", String.valueOf(left + marginl), String.valueOf(top), value);
                    top += lineh;
                    top += marginv;
                }
                int qrcodeY = 65;
                if (facei == 0) qrcodeY = 0;
                PrinterHelper.PrintQR(PrinterHelper.BARCODE, "400", String.valueOf(qrcodeY), "4", "3", qrcode);
                PrinterHelper.Form();
                if (facei == 1) {
                    PrinterHelper.Print();
                } else {
                    PrinterHelper.PoPrint();
                }
                //通知uniapp
                HashMap<String, Object> info = new HashMap<String, Object>();
                info.put("method", "print");
                JSONObject r = new JSONObject(info);
                String rstr = r.toString();
                if (printCallback != null) printCallback.result(1, rstr);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e("rongjingtai", (new StringBuilder("Activity_Main --> printResult ")).append(e.getMessage()).toString());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUsbDevPermission.unRegisterReceiver();
    }

}