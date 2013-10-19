package com.socialwalk;

import java.util.Date;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.socialwalk.dataclass.AroundersItems.AroundersItem;
import com.socialwalk.dataclass.AroundersItems;
import com.socialwalk.dataclass.NeoClickItems;

public class LockService extends Service
{
	public static boolean IsRegisted = false;
	
	public static NeoClickItems NeoClickAds = new NeoClickItems();
	public static Date NeoClickUpdateTime;
	
	private BroadcastReceiver mReceiver;
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock kl = km.newKeyguardLock("IN");
		kl.disableKeyguard();
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		
		mReceiver = new LockReceiver();
		registerReceiver(mReceiver, filter);
		
		// 슬라이더 등록 상태 저장
		this.IsRegisted = true;
//		SharedPreferences envPrefs = getSharedPreferences(Globals.PREFS_ENVIRNMENT, MODE_PRIVATE);
//		SharedPreferences.Editor prefsEditor = envPrefs.edit();
//		prefsEditor.putBoolean(Globals.PREF_KEY_SLIDER_REGIST, true);
//		prefsEditor.commit();
		
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
	}	
}

