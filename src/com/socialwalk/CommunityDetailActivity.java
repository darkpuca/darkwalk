package com.socialwalk;



import com.socialwalk.request.ServerRequestManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CommunityDetailActivity extends Activity
{
	private Button btnJoin;
	private ServerRequestManager m_server = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_detail);
		
		m_server = new ServerRequestManager();
		
		btnJoin = (Button)findViewById(R.id.btnJoin);
		btnJoin.setOnClickListener(new OnClickListener()
		{	
			@Override
			public void onClick(View v)
			{
				int selGroupId = 12345;
				if (true == m_server.GroupJoin("root", selGroupId))
				{
					Intent i = new Intent();
					i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, selGroupId);
					setResult(RESULT_OK, i);
					finish();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group_detail, menu);
		return true;
	}

}
