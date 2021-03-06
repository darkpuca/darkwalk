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
import com.socialwalk.dataclass.AccountData;
import com.socialwalk.dataclass.AccountHeart;
import com.socialwalk.dataclass.WalkHistory;

public class ServerRequestManager implements Response.Listener<String>, Response.ErrorListener
{
	public static boolean IsLogin = false;
	public static AccountData LoginAccount = null;
	
	private static final String TAG = "SW-NET";
	
	private static final int REQUEST_TYPE_AUTOLOGIN = 1;
	private static final int REQUEST_TYPE_HEARTS = 2;
	
	private int m_reqType = 0;
	private ServerRequestListener listener = null;
	
	public interface ServerRequestListener
	{
		void onFinishAutoLogin(boolean isLogin);
	}

	public ServerRequestManager()
	{
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
	}
	
	public void Logout(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/logout";
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);
	}

	public boolean AutoLogin(Context context, ServerRequestListener listener)
	{
		if (true == IsLogin) return true;
		
		this.listener = listener;
		
		SharedPreferences loginPrefs = context.getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
		
		boolean auto = loginPrefs.getBoolean(Globals.PREF_KEY_AUTOLOGIN, false);
		if (auto)
		{
			Log.d("DEBUG", "auto-login start");
			String uid = loginPrefs.getString(Globals.PREF_KEY_USER_ID, "");
			String pwd = loginPrefs.getString(Globals.PREF_KEY_PASSWORD, "");
			
			m_reqType = REQUEST_TYPE_AUTOLOGIN;
			this.Login(this, this, uid, pwd);
		}
		
		return auto;
	}
	
	public void AccountData(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/users/" + LoginAccount.Sequence;

		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);
	}
	
	public void CommunityGroups(Response.Listener<String> listener, Response.ErrorListener errorListener, int pageIndex)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		int pageSize = 10;
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community/" + LoginAccount.Sequence + "/page/" + pageIndex + "/" + pageSize;
		
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
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community/search/" + LoginAccount.Sequence + "/" + utfKeyword + "/page/" + pageIndex + "/" + pageSize;
		
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
	
	public void CommunityModify(Response.Listener<String> listener, Response.ErrorListener errorListener, int communityId, String name, String description)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community/" + communityId;
		String xmlBody = MyXmlWriter.CommunityModify(name, description);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.PUT, urlString, listener, errorListener);
		req.SetXMLBody(xmlBody);
		reqQueue.add(req);		

	}
	
	public void CommunityDelete(Response.Listener<String> listener, Response.ErrorListener errorListener, int communityId)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community/" + communityId;
		SocialWalkRequest req = new SocialWalkRequest(Method.DELETE, urlString, listener, errorListener);
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
	
	public void CommunityPostDetail(Response.Listener<String> listener, Response.ErrorListener errorListener, int postSequence)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community_info/" + postSequence;
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}
	
	public void CommunityPosting(Response.Listener<String> listener, Response.ErrorListener errorListener, int communityId, String contents)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community_info/" + communityId;
		String xmlBody = MyXmlWriter.CommunityPosting(LoginAccount.Sequence, communityId, contents);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.POST, urlString, listener, errorListener);
		req.SetXMLBody(xmlBody);
		reqQueue.add(req);		
	}
	
	public void CommunityPostModify(Response.Listener<String> listener, Response.ErrorListener errorListener, int postSeq, String contents)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community_info/" + postSeq;
		String xmlBody = MyXmlWriter.CommunityPostModify(contents);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.PUT, urlString, listener, errorListener);
		req.SetXMLBody(xmlBody);
		reqQueue.add(req);		
	}
	
	public void CommunityPostDelete(Response.Listener<String> listener, Response.ErrorListener errorListener, int postSequence)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community_info/" + postSequence;
		SocialWalkRequest req = new SocialWalkRequest(Method.DELETE, urlString, listener, errorListener);
		reqQueue.add(req);		
	}

	
	public void CommunityReplies(Response.Listener<String> listener, Response.ErrorListener errorListener, int postSequence, int pageIndex)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		int pageSize = 10;
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/reply_info/" + postSequence + "/page/" + pageIndex + "/" + pageSize;
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);
	}
	
	public void CommunityMembers(Response.Listener<String> listener, Response.ErrorListener errorListener, int commSeq, int pageIndex, boolean isApplicant)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		int type = isApplicant ? 0 : 1;
		
		int pageSize = 10;
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community_member/" + commSeq + "/" + type + "/page/" + pageIndex + "/" + pageSize;
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);
	}

	
	public void ReplyPosting(Response.Listener<String> listener, Response.ErrorListener errorListener, int postSequence, String contents)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/reply_info/" + postSequence;
		String xmlBody = MyXmlWriter.ReplyPosting(LoginAccount.Sequence, postSequence, contents);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.POST, urlString, listener, errorListener);
		req.SetXMLBody(xmlBody);
		reqQueue.add(req);		
	}
	
	public void ReplyDelete(Response.Listener<String> listener, Response.ErrorListener errorListener, int replySequence)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/reply_info/" + replySequence;
		SocialWalkRequest req = new SocialWalkRequest(Method.DELETE, urlString, listener, errorListener);
		reqQueue.add(req);		
	}

	public void BeneficiarySummary(Response.Listener<String> listener, Response.ErrorListener errorListener, boolean isGlobal)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String target = isGlobal ? "all" : "local";
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/benefit_group/graph/" + target;
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}

	public void Beneficiaries(Response.Listener<String> listener, Response.ErrorListener errorListener, boolean isGlobal, int pageIndex)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		int pageSize = 10;
		String target = isGlobal ? "all" : "local";
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/benefit/" + target + "/page/" + pageIndex + "/" + pageSize;
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}
	
	public void BeneficiaryDetail(Response.Listener<String> listener, Response.ErrorListener errorListener, String sequence)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/benefit/" + sequence;
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}
	
	public void UpdateHearts(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/users/" + LoginAccount.Sequence + "/point";
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}
	
	public void ChangePassword(Response.Listener<String> listener, Response.ErrorListener errorListener, String newPassword)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/users/" + LoginAccount.Sequence + "/user_pw";

		final String xmlBody = MyXmlWriter.ChangePassword(LoginAccount.Password, newPassword);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.PUT, urlString, listener, errorListener);
		req.SetXMLBody(xmlBody);

		reqQueue.add(req);
	}
	
	
	public void CommunityJoin(Response.Listener<String> listener, Response.ErrorListener errorListener, int communitySeq, String message)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community_member/" + communitySeq;
		String xmlBody = MyXmlWriter.CommunityJoin(LoginAccount.Sequence, message);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.POST, urlString, listener, errorListener);
		req.SetXMLBody(xmlBody);
		reqQueue.add(req);		
	}

	public void CommunitySecession(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;

		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community_member/" + LoginAccount.Sequence;

		SocialWalkRequest req = new SocialWalkRequest(Method.DELETE, urlString, listener, errorListener);
		reqQueue.add(req);		
	}

	public void IsExistCommunityName(Response.Listener<String> listener, Response.ErrorListener errorListener, String name)
	{
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		
		String utfVal = null;
		try
		{
			utfVal = URLEncoder.encode(new String(name.getBytes("UTF-8")));
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getLocalizedMessage());
			return;
		}

