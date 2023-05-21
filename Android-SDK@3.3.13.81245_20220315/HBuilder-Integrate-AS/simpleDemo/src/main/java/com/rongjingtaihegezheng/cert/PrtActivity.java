package com.rongjingtaihegezheng.cert;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.concurrent.ExecutorService;

import cpcl.PrinterHelper;
import rx.functions.Action1;

public class PrtActivity extends Activity {
    private Context thisCon = null;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler handler;
    private PendingIntent mPermissionIntent = null;
    private ProgressDialog dialog;
    public static String paper = "0";
    private String ConnectType = "";
    private ExecutorService executorService;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static String[] wifi_PERMISSIONS = {
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.ACCESS_WIFI_STATE"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prt);
        thisCon = this;

        final Button btnDayin = this.findViewById(R.id.dayin);//打印
        final Button btnLanya = this.findViewById(R.id.bt);//蓝牙

        btnDayin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!PrinterHelper.IsOpened()) {
                    Toast.makeText(thisCon, "请连接打印机！", Toast.LENGTH_SHORT).show();
                    return;
                }
                setTestPage();
            }
        });
        btnLanya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //获取蓝牙动态权限
                    RxPermissions rxPermissions = new RxPermissions((Activity) thisCon);
                    rxPermissions.request(android.Manifest.permission.BLUETOOTH_ADMIN,
                            android.Manifest.permission.BLUETOOTH,
                            android.Manifest.permission.ACCESS_FINE_LOCATION).subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            if (aBoolean) {
                                ConnectType = "Bluetooth";
                                Intent intent = new Intent(thisCon, BTActivity.class);
                                intent.putExtra("TAG", 0);
                                startActivityForResult(intent, 0);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("rongjingtai", (new StringBuilder("Activity_Main --> onClickConnect " + ConnectType)).append(e.getMessage()).toString());
                }

            }
        });



        EnableBluetooth();
    }

    //call back by scan bluetooth printer
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        try {
            String strIsConnected;
            if (data == null)
                return;
            switch (resultCode) {
                case RESULT_CANCELED:
                    connectBT(data.getStringExtra("SelectedBDAddress"));
                    break;
            }
        } catch (Exception e) {
            Log.e("SDKSample", (new StringBuilder("Activity_Main --> onActivityResult ")).append(e.getMessage()).toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectBT(final String selectedBDAddress) {
        if (TextUtils.isEmpty(selectedBDAddress))
            return;
        final ProgressDialog progressDialog = new ProgressDialog(PrtActivity.this);
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
            if (mBluetoothAdapter.isEnabled())
                return true;
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

    private void setTestPage() {
        try {
            PrinterHelper.printAreaSize("0", "200", "200", "460", "1");
//			PrinterHelper.Box("50", "5", "450", "400", "1");
            PrinterHelper.Align(PrinterHelper.CENTER);
            PrinterHelper.Text(PrinterHelper.TEXT, "8", "0", "50", "15", "鲁青霞");
            PrinterHelper.Align(PrinterHelper.LEFT);
            PrinterHelper.Text(PrinterHelper.TEXT, "8", "0", "30", "66", "内容");
//            if ("1".equals(Activity_Main.paper)) {
//                PrinterHelper.Form();
//            }
            PrinterHelper.Print();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("rongjingtai", (new StringBuilder("Activity_Main --> onClickWIFI ")).append(e.getMessage()).toString());
        }
    }
}