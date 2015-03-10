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
 
import java.util.ArrayList;




import com.tju.bluetoothlegatt52.R;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

 
/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean mScanning;
    private Handler mHandler;
    
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ACTIVITY = 2;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
        
        getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();
 
        

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
 
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.

 
        // Checks if Bluetooth is supported on the device.
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
            menu.findItem(R.id.wifi).setVisible(true);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
            menu.findItem(R.id.wifi).setVisible(true);
        }
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
           
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case R.id.wifi:
            	final Intent intent = new Intent(this, DeviceControlActivity.class); 
            	intent.putExtra(DeviceControlActivity.EXTRAS_SERVER_ADDRESS, "192.168.1.3");
            	intent.putExtra(DeviceControlActivity.EXTRAS_SERVER_PORT, 5001);
            	startActivity(intent);
            	break;
        }
        return true;
    }
 
    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
 
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        
    }
 
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    
    }
 
 
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
 
            mScanning = true;
        } else {
            mScanning = false;
        }
        invalidateOptionsMenu();
    }
 
    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private LayoutInflater mInflator;
 
        public LeDeviceListAdapter() {
            super();
    
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }
 
      
 
       
 
       
      
      
        @Override
        public long getItemId(int i) {
            return i;
        }
 
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
 
  
           
 
            return view;
        }








		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}








		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}
    }
 
    // Device scan callback.
//    private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
// 
//        @Override
//        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mLeDeviceListAdapter.addDevice(device);
//                    mLeDeviceListAdapter.notifyDataSetChanged();
//                }
//            });
//        }
//    };
 
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}