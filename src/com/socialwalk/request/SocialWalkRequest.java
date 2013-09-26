package com.socialwalk.request;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class SocialWalkRequest extends StringRequest
{
	private static final String TAG = "VOLLEY";
	private static final String AES_KEY = "";
	
	private static String SessionKey = "";
	private static String SessionData = "";
	
	public SocialWalkRequest(int method, String url, Listener<String> listener, ErrorListener errorListener)
	{
		super(method, url, listener, errorListener);
	}

	
	@Override
	public Map<String, String> getHeaders() throws AuthFailureError
	{
		Map<String, String> headers = new HashMap<String, String>();
		
		if (!SessionKey.isEmpty())
			headers.put("Session-key", SessionKey);
		
		if (!SessionData.isEmpty())
			headers.put("Session-data", SessionData);
		
		headers.put("Secure-flag", Integer.toString(0));
		
		return headers;
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response)
	{
		ReadSessionInformation(response.headers);

		return super.parseNetworkResponse(response);
	}

	private void ReadSessionInformation(Map<String,String> headers)
	{
		if (SessionData.isEmpty())
		{
			if (headers.containsKey("Session-data "))
				SessionData = headers.get("Session-data ");
			else if (headers.containsKey("Session-data"))
				SessionData = headers.get("Session-data");
		}

		if (SessionKey.isEmpty())
		{
			if (headers.containsKey("Session-key "))
				SessionKey = headers.get("Session-key ");
			else if (headers.containsKey("Session-key"))
				SessionKey = headers.get("Session-key");			
		}		
	}

	@Override
	protected void deliverResponse(String response) 
	{
		Log.d(TAG, response);		
		super.deliverResponse(response);
	}

	
	public static void ClearSessionInformation()
	{
		SessionData = "";
		SessionKey = "";
	}
	
	public static Map<String, String> GetSessionInformation()
	{
		if (SessionData.isEmpty()) return null;
		
		
		Map<String, String> infoMap = new HashMap<String, String>();
		
		return infoMap;
	}
	
}
