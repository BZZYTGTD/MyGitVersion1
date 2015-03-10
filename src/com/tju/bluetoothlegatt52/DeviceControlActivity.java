/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tju.bluetoothlegatt52;

import java.io.FileInputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tju.bluetoothlegatt52.R;
import com.tju.bluetoothlegatt52.WifiService_Server.LocalBinder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements OnClickListener {
	private final static String TAG = DeviceControlActivity.class.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	public static final String EXTRAS_SERVER_ADDRESS = "SERVER_ADDRESS";
	public static final String EXTRAS_SERVER_PORT = "SERVER_PORT";

	private Button button, button2, startTest, stopTest, openRecord, pause_start, filterSelect;
	// private SeekBar seekBarx, seekBary;
	private SeekBar seekBarRecord;
	private TextView mConnectionState;
	private TextView mDataField;
	private static  TextView heartRate;
	// private TextView debug_data;
	private TextView breathRate;
	private Switch switch1;
	private String mDeviceName;
	private String mDeviceAddress;
	private String mServerAddress;
	private int mServerPort;
	private DrawUtils mDrawUtils = new DrawUtils();
	private ExpandableListView mGattServicesList;

	private WifiService_Server wService;

	private boolean mConnected = false;

	private ReadFileThread mReadFileThread = null;
	private int drawThreadId = 0;
	private GestureDetector mGestureDetector = null;

	private Intent gattServiceIntent = null;
	private Intent wifiServiceIntent = null;

	private boolean wifi_or_ble;
	private ArrayList<Integer> displayBuf = new ArrayList<Integer>();

	private final int menuStartTest = 101;
	private final int menuStopTest = 103;
	private final int menuFilter = 105;
	private final int menuFilterA = 107;
	private final int menuFilterB = 109;
	private final int readRecord = 111;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";

	private PopupWindow popupWindow;
	private ListView listViewMenu;

	private native int QRSDetNative(double[] data1, int rdIdx, 
			int[] data2, int[] data3, int[] data4, int[] data5,
			int[] data6, int[] data7, int[] data8,
			int[] RCount);

	// Code to manage Service lifecycle.
	// private final ServiceConnection mServiceConnection = new
	// ServiceConnection() {
	//
	// @Override
	// public void onServiceConnected(ComponentName componentName, IBinder
	// service) {
	// mBluetoothLeService = ((BluetoothLeService.LocalBinder)
	// service).getService();
	// myBind = (BluetoothLeService.LocalBinder) service;
	// if (!mBluetoothLeService.initialize()) {
	// Log.e(TAG, "Unable to initialize Bluetooth");
	// finish();
	// }
	// // Automatically connects to the device upon successful start-up
	// // initialization.
	// mBluetoothLeService.connect(mDeviceAddress);
	// }
	//
	// @Override
	// public void onServiceDisconnected(ComponentName componentName) {
	// mBluetoothLeService = null;
	// }
	// };

	// Code to manage Service lifecycle.
	private final ServiceConnection wServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			System.out.println("onServiceConnected");
			LocalBinder lBinder = (LocalBinder) service;
			wService = lBinder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			System.out.println("onServiceDisconnected");
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			// System.out.println("action----"+action);
			if (WifiService_Server.ACTION_DATA_WIFI_ECG.equals(action)) {
				int[] dataWifi = intent.getIntArrayExtra(WifiService_Server.DATA_WIFI);
				synchronized (displayBuf) {
					// for (int i : dataWifi) {
					// displayBuf.add(dataWifi[i]);
					// if(displayBuf.size() >= nDrawArraySize*2){
					// displayBuf.remove(0);
					// }
					// }
					for (int i = 0; i < dataWifi.length; i++) {
						displayBuf.add(dataWifi[i]);
						if (displayBuf.size() >= nDrawArraySize * 2) {
							displayBuf.remove(0);
						}
					}
					System.out.println("displayBuf.size() : " + displayBuf.size());
				}
				displayData2(wService.hRate);
				switch1.setChecked(wService.Senser);
				System.out.println("WifiService.ACTION_DATA_WIFI_ECG.equals(action)" + dataWifi.length);
			}
		}
		
	};
	
	public static void displayData2(String hRate){
		heartRate.setText(hRate);
	}
	// If a given GATT characteristic is selected, check for supported features.
	// This sample
	// demonstrates 'Read' and 'Notify' features. See
	// http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for
	// the complete
	// list of supported characteristic features.
	// private final ExpandableListView.OnChildClickListener
	// servicesListClickListner = new ExpandableListView.OnChildClickListener()
	// {
	// @Override
	// public boolean onChildClick(ExpandableListView parent, View v, int
	// groupPosition, int childPosition, long id) {
	// if (mGattCharacteristics != null) {
	// final BluetoothGattCharacteristic characteristic =
	// mGattCharacteristics.get(groupPosition).get(childPosition);
	// final int charaProp = characteristic.getProperties();
	// System.out.println(charaProp + "charaprop");
	// if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
	// // If there is an active notification on a characteristic,
	// // clear
	// // it first so it doesn't update the data field on the user
	// // interface.
	// if (mNotifyCharacteristic != null) {
	// mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic,
	// false);
	// mNotifyCharacteristic = null;
	// }
	// mBluetoothLeService.readCharacteristic(characteristic);
	// }
	// if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
	// mNotifyCharacteristic = characteristic;
	// mBluetoothLeService.setCharacteristicNotification(characteristic, true);
	// }
	// // if((charaProp | BluetoothGattCharacteristic.FORMAT_SINT8) >
	// // 0)
	// // System.out.println("hahahahh");
	// return true;
	// }
	// return false;
	// }
	// };

	private void clearUI() {
		// mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		// mDataField.setText(R.string.no_data);
	}
	private float fStartX = 100.0f;
	private int mHeight;
	private int mWidth;
	private int mTop;
	private Paint mPaint;
	private SurfaceHolder surfaceHolder;
	private MyView myView;
	private LinearLayout linearLayout;
	private boolean dataFromBluetooth = true;
	private boolean dataFromRecord = false;
	private boolean dataFromWifi = true;
	private boolean characteristicNotificationEnabled = false;
	private boolean readBoolean = true;
	private ProgressDialog readRecordDialog = null;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			readRecordDialog.dismiss();
			displayData(wService.hRate);
		}

	};

	LayoutInflater inflater = null;
	View view = null;
	List<Map<String, String>> list = null;

	private void initPopUpMenu() {

		inflater = LayoutInflater.from(this);
		view = inflater.inflate(R.layout.listview_menu, null);
		listViewMenu = (ListView) view.findViewById(R.id.listviewmenu);
		list = new ArrayList<Map<String, String>>();
		Map<String, String> map = new HashMap<String, String>();

//		map.put("item", this.getString(R.string.menu1_connect_item));// 连接和断开
//		map.put("state", this.getString(R.string.menu1_disconnected));// 已断开
//		list.add(map);

		map = new HashMap<String, String>();
		map.put("item", this.getString(R.string.menu1_state_item));// 开始和暂停
		map.put("state", this.getString(R.string.menu1_stop));// 已停止
		list.add(map);

		map = new HashMap<String, String>();
		map.put("item", this.getString(R.string.menu1_read_record));
		map.put("state", this.getString(R.string.menu1_record_null));
		list.add(map);

		map = new HashMap<String, String>();
		map.put("item", this.getString(R.string.menu1_change_filter));
		map.put("state", this.getString(R.string.menu1_filter_A));
		list.add(map);
		// help暂时没做任何事情
		map = new HashMap<String, String>();
		map.put("item", this.getString(R.string.menu1_help));
		map.put("state", this.getString(R.string.menu1_null));
		list.add(map);

		SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item, new String[] { "item", "state" }, new int[] { R.id.menu_item, R.id.menu_state });
		listViewMenu.setAdapter(adapter);
		listViewMenu.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("static-access")
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				// TODO Auto-generated method stub
				// System.out.println("item->" + position);
				switch (position) {
				
//				case 0:
//					if (list.get(position).get("state").equals(arg1.getContext().getString(R.string.menu1_disconnected))) {
//						HashMap<String, String> map = new HashMap<String, String>();
//						map.put("item", arg1.getContext().getString(R.string.menu1_connect_item));// 连接和断开
//						map.put("state", arg1.getContext().getString(R.string.menu1_connecting));// 已连接
//						list.set(position, map);
//						SimpleAdapter adapter = new SimpleAdapter(arg1.getContext(), list, R.layout.item, new String[] { "item", "state" }, new int[] {
//								R.id.menu_item, R.id.menu_state });
//						listViewMenu.setAdapter(adapter);
//					} else {
//						HashMap<String, String> map = new HashMap<String, String>();
//						map.put("item", arg1.getContext().getString(R.string.menu1_connect_item));
//						map.put("state", arg1.getContext().getString(R.string.menu1_disconnecting));
//						list.set(position, map);
//						SimpleAdapter adapter = new SimpleAdapter(arg1.getContext(), list, R.layout.item, new String[] { "item", "state" }, new int[] {
//								R.id.menu_item, R.id.menu_state });
//						listViewMenu.setAdapter(adapter);
//					}
//					break;
				case 0:
//					if (!wifi_or_ble) {
//						if (list.get(position).get("state").equals(arg1.getContext().getString(R.string.menu1_stop))) {
//							// start test
////							dataFromBluetooth = true;
//							dataFromRecord = false;
//							clearBuf();
//							nDrawIndex = 0;
//							isDrawing = true;
//							if (drawArray != null)
//								for (int i = 0; i < drawArray.length; i++)
//									drawArray[i] = null;
//							mDrawUtils.draw();
//							if (mDrawThread == null) {
//								mDrawThread = new DrawThread(myView);
//								mDrawThread.start();
//							}
//							mDrawThread = null;
//							HashMap<String, String> map = new HashMap<String, String>();
//							map.put("item", arg1.getContext().getString(R.string.menu1_state_item));
//							map.put("state", arg1.getContext().getString(R.string.menu1_start));
//							list.set(position, map);
//							SimpleAdapter adapter = new SimpleAdapter(arg1.getContext(), list, R.layout.item, new String[] { "item", "state" }, new int[] {
//									R.id.menu_item, R.id.menu_state });
//							listViewMenu.setAdapter(adapter);
//						} else {
//							// stop test
//							characteristicNotificationEnabled = false;
//							android.os.Process.killProcess(drawThreadId);
//							mReadFileThread = null;
//							HashMap<String, String> map = new HashMap<String, String>();
//							map.put("item", arg1.getContext().getString(R.string.menu1_state_item));
//							map.put("state", arg1.getContext().getString(R.string.menu1_stop));
//							list.set(position, map);
//							SimpleAdapter adapter = new SimpleAdapter(arg1.getContext(), list, R.layout.item, new String[] { "item", "state" }, new int[] {
//									R.id.menu_item, R.id.menu_state });
//							listViewMenu.setAdapter(adapter);
//						}
//					} 
//					else {
						if (list.get(position).get("state").equals(arg1.getContext().getString(R.string.menu1_stop))) {

							System.out.println("点击开始");
							wService.startReceive();
							dataFromRecord = false;
							
							isDrawing = true;
							 xPosition = 0;
							if (drawArray != null)
								for (int i = 0; i < drawArray.length; i++)
									drawArray[i] = null;
							mDrawUtils.draw();
							if (mDrawThread == null) {
								mDrawThread = new DrawThread(myView);
								mDrawThread.start();
							}
							
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("item", arg1.getContext().getString(R.string.menu1_state_item));
							map.put("state", arg1.getContext().getString(R.string.menu1_start));
							list.set(position, map);
							SimpleAdapter adapter = new SimpleAdapter(arg1.getContext(), list, R.layout.item, new String[] { "item", "state" }, new int[] {
									R.id.menu_item, R.id.menu_state });
							listViewMenu.setAdapter(adapter);
						} else {
							wService.stopReceive();
							android.os.Process.killProcess(drawThreadId);
							displayBuf = new ArrayList<Integer>();
							isDrawing = false;
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("item", arg1.getContext().getString(R.string.menu1_state_item));
							map.put("state", arg1.getContext().getString(R.string.menu1_stop));
							list.set(position, map);
							SimpleAdapter adapter = new SimpleAdapter(arg1.getContext(), list, R.layout.item, new String[] { "item", "state" }, new int[] {
									R.id.menu_item, R.id.menu_state });
							listViewMenu.setAdapter(adapter);
						}

//					}
					break;
				case 1:
					if (list.get(position).get("state").equals(arg1.getContext().getString(R.string.menu1_record_null))) {
						iii = 0;
						// fileUtils.createLog("ecgProcessed.txt");
						dataFromRecord = true;
						dataFromWifi = false;
						clearBuf();
						readRecordDialog = ProgressDialog.show(DeviceControlActivity.this, "读记录文件中", "请等待", true);
						if (mReadFileThread == null) {
							// mReadFileThread = new
							// ReadFileThread("ECG/ecgProcessed.txt");
							mReadFileThread = new ReadFileThread("ECG/ecg1_11_20_1.txt");
							mReadFileThread.start();
							System.out.println("开始读取");
						} else { // if(!mReadFileThread.isAlive()){
							readBoolean = true;
						}
						// readRecord("ECG/ecg.log");
						if (drawArray != null)
							for (int i = 0; i < drawArray.length; i++)
								drawArray[i] = null;
						// if(inBuf2.size() != 0){
						//
						// dataFromBluetooth = false;
						// dataFromRecord = true;
						// }
						isDrawing = true;
						System.out.println("mDrawThread" + mDrawThread);
						if (mDrawThread == null) {
							mDrawThread = new DrawThread(myView);
							mDrawThread.start();
							System.out.println("mDrawThread  start!");
						} else {

						}

						HashMap<String, String> map = new HashMap<String, String>();
						map.put("item", arg1.getContext().getString(R.string.menu1_read_record));
						map.put("state", arg1.getContext().getString(R.string.menu1_record_stop));// 停止读取
						list.set(position, map);
						SimpleAdapter adapter = new SimpleAdapter(arg1.getContext(), list, R.layout.item, new String[] { "item", "state" }, new int[] {
								R.id.menu_item, R.id.menu_state });
						listViewMenu.setAdapter(adapter);
					} else {
						dataFromRecord = false;
						clearBuf();
						System.out.println("vvvv  " + dataFromRecord + "stop  readRecord");
						// mReadFileThread.destroy();
						characteristicNotificationEnabled = false;
						android.os.Process.killProcess(drawThreadId);

						HashMap<String, String> map = new HashMap<String, String>();
						map.put("item", arg1.getContext().getString(R.string.menu1_read_record));
						map.put("state", arg1.getContext().getString(R.string.menu1_record_null));
						list.set(position, map);
						SimpleAdapter adapter = new SimpleAdapter(arg1.getContext(), list, R.layout.item, new String[] { "item", "state" }, new int[] {
								R.id.menu_item, R.id.menu_state });
						listViewMenu.setAdapter(adapter);

					}
					break;
				case 2:
					if (list.get(position).get("state").equals(arg1.getContext().getString(R.string.menu1_filter_A))) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("item", arg1.getContext().getString(R.string.menu1_connect_item));
						map.put("state", arg1.getContext().getString(R.string.menu1_filter_B));
						list.set(position, map);
						SimpleAdapter adapter = new SimpleAdapter(arg1.getContext(), list, R.layout.item, new String[] { "item", "state" }, new int[] {
								R.id.menu_item, R.id.menu_state });
						listViewMenu.setAdapter(adapter);
					} else {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("item", arg1.getContext().getString(R.string.menu1_connect_item));
						map.put("state", arg1.getContext().getString(R.string.menu1_filter_A));
						list.set(position, map);
						SimpleAdapter adapter = new SimpleAdapter(arg1.getContext(), list, R.layout.item, new String[] { "item", "state" }, new int[] {
								R.id.menu_item, R.id.menu_state });
						listViewMenu.setAdapter(adapter);
					}
					break;
				case 3:
					break;
				}

			}
		});

		popupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		// popupWindow.setBackgroundDrawable(null);
		ColorDrawable dw = new ColorDrawable(0xff);
		popupWindow.setBackgroundDrawable(dw);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
		popupWindow.update();
		popupWindow.setTouchable(true);
		popupWindow.setFocusable(true);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels; // 屏幕高度（像素）
		System.out.println("屏幕高度" + width);// 1280
		popupWindow.setWidth(width / 2);
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		window.setAttributes(params);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gatt_services_characteristics);

		final Intent intent = getIntent();
		heartRate = (TextView) findViewById(R.id.heartRate);	
		breathRate = (TextView) findViewById(R.id.breathRate);
		// debug_data = (TextView) findViewById(R.id.debug_data); // for debug
		// debug_data.setText(" " + fScaleY);
		switch1 = (Switch)findViewById(R.id.switch1);
		
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
		mServerAddress = intent.getStringExtra(EXTRAS_SERVER_ADDRESS);
		mServerPort = intent.getIntExtra(EXTRAS_SERVER_PORT, 0);
		
		if (mDeviceName == null && mServerPort != 0) {
			wifi_or_ble = true;
		} else {
			wifi_or_ble = false;
		}

		myView = new MyView(this);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int height = metric.heightPixels; // 屏幕高度（像素）
		LinearLayout.LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, height * 5 / 6);
		myView.setLayoutParams(p);
		myView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		linearLayout = (LinearLayout) findViewById(R.id.layout);
		linearLayout.addView(myView);
		
