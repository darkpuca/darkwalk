package com.socialwalk;



import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.fsn.cauly.CaulyAdInfo;
import com.fsn.cauly.CaulyAdInfoBuilder;
import com.fsn.cauly.CaulyInterstitialAd;
import com.fsn.cauly.CaulyInterstitialAdListener;
import com.fsn.cauly.Logger;
import com.fsn.cauly.Logger.LogLevel;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.AroundersItems;
import com.socialwalk.dataclass.AroundersItems.AroundersItem;
import com.socialwalk.request.ImageCacheManager;
import com.socialwalk.request.ServerRequestManager;
import com.socialwalk.request.ServerRequestManager.ServerRequestListener;

public class MainActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener, OnClickListener, ServerRequestListener
, CaulyInterstitialAdListener
{
	// cauly 광고 유지시간 끝난후 앱 종료 핸들러.
	private Handler caulyCloseHandler = null;
	private Runnable caulyCloseRunnable = new Runnable()
	{	
		@Override
		public void run()
		{
			if (null != currentCauly)
				currentCauly.cancel();

			finish();
			System.exit(0);
		}
	};
	
	
	private ServerRequestManager m_server = null;
	private LocationManager m_locationManager;
	private LocationListener m_locationListener;
	private RelativeLayout m_aroundersLayout = null, m_startLayout = null;
	private ImageView characterBgView;
	
	// 어라운더스 광고 표시 관련 변수들.
	private AroundersItems aroundersAds = new AroundersItems();
	private AroundersItem currentArounders = null;
	private Date aroundersUpdateTime = null;
	private boolean isIntroAdVisit = false, isAutologinRun = false;
	
	// 서버 통신 변수들.
	private int reqType = 0;
	private static final int REQUEST_AROUNDERS = 200;
	private static final int REQUEST_MAIN_ACCUMULATE = 201;
	private static final int REQUEST_INTRO_ACCUMULATE = 202;
	
	private ProgressDialog progDlg;
	
	// 광고 요청을 위한 App Code
	private static final String CAULY_APP_CODE = "0yM85YHh";
	private CaulyInterstitialAd currentCauly = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        m_server = new ServerRequestManager();
        this.isAutologinRun = m_server.AutoLogin(this, this);
        
        m_locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        m_locationListener = new MyLocationListener();

        // cauly 광고 미리 수신.
        prepareCaulyAd();
        
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_RECEIVE_USER_DATA));
		
        if (this.isAutologinRun)
        {
        	if (!progDlg.isShowing())
        		progDlg.show();
        }

        
        this.characterBgView = (ImageView)findViewById(R.id.mainBgImage);
		Bitmap newBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.main_character_bg);
		this.characterBgView.setImageBitmap(newBitmap );
        
        m_aroundersLayout = (RelativeLayout)findViewById(R.id.layoutArounders);
        m_aroundersLayout.setVisibility(View.GONE);
        m_aroundersLayout.setOnClickListener(this);
        
        // start button action
		m_startLayout = (RelativeLayout)findViewById(R.id.layoutStartButton);
		m_startLayout.setOnClickListener(this);
        
        ImageButton btnSponsor = (ImageButton)findViewById(R.id.btnSponsor);
        btnSponsor.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), BeneficiariesActivity.class);
				startActivity(i);
			}
		});
        
        ImageButton btnGroup = (ImageButton)findViewById(R.id.btnGroup);
        btnGroup.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
				if (null == ServerRequestManager.LoginAccount) return;
				
				int commSeq = ServerRequestManager.LoginAccount.CommunitySeq;
//				commId = 0;	// test value
				if (0 == commSeq)
				{
					AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
					dlg.setCancelable(true);
					dlg.setTitle(R.string.TITLE_INFORMATION);
					dlg.setMessage(R.string.MSG_COMMUNITY_SIGNUP_CONFIRM);
					dlg.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener()
					{						
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					dlg.setPositiveButton(R.string.CONTINUE, new DialogInterface.OnClickListener()
					{	
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Intent i = new Intent(getBaseContext(), CommunitySelectionActivity.class);
							startActivityForResult(i, Globals.INTENT_REQ_GROUP_SELECT);
						}
					});
					dlg.show();
				}
				else
				{
					Intent i = new Intent(getBaseContext(), CommunityActivity.class);
					i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, commSeq);
					startActivity(i);
				}
			}
		});
        
        ImageButton btnHistory = (ImageButton)findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WalkHistoryActivity.class);
				startActivity(i);
			}
		});
        
        ImageButton btnMore = (ImageButton)findViewById(R.id.btnMore);
        btnMore.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), MoreActivity.class);
				startActivityForResult(i, Globals.INTENT_REQ_SETTING);
			}
		});
        
        RelativeLayout supportLayout = (RelativeLayout)findViewById(R.id.supportLayout);
        supportLayout.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
