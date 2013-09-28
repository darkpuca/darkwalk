package com.socialwalk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class WalkService extends Service
{
	public static boolean IsStarted = false;
	private static final String TAG = "SW_WALK_SVC";
	
	private NotificationManager m_notificationManager;
	private LocationManager m_locationManager;
	private PendingIntent m_locationIntent;
	private WalkUpdateReceiver m_walkReceiver = new WalkUpdateReceiver();

	public WalkHistory WalkingData = null;
	
	private Location LastBestLocation = null;
	private Timer locationTimer;
	private Handler locationHandler;
	private TimerTask locationTask;
	private static int LocationWaitCount = 0;

	public WalkService()
	{

	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		Log.d(TAG, "service created.");
		
		m_notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		m_locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		
	}

	@Override
	public void onDestroy()
	{
		if (IsStarted)
		{
			StopWalking();
		}

		Log.d(TAG, "service destroyed.");
		
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (!IsStarted)
		{
			// 걷기 작업 시작
			StartWalking();
		}
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	private void StartWalking()
	{
		IsStarted = true;
		Log.d(TAG, "walking started.");
		Toast.makeText(getBaseContext(), "social walking started.", Toast.LENGTH_SHORT).show();
		
		this.WalkingData = WalkHistoryManager.StartNewLog();

		// show notification item
		Intent notiIntent = new Intent(getApplicationContext(), WalkingActivity.class);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notiIntent, 0);
		
		Notification noti = new Notification(android.R.drawable.ic_dialog_map, "SOCIALWALK", System.currentTimeMillis());
		noti.setLatestEventInfo(getApplicationContext(), "SocialWalk", "Walking is started.", pi);
		m_notificationManager.notify(Globals.NOTI_WALKING, noti);
		
		IntentFilter filter = new IntentFilter("com.darkpuca.socialwalk.location");
		m_walkReceiver = new WalkUpdateReceiver();
		this.registerReceiver(m_walkReceiver, filter);

		// create pending intent for gps location 
		Intent locIntent = new Intent(getApplicationContext(), WalkLocationReceiver.class);
		m_locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, locIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// start gps update
		m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, m_locationIntent);
		m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, m_locationIntent);
		
		// start locaton update timer
		locationHandler = new Handler();
		locationTask = new TimerTask()
		{			
			@Override
			public void run()
			{
				locationHandler.post(new Runnable()
				{
					@Override
					public void run()
					{
						if (null == LastBestLocation)
						{
							if (0 == LocationWaitCount % 10)
								Toast.makeText(getApplicationContext(), "Waiting GPS signals...", Toast.LENGTH_SHORT).show();

							LocationWaitCount++;
							if (LocationWaitCount > 10000) LocationWaitCount = 0;
						}
						else
						{
							if (null != WalkingData)
							{	
								Location locNow = new Location(LastBestLocation);
								Date now = new Date();
								locNow.setTime(now.getTime());
								WalkingData.AddLog(locNow);
							}
						}
					}
				});
			}
		};
		locationTimer = new Timer();
		locationTimer.scheduleAtFixedRate(locationTask, 1000, 1000);

	}
	
	private void StopWalking()
	{
		IsStarted = false;
		Log.d(TAG, "walking stopped");
		
		WalkingData.Finish();
		
		String strXml = WalkingData.GetXML();
//		Log.d(TAG, strXml);
		
		// save log file
		SimpleDateFormat formatter = new SimpleDateFormat(Globals.DATE_FORMAT_FOR_HISTORY);
		String filename = formatter.format(WalkingData.StartTime) + ".log";
		
		try
		{
			FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			osw.write(strXml);
			osw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			Log.d(TAG, e.getLocalizedMessage());
			Toast.makeText(getApplicationContext(), "file save failed.", Toast.LENGTH_SHORT).show();
		}
				
		Toast.makeText(getBaseContext(), "social walking stopped.", Toast.LENGTH_SHORT).show();
		
		// remove notification item
		m_notificationManager.cancel(Globals.NOTI_WALKING);
		
		// stop location update timer
		locationTimer.cancel();
		
		// stop gps update
		this.unregisterReceiver(m_walkReceiver);
		m_locationManager.removeUpdates(m_locationIntent);
	}
	
	
	public void UpdateNewLocation(Location newLoc)
	{
		if (isBetterLocation(newLoc, LastBestLocation))
		{
			LastBestLocation = newLoc;
//			Toast.makeText(getApplicationContext(), "LOC. " + LastBestLocation.getLatitude() + ", " + LastBestLocation.getLongitude(), Toast.LENGTH_SHORT).show();
			Log.d(TAG, "[LOC] lat:" + LastBestLocation.getLatitude() + ", lng:" + LastBestLocation.getLongitude() + ", accu:" + LastBestLocation.getAccuracy());
		}
	}
	
	
	private static final int MINUTE_LIMIT = 1000 * 60 * 1;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation)
	{
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }
	    
	    // GPS 오차거리 20미터 이상이면 무시
	    if (20.0 < location.getAccuracy() && location.getProvider().equals(LocationManager.GPS_PROVIDER))
	    	return false;

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > MINUTE_LIMIT;
	    boolean isSignificantlyOlder = timeDelta < -MINUTE_LIMIT;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

	
	public class WalkUpdateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Location loc = (Location)intent.getExtras().get(Globals.EXTRA_KEY_LOCATION);
			if (null != loc)
			{
				UpdateNewLocation(loc);
//				WalkLogItem logItem = WalkingData.AddLog(loc);
//				Log.d(TAG, "[walk log, " + logItem.LogDate.toLocaleString() + "] dist:" + logItem.DistanceFromPrevious + ", spd:" + logItem.CurrentSpeed);
//				Log.d(TAG, "[walking] dist:" + WalkingData.TotalDistanceString() + ", seconds:" + WalkingData.TotalWalkingTimeString() + ", avg:" + WalkingData.AverageSpeed());
				
			}
		}
	}

}
