package com.socialwalk;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
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
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.AroundersItems;
import com.socialwalk.dataclass.AroundersItems.AroundersItem;
import com.socialwalk.dataclass.WalkHistory;
import com.socialwalk.dataclass.WalkHistory.WalkLogItem;
import com.socialwalk.dataclass.WalkHistoryManager;
import com.socialwalk.request.ImageCacheManager;
import com.socialwalk.request.ServerRequestManager;

public class WalkingActivity extends NMapActivity 
implements OnMapStateChangeListener, OnMapViewTouchEventListener
, OnPageChangeListener, View.OnClickListener,
Response.Listener<String>, Response.ErrorListener
{
	static final String NAVER_MAP_KEY = "91325246f9eb73ab763580a53e90a23b";
	private NMapView walkMap;
	private NMapController mapController;
	private NaverMapResourceProvider mapResProvider;
	private NMapOverlayManager mapOverlayManager;
	private NMapLocationManager mapLocationManager;
	private NMapMyLocationOverlay mapMyLocationOverlay;
//	private NMapPathDataOverlay pathDataOverlay;

	private ServerRequestManager server = null;
	private int reqType;
	private static final int REQUEST_AROUNDERS = 100;
	private static final int REQUEST_AD_ACCUMULATE = 101;
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	
	private AroundersItems aroundersAds = new AroundersItems();
	private AroundersItem currentArounders = null;
	private Date aroundersUpdateTime = null;

	private ImageView walkAni, characterBgView;
	private Timer updateTimer;
	private Handler updateHandler;
	private TimerTask updateTask;
	private boolean IsMapMode = false;
	private RelativeLayout animodeLayout, mapmodeLayout, stopLayout, aroundersLayout;
	
	private static boolean characterAminatonEnable = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk);
		
		this.server = new ServerRequestManager();
		
        this.locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        this.locationListener = new NetworkLocationListener();

		
        this.characterBgView = (ImageView)findViewById(R.id.mainBgImage);
		Bitmap newBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.main_character_bg);
		this.characterBgView.setImageBitmap(newBitmap );

		
        this.aroundersLayout = (RelativeLayout)findViewById(R.id.layoutArounders);
        this.aroundersLayout.setVisibility(View.INVISIBLE);
        this.aroundersLayout.setOnClickListener(this);

		
		this.stopLayout = (RelativeLayout)findViewById(R.id.layoutStop);
		this.stopLayout.setOnClickListener(this);
		
		walkAni = (ImageView)findViewById(R.id.walkCharacter);
		if (characterAminatonEnable)
		{
			walkAni.setBackgroundResource(R.drawable.charactor_walk_ani);
			walkAni.post(new Runnable()
			{
			    @Override
			    public void run()
			    {
					AnimationDrawable charactorAnimation = (AnimationDrawable)walkAni.getBackground();
					charactorAnimation.start();
			    }
			});
		}
		
		walkMap = (NMapView)findViewById(R.id.walkMap);
		
		walkMap.setApiKey(NAVER_MAP_KEY);
		walkMap.setClickable(true);
		walkMap.setBuiltInZoomControls(true, null);
		walkMap.setOnMapStateChangeListener(this);
		walkMap.setOnMapViewTouchEventListener(this);
		
		mapController = walkMap.getMapController();
		mapResProvider = new NaverMapResourceProvider(this);
		mapOverlayManager = new NMapOverlayManager(this, walkMap, mapResProvider);
