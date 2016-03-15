package com.goodocom.rk;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.goodocom.gocsdk.IGocsdkService;
import com.goodocom.rk.GocsdkCallback.BtDevices;
import com.goodocom.rk.PhoneBookFragment.phoneBook;


public class MainActivity extends Activity implements  View.OnClickListener,
    View.OnKeyListener,BluetoothService{
	
	
	  // Keys used with onSaveInstanceState().
    private static final String LAST_NUMBER = "lastNumber";

	public static IGocsdkService service = null;
	private GocsdkCallback callback;
	private static boolean DBG =true;
	
	//message of handle
	public static final int MSG_HF_DISCONNECTED = 0; 
	public static final int MSG_HF_CONNECTED = 1;
	public static final int MSG_REMOTE_ADDRESS = 2;
	public static final int MSG_DEVICES = 3; 
	public static final int MSG_COMING = 4; 
	public static final int MSG_OUTGONG = 5; 
	public static final int MSG_TALKING = 6; 
	public static final int MSG_HANGUP = 7; 
	public static final int MSG_PAIRLIST = 8;
	public static final int MSG_REMOTE_NAME = 9;
	public static final int MSG_DEVICENAME = 11;
	public static final int MSG_DEVICEPINCODE =12;
	public static final int MSG_MUSIC_VOLUME_DOWN = 13;
	public static final int MSG_MUSIC_VOLUME_UP =14;
	public static final int MSG_MUSIC_PLAY = 15;
	public static final int MSG_MUSIC_STOP = 16;
	public static final int MSG_UPDATE_PHONEBOOK = 17;
	public static final int MSG_UPDATE_PHONEBOOK_DONE = 18;
	public static final int MSG_SET_MICPHONE_ON = 19;
	public static final int MSG_SET_MICPHONE_OFF = 20;
	public static final int MSG_SET_SPEAERPHONE_ON = 21;
	public static final int MSG_SET_SPEAERPHONE_OFF = 22;
	public static final int MSG_DIAL_DIALOG =23;
	public static final int MSG_UPDATE_DEVICE_LIST =24;
	
	//Fragment show id
	public static final int FRAGMENT_ANSWER = 0;
	public static final int FRAGMENT_BLUETOOTHSETTING = 1;
	public static final int FRAGMENT_DEVICELIST = 2;
	public static final int FRAGMENT_DIALPAD = 3;
	public static final int FRAGMENT_MUSICPLAY = 4;
	public static final int FRAGMENT_PHONEBOOK = 5;
	public static final int FRAGMENT_CALLLOG = 6;
	public static final int FRAGMENT_TALKING = 7;
	
	//the latest call info use to show call log
	public static String mLatestPhoneName = null;
	public static String mLatestPhoneNum = null;
	public static int mLatestCallType = 0;
	
	public static String mCurDevName = null;
	public static String mCurDevAddr = null;
	public static String mLocalName = null;
	public static String mPinCode = null;
	public static String mComingPhoneNum = null; //Coming Fragment use to present num
	public static int mMusicPlaying = 0;   //1:Play 0:NotPlay
	public static int mAutoConnect = 0;    //0:auto connect last device  1: do not
	public static int mPhoneBookDbInsert=0;  //0:Do not insert from Phonebook DB   1:Already insert from DB
	
	
	private String mLastNumber; // last number we tried to dial. Used to restore error dialog.
	
	SQLiteDatabase mDbDataBase;         //DataBase
	static ProgressDialog PhoneBookdialog;
	
	private DialpadFragment mDialpadFragment;	
	private DeviceListFragment mDeviceListFragment;
	private BluetoothSettingFragment mBluetoothSettingFragment;
	private AnswerFragment mAnswerFragment;
	private MusicPlayFragment mMusicPlayFragment;
	private PhoneBookFragment mPhoneBookFragment;
	private CallFragment mCallFragment;
	private CallLogFragment mCallLogFragment;
	
	public static final String TAG_REMOTE_ADDRESS = "remote_address";
	
	public final String TAG = MainActivity.class.getName();   
	
	public final String EXTRA_MESSAGE = "com.gdoodocom.demo.MESSAGE"; 
	
	private List<Map<String, String>> devices = new ArrayList<Map<String, String>>();
	private List<Map<String, String>> pairlists = new ArrayList<Map<String, String>>();
	
	//private SimpleAdapter simpleAdapter;
	private long mDownTime;
    private static final int BAD_EMERGENCY_NUMBER_DIALOG = 0;

    AudioManager mAudioManager;
    
	public static IGocsdkService getService(){
		return service;
	}
	
	// close activity when screen turns off
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
               // finish();
            }
        }
    };

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main_dialer);
		/*this.bindService(new Intent("com.goodocom.gocsdk.service.GocsdkService"),
				connection, Context.BIND_AUTO_CREATE);*/
		//  mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
		
		// Allow this activity to be displayed in front of the keyguard / lockscreen.
	    WindowManager.LayoutParams lp = getWindow().getAttributes();
	    lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

	        // When no proximity sensor is available, use a shorter timeout.
	        // TODO: Do we enable this for non proximity devices any more?
	        // lp.userActivityTimeout = USER_ACTIVITY_TIMEOUT_WHEN_NO_PROX_SENSOR;
	    getWindow().setAttributes(lp);
	    
		if (GocsdkCallback.hfpStatus >= MSG_HF_CONNECTED) {
		 //   Intent intent = new Intent(MainActivity.this, BlueTootSeviceActivity.class);
		 //   startActivity(intent);
		}
		initializeInCall();
        if (icicle != null) {
            super.onRestoreInstanceState(icicle);
        }
        // Extract phone number from intent
        Uri data = getIntent().getData();
        if (data != null && (Constants.SCHEME_TEL.equals(data.getScheme()))) {
            String number = PhoneNumberUtils.getNumberFromIntent(getIntent(), this);
            if (number != null) {
                mDialpadFragment.setText(number);
            }
        }
        
        //when screen off,finish activity
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mBroadcastReceiver, intentFilter);
	
		callback = new GocsdkCallback();
		hand = handler;
		bindService(new Intent("com.goodocom.gocsdk.service.GocsdkService"),
				connection, Context.BIND_AUTO_CREATE);
		
		//音量控制,初始化定义    
	  	mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);    
	  	//最大音量    
	  	int MusicMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);    
	  	//当前音量    
	  	int MusicCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
	}
	
	 private void initializeInCall() {
		 if (mDeviceListFragment == null) {
			 mDeviceListFragment = (DeviceListFragment) getFragmentManager()
	                    .findFragmentById(R.id.devicelistFragment);
			 mDeviceListFragment.getView().setVisibility(View.GONE);
	        }
		 
		 if ( mBluetoothSettingFragment == null) {
			  mBluetoothSettingFragment = (BluetoothSettingFragment) getFragmentManager()
	                    .findFragmentById(R.id.bluetoothsettingFragment);
			 mDeviceListFragment.getView().setVisibility(View.GONE);
	        }
		
		 if ( mAnswerFragment == null) {
			 mAnswerFragment = (AnswerFragment) getFragmentManager()
	                    .findFragmentById(R.id.answerFragment);
			 mAnswerFragment.getView().setVisibility(View.GONE);
	        }
		 
		 if ( mMusicPlayFragment == null) {
			 mMusicPlayFragment = (MusicPlayFragment) getFragmentManager()
	                    .findFragmentById(R.id.musicPlayFragment);
			 mMusicPlayFragment.getView().setVisibility(View.GONE);
	     }
		 
		 if ( mPhoneBookFragment == null) {
			 mPhoneBookFragment = (PhoneBookFragment) getFragmentManager()
	                    .findFragmentById(R.id.phoneBookFragment);
			 mPhoneBookFragment.getView().setVisibility(View.GONE);
	     }
		 
		if (mDialpadFragment == null) {
		    mDialpadFragment = (DialpadFragment) getFragmentManager()
		            .findFragmentById(R.id.dialpadFragment);
		    mDialpadFragment.getView().setVisibility(View.VISIBLE);
		    mDialpadFragment.setBluetoothService(this);
		}
		
		if ( mCallFragment == null) {
			mCallFragment = (CallFragment) getFragmentManager()
	                    .findFragmentById(R.id.callFragment);
			mCallFragment.getView().setVisibility(View.GONE);
	     }
		
		if ( mCallLogFragment == null) {
			mCallLogFragment = (CallLogFragment) getFragmentManager()
	                    .findFragmentById(R.id.callLogFragment);
			mCallLogFragment.getView().setVisibility(View.GONE);
	     }
		

        findViewById(R.id.bt_home).setOnClickListener(this);
        findViewById(R.id.bt_back).setOnTouchListener(mHomeKeyTouchListner);
        findViewById(R.id.bt_dialer).setOnClickListener(this);
        findViewById(R.id.bt_call).setOnClickListener(this);
        findViewById(R.id.bt_phone_contact).setOnClickListener(this);
        findViewById(R.id.bt_phone_nfc).setOnClickListener(this);
        findViewById(R.id.bt_phone_ring).setOnClickListener(this);
        findViewById(R.id.bt_phone_settings).setOnClickListener(this);
	}
	 @Override
	protected void onPause() {
		super.onPause();
		mDialpadFragment.onDialerKeyUp(null);
	}
	 @Override
	protected void onResume() {
	        super.onResume();
	      //  mDialpadFragment.getView().setVisibility(View.VISIBLE);
	    }
	 @Override
    protected void onDestroy() {
		super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        try {
			service.GOCSDK_unregisterCallback(callback);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
        unbindService(connection);
    }
	 
    @Override
    protected void onRestoreInstanceState(Bundle icicle) {
        mLastNumber = icicle.getString(LAST_NUMBER);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LAST_NUMBER, mLastNumber);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // This can't be done in onCreate(), since the auto-restoring of the digits
        // will play DTMF tones for all the old digits if it is when onRestoreSavedInstanceState()
        // is called. This method will be called every time the activity is created, and
        // will always happen after onRestoreSavedInstanceState().
    }
	    /**
	     * handle key events
	     */
	    @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	        switch (keyCode) {
	            // Happen when there's a "Call" hard button.
	            case KeyEvent.KEYCODE_CALL: {
	                if (mDialpadFragment.isEmpty()) {
	                    // if we are adding a call from the InCallScreen and the phone
	                    // number entered is empty, we just close the dialer to expose
	                    // the InCallScreen under it.
	                    finish();
	                } else {
	                    // otherwise, we place the call.
	                    placeCall();
	                }
	                return true;
	            }
	        }
	        if (event.getRepeatCount() == 0 && handleDialerKeyDown(keyCode, event)) {
	            return true;
	        }
	        return super.onKeyDown(keyCode, event);
	    }

	    @Override
	    public boolean onKeyUp(int keyCode, KeyEvent event) {
	        // push input to the dialer.
	        if ((mDialpadFragment.isVisible()) && (mDialpadFragment.onDialerKeyUp(event))){
	            return true;
	        } else if (keyCode == KeyEvent.KEYCODE_CALL) {
	            // Always consume CALL to be sure the PhoneWindow won't do anything with it
	            return true;
	        }
	        return super.onKeyUp(keyCode, event);
	    }

	    @Override
	    public boolean onKey(View view, int keyCode, KeyEvent event) {
	        switch (view.getId()) {
	            case R.id.digits:
	                // Happen when "Done" button of the IME is pressed. This can happen when this
	                // Activity is forced into landscape mode due to a desk dock.
	                if (keyCode == KeyEvent.KEYCODE_ENTER
	                        && event.getAction() == KeyEvent.ACTION_UP) {
	                    placeCall();
	                    return true;
	                }
	                break;
	        }
	        return false;
	    }
	    
	    @Override
	    public void onBackPressed() {
	        // BACK is also used to exit out of any "special modes" of the
	        // in-call UI:

	        if (mDialpadFragment.isVisible()) {
	            return;
	        } /*else if (mConferenceManagerFragment.isVisible()) {
	           / mConferenceManagerFragment.setVisible(false);
	            return;
	        }
*/
	        // Nothing special to do.  Fall back to the default behavior.
	        super.onBackPressed();
	    }
	    
	    private boolean handleDialerKeyDown(int keyCode, KeyEvent event) {
	        //Log.d(this, "handleDialerKeyDown: keyCode " + keyCode + ", event " + event + "...");

	        // As soon as the user starts typing valid dialable keys on the
	        // keyboard (presumably to type DTMF tones) we start passing the
	        // key events to the DTMFDialer's onDialerKeyDown.
	        if (mDialpadFragment.isVisible()) {
	            return mDialpadFragment.onDialerKeyDown(event);

	            // TODO: If the dialpad isn't currently visible, maybe
	            // consider automatically bringing it up right now?
	            // (Just to make sure the user sees the digits widget...)
	            // But this probably isn't too critical since it's awkward to
	            // use the hard keyboard while in-call in the first place,
	            // especially now that the in-call UI is portrait-only...
	        }

	        return false;
	    }
	    
	    //Fragment choose display  
	    public void displayFragment(int whatFragment, boolean show){
	    	switch(whatFragment){
	    		case FRAGMENT_ANSWER:
	    			displayAnswer(show);
	    			break;
	    		case FRAGMENT_BLUETOOTHSETTING:
	    			displayBluetoothSetting(show);
	    			break;
	    		case FRAGMENT_DEVICELIST:
	    			displayDeviceList(show);
	    			break;
	    		case FRAGMENT_DIALPAD:
	    			displayDialpad(show);
	    			break;
	    		case FRAGMENT_MUSICPLAY:
	    			displayMusicPlay(show);
	    			break;
	    		case FRAGMENT_PHONEBOOK:
	    			displayPhoneBook(show);
	    			break;
	    		case FRAGMENT_CALLLOG:
	    			displayCallLog(show);
	    			break;
	    		case FRAGMENT_TALKING:
	    			displayCall(show);
	    			break;
	    	}
	    }
	    
	    public void displayAnswer(boolean show){
	    	int display;
	    	if(show){
		    	display = View.VISIBLE;
	    	}else{
	    		display = View.INVISIBLE;
	    	}
	    	mBluetoothSettingFragment.getView().setVisibility(View.INVISIBLE);
			mDeviceListFragment.getView().setVisibility(View.INVISIBLE);
			mAnswerFragment.getView().setVisibility(display);
			mMusicPlayFragment.getView().setVisibility(View.INVISIBLE);
			mDialpadFragment.getView().setVisibility(View.INVISIBLE);
			mPhoneBookFragment.getView().setVisibility(View.INVISIBLE);
			mCallFragment.getView().setVisibility(View.INVISIBLE);
			mCallLogFragment.getView().setVisibility(View.INVISIBLE);
	    }
	    public void displayBluetoothSetting(boolean show){
	    	int display;
	    	if(show){
		    	display = View.VISIBLE;
	    	}else{
	    		display = View.INVISIBLE;
	    	}
	    	mBluetoothSettingFragment.getView().setVisibility(display);
			mDeviceListFragment.getView().setVisibility(View.INVISIBLE);
			mAnswerFragment.getView().setVisibility(View.GONE);
			mMusicPlayFragment.getView().setVisibility(View.INVISIBLE);
			mDialpadFragment.getView().setVisibility(View.INVISIBLE);
			mPhoneBookFragment.getView().setVisibility(View.INVISIBLE);
			mCallFragment.getView().setVisibility(View.INVISIBLE);
			mCallLogFragment.getView().setVisibility(View.INVISIBLE);
	    }
	    public void displayDeviceList(boolean show){
	    	int display;
	    	if(show){
		    	display = View.VISIBLE;
	    	}else{
	    		display = View.INVISIBLE;
	    	}
	    	mBluetoothSettingFragment.getView().setVisibility(View.INVISIBLE);
			mDeviceListFragment.getView().setVisibility(display);
			mAnswerFragment.getView().setVisibility(View.INVISIBLE);
			mMusicPlayFragment.getView().setVisibility(View.INVISIBLE);
			mDialpadFragment.getView().setVisibility(View.INVISIBLE);
			mPhoneBookFragment.getView().setVisibility(View.INVISIBLE);
			mCallFragment.getView().setVisibility(View.INVISIBLE);
			mCallLogFragment.getView().setVisibility(View.INVISIBLE);
	    }
	    public void displayMusicPlay(boolean show){
	    	int display;
	    	if(show){
		    	display = View.VISIBLE;
	    	}else{
	    		display = View.INVISIBLE;
	    	}
	    	mBluetoothSettingFragment.getView().setVisibility(View.INVISIBLE);
			mDeviceListFragment.getView().setVisibility(View.INVISIBLE);
			mAnswerFragment.getView().setVisibility(View.INVISIBLE);
			mMusicPlayFragment.getView().setVisibility(display);
			mDialpadFragment.getView().setVisibility(View.INVISIBLE);
			mPhoneBookFragment.getView().setVisibility(View.INVISIBLE);
			mCallFragment.getView().setVisibility(View.INVISIBLE);
			mCallLogFragment.getView().setVisibility(View.INVISIBLE);
	    }
	    public void displayDialpad(boolean show){
	    	int display;
	    	if(show){
		    	display = View.VISIBLE;
	    	}else{
	    		display = View.INVISIBLE;
	    	}
	    	mBluetoothSettingFragment.getView().setVisibility(View.INVISIBLE);
			mDeviceListFragment.getView().setVisibility(View.INVISIBLE);
			mAnswerFragment.getView().setVisibility(View.INVISIBLE);
			mMusicPlayFragment.getView().setVisibility(View.INVISIBLE);
			mDialpadFragment.getView().setVisibility(display);
			mPhoneBookFragment.getView().setVisibility(View.INVISIBLE);
			mCallFragment.getView().setVisibility(View.INVISIBLE);
			mCallLogFragment.getView().setVisibility(View.INVISIBLE);
	    }
	    public void displayPhoneBook(boolean show){
	    	int display;
	    	if(show){
		    	display = View.VISIBLE;
	    	}else{
	    		display = View.INVISIBLE;
	    	}
	    	mBluetoothSettingFragment.getView().setVisibility(View.INVISIBLE);
			mDeviceListFragment.getView().setVisibility(View.INVISIBLE);
			mAnswerFragment.getView().setVisibility(View.INVISIBLE);
			mMusicPlayFragment.getView().setVisibility(View.INVISIBLE);
			mDialpadFragment.getView().setVisibility(View.INVISIBLE);
			mPhoneBookFragment.getView().setVisibility(display);
			mCallFragment.getView().setVisibility(View.INVISIBLE);
			mCallLogFragment.getView().setVisibility(View.INVISIBLE);
	    }
	    public void displayCall(boolean show){
	    	int display;
	    	if(show){
		    	display = View.VISIBLE;
	    	}else{
	    		display = View.INVISIBLE;
	    	}
	    	mBluetoothSettingFragment.getView().setVisibility(View.INVISIBLE);
			mDeviceListFragment.getView().setVisibility(View.INVISIBLE);
			mAnswerFragment.getView().setVisibility(View.INVISIBLE);
			mMusicPlayFragment.getView().setVisibility(View.INVISIBLE);
			mDialpadFragment.getView().setVisibility(View.INVISIBLE);
			mPhoneBookFragment.getView().setVisibility(View.INVISIBLE);
			mCallFragment.getView().setVisibility(display);
			mCallLogFragment.getView().setVisibility(View.INVISIBLE);
	    }
	    public void displayCallLog(boolean show){
	    	int display;
	    	if(show){
		    	display = View.VISIBLE;
	    	}else{
	    		display = View.INVISIBLE;
	    	}
	    	mBluetoothSettingFragment.getView().setVisibility(View.INVISIBLE);
			mDeviceListFragment.getView().setVisibility(View.INVISIBLE);
			mAnswerFragment.getView().setVisibility(View.INVISIBLE);
			mMusicPlayFragment.getView().setVisibility(View.INVISIBLE);
			mDialpadFragment.getView().setVisibility(View.INVISIBLE);
			mPhoneBookFragment.getView().setVisibility(View.INVISIBLE);
			mCallFragment.getView().setVisibility(View.INVISIBLE);
			mCallLogFragment.getView().setVisibility(display);
			
	    }

	    
	   View.OnTouchListener mHomeKeyTouchListner =  new View.OnTouchListener() {
			@Override
				public boolean onTouch(View v, MotionEvent ev) {
				    int code = (v.getId() == R.id.bt_home ? KeyEvent.KEYCODE_HOME :KeyEvent.KEYCODE_BACK);
					final int action = ev.getAction();
					//Log.d(this, action+"mHomeKeyTouchListner              ()..."+code);
					switch (action) {
					case MotionEvent.ACTION_DOWN:
						// Log.d("KeyButtonView", "press");
						mDownTime = SystemClock.uptimeMillis();
						v.setPressed(true);
						sendEvent(KeyEvent.ACTION_DOWN, 0, mDownTime,code);
						break;
					case MotionEvent.ACTION_CANCEL:
						v.setPressed(false);
						sendEvent(KeyEvent.ACTION_UP,
								KeyEvent.FLAG_CANCELED, code);
						break;
					case MotionEvent.ACTION_UP:
						final boolean doIt = v.isPressed();
						v.setPressed(false);
						if (doIt) {
							sendEvent(KeyEvent.ACTION_UP, 0, code);
							v.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
						} else {
							sendEvent(KeyEvent.ACTION_UP,
									KeyEvent.FLAG_CANCELED, code);
						}
						break;

					}
					return false;
				}
		};
		
	    public boolean isDialpadVisible() {
	        return mDialpadFragment.isVisible();
	    }
	    
	    @Override
	    public void onClick(View view) {
	        switch (view.getId()) {
	        case R.id.bt_back:
	        	dismissAndGoBack();
	        	return;
	        case R.id.bt_home:
	        	dismissAndGoHome();
	        	return;
	        case R.id.bt_call:                //call log
	        	
	        	Handler handler3 = CallLogFragment.getHandler();
	    		handler3.sendEmptyMessage(CallLogFragment.MSG_READ_CALL_LOG_FROM_DB);
	        	displayFragment(FRAGMENT_CALLLOG,true);
	        	    	
	        	return;
	        case R.id.bt_dialer:
	        	displayFragment(FRAGMENT_DIALPAD,true);
	        	return;
	        case R.id.bt_phone_contact:       //PhoneBook
	        	displayFragment(FRAGMENT_PHONEBOOK,true);
	        	
               if(mPhoneBookDbInsert == 0){     //if the first time open the APP
            	   
	            	mDbDataBase = Database.getPhoneBookDb();
	            	//if not exsit then create the table 
	            	Database.createTable(mDbDataBase,Database.Sql_create_phonebook_tab);
	            	
	            	Cursor cursor = mDbDataBase.query(Database.PhoneBookTable,null,null,null,null,null,null);  
	            	if(PhoneBookFragment.contacts.isEmpty() == false){
	            		PhoneBookFragment.contacts.clear();
					}
	               //If not empty
	        	   if(cursor.moveToFirst()) {  
	        		   while(!cursor.isAfterLast()){
	        	            String PhoneName=cursor.getString(cursor.getColumnIndex("phonename")); 
	        	            String PhoneNum=cursor.getString(cursor.getColumnIndex("phonenumber")); 
	        	            
	        	            Map<String, String> phoBook = new HashMap<String, String>();
	    					phoBook.put("itemName", PhoneName);
	    					phoBook.put("itemnum", PhoneNum);
	    					PhoneBookFragment.contacts.add(phoBook);
	    					PhoneBookFragment.simpleAdapter.notifyDataSetChanged();
	        	            
	        	            cursor.moveToNext();
	        		   }  
	        	   }   
	               mPhoneBookDbInsert = 1;        //Not the first time 
               }
	        	return;
	        case R.id.bt_phone_nfc:           //BlueToothDeviceList
	        	Handler handler = DeviceListFragment.getHandler();
				if(handler != null){
					handler.sendEmptyMessage(DeviceListFragment.MSG_SCAN_DEVICE);
				}
	        	displayFragment(FRAGMENT_DEVICELIST,true);
				return;
	        case R.id.bt_phone_settings:      //setting
	        	//get device name and pin code
	    		try {
	    			MainActivity.service.GOCSDK_getLocalName();
	    			MainActivity.service.GOCSDK_getPinCode();
				
	    		} catch (RemoteException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    		displayFragment(FRAGMENT_BLUETOOTHSETTING,true);
	        	return;
	        	
	        case R.id.bt_phone_ring:         //MusicPlaying
	        	displayFragment(FRAGMENT_MUSICPLAY,true);
	        	return;
	        }
	    }

	void sendEvent(int action, int flags, int code) {
		sendEvent(action, flags, SystemClock.uptimeMillis(), code);
	}

	void sendEvent(int action, int flags, long when, int mCode) {

		final int repeatCount = (flags & KeyEvent.FLAG_LONG_PRESS) != 0 ? 1 : 0;
		final KeyEvent ev = new KeyEvent(mDownTime, when, action, mCode,
				repeatCount, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, flags
						| KeyEvent.FLAG_FROM_SYSTEM
						| KeyEvent.FLAG_VIRTUAL_HARD_KEY,
				InputDevice.SOURCE_KEYBOARD);
//		InputManager.getInstance().injectInputEvent(ev,
//				InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Instrumentation inst=new Instrumentation();
				inst.sendKeySync(ev);				
			}
		}).start();

	}

	
	public void dismissAndGoHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
	           homeIntent.addCategory(Intent.CATEGORY_HOME);
	            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
	              | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	           startActivity(homeIntent);
	}
	    	
    @SuppressLint("NewApi")
	public void dismissAndGoBack() {
            final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 
             final List<ActivityManager.RecentTaskInfo> recentTasks =
                    am.getRecentTasks(2,
                             ActivityManager.RECENT_WITH_EXCLUDED |
                            ActivityManager.RECENT_IGNORE_UNAVAILABLE);
             if (recentTasks.size() > 1 
                    ) {
            	 am.moveTaskToFront(recentTasks.get(0).id, ActivityManager.MOVE_TASK_WITH_HOME,
            			                     null);
                 return;
            }
            finish();
    }  
	 
	public static Handler hand = null;
	public static Handler getHandler(){
		return hand;
	}
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			
			case MSG_HF_CONNECTED:
				CharSequence str=getString(R.string.bt_connect_info);
				Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
				break;
			case MSG_HF_DISCONNECTED:
				setTitle(getString(R.string.no_device_connected));
				
				CharSequence str1=getString(R.string.bt_disconnect_info);
				Toast.makeText(getApplicationContext(), str1, Toast.LENGTH_SHORT).show();				
				
				pairlists.clear();
				if(false == isConnected())return;
				try {
					MainActivity.service.GOCSDK_getPairList();
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				
				mCurDevName=null;        //disconnect clear device info
				mCurDevAddr=null;
			    break;
			case MSG_REMOTE_NAME:				
				String Name = (String) msg.obj;
				mCurDevName = Name;
				setTitle(Name);
				try {
					MainActivity.service.GOCSDK_getPairList();
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				break;
				
			case MSG_REMOTE_ADDRESS:
				String Addr = (String)msg.obj;
				mCurDevAddr = Addr;
				break;
				
			case MSG_COMING:
				String phonenum = (String)msg.obj;
				String phonename= "";
				mComingPhoneNum = phonenum;
				
				mDbDataBase=Database.getPhoneBookDb();
				try{
					phonename = Database.queryPhoneName(mDbDataBase, Database.PhoneBookTable, phonenum);
					AnswerFragment.mPhoneName.setText(phonename);
				}catch(Exception e){
					CallFragment.CallPeople.setText(R.string.phone_name_info);
				}
				mDbDataBase.close();
				
				AnswerFragment.mPhoneNum.setText(mComingPhoneNum);
				AnswerFragment.mGlowpad.startPing();
				displayFragment(FRAGMENT_ANSWER,true);
				
				mLatestPhoneName = phonename;
				mLatestPhoneNum = phonenum;
				mLatestCallType = 3;
				break;
			
			case MSG_OUTGONG:
				try {
                    Thread.currentThread();
					Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
				String num =GocsdkCallback.number;
				String peoplename = "";
				mDbDataBase=Database.getPhoneBookDb();
				try{
					peoplename = Database.queryPhoneName(mDbDataBase, Database.PhoneBookTable, num);
					CallFragment.CallPeople.setText(peoplename);
				}catch(Exception e){
					CallFragment.CallPeople.setText(R.string.phone_name_info);
				}
				                        
				CallFragment.CallPeopleNum.setText(num);
				mDbDataBase.close();
				displayFragment(FRAGMENT_TALKING,true);
				
				mLatestPhoneName = peoplename;
				mLatestPhoneNum = num;
				mLatestCallType = 2;
				
				Log.d(TAG, "MSG_OUTGONG() !!!!!!!!!!!!!!!!!!!!!:"+num);
				break;
				
			case MSG_TALKING:
				displayFragment(FRAGMENT_TALKING,true);
				Log.d(TAG, "The Phone is on Talking.");		
				
				break;
			case MSG_HANGUP:
				CharSequence str2=getString(R.string.phone_hangup_info);
				Toast.makeText(getApplicationContext(), str2, Toast.LENGTH_SHORT).show();
				displayFragment(FRAGMENT_DIALPAD,true);
			    
				break;
			case MSG_PAIRLIST:
				BtDevices btPairList = (BtDevices) msg.obj;
				Map<String, String> pairlist = new HashMap<String, String>();

				pairlist.put("itemName","δ����   " + btPairList.name);
				pairlist.put("itemAddr", btPairList.addr);
				devices.add(pairlist);
				//simpleAdapter.notifyDataSetChanged();
				break;
			case MSG_DEVICENAME:
				String name = (String)msg.obj;
				mLocalName = name;
				BluetoothSettingFragment.mDevice_name.setText(mLocalName);
				
				Log.d(TAG, "The local device name is :" + mLocalName);
				break;
			case MSG_DEVICEPINCODE:
				String pincode = (String)msg.obj;
				mPinCode = pincode;
				BluetoothSettingFragment.mPin_num.setText(mPinCode);
				Log.d(TAG, "The local device PIN CODE is :" + mPinCode);
				break;
			
			case MSG_MUSIC_VOLUME_DOWN:
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,    
                        AudioManager.FX_FOCUS_NAVIGATION_UP);  
				break;
				
			case MSG_MUSIC_VOLUME_UP:
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,    
                        AudioManager.FX_FOCUS_NAVIGATION_UP); 
				break;
				
			case MSG_MUSIC_PLAY:
				MusicPlayFragment.PlayBt.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause)); 
				mMusicPlaying = 1;
				break;
				
			case MSG_MUSIC_STOP:
				MusicPlayFragment.PlayBt.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play)); 
				mMusicPlaying = 0;
				break;
							
			case MSG_SET_MICPHONE_ON:
				mAudioManager.setMicrophoneMute(true);
				CharSequence str3=getString(R.string.mic_on_info);
				Toast.makeText(getApplicationContext(), str3, Toast.LENGTH_SHORT).show();
				break;
				
			case MSG_SET_MICPHONE_OFF:
				mAudioManager.setMicrophoneMute(false); 
				CharSequence str4=getString(R.string.mic_off_info);
				Toast.makeText(getApplicationContext(), str4, Toast.LENGTH_SHORT).show();
				break;
				
			case MSG_SET_SPEAERPHONE_ON:
				mAudioManager.setMicrophoneMute(true);
				CharSequence str5=getString(R.string.speaker_on_info);
				Toast.makeText(getApplicationContext(), str5, Toast.LENGTH_SHORT).show();
				break;
				
			case MSG_SET_SPEAERPHONE_OFF:
				mAudioManager.setMicrophoneMute(false);
				CharSequence str6 = getString(R.string.speaker_off_info);
				Toast.makeText(getApplicationContext(), str6, Toast.LENGTH_SHORT).show();
				break;
			
			case MSG_UPDATE_PHONEBOOK:
				PhoneBookdialog=ProgressDialog.show(MainActivity.this,"Updating","Please wait...",true);
				//sleep 4s to wait 
				new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							//wait update complete
							sleep(4000);
						}
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
						finally
						{
							//dismiss the dialog
							PhoneBookdialog.dismiss();
						}
					}
					
				}.start();
				break;
			
			case MSG_DIAL_DIALOG:
				
					final phoneBook phone = (phoneBook)msg.obj;
					AlertDialog.Builder builder = new Builder(MainActivity.this);
					builder.setMessage("确定要拨打吗?"+phone.name+":"+phone.num);
					builder.setTitle("提示");
					builder.setPositiveButton("确认",
							new android.content.DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub
									arg0.dismiss();
									
									try {
										MainActivity.service.GOCSDK_phoneDail(phone.num);     
										CallFragment.CallPeople.setText(phone.name);
									} catch (RemoteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
					builder.setNegativeButton("取消",
							new android.content.DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
					builder.create().show(); 
				break;
				
			case MSG_UPDATE_DEVICE_LIST:
				PhoneBookdialog=ProgressDialog.show(MainActivity.this,"Wait","Please wait...",true);
				//sleep 4s to wait 
				new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							sleep(6000);
						}
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
						finally
						{
							if(PhoneBookdialog !=null){
								PhoneBookdialog.dismiss();
								PhoneBookdialog=null;
							}
						}
					}
				}.start();
				break;
			default:
				break;
			}
		};
	};	
	 /**
     * place the call, but check to make sure it is a viable number.
     */
    private void placeCall() {
        mLastNumber = mDialpadFragment.getPhoneNumberText();
        if(mLastNumber.length() == 0) return;
        if (PhoneNumberUtils.isGlobalPhoneNumber(mLastNumber)) {
            if (DBG) Log.d(TAG, "placing call to " + mLastNumber);

            // place the call if it is a valid number
            if (mLastNumber == null || !TextUtils.isGraphic(mLastNumber)) {
                // There is no number entered.
                return;
            }
            try {
				MainActivity.service.GOCSDK_phoneDail(mLastNumber);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
            
            //TODO
        } else {
            if (DBG) Log.d(TAG, "rejecting bad requested number " + mLastNumber);

            // erase the number and throw up an alert dialog.
           // mDigits.getText().delete(0, mDigits.getText().length());
            showDialog(BAD_EMERGENCY_NUMBER_DIALOG);
        }
    }

   
	  private CharSequence createErrorMessage(String number) {
	        if (!TextUtils.isEmpty(number)) {
	            return getString(R.string.dial_emergency_error, mLastNumber);
	        } else {
	            return getText(R.string.dial_emergency_empty_error).toString();
	        }
	    }

	    @Override
	    protected Dialog onCreateDialog(int id) {
	        AlertDialog dialog = null;
	        if (id == BAD_EMERGENCY_NUMBER_DIALOG) {
	            // construct dialog
	            dialog = new AlertDialog.Builder(this)
	                    .setTitle(getText(R.string.emergency_enable_radio_dialog_title))
	                    .setMessage(createErrorMessage(mLastNumber))
	                    .setPositiveButton(R.string.ok, null)
	                    .setCancelable(true).create();

	            // blur stuff behind the dialog
	            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	        }
	        return dialog;
	    }

	    @Override
	    protected void onPrepareDialog(int id, Dialog dialog) {
	        super.onPrepareDialog(id, dialog);
	        if (id == BAD_EMERGENCY_NUMBER_DIALOG) {
	            AlertDialog alert = (AlertDialog) dialog;
	            alert.setMessage(createErrorMessage(mLastNumber));
	        }
	    }
	   
	public  boolean isConnected(){
		return service != null;
	}
	
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder serv) {
			service = IGocsdkService.Stub.asInterface(serv);
			try {
				service.GOCSDK_registerCallback(callback);
				 if (DBG) Log.d(TAG, "onServiceConnected !!!!!!!!!!!!!!!!!!!!! " );
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			 if (DBG) Log.d(TAG, "onServiceDisconnected !!!!!!!!!!!!!!!!!!!!! " );
		     service = null;
		}
	};
	
	@Override
	public void getLocalName() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "getLocalName " + mLastNumber);
	}
	
	@Override
	public void setLocalName(String name) {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "setLocalName" + mLastNumber);
	}
	
	@Override
	public void getPinCode() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "rejecting bad requested number " + mLastNumber);
	}
	
	@Override
	public void setPinCode(String pincode) {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "getPinCode" + mLastNumber);
	}
	
	@Override
	public void connectLast() {
		// TODO Auto-generated method stub
		try {
			service.GOCSDK_connectLast();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 if (DBG) Log.d(TAG, "connectLast" + mLastNumber);
	}
	
	@Override
	public void connectA2dp(String addr) {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "connectA2dp" + mLastNumber);
	}
	
	@Override
	public void connectHFP(String addr) {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "connectHFP" + mLastNumber);
	}
	
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		try {
			service.GOCSDK_disconnect();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void disconnectA2DP() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "disconnectA2DP" + mLastNumber);
	}
	
	@Override
	public void disconnectHFP() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "disconnectHFP" + mLastNumber);
	}
	
	@Override
	public void deletePair(String addr) {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "deletePair" + mLastNumber);
	}
	
	@Override
	public void startDiscovery() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "startDiscovery" + mLastNumber);
	}
	
	@Override
	public void getPairList() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "getPairList" + mLastNumber);
	}
	
	@Override
	public void stopDiscovery() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "stopDiscovery" + mLastNumber);
	}
	
	@Override
	public void phoneAnswer() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "phoneAnswer" + mLastNumber);
	}
	
	@Override
	public void phoneHangUp() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "phoneHangUp" + mLastNumber);
	}
	
	@Override
	public void phoneDail(String phonenum) {
		// TODO Auto-generated method stub
		placeCall();
	}
	
	@Override
	public void phoneTransmitDTMFCode(char code) {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "phoneTransmitDTMFCode" + mLastNumber);
	}
	
	@Override
	public void phoneTransfer() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "rejecting bad requested number " + mLastNumber);
	}
	
	@Override
	public void phoneTransferBack() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "phoneTransferBack" + mLastNumber);
	}
	
	@Override
	public void phoneBookStartUpdate() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "phoneBookStartUpdate" + mLastNumber);
	}
	
	@Override
	public void callLogstartUpdate(int type) {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "callLogstartUpdate" + mLastNumber);
	}
	
	@Override
	public void musicPlayOrPause() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "musicPlayOrPause" + mLastNumber);
	}
	
	@Override
	public void musicStop() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "musicStop");
		 
	}
	
	@Override
	public void musicPrevious() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "musicPrevious" + mLastNumber);
	}
	
	@Override
	public void musicNext() {
		// TODO Auto-generated method stub
		 if (DBG) Log.d(TAG, "musicNext" + mLastNumber);
	}
}

