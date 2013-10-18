package com.socialwalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class MoreActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more);
		
		RelativeLayout noticeLayout = (RelativeLayout)findViewById(R.id.noticeLayout);
		noticeLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_NOTICES);
				startActivity(i);
			}
		});
		
		RelativeLayout profileLayout = (RelativeLayout)findViewById(R.id.profileLayout);
		profileLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), ProfileActivity.class);
				startActivity(i);
			}
		});
		
		RelativeLayout helpLayout = (RelativeLayout)findViewById(R.id.helpLayout);
		helpLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_HELP);
				startActivity(i);
			}
		});
		
		RelativeLayout infoLayout = (RelativeLayout)findViewById(R.id.infoLayout);
		infoLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_ABOUT);
				startActivity(i);
			}
		});
		
		RelativeLayout sponsorLayout = (RelativeLayout)findViewById(R.id.sponsorLayout);
		sponsorLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_SPONSOR);
				startActivity(i);
			}
		});

		RelativeLayout volunteerLayout = (RelativeLayout)findViewById(R.id.volunteerLayout);
		volunteerLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_VOLUNTEERS);
				startActivity(i);
			}
		});
		
		RelativeLayout settingsLayout = (RelativeLayout)findViewById(R.id.settingLayout);
		settingsLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), SettingsActivity.class);
				startActivity(i);
			}
		});


		RelativeLayout qnaLayout = (RelativeLayout)findViewById(R.id.qnaLayout);
		qnaLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_CONTACT_US);
				startActivity(i);
			}
		});

	}

}
