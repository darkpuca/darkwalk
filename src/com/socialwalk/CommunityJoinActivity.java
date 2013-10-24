package com.socialwalk;



import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.request.ServerRequestManager;

public class CommunityJoinActivity extends Activity
implements Response.Listener<String>,Response.ErrorListener , OnClickListener
{
	private ServerRequestManager server = null;
	private int reqType;
	private static int REQUEST_COMMUNITY_JOIN = 100;
	private static int REQUEST_COMMUNITY_SECESSION = 101;
	
	private Button btnJoin, btnSecession;
	private int communitySeq;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_join);
		
		this.server = new ServerRequestManager();

		btnJoin = (Button)findViewById(R.id.btnJoin);

		this.communitySeq = getIntent().getIntExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, 0);
		if (0 < this.communitySeq)
		{
			if (null != ServerRequestManager.LoginAccount)
			{
				// 이미 가입자인 경우 가입 탈퇴 버튼 보임
				if (this.communitySeq == ServerRequestManager.LoginAccount.CommunitySeq)
				{
					btnJoin.setVisibility(View.GONE);
					btnSecession.setVisibility(View.VISIBLE);
				}
				else
				{
					btnJoin.setVisibility(View.VISIBLE);
					btnSecession.setVisibility(View.GONE);
				}
			}
		}
		
		btnJoin.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group_detail, menu);
		return true;
	}

	@Override
	public void onClick(View v)
	{
		if (this.btnJoin.equals(v))
		{
			String message = "";
			
			this.reqType = REQUEST_COMMUNITY_JOIN;
			this.server.CommunityJoin(this, this, this.communitySeq, message);
		}
		else if (this.btnSecession.equals(v))
		{
			
		}
	}

	@Override
	public void onErrorResponse(VolleyError e)
	{
		e.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
	}

}
