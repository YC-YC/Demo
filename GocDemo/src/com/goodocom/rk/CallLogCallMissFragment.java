package com.goodocom.rk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.RemoteException;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemLongClickListener;

public class CallLogCallMissFragment extends Fragment implements
	View.OnClickListener{

	private static final String TAG = "CallLogCallMissFragment";
	

	public static List<Map<String, String>> call_log = new ArrayList<Map<String,String>>();
	public static SimpleAdapter simpleLogAdapter;
	private int nIndex = -1; 
	private int LogType = 1;     //1:Call in 2:Call out 3:Miss Anwser
	
	ListView mLogListView = null;
	
     
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View parent = inflater.inflate(R.layout.call_log_main_layout, container,
				false);
		initializeDeviceList(parent);
		return parent;
	}

	private void initializeDeviceList(View parent) {
		mLogListView = (ListView)parent.findViewById(R.id.call_log_listView);
		
		simpleLogAdapter = new SimpleAdapter(getActivity(), call_log, R.layout.single_call_miss_log, 
				new String[] {"itemName","itemnum","time" },new int[] {R.id.callmiss_log_name, R.id.callmiss_log_num,R.id.callmiss_log_time});
		
		
		Map<String, String> log = new HashMap<String, String>();
		log.put("itemName", "Miss");
		log.put("itemnum", "18059046979");
		log.put("time", "10:59");
		call_log.add(log);
		simpleLogAdapter.notifyDataSetChanged();
		
		mLogListView.setAdapter(simpleLogAdapter);
		mLogListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
	
				nIndex = mLogListView.getId();
				if(-1 == nIndex)return false;
				Log.d("goc", "onItemLongClick");
				return false;
			}
		});
		
	
		mLogListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Map<String,String>People =(Map<String,String>)simpleLogAdapter.getItem(arg2);
				String Name = People.get("itemName");
				String Num = People.get("itemnum");
				
				try {
					MainActivity.service.GOCSDK_phoneDail(Num);     
					CallFragment.CallPeople.setText(Name);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// TODO Auto-generated method stub
				nIndex = mLogListView.getId();
				Log.d("goc", "onItemClick" + Name +Num);
			}
		});
		
	}
		
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
	
	
}

	