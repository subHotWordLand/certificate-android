package com.rongjingtaihegezheng.cert;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;

public class UsbDevPermission {
	
	private static String TAG = "UsbDevPermission";
	private static UsbManager mUsbManager;
	private static UsbDevice mUsbDevice;
	//private static UsbInterface mInterface;
	private static UsbDeviceConnection mDeviceConnection;
	private static PendingIntent mPermissionIntent;
	private static Context mContext;
	
	public static final int ERR_NOT_FOUND_USBDEVICE =   -90;
	public static final int ERR_USB_NOPERMISSION	= 	-91;
	public static final int ERR_USB_CANNOT_CONNTECT = 	-92;
	
	private static String usbDevPath;
    //注1：UsbManager.ACTION_USB_DEVICE_ATTACHED对应的广播在USB每次插入时都能监听到，所以用这个就可以监听USB插入。
    //注2：UsbManager.ACTION_USB_DEVICE_DETACHED用来监听USB拔出广播。
	
	public UsbDevPermission(Context context) {
		mContext = context;
		mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent("com.hdos.usbdevice.UsbDeviceLib.USB_PERMISSION"), 0);
		//注册USB设备权限管理广播
		IntentFilter filter = new IntentFilter("com.hdos.usbdevice.UsbDeviceLib.USB_PERMISSION");
		filter.addAction("com.android.example.USB_PERMISSION");
		filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
		filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
		mContext.registerReceiver(mUsbReceiver, filter);
	}
	
	//USB授权
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
			switch (intent.getAction()) {
			//USB设备插入
			case "android.hardware.usb.action.USB_DEVICE_ATTACHED":
				if ((intent.getBooleanExtra("permission", false)) && (device != null)) {
					Log.d(TAG, "UsbDevice (VID:"+device.getVendorId()+",PID:"+device.getProductId() +") DEVICE_ATTACHED");
				}
				break;
			//USB设备拔出
			case "android.hardware.usb.action.USB_DEVICE_DETACHED":
				if ((intent.getBooleanExtra("permission", false)) && (device != null)) {
					Log.d(TAG, "UsbDevice (VID:"+device.getVendorId()+",PID:"+device.getProductId() +") DEVICE_DETACHED");
				}
				break;
			case "com.android.example.USB_PERMISSION":
			case "com.hdos.usbdevice.UsbDeviceLib.USB_PERMISSION":
				synchronized (this) {
					if ((intent.getBooleanExtra("permission", false)) && (device != null)) {
						Log.d(TAG, "permission request get" + device);
					} else {
						Log.d(TAG, "permission denied for device " + device);
					}
				}
				break;
			}
		}
	};

	public int getUsbFileDescriptor(int VendorId,int ProductID) {
		int UsbFileDescriptor = 0;
		mUsbManager = ((UsbManager) mContext.getSystemService("usb"));
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		if (deviceList.size() <= 0) {
			return -ERR_NOT_FOUND_USBDEVICE;
		}
		if (!deviceList.isEmpty()) {
			for (UsbDevice device : deviceList.values()) {
				setUsbDevPath(device.getDeviceName());
				if ((device.getVendorId() == VendorId) && (device.getProductId() == ProductID)) {
					mUsbDevice = device;
					//判断下设备权限，如果没有权限，则请求权限
					if (!mUsbManager.hasPermission(mUsbDevice)) {
						mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
					}
				}
			}
		}
		/*
		if (mUsbDevice != null) {
			Log.d(TAG, "interfaceCounts : " + mUsbDevice.getInterfaceCount());
			for (int i = 0; i < mUsbDevice.getInterfaceCount(); i++) {
				UsbInterface intf = mUsbDevice.getInterface(i);
				Log.d(TAG,"interface class =" + intf.getInterfaceClass() + " ;InterfaceSubclass="+ intf.getInterfaceSubclass() + " ;InterfaceProtocol=" + intf.getInterfaceProtocol());
				if ((intf.getInterfaceClass() == 3) && (intf.getInterfaceSubclass() == 0)&& (intf.getInterfaceProtocol() == 0)) {
					mInterface = intf;
					break;
				}
			}
		}
		if (mInterface != null)
		*/
		if (mUsbDevice != null)
		{
			UsbDeviceConnection conn = null;
			if (mUsbManager.hasPermission(mUsbDevice)) {
				Log.d(TAG, "has permission");
				conn = mUsbManager.openDevice(mUsbDevice);
			} else {
				Log.d(TAG, "no permission");
				return -ERR_USB_NOPERMISSION;
			}
			if (conn == null) {	
				return -ERR_USB_CANNOT_CONNTECT;	
			}
			mDeviceConnection = conn;
			UsbFileDescriptor = conn.getFileDescriptor();
			Log.d(TAG, " getFileDescriptor is "+ UsbFileDescriptor);
			/*
			if (conn.claimInterface(mInterface, true)) {
				mDeviceConnection = conn;
				UsbFileDescriptor = conn.getFileDescriptor();
				Log.d(TAG, "mtReaderDev FileDescriptor is " + UsbFileDescriptor);
			} else {
				conn.close();
			}
			boolean isOp = isOpen();
			return isOp;
			*/
			return UsbFileDescriptor;
		}
		return -ERR_NOT_FOUND_USBDEVICE;
	}

	public boolean isOpen() {
		if ((mUsbManager != null) && (mUsbDevice != null)
				/*&& (mInterface != null)*/
				&& (mDeviceConnection != null)
				&& (mUsbManager.hasPermission(mUsbDevice))) {
			return true;
		}
		return false;
	}
	
	public String getUsbDevPath() {
		return usbDevPath;
	}

	public static void setUsbDevPath(String usbDevPath) {
		UsbDevPermission.usbDevPath = usbDevPath;
	}
}
