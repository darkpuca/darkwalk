package com.socialwalk;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class LockService extends Service
{
	public static boolean IsRegisted = false;
	public static boolean IsAlarm = false;
	
	private static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";
	private static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";
	private static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";
	private static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";
	
	
	private BroadcastReceiver mLockReceiver;
	
	private BroadcastReceiver mAlarmReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action.equals(ALARM_ALERT_ACTION) || 
					action.equals(ALARM_DISMISS_ACTION) || 
					action.equals(ALARM_SNOOZE_ACTION) || 
					action.equals(ALARM_DONE_ACTION))
			{
				// �˶� �̺�Ʈ �߻��� �����̵� ����.
				IsAlarm = true;
			}
		}
	};
	

	
	
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		// ��ũ�� on/off �̺�Ʈ ���� ���.
		IntentFilter lockFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
		
		mLockReceiver = new LockReceiver();
		registerReceiver(mLockReceiver, lockFilter);
		
		// �˶� �̺�Ʈ ���� ����Ʈ ���.
		IntentFilter alarmFilter = new IntentFilter(ALARM_ALERT_ACTION);
		alarmFilter.addAction(ALARM_DISMISS_ACTION);
		alarmFilter.addAction(ALARM_SNOOZE_ACTION);
		alarmFilter.addAction(ALARM_DONE_ACTION);
		registerReceiver(mAlarmReceiver, alarmFilter);

		
		// �����̴� ��� ���� ����
		IsRegisted = true;
		
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
	}

}

