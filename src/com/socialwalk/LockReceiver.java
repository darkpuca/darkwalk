package com.socialwalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockReceiver extends BroadcastReceiver
{
	public static boolean wasScreenOn = true;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (true == SlideActivity.IsPhoneCalling) return;

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
		{
			wasScreenOn = false;
			Intent i = new Intent(context, SlideActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
		else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
		{
			wasScreenOn = true;
			Intent i = new Intent(context, SlideActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
		else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
			Log.d("SW", "BOOT_COMPLETED received.");
			Intent i = new Intent(context, SlideActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}

	}

}
