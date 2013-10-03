package com.socialwalk;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

public class IntroActivity extends Activity
{
	Timer closeTimer;
	TimerTask closeTask;
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
		closeTask = new TimerTask()
		{			
			@Override
			public void run()
			{
				setResult(RESULT_OK);
				finish();
			}
		};
		closeTimer = new Timer();
		closeTimer.schedule(closeTask, Globals.INTRO_WAITING);
		
		RelativeLayout buttonLayout = (RelativeLayout)findViewById(R.id.layoutHeartButton);
		buttonLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_SERVER_DOMAIN);
				startActivity(i);
				
				finish();
			}
		});
	}

	@Override
	protected void onDestroy()
	{
		closeTimer.cancel();
		super.onDestroy();
	}

}