//		pathDataOverlay = mapOverlayManager.createPathDataOverlay();
		
		mapLocationManager = new NMapLocationManager(this);
		mapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);
		mapMyLocationOverlay = mapOverlayManager.createMyLocationOverlay(mapLocationManager, null);

		this.animodeLayout = (RelativeLayout)findViewById(R.id.animodeLayout);
		this.mapmodeLayout = (RelativeLayout)findViewById(R.id.mapmodeLayout);
		
		updateModeLayout();
		
		if (true == this.IsMapMode)
			mapController.setMapCenter(new NGeoPoint(126.978371, 37.5666091), 13);

		
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
						Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(goToSettings);
					}
				}
			}
		});

		// 모드 버튼 동작 구현
		Button btnMode = (Button)findViewById(R.id.btnMode);
		btnMode.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{	
				IsMapMode = !IsMapMode;
				updateModeLayout();
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
							updateWalkInfos(currentWalk);
					}
				});
			}
		};
		
		updateTimer = new Timer();
		updateTimer.scheduleAtFixedRate(updateTask, 1000, 1000);

//		UpdateUserInformation();
	}

	private void updateModeLayout()
	{
		if (IsMapMode)
		{
			this.animodeLayout.setVisibility(View.INVISIBLE);
			this.mapmodeLayout.setVisibility(View.VISIBLE);
		}
		else
		{
			this.animodeLayout.setVisibility(View.VISIBLE);
			this.mapmodeLayout.setVisibility(View.INVISIBLE);
		}
	}
	
	private void updateWalkInfos(WalkHistory currentWalk)
	{
		TextView walkDistance = (TextView)findViewById(R.id.walkDistance);
		TextView walkSpeed = (TextView)findViewById(R.id.walkSpeed);
		TextView altitude = (TextView)findViewById(R.id.altitude);

		walkDistance.setText(currentWalk.TotalDistanceString());

		WalkLogItem lastItem = currentWalk.GetLastValidItem();
		if (null != lastItem)
		{
			walkSpeed.setText(String.format(getResources().getString(R.string.FORMAT_SPEED), lastItem.CurrentSpeed));
			altitude.setText(String.format(getResources().getString(R.string.FORMAT_ALTITUDE), lastItem.LogLocation.getAltitude()));
		}
		else
		{
			walkSpeed.setText(String.format(getResources().getString(R.string.FORMAT_SPEED), 0.0f));
			altitude.setText(String.format(getResources().getString(R.string.FORMAT_ALTITUDE), 0.0f));
		}

		TextView walkTime = (TextView)findViewById(R.id.walkTime);
		walkTime.setText(currentWalk.TotalWalkingTimeStringFromNow());
		
		TextView walkHearts = (TextView)findViewById(R.id.walkHearts);
		TextView touchHearts = (TextView)findViewById(R.id.touchHearts);
		TextView calories = (TextView)findViewById(R.id.calories);
		
		walkHearts.setText(currentWalk.RedHeartStringByWalk() + " " + getResources().getString(R.string.HEART));
		touchHearts.setText(currentWalk.RedHeartStringByTouch() + " " + getResources().getString(R.string.HEART));
		calories.setText(currentWalk.WalkingCaloriesStringFromNow() + " " + getResources().getString(R.string.CALORIES_UNIT));
	}
	
	private void requestArounders(Location loc)
	{
		this.reqType = REQUEST_AROUNDERS;
        this.server.AroundersItems(this, this, loc.getLatitude(), loc.getLongitude());
	}
	
	

	@Override
	protected void onDestroy()
	{
		if (mapLocationManager.isMyLocationEnabled())
			mapLocationManager.disableMyLocation();

    	if (null != this.locationManager && null != this.locationListener)
    		this.locationManager.removeUpdates(this.locationListener);
		
		updateTimer.cancel();
		
		((BitmapDrawable)this.characterBgView.getDrawable()).getBitmap().recycle();

		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
    	if (null != this.locationManager && null != this.locationListener)
    		this.locationManager.removeUpdates(this.locationListener);

		super.onPause();
	}

	@Override
	protected void onResume()
	{
    	// 네트워크 기반 위치정보 수신 모듈 재시작
    	if (null != this.locationManager && null != this.locationListener)
    		this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 10, this.locationListener);

		super.onResume();
	}

	@Override
	public void onBackPressed()
	{
		// 걷기 화면 표시중엔 Back으로 이동 안함
		return;
//		super.onBackPressed();
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
		}
		catch (Exception e)
		{
			System.out.println(e.getLocalizedMessage());
		}
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
		if (this.stopLayout.equals(v))
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
					if (characterAminatonEnable)
					{
						AnimationDrawable charactorAnimation = (AnimationDrawable)walkAni.getBackground();
						charactorAnimation.stop();
					}
					
					WalkHistory currentWalk = WalkHistoryManager.LastWalking();
					currentWalk.Finish();

					// 서비스 중지
					Intent svcIntent = new Intent(getApplicationContext(), WalkService.class);
					stopService(svcIntent);
					
					// 걷기 결과 화면 전환
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
		else if (this.aroundersLayout.equals(v))
		{
			if (null == currentArounders) return;
			
			if (Utils.GetDefaultTool().IsBillingArounders(currentArounders.getSequence()))
			{
				reqType = REQUEST_AD_ACCUMULATE;
				this.server.AccumulateHeart(this, this, Globals.AD_TYPE_WALK, currentArounders.getSequence(), Globals.AD_POINT_WALK);
			}
			else
			{
				// TODO: 적립되는 광고가 아닐 경우에는 어떻게 표시하
			}

			this.currentArounders.SetAccessStamp();
			Intent i = new Intent(getBaseContext(), WebPageActivity.class);
			i.putExtra(Globals.EXTRA_KEY_URL, currentArounders.getTargetURL());
			startActivity(i);
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


	private void updateArounders()
	{
		RelativeLayout layoutArounders = (RelativeLayout)findViewById(R.id.layoutArounders);
		layoutArounders.setVisibility(View.VISIBLE);
		
		NetworkImageView adIcon = (NetworkImageView)findViewById(R.id.advIcon);
		TextView adCompany = (TextView)findViewById(R.id.adCompany);
		TextView adPromotion = (TextView)findViewById(R.id.adPromo);
		TextView adDistance = (TextView)findViewById(R.id.adDistance);
		
		adIcon.setImageUrl(currentArounders.IconURL, ImageCacheManager.getInstance().getImageLoader());
		adCompany.setText(currentArounders.Company);
		adPromotion.setText(currentArounders.Promotion);
		adDistance.setText(Integer.toString(currentArounders.Distance) + "m");
	}

	

	@Override
	public void onErrorResponse(VolleyError error)
	{
		error.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (0 == response.length()) return;

		MyXmlParser parser = new MyXmlParser(response);
		
		if (REQUEST_AROUNDERS == this.reqType)
		{
			System.out.println(response);
			
			AroundersItems items = parser.GetArounders();
			if (null == items) return;
			if (0 == items.Items.size()) return;
			
			this.aroundersUpdateTime = new Date();
			this.aroundersAds.Items.clear();
			this.aroundersAds.Items.addAll(items.Items);
			
			this.currentArounders = this.aroundersAds.GetItem();
			
			updateArounders();
		}
		else if (REQUEST_AD_ACCUMULATE == this.reqType)
		{
			SWResponse result = parser.GetResponse();
			if (null == result) return;

			if (Globals.ERROR_NONE == result.Code)
			{
				LockService.AroundersVisitCodes.add(this.currentArounders.getSequence());
				ServerRequestManager.LoginAccount.Hearts.addRedPointByTouch(Globals.AD_AROUNDERS_RED);
				ServerRequestManager.LoginAccount.Hearts.addGreenPoint(Globals.AD_AROUNDERS_GREEN);
				updateUserInformation();
			}
			else
			{
				// TODO: 적립 실패할 경우 어떻게 처리하지?
				System.out.println("하트 적립 실패");
			}
		}
	}
	
	
	private class NetworkLocationListener implements LocationListener
	{
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
				
				requestArounders(location);
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

}
