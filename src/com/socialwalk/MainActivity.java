package com.socialwalk;



import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.AroundersItems;
import com.socialwalk.dataclass.AroundersItems.AroundersItem;
import com.socialwalk.request.ImageCacheManager;
import com.socialwalk.request.ServerRequestManager;

public class MainActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener, OnClickListener
{	
	private ServerRequestManager m_server = null;
	private LocationManager m_locationManager;
	private LocationListener m_locationListener;
	private RelativeLayout m_aroundersLayout = null, m_startLayout = null;
	
	private AroundersItems aroundersAds = new AroundersItems();
	private AroundersItem currentArounders = null;
	private Date aroundersUpdateTime = null;
	private boolean isIntroAdVisit = false;
	
	private int reqType = 0;
	private static final int REQUEST_AROUNDERS = 200;
	private static final int REQUEST_MAIN_ACCUMULATE = 201;
	private static final int REQUEST_INTRO_ACCUMULATE = 202;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        m_server = new ServerRequestManager();
//        m_server.AutoLogin(this);
        
        m_locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        m_locationListener = new MyLocationListener();
        
        m_aroundersLayout = (RelativeLayout)findViewById(R.id.layoutArounders);
        m_aroundersLayout.setVisibility(View.GONE);
        m_aroundersLayout.setOnClickListener(this);
        
        // slide 서비스를 등록
        if (!LockService.IsRegisted)
			startService(new Intent(this, LockService.class));

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
				startActivity(i);
			}
		});
        
        RelativeLayout supportLayout = (RelativeLayout)findViewById(R.id.supportLayout);
        supportLayout.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
				Toast.makeText(getBaseContext(), "support button pressed!", Toast.LENGTH_SHORT).show();
			}
		});
        
        if (!WalkService.IsStarted)
        {
        	// show intro activity
			Intent i = new Intent(this, IntroActivity.class);
			startActivityForResult(i, Globals.INTENT_REQ_INTRO);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
				MainActivity.this.finish();
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
        	UpdateUserInformation();
        	
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
        		m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 100, m_locationListener);
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
				UpdateUserInformation();
				
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
				

			
			}
		}
		else if (Globals.INTENT_REQ_INTRO == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				if (null != data)
					this.isIntroAdVisit = data.getBooleanExtra(Globals.EXTRA_KEY_INTRO_AD_VISIT, false);
				
				StartupProc();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private final void StartupProc()
	{
        // check network connection
		MainApplication app = (MainApplication)getApplication();
        if (!app.IsNetworkAvailable())
        {
        	Utils.GetDefaultTool().ShowFinishDialog(this, R.string.MSG_NETWORK_NOT_CONNECTED);
			return;
        }
        
        // login
        if (!ServerRequestManager.IsLogin)
        {
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, Globals.INTENT_REQ_LOGIN);
        }
        else
        {
        	UpdateUserInformation();
        }
	}


	@Override
	public void onErrorResponse(VolleyError error)
	{
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
				ServerRequestManager.LoginAccount.Hearts.addRedPointByTouch(Globals.AD_POINT_AROUNDERS);
				UpdateUserInformation();
			}
			else
			{
				// TODO: 적립 실패할 경우 어떻게 처리하지?
				System.out.println("하트 적립 실패");
			}
		}
		else if (REQUEST_INTRO_ACCUMULATE == reqType)
		{
			SWResponse result = parser.GetResponse();
			if (null == result) return;
			
			if (Globals.ERROR_NONE == result.Code)
			{
				ServerRequestManager.LoginAccount.Hearts.addGreenPoint(Globals.AD_POINT_INTRO);
				UpdateUserInformation();
			}
			
		}
	}

	private void UpdateArounders()
	{
		RelativeLayout layoutArounders = (RelativeLayout)findViewById(R.id.layoutArounders);
		layoutArounders.setVisibility(View.VISIBLE);
		
		NetworkImageView adIcon = (NetworkImageView)findViewById(R.id.adIcon);
		TextView adCompany = (TextView)findViewById(R.id.adCompany);
		TextView adPromotion = (TextView)findViewById(R.id.adPromo);
		TextView adDistance = (TextView)findViewById(R.id.adDistance);
		
		adIcon.setImageUrl(currentArounders.IconURL, ImageCacheManager.getInstance().getImageLoader());
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
			
			reqType = REQUEST_MAIN_ACCUMULATE;
			m_server.AccumulateHeart(this, this, Globals.AD_TYPE_MAIN, currentArounders.getSequence(), Globals.AD_POINT_AROUNDERS);
			
			this.currentArounders.SetAccessStamp();
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
	
	private void UpdateUserInformation()
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
				TextView totalRedHearts = (TextView)findViewById(R.id.redHeartTotal);
				
				String strGreenHearts = ServerRequestManager.LoginAccount.Hearts.getGreenPoint() + getResources().getString(R.string.HEART);
				String strRedHearts = ServerRequestManager.LoginAccount.Hearts.getRedPoint() + getResources().getString(R.string.HEART);
				String strTotalRedHearts = "(" + ServerRequestManager.LoginAccount.Hearts.getRedPointTotal() + getResources().getString(R.string.HEART) + ")";
				
				greenHearts.setText(strGreenHearts);
				redHearts.setText(strRedHearts);
				totalRedHearts.setText(strTotalRedHearts);			
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getLocalizedMessage());
		}
	}
}
