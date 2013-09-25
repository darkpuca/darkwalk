package com.socialwalk.request;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class SocialWalkRequest extends StringRequest {

	public SocialWalkRequest(String url, Listener<String> listener, ErrorListener errorListener)
	{
		super(url, listener, errorListener);
	}

	public SocialWalkRequest(int method, String url, Listener<String> listener, ErrorListener errorListener)
	{
		super(method, url, listener, errorListener);

	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response)
	{
		Log.d("VOLLEY", response.headers.toString());
		return super.parseNetworkResponse(response);
		
	}

	@Override
	protected void deliverResponse(String response) {
		// TODO Auto-generated method stub
		super.deliverResponse(response);
	}

	
	
}
