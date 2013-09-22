package com.socialwalk;

import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.NMapView.OnMapStateChangeListener;
import com.nhn.android.maps.NMapView.OnMapViewTouchEventListener;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;
import com.socialwalk.WalkHistory.WalkLogItem;

public class WalkRouteActivity extends NMapActivity implements OnMapStateChangeListener, OnMapViewTouchEventListener
{
	static final String NAVER_MAP_KEY = "91325246f9eb73ab763580a53e90a23b";
	private NMapView walkMap;
	private NMapController mapController;
	private NaverMapResourceProvider mapResProvider;
	private NMapOverlayManager mapOverlayManager;
	private NMapLocationManager mapLocationManager;
	private NMapMyLocationOverlay mapMyLocationOverlay;
	private NMapPathDataOverlay pathDataOverlay;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk_route);
		
		walkMap = (NMapView)findViewById(R.id.walkMap);
		walkMap.setApiKey(NAVER_MAP_KEY);
		walkMap.setClickable(true);
		walkMap.setBuiltInZoomControls(true, null);
		walkMap.setOnMapStateChangeListener(this);
		walkMap.setOnMapViewTouchEventListener(this);
		
		mapController = walkMap.getMapController();
		mapResProvider = new NaverMapResourceProvider(this);
		mapOverlayManager = new NMapOverlayManager(this, walkMap, mapResProvider);
		
		String fileName = getIntent().getStringExtra(Globals.EXTRA_KEY_FILENAME);
		WalkHistory history = Utils.defaultTool.WalkHistoryFromFile(fileName);
		if(null != history)
		{
			// set path data points
			NMapPathData pathData = new NMapPathData(history.LogItems.size());

			pathData.initPathData();
			for (int i = 0; i < history.LogItems.size(); i++)
			{
				WalkLogItem logItem = history.LogItems.get(i);
				if (0 == i)
					pathData.addPathPoint(logItem.LogLocation.getLongitude(), logItem.LogLocation.getLatitude(), NMapPathLineStyle.TYPE_SOLID);
				else
					pathData.addPathPoint(logItem.LogLocation.getLongitude(), logItem.LogLocation.getLatitude(), 0);
			}
			pathData.endPathData();
			
			NMapPathDataOverlay pathDataOverlay = mapOverlayManager.createPathDataOverlay(pathData);
			pathDataOverlay.showAllPathData(0);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.walk_route, menu);
		return true;
	}

	@Override
	public void onLongPress(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLongPressCanceled(NMapView arg0) {
		// TODO Auto-generated method stub
		
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

}
