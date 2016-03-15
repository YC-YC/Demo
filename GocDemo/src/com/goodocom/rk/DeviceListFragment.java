/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.goodocom.rk;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.goodocom.rk.GocsdkCallback.BtDevices;


/**
 * Parent class for settings fragments that contain a list of Bluetooth devices.
 * 
 * @see BluetoothSettings
 * @see DevicePickerFragment
 */
public class DeviceListFragment extends Fragment implements
		View.OnClickListener {

	private static final String TAG = "DeviceListFragment";

	
	public static final int MSG_DEVICES = 0; 
	public static final int MSG_DEVICES_DONE = 1; 
	public static final int MSG_SCAN_DEVICE = 2;
	public static final int MSG_DEVICES_DISCONT = 3;
	public static final int MSG_DEVICES_CONNECT = 4;
	
	private Button scanBt = null;
	static List<Map<String, String>> devicelist = new ArrayList<Map<String,String>>();
	private SimpleAdapter simpleAdapter;
	private int nIndex = -1; 
	ListView mListView = null;
	static TextView SearchingText = null;
	
	public int mIsScaning = 0;   //1:Scaning    0:Do not 
	
	BluetoothDevice mSelectedDevice;
	private PreferenceGroup mDeviceListGroup;
	private BluetoothService mBluetoothService;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBluetoothService = (BluetoothService)getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View parent = inflater.inflate(R.layout.bt_list_card, container,
				false);
		initializeDeviceList(parent);
		return parent;
	}

	private void initializeDeviceList(View parent) {
		parent.findViewById(R.id.list_Button).setOnClickListener(this);
		parent.findViewById(R.id.scan_Button).setOnClickListener(this);
		parent.findViewById(R.id.connect_Button).setOnClickListener(this);
		parent.findViewById(R.id.disconnect_Button).setOnClickListener(this);
		
		scanBt = (Button)parent.findViewById(R.id.scan_Button);
		mListView = (ListView)parent.findViewById(R.id.DeviceListView);
		SearchingText = (TextView)parent.findViewById(R.id.searching_device);
		SearchingText.setVisibility(View.INVISIBLE);
		
		simpleAdapter = new SimpleAdapter(getActivity(), devicelist, R.layout.btdevices, 
				new String[] {"itemName","itemAddr","connectState" },new int[] {R.id.btdevicename, R.id.btdeviceaddr, R.id.connect_state});
		mListView.setAdapter(simpleAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Map<String,String>Device =(Map<String,String>)simpleAdapter.getItem(arg2);
				String Name = Device.get("itemName");
				String Addr = Device.get("itemAddr");
				String State = Device.get("connectState");
				if(mIsScaning == 1){
					try {
						MainActivity.service.GOCSDK_stopDiscovery();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mIsScaning = 0;
				}
				
				if(State.equals(getString(R.string.device_disconnected))){
					try {
						MainActivity.service.GOCSDK_connectHFP(Addr);
						Handler handler = MainActivity.getHandler();
						if(handler != null){
							handler.sendEmptyMessage(MainActivity.MSG_UPDATE_DEVICE_LIST);
						}	
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else {
					try {
						MainActivity.service.GOCSDK_disconnect();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				// TODO Auto-generated method stub
				nIndex = mListView.getId();
				Log.d("goc", "onItemClick" + Name +Addr);
			}
		});
		hand = handler;
	}

	
	void setDeviceListGroup(PreferenceGroup preferenceGroup) {
		mDeviceListGroup = preferenceGroup;
	}

	/** Add preferences from the subclass. */

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
			case R.id.list_Button:
				break;
			case R.id.scan_Button:
				
				Handler handler = DeviceListFragment.getHandler();
				if(handler != null){
					handler.sendEmptyMessage(DeviceListFragment.MSG_SCAN_DEVICE);
				}
				break;
			case R.id.disconnect_Button:
				try {
					MainActivity.service.GOCSDK_disconnect();
					CharSequence str=getString(R.string.disconnet_info);
					Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				break;
			case R.id.connect_Button:
				try {
					MainActivity.service.GOCSDK_connectLast();
					CharSequence str=getString(R.string.auto_connet_info);
					Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
		}
	}
	

	
	public static Handler hand = null;
	public static Handler getHandler(){
		return hand;
	}
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			
			case MSG_DEVICES:
				mIsScaning = 1;
				BtDevices BtDev = (BtDevices) msg.obj;
				if(BtDev.name.equals("")){       //if name equals "",do not insert to list
					break;
				}
				if(IsSearchDeviceConnect(BtDev.name) == false){
					
					Map<String, String> BtDevMap = new HashMap<String, String>();
					BtDevMap.put("itemName", BtDev.name);
					BtDevMap.put("itemAddr", BtDev.addr);
					String str=getString(R.string.device_disconnected); 
					BtDevMap.put("connectState",str);
					devicelist.add(BtDevMap);
					simpleAdapter.notifyDataSetChanged();
				}
				break;
			case MSG_DEVICES_DONE:
				mIsScaning = 0;
				SearchingText.setVisibility(View.INVISIBLE);
				scanBt.setClickable(true);
				scanBt.setTextColor(Color.WHITE);
				break;
			
			case MSG_SCAN_DEVICE:
				if(mIsScaning == 0)	{
					devicelist.clear();
					try {
						if(IsDeviceConnect()){
							Map<String, String> BtDevMap = new HashMap<String, String>();
							BtDevMap.put("itemName", MainActivity.mCurDevName);
							BtDevMap.put("itemAddr", MainActivity.mCurDevAddr);
							String con_str=getString(R.string.device_connected); 
							BtDevMap.put("connectState",con_str);
							devicelist.add(BtDevMap);
							simpleAdapter.notifyDataSetChanged();
						}
						MainActivity.service.GOCSDK_startDiscovery();
						
						SearchingText.setVisibility(View.VISIBLE);
						scanBt.setClickable(false);
						scanBt.setTextColor(Color.BLACK);
						
						//add connected device
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mIsScaning = 1;
				}
				break;
			case MSG_DEVICES_DISCONT:
	                int itemNum = simpleAdapter.getCount();
	                for(int i=0; i<itemNum; i++)
	                {
	                        HashMap<String, String> map = (HashMap<String, String>)simpleAdapter.getItem(i);
	                        String State = map.get("connectState");
	                        if ( State.equals(getString(R.string.device_connected)))
	                        	map.put("connectState", getString(R.string.device_disconnected));
	                }
	                simpleAdapter.notifyDataSetChanged();
	                
	                if(MainActivity.PhoneBookdialog!=null){
	                	MainActivity.PhoneBookdialog.dismiss();
	                	MainActivity.PhoneBookdialog=null;
	                }
				break;
				
			case MSG_DEVICES_CONNECT:
				int itNum = simpleAdapter.getCount();
				int IsExist = 0;
                for(int i=0; i<itNum; i++)
                {
                        HashMap<String, String> map = (HashMap<String, String>)simpleAdapter.getItem(i);
                        String Name = map.get("itemName");
                        if ( Name.equals(MainActivity.mCurDevName)){
                        	map.put("connectState", getString(R.string.device_connected));
                        	IsExist = 1;
                        	break;
                        }
                }
                
                if(IsExist == 0){
                	Map<String, String> Map = new HashMap<String, String>();
                	Map.put("itemName", MainActivity.mCurDevName);
                	Map.put("itemAddr", MainActivity.mCurDevAddr);
					Map.put("connectState",getString(R.string.device_connected));
					devicelist.add(Map);
					simpleAdapter.notifyDataSetChanged();
                }
                simpleAdapter.notifyDataSetChanged();
                
                if(MainActivity.PhoneBookdialog!=null){
                	MainActivity.PhoneBookdialog.dismiss();
                	MainActivity.PhoneBookdialog=null;
                }
				break;
			default:
				break;
			}
		};
	};
	
	
	boolean IsDeviceConnect(){
		if(GocsdkCallback.hfpStatus != 0){
			return true;
		}
		return false;
	}
	
	boolean IsSearchDeviceConnect(String Name){
		if(MainActivity.mCurDevName != null){
			if(MainActivity.mCurDevName.equals(Name)){
				return true;
			}
			return false;
		}
		return false;
	}
	
	void removeAllDevices() {
		devicelist.clear();
	}

	void addCachedDevices() {
		/*
		 * Collection<CachedBluetoothDevice> cachedDevices =
		 * mLocalManager.getCachedDeviceManager().getCachedDevicesCopy(); for
		 * (CachedBluetoothDevice cachedDevice : cachedDevices) {
		 */
		onDeviceAdded();
		// }
	}

	void onDevicePreferenceClick() {
		
	}

	public void onDeviceAdded() {

	}

	public void onDeviceDeleted() {

	}

	public void onScanningStateChanged(boolean started) {
		updateProgressUi(started);
	}

	private void updateProgressUi(boolean start) {

	}

	public void onBluetoothStateChanged(int bluetoothState) {
		if (bluetoothState == BluetoothAdapter.STATE_OFF) {
			updateProgressUi(false);
		}
	}
}
