package com.socialwalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.request.ServerRequestManager;
import com.socialwalk.request.SocialWalkRequest;

public class SettingsActivity extends Activity 
implements OnClickListener, Response.Listener<String>, Response.ErrorListener 
{
	private Button m_btnLogin, m_btnEditProfile;
	private ServerRequestManager m_server = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		m_server = new ServerRequestManager();
		
		m_btnLogin = (Button)findViewById(R.id.btnLogin);
		m_btnLogin.setOnClickListener(this);
		
		m_btnEditProfile = (Button)findViewById(R.id.btnProfile);
		m_btnEditProfile.setOnClickListener(this);
		
		UpdateProfiles();
	}

	private void UpdateProfiles()
	{
		if (ServerRequestManager.IsLogin)
			m_btnLogin.setText(R.string.LOGOUT);
		else
			m_btnLogin.setText(R.string.LOGIN);
		
		m_btnEditProfile.setEnabled(ServerRequestManager.IsLogin);
	}

	
	@Override
	public void onClick(View v)
	{
		if (m_btnLogin.equals(v))
		{
			if (ServerRequestManager.IsLogin)
			{
				AlertDialog.Builder dlg = new AlertDialog.Builder(this);
				dlg.setCancelable(true);
				dlg.setTitle(R.string.TITLE_INFORMATION);
				dlg.setMessage(R.string.MSG_LOGOUT_CONFIRM);
				dlg.setPositiveButton(R.string.CONTINUE, new DialogInterface.OnClickListener()
				{					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						m_server.Logout(SettingsActivity.this, SettingsActivity.this);
					}
				});
				dlg.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener()
				{					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
				dlg.show();
			}
			else
			{
	            Intent loginIntent = new Intent(this, LoginActivity.class);
	            startActivityForResult(loginIntent, Globals.INTENT_REQ_LOGIN);
			}
		}	
		else if (m_btnEditProfile.equals(v))
		{
			Intent i = new Intent(this, ProfileActivity.class);
			startActivity(i);
		}
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Globals.INTENT_REQ_LOGIN == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				UpdateProfiles();
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onErrorResponse(VolleyError error)
	{

	}

	@Override
	public void onResponse(String response)
	{
		SWResponse result = new MyXmlParser(response).GetResponse();
		if (null == result) return;
		
		if (0 == result.Code)
		{
			ServerRequestManager.IsLogin = false;
			SocialWalkRequest.ClearSessionInformation();
			
			UpdateProfiles();
		}
	}

}
