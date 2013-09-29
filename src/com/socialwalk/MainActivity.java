package com.socialwalk;



import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.socialwalk.MyXmlParser.AroundersItem;
import com.socialwalk.request.ImageCacheManager;
import com.socialwalk.request.ServerRequestManager;

public class MainActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener, OnClickListener
{	
	private ServerRequestManager m_server = null;
	private LocationManager m_locationManager;
	private LocationListener m_locationListener;
	private AroundersItem m_currentArounders = null;
	private RelativeLayout m_aroundersLayout = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        m_server = new ServerRequestManager();
        m_server.AutoLogin(this);
        
        m_locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        m_locationListener = new MyLocationListener();
        
        m_aroundersLayout = (RelativeLayout)findViewById(R.id.layoutArounders);
        m_aroundersLayout.setVisibility(View.GONE);
        m_aroundersLayout.setOnClickListener(this);
        
        // create utility class
        Utils.CreateDefaultTool(this);
        
        // slide 서비스를 등록
        if (!LockService.IsRegisted)
			startService(new Intent(this, LockService.class));

		// show intro activity
		Intent i = new Intent(this, IntroActivity.class);
		startActivityForResult(i, Globals.INTENT_REQ_INTRO);
        
        // start button action
        Button btnStart = (Button)findViewById(R.id.btnWalkStart);
        btnStart.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
				if (!Utils.defaultTool.IsGpsAvailable())
				{
					Utils.defaultTool.showGpsSettingWithDialog();
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
		});
        
        ImageButton btnSponsor = (ImageButton)findViewById(R.id.btnSponsor);
        btnSponsor.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), SponsorMainActivity.class);
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
				
				int commId = ServerRequestManager.LoginAccount.getCommunityId();
//				// 테스트값
//				commId = -1;
				if (-1 == commId)
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
							Intent i = new Intent(getBaseContext(), GroupSelectionActivity.class);
							startActivityForResult(i, Globals.INTENT_REQ_GROUP_SELECT);
						}
					});
					dlg.show();
				}
				else
				{
					Intent i = new Intent(getBaseContext(), CommunityActivity.class);
					i.putExtra(Globals.EXTRA_KEY_COMMUNITY_ID, commId);
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
        
        ImageButton btnSettings = (ImageButton)findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), SettingsActivity.class);
				startActivity(i);
			}
		});
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
		ServerRequestManager.IsLogin = false;
		
    	if (null != m_locationManager && null != m_locationListener)
    		m_locationManager.removeUpdates(m_locationListener);
		
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
		Utils.defaultTool.SetBaseActivity(this);
		
        if (WalkService.IsStarted)
        {
        	// 걷기 모드로 시작일 경우 걷기 상태 화면으로 바로 이동
        	Intent i = new Intent(this, WalkingActivity.class);
        	startActivity(i);
        }
        else
        {
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
				Toast.makeText(this, "login result ok", Toast.LENGTH_SHORT).show();
				
			}
			else
			{
				Utils.defaultTool.showFinishDialog(R.string.MSG_NO_LOGIN_EXIT);
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
			StartupProc();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void StartupProc()
	{
        // check network connection
        if (!Utils.defaultTool.IsNetworkAvailable())
        {
        	Utils.defaultTool.showFinishDialog(R.string.MSG_NETWORK_NOT_CONNECTED);
			return;
        }
        
        // login
        if (!ServerRequestManager.IsLogin)
        {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, Globals.INTENT_REQ_LOGIN);
        }
        
        // arounders update
        m_server.AroundersItems(MainActivity.this, MainActivity.this, 37.5666091, 126.978371);
	}


	@Override
	public void onErrorResponse(VolleyError error)
	{
	}


	@Override
	public void onResponse(String response)
	{
		if (0 == response.length()) return;
		
		Vector<AroundersItem> items = new MyXmlParser(response).GetArounders();
		if (null == items) return;
		if (0 == items.size()) return;
		
		RelativeLayout layoutArounders = (RelativeLayout)findViewById(R.id.layoutArounders);
		layoutArounders.setVisibility(View.VISIBLE);
		
		m_currentArounders = items.get(0);
		
		NetworkImageView adIcon = (NetworkImageView)findViewById(R.id.adIcon);
		TextView adCompany = (TextView)findViewById(R.id.adCompany);
		TextView adPromotion = (TextView)findViewById(R.id.adPromotion);
		TextView adDistance = (TextView)findViewById(R.id.adDistance);
		
		adIcon.setImageUrl(m_currentArounders.IconURL, ImageCacheManager.getInstance().getImageLoader());
		adCompany.setText(m_currentArounders.Company);
		adPromotion.setText(m_currentArounders.Promotion);
		adDistance.setText(Integer.toString(m_currentArounders.Distance) + "m");
	}
	
	
	
	private class MyLocationListener implements LocationListener
	{
		private ServerRequestManager m_server = new ServerRequestManager();

		@Override
		public void onLocationChanged(Location location)
		{
			if (null != location)
			{
		        // 위치기반 광고의 위치정보는 네트워크를 이용한 위치 정보를 활용한다.
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
			if (null == m_currentArounders) return;
			
			Intent i = new Intent(getBaseContext(), WebPageActivity.class);
			i.putExtra(Globals.EXTRA_KEY_URL, m_currentArounders.TargetURL);
			startActivity(i);
		}
	}
}
