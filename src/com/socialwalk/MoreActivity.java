package com.socialwalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.socialwalk.request.ServerRequestManager;

public class MoreActivity extends Activity
implements View.OnClickListener
{
	private RelativeLayout noticeLayout, profileLayout, helpLayout, infoLayout, sponsorLayout;
	private RelativeLayout volunteerLayout, settingsLayout, qnaLayout, heartmallLayout;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more);
		
		this.noticeLayout = (RelativeLayout)findViewById(R.id.noticeLayout);
		this.profileLayout = (RelativeLayout)findViewById(R.id.profileLayout);
		this.helpLayout = (RelativeLayout)findViewById(R.id.helpLayout);
		this.infoLayout = (RelativeLayout)findViewById(R.id.infoLayout);
		this.sponsorLayout = (RelativeLayout)findViewById(R.id.sponsorLayout);
		this.volunteerLayout = (RelativeLayout)findViewById(R.id.volunteerLayout);
		this.settingsLayout = (RelativeLayout)findViewById(R.id.settingLayout);
		this.qnaLayout = (RelativeLayout)findViewById(R.id.qnaLayout);
		this.heartmallLayout = (RelativeLayout)findViewById(R.id.heartmallLayout);

		this.noticeLayout.setOnClickListener(this);
		this.profileLayout.setOnClickListener(this);
		this.helpLayout.setOnClickListener(this);
		this.infoLayout.setOnClickListener(this);
		this.sponsorLayout.setOnClickListener(this);
		this.volunteerLayout.setOnClickListener(this);
		this.settingsLayout.setOnClickListener(this);
		this.qnaLayout.setOnClickListener(this);
		this.heartmallLayout.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Globals.INTENT_REQ_SETTING == requestCode || Globals.INTENT_REQ_PROFILE == requestCode)
		{
			if (false == ServerRequestManager.IsLogin)
				finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v)
	{
		if (this.noticeLayout.equals(v))
		{
			Intent i = new Intent(getBaseContext(), WebPageActivity.class);
			String url = Globals.URL_NOTICES + "?seq=" + ServerRequestManager.LoginAccount.Sequence;
			i.putExtra(Globals.EXTRA_KEY_URL, url);
			startActivity(i);
		}
		else if (this.profileLayout.equals(v))
		{
			Intent i = new Intent(getBaseContext(), ProfileActivity.class);
			startActivityForResult(i, Globals.INTENT_REQ_PROFILE);
		}
		else if (this.helpLayout.equals(v))
		{
			Intent i = new Intent(getBaseContext(), WebPageActivity.class);
			i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_HELP);
			startActivity(i);
		}
		else if (this.infoLayout.equals(v))
		{
			Intent i = new Intent(getBaseContext(), WebPageActivity.class);
			i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_ABOUT);
			startActivity(i);
		}
		else if (this.sponsorLayout.equals(v))
		{
			Intent i = new Intent(getBaseContext(), WebPageActivity.class);
			i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_SPONSOR);
			startActivity(i);
		}
		else if (this.volunteerLayout.equals(v))
		{
			Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_SERVICE_PREPARING);
//			Intent i = new Intent(getBaseContext(), WebPageActivity.class);
//			i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_VOLUNTEERS);
//			startActivity(i);
		}
		else if (this.settingsLayout.equals(v))
		{
			Intent i = new Intent(getBaseContext(), SettingsActivity.class);
			startActivityForResult(i, Globals.INTENT_REQ_SETTING);
		}
		else if (this.qnaLayout.equals(v))
		{
			Intent i = new Intent(getBaseContext(), WebPageActivity.class);
			String url = Globals.URL_CONTACT_US + "?seq=" + ServerRequestManager.LoginAccount.Sequence;
			i.putExtra(Globals.EXTRA_KEY_URL, url);
			startActivity(i);
		}
		else if (this.heartmallLayout.equals(v))
		{
			Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_SERVICE_PREPARING);
		}
	}

}
