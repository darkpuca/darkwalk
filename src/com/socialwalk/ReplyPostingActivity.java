package com.socialwalk;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.request.ServerRequestManager;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ReplyPostingActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener, OnClickListener
{
	private ServerRequestManager m_server;
	private int postSequence;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reply_posting);
		
		m_server = new ServerRequestManager();

		if (getIntent().hasExtra(Globals.EXTRA_KEY_POST_SEQUENCE))
			postSequence = getIntent().getExtras().getInt(Globals.EXTRA_KEY_POST_SEQUENCE);

		Button btnPost = (Button)findViewById(R.id.btnPost);
		btnPost.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v)
	{
		if (0 == postSequence) return;
		
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

		m_server.ReplyPosting(this, this, postSequence, contents);
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
			setResult(RESULT_OK);
			finish();
		}
	}


}