//				Toast.makeText(getBaseContext(), "support button pressed!", Toast.LENGTH_SHORT).show();
				Utils.GetDefaultTool().ShowMessageDialog(MainActivity.this, R.string.MSG_SERVICE_PREPARING);
			}
		});
        
        if (!WalkService.IsStarted)
        {
        	// show intro activity
			Intent i = new Intent(this, IntroActivity.class);
			startActivityForResult(i, Globals.INTENT_REQ_INTRO);
        }
    }

	private void prepareCaulyAd()
	{
		// cauly 광고 설정 시작.
        Logger.setLogLevel(LogLevel.Debug);
        
        CaulyAdInfo caulyAdInfo = new CaulyAdInfoBuilder(CAULY_APP_CODE).build();
     
        // 전면 광고 생성
        CaulyInterstitialAd interstial = new CaulyInterstitialAd(); 
        interstial.setAdInfo(caulyAdInfo);
        interstial.setInterstialAdListener(this);

        // 광고 요청. 광고 노출은 CaulyInterstitialAdListener의 onReceiveInterstitialAd에서 처리한다.
        interstial.requestInterstitialAd (this); 
	}
    
	@Override
	public void onBackPressed()
	{
		AlertDialog.Builder exitDlg = new AlertDialog.Builder(this);
		exitDlg.setCancelable(true);
		exitDlg.setTitle(R.string.TITLE_INFORMATION);
		exitDlg.setMessage(R.string.MSG_EXIT_CONFIRM);
		exitDlg.setPositiveButton(R.string.EXIT, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				((MainApplication)getApplication()).SaveMetas();
				
				showCaulyAd();
			}
		});
		exitDlg.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		exitDlg.show();
	}


	@Override
	protected void onDestroy()
	{
    	if (null != m_locationManager && null != m_locationListener)
    		m_locationManager.removeUpdates(m_locationListener);
		
		((BitmapDrawable)this.characterBgView.getDrawable()).getBitmap().recycle();

		((MainApplication)getApplication()).SaveMetas();
		
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
        if (WalkService.IsStarted)
        {
        	// 걷기 모드로 시작일 경우 걷기 상태 화면으로 바로 이동
        	Intent i = new Intent(this, WalkingActivity.class);
        	startActivity(i);
        }
        else
        {
        	updateUserInformation();
        	
        	// 저장된 어라운더스 광고 정보가 있을 경우 이를 이용하여 위치 기반 광고를 표시한다.
        	m_aroundersLayout.setVisibility(View.INVISIBLE);
        	if (0 < this.aroundersAds.Items.size())
        	{
        		AroundersItem item = this.aroundersAds.GetItem();
        		if (null != item)
        		{
        			this.currentArounders = item;
        			UpdateArounders();
        		}
        	}
        	
        	// 네트워크 기반 위치정보 수신 모듈 재시작
        	if (null != m_locationManager && null != m_locationListener)
        		m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 10, m_locationListener);
        }
		
		super.onResume();
	}

	@Override
	protected void onPause()
	{
    	if (null != m_locationManager && null != m_locationListener)
    		m_locationManager.removeUpdates(m_locationListener);
    	
		super.onPause();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Globals.INTENT_REQ_LOGIN == requestCode)
		{ 
			if (RESULT_OK == resultCode)
			{
				updateUserInformation();
				
				if (this.isIntroAdVisit)
				{
					reqType = REQUEST_INTRO_ACCUMULATE;
					m_server.AccumulateHeart(this, this, Globals.AD_TYPE_INTRO, "", Globals.AD_POINT_INTRO);
				}
			}
			else
			{
				Utils.GetDefaultTool().ShowFinishDialog(this, R.string.MSG_NO_LOGIN_EXIT);
			}
		}
		else if (Globals.INTENT_REQ_GROUP_SELECT == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				if (null == ServerRequestManager.LoginAccount) return;
				
				int commSeq = data.getIntExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, 0);
				String name = data.getStringExtra(Globals.EXTRA_KEY_COMMUNITY_NAME);
				ServerRequestManager.LoginAccount.CommunitySeq = commSeq;
				ServerRequestManager.LoginAccount.CommunityName = name;
				updateUserInformation();
			}
		}
		else if (Globals.INTENT_REQ_INTRO == requestCode)
		{
	        // check network connection
			MainApplication app = (MainApplication)getApplication();
	        if (!app.IsNetworkAvailable())
	        {
	        	Utils.GetDefaultTool().ShowFinishDialog(this, R.string.MSG_NETWORK_NOT_CONNECTED);
				return;
	        }
	        
			if (RESULT_OK == resultCode)
			{
				if (null != data)
					this.isIntroAdVisit = data.getBooleanExtra(Globals.EXTRA_KEY_INTRO_AD_VISIT, false);
				
				if (false == this.isAutologinRun)
					startupProc();
			}
		}
		else if (Globals.INTENT_REQ_SETTING == requestCode)
		{
			if (false == ServerRequestManager.IsLogin)
			{
				((MainApplication)getApplication()).SaveMetas();

				finish();
				System.exit(0);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private final void startupProc()
	{
        // login
        if (!ServerRequestManager.IsLogin)
        {
        	Log.d("DEBUG", "manual login start");
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, Globals.INTENT_REQ_LOGIN);
        }
        else
        {
        	Log.d("DEBUG", "update user information");
        	updateUserInformation();
        }
        
        //m_server.AroundersItems(MainActivity.this, MainActivity.this, 37.48454f, 127.03394f);
	}


	@Override
	public void onErrorResponse(VolleyError error)
	{
		if (progDlg.isShowing())
			progDlg.dismiss();
		
		error.printStackTrace();
	}


	@Override
	public void onResponse(String response)
	{
		if (0 == response.length()) return;
		
		MyXmlParser parser = new MyXmlParser(response);
		
		if (REQUEST_AROUNDERS == reqType)
		{
			System.out.println(response);
			
			AroundersItems items = parser.GetArounders();
			if (null == items) return;
			if (0 == items.Items.size()) return;
			
			this.aroundersUpdateTime = new Date();
			this.aroundersAds.Items.clear();
			this.aroundersAds.Items.addAll(items.Items);
			
			this.currentArounders = this.aroundersAds.GetItem();
			UpdateArounders();
		}
		else if (REQUEST_MAIN_ACCUMULATE == reqType)
		{
			SWResponse result = parser.GetResponse();
			if (null == result) return;
			
			if (Globals.ERROR_NONE == result.Code)
			{
				Utils.GetDefaultTool().SaveAroundersCode(this.currentArounders.getSequence());
				ServerRequestManager.LoginAccount.Hearts.addRedPointByTouch(Globals.AD_AROUNDERS_RED);
				ServerRequestManager.LoginAccount.Hearts.addGreenPoint(Globals.AD_AROUNDERS_GREEN);
				updateUserInformation();
			}
		}
		else if (REQUEST_INTRO_ACCUMULATE == reqType)
		{
			SWResponse result = parser.GetResponse();
			if (null == result) return;
			
			if (Globals.ERROR_NONE == result.Code)
			{
				ServerRequestManager.LoginAccount.Hearts.addGreenPoint(Globals.AD_POINT_INTRO);
				updateUserInformation();
			}
			
		}
	}

	private void UpdateArounders()
	{
		RelativeLayout layoutArounders = (RelativeLayout)findViewById(R.id.layoutArounders);
		layoutArounders.setVisibility(View.VISIBLE);
		
		NetworkImageView adIcon = (NetworkImageView)findViewById(R.id.advIcon);
		TextView adCompany = (TextView)findViewById(R.id.adCompany);
		TextView adPromotion = (TextView)findViewById(R.id.adPromo);
		TextView adDistance = (TextView)findViewById(R.id.adDistance);
		
		adIcon.setImageUrl(currentArounders.getIconURL(), ImageCacheManager.getInstance().getImageLoader());
		adCompany.setText(currentArounders.Company);
		adPromotion.setText(currentArounders.Promotion);
		adDistance.setText(Integer.toString(currentArounders.Distance) + "m");
	}

	
	private class MyLocationListener implements LocationListener
	{
		private ServerRequestManager m_server = new ServerRequestManager();

		@Override
		public void onLocationChanged(Location location)
		{
			if (null != location)
			{
				if (null == aroundersUpdateTime)
				{
					aroundersUpdateTime = new Date();
				}
				else
				{
					// 어라운더스 정보가 10분 이내의 정보이면 갱신하지 않는다
					Date now = new Date();
					long diffInMs = now.getTime() - aroundersUpdateTime.getTime();
					long diffInMinutes= TimeUnit.MILLISECONDS.toMinutes(diffInMs);
					if (10 > diffInMinutes) return;
				}
		        // 위치기반 광고의 위치정보는 네트워크를 이용한 위치 정보를 활용한다.
				reqType = REQUEST_AROUNDERS;
		        m_server.AroundersItems(MainActivity.this, MainActivity.this, location.getLatitude(), location.getLongitude());
//		        m_server.AroundersItems(MainActivity.this, MainActivity.this, 37.48454f, 127.03394f);
			}
			
		}

		@Override
		public void onProviderDisabled(String provider)
		{
		}

		@Override
		public void onProviderEnabled(String provider)
		{
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
		
	}

	@Override
	public void onClick(View v)
	{
		if (m_aroundersLayout.equals(v))
		{
			if (null == currentArounders) return;
			
			if (Utils.GetDefaultTool().IsBillingArounders(currentArounders.getSequence()))
			{
				reqType = REQUEST_MAIN_ACCUMULATE;
				m_server.AccumulateHeart(this, this, Globals.AD_TYPE_MAIN, currentArounders.getSequence(), Globals.AD_POINT_AROUNDERS);
			}

			Intent i = new Intent(getBaseContext(), WebPageActivity.class);
			i.putExtra(Globals.EXTRA_KEY_URL, currentArounders.getTargetURL());
			startActivity(i);
		}
		else if (m_startLayout.equals(v))
		{
			MainApplication app = (MainApplication)getApplication();
			if (!app.IsGpsAvailable())
			{
				Utils.GetDefaultTool().ShowGpsSettingWithDialog(this);
	        }
	        else
	        {
	        	// start walking service
	        	Intent svcIntent = new Intent(getBaseContext(), WalkService.class);
	        	startService(svcIntent);		        	
	        	
	        	// load walking state activity
				Intent viewIntent = new Intent(getBaseContext(), WalkingActivity.class);
				startActivity(viewIntent);
	        }
		}
	}
	
	private void updateUserInformation()
	{
		if (false == ServerRequestManager.IsLogin) return;
		if (null == ServerRequestManager.LoginAccount) return;
		
		try
		{
			TextView userName = (TextView)findViewById(R.id.userName);
			TextView groupName = (TextView)findViewById(R.id.groupName);
			TextView areaName = (TextView)findViewById(R.id.areaName);
			
			userName.setText(ServerRequestManager.LoginAccount.Name);
			if (null == ServerRequestManager.LoginAccount.CommunityName)
				groupName.setText(R.string.NO_GROUP);
			else
				groupName.setText(ServerRequestManager.LoginAccount.CommunityName);
			String areaNameVal = ServerRequestManager.LoginAccount.AreaName + " " + ServerRequestManager.LoginAccount.AreaSubName;
			areaName.setText(areaNameVal);			

			if (null != ServerRequestManager.LoginAccount.Hearts)
			{
				TextView greenHearts = (TextView)findViewById(R.id.greenHeart);
				TextView redHearts = (TextView)findViewById(R.id.redHeart);
				TextView redHeartsWalk = (TextView)findViewById(R.id.redHeartWalk);
				TextView redHeartsTouch = (TextView)findViewById(R.id.redHeartTouch);
				
				redHearts.setText(Utils.GetDefaultTool().DecimalNumberString(ServerRequestManager.LoginAccount.Hearts.getRedPoint()));
				redHeartsWalk.setText(Utils.GetDefaultTool().DecimalNumberString(ServerRequestManager.LoginAccount.Hearts.getRedPointByWalk()));
				redHeartsTouch.setText(Utils.GetDefaultTool().DecimalNumberString(ServerRequestManager.LoginAccount.Hearts.getRedPointByTouch()));
				greenHearts.setText(Utils.GetDefaultTool().DecimalNumberString(ServerRequestManager.LoginAccount.Hearts.getGreenPoint()));
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getLocalizedMessage());
		}
	}


	@Override
	public void onFinishAutoLogin(boolean isLogin)
	{
		if (progDlg.isShowing())
			progDlg.dismiss();

		startupProc();
	}
	
	
	/****************************************************************
	* cauly 광고 listener functions
	****************************************************************/
	@Override
	public void onReceiveInterstitialAd(CaulyInterstitialAd ad, boolean isChargeableAd)
	{
		// 광고 수신 성공한 경우 호출됨.
		// 수신된 광고가 무료 광고인 경우 isChargeableAd 값이 false 임.
		if (isChargeableAd == false)
			Log.d("CaulyExample", "free interstitial AD received.");
		else
			Log.d("CaulyExample", "normal interstitial AD received.");

		currentCauly = ad;
	}

	private void showCaulyAd()
	{
		if (null != currentCauly)
		{
			currentCauly.show();
			
			caulyCloseHandler = new Handler();
			caulyCloseHandler.postDelayed(caulyCloseRunnable, Globals.CAULY_WAITING);
		}
		else
		{
			finish();
			System.exit(0);
		}
	}
	
	@Override
	public void onFailedToReceiveInterstitialAd(CaulyInterstitialAd ad, int errorCode, String errorMsg)
	{
		// 전면 광고 수신 실패할 경우 호출됨.
		Log.d("CaulyExample", "failed to receive interstitial AD.");		

		currentCauly = null;
	}

	@Override
	public void onClosedInterstitialAd(CaulyInterstitialAd ad)
	{
		// 전면 광고가 닫힌 경우 호출됨.
		Log.d("CaulyExample", "interstitial AD closed.");

		currentCauly = null;
		
		finish();
		System.exit(0);
	}

}
