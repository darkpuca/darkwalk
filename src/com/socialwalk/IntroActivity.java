package com.socialwalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class IntroActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{			
			@Override
			public void run()
			{
//				IntroActivity.this.setResult(RESULT_OK);
				IntroActivity.this.finish();
			}
		}, Globals.INTRO_WAITING);
		

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
		super.onDestroy();
	}

}
