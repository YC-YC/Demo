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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;




/**
 * Parent class for settings fragments that contain a list of Bluetooth devices.
 * 
 * @see BluetoothSettings
 * @see DevicePickerFragment
 */
public class BluetoothSettingFragment extends Fragment implements
		View.OnClickListener {


	private static final String KEY_BT_DEVICE_LIST = "bt_device_list";
	private static final String KEY_BT_SCAN = "bt_scan";

	BluetoothDevice mSelectedDevice;
	static TextView mDevice_name;
	static TextView mPin_num;

	private PreferenceGroup mDeviceListGroup;
	
	private static final int DEVICE_NAME_DIALOG = 0;
	private static final int PIN_PASSWORD_DIALOG = 1;
	private static final int AUTO_CONNECT_DIALOG = 2;
	
	private static int mAutoConnectSelect = 0; //0:Auto Connect 1:Do not
	
	public static String TAG = BluetoothSettingFragment.class.getName();  
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View parent = inflater.inflate(R.layout.bt_setting_card, container,
				false);
		initializeDeviceList(parent);
		return parent;
	}

	private void initializeDeviceList(View parent) {
		parent.findViewById(R.id.device_name_tx).setOnClickListener(this);
		parent.findViewById(R.id.pin_password_tx).setOnClickListener(this);
		parent.findViewById(R.id.auto_connect).setOnClickListener(this);
		mDevice_name = (TextView)parent.findViewById(R.id.device_name);
		mPin_num = (TextView)parent.findViewById(R.id.pin_code);
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
		removeAllDevices();
	}

	@Override
	public void onClick(View v) {
		Log.d(this, "onClick");
		switch(v.getId()){
		case R.id.device_name_tx:
			 DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
					 DEVICE_NAME_DIALOG,R.string.device_name);
	         dialogFragment.show(getFragmentManager(), "voicemail_not_ready");
			break;
		case R.id.pin_password_tx:
			 DialogFragment ppDialogFragment = ErrorDialogFragment.newInstance(
					 PIN_PASSWORD_DIALOG,R.string.pin_password);
			 ppDialogFragment.show(getFragmentManager(), "voicemail_not_ready");
	         break;
		case R.id.auto_connect:
			 DialogFragment acDialogFragment = ErrorDialogFragment.newInstance(
					 AUTO_CONNECT_DIALOG,R.string.auto_connect,R.string.auto_connect);
			 acDialogFragment.show(getFragmentManager(), "voicemail_not_ready");
	         break;
		}
		
	}

	void removeAllDevices() {
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
	
	
	
	 public static class ErrorDialogFragment extends DialogFragment {
	        private int mTitleResId;
	        private int mMessageResId;
	        private int mDialogId;
	        private static final String ARG_TITLE_RES_ID = "argTitleResId";
	        private static final String ARG_MESSAGE_RES_ID = "argMessageResId";
	        private static final String ARG_DIALOG_ID = "dialogid";

	        public static ErrorDialogFragment newInstance(int dialogid,int messageResId) {
	            return newInstance(dialogid,0, messageResId);
	        }

	        public static ErrorDialogFragment newInstance(int dialogid,int titleResId, int messageResId) {
	            final ErrorDialogFragment fragment = new ErrorDialogFragment();
	            final Bundle args = new Bundle();
	            args.putInt(ARG_TITLE_RES_ID, titleResId);
	            args.putInt(ARG_MESSAGE_RES_ID, messageResId);
	            args.putInt(ARG_DIALOG_ID, dialogid);
	            fragment.setArguments(args);
	            return fragment;
	        }

	        @Override
	        public void onCreate(Bundle savedInstanceState) {
	            super.onCreate(savedInstanceState);
	            mTitleResId = getArguments().getInt(ARG_TITLE_RES_ID);
	            mMessageResId = getArguments().getInt(ARG_MESSAGE_RES_ID);
	            mDialogId = getArguments().getInt(ARG_DIALOG_ID);
	        }

	        @Override
	        public Dialog onCreateDialog(Bundle savedInstanceState) {
	        	System.out.println(getTag());
	        	 LayoutInflater inflater = LayoutInflater.from(getActivity());
	        	 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        	 switch( mDialogId){
	        	 case DEVICE_NAME_DIALOG :
	        		 	//get local name
	        		 	try {
	        				MainActivity.service.GOCSDK_getLocalName();
	        			} catch (RemoteException e) {
	        				// TODO Auto-generated catch block
	        				e.printStackTrace();
	        			}
	        		 
	        		 View v = inflater.inflate(R.layout.alert_dialog_text_entry, null, false);
		              final View tv = v.findViewById(R.id.devicename_edit);
		             /* ((EditText)tv).setText("Dialog #" + mNum + ": using style "
		                      + getNameForNum(mNum));*/
		              ((EditText)tv).setText(MainActivity.mLocalName);
		              
		            if (mTitleResId != 0) {
		                builder.setTitle(mTitleResId);
		            }
		            if (mMessageResId != 0) {
		                builder.setMessage(mMessageResId);
		            }
		            builder.setView(v);
		            builder.setPositiveButton(android.R.string.ok,
		                    new DialogInterface.OnClickListener() {
		                            @Override
		                            public void onClick(DialogInterface dialog, int which) {
		                            	String edit = ((EditText)tv).getText().toString();
		                            	if(edit.equals(MainActivity.mLocalName) == false){
		                            		try {
		                            			MainActivity.service.GOCSDK_setLocalName(edit);
		                            			MainActivity.mLocalName = edit;
		                            			mDevice_name.setText(edit);
		            	        			} catch (RemoteException e) {
		            	        				// TODO Auto-generated catch block
		            	        				e.printStackTrace();
		            	        			}
		                            	}
		                                dismiss();
		                            }
		                    });
		            return builder.create();
	        	 case PIN_PASSWORD_DIALOG:
	        		 	try {
	        				MainActivity.service.GOCSDK_getPinCode();
	        			} catch (RemoteException e) {
	        				// TODO Auto-generated catch block
	        				e.printStackTrace();
	        			}
	        		 
	        		 View passwordv = inflater.inflate(R.layout.alert_dialog_text_entry, null, false);
		              final View passwordtv = passwordv.findViewById(R.id.devicename_edit);
		             /* ((EditText)tv).setText("Dialog #" + mNum + ": using style "
		                      + getNameForNum(mNum));*/
		           
		              ((EditText)passwordtv).setText(MainActivity.mPinCode);
		            if (mTitleResId != 0) {
		                builder.setTitle(mTitleResId);
		            }
		            if (mMessageResId != 0) {
		                builder.setMessage(mMessageResId);
		            }
		            builder.setView(passwordv);
		            builder.setPositiveButton(android.R.string.ok,
		                    new DialogInterface.OnClickListener() {
		                            @Override
		                            public void onClick(DialogInterface dialog, int which) {
		                            	String edit = ((EditText)passwordtv).getText().toString();
		                            	if(edit.equals(MainActivity.mPinCode) == false){
		                            		try {
		                            			MainActivity.service.GOCSDK_setPinCode(edit);
		                            			MainActivity.mPinCode = edit;
		                            			mPin_num.setText(edit);
		            	        			} catch (RemoteException e) {
		            	        				// TODO Auto-generated catch block
		            	        				e.printStackTrace();
		            	        			}
		                            	}
		                                dismiss();
		                            }
		                    });
		            return builder.create();
	        	 case AUTO_CONNECT_DIALOG:
		            if (mTitleResId != 0) {
		                builder.setTitle(mTitleResId);
		            }
		            if (mMessageResId != 0) {
		                //builder.setMessage(mMessageResId);
		            }
		            builder .setSingleChoiceItems(R.array.select_dialog_items, MainActivity.mAutoConnect, new DialogInterface.OnClickListener() {
	                    @Override
						public void onClick(DialogInterface dialog, int whichButton) {
	                    	
	                    	mAutoConnectSelect = whichButton;
	                        /* User clicked on a radio button do some stuff */
	                    }
	                })
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    @Override
						public void onClick(DialogInterface dialog, int whichButton) {
	                    	
	                    	/* User clicked Yes so do some stuff */
	                    	if(mAutoConnectSelect == 0){
	                    		MainActivity.mAutoConnect = 0;
	                    	}else if(mAutoConnectSelect == 1){
	                    		MainActivity.mAutoConnect = 1;
	                    	}
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    @Override
						public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked No so do some stuff */
	                    }
	                });
		            return builder.create();
	        	 }
	        	 return null;
	        }
	    }
}
