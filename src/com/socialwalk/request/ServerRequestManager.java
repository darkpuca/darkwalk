package com.socialwalk.request;


import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
	public static boolean IsLogin = true;
	public static AccountData LoginAccount = null;
	
	private static final String TAG = "SW-NET";
	private static final int REQUEST_AUTOLOGIN = 1;
	
	private int m_reqType = 0;

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
		
		SocialWalkRequest req = new SocialWalkRequest(Method.POST, urlString, listener, errorListener);
		req.SetXMLBody(xmlBody);
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
			
			m_reqType = REQUEST_AUTOLOGIN;
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
	
	public void CommunitySearch(Response.Listener<String> listener, Response.ErrorListener errorListener, String keyword, int pageIndex)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		if (null == keyword || 0 == keyword.length()) return;
		
		int pageSize = 10;
		String utfKeyword = null;
		
		try
		{
			utfKeyword = URLEncoder.encode(new String(keyword.getBytes("UTF-8")));
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getLocalizedMessage());
			return;
		}
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community/search/" + 
		LoginAccount.getUserSequence() + "/" + utfKeyword + "/page/" + pageIndex + "/" + pageSize;
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}
	
	public void CommunityDetail(Response.Listener<String> listener, Response.ErrorListener errorListener, int communityId)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community/" + communityId;
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}
	
	public void CommunityPosts(Response.Listener<String> listener, Response.ErrorListener errorListener, int communityId, int pageIndex)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		int pageSize = 10;
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community_info/" + communityId + "/page/" + pageIndex + "/" + pageSize;
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}
	
	public void CommunityPosting(Response.Listener<String> listener, Response.ErrorListener errorListener, int communityId, String contents)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community_info/" + communityId;
		String xmlBody = MyXmlWriter.CommunityPosting(LoginAccount.getUserSequence(), communityId, contents);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.POST, urlString, listener, errorListener);
		req.SetXMLBody(xmlBody);
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
	
	public void IsExistGroupName(Response.Listener<String> listener, Response.ErrorListener errorListener, String groupName)
	{
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		
		String url = Globals.URL_SERVER_DOMAIN + "/api/check/community/community_name/" + groupName;

		SocialWalkRequest req = new SocialWalkRequest(Method.GET, url, listener, errorListener);
		queue.add(req);
	}
	
	public void CreateGroup(Response.Listener<String> listener, Response.ErrorListener errorListener, String name, String desc)
	{
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		if (null == LoginAccount) return;
		
		String url = Globals.URL_SERVER_DOMAIN + "/api/community";
		String xmlBody = MyXmlWriter.CreateGroup(LoginAccount.getUserSequence(), name, desc);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.POST, url, listener, errorListener);
		req.SetXMLBody(xmlBody);
		
		queue.add(req);
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
	
	public static void BuildLoginAccountFromSessionData()
	{
		if (!IsLogin) return;
		
		String sessionData = SocialWalkRequest.GetSessionData();
		if (null == sessionData) return;
		
		MyXmlParser parser = new MyXmlParser(sessionData);
		LoginAccount = parser.GetAccountData();
	}
	
	
	@Override
	public void onErrorResponse(VolleyError error)
	{
		error.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (0 == response.length()) return;
		SWResponse result = new MyXmlParser(response).GetResponse();
		if (null == result) return;
		
		if (Globals.ERROR_NONE == result.Code)
		{
			IsLogin = true;
			BuildLoginAccountFromSessionData();
		}		
	}
	

	

	

}
