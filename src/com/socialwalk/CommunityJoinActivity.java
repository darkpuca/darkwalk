package com.socialwalk;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.CommunityDetail;
import com.socialwalk.request.ServerRequestManager;

public class CommunityJoinActivity extends Activity
implements Response.Listener<String>,Response.ErrorListener , OnClickListener
{
	private ServerRequestManager server = null;
	private int reqType;
	private static int REQUEST_COMMUNITY_DETAIL = 100;
	private static int REQUEST_COMMUNITY_JOIN = 101;
	
	private Button btnJoin;
	private TextView tvMessage;
	private int communitySeq;

	private ProgressDialog progDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_join);
		
		this.server = new ServerRequestManager();

		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));

		this.communitySeq = getIntent().getIntExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, 0);
		
		this.btnJoin = (Button)findViewById(R.id.btnJoin);
		btnJoin.setOnClickListener(this);
		
		this.tvMessage = (TextView)findViewById(R.id.reqMessage);
		
		if (0 < this.communitySeq)
		{
			progDlg.show();
			this.reqType = REQUEST_COMMUNITY_DETAIL;
			this.server.CommunityDetail(this, this, this.communitySeq);
		}
	}

	@Override
	public void onClick(View v)
	{
		if (this.btnJoin.equals(v))
		{
			String message = this.tvMessage.getText().toString();
			if (0 == message.length())
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_EMPTY_CONTENTS);
				return;
			}
			
			if (!progDlg.isShowing()) progDlg.show();
			
			this.reqType = REQUEST_COMMUNITY_JOIN;
			this.server.CommunityJoin(this, this, this.communitySeq, message);
		}
	}
	
	private void updateCommunityDetail(CommunityDetail detail)
	{
		if (null == detail) return;
		
		TextView tvTitle = (TextView)findViewById(R.id.titleLabel);
		TextView tvDesc = (TextView)findViewById(R.id.groupDesc);
		
		tvTitle.setText(detail.Name);
		tvDesc.setText(detail.Description);
	}
	
	

	@Override
	public void onErrorResponse(VolleyError e)
	{
		if (progDlg.isShowing()) progDlg.dismiss();
		
		e.printStackTrace();
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
		else if (REQUEST_COMMUNITY_JOIN == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				Intent i = new Intent();
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, this.communitySeq);
				setResult(RESULT_OK, i);
				finish();
			}
		}
	}

}
