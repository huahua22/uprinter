package com.xwr.uprinter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Iterator;

import print.Print;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  private static final String TAG = "xwr";
  private UsbManager mUsbManager = null;
  private UsbDevice device = null;
  private PendingIntent mPermissionIntent = null;
  private static final String ACTION_USB_PERMISSION = "com.xwr.uprinter";
  private Context thisCon = null;
  Button btnPrint;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    btnPrint = findViewById(R.id.print);
    btnPrint.setOnClickListener(this);
    thisCon = this.getApplicationContext();
    mPermissionIntent = PendingIntent.getBroadcast(thisCon, 0, new Intent(ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
    this.registerReceiver(mUsbReceiver, filter);
    connectUSB();
  }

  private void connectUSB() {
    mUsbManager = (UsbManager) thisCon.getSystemService(Context.USB_SERVICE);
    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

    boolean HavePrinter = false;
    while (deviceIterator.hasNext()) {
      device = deviceIterator.next();
      int count = device.getInterfaceCount();
      for (int i = 0; i < count; i++) {
        UsbInterface intf = device.getInterface(i);
        if (intf.getInterfaceClass() == 7) {
          Log.d(TAG, "vendorID--" + device.getVendorId() + "ProductId--" + device.getProductId());
          //							if (device.getVendorId()==8401&&device.getProductId()==28680){
          //								Log.d("PRINT_TAG","123");
          HavePrinter = true;
          mUsbManager.requestPermission(device, mPermissionIntent);
          //							}
        }
      }
    }
    if (!HavePrinter)
      Log.d(TAG, "请连接打印机");
  }

  private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      try {
        String action = intent.getAction();
        if (ACTION_USB_PERMISSION.equals(action)) {
          synchronized (this) {
            device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
              if (Print.PortOpen(thisCon, device) != 0) {
                Log.d(TAG, "success fail");
                return;
              } else
                Log.d(TAG, "connect success");

            } else {
              return;
            }
          }
        }
        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
          device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
          if (device != null) {
            int count = device.getInterfaceCount();
            for (int i = 0; i < count; i++) {
              UsbInterface intf = device.getInterface(i);
              //Class ID 7代表打印机
              if (intf.getInterfaceClass() == 7) {
                Print.PortClose();
                Log.d(TAG, "请连接打印机");
              }
            }
          }
        }
      } catch (Exception e) {
        Log.e(TAG, (new StringBuilder("Activity_Main --> mUsbReceiver ")).append(e.getMessage()).toString());
      }
    }
  };

  public void PrintTestPage() {
    try {
      Print.PrintText("看着飞舞的尘埃\n");
      Print.PrintText("没人发现它存在 多自由自在\n");
      Print.PrintText("可世界都爱热热闹闹\n");
      Print.PrintText("容不下我百无聊赖\n");
      Print.PrintText("不应该\n");
      Print.PrintText("一个人 发呆\n");
      Print.PrintText("只有我\n");
      Print.PrintText("守着安静的沙漠\n");
      Print.PrintText("等待着花开\n");
      Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher_round);
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      Bitmap bmp = bitmapDrawable.getBitmap();
      Print.PrintBitmap(bmp, 1, 0);
      Print.CutPaper(1,8);
    } catch (Exception e) {
      Log.e(TAG, (new StringBuilder("Activity_Main --> onClickPrint:")).append(e.getMessage()).toString());
    }
  }


  @Override
  public void onClick(View v) {
    PrintTestPage();
  }
}
