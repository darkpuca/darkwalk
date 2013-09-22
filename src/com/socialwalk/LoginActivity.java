package com.socialwalk;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;

public class LoginActivity extends Activity 
implements View.OnClickListener, Response.Listener<String>, Response.ErrorListener
{
	SharedPreferences loginPrefs;
	EditText txtUid, txtPwd;
	Button btnLogin;
	CheckBox chkAuto;
	
	private ServerRequestManager m_reqManager = null;
	
	private static final String TAG = "SW-LOGIN";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		m_reqManager = new ServerRequestManager(this);		
		
		loginPrefs = getSharedPreferences(Globals.PREF_NAME_LOGIN, MODE_PRIVATE);

		txtUid = (EditText)findViewById(R.id.loginUid);
		txtPwd = (EditText)findViewById(R.id.loginPwd);
		txtUid.setText("abc");
		txtPwd.setText("def");
		
		btnLogin = (Button)findViewById(R.id.btnLogin);
		Button btnSignUp = (Button)findViewById(R.id.btnSignUp);
		Button btnForgot = (Button)findViewById(R.id.btnFindPwd);
		
		chkAuto = (CheckBox)findViewById(R.id.chkAutoLogin);
		
		btnSignUp.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_SIGN_UP);
				startActivity(i);				
			}
		});
		
		btnForgot.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_FORGET_PASSWORD);
				startActivity(i);
			}
		});
		
		btnLogin.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public void onClick(View v)
	{
		if (0 == txtUid.getText().length())
		{
			new AlertDialog.Builder(this).setTitle(R.string.TITLE_INFORMATION).setMessage(R.string.MSG_EMPTY_USERID).setNeutralButton(R.string.CLOSE, null).show();
			return;
		}
		
		if (0 == txtPwd.getText().length())
		{
			new AlertDialog.Builder(this).setTitle(R.string.TITLE_INFORMATION).setMessage(R.string.MSG_EMPTY_USERID).setNeutralButton(R.string.CLOSE, null).show();
		}
		
		String uid = txtUid.getText().toString();
		String pwd = txtPwd.getText().toString();

		m_reqManager.Login(this, this, uid, pwd);
	}

	@Override
	public void onErrorResponse(VolleyError error)
	{
		Log.e(TAG, error.getLocalizedMessage());
	}

	@Override
	public void onResponse(String response)
	{
		Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
		
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse swResponse = parser.GetResponse();
		
		if (swResponse.Code.equalsIgnoreCase("LOG1200"))
		{
			ServerRequestManager.IsLogin = true;
			m_reqManager.SaveLoginParams(chkAuto.isChecked(), txtUid.getText().toString(), txtPwd.getText().toString());
			setResult(RESULT_OK);
			this.finish();
		}
		else
		{
			Toast.makeText(this, swResponse.Message, Toast.LENGTH_SHORT).show();			
		}
	}
}
