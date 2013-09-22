package com.socialwalk;



public class Globals
{
	public static final String[] sampleStrings = { "sample string1", "sample string2", "sample string3", "sample string4", "sample string5", "sample string6", "sample string7", "sample string8", "sample string9" };

	public static final String URL_HOME_DOMAIN = "http://218.145.31.240";
	public static final String URL_HOME = URL_HOME_DOMAIN + "";
	public static final String URL_LOGIN = URL_HOME_DOMAIN + "/api/login";
	public static final String URL_SIGN_UP = URL_HOME_DOMAIN + "/mobile/join_step1.php";
	public static final String URL_FORGET_PASSWORD = URL_HOME_DOMAIN + "/mobile/findpw.php";
	public static final String URL_NOTICES = URL_HOME_DOMAIN + "/mobile/notice.php";
	public static final String URL_HELP = URL_HOME_DOMAIN + "/mobile/qna.php";
	public static final String URL_ABOUT = URL_HOME_DOMAIN + "/mobile/socialwalk.php";
	public static final String URL_SPONSOR = URL_HOME_DOMAIN + "/mobile/sponsor.php";
	public static final String URL_VOLUNTEERS = URL_HOME_DOMAIN + "/mobile/socialwalk.group.php";
	public static final String URL_CONTECT_US = URL_HOME_DOMAIN + "/mobile/contact.php";
	
	
	public static final String URL_NEO_AD_DOMAIN = "http://web.howapi.co.kr";
	public static final String URL_NEO_AD = URL_NEO_AD_DOMAIN + "/zzzCashSlide.asp?tp=1";

	public static final String URL_AROUNDERS_REQ_TEST = "http://test.arounders.co.kr/adRequest.skp";
	public static final String AROUNDERS_REQ_URL = "http://ad.arounders.co.kr/adRequest.skp";
	public static final String AROUNDERS_API_KEY_TEST = "QNN6";
	public static final String AROUNDERS_APP_KEY = "123";
	


	public static final String EXTRA_KEY_URL = "url";
	public static final String EXTRA_KEY_GROUP_NAME = "group_name";
	public static final String EXTRA_KEY_GROUP_ID = "group_id";
	public static final String EXTRA_KEY_LOCATION = "location";
	public static final String EXTRA_KEY_FILENAME = "filename";

	public static final String PREF_ENVIRNMENT = "ENV";
	public static final String PREF_NAME_SLIDER = "SLIDER";
	public static final String PREF_NAME_LOGIN = "LOGIN";
	
	public static final String PREF_KEY_AUTOLOGIN = "AUTO_LOGIN";
	public static final String PREF_KEY_USER_ID = "USER_ID";
	public static final String PREF_KEY_PASSWORD = "PASSWORD";
	
	public static final String XML_DATE_FORMAT = "yyyyMMddHHmmss";
	
	public static final int NOTI_WALKING = 2000;
	
	public static final int INTENT_REQ_LOGIN = 10;
	public static final int INTENT_REQ_GROUP_SELECT = 11;
	public static final int INTENT_REQ_INTRO = 12;

	public static final long INTRO_WAITING = 1000;
	
	private Globals() {
		// TODO Auto-generated constructor stub
	}


}

