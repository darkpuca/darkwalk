package com.socialwalk;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.NMapView.OnMapStateChangeListener;
import com.nhn.android.maps.NMapView.OnMapViewTouchEventListener;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;
import com.socialwalk.dataclass.WalkHistory;
import com.socialwalk.dataclass.WalkHistoryManager;
import com.socialwalk.dataclass.WalkHistory.WalkLogItem;

public class WalkingActivity extends NMapActivity 
implements OnMapStateChangeListener, OnMapViewTouchEventListener, OnPageChangeListener, View.OnClickListener
{
	static final String NAVER_MAP_KEY = "91325246f9eb73ab763580a53e90a23b";
	private NMapView walkMap;
	private NMapController mapController;
	private NaverMapResourceProvider mapResProvider;
	private NMapOverlayManager mapOverlayManager;
	private NMapLocationManager mapLocationManager;
	private NMapMyLocationOverlay mapMyLocationOverlay;
	private NMapPathDataOverlay pathDataOverlay;

	
	private ImageView walkAni;
	private Timer updateTimer;
	private Handler updateHandler;
	private TimerTask updateTask;
	private boolean IsMapMode = false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk);
		
		RelativeLayout layoutStop = (RelativeLayout)findViewById(R.id.layoutStop);
		layoutStop.setOnClickListener(this);
		
		walkAni = (ImageView)findViewById(R.id.walkAniImage);
		
		walkAni.setBackgroundResource(R.drawable.charactor_walk_animation);
		walkAni.post(new Runnable()
		{
		    @Override
		    public void run()
		    {
				AnimationDrawable charactorAnimation = (AnimationDrawable)walkAni.getBackground();
				charactorAnimation.start();
		    }
		});
		
		walkMap = (NMapView)findViewById(R.id.walkMap);
		
		walkMap.setApiKey(NAVER_MAP_KEY);
		walkMap.setClickable(true);
		walkMap.setBuiltInZoomControls(true, null);
		walkMap.setOnMapStateChangeListener(this);
		walkMap.setOnMapViewTouchEventListener(this);
		
		mapController = walkMap.getMapController();
		mapResProvider = new NaverMapResourceProvider(this);
		mapOverlayManager = new NMapOverlayManager(this, walkMap, mapResProvider);
		pathDataOverlay = mapOverlayManager.createPathDataOverlay();
		
		mapLocationManager = new NMapLocationManager(this);
		mapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);
		mapMyLocationOverlay = mapOverlayManager.createMyLocationOverlay(mapLocationManager, null);

		if (IsMapMode)
		{
			walkAni.setVisibility(View.INVISIBLE);
			walkMap.setVisibility(View.VISIBLE);
			mapController.setMapCenter(new NGeoPoint(126.978371, 37.5666091), 14);
		}
		else
		{
			walkMap.setVisibility(View.INVISIBLE);
			walkAni.setVisibility(View.VISIBLE);
		}
		
		ImageButton btnMyLocation = (ImageButton)findViewById(R.id.btnMyLocation);
		btnMyLocation.setVisibility(walkMap.getVisibility());
		
		btnMyLocation.setOnClickListener(new OnClickListener()
		{					
			@Override
			public void onClick(View v)
			{
				if (null != mapMyLocationOverlay)
				{
					if (!mapOverlayManager.hasOverlay(mapMyLocationOverlay))
						mapOverlayManager.addOverlay(mapMyLocationOverlay);

					boolean isMyLocationEnabled = mapLocationManager.enableMyLocation(true);
					if (!isMyLocationEnabled)
					{
//						Toast.makeText(WalkingActivity.this, "Please enable a My Location source in system settings", Toast.LENGTH_LONG).show();

						Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(goToSettings);

						return;
					}
				}
			}
		});

		
		TextView userName = (TextView)findViewById(R.id.userName);
		userName.setText("darkpuca");
		
		// 모드 버튼 동작 구현
		Button btnMode = (Button)findViewById(R.id.btnMode);
		btnMode.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				IsMapMode = !IsMapMode;
				if (IsMapMode)
				{
					walkAni.setVisibility(View.INVISIBLE);
					walkMap.setVisibility(View.VISIBLE);
				}
				else
				{
					walkMap.setVisibility(View.INVISIBLE);
					walkAni.setVisibility(View.VISIBLE);
				}				
			}
		});
		
		
		updateHandler = new Handler();
		updateTask = new TimerTask()
		{			
			@Override
			public void run()
			{
				updateHandler.post(new Runnable()
				{					
					@Override
					public void run()
					{
						WalkHistory currentWalk = WalkHistoryManager.LastWalking();
						if(null != currentWalk)
						{							
							TextView walkDistance = (TextView)findViewById(R.id.walkDistance);
							TextView walkSpeed = (TextView)findViewById(R.id.walkSpeed);
							TextView walkTime = (TextView)findViewById(R.id.walkTime);
							
							walkDistance.setText(currentWalk.TotalDistanceString());
//							walkSpeed.setText(currentWalk.AverageSpeedFromNow());
							walkTime.setText(currentWalk.TotalWalkingTimeStringFromNow());
							
							WalkLogItem lastItem = currentWalk.GetLastValidItem();
							if (null != lastItem)
							{
								String speed = String.format(getResources().getString(R.string.FORMAT_SPEED), lastItem.CurrentSpeed);
								walkSpeed.setText(speed);
								
//								if (IsMapMode)
//									mapController.setMapCenter(lastItem.LogLocation.getLongitude(), lastItem.LogLocation.getLatitude());
							}
						}
					}
				});
			}
		};
		updateTimer = new Timer();
		updateTimer.scheduleAtFixedRate(updateTask, 1000, 1000);

	}

	@Override
	protected void onDestroy()
	{
		if (mapLocationManager.isMyLocationEnabled())
			mapLocationManager.disableMyLocation();
		
		updateTimer.cancel();
		super.onDestroy();
	}

	
	@Override
	public void onBackPressed()
	{
		// 걷기 화면 표시중엔 Back으로 이동 안함
		return;
//		super.onBackPressed();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.walk, menu);
		return true;
	}

	
	@Override
	public void onLongPress(NMapView arg0, MotionEvent arg1) {
		
	}

	
	@Override
	public void onLongPressCanceled(NMapView arg0) {
		
	}

	@Override
	public void onScroll(NMapView arg0, MotionEvent arg1, MotionEvent arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSingleTapUp(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTouchDown(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTouchUp(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStateChange(NMapView arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapCenterChange(NMapView arg0, NGeoPoint arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapCenterChangeFine(NMapView arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapInitHandler(NMapView arg0, NMapError arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onZoomLevelChange(NMapView arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v)
	{
		if (R.id.layoutStop == v.getId())
		{
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			dlg.setCancelable(true);
			dlg.setTitle(R.string.TITLE_INFORMATION);
			dlg.setMessage(R.string.MSG_STOP_WALKING_CONFIRM);
			dlg.setPositiveButton(R.string.STOP, new DialogInterface.OnClickListener()
			{					
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					AnimationDrawable charactorAnimation = (AnimationDrawable)walkAni.getBackground();
					charactorAnimation.stop();

					// 서비스 중지
					Intent svcIntent = new Intent(getApplicationContext(), WalkService.class);
					stopService(svcIntent);
					
					// 걷기 결과 화면 전
					Intent i = new Intent(getApplicationContext(), WalkResultActivity.class);
					startActivity(i);
					
					finish();
				}
			});
			dlg.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener()
			{					
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			dlg.show();
		}		
	}
	
	
	private void stopMyLocation()
	{
		if (mapOverlayManager != null)
		{
			mapLocationManager.disableMyLocation();

			mapMyLocationOverlay.setCompassHeadingVisible(false);
			walkMap.setAutoRotateEnabled(false, false);
		}
	}
	
	
	/* MyLocation Listener */
	private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener()
	{
		@Override
		public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {

			if (mapController != null)
			{
				mapController.animateTo(myLocation);
				stopMyLocation();
			}

			return true;
		}

		@Override
		public void onLocationUpdateTimeout(NMapLocationManager locationManager)
		{
//			Toast.makeText(WalkingActivity.this, R.string.MSG_MY_LOCATION_FAIL, Toast.LENGTH_LONG).show();
			stopMyLocation();
		}

		@Override
		public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation)
		{
			Toast.makeText(WalkingActivity.this, R.string.MSG_MY_LOCATION_FAIL, Toast.LENGTH_LONG).show();
			stopMyLocation();
		}

	};


}
