package com.goodocom.rk;

import com.goodocom.rk.R.string;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

public class CallFragment extends Fragment implements
	View.OnClickListener{

	private static final String TAG = "CallFragment";
	
	public Chronometer mChronometer = null;
	public TextView CallStatus = null;
	public static TextView CallPeople = null;
	public static TextView CallPeopleNum = null;
	public static final int MSG_OUTGONG = 1; 
	public static final int MSG_TALKING = 2; 
	public static final int MSG_HANGUP = 3; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View parent = inflater.inflate(R.layout.call_fragment, container,
				false);
		initializeDeviceList(parent);
		return parent;
	}

	private void initializeDeviceList(View parent) {
		parent.findViewById(R.id.calldialButton).setOnClickListener(this);
		parent.findViewById(R.id.callhandupButton).setOnClickListener(this);
		parent.findViewById(R.id.callmic_muteButton).setOnClickListener(this);
		parent.findViewById(R.id.callspeaker_muteButton).setOnClickListener(this);
		mChronometer=(Chronometer)parent.findViewById(R.id.chronometer);
		CallStatus=(TextView)parent.findViewById(R.id.call_status);
		CallPeople=(TextView)parent.findViewById(R.id.call_people);
		CallPeopleNum=(TextView)parent.findViewById(R.id.call_people_num);
		mChronometer.setVisibility(View.INVISIBLE);
		
		
		
		hand = handler;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			
			case R.id.callhandupButton:
				try {
					MainActivity.service.GOCSDK_phoneHangUp();     //挂断
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case R.id.callmic_muteButton:
				if(DialpadFragment.mMicPhoneEnabled == true){
					handler.sendEmptyMessage(MainActivity.MSG_SET_MICPHONE_OFF);
					DialpadFragment.mMicPhoneEnabled=false;
				}else{
					handler.sendEmptyMessage(MainActivity.MSG_SET_MICPHONE_ON);
					DialpadFragment.mMicPhoneEnabled=true;
				}
				break;
				
			case R.id.callspeaker_muteButton:
				if(DialpadFragment.mSpeakerEnabled == true){
					handler.sendEmptyMessage(MainActivity.MSG_SET_MICPHONE_OFF);
					DialpadFragment.mSpeakerEnabled=false;
				}else{
					handler.sendEmptyMessage(MainActivity.MSG_SET_MICPHONE_ON);
					DialpadFragment.mSpeakerEnabled=true;
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
			case MSG_OUTGONG:
//				String phonenum = (String)msg.obj;
//				CallPeopleNum.setText(phonenum);
				CallStatus.setText(string.Call_status);    //正在呼叫
				break;
				
			case MSG_TALKING:
				mChronometer.setBase(SystemClock.elapsedRealtime());
				
				mChronometer.setVisibility(View.VISIBLE);
				mChronometer.start();
				CallStatus.setText(string.Call_talk);      //通话中
				break;
				
			case MSG_HANGUP:
				mChronometer.setBase(SystemClock.elapsedRealtime());
				mChronometer.stop();
				mChronometer.setVisibility(View.INVISIBLE);
				CallStatus.setText(string.Call_over);      //通话结束
				break;
			}
		}
	};

}
