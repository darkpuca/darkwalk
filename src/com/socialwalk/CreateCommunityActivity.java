package com.socialwalk;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.request.ServerRequestManager;

public class CreateCommunityActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener, View.OnClickListener
{
	private EditText groupName, groupDesc;
	private Button btnCheck, btnCreate, btnCancel;
	private ServerRequestManager m_server = null;
	
	private static int RequestTypeCheck = 1;
	private static int RequestTypeCreate = 2;
	private int RequestType = 0;
	private boolean isValidName = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_community);
		
		m_server = new ServerRequestManager();
		
		groupName = (EditText)findViewById(R.id.groupName);
		groupDesc = (EditText)findViewById(R.id.groupDesc);
		btnCheck = (Button)findViewById(R.id.btnCheck);
		btnCreate = (Button)findViewById(R.id.btnCreateGroup);
		
		btnCheck.setOnClickListener(this);
		btnCreate.setOnClickListener(this);
		
		if (getIntent().hasExtra(Globals.EXTRA_KEY_COMMUNITY_NAME))
		{
			String name = getIntent().getExtras().getString(Globals.EXTRA_KEY_COMMUNITY_NAME);
			groupName.setText(name);
			btnCheck.setFocusable(true);
		}
	}

	@Override
	public void onClick(View v)
	{
		if (btnCheck.equals(v))
		{
			if (0 == groupName.getText().length())
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_EMPTY_GROUP_NAME);
				return;
			}
			
			String name = groupName.getText().toString();
			RequestType = RequestTypeCheck;
			m_server.IsExistGroupName(this, this, name);
		}
		else if (btnCreate.equals(v))
		{
			if (false == isValidName)
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_CHECK_DUPLICATE);
				return;
			}
			
			String name = groupName.getText().toString();
			String desc = groupDesc.getText().toString();
			
			RequestType = RequestTypeCreate;
			m_server.CreateGroup(this, this, name, desc);
		}
		else if (btnCancel.equals(v))
		{
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	@Override
	public void onErrorResponse(VolleyError error)
	{
		error.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (0 == response.length()) return;
		
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;

		if (RequestTypeCheck == RequestType)
		{			
			if (Globals.ERROR_NO_RESULT == result.Code)
			{
				isValidName = true;
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_NAME_AVAILABLE);
			}
			else
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_DUPLICATE_GROUP_NAME);
				return;
			}
			
		}
		else if (RequestTypeCreate == RequestType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				int newSeq = parser.GetReturnId();
				Intent i = new Intent();
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, newSeq);
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_NAME, groupName.getText().toString());
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_DESC, groupDesc.getText().toString());
				
				setResult(RESULT_OK, i);
				finish();
			}
		}

	}


}
