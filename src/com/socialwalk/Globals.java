package com.socialwalk;

import java.util.Date;



public class Globals
{
	public static final String[] sampleStrings = { "sample string1", "sample string2", "sample string3", "sample string4", "sample string5", "sample string6", "sample string7", "sample string8", "sample string9" };

	public static final String URL_SERVER_DOMAIN = "http://218.145.31.240";
	public static final String URL_SIGN_UP = URL_SERVER_DOMAIN + "/mobile/join_step1.php";
	public static final String URL_FORGET_PASSWORD = URL_SERVER_DOMAIN + "/mobile/findpw.php";
	public static final String URL_NOTICES = URL_SERVER_DOMAIN + "/mobile/notice.php";
	public static final String URL_HELP = URL_SERVER_DOMAIN + "/mobile/qna.php";
	public static final String URL_ABOUT = URL_SERVER_DOMAIN + "/mobile/socialwalk.php";
	public static final String URL_SPONSOR = URL_SERVER_DOMAIN + "/mobile/sponsor.php";
	public static final String URL_VOLUNTEERS = URL_SERVER_DOMAIN + "/mobile/socialwalk.group.php";
	public static final String URL_CONTECT_US = URL_SERVER_DOMAIN + "/mobile/contact.php";
	
	
	public static final String URL_NEO_AD_DOMAIN = "http://web.howapi.co.kr";
	public static final String URL_NEO_AD = URL_NEO_AD_DOMAIN + "/zzzCashSlide.asp?tp=1";

	public static final String URL_AROUNDERS_REQ_TEST = "http://test.arounders.co.kr/adRequest.skp";
	public static final String AROUNDERS_REQ_URL = "http://ad.arounders.co.kr/adRequest.skp";
	public static final String AROUNDERS_API_KEY_TEST = "QNN6";
	public static final String AROUNDERS_APP_KEY = "123";

	public static final String EXTRA_KEY_URL = "url";
	public static final String EXTRA_KEY_COMMUNITY_ID = "group_id";
	public static final String EXTRA_KEY_COMMUNITY_NAME = "group_name";
	public static final String EXTRA_KEY_COMMUNITY_DESC = "group_desc";
	
	public static final String EXTRA_KEY_LOCATION = "location";
	public static final String EXTRA_KEY_FILENAME = "filename";

	public static final String PREF_ENVIRNMENT = "ENV";
	public static final String PREF_NAME_SLIDER = "SLIDER";
	public static final String PREF_NAME_LOGIN = "LOGIN";
	
	public static final String PREF_KEY_AUTOLOGIN = "AUTO_LOGIN";
	public static final String PREF_KEY_USER_ID = "USER_ID";
	public static final String PREF_KEY_PASSWORD = "PASSWORD";
	
	public static final String DATE_FORMAT_FOR_HISTORY = "yyyyMMddHHmmss";
	public static final String DATE_FORMAT_FOR_SERVER = "yyyy-MM-dd HH:mm:ss";
	
	public static final int NOTI_WALKING = 2000;
	
	public static final int INTENT_REQ_LOGIN = 10;
	public static final int INTENT_REQ_GROUP_SELECT = 11;
	public static final int INTENT_REQ_INTRO = 12;
	public static final int INTENT_REQ_POSTING = 13;

	public static final long INTRO_WAITING = 3000;
	
	public static final int ERROR_NONE					= 0;
	public static final int ERROR_NO_RESULT				= 1;
	public static final int ERROR_EXISTED_ACCOUNT		= 62;
	public static final int ERROR_SECEDED_ACCOUNT		= 63;
	public static final int ERROR_SESSION_DATA			= 93;
	public static final int ERROR_SESSION_EXPIRED		= 94;
	public static final int ERROR_PASSWORD_NOT_MATCH	= 96;
	public static final int ERROR_SYNTAX				= 98;
	public static final int ERROR_UNKNOWN				= 99;
	
	private Globals() {
		// TODO Auto-generated constructor stub
	}

	
	


}

