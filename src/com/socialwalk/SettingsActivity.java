package com.socialwalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.AccountHeart;
import com.socialwalk.request.ServerRequestManager;
import com.socialwalk.request.SocialWalkRequest;

public class SettingsActivity extends Activity 
implements OnClickListener, Response.Listener<String>, Response.ErrorListener 
{
	private ServerRequestManager server = null;
	private int reqType;
	private static final int REQUEST_LOGOUT = 400;
	private static final int REQUEST_ACCUMULATED_HEARTS = 401;
	
	private ProgressDialog progDlg;

	private CheckBox autoLoginCheck, slideCheck;
	private RelativeLayout logoutLayout;
	private LinearLayout heartsLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		this.server = new ServerRequestManager();
		
		this.autoLoginCheck = (CheckBox)findViewById(R.id.autologinCheck);
		this.autoLoginCheck.setOnClickListener(this);
		
		this.slideCheck = (CheckBox)findViewById(R.id.slideCheck);
		this.slideCheck.setOnClickListener(this);
		
		this.logoutLayout = (RelativeLayout)findViewById(R.id.logoutLayout);
		this.logoutLayout.setOnClickListener(this);
		
		this.heartsLayout = (LinearLayout)findViewById(R.id.totalHeartsLayout);
		this.heartsLayout.setVisibility(View.INVISIBLE);
		
		try
		{
			TextView version = (TextView)findViewById(R.id.version);
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version.setText(pInfo.versionName);		
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		updateOptions();

		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_SEND_DATA));
		
		requestAccumulatedHearts();
	}
	
	private void requestAccumulatedHearts()
	{
		if (!progDlg.isShowing()) progDlg.show();
		
		this.reqType = REQUEST_ACCUMULATED_HEARTS;
		this.server.TotalAccumulatedHearts(this, this);
	}
	
	private void setAutologinOption()
	{
		SharedPreferences loginPrefs = this.getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = loginPrefs.edit();
		editor.putBoolean(Globals.PREF_KEY_AUTOLOGIN, this.autoLoginCheck.isChecked());
		editor.commit();
	}
	
	private void setSlideOption()
	{
		MainApplication.IsSlideActive = slideCheck.isChecked();
		LockReceiver.SlideLastAccess = null;
		
		SharedPreferences loginPrefs = this.getSharedPreferences(Globals.PREF_NAME_SLIDE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = loginPrefs.edit();
		editor.putBoolean(Globals.PREF_KEY_SLIDE, this.slideCheck.isChecked());
		editor.commit();
	}
	
	private void logout()
	{
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setCancelable(true);
		dlg.setTitle(R.string.TITLE_INFORMATION);
		dlg.setMessage(R.string.MSG_LOGOUT_CONFIRM);
		dlg.setPositiveButton(R.string.LOGOUT, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (!progDlg.isShowing()) progDlg.show();
				
				reqType = REQUEST_LOGOUT;
				server.Logout(SettingsActivity.this, SettingsActivity.this);
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
	
	private void updateOptions()
	{
		SharedPreferences loginPrefs = this.getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
		this.autoLoginCheck.setChecked(loginPrefs.getBoolean(Globals.PREF_KEY_AUTOLOGIN, false));

		SharedPreferences slidePrefs = this.getSharedPreferences(Globals.PREF_NAME_SLIDE, Context.MODE_PRIVATE);
		this.slideCheck.setChecked(slidePrefs.getBoolean(Globals.PREF_KEY_SLIDE, false));
	}

	
	@Override
	public void onClick(View v)
	{
		if (autoLoginCheck.equals(v))
			setAutologinOption();
		else if (slideCheck.equals(v))
			setSlideOption();
		else if (logoutLayout.equals(v))
			logout();
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onErrorResponse(VolleyError e)
	{
		if (progDlg.isShowing()) progDlg.dismiss();
		e.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (progDlg.isShowing()) progDlg.dismiss();
		
		if (0 == response.length()) return;
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (REQUEST_LOGOUT == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				ServerRequestManager.IsLogin = false;
				SocialWalkRequest.ClearSessionInformation();
				
				SharedPreferences loginPrefs = this.getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = loginPrefs.edit();
				editor.putBoolean(Globals.PREF_KEY_AUTOLOGIN, false);
				editor.commit();
				
				finish();
			}
		}
		else if (REQUEST_ACCUMULATED_HEARTS == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				AccountHeart hearts = parser.GetAccumulatedHearts();
				if (null == hearts) return;
				
				TextView tvRedHearts = (TextView)findViewById(R.id.totalRedHearts);
				TextView tvGreenHearts = (TextView)findViewById(R.id.totalGreenHearts);
				tvRedHearts.setText(Utils.GetDefaultTool().DecimalNumberString(hearts.getRedPointTotal()));
				tvGreenHearts.setText(Utils.GetDefaultTool().DecimalNumberString(hearts.getGreenPoint()));
				
				heartsLayout.setVisibility(View.VISIBLE);
			}
			else
			{
				heartsLayout.setVisibility(View.INVISIBLE);
			}
		}
	}

}
