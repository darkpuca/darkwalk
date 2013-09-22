package com.socialwalk;


import com.socialwalk.R;
import com.socialwalk.request.ServerRequestManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView.CommaTokenizer;

public class CreateGroupActivity extends Activity
{
	private boolean isUniqueName = false, isChecked = false;
	private EditText groupName;
	private ServerRequestManager m_server = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_group);
		
		m_server = new ServerRequestManager();
		
		groupName = (EditText)findViewById(R.id.groupName);
		Button btnCheck = (Button)findViewById(R.id.btnCheck);
		Button btnCancel = (Button)findViewById(R.id.btnCancel);
		Button btnCreate = (Button)findViewById(R.id.btnCreate);
		
		btnCheck.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (0 == groupName.getText().length())
				{
					new AlertDialog.Builder(getBaseContext()).setTitle(R.string.TITLE_INFORMATION).setMessage(R.string.MSG_EMPTY_GROUP_NAME).setNeutralButton(R.string.CLOSE, null).show();
					return;
				}
				
				String name = groupName.getText().toString();
				if (true == m_server.IsExistGroupName(name))
				{
					new AlertDialog.Builder(getBaseContext()).setTitle(R.string.TITLE_INFORMATION).setMessage(R.string.MSG_DUPLICATE_GROUP_NAME).setNeutralButton(R.string.CLOSE, null).show();
					return;
				}
				
				if (true == m_server.CreateGroup(name))
				{
					Intent i = new Intent();
					i.putExtra(Globals.EXTRA_KEY_GROUP_NAME, name);
					setResult(RESULT_OK, i);
					finish();
				}
					
			}
		});
		
		btnCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_group, menu);
		return true;
	}

}
