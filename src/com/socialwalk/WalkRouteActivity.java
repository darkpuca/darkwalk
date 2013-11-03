package com.socialwalk;

import android.os.Bundle;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;
import com.socialwalk.dataclass.WalkHistory;
import com.socialwalk.dataclass.WalkHistory.WalkLogItem;

public class WalkRouteActivity extends NMapActivity
{
	static final String NAVER_MAP_KEY = "91325246f9eb73ab763580a53e90a23b";
	private NMapView walkMap;
	private NMapController mapController;
	private NaverMapResourceProvider mapResProvider;
	private NMapOverlayManager mapOverlayManager;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk_route);
		
		walkMap = (NMapView)findViewById(R.id.walkMap);
		walkMap.setApiKey(NAVER_MAP_KEY);
		walkMap.setClickable(true);
		walkMap.setBuiltInZoomControls(true, null);
		
		mapController = walkMap.getMapController();
		mapController.setMapCenter(new NGeoPoint(126.978371, 37.5666091), 13);

		mapResProvider = new NaverMapResourceProvider(this);
		mapOverlayManager = new NMapOverlayManager(this, walkMap, mapResProvider);
		
		String fileName = getIntent().getStringExtra(Globals.EXTRA_KEY_FILENAME);
		WalkHistory history = Utils.GetDefaultTool().WalkHistoryFromFile(this, fileName);
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
}
