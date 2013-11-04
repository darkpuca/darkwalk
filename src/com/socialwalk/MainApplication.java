package com.socialwalk;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap.CompressFormat;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

	@Override
	public void onCreate()
	{
		super.onCreate();
		init();
		getSlideActive();
	}

	/**
	 * Intialize the request manager and the image cache 
	 */
	private void init() {
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


}