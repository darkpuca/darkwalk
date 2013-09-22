package com.socialwalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

public class WalkLocationReceiver extends BroadcastReceiver
{
	public WalkLocationReceiver()
	{

	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED))
		{
			if (!intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, true))
				Toast.makeText(context, "GPS disabled.", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(context, "GPS enabled.", Toast.LENGTH_SHORT).show();
		}
		
		if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED))
		{
			Location loc = (Location)intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
//			Toast.makeText(context, "LOC. " + loc.getLatitude() + ", " + loc.getLongitude(), Toast.LENGTH_SHORT).show();
			
			String actionString = "com.darkpuca.socialwalk.location";
			Intent i = new Intent();
			i.setAction(actionString);
			i.putExtra(Globals.EXTRA_KEY_LOCATION, loc);
			context.sendBroadcast(i);
		}

	}

}
