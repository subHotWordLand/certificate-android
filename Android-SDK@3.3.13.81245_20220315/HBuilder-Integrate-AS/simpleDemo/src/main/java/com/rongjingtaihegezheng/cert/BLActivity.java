package com.rongjingtaihegezheng.cert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.r01lib.INotifyMessage;
import com.example.r01lib.WTR01;

public class BLActivity extends AppCompatActivity {
    private int c = 1;
    private WTR01 wtr01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bl);
        final Button btnRefreshEquipment = this.findViewById(R.id.btnRereshEquipmentList);
        final TextView tvDebug = this.findViewById(R.id.tvDebug);//调试信息
        final Button btnOpen = this.findViewById(R.id.btnOpen);//开仓门
        final Button btnClose = this.findViewById(R.id.btnClose);//关仓门
        final Button btnCheck = this.findViewById(R.id.btnCheck);//开始检测
        /*权限动态授予*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 10001);

            }
            if (this.checkSelfPermission(android.Manifest.permission.BLUETOOTH_PRIVILEGED) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_PRIVILEGED}, 10002);
            }
        }
        //实例化
        wtr01 = new WTR01(getApplication());
        //蓝牙底层调试信息
        wtr01.setDebugCallback(new INotifyMessage() {
            @Override
            public void notifyMessage(final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDebug.setText(String.valueOf(c++) + ":" + s + "\r\n" + tvDebug.getText().toString());
                    }
                });
            }
        });
        //开仓门
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wtr01.currentBleEquipment() == null) {
                    Toast.makeText(BLActivity.this, "没选中仪器", Toast.LENGTH_SHORT).show();
                } else {
                    wtr01.currentBleEquipment().open();
                }
            }
        });
        //关仓门
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wtr01.currentBleEquipment() == null) {
                    Toast.makeText(BLActivity.this, "没选中仪器", Toast.LENGTH_SHORT).show();
                } else {
                    wtr01.currentBleEquipment().close();
                }
            }
        });
        //开始检测
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wtr01.currentBleEquipment() == null) {
                    Toast.makeText(BLActivity.this, "没选中仪器", Toast.LENGTH_SHORT).show();
                } else {
                    wtr01.currentBleEquipment().check(false);
                }
            }
        });
        //刷新仪器列表
        btnRefreshEquipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wtr01.refreshList();
                Toast.makeText(BLActivity.this, "刷新仪器", Toast.LENGTH_SHORT).show();
            }
        });
        /*
        ListView listView = (ListView) findViewById(R.id.lvEquipments);
        //从仪器列表选择仪器连接
        final EquipmentAdapter adpt = new EquipmentAdapter(wtr01.getEquipList(), this, new ISelectItemCallback() {
            @Override
            public void select(IGetRecordInfo item) {
                //连接设备
                if (wtr01.connectEquipment(MainActivity.this, item.getKey(), item.getValue())) {
                    Toast.makeText(BLActivity.this, "选中仪器" + item.getValue(), Toast.LENGTH_SHORT).show();
                    if (wtr01.currentBleEquipment() != null) {
                        wtr01.currentBleEquipment().setNotifyCallBack(new INotifyMessage() {
                            @Override
                            public void notifyMessage(final String s) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvDebug.setText((++c) + ":" + s + "\r\n" + tvDebug.getText().toString());
                                    }
                                });
                            }
                        });
                        //读取到了检测结果、ad值
                        wtr01.currentBleEquipment().setAdCallBack(new INotifyPesticideResult() {
                            @Override
                            public void notifyResult(int hole, double result) {
                                //读取到了检测结果
                                //hole:通道
                                //result:检测结果
                                tvDebug.setText(String.valueOf(c++) + ":" + "通道:" + hole + ",检测结果:" + result + "%" + "\r\n" + tvDebug.getText().toString());
                            }

                            @Override
                            public void notifyAdValue(final int hole, final int ad, final boolean firstScan) {
                                //读取到了ad值
                                //hole:通道
                                //ad:信号值
                                //firstScan:是否第一次扫描(正常检测扫描两次)
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvDebug.setText(String.valueOf(c++) + ":" + "第" + (firstScan ? "一" : "二") + "次扫描,通道:" + hole + ",ad值:" + ad + "\r\n" + tvDebug.getText().toString());
                                    }
                                });
                            }
                        });
                    }
                } else {
                    Toast.makeText(BLActivity.this, "仪器" + item.getValue() + "连接失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listView.setAdapter(adpt);
        //扫描到了新设备
        wtr01.setDataSetChangedCallback(new INotifyDataSetChanged() {
            @Override
            public void notifyDataSetChanged(String mac, String serialNo, boolean b) {
                //mac:物理地址
                //serialNo:序列号
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adpt.notifyDataSetChanged();
                    }
                });

            }
        });
        */

    }

    @Override
    protected void onDestroy() {
        if (wtr01 != null) {
            wtr01.release();//释放蓝牙通信资源
        }
        super.onDestroy();
    }
}