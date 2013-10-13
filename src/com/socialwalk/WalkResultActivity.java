package com.socialwalk;

import com.socialwalk.dataclass.WalkHistory;
import com.socialwalk.dataclass.WalkHistoryManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WalkResultActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk_result);
		
		Button btnClose = (Button)findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		
		Button btnDetail = (Button)findViewById(R.id.btnDetail);
		btnDetail.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WalkDetailActivity.class);
				startActivity(i);
			}
		});
		
		// 걷기 상세 정보 업데이트
		WalkHistory lastWalk = WalkHistoryManager.LastWalking();
		if(null != lastWalk)
		{
			TextView distance = (TextView)findViewById(R.id.distance);
			TextView walkTime = (TextView)findViewById(R.id.walkTime);
			TextView walkSpeed = (TextView)findViewById(R.id.walkSpeed);
			TextView calories = (TextView)findViewById(R.id.calories);
			TextView hearts = (TextView)findViewById(R.id.resultHeart);

			distance.setText(lastWalk.TotalDistanceString());
			walkTime.setText(lastWalk.TotalWalkingTimeString());
			walkSpeed.setText(lastWalk.AverageSpeed());
			calories.setText(lastWalk.TotalCalories());
			hearts.setText(lastWalk.RedHeartString());
		}

	}

}
