package com.socialwalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WalkDetailActivity extends Activity
{
	private WalkHistory m_history = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk_detail);
		
		String fileName = getIntent().getStringExtra(Globals.EXTRA_KEY_FILENAME);
		m_history = Utils.defaultTool.WalkHistoryFromFile(fileName);
		
		UpdateDetails();
		
		Button btnRoute = (Button)findViewById(R.id.btnRoute);
		btnRoute.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WalkRouteActivity.class);
				i.putExtra(Globals.EXTRA_KEY_FILENAME, WalkDetailActivity.this.m_history.FileName);
				startActivity(i);
			}
		});
		
		Button btnClose = (Button)findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new OnClickListener()
		{	
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.walk_detail, menu);
		return true;
	}
	
	private void UpdateDetails()
	{
		if (null == m_history) return;
		
		TextView distance = (TextView)findViewById(R.id.distance);
		TextView walkTime = (TextView)findViewById(R.id.walkTime);
		TextView walkSpeed = (TextView)findViewById(R.id.walkSpeed);
		TextView startTime = (TextView)findViewById(R.id.startTime);
		TextView endTime = (TextView)findViewById(R.id.endTime);
		TextView calories = (TextView)findViewById(R.id.calories);
		
		distance.setText(m_history.TotalDistanceString());
		walkTime.setText(m_history.TotalWalkingTimeString());
		walkSpeed.setText(m_history.AverageSpeed());
		startTime.setText(m_history.StartTime.toLocaleString());
		endTime.setText(m_history.EndTime.toLocaleString());		
	}

}
