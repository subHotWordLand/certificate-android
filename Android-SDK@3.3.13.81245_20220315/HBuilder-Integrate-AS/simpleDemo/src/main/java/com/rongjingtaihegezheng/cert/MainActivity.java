package com.rongjingtaihegezheng.cert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.telecom.Connection;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.r01lib.WTR01;
import com.rongjingtaihegezheng.network.BaseNetApi;
import com.rongjingtaihegezheng.network.Forground;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

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
    private int btIntentReqCode = 23550;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisCon = this;
//        EnableBluetooth();
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
                PrinterHelper.PrintQR(PrinterHelper.BARCODE, "400", String.valueOf(qrcodeY), "4", "4", qrcode);
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
    }

}