//		String url = Globals.URL_SERVER_DOMAIN + "/api/check/community/community_name/" + utfVal + "/0";
		String url = Globals.URL_SERVER_DOMAIN + "/api/search/community_name/" + utfVal + "/page/1/1";

		SocialWalkRequest req = new SocialWalkRequest(Method.GET, url, listener, errorListener);
		queue.add(req);
	}
	
	public void CreateGroup(Response.Listener<String> listener, Response.ErrorListener errorListener, String name, String desc)
	{
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		if (null == LoginAccount) return;
		
		String url = Globals.URL_SERVER_DOMAIN + "/api/community";
		String xmlBody = MyXmlWriter.CommunityAdd(LoginAccount.Sequence, name, desc);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.POST, url, listener, errorListener);
		req.SetXMLBody(xmlBody);
		
		queue.add(req);
	}
	
	
	
	public void UpdateNeoClickItems(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		
//		String url = Globals.URL_NEO_AD;
		String url = Globals.URL_ROBINHOOT_AD2;

		StringRequest req = new StringRequest(Method.GET, url, listener, errorListener);
		queue.add(req);
	}
	
	
	public void AroundersItems(Response.Listener<String> listener, Response.ErrorListener errorListener, double latitude, double longitude)
	{
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		
		final double mt = latitude;
		final double mn = longitude;
		
		
		String url = Globals.URL_AROUNDERS_REQ;
		StringRequest req = new StringRequest(Method.POST, url, listener, errorListener)
		{
			@Override
			protected Map<String, String> getParams() throws AuthFailureError
			{
				Map<String, String>  params = new HashMap<String, String>();  
	            params.put("m", Globals.AROUNDERS_APP_KEY);  
	            params.put("k", Globals.AROUNDERS_API_KEY);
	            params.put("mt", Double.toString(mt));
	            params.put("mn", Double.toString(mn));
	            params.put("r", "2000");
	            params.put("n", Integer.toString(Globals.AROUNDERS_AD_SIZE));
				return params;
			}			
		};
		
		queue.add(req);
	}
	
	
	public void AccumulateHeart(Response.Listener<String> listener, Response.ErrorListener errorListener, String adType, String adSequence, int point)
	{
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		if (null == LoginAccount) return;
		
		String url = Globals.URL_SERVER_DOMAIN + "/api/ad_info/" + adType;
		String xmlBody = MyXmlWriter.AccumulateHeart(LoginAccount.Sequence, adSequence, point);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.POST, url, listener, errorListener);
		req.SetXMLBody(xmlBody);
		
		queue.add(req);
	}
	
	public void WalkResult(Response.Listener<String> listener, Response.ErrorListener errorListener, WalkHistory history)
	{
		if (null == history) return;
		
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		if (null == LoginAccount) return;
		
		String url = Globals.URL_SERVER_DOMAIN + "/api/benefit_history";
		String xmlBody = MyXmlWriter.WalkResult(LoginAccount.Sequence, history);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.POST, url, listener, errorListener);
		req.SetXMLBody(xmlBody);
		
		queue.add(req);
	}
	
	public void GetUserProfile(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/users/" + LoginAccount.Sequence;
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}

	public void CheckEmail(Response.Listener<String> listener, Response.ErrorListener errorListener, String email)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/users/" + LoginAccount.Sequence;
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}
	
	public void AreaItems(Response.Listener<String> listener, Response.ErrorListener errorListener, int parentCode)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String urlString;
		if (0 == parentCode)
			urlString = Globals.URL_SERVER_DOMAIN + "/api/first_code/" + (LoginAccount.IsGroupUser ? 1:0);
		else
			urlString = Globals.URL_SERVER_DOMAIN + "/api/second_code/" + (LoginAccount.IsGroupUser ? 1:0) + "/" + parentCode;
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}
	
	public void UpdateProfile(Response.Listener<String> listener, Response.ErrorListener errorListener, AccountData profile)
	{
		if (null == profile) return;
		
		RequestQueue queue = RequestManager.getRequestQueue();
		if (null == queue) return;
		if (null == LoginAccount) return;
		
		String url = Globals.URL_SERVER_DOMAIN + "/api/users/" + LoginAccount.Sequence;
		String xmlBody = MyXmlWriter.UpdateProfile(profile);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.PUT, url, listener, errorListener);
		req.SetXMLBody(xmlBody);
		
		queue.add(req);
	}
	
	public void UserSecessionPassword(Response.Listener<String> listener, Response.ErrorListener errorListener, String password)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/check/users/" + LoginAccount.Sequence + "/user_pw/" + password; 
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}

	public void UserSecession(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/users/" + LoginAccount.Sequence; 
		
		SocialWalkRequest req = new SocialWalkRequest(Method.DELETE, urlString, listener, errorListener);
		reqQueue.add(req);		
	}

	public void Rankings(Response.Listener<String> listener, Response.ErrorListener errorListener, boolean isGlobal)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String target = isGlobal ? "all" : "local";
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/rank/" + target;
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}
	
	public void TotalAccumulatedHearts(Response.Listener<String> listener, Response.ErrorListener errorListener)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/users/" + LoginAccount.Sequence + "/point/total";
		
		SocialWalkRequest req = new SocialWalkRequest(Method.GET, urlString, listener, errorListener);
		reqQueue.add(req);		
	}

	public void CommunityMemberAllow(Response.Listener<String> listener, Response.ErrorListener errorListener, int communitySequence, String memberSequence, boolean allow)
	{
		RequestQueue reqQueue = RequestManager.getRequestQueue();
		if (null == reqQueue) return;
		if (null == LoginAccount) return;
		
		String urlString = Globals.URL_SERVER_DOMAIN + "/api/community_member/" + communitySequence;
		String xmlBody = MyXmlWriter.CommunityMemberAllow(memberSequence, allow ? 1 : 3);
		
		SocialWalkRequest req = new SocialWalkRequest(Method.PUT, urlString, listener, errorListener);
		req.SetXMLBody(xmlBody);
		
		reqQueue.add(req);		
	}
	
	@Override
	public void onErrorResponse(VolleyError error)
	{
		if (null != listener)
			listener.onFinishAutoLogin(false);
		
		error.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (0 == response.length()) return;
		SWResponse result = new MyXmlParser(response).GetResponse();
		if (null == result) return;
		
		if (REQUEST_TYPE_AUTOLOGIN == m_reqType)
		{
			m_reqType = 0;
			
			if (Globals.ERROR_NONE == result.Code)
			{
				IsLogin = true;
				BuildLoginAccountFromSessionData();
				
				m_reqType = REQUEST_TYPE_HEARTS;
				UpdateHearts(this, this);
			}
			else
			{
				if (null != listener)
					listener.onFinishAutoLogin(IsLogin);
			}
		}
		else if (REQUEST_TYPE_HEARTS == m_reqType)
		{
			m_reqType = 0;
			if (Globals.ERROR_NONE == result.Code)
			{
				if (null == LoginAccount) return;
				
				MyXmlParser parser = new MyXmlParser(response);
				AccountHeart hearts = parser.GetHearts();
				if (null == hearts) return;
				LoginAccount.Hearts.Copy(hearts);
			}
			
			if (null != listener)
				listener.onFinishAutoLogin(IsLogin);
		}
	}

	
	public static void BuildLoginAccountFromSessionData()
	{
		if (false == IsLogin) return;
		
		String sessionData = SocialWalkRequest.GetSessionData();
		if (null == sessionData) return;
		
		MyXmlParser parser = new MyXmlParser(sessionData);
		LoginAccount = parser.GetAccountData();
	}
	
	

}
