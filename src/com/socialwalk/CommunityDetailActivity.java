package com.socialwalk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.CommunityDetail;
import com.socialwalk.request.ServerRequestManager;

public class CommunityDetailActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener
{
	private ServerRequestManager server;
	private int reqType;
	private static final int REQUEST_COMMUNITY_DETAIL = 400;
	private static final int REQUEST_COMMUNITY_SECESSION = 401;
	
	private int communitySeq;
	private Button manageButton, secessionButton;
	private ProgressDialog progDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_detail);
		
		this.server = new ServerRequestManager();
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));
		
		this.manageButton = (Button)findViewById(R.id.manageButton);
		this.manageButton.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), CommunityManageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, communitySeq);
				startActivityForResult(i, Globals.INTENT_REQ_COMMUNITY_DELETE);				
			}
		});
		
		this.secessionButton = (Button)findViewById(R.id.secessionButton);
		this.secessionButton.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if (!progDlg.isShowing()) progDlg.show();
				reqType = REQUEST_COMMUNITY_SECESSION;
				server.CommunitySecession(CommunityDetailActivity.this, CommunityDetailActivity.this);
			}
		});
		
		this.communitySeq = getIntent().getIntExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, 0);
		if (0 < this.communitySeq)
		{
			if (null != ServerRequestManager.LoginAccount)
			{
				// group management button visibility
				if (this.communitySeq == ServerRequestManager.LoginAccount.CommunitySeq)
				{
					manageButton.setVisibility(View.VISIBLE);
					secessionButton.setVisibility(View.INVISIBLE);
				}
				else
				{
					manageButton.setVisibility(View.INVISIBLE);
					secessionButton.setVisibility(View.VISIBLE);
				}
			}

			progDlg.show();
			this.reqType = REQUEST_COMMUNITY_DETAIL;
			this.server.CommunityDetail(this, this, this.communitySeq);
		}
	}

	private void updateCommunityDetail(CommunityDetail detail)
	{
		TextView tvName = (TextView)findViewById(R.id.communityName);
		TextView tvDesc = (TextView)findViewById(R.id.description);
		
		tvName.setText(detail.Name);
		tvDesc.setText(detail.Description);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Globals.INTENT_REQ_COMMUNITY_DELETE == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				setResult(RESULT_OK);
				finish();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onErrorResponse(VolleyError error)
	{
		if (progDlg.isShowing()) progDlg.dismiss();
		
		error.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (progDlg.isShowing()) progDlg.dismiss();

		if (0 == response.length()) return;
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (REQUEST_COMMUNITY_DETAIL == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityDetail detail = parser.GetCommunityDetail();
				if (null == detail) return;
				updateCommunityDetail(detail);
			}
		}
		else if (REQUEST_COMMUNITY_SECESSION == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				setResult(RESULT_OK);
				finish();
			}
			else
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_API_FAIL);
			}
		}
	}

}
