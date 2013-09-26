package com.socialwalk.request;


import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.socialwalk.Globals;
import com.socialwalk.MyXmlParser;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.MyXmlWriter;

public class ServerRequestManager implements Response.Listener<String>, Response.ErrorListener
{
	public static boolean IsLogin = false;
	private static AccountData LoginAccount = null;
	private static final String TAG = "SW-NET";

	public ServerRequestManager()
	{
		if (null == LoginAccount)
			LoginAccount = new AccountData();
	}

	public void Login(Response.Listener<String> listener, Response.ErrorListener errorListener, String userId, String password)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;

		final String xmlBody = MyXmlWriter.Login(userId, password);
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/login";
		
		SocialWalkRequest req = new SocialWalkRequest(Method.POST, urlString, listener, errorListener)
		{
			@Override
			public byte[] getBody() throws AuthFailureError
			{
				return xmlBody.getBytes();
			}			
		};
		reqQueue.add(req);
		
		IsLogin = true;
	}
	
	public void Logout(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/logout";
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);
	}

	
	public void AutoLogin(Context context)
	{
		if (IsLogin) return;
		
		SharedPreferences loginPrefs = context.getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
		
		boolean auto = loginPrefs.getBoolean(Globals.PREF_KEY_AUTOLOGIN, false);
		if (auto)
		{
			String uid = loginPrefs.getString(Globals.PREF_KEY_USER_ID, "");
			String pwd = loginPrefs.getString(Globals.PREF_KEY_PASSWORD, "");
			
			this.Login(this, this, uid, pwd);
		}
	}
	
	public void AccountData(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/users/" + LoginAccount.getUserSequence();

		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);
	}
	
	public void ChangePassword(Response.Listener<String> listener, Response.ErrorListener errorListener, String newPassword)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		
		final String xmlBody = MyXmlWriter.ChangePassword(LoginAccount.getUserPassword(), newPassword);

		String urlString = Globals.URL_SERVER_DOMAIN + "/users/" + LoginAccount.getUserSequence() + "/user_pw";
		
		SocialWalkRequest req = new SocialWalkRequest(Method.PUT, urlString, listener, errorListener)
		{
			@Override
			public byte[] getBody() throws AuthFailureError
			{
				return xmlBody.getBytes();
			}			
		};
		
		reqQueue.add(req);
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
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		
		String url = Globals.URL_NEO_AD;

		StringRequest req = new StringRequest(Method.GET, url, listener, errorListener);
		queue.add(req);
	}

	
	
	public void AroundersItems(Response.Listener<String> listener, Response.ErrorListener errorListener, double latitude, double longitude)
	{
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		
		final double mt = latitude;
		final double mn = longitude;
		
		String url = Globals.URL_AROUNDERS_REQ_TEST;
		StringRequest req = new StringRequest(Method.POST, url, listener, errorListener)
		{
			@Override
			protected Map<String, String> getParams() throws AuthFailureError
			{
				Map<String, String>  params = new HashMap<String, String>();  
	            params.put("m", Globals.AROUNDERS_APP_KEY);  
	            params.put("k", Globals.AROUNDERS_API_KEY_TEST);
	            params.put("mt", Double.toString(mt));
	            params.put("mn", Double.toString(mn));
	            params.put("n", "3");
				return params;
			}			
		};
		
		queue.add(req);
	}
	
	
	@Override
	public void onErrorResponse(VolleyError error)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResponse(String response)
	{
		SWResponse serverResponse = new MyXmlParser(response).GetResponse();
		if (0 == serverResponse.Code)
			IsLogin = true;
		
	}
	

	

	

}
