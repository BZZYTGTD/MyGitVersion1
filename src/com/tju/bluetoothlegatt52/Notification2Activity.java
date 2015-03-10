package com.tju.bluetoothlegatt52;

import com.tju.bluetoothlegatt52.R;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Notification2Activity extends Activity{
	private LinearLayout layout; 
	private double voltage;
	private TextView voltageTextView;
	@Override 
	protected void onCreate(Bundle savedInstanceState) { 
	super.onCreate(savedInstanceState); 
	NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);  
    // 取消的只是当前Context的Notification  
    mNotificationManager.cancel(0x1123);  
    final Intent intent = getIntent();
	setContentView(R.layout.low_battery_warn); 
	layout=(LinearLayout)findViewById(R.id.exit_layout); 
	layout.setOnClickListener(new OnClickListener() { 
	@Override 
	public void onClick(View v) { 
	// TODO Auto-generated method stub 
	Toast.makeText(getApplicationContext(), "提示：点击窗口外部关闭窗口！", 
	Toast.LENGTH_SHORT).show(); 
	} 
	}); 
	voltageTextView = (TextView)findViewById(R.id.voltage_textview);
	String volStr = String.format("%5.5s", voltage);
	voltageTextView.setText("心电设备当前电量为："+volStr+"V");
	voltageTextView.setGravity(Gravity.CENTER_HORIZONTAL);
	

	} 
	@Override 
	public boolean onTouchEvent(MotionEvent event){ 
	finish(); 
	return true; 
	} 
//	public void exitbutton1(View v) { 
//		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);  
//        // 取消的只是当前Context的Notification  
//        mNotificationManager.cancel(2);  
//	this.finish(); 
//	} 
	public void exitbutton0(View v) { 
		
	finish(); 
//	MainWeixin.instance.finish();//关闭Main 这个Activity 
	} 
}