//		seekBarRecord = (SeekBar)findViewById(R.id.seekBar);
//		seekBarRecord.setOnSeekBarChangeListener(new XsbListener());
		// getActionBar().setTitle(mDeviceName);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		if (!wifi_or_ble) {
		} else {
			wifiServiceIntent = new Intent(this, WifiService_Server.class);
			wifiServiceIntent.putExtra(EXTRAS_SERVER_ADDRESS, mServerAddress);
			wifiServiceIntent.putExtra(EXTRAS_SERVER_PORT, mServerPort);
			bindService(wifiServiceIntent, wServiceConnection, BIND_AUTO_CREATE);
		}
		initPopUpMenu();
		
		OnGestureListener onGestureListener = new OnGestureListener() {

			//  鼠标按下的时候，会产生onDown。由一个ACTION_DOWN产生。   
			@Override
			public boolean onDown(MotionEvent e) {
				// TODO Auto-generated method stub
				if (!popupWindow.isShowing()) {
					popupWindow.showAsDropDown(myView, 0, -myView.getHeight());
				}
				return false;

			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				// TODO Auto-generated method stub
				if (Math.abs(velocityX) >= Math.abs(velocityY) && e2.getPointerCount() == 1) {
					if (!popupWindow.isShowing()) {
						popupWindow.showAsDropDown(myView, 0, -myView.getHeight());
					}
				}

				return false;
			}

			// 长按屏幕时触发
			@Override
			public void onLongPress(MotionEvent e) {
				// TODO Auto-generated method stub
				if (!popupWindow.isShowing()) {
					popupWindow.showAsDropDown(myView, 0, -myView.getHeight());
				}

			}

			// 当手在屏幕上滑动离开屏幕时触发
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				// TODO Auto-generated method stub
				// fScaleY
				// System.out.println("X = " + distanceX);
				// System.out.println("Y = " + distanceY);
				if (Math.abs(distanceX) < Math.abs(distanceY)) {
					if (wifi_or_ble) {
						fScaleY -= distanceY / 1000;
					} else {
						// fScaleY -= distanceY / 200;
						nBaseLine -= (int) distanceY;
					}
				}
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}

			// 添加未实现的方法

		};
		mGestureDetector = new GestureDetector(this, onGestureListener);
	}


	private double lastFingerDis;
	private double fingerDis;

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_POINTER_DOWN:
			if (event.getPointerCount() == 2) {
				lastFingerDis = getFingerDis(event);
			}
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() == 2) {
				fingerDis = getFingerDis(event);
				fScaleY = (float) (lastfScaleY - (fingerDis - lastFingerDis) / 10);
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			lastfScaleY = fScaleY;
			break;
		}

		return mGestureDetector.onTouchEvent(event);

	}

	private double getFingerDis(MotionEvent event) {
		// TODO Auto-generated method stub
		float disX = Math.abs(event.getX(0) - event.getX(1));
		float disY = Math.abs(event.getY(0) - event.getY(1));
		return Math.sqrt(disX * disX + disY * disY);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		isDrawing = true;

	}

	@Override
	protected void onPause() {
		super.onPause();
		wService.stopReceive();
		unregisterReceiver(mGattUpdateReceiver); 
		isDrawing = false;
	}

