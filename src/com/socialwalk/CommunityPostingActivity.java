package com.socialwalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.request.ServerRequestManager;

public class CommunityPostingActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener, OnClickListener
{
	private ServerRequestManager m_server;
	private int m_communityId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_posting);
		
		m_server = new ServerRequestManager();

		if (getIntent().hasExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE))
			m_communityId = getIntent().getExtras().getInt(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE);

		Button btnPost = (Button)findViewById(R.id.btnPost);
		btnPost.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		if (0 == m_communityId) return;
		
		EditText etContents = (EditText)findViewById(R.id.contents);
		String contents = etContents.getText().toString();
		
		if (0 == contents.length())
		{
			new AlertDialog.Builder(getBaseContext())
			.setTitle(R.string.TITLE_INFORMATION)
			.setMessage(R.string.MSG_EMPTY_CONTENTS)
			.setNeutralButton(R.string.CLOSE, null)
			.show();
		}

		m_server.CommunityPosting(this, this, m_communityId, contents);
	}

	@Override
	public void onErrorResponse(VolleyError e)
	{
		e.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (0 == response.length()) return;
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (Globals.ERROR_NONE == result.Code)
		{
//			Toast.makeText(getBaseContext(), R.string.MSG_COMMUNITY_POSTING_SUCCESS, Toast.LENGTH_SHORT).show();
			
			Intent i = new Intent();
			setResult(RESULT_OK, i);
			finish();
		}
	}

}
