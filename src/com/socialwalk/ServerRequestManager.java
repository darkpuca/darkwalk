package com.socialwalk;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;

public class ServerRequestManager
{
	public static boolean IsLogin = false;
	
	private RequestQueue m_requestQueue = null;
	private Context m_context = null;
	
	private static final String TAG = "SW-NET";

	public ServerRequestManager(Context context)
	{
		m_context = context;
		m_requestQueue = Volley.newRequestQueue(m_context);
	}
	
	public RequestQueue GetRequestQueue()
	{
		return m_requestQueue;
	}

	public void Login(Response.Listener<String> listener, Response.ErrorListener errorListener, String userId, String password)
	{
		// sample value
		userId = "kms7610@gmail.com";
		password = "jin090701";

		final String body = MyXmlWriter.Login(userId, password);
		String url = Globals.URL_LOGIN;
		
		StringRequest req = new StringRequest(Method.POST, url, listener, errorListener)
		{
			@Override
			public byte[] getBody() throws AuthFailureError
			{
				return body.getBytes();
			}			
		};
		m_requestQueue.add(req);
		
		IsLogin = true;
	}
	
	public void SaveLoginParams(boolean isAuto, String userId, String password)
	{
		SharedPreferences loginPrefs = m_context.getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = loginPrefs.edit();
		editor.putBoolean(Globals.PREF_KEY_AUTOLOGIN, isAuto);
		editor.putString(Globals.PREF_KEY_USER_ID, userId);
		editor.putString(Globals.PREF_KEY_PASSWORD, password);
		editor.commit();
	}
	
	public boolean AutoLogin(Context context)
	{
		if (IsLogin) return true;
		
		SharedPreferences loginPrefs = context.getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
		
		boolean auto = loginPrefs.getBoolean(Globals.PREF_KEY_AUTOLOGIN, false);
		if (auto)
		{
			String uid = loginPrefs.getString(Globals.PREF_KEY_USER_ID, "");
			String pwd = loginPrefs.getString(Globals.PREF_KEY_PASSWORD, "");
			
			//return this.Login(uid, pwd);
		}
		return false;
	}
	
	public boolean GroupJoin(String user_id, int group_id)
	{
		return true;
	}
	
	public boolean IsExistGroupName(String group_name)
	{
		if (group_name.equals("group"))
			return true;
		
		return false;
	}
	
	public boolean CreateGroup(String group_name)
	{
		return true;
	}
	
	
	
	public void NeoClickItem(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		String url = Globals.URL_NEO_AD;

		StringRequest req = new StringRequest(Method.GET, url, listener, errorListener);
		m_requestQueue.add(req);
	}
	
	
}