//	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!wifi_or_ble) {
		} else {
			wService.stopReceive();  
			unbindService(wServiceConnection);
		}
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (wifi_or_ble) {
				wService.stopReceive();
			}
			return super.onKeyDown(keyCode, event);
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			if (wifi_or_ble) {
				fScaleY -= 0.001f;
			} else {
				fScaleY /= 1.1f;
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if (wifi_or_ble) {
				fScaleY += 0.001f;
			} else {
				fScaleY *= 1.1f;
			}
			return true;
		} 
		else if(keyCode == KeyEvent.KEYCODE_MENU){
			if (!popupWindow.isShowing()) {
				popupWindow.showAsDropDown(myView, 0, -myView.getHeight());
			}
			return true;
		}
		else {
			return super.onKeyDown(keyCode, event);
		}
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.menu.gatt_services, menu);
	// if (mConnected) {
	// menu.findItem(R.id.menu_connect).setVisible(false);
	// menu.findItem(R.id.menu_disconnect).setVisible(true);
	// } else {
	// menu.findItem(R.id.menu_connect).setVisible(true);
	// menu.findItem(R.id.menu_disconnect).setVisible(false);
	// }
	// menu.add(0, menuStartTest, 1, "开始测试");
	// menu.add(0, menuStopTest, 1, "结束测试");
	// menu.add(0, readRecord, 1, "读取记录");
	// SubMenu filterMenu = menu.addSubMenu(0, menuFilter, 1, "滤波器选择");
	// MenuItem filterA = filterMenu.add(0, menuFilterA, 2, "滤波器A");
	// filterA.setChecked(true);
	// MenuItem filterB = filterMenu.add(0, menuFilterB, 2, "滤波器B");
	// filterMenu.setGroupCheckable(0, true, true);
	//
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.menu_connect:
	// mBluetoothLeService.connect(mDeviceAddress);
	// return true;
	// case R.id.menu_disconnect:
	// mBluetoothLeService.disconnect();
	// return true;
	// case android.R.id.home:
	// onBackPressed();
	// return true;
	// case menuFilterA:
	// myBind.filterSelect(true);
	// item.setChecked(true);
	// return true;
	// case menuFilterB:
	// myBind.filterSelect(false);
	// item.setChecked(true);
	// return true;
	// case menuStartTest:
	// mBluetoothLeService.dataRead();
	// dataFromBluetooth = true;
	// dataFromRecord = false;
	// clearBuf();
	// nDrawIndex = 0;
	// isDrawing = true;
	// if (drawArray != null)
	// for (int i = 0; i < drawArray.length; i++)
	// drawArray[i] = null;
	// mDrawUtils.draw();
	// if (mDrawThread == null) {
	// mDrawThread = new DrawThread(myView);
	// mDrawThread.start();
	// }
	// return true;
	// case menuStopTest:
	// mBluetoothLeService.stopDataRead();
	// characteristicNotificationEnabled = false;
	// android.os.Process.killProcess(drawThreadId);
	// return true;
	// case readRecord:
	// iii = 0;
	// // fileUtils.createLog("ecgProcessed.txt");
	// dataFromRecord = false;
	// clearBuf();
	// readRecordDialog = ProgressDialog.show(DeviceControlActivity.this,
	// "读记录文件中", "请等待", true);
	// if (mReadFileThread == null) {
	// mReadFileThread = new ReadFileThread("ECG/ecg1_11_20_1.txt");
	// mReadFileThread.start();
	// } else { // if(!mReadFileThread.isAlive()){
	// readBoolean = true;
	// }
	// // readRecord("ECG/ecg.log");
	// if (drawArray != null)
	// for (int i = 0; i < drawArray.length; i++)
	// drawArray[i] = null;
	// // if(inBuf2.size() != 0){
	// //
	// // dataFromBluetooth = false;
	// // dataFromRecord = true;
	// // }
	// isDrawing = true;
	// if (mDrawThread == null) {
	// mDrawThread = new DrawThread(myView);
	// mDrawThread.start();
	// } else {
	//
	// }
	// return true;
	// }
	// return super.onOptionsItemSelected(item);
	// }

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		isDrawing = false;
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// mConnectionState.setText(resourceId);
			}
		});
	}

	public void displayData(String data) {
		if (data != null) {
			mDataField.setText(data);
		}
	}

	// private int dataTime;
	private final int ecgDataLength = 18;

	// private boolean flag = false;

	private int rdIdx = 0;
	private int wrIdx = 0;
	private boolean wr_wrap = false;// 标志位

	private int drawDiv = 1;
	private int drawDivCnt = 0;

	public void displayData(float[] floatArray) {

		PointF p = null;
		for (int i = 1; i < floatArray.length; i++) {
			if (i % (ecgDataLength / 2) == 0)
				continue;
			float fx = floatArray[0];// (floatArray[0]+i/3-1)*10.0f/3.0f;//存储序列号，可以换成floarArray[i],此时放在if条件中
			float fy = floatArray[i];
			// System.out.println("fy=  "+fy);
			QRS_data[wrIdx] = fy;// 初始定义长度为4096*3
			wrIdx++;
			if (wrIdx >= QRS_data.length) {
				wrIdx = 0;
				wr_wrap = true;
			}

			// System.out.println("wr_wrap" + " " + wr_wrap + " " +
			// QRS_data_cnt);
			QRS_data_cnt = wr_wrap ? MAXLENGTH * 3 + wrIdx - rdIdx : wrIdx - rdIdx;

			if (QRS_data_cnt >= MAXLENGTH) {
				QRS_data_flag = true;
			} else {
				QRS_data_flag = false;
			}
			p = new PointF(fx, fy);

			drawDivCnt = (drawDivCnt + 1) % drawDiv;
//			紧邻一行为源代码
//			if (dataFromBluetooth == true && drawDivCnt == 0) {
			if ( drawDivCnt == 0) {
				synchronized (inBuf) {//
					inBuf.add(p);// 添加数据
					if (inBuf.size() > 2 * nDrawArraySize)
						inBuf = (ArrayList<PointF>) inBuf.subList(inBuf.size() - nDrawArraySize, inBuf.size());
					inBuf.notifyAll();
				}
			}

		}

		// QRS_data_flag为TRUE时，会开始测试心率
		// System.out.println("mmmmmn" + QRS_data_cnt + " " + QRS_data_flag +
		// " " + wr_wrap);

		if (QRS_data_flag) {
			// System.out.println("nnnnn"+ QRS_data_cnt + QRS_data_flag+ " " +
			// flag);
			// for(int i=0;i<MAXLENGTH - QRS_data_cnt;i++)
			// gg[i] = QRS_data[i+QRS_data_cnt];
			// for(int i=MAXLENGTH-QRS_data_cnt;i<MAXLENGTH;i++)
			// gg[ i] = QRS_data[i-(MAXLENGTH-QRS_data_cnt)];

			 QRSDetNative(QRS_data, rdIdx, R_location, TStart, TEnd, QStart,
			 QEnd, PStart, PEnd, RCount);
			 System.out.println(" "+RCount[0] +" "+RCount[2]+" "+ RCount[1]);
			 System.out.println("-------Rate---"+(RCount[0]-1)*250*60/(RCount[2]-RCount[1]));
			if (RCount[2] != RCount[1])
				heartRate.setText((RCount[0] - 1) * 250 * 60 / (RCount[2] - RCount[1]) + "");
			rdIdx += 1000;
			while (rdIdx >= QRS_data.length) {
				rdIdx -= QRS_data.length;
				wr_wrap = false;
			}
		}
	}

