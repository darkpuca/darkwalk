package com.socialwalk;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.socialwalk.dataclass.WalkHistory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WalkDetailActivity extends Activity
{
	private WalkHistory history = null;
	private String targetFileName = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk_detail);
		
		this.targetFileName = getIntent().getStringExtra(Globals.EXTRA_KEY_FILENAME);
		this.history = Utils.GetDefaultTool().WalkHistoryFromFile(this, this.targetFileName);
		
		UpdateDetails();
		
		RelativeLayout routeButtonLayout = (RelativeLayout)findViewById(R.id.routeButtonLayout);
		routeButtonLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WalkRouteActivity.class);
				i.putExtra(Globals.EXTRA_KEY_FILENAME, targetFileName);
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
	
	private void UpdateDetails()
	{
		if (null == this.history) return;
		
		TextView distance = (TextView)findViewById(R.id.distance);
		TextView walkTime = (TextView)findViewById(R.id.walkTime);
		TextView walkSpeed = (TextView)findViewById(R.id.walkSpeed);
		TextView startTime = (TextView)findViewById(R.id.startTime);
		TextView endTime = (TextView)findViewById(R.id.endTime);
		TextView calories = (TextView)findViewById(R.id.calories);
		
		distance.setText(this.history.TotalDistanceString());
		walkTime.setText(this.history.TotalWalkingTimeString());
		walkSpeed.setText(this.history.AverageSpeed());

		SimpleDateFormat formatter = new SimpleDateFormat(Globals.DATE_FORMAT_FOR_SERVER, Locale.US);
		startTime.setText(formatter.format(this.history.StartTime));
		endTime.setText(formatter.format(this.history.EndTime));
		calories.setText(this.history.TotalCalories());
	}

}
