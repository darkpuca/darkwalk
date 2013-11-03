package com.socialwalk;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LockReceiver extends BroadcastReceiver
{
	public static boolean wasScreenOn = true;
	public static Date LastAccess;
	
	public static boolean IsPhoneCalling = false;
	private static int LimitTime = 0;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (true == IsPhoneCalling) return;

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
		{
			wasScreenOn = false;
//			Intent i = new Intent(context, SlideActivity.class);
//			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startActivity(i);
		}
		else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
		{
			wasScreenOn = true;
			
			if (!LockService.IsActive) return;
			
			if (null != LastAccess)
			{
				Date now = new Date();
				long diffInMs = now.getTime() - LastAccess.getTime();
				long diffInMinutes= TimeUnit.MILLISECONDS.toMinutes(diffInMs);
				if (LimitTime > diffInMinutes) return;
			}

			Intent i = new Intent(context, SlideActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(i);
		}
		else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
			if (!LockService.IsActive) return;

			Intent i = new Intent(context, SlideActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(i);
		}

	}
	
	public static void UpdateAccessTime()
	{
		LastAccess = new Date();
	}

}
