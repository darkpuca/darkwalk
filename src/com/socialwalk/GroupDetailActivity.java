package com.socialwalk;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GroupDetailActivity extends Activity
{
	private Button btnJoin;
	private ServerRequestManager m_reqManager = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_detail);
		
		m_reqManager = new ServerRequestManager(this);
		
		btnJoin = (Button)findViewById(R.id.btnJoin);
		btnJoin.setOnClickListener(new OnClickListener()
		{	
			@Override
			public void onClick(View v)
			{
				int selGroupId = 12345;
				if (true == m_reqManager.GroupJoin("root", selGroupId))
				{
					Intent i = new Intent();
					i.putExtra(Globals.EXTRA_KEY_GROUP_ID, selGroupId);
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
