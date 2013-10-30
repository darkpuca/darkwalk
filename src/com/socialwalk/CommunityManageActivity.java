package com.socialwalk;

import com.socialwalk.request.ServerRequestManager;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;

public class CommunityManageActivity extends Activity
{
	private int communitySequence;
	
	private ServerRequestManager server;
	private int reqType;
	private static final int REQUEST_COMMUNITY_MEMBERS = 100;
	
	private ListView membersList, applicantsList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_manage);
		
		this.membersList = (ListView)findViewById(R.id.membersList);
		this.applicantsList = (ListView)findViewById(R.id.applicantsList);
		
		
		this.communitySequence = getIntent().getIntExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, 0);
		
		
	}

}
