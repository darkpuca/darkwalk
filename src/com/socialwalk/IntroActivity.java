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
		
		Button btnAd = (Button)findViewById(R.id.btnAd);
		btnAd.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_HOME);
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
