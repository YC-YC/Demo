package com.goodocom.rk;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class MusicPlayFragment extends Fragment implements
	View.OnClickListener{

	private static final String TAG = "MusicPlayFragment";
	static ImageButton PlayBt = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View parent = inflater.inflate(R.layout.activity_music, container,
				false);
		initializeDeviceList(parent);
		return parent;
	}

	private void initializeDeviceList(View parent) {
		parent.findViewById(R.id.Music_PlayOrPause).setOnClickListener(this);
		parent.findViewById(R.id.Music_Next).setOnClickListener(this);
		parent.findViewById(R.id.Music_Last).setOnClickListener(this);
		parent.findViewById(R.id.Music_volume_down).setOnClickListener(this);
		parent.findViewById(R.id.Music_volume_up).setOnClickListener(this);
		
		PlayBt = (ImageButton)parent.findViewById(R.id.Music_PlayOrPause);
		
		if(MainActivity.mMusicPlaying == 0){
			PlayBt.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play)); 
		}else{
			PlayBt.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause)); 
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		
			case R.id.Music_PlayOrPause:
				
				//Play or Pause Icon transfer
				if(MainActivity.mMusicPlaying == 0){
					try {
						MainActivity.service.GOCSDK_musicPlayOrPause();
						PlayBt.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause)); 
						MainActivity.mMusicPlaying = 1;
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					try {
						MainActivity.service.GOCSDK_musicPlayOrPause();
						PlayBt.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play)); 
						MainActivity.mMusicPlaying = 0;
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;

			case R.id.Music_Next:
				try {
					MainActivity.service.GOCSDK_musicNext();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case R.id.Music_Last:
				try {
					MainActivity.service.GOCSDK_musicPrevious();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case R.id.Music_volume_down:
				Handler handler = MainActivity.getHandler();
				if(null == handler)return;
				handler.sendEmptyMessage(MainActivity.MSG_MUSIC_VOLUME_DOWN); 
				break;
				
			case R.id.Music_volume_up:
				Handler handler1 = MainActivity.getHandler();
				if(null == handler1)return;
				handler1.sendEmptyMessage(MainActivity.MSG_MUSIC_VOLUME_UP); 
				break;
		}
	}

}
