package com.goodocom.rk;
import android.os.Handler;
import android.os.RemoteException;

import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.rk.PhoneBookFragment.phoneBook;

public class GocsdkCallback extends IGocsdkCallback.Stub{
	public static String number = "";
	public static int hfpStatus = 0;
	private static int callType = 0;
	
	public class BtDevices{
		public String name = null;
		public String addr = null;
	}
	
	
	public class callLog{
		public int callType = 0;
		public String num = null; 
	}
	
	@Override
	public void onHfpDisconnected() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onHfpDisconnected");
		Handler handler = MainActivity.getHandler();
		if(null == handler)return;
		handler.sendEmptyMessage(MainActivity.MSG_HF_DISCONNECTED);
		
		Handler handler1 = DeviceListFragment.getHandler();
		if(handler1 != null){
			handler1.sendEmptyMessage(DeviceListFragment.MSG_DEVICES_DISCONT);
		}
		
		GocsdkCallback.hfpStatus = 0;
	}

	@Override
	public void onHfpConnected() throws RemoteException {
		Log.d(this, "onHfpConnected");
		Handler handler = MainActivity.getHandler();
		if(handler == null)return;
		handler.sendEmptyMessage(MainActivity.MSG_HF_CONNECTED);
		GocsdkCallback.hfpStatus = 1;
	}

	@Override
	public void onCallSucceed() throws RemoteException {
		// TODO Auto-generated method stub
		String dbg = "onCallSucceed =====================";
		Log.d(this, dbg); 
		
		Handler handler2 = CallFragment.getHandler();
		handler2.sendEmptyMessage(CallFragment.MSG_OUTGONG);
		
		Handler handler1 = MainActivity.getHandler();
		handler1.sendEmptyMessage(MainActivity.MSG_OUTGONG);
		GocsdkCallback.hfpStatus = 5;
	}

	@Override
	public void onIncoming(String number) throws RemoteException {
		// TODO Auto-generated method stub
		String dbg = "onIncoming =======================" + number;
		Log.d(this, dbg); 
		
		Handler handler1 = MainActivity.getHandler();
		handler1.sendMessage(handler1.obtainMessage(MainActivity.MSG_COMING, number));
		GocsdkCallback.number = number;
		GocsdkCallback.hfpStatus = 4;
	}

	@Override
	public void onHangUp() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onHangUp =======================");
		Handler handler1 = MainActivity.getHandler();
		handler1.sendEmptyMessage(MainActivity.MSG_HANGUP);
		
		Handler handler2 = CallFragment.getHandler();
		handler2.sendEmptyMessage(CallFragment.MSG_HANGUP);
		
		Handler handler3 = CallLogFragment.getHandler();
		handler3.sendEmptyMessage(CallLogFragment.MSG_CALL_OUT);
		
		GocsdkCallback.hfpStatus = 7;
	}

	@Override
	public void onTalking() throws RemoteException {
		// TODO Auto-generated method stub
		String dbg = "onTalking ======================= ";
		Log.d(this, dbg); 
		
		Handler handler = MainActivity.getHandler();
		handler.sendEmptyMessage(MainActivity.MSG_TALKING);
		Handler handler1 = CallFragment.getHandler();
		handler1.sendEmptyMessage(CallFragment.MSG_TALKING);
		
		GocsdkCallback.hfpStatus = 6;
	}

	@Override
	public void onRingStart() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onRingStart");
	}

	@Override
	public void onRingStop() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onRingStop");
	}

	@Override
	public void onHfpLocal() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onHfpLocal");
		
	}

	@Override
	public void onHfpRemote() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onHfpRemote");
		
	}

	@Override
	public void onInPairMode() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onInPairMode");
		
	}

	@Override
	public void onExitPairMode() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onExitPairMode");
		
	}

	@Override
	public void onInitSucceed() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onInitSucceed");
		
	}

	@Override
	public void onMusicPlaying() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onMusicPlaying");
		
		Handler handler = MainActivity.getHandler();
		if(null == handler)return;
		handler.sendEmptyMessage(MainActivity.MSG_MUSIC_PLAY);
	}

	@Override
	public void onMusicStopped() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onMusicStopped");
		
		Handler handler = MainActivity.getHandler();
		if(null == handler)return;
		handler.sendEmptyMessage(MainActivity.MSG_MUSIC_STOP);
	}

	@Override
	public void onAutoConnectAccept(boolean autoConnect, boolean autoAccept)
			throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onAutoConnectAccept");
		
	}

	@Override
	public void onCurrentAddr(String addr) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onCurrentAddr==========================="+addr);
		Handler handler = MainActivity.getHandler();
		if(handler == null)return;
		handler.sendMessage(handler.obtainMessage(MainActivity.MSG_REMOTE_ADDRESS, addr));		
	}

	@Override
	public void onCurrentName(String name) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onCurrentName");
		Handler handler = MainActivity.getHandler();
		if(handler == null)return;
		handler.sendMessage(handler.obtainMessage(MainActivity.MSG_REMOTE_NAME, name));	
		
		Handler handler1 = DeviceListFragment.getHandler();
		if(handler1 != null){
			handler1.sendEmptyMessage(DeviceListFragment.MSG_DEVICES_CONNECT);
		}
	}

	@Override
	public void onHfpStatus(int status) throws RemoteException {
		Log.d(this, "onHfpStatus"+status);		
	}

	@Override
	public void onAvStatus(int status) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onAvStatus");
	}

	@Override
	public void onVersionDate(String version) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onVersionDate");
		
	}

	@Override
	public void onCurrentDeviceName(String name) throws RemoteException {
		Log.d(this, "onCurrentDeviceName");
		
		Handler handler = MainActivity.getHandler();
		if(handler == null)return;
		Log.d(this, "handler != null");
		handler.sendMessage(handler.obtainMessage(MainActivity.MSG_DEVICENAME, name));
	}

	@Override
	public void onCurrentPinCode(String code) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onCurrentPinCode");
		
		Handler handler = MainActivity.getHandler();
		if(handler == null)return;
		Log.d(this, "handler != null");
		handler.sendMessage(handler.obtainMessage(MainActivity.MSG_DEVICEPINCODE, code));	
	}

	@Override
	public void onA2dpConnected() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onA2dpConnected");
		
	}

	@Override
	public void onCurrentAndPairList(int index, String name, String addr)
			throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onCurrentAndPairList");
		String btname = name;
		if(btname.equals(MainActivity.mCurDevName)){
			Log.d(this, "onCurrentAndPairList"+btname+"equsls:"+ MainActivity.mCurDevName);
			String btaddr = addr;
			Handler handler = MainActivity.getHandler();
			if(handler == null)return;
			handler.sendMessage(handler.obtainMessage(MainActivity.MSG_REMOTE_ADDRESS, btaddr));
		}
	}

	@Override
	public void onA2dpDisconnected() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onA2dpDisconnected");
		
	}

	@Override
	public void onPhoneBook(String name, String number) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onPhoneBook");
		Handler handler = PhoneBookFragment.getHandler();
		if(handler == null)return;
		phoneBook phobook = new phoneBook();
		phobook.name = name;
		phobook.num = number;

		handler.sendMessage(handler.obtainMessage(PhoneBookFragment.MSG_PHONE_BOOK,phobook));
	}

	@Override
	public void onSimBook(String name, String number) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onSimBook");
	}

	@Override
	public void onPhoneBookDone() throws RemoteException {
		// TODO Auto-generated method stub
		Handler handler = PhoneBookFragment.getHandler();
		if(handler == null)return;
		Log.d(this, "onPhoneBookDone");
		handler.sendEmptyMessage(PhoneBookFragment.MSG_PHONE_BOOK_DONE);
	}

	@Override
	public void onSimDone() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onSimDone");
	}

	@Override
	public void onCalllogDone() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onCalllogDone");
	}

	@Override
	public void onCalllog(int type, String number) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d("goc_calllog", number);

	}

	@Override
	public void onDiscovery(String name, String addr) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onDiscovery");		
		Handler handler = DeviceListFragment.getHandler();
		if (handler == null)return;
		BtDevices btdevice = new BtDevices();  
		btdevice.name = name;
		btdevice.addr = addr;
		handler.sendMessage(handler.obtainMessage(DeviceListFragment.MSG_DEVICES,btdevice));
	}

	@Override
	public void onDiscoveryDone() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onDiscoveryDone");
		Handler handler = DeviceListFragment.getHandler();
		if(null == handler)return;
		handler.sendEmptyMessage(DeviceListFragment.MSG_DEVICES_DONE);
	}

	@Override
	public void onLocalAddress(String addr) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onLocalAddress");
	}

	@Override
	public void onOutGoingOrTalkingNumber(String number) throws RemoteException {
		// TODO Auto-generated method stub
		String dbg = "onOutGoingOrTalkingNumber" + number;
		Log.d(this, dbg); 
		
		GocsdkCallback.number = number;
	}

	@Override
	public void onConnecting() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onConnecting");
	}

	@Override
	public void onSppData(int index, String data) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onSppData");
	}

	@Override
	public void onSppConnect(int index) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onSppConnect");
	}

	@Override
	public void onSppDisconnect(int index) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onSppDisconnect");
	}

	@Override
	public void onSppStatus(int status) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onSppStatus");
	}

	@Override
	public void onOppReceivedFile(String path) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onOppReceivedFile");
	}

	@Override
	public void onOppPushSuccess() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onOppPushSuccess");
	}

	@Override
	public void onOppPushFailed() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onOppPushFailed");
	}

	@Override
	public void onHidConnected() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onHidConnected");
	}

	@Override
	public void onHidDisconnected() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onHidDisconnected");
	}

	@Override
	public void onHidStatus(int status) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onHidStatus");
	}

	@Override
	public void onMusicInfo(String MusicName, String artist)
			throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onMusicInfo");

	}

	@Override
	public void onPanConnect() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onPanConnect");
	}

	@Override
	public void onPanDisconnect() throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onPanDisconnect");
	}

	@Override
	public void onPanStatus(int status) throws RemoteException {
		// TODO Auto-generated method stub
		Log.d(this, "onPanStatus");
	}
}
