package com.socialwalk;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap.CompressFormat;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.socialwalk.dataclass.NeoClickItems;
import com.socialwalk.request.ImageCacheManager;
import com.socialwalk.request.ImageCacheManager.CacheType;
import com.socialwalk.request.RequestManager;

/**
 * Example application for adding an L1 image cache to Volley. 
 * 
 * @author Trey Robinson
 *
 */
public class MainApplication extends Application
{
	public static boolean IsSlideActive;
	
	private static int DISK_IMAGECACHE_SIZE = 1024*1024*10;
	private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;
	private static int DISK_IMAGECACHE_QUALITY = 100;  //PNG is lossless so quality is ignored but must be provided
	
	private ConnectivityManager connectivity;
	private NetworkInfo wifiNetInfo, mobileNetInfo;
	
	public static Vector<String> AroundersVisitCodes;
	public static Date AroundersDate;
	
	public static NeoClickItems NeoClickAds = new NeoClickItems();
	public static Date NeoClickUpdateTime;


	@Override
	public void onCreate()
	{
		super.onCreate();

        // slide 서비스를 등록
        if (!LockService.IsRegisted)
			startService(new Intent(this, LockService.class));
        
        createAroundersMetas();

		init();
		
		getSlideActive();
	}
	
	

	/**
	 * Intialize the request manager and the image cache 
	 */
	private void init()
	{
		RequestManager.init(this);
		createImageCache();
	}
	
	/**
	 * Create the image cache. Uses Memory Cache by default. Change to Disk for a Disk based LRU implementation.  
	 */
	private void createImageCache(){
		ImageCacheManager.getInstance().init(this,
				this.getPackageCodePath()
				, DISK_IMAGECACHE_SIZE
				, DISK_IMAGECACHE_COMPRESS_FORMAT
				, DISK_IMAGECACHE_QUALITY
				, CacheType.MEMORY);
	}
	
	public boolean IsNetworkAvailable()
	{
		connectivity = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		mobileNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
//		Toast.makeText(m_baseActivity, "wifi: " + wifiNetInfo.isConnected() + " mobile: " + mobileNetInfo.isConnected(), Toast.LENGTH_SHORT).show();
		
		if (false == wifiNetInfo.isAvailable() && false == mobileNetInfo.isAvailable())
			return false;

		return true;
	}
	
	public boolean IsGpsAvailable()
	{
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	private void getSlideActive()
	{
		SharedPreferences slidePrefs = this.getSharedPreferences(Globals.PREF_NAME_SLIDE, Context.MODE_PRIVATE);
		IsSlideActive = slidePrefs.getBoolean(Globals.PREF_KEY_SLIDE, true);
	}

	private void createAroundersMetas()
	{
		if (null == AroundersVisitCodes)
			AroundersVisitCodes = new Vector<String>();
		
		SharedPreferences adPrefs = this.getSharedPreferences(Globals.PREF_NAME_AD, Context.MODE_PRIVATE);
		String prefCodes = adPrefs.getString(Globals.PREF_KEY_AROUNDERS_VISIT_CODES, "");
		if (0 < prefCodes.length())
		{
			prefCodes.replace("[", "");
			prefCodes.replace("]", "");
			
			String[] codes = prefCodes.split(",");
			for (String code : codes)
				AroundersVisitCodes.add(code);
		}
		
		String prefTime = adPrefs.getString(Globals.PREF_KEY_AROUDERS_TIME, "");
		if (0 < prefTime.length())
		{
			try
			{
				SimpleDateFormat  format = new SimpleDateFormat(Globals.DATE_FORMAT_FOR_SERVER, Locale.US);  
				AroundersDate = format.parse(prefTime);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				AroundersDate = new Date();
			}
		}
		else
		{
			AroundersDate = new Date();
		}

	}
	
	public void SaveMetas()
	{
		saveAroundersMetas();
	}
	
	private void saveAroundersMetas()
	{
		SharedPreferences adPrefs = getSharedPreferences(Globals.PREF_NAME_AD, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = adPrefs.edit();

		if (0 < AroundersVisitCodes.size())
		{
			String codes = "";
			for (String code : AroundersVisitCodes)
			{
				if (0 < codes.length()) code += ",";
				codes += code;
			}
			
			editor.putString(Globals.PREF_KEY_AROUNDERS_VISIT_CODES, codes);

			SimpleDateFormat  format = new SimpleDateFormat(Globals.DATE_FORMAT_FOR_SERVER, Locale.US);
			editor.putString(Globals.PREF_KEY_AROUDERS_TIME, format.format(AroundersDate));
		}
		else
		{
			editor.putString(Globals.PREF_KEY_AROUNDERS_VISIT_CODES, "");
		}

		editor.commit();
	}

}