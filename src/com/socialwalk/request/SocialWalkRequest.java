package com.socialwalk.request;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.squareup.okhttp.internal.Base64;

public class SocialWalkRequest extends StringRequest
{
	private static final String TAG = "VOLLEY";
	private static final String AES_KEY = "1234567890abcdef";
	
	private static String SessionKey = "";
	private static String SessionData = "";
	
	private String m_xmlBody;
	
	public SocialWalkRequest(int method, String url, Listener<String> listener, ErrorListener errorListener)
	{
		super(method, url, listener, errorListener);
	}
	
	void SetXMLBody(String xmlBody)
	{
		this.m_xmlBody = xmlBody;
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
	
	@Override
	public byte[] getBody() throws AuthFailureError
	{
		if (null != m_xmlBody)
			return m_xmlBody.getBytes();
		
		return super.getBody();
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
	
	public static String GetSessionData()
	{
		try
		{
			SecretKeySpec skeySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] origin = cipher.doFinal(Base64.decode(SessionData.getBytes()));
			String originMessage = new String(origin);
//			Log.d("AES", originMessage);
			
			return originMessage;
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}catch (NoSuchPaddingException e) {
			Log.e("AES", e.getLocalizedMessage());
		}catch (InvalidKeyException e) {
			Log.e("AES", e.getLocalizedMessage());
		} catch (IllegalBlockSizeException e) {
			Log.e("AES", e.getLocalizedMessage());
		} catch (BadPaddingException e) {
			Log.e("AES", e.getLocalizedMessage());
		}

		return null;
	}
	
}
