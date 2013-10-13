package com.socialwalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.AccountHeart;
import com.socialwalk.request.ServerRequestManager;

public class LoginActivity extends Activity
implements View.OnClickListener, Response.Listener<String>, Response.ErrorListener
{
	SharedPreferences loginPrefs;
	EditText txtUid, txtPwd;
	Button btnLogin;
	CheckBox chkAuto;
	
	private ServerRequestManager m_server = null;
	private int reqType;
	
	private static final int REQUEST_LOGIN = 100;
	private static final int REQUEST_HEARTS = 101;

	private static final String TAG = "SW-LOGIN";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		m_server = new ServerRequestManager();		
		
		loginPrefs = getSharedPreferences(Globals.PREF_NAME_LOGIN, MODE_PRIVATE);

		txtUid = (EditText)findViewById(R.id.loginUid);
		txtPwd = (EditText)findViewById(R.id.loginPwd);
		chkAuto = (CheckBox)findViewById(R.id.chkAutoLogin);
		btnLogin = (Button)findViewById(R.id.btnLogin);

		// sample value
		txtUid.setText("kms7610@gmail.com");
		txtPwd.setText("jinsuyeon0701");
		chkAuto.setChecked(false);
		
		Button btnSignUp = (Button)findViewById(R.id.btnSignUp);
		Button btnForgot = (Button)findViewById(R.id.btnFindPwd);
		
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
		
		SpannableString content = new SpannableString(getResources().getString(R.string.FIND_PASSWORD));
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		btnForgot.setText(content);
		
		btnLogin.setOnClickListener(this);
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

		reqType = REQUEST_LOGIN;
		m_server.Login(this, this, uid, pwd);
	}

	@Override
	public void onErrorResponse(VolleyError error)
	{
		Log.e(TAG, error.getLocalizedMessage());
	}

	@Override
	public void onResponse(String response)
	{
		if (0 == response.length()) return;
		
		MyXmlParser parser = new MyXmlParser(response);
		
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (REQUEST_LOGIN == reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				ServerRequestManager.IsLogin = true;
				ServerRequestManager.BuildLoginAccountFromSessionData();

				Utils.SaveLoginParams(this, chkAuto.isChecked(), txtUid.getText().toString(), txtPwd.getText().toString());
				
				reqType = REQUEST_HEARTS;
				m_server.UpdateHearts(this, this);
			}
			else if (Globals.ERROR_NO_RESULT == result.Code)
			{
				new AlertDialog.Builder(this)
				.setTitle(R.string.TITLE_INFORMATION)
				.setMessage(R.string.MSG_ACCOUNT_NOT_EXIST)
				.setNeutralButton(R.string.CLOSE, null)
				.show();
			}
		}
		else if (REQUEST_HEARTS == reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				AccountHeart hearts = parser.GetHearts();
				if (null == hearts) return;
				ServerRequestManager.LoginAccount.Hearts.Copy(hearts);
			}
			
			setResult(RESULT_OK);
			finish();
		}

	}
}
