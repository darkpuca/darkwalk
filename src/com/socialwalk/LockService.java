package com.socialwalk;

import java.util.Date;
import java.util.Vector;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.socialwalk.dataclass.AroundersItems.AroundersItem;
import com.socialwalk.dataclass.AroundersItems;
import com.socialwalk.dataclass.NeoClickItems;

public class LockService extends Service
{
	public static boolean IsRegisted = false;
	
	public static NeoClickItems NeoClickAds = new NeoClickItems();
	public static Date NeoClickUpdateTime;
	
	public static Vector<String> AroundersVisitCodes = new Vector<String>();
	public static Date AroundersDate = new Date();
	
	private BroadcastReceiver mReceiver;
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
//		KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
//		KeyguardManager.KeyguardLock kl = km.newKeyguardLock("IN");
//		kl.disableKeyguard();
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		
		mReceiver = new LockReceiver();
		registerReceiver(mReceiver, filter);
		
		// 슬라이더 등록 상태 저장
		IsRegisted = true;
		
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
	}
		
}

