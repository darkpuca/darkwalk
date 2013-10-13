package com.socialwalk;

import java.io.File;

import com.socialwalk.dataclass.WalkHistory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

public class Utils
{
	public static Utils defaultTool;
	
	private Activity m_baseActivity;
	private ConnectivityManager connectivity;
	private NetworkInfo wifiNetInfo, mobileNetInfo;
	
	public static void CreateDefaultTool(Activity baseActivity)
	{
		if (null == defaultTool)
			defaultTool = new Utils(baseActivity);
		else
			defaultTool.SetBaseActivity(baseActivity);
	}

	public Utils(Activity baseActivity)
	{
		m_baseActivity = baseActivity;
	}
	
	public void SetBaseActivity(Activity baseActivity)
	{
		m_baseActivity = baseActivity;
	}
	
	public boolean IsNetworkAvailable()
	{
		connectivity = (ConnectivityManager)m_baseActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		mobileNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
//		Toast.makeText(m_baseActivity, "wifi: " + wifiNetInfo.isConnected() + " mobile: " + mobileNetInfo.isConnected(), Toast.LENGTH_SHORT).show();
		
		if (false == wifiNetInfo.isAvailable() && false == mobileNetInfo.isAvailable())
			return false;

		return true;
	}
	
	public boolean IsGpsAvailable()
	{
        LocationManager lm = (LocationManager)m_baseActivity.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	public void showGpsSettingWithDialog()
	{
		AlertDialog.Builder dlg = new AlertDialog.Builder(m_baseActivity);
		dlg.setCancelable(true);
		dlg.setTitle(R.string.TITLE_INFORMATION);
		dlg.setMessage(R.string.MSG_GPS_SETTING_CONFIRM);
		dlg.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
	        	// show gps setting activity
	        	Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	        	m_baseActivity.startActivity(i);
			}
		});
		dlg.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		dlg.show();		        	
	}
	
	public void showFinishDialog(int messageId)
	{
		AlertDialog.Builder closeDlg = new AlertDialog.Builder(m_baseActivity);
		closeDlg.setCancelable(false);
		closeDlg.setTitle(R.string.TITLE_INFORMATION);
		closeDlg.setMessage(messageId);
		closeDlg.setNeutralButton(R.string.CLOSE, new DialogInterface.OnClickListener()
		{					
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
//				finish();
				System.exit(0);
			}
		});
		closeDlg.show();
	}
	
	public WalkHistory WalkHistoryFromFile(String fileName)
	{
		if (null != fileName)
		{
			File dataDir = m_baseActivity.getFilesDir();
			String filePath = dataDir.getPath() + "/" + fileName;
			File file = new File(filePath);
			MyXmlParser parser = new MyXmlParser(file);
			WalkHistory history = parser.GetWalkHistory();
			history.FileName = file.getName();
			return history;
		}
		return null;
	}
	
	public static void SaveLoginParams(Context context, boolean isAuto, String userId, String password)
	{
		SharedPreferences loginPrefs = context.getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = loginPrefs.edit();
		editor.putBoolean(Globals.PREF_KEY_AUTOLOGIN, isAuto);
		editor.putString(Globals.PREF_KEY_USER_ID, userId);
		editor.putString(Globals.PREF_KEY_PASSWORD, password);
		editor.commit();
	}
}
