package com.socialwalk;

import java.io.File;
import java.text.DecimalFormat;

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
	private static Utils defaultTool = null;
	
	public static Utils GetDefaultTool()
	{
		if (null == defaultTool)
			defaultTool = new Utils();
		
		return defaultTool;
	}
	
	public Utils()
	{

	}
	
	public void ShowMessageDialog(Context context, int messageId)
	{
		new AlertDialog.Builder(context)
		.setTitle(R.string.TITLE_INFORMATION)
		.setMessage(messageId)
		.setNeutralButton(R.string.CLOSE, null)
		.show();
	}

	public void ShowFinishDialog(Context context, int messageId)
	{
		AlertDialog.Builder closeDlg = new AlertDialog.Builder(context);
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
	
	public void ShowGpsSettingWithDialog(Context context)
	{
		final Context baseContext = context;
		
		AlertDialog.Builder dlg = new AlertDialog.Builder(context);
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
	        	baseContext.startActivity(i);
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
	

	public static void SaveLoginParams(Context context, boolean isAuto, String userId, String password)
	{
		SharedPreferences loginPrefs = context.getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = loginPrefs.edit();
		editor.putBoolean(Globals.PREF_KEY_AUTOLOGIN, isAuto);
		editor.putString(Globals.PREF_KEY_USER_ID, userId);
		editor.putString(Globals.PREF_KEY_PASSWORD, password);
		editor.commit();
	}
	
	public WalkHistory WalkHistoryFromFile(Context context, String fileName)
	{
		if (null != fileName)
		{
			File dataDir = context.getFilesDir();
			String filePath = dataDir.getPath() + "/" + fileName;
			File file = new File(filePath);
			MyXmlParser parser = new MyXmlParser(file);
			WalkHistory history = parser.GetWalkHistory();
			history.FileName = file.getName();
			return history;
		}
		return null;
	}
	
	public String DecimalNumberString(long value)
	{
		DecimalFormat format = new DecimalFormat("###,###");
        String strVal = format.format(value);
        return strVal;
	}
}
