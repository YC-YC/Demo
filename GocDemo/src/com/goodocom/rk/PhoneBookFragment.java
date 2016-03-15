package com.goodocom.rk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemLongClickListener;

public class PhoneBookFragment extends Fragment implements
	View.OnClickListener{

	private static final String TAG = "PhoneBookFragment";
	
	SQLiteDatabase phonebookdb;      //database (phone book and call log)
	public final static int MSG_PHONE_BOOK = 1;
	public final static int MSG_CALLLOG = 2;
	public final static int MSG_PHONE_BOOK_DONE = 3;
	public final static int MSG_CALL_LOG_DONE = 4;
	
	static List<Map<String, String>> contacts = new ArrayList<Map<String,String>>();
	static SimpleAdapter simpleAdapter;
	private int nIndex = -1; 
	
	Button button21 = null;
	EditText searchEt = null;
	ListView mListView = null;
	
	public static class phoneBook{
		public String name = null;
		public String num = null; 
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View parent = inflater.inflate(R.layout.activity_phone_book, container,
				false);
		initializeDeviceList(parent);
		return parent;
	}

	public void initializeDeviceList(View parent) {
		button21 = (Button)parent.findViewById(R.id.button21);
		searchEt = (EditText)parent.findViewById(R.id.searchEt);
		
		button21.setOnClickListener(this);
		
		mListView = (ListView)parent.findViewById(R.id.listView2);
		
		//SimpleAdapter
		simpleAdapter = new SimpleAdapter(getActivity(), contacts, R.layout.devices, 
				new String[] {"itemName","itemnum" },new int[] {R.id.btname, R.id.btaddr});
		
		//SimpleAdapter ListView
		mListView.setAdapter(simpleAdapter);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
	
				nIndex = mListView.getId();
				if(-1 == nIndex)return false;
				Log.d("goc", "onItemLongClick");
				return false;
			}
		});
		
	
		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Map<String,String>People =(Map<String,String>)simpleAdapter.getItem(arg2);
				String Name = People.get("itemName");
				String Num = People.get("itemnum");
				
				phoneBook phobook = new phoneBook() ;
				phobook.name = Name;
				phobook.num = Num;
				
				Handler handler = MainActivity.getHandler();
				if(handler != null){
					handler.sendMessage(handler.obtainMessage(MainActivity.MSG_DIAL_DIALOG,phobook));
				}
					
				// TODO Auto-generated method stub
				nIndex = mListView.getId();
				Log.d("goc", "onItemClick" + Name +Num);
			}
		});
		
		
		searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                            int count) {
            	simpleAdapter.getFilter().filter(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                            int after) {
                    // TODO Auto-generated method stub
            
            }

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}
        });

		hand = handler;
	}
	
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.button21:
				try {
					if(contacts.isEmpty() == false){
						contacts.clear();
					}
					MainActivity.getService().GOCSDK_phoneBookStartUpdate();
					
					Handler handler = MainActivity.getHandler();
					if(handler == null)return;
					handler.sendEmptyMessage(MainActivity.MSG_UPDATE_PHONEBOOK);
					
					
					phonebookdb=Database.getPhoneBookDb();
					
					Database.delete_table_data(phonebookdb, Database.PhoneBookTable);
					Database.createTable(phonebookdb,Database.Sql_create_phonebook_tab);
					
					//MainActivity.getService().callLogstartUpdate(3);
					Log.d("PhoneBookFragment", "PhoneBookStartUpdate Call!");
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
		public void handleMessage(final android.os.Message msg) {
                	switch (msg.what) {
    				
    				case MSG_PHONE_BOOK:
    					phoneBook phoBooks = (phoneBook) msg.obj;
    					Map<String, String> phoBook = new HashMap<String, String>();
    					phoBook.put("itemName", phoBooks.name);
    					phoBook.put("itemnum", phoBooks.num);
    					contacts.add(phoBook);
    					simpleAdapter.notifyDataSetChanged();
    					
    					if(phonebookdb != null){
    						Database.insert_phonebook(phonebookdb,"phonebook",phoBooks.name,phoBooks.num);
    					}
    					
    					break;
    				case MSG_PHONE_BOOK_DONE:
    					
    					if(phonebookdb != null){
    						phonebookdb.close();           //close database
    						phonebookdb = null;
    					}
    					
    					break;
    					
    				default:
    					break;
    				}
                
		};
	};
}