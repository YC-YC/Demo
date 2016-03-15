package com.goodocom.rk;

public interface BluetoothService {
	//void registerCallback(IGocsdkCallback callback);
	//void unregisterCallback(IGocsdkCallback callback);
	void getLocalName();

	void setLocalName(String name);

	void getPinCode();

	void setPinCode(String pincode);

	void connectLast();
	
	void connectA2dp(String addr);

	void connectHFP(String addr);

	void disconnect();

	void disconnectA2DP();

	void disconnectHFP();

	void deletePair(String addr);

	void startDiscovery();

	void getPairList();

	void stopDiscovery();

	void phoneAnswer();

	void phoneHangUp();

	void phoneDail(String phonenum);

	void phoneTransmitDTMFCode(char code);
	
	void phoneTransfer();

	void phoneTransferBack();

	void phoneBookStartUpdate();

	void callLogstartUpdate(int type);

	void musicPlayOrPause();

	void musicStop();

	void musicPrevious();

	void musicNext();
}