//	private void displayGattServices(List<BluetoothGattService> gattServices) {
//		if (gattServices == null)
//			return;
//		String uuid = null;
//		String unknownServiceString = getResources().getString(R.string.unknown_service);
//		String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
//		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
//		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
//		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
//
//		// Loops through available GATT Services.
//		for (BluetoothGattService gattService : gattServices) {
//			HashMap<String, String> currentServiceData = new HashMap<String, String>();
//			uuid = gattService.getUuid().toString();
//			currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
//			currentServiceData.put(LIST_UUID, uuid);
//			gattServiceData.add(currentServiceData);
//
//			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
//			List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
//
//			// Loops through available Characteristics.
//			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//				charas.add(gattCharacteristic);
//				HashMap<String, String> currentCharaData = new HashMap<String, String>();
//				uuid = gattCharacteristic.getUuid().toString();
//				currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
//				currentCharaData.put(LIST_UUID, uuid);
//				gattCharacteristicGroupData.add(currentCharaData);
//				List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
//				for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
//					byte[] buffer = gattDescriptor.getValue();
//
//					if (buffer != null && buffer.length > 0) {
//						final StringBuilder stringBuilder = new StringBuilder(buffer.length);
//						for (byte byteChar : buffer)
//							stringBuilder.append(String.format("%02X ", byteChar));
//						System.out.println(stringBuilder.toString() + "----------");
//					}
//				}
//
//			}
//			gattCharacteristicData.add(gattCharacteristicGroupData);
//		}
//
//		SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(this, gattServiceData, android.R.layout.simple_expandable_list_item_2,
//				new String[] { LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1, android.R.id.text2 }, gattCharacteristicData,
//				android.R.layout.simple_expandable_list_item_2, new String[] { LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1, android.R.id.text2 });
//		mGattServicesList.setAdapter(gattServiceAdapter);
//	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiService_Server.ACTION_DATA_WIFI_ECG);
		return intentFilter;
	}

	class XsbListener implements SeekBar.OnSeekBarChangeListener {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// TODO Auto-generated method stub
			fScaleX = (float) (progress - 1000);
//			fStartX = (float) (progress - 1000);
//			System.out.println("fStartX" + fStartX);
			xRate = progress / 20 + 1;
		
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

	}

	class YsbListener implements SeekBar.OnSeekBarChangeListener {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// TODO Auto-generated method stub
			fScaleY = -progress / 0.1f;
			
			
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

	}

	class RecordsbListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// TODO Auto-generated method stub
			if (fromUser == true) {
				dataFromRecord = false;
				iii = progress * inBuf2.size() / 100;
				dataFromRecord = true;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			// isChanging = true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			// isChanging = false;
			// iii = seekBar.getProgress()*inBuf2.size()/100;

		}

	}

	class MyView extends SurfaceView implements SurfaceHolder.Callback {

		public MyView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			surfaceHolder = this.getHolder();
			surfaceHolder.addCallback(this);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			// draw();
			mHeight = getHeight();
			mWidth = getWidth();
			mTop = getTop();
			mPaint = new Paint();
			mPaint.setStyle(Style.STROKE);
			mPaint.setAntiAlias(true);
			mPaint.setColor(Color.WHITE);
			mPaint.setStrokeWidth(2);
			mDrawUtils.setValue(mHeight, mWidth, mTop, surfaceHolder, mPaint);
			mDrawUtils.draw();

		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			// TODO Auto-generated method stub

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub

		}
	}

	int t = 0;
	private ArrayList<PointF> inBuf = new ArrayList<PointF>();
	private PointF[] drawArray;
	private int nDrawArraySize;
	private int nDrawIndex = 0;
	public boolean isReceiving = true;// 线程控制标记
	public boolean isDrawing = true;
	private float lastfScaleY = -0.01f;
	float fScaleX = 100.0f;
	float fScaleY = lastfScaleY;
	int nBaseLine;
	float temp_dat = 0.0f;
	public int xRate = 1;
	DrawThread mDrawThread;
	private int iii = 0;
	private final int len = 10;
	private int xPosition = 0;
	
	class DrawThread extends Thread {
		private SurfaceView sfv;// 画板
		private int mHeight;
		private int mWidth;
		private Paint mPaint;
		private int nYNum;
		private int nXNum;
		private int nDelta;
		 float fStartX = 100.0f;
		float fEndX = 0.0f;

		private Path path;
		

		public DrawThread(SurfaceView sfv) {
			this.sfv = sfv;
			this.mPaint = new Paint();
			this.mPaint.setStyle(Style.STROKE);
			this.mPaint.setAntiAlias(true);
			this.mPaint.setColor(Color.RED);
			this.mPaint.setStrokeWidth(2);
			this.mWidth = this.sfv.getWidth();

			this.mHeight = this.sfv.getHeight();
			nBaseLine = this.mHeight / 2;
			this.nYNum = 6; // /////
			this.nDelta = this.mHeight / this.nYNum / 5; // /////
			this.nXNum = this.mWidth / (this.nDelta * 5); // /////
			fStartX = ((float) this.mWidth - (this.nXNum * 5 * this.nDelta)) / 2.0f; // //////
			fEndX = this.mWidth - fStartX;
			nDrawArraySize = (int) (fEndX - fStartX);
			drawArray = new PointF[nDrawArraySize];// ////

		}

		@SuppressWarnings("unchecked")
		public void run() {
			
			
			ArrayList<PointF> buf = new ArrayList<PointF>();
			int nBufSize;
			drawThreadId = (int) Thread.currentThread().getId();

			while (true) {
				// System.out.println(Thread.currentThread().getId()+"DrawThread");
				 //System.out.println("-----"+"画图线程"+getName());

				if (isDrawing) {
					if (!wifi_or_ble) {
						if (dataFromBluetooth == true) {
							synchronized (inBuf) {
								while (inBuf.size() < 25) {
									try {
										// Thread.sleep(5);
										inBuf.wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								buf = (ArrayList<PointF>) inBuf.clone();// 保存
								// Log.v(TAG, "DRAW inbuf.size " + inBuf.size()
								// + "buf.size " + buf.size());
								inBuf.clear();// 清除
								nBufSize = buf.size();
								// if (nBufSize > nDrawArraySize) {
								// for (int ii = 0; ii < nDrawArraySize; ii++) {
								// drawArray[ii] = buf.get(nBufSize -
								// nDrawArraySize + ii);
								// }
								// } else if (nDrawIndex + nBufSize >=
								// nDrawArraySize) {
								// for (int ii = 0; ii < nDrawArraySize -
								// nBufSize; ii++) {
								// drawArray[ii] = drawArray[nDrawIndex +
								// nBufSize - nDrawArraySize + ii];
								// }
								// for (int ii = nDrawArraySize - nBufSize; ii <
								// nDrawArraySize; ii++)
								// drawArray[ii] = buf.get(ii - (nDrawArraySize
								// - nBufSize));
								// } else if (nDrawIndex + nBufSize <
								// nDrawArraySize) {
								// for (int ii = 0; ii < nBufSize; ii++) {
								// drawArray[nDrawIndex + ii] = buf.get(ii);
								// }
								// }

								if (nBufSize > nDrawArraySize) {
									for (int ii = 0; ii < drawArray.length; ii++) {
										drawArray[ii] = buf.get(nBufSize - nDrawArraySize + ii);
									}
								} else {
									for (int ii = 0; ii < nBufSize; ii++) {
										drawArray[(ii + nDrawIndex) % nDrawArraySize] = buf.get(ii);
									}
								}

								// if ((nDrawIndex + nBufSize) >=
								// nDrawArraySize) {
								// for (int ii = 0; ii < nBufSize; ii++) {
								// if ((nDrawIndex + ii) >= nDrawArraySize) {
								// drawArray[nDrawIndex + ii - nDrawArraySize] =
								// buf
								// .get(ii);
								// } else {
								// drawArray[nDrawIndex + ii] = buf.get(ii);
								// }
								// }
								//
								// } else {
								// for (int ii = 0; ii < nBufSize; ii++) {
								// drawArray[nDrawIndex + ii] = buf.get(ii);
								// }
								// }
								if (xRate != 1) {
									for (int j = 0; j < drawArray.length; j++) {
										if (j % xRate != 0) {
											drawArray[j] = null;
										}
									}
								}
								nDrawIndex = (nBufSize + nDrawIndex) % nDrawArraySize;
								DrawECG();

								// while (nDrawIndex + inBufSize-1 >=
								// nDrawArraySize) {
								// nDrawIndex -= nDrawArraySize - inBufSize +1;
								// }
							}
						} 
						
					} else if(wifi_or_ble) {
						if(dataFromWifi == true){
							if (displayBuf.size() >= 25) {
								synchronized (displayBuf) {
									for (int i = 0; i < 25; i++) {
										drawArray[xPosition++] = new PointF(xPosition, displayBuf.get(0));
										displayBuf.remove(0);
										if (xPosition >= nDrawArraySize) {
											xPosition = 0;
										}
									}
								}
	
							}
							DrawECG();
						}else if (dataFromRecord == true) {
						System.out.println("Record    drawing!!!!");
						if (inBuf2.size() < len && inBuf2.size() > 0) {
							for (int ii = 0, iii = 0; ii < len; ii++, iii++) {
								drawArray[nDrawArraySize - ii - 1] = inBuf2.get(iii);
							}
						} else {
							if (inBuf2.size() - iii >= len) {
								for (int ii = 0; ii < nDrawArraySize - len; ii++) {
									drawArray[ii] = drawArray[ii + len];
								}
								for (int ii = nDrawArraySize - len; ii < nDrawArraySize; ii++, iii++) {
									drawArray[ii] = inBuf2.get(iii);
								}
							} else {
								for (int ii = 0; ii < nDrawArraySize - (inBuf2.size() - iii); ii++) {
									drawArray[ii] = drawArray[ii + (inBuf2.size() - iii)];
								}
								for (int ii = nDrawArraySize - (inBuf2.size() - iii); ii < nDrawArraySize; ii++, iii++) {
									drawArray[ii] = inBuf2.get(iii);
								}
							}
							// if (inBuf2.size() != 0)
							// seekBarRecord.setProgress(iii * 100 /
							// inBuf2.size());
						}
						DrawECG();
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
				else {
					try {
						sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}

		void DrawECG() {
//			System.out.println("DrawECG");
			Canvas canvas = surfaceHolder.lockCanvas(new Rect(0, 0, mWidth, mHeight));// 关键:获取画布
			if (canvas != null) {
				canvas.drawColor(Color.BLACK);// 清除背景
				// TODO: handle exception
				
				 mPaint.setStrokeWidth(1);
				 mPaint.setStyle(Style.STROKE);
				 mPaint.setColor(Color.argb(255, 248, 195, 175));
//				 mPaint.setColor(Color.argb(255, 50, 50, 50));
				
				 for (int yy = 0; yy <= nYNum * 5; yy++) {
					 canvas.drawLine(fStartX, nBaseLine + (yy - 15) * (nDelta),
					 fEndX, nBaseLine + (yy - 15) * (nDelta), mPaint);
				 }
				 for (int xx = 0; xx <= nXNum * 5; xx++) {
					 canvas.drawLine(fStartX + xx * nDelta, nBaseLine - 3 *
					 (nDelta * 5), fStartX + xx * nDelta, nBaseLine + 3 * (nDelta
					 * 5), mPaint);
				 }
				
				 mPaint.setColor(Color.argb(255, 240, 152, 116));
				 for (int yy = 0; yy <= nYNum; yy++) {
					 canvas.drawLine(fStartX, nBaseLine + (yy - 3) * (nDelta * 5),
					 fEndX, nBaseLine + (yy - 3) * (nDelta * 5), mPaint);
				 }
				 for (int xx = 0; xx <= nXNum * 5; xx++) {
					 canvas.drawLine(fStartX + xx * 5 * nDelta, nBaseLine - 3 *
					 (nDelta * 5), fStartX + xx * 5 * nDelta, nBaseLine + 3 *
					 (nDelta * 5), mPaint);
				 }
				mPaint.setColor(Color.YELLOW);
				mPaint.setStrokeWidth(2);
				mPaint.setStyle(Style.STROKE);

				path = new Path();
				if (drawArray[0] != null)
					path.moveTo(0.0f + fStartX, nBaseLine + fScaleY * drawArray[0].y);
				else
					path.moveTo(0.0f + fStartX, nBaseLine);

				for (int i = 1; i < nDrawArraySize; i++) {
					if (drawArray[i] != null) {
						path.lineTo(i + fStartX, nBaseLine + fScaleY * drawArray[i].y);
						// if(drawArray[i].x == 0.111f )
						// canvas.drawCircle((i) * 1.0f+fStartX, nBaseLine +
						// fScaleY
						// * drawArray[i].y, 4, mPaint);

					} else
						path.moveTo(i + fStartX, nBaseLine);
				}
				canvas.drawPath(path, mPaint);
				// mPaint.setStyle(Style.FILL);
				// mPaint.setColor(Color.CYAN);
				// if (nDrawIndex != 0 && drawArray[nDrawIndex - 1] != null &&
				// dataFromBluetooth)
				// canvas.drawCircle((nDrawIndex - 1) * 1.0f + fStartX,
				// nBaseLine + fScaleY * drawArray[nDrawIndex - 1].y, 5,
				// mPaint);
				surfaceHolder.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
				t++;
			}
		}

	}

	private FileUtils fileUtils = new FileUtils();
	// private File file;
	// private OutputStreamWriter osw;
	// private OutputStream os;
	private FileInputStream fis;
	// public boolean createLog(){
	// try{
	//
	// if(fileUtils.isFileExist("/ECG/ecgProcessed.txt"))
	// file = new File(fileUtils.getSDPath() + "/ECG/ecgProcessed.txt");
	// else
	// file = fileUtils.createFileInSdCard("ecgProcessed.txt", "ECG");
	// System.out.println(file.getName()+ "---------------"+file.getPath()+
	// "------file");
	// try{
	// os = new FileOutputStream(file,true);
	// osw = new OutputStreamWriter(os,"UTF-8");
	// }catch(FileNotFoundException e){
	// e.printStackTrace();
	// }
	// }catch(Exception e){
	// e.printStackTrace();
	// }
	// return true;
	// }
	//

	private ArrayList<PointF> inBuf2 = new ArrayList<PointF>();
	private boolean recordExist = false;
	private double[] inputfy = new double[101];// 处理原始数据时用到，滤波器记忆
	private final int MAXLENGTH = 4096;
	private final int MAXHEARTBEAT = MAXLENGTH * 3 / 250;
	private double[] gg = new double[MAXLENGTH];

	private double[] QRS_data = new double[MAXLENGTH * 3];
	// private double[] QRS_data_temp = new double[MAXLENGTH];
	private int[] R_location = new int[MAXHEARTBEAT];
	private int[] TStart = new int[MAXHEARTBEAT];
	private int[] TEnd = new int[MAXHEARTBEAT];
	private int[] QStart = new int[MAXHEARTBEAT];
	private int[] QEnd = new int[MAXHEARTBEAT];
	private int[] PStart = new int[MAXHEARTBEAT];
	private int[] PEnd = new int[MAXHEARTBEAT];
	private int QRS_data_cnt = 0;
	private boolean QRS_data_flag = false;
	private int R_location_cnt = 0;
	private boolean R_flag = false;
	private int[] RCount = new int[10];
	private final int QRS_NUM = 5;
	private int[] qrsCnt = new int[QRS_NUM];
	private int[] qrsFirst = new int[QRS_NUM];
	private int[] qrsLast = new int[QRS_NUM];
	private int qrsRdyCnt = 0;

	// 读文件原为private 改为public
	// 现在程序为读已有的波形
	public void readRecord(String fileName) {
		// inBuf2.clear();
		ArrayList<String> strs = fileUtils.readFile(fileName);

		if (strs != null) {
			try {
				inBuf2.clear();

				for (String str : strs) {
					Pattern p = Pattern.compile("(.*?):    ");
					Matcher m = p.matcher(str);
					while (m.find()) {
						float x = Float.parseFloat(m.group(1));
						float y = Float.parseFloat(str.substring(m.group(1).length() + ":       ".length()));
//						System.out.println("x = " + x);
//						System.out.println("y = " + y);
						// ///////////////////////////////////////用滤波器处理文件数据时用的下面一段代码
						// for(int n=0;n<100;n++)
						// inputfy[n]= inputfy[n+1];
						// inputfy[100] = y;
						//
						// y = 0;
						//
						// for (int n = 0;n<101;n++)
						// y += Filter.filterh[100-n]*inputfy[n];
						// for(int n = 0;n<65;n++)
						// y += Filter.filterh2[64-n]*inputfy[36+n];
						// try{
						// else{
						// osw.write(x + ":       " + y + "\r\n");
						// osw.flush();
						// }
						// }catch(Exception e){
						// e.printStackTrace();
						// }
						// y= 5*y;
						// y = filterFIR(y);
						// 下面我改的

						// QRS_data[wrIdx] = y;// 初始定义长度为4096*3
						// wrIdx++;
						// if (wrIdx >= QRS_data.length) {
						// wrIdx = 0;
						// wr_wrap = true;
						// }

						PointF point = new PointF(x, y);
						inBuf2.add(point);
//						if (QRS_data_cnt < MAXLENGTH) {
//							QRS_data[QRS_data_cnt] = y;
//							QRS_data_cnt++;
//						} else {
//						}

					}

				}

//				// 读取已有波形的心率
//				QRSDetNative(QRS_data, rdIdx, R_location, TStart, TEnd, QStart, QEnd, PStart, PEnd, RCount);
//				System.out.println("-------data9---" + RCount[0]);
//				System.out.println("-------Rate---" + (RCount[0] - 1) * 250 * 60 / (RCount[2] - RCount[1]));
//				// System.out.println("-------" + QRS_data_cnt);
				// System.out.println("-------RCount[0]" + RCount[0]);
				// System.out.println("-------RCount[2]" + RCount[2] + "  " +
				// RCount[1]);
				// if (RCount[2] != RCount[1])
				// heartRate.setText((RCount[0] - 1) * 250 * 60 / (RCount[2] -
				// RCount[1]) + "");
				// rdIdx += 2000;
				// while (rdIdx >= QRS_data.length) {
				// rdIdx -= QRS_data.length;
				// wr_wrap = false;
				// }
				float x = 0;

				int m = 0;
//				for (int i = 0; i < QRS_data_cnt; i++) {
//					for (int j = 0; j < MAXHEARTBEAT; j++) {
//						if (R_location[j] == i && i != 0) {
//							x = (float) 0.111;
//							R_flag = true;
//							break;
//						}
						// System.out.println("llllll"+m++);
						// else if(TStart[j]==i&&i!=0){
						// x=(float) 0.222;
						// break;
						// }else if(TEnd[j]==i&&i!=0){
						// x=(float) 0.333;
						// break;
						// }
						// else if(QStart[j]==i&&i!=0){
						// x=(float) 0.444;
						// break;
						// }
						// else if(QEnd[j]==i&&i!=0){
						// x=(float) 0.555;
						// break;
						// }
						// else if(PStart[j]==i&&i!=0){
						// x=(float) 0.666;
						// break;
						// }else if(PEnd[j]==i&&i!=0){
						// x=(float) 0.777;
						// break;
						// }

//					}
					// System.out.println("iiiiii");
					// PointF point = new PointF(x,(float) QRS_data[i]);
					// inBuf2.add(point);
					// x = 0.0f;

//				}
				for (int j = 0; j < MAXHEARTBEAT; j++)
					R_location[j] = 0;
				dataFromWifi = false;
				dataFromRecord = true;
//				System.out.println("dataFromRecord" + dataFromRecord);
				// clearBuf();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 读取dataFromBluetooth
	public void readRecord2() {
		// inBuf2.clear();
		ArrayList<String> strs = fileUtils.readECGRecord();
		if (strs != null) {
			try {
				inBuf2.clear();
				for (String str : strs) {
					Pattern p = Pattern.compile("(.*?):    ");
					Matcher m = p.matcher(str);
					while (m.find()) {
						float x = Float.parseFloat(m.group(1));
						float y = Float.parseFloat(str.substring(m.group(1).length() + ":       ".length()));
						PointF point = new PointF(x, y);
						inBuf2.add(point);
					}
				}
				dataFromWifi = false;
				dataFromRecord = true;
				// clearBuf();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class ReadFileThread extends Thread {

		private String filePath;

		public ReadFileThread(String filePath) {
			super();
			this.filePath = filePath;
		
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (true) {
				if (readBoolean) {
					System.out.println("-----" + "读文件线程" + getName());
					// readRecord2();//读取的是ecg1,2的文件存储的是蓝牙输入的
					readRecord(filePath);// 读取的是ecgprocesed.txt的文件已有的波形
					readBoolean = false;
					handler.sendEmptyMessage(0);
				}
			}
		}

	}

	@SuppressWarnings("deprecation")
	private void lowBatteryNotification() {
		Notification lowBatteryNotification = new Notification();
		lowBatteryNotification.icon = R.drawable.ic_launcher;
		lowBatteryNotification.tickerText = "低电量提示！";
		lowBatteryNotification.defaults = Notification.DEFAULT_SOUND;
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, Notification2Activity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		lowBatteryNotification.setLatestEventInfo(this, "心电采集设备提示：", "低电量提示", pendingIntent);
		notificationManager.notify(0x1123, lowBatteryNotification);
	}

	private void clearBuf() {
		for (int i = 0; i <= QRS_data_cnt; i++) {
			QRS_data[i] = 0;
		}
		for (int i = 0; i <= R_location_cnt; i++) {
			R_location[i] = 0;
		}
		QRS_data_cnt = 0;
		R_location_cnt = 0;

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}