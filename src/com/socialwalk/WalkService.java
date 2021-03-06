package com.socialwalk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.socialwalk.dataclass.WalkHistory;
import com.socialwalk.dataclass.WalkHistoryManager;
import com.socialwalk.request.ServerRequestManager;

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

	private Timer tempSaveTimer;
	private Handler tempSaveHandler;
	
	public WalkService()
	{

	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		m_notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		m_locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
	}

	@Override
	public void onDestroy()
	{
		if (IsStarted)
			StopWalking();
		else
			super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (!IsStarted)
		{
			boolean is_restore = intent.getBooleanExtra(Globals.EXTRA_KEY_TEMP_RESTORE, false);
			
			startWalking(is_restore);			// 걷기 작업 시작
		}
		
		return START_STICKY_COMPATIBILITY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	private void startWalking(boolean restore)
	{
		IsStarted = true;
		Log.d(TAG, "walking started.");
		
		if (restore)
		{
			// 임시 로그 데이타에서 복원.
			WalkHistory restore_history = Utils.GetDefaultTool().WalkHistoryFromFile(this, Globals.TEMPORARY_WALK_FILENAME);
			WalkHistoryManager.AddWalking(restore_history);
			
			this.WalkingData = restore_history;
		}
		else
		{
			// 정상적 걷기 시작.
			this.WalkingData = WalkHistoryManager.StartNewLog();
		}

		showNotification();
		
		IntentFilter filter = new IntentFilter("com.darkpuca.socialwalk.location");
		m_walkReceiver = new WalkUpdateReceiver();
		this.registerReceiver(m_walkReceiver, filter);

		// create pending intent for gps location 
		Intent locIntent = new Intent(getApplicationContext(), WalkLocationReceiver.class);
		m_locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, locIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// start gps update
		m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, m_locationIntent);
		m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, m_locationIntent);
		
		createLocationUpdateTimer();
		
		createTemporarySaveTimer();
	}

	private void createTemporarySaveTimer()
	{
		tempSaveHandler = new Handler();
		TimerTask tempSaveTask = new TimerTask()
		{			
			@Override
			public void run()
			{
				tempSaveHandler.post(new Runnable()
				{
					@Override
					public void run()
					{
						Log.d("temp", "temp timer run!");
						saveTemporary();
					}
				});
			}
		};
		tempSaveTimer = new Timer();
		tempSaveTimer.scheduleAtFixedRate(tempSaveTask, Globals.TEMPORARY_WALK_SAVE_INTERVAL, Globals.TEMPORARY_WALK_SAVE_INTERVAL);
	}

	private void createLocationUpdateTimer()
	{
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

	private void showNotification()
	{
		// show notification item
		Intent notiIntent = new Intent(getApplicationContext(), MainActivity.class);
		notiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notiIntent, 0);
		
		Notification noti = new Notification(R.drawable.noti_icon, "SOCIALWALK", System.currentTimeMillis());
		String appName = getResources().getString(R.string.APP_NAME);
		String message = getResources().getString(R.string.MSG_NOTI_SOCIAL_WALKING);
		noti.setLatestEventInfo(getApplicationContext(), appName, message, pi);

		startForeground(1, noti);
	}
	
	private void StopWalking()
	{
		IsStarted = false;
		Log.d(TAG, "walking stopped");
		
		WalkingData.Finish();
		
		// 임시 저장 타이머 중지. 임시 저장 파일 삭제. 정상 종료되면 임시파일은 필요없다.
		tempSaveTimer.cancel();
		clearTemporary();

		// save log file
		SimpleDateFormat formatter = new SimpleDateFormat(Globals.DATETIME_FORMAT_FOR_HISTORY, Locale.US);
		String filename = formatter.format(WalkingData.StartTime) + ".log";
		WalkingData.FileName = filename;

		String strXml = WalkingData.GetXML();
		
		try
		{
			File logDir = this.getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);
			File logFile = new File(logDir, filename);

			FileOutputStream fos = new FileOutputStream(logFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			osw.write(strXml);
			osw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			Log.d(TAG, e.getLocalizedMessage());
		}
				
//		Toast.makeText(getBaseContext(), "social walking stopped.", Toast.LENGTH_SHORT).show();
		
		// remove notification item
		m_notificationManager.cancel(Globals.NOTI_WALKING);
		
		// stop location update timer
		locationTimer.cancel();
		
		// stop gps update
		this.unregisterReceiver(m_walkReceiver);
		m_locationManager.removeUpdates(m_locationIntent);
	}
	
	private void saveTemporary()
	{
		String strXml = WalkingData.GetXML();
		
		try
		{
			File tempDir = this.getDir("temp", Context.MODE_PRIVATE);
			File tempFile = new File(tempDir, Globals.TEMPORARY_WALK_FILENAME);
			
			// 기존 임시 파일 삭제.
			if (tempFile.exists())
				tempFile.delete();

			FileOutputStream fos = new FileOutputStream(tempFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			osw.write(strXml);
			osw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			Log.d(TAG, e.getLocalizedMessage());
		}

	}
	
	private void clearTemporary()
	{
		File tempDir = this.getDir("temp", Context.MODE_PRIVATE);
		File tempFile = new File(tempDir.getPath(), Globals.TEMPORARY_WALK_FILENAME);
		if (tempFile.exists())
			tempFile.delete();
	}
	
	private File getUserFolder()
	{
		if (null == ServerRequestManager.LoginAccount) return null;
		
		File folder = new File(ServerRequestManager.LoginAccount.Sequence);
		if (!folder.exists())
			folder.mkdir();
		
		return folder;
	}
	
	private File getUserLogFile(File folder, String filename)
	{
		if (null == ServerRequestManager.LoginAccount || null == folder || null == filename) return null;
		
		try
		{
			File logFile = new File(folder, filename);
			if (logFile.exists())
				logFile.delete();
			
//			logFile.createNewFile();
			
			return logFile;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void UpdateNewLocation(Location newLoc)
	{
		if (isBetterLocation(newLoc, LastBestLocation))
		{
			LastBestLocation = newLoc;
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
				UpdateNewLocation(loc);
		}
	}


}
