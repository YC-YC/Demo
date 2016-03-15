package com.goodocom.rk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class CallLogFragment extends Fragment implements
	View.OnClickListener{

	private static final String TAG = "CallLogFragment";
	
	private CallLogCallInFragment mCallLogCallInFragment;	
	private CallLogCallOutFragment mCallLogCallOutFragment;	
	private CallLogCallMissFragment mCallLogCallMissFragment;	
	
	SQLiteDatabase CallLogdb;
	
	public final static int MSG_CALL_IN = 1;
	public final static int MSG_CALL_OUT = 2;
	public final static int MSG_CALL_MISS = 3;
	public final static int MSG_READ_CALL_LOG_FROM_DB =4;
	
	public final static int mCallInType = 1;
	public final static int mCallOutType = 2;
	public final static int mCallMissType = 3;
	
	static List<Map<String, String>> calllog = new ArrayList<Map<String,String>>();
	static SimpleAdapter simpleAdapter;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View parent = inflater.inflate(R.layout.call_log_fragment, container,
				false);
		initializeDeviceList(parent);
    	Database.createTable(CallLogdb,Database.Sql_create_calllog_tab);
		return parent;
	}

	private void initializeDeviceList(View parent) {
		
		parent.findViewById(R.id.bt_call_in).setOnClickListener(this);
		parent.findViewById(R.id.bt_call_out).setOnClickListener(this);
		parent.findViewById(R.id.bt_call_missed).setOnClickListener(this);
		
		CallLogdb=Database.getPhoneBookDb();
		
		if ( mCallLogCallInFragment == null) {
			mCallLogCallInFragment = (CallLogCallInFragment) getFragmentManager()
	                    .findFragmentById(R.id.callLogCallInFragment);
			mCallLogCallInFragment.getView().setVisibility(View.VISIBLE);
	     }
		
		if ( mCallLogCallOutFragment == null) {
			mCallLogCallOutFragment = (CallLogCallOutFragment) getFragmentManager()
	                    .findFragmentById(R.id.callLogCallOutFragment);
			mCallLogCallOutFragment.getView().setVisibility(View.INVISIBLE);
	     }
		
		if ( mCallLogCallMissFragment == null) {
			mCallLogCallMissFragment = (CallLogCallMissFragment) getFragmentManager()
	                    .findFragmentById(R.id.callLogCallMissFragment);
			mCallLogCallMissFragment.getView().setVisibility(View.INVISIBLE);
	     }
		
		
		hand = handler;
	}
		
	
	public String TimestamptoDate(String time){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String re_StrTime;
		long lcc_time = Long.valueOf(time);
		re_StrTime = sdf.format(lcc_time);
		return re_StrTime;
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.bt_call_in:
				mCallLogCallInFragment.getView().setVisibility(View.VISIBLE);
				mCallLogCallOutFragment.getView().setVisibility(View.INVISIBLE);
				mCallLogCallMissFragment.getView().setVisibility(View.INVISIBLE);
				
				if(CallLogCallInFragment.call_log.isEmpty() == false){
					CallLogCallInFragment.call_log.clear();
				}
				
				String Phonename;
				String Phonenum;
				String time;
		        Cursor cursor = CallLogdb.query("calllog", new String[]{"phonename","phonenumber","time"}, "calltype=?", 
		        		new String[]{"1"}, null, null, "time desc");  //"ORDEY BY ASC"
		        
		        while(cursor.moveToNext()){  
		        	Phonenum = cursor.getString(cursor.getColumnIndex("phonenumber"));
		            Phonename = cursor.getString(cursor.getColumnIndex("phonename")); 
		            time = cursor.getString(cursor.getColumnIndex("time")); 
		            
		            Map<String, String> callin_log = new HashMap<String, String>();
		            callin_log.put("itemName", Phonename);
		            callin_log.put("itemnum", Phonenum);
		            callin_log.put("time", TimestamptoDate(time));
		            
		            CallLogCallInFragment.call_log.add(callin_log);
		            CallLogCallInFragment.simpleLogAdapter.notifyDataSetChanged();
		        }  

				break;
			case R.id.bt_call_out:
				mCallLogCallInFragment.getView().setVisibility(View.INVISIBLE);
				mCallLogCallOutFragment.getView().setVisibility(View.VISIBLE);
				mCallLogCallMissFragment.getView().setVisibility(View.INVISIBLE);
				
				if(CallLogCallOutFragment.call_log.isEmpty() == false){
					CallLogCallOutFragment.call_log.clear();
				}
				
				String Phonename1;
				String Phonenum1;
				String time1;
		        Cursor cursor1 = CallLogdb.query("calllog", new String[]{"phonename","phonenumber","time"}, "calltype=?", 
		        		new String[]{"2"}, null, null, "time desc");  //"ORDEY BY ASC"
				
		        while(cursor1.moveToNext()){  
		        	Phonenum1 = cursor1.getString(cursor1.getColumnIndex("phonenumber"));
		            Phonename1 = cursor1.getString(cursor1.getColumnIndex("phonename")); 
		            time1 = cursor1.getString(cursor1.getColumnIndex("time")); 
		            
		            Map<String, String> callin_log = new HashMap<String, String>();
		            callin_log.put("itemName", Phonename1);
		            callin_log.put("itemnum", Phonenum1);
		            callin_log.put("time", TimestamptoDate(time1));
		            
		            CallLogCallOutFragment.call_log.add(callin_log);
		            CallLogCallOutFragment.simpleLogAdapter.notifyDataSetChanged();
		        }  
				
				break;
			case R.id.bt_call_missed:
				mCallLogCallInFragment.getView().setVisibility(View.INVISIBLE);
				mCallLogCallOutFragment.getView().setVisibility(View.INVISIBLE);
				mCallLogCallMissFragment.getView().setVisibility(View.VISIBLE);
				
				if(CallLogCallMissFragment.call_log.isEmpty() == false){
					CallLogCallMissFragment.call_log.clear();
				}
				
				String Phonename2;
				String Phonenum2;
				String time2;
		        Cursor cursor2 = CallLogdb.query("calllog", new String[]{"phonename","phonenumber","time"}, "calltype=?", 
		        		new String[]{"3"}, null, null, "time desc");  //"ORDEY BY ASC"
				
		        while(cursor2.moveToNext()){  
		        	Phonenum2 = cursor2.getString(cursor2.getColumnIndex("phonenumber"));
		            Phonename2 = cursor2.getString(cursor2.getColumnIndex("phonename")); 
		            time2 = cursor2.getString(cursor2.getColumnIndex("time")); 
		            
		            Map<String, String> callin_log = new HashMap<String, String>();
		            callin_log.put("itemName", Phonename2);
		            callin_log.put("itemnum", Phonenum2);
		            callin_log.put("time", TimestamptoDate(time2));
		            
		            CallLogCallMissFragment.call_log.add(callin_log);
		            CallLogCallMissFragment.simpleLogAdapter.notifyDataSetChanged();
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
		public void handleMessage(final android.os.Message msg) {
                	switch (msg.what) {
    				
    				case MSG_READ_CALL_LOG_FROM_DB:
    					mCallLogCallInFragment.getView().setVisibility(View.VISIBLE);
    					mCallLogCallOutFragment.getView().setVisibility(View.INVISIBLE);
    					mCallLogCallMissFragment.getView().setVisibility(View.INVISIBLE);
    					
    					if(CallLogCallInFragment.call_log.isEmpty() == false){
    						CallLogCallInFragment.call_log.clear();
    					}
    					
    					String Phonename;
    					String Phonenum;
    					String time;
    			        Cursor cursor = CallLogdb.query("calllog", new String[]{"phonename","phonenumber","time"}, "calltype=?", 
    			        		new String[]{"1"}, null, null, "time desc");  //"ORDEY BY ASC"
    					
    			        while(cursor.moveToNext()){  
    			        	Phonenum = cursor.getString(cursor.getColumnIndex("phonenumber"));
    			            Phonename = cursor.getString(cursor.getColumnIndex("phonename")); 
    			            time = cursor.getString(cursor.getColumnIndex("time")); 
    			            
    			            Map<String, String> callin_log = new HashMap<String, String>();
    			            callin_log.put("itemName", Phonename);
    			            callin_log.put("itemnum", Phonenum);
    			            callin_log.put("time", TimestamptoDate(time));
    			            
    			            CallLogCallInFragment.call_log.add(callin_log);
    			            CallLogCallInFragment.simpleLogAdapter.notifyDataSetChanged();
    			        } 
    					break;
    				
    				case MSG_CALL_OUT:
    					Log.d("CallLogFragment", "MSG_CALL_OUT ==============");
    					Database.insert_calllog(CallLogdb, Database.CallLogTable, MainActivity.mLatestPhoneName, MainActivity.mLatestPhoneNum, MainActivity.mLatestCallType, System.currentTimeMillis());
    					Log.d("CallLogFragment", "insert_calllog ==============");
    					break;
    					
    				default:
    					break;
    				}
                
		};
	
	};
}

	
