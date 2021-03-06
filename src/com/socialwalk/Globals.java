package com.socialwalk;





public class Globals
{
	public static final int SECURE_SESSION_FLAG = 0;
	
	public static final String[] sampleStrings = { "sample string1", "sample string2", "sample string3", "sample string4", "sample string5", "sample string6", "sample string7", "sample string8", "sample string9" };

	public static final String URL_SERVER_DOMAIN = "http://socialwalk.or.kr";
//	public static final String URL_SERVER_DOMAIN = "http://218.145.31.240";
	public static final String URL_SIGN_UP = URL_SERVER_DOMAIN + "/mobile/join_step1.php";
	public static final String URL_FORGET_PASSWORD = URL_SERVER_DOMAIN + "/mobile/findpw.php";
	public static final String URL_NOTICES = URL_SERVER_DOMAIN + "/mobile/notice.php";
	public static final String URL_HELP = URL_SERVER_DOMAIN + "/mobile/qna.php";
	public static final String URL_INFORM = URL_SERVER_DOMAIN + "/mobile/socialwalk.link.php";
	public static final String URL_ABOUT = URL_SERVER_DOMAIN + "/mobile/socialwalk.php";
	public static final String URL_SPONSOR = URL_SERVER_DOMAIN + "/mobile/sponsor.php";
	public static final String URL_VOLUNTEERS = URL_SERVER_DOMAIN + "/mobile/socialwalk.group.php";
	public static final String URL_CONTACT_US = URL_SERVER_DOMAIN + "/mobile/contact.php";
	
	
	public static final String URL_NEO_AD_DOMAIN = "http://web.howapi.co.kr";
	public static final String URL_NEO_AD = URL_NEO_AD_DOMAIN + "/zzzCashSlide.asp?tp=1";
	public static final String URL_ROBINHOOT_AD = "http://ad.robinhoot.co.kr/app/robinad_xml.utf8.asp?part=socialwalk1&area=55&adcnt=5";
	public static final String URL_ROBINHOOT_AD2 = "http://ads.robinhoot.co.kr/?c=adg&part=socialwalk1&area=55&adcnt=5&out=xml";

	public static final String URL_AROUNDERS_REQ_TEST = "http://test.arounders.co.kr/adRequest.skp";
	public static final String URL_AROUNDERS_REQ = "http://ad.arounders.co.kr/adRequest.skp";
	public static final String AROUNDERS_API_KEY_TEST = "QNN6";
	public static final String AROUNDERS_API_KEY = "wwDAWe";
	public static final String AROUNDERS_APP_KEY = "123";
	public static final int AROUNDERS_AD_SIZE = 10;

	public static final String EXTRA_KEY_URL = "url";
	public static final String EXTRA_KEY_COMMUNITY_SEQUENCE = "communitySeq";
	public static final String EXTRA_KEY_COMMUNITY_NAME = "communityName";
	public static final String EXTRA_KEY_COMMUNITY_DESC = "communityDesc";
	public static final String EXTRA_KEY_LOCATION = "location";
	public static final String EXTRA_KEY_FILENAME = "filename";
	public static final String EXTRA_KEY_POST_SEQUENCE = "postSeq";
	public static final String EXTRA_KEY_POST_CONTENTS = "postContents";
	public static final String EXTRA_KEY_WRITER_SEQUENCE = "writerSeq";
	public static final String EXTRA_KEY_INTRO_AD_VISIT = "introAd";
	public static final String EXTRA_KEY_IS_GLOBAL_PROJECT = "isGlobal"; 
	public static final String EXTRA_KEY_IS_HISTORY_PROJECT = "isHistory"; 
	public static final String EXTRA_KEY_SEQUENCE = "seq";
	public static final String EXTRA_KEY_TEMP_RESTORE = "restore";
	
	public static final String PREF_ENVIRNMENT = "ENV";
	public static final String PREF_NAME_SLIDE = "SLIDE";
	public static final String PREF_NAME_LOGIN = "LOGIN";
	public static final String PREF_NAME_AD = "AD";
	
	public static final String PREF_KEY_AUTOLOGIN = "AUTO_LOGIN";
	public static final String PREF_KEY_USER_ID = "USER_ID";
	public static final String PREF_KEY_PASSWORD = "PASSWORD";
	public static final String PREF_KEY_SLIDE = "SLIDE";
	public static final String PREF_KEY_AROUDERS_TIME = "AROUNDERS_TIME";
	public static final String PREF_KEY_AROUNDERS_VISIT_CODES = "AROUNDERS_CODES";
	public static final String PREF_KEY_SLIDE_AD_HISTORIES = "SLIDE_HISTORIES";
	
	public static final String DATETIME_FORMAT_FOR_HISTORY = "yyyyMMddHHmmss";
	public static final String DATETIME_FORMAT_FOR_SERVER = "yyyy-MM-dd HH:mm:ss";
	public static final String DATETIME_FORMAT_FOR_UI = "yyyy-MM-dd HH�� mm��";
	public static final String DATE_FORMAT_FOR_SERVER = "yyyy-MM-dd";
	
	public static final int NOTI_WALKING = 2000;
	
	public static final int INTENT_REQ_LOGIN = 1010;
	public static final int INTENT_REQ_GROUP_SELECT = 1011;
	public static final int INTENT_REQ_INTRO = 1012;
	public static final int INTENT_REQ_POSTING = 1013;
	public static final int INTENT_REQ_REFRESH = 1100;
	public static final int INTENT_REQ_WEBVIEW = 1101;
	public static final int INTENT_REQ_PROFILE = 1102;
	public static final int INTENT_REQ_SETTING = 1103;
	public static final int INTENT_REQ_COMMUNITY_DELETE = 1104;
	public static final int INTENT_REQ_POST_MODIFY = 1105;

	public static final long INTRO_WAITING = 3000;
	public static final long CAULY_WAITING = 3000;

	public static final String AD_TYPE_SLIDE = "slide";
	public static final String AD_TYPE_INTRO = "intro";
	public static final String AD_TYPE_MAIN = "main";
	public static final String AD_TYPE_WALK = "walk";
	
	public static final int AD_POINT_SLIDE_VISIT = 5;
	public static final int AD_POINT_SLIDE_START = 0;
	public static final int AD_POINT_AROUNDERS = 10;
	public static final int AD_POINT_INTRO = 10;
//	public static final int AD_POINT_START = 10;
	public static final int AD_POINT_WALK = 10;
	public static final int AD_AROUNDERS_RED = 5;
	public static final int AD_AROUNDERS_GREEN = 5;
	
	public static final int DEFAULT_WEIGHT = 70;
	
	public static final long TEMPORARY_WALK_SAVE_INTERVAL = 10 * 60 * 1000;	// 10 minutes
	public static final String TEMPORARY_WALK_FILENAME = "tmp_walk.xml";
	
	public static final int ERROR_NONE					= 0;
	public static final int ERROR_NO_RESULT				= 1;
	public static final int ERROR_EXISTED_ACCOUNT		= 62;
	public static final int ERROR_SECEDED_ACCOUNT		= 63;
	public static final int ERROR_SESSION_DATA			= 93;
	public static final int ERROR_SESSION_EXPIRED		= 94;
	public static final int ERROR_PASSWORD_NOT_MATCH	= 96;
	public static final int ERROR_SYNTAX				= 98;
	public static final int ERROR_UNKNOWN				= 99;
	
	public Globals()
	{
	}
	


	
}

