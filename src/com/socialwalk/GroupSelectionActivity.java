package com.socialwalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class GroupSelectionActivity extends Activity implements OnItemClickListener
{
	private static final int INTENT_REQ_CREATE_GROUP = 10;
	private static final int INTENT_REQ_GROUP_JOIN = 11;
	
	EditText keyword;
	ListView resultList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_selection);
		
		resultList = (ListView)findViewById(R.id.resultList);
		resultList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Globals.sampleStrings));
		resultList.setOnItemClickListener(this);
		
		keyword = (EditText)findViewById(R.id.searchKeyword);		
		
		Button btnSelect = (Button)findViewById(R.id.btnSelect);
		Button btnCancel = (Button)findViewById(R.id.btnCancel);
		Button btnCreate = (Button)findViewById(R.id.btnCreate);
		
		btnSelect.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent();
				i.putExtra("sel_group", "SEL_GROUP");
				setResult(RESULT_OK, i);
				finish();
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
		
		btnCreate.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), CreateGroupActivity.class);
				
				if (0 < keyword.getText().length())
					i.putExtra(Globals.EXTRA_KEY_GROUP_NAME, keyword.getText().toString());
				
				startActivityForResult(i, INTENT_REQ_CREATE_GROUP);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group_selection, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	{
		Toast.makeText(this, "list item selected. idx: " + position, Toast.LENGTH_SHORT).show();
		
		int groupId = 12345;
		Intent i = new Intent(this, GroupDetailActivity.class);
		i.putExtra(Globals.EXTRA_KEY_GROUP_ID, groupId);
		startActivityForResult(i, INTENT_REQ_GROUP_JOIN);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (INTENT_REQ_CREATE_GROUP == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				String name = data.getStringExtra(Globals.EXTRA_KEY_GROUP_NAME);
				Toast.makeText(this, "create group ok. name: " + name, Toast.LENGTH_SHORT).show();
				
				Intent i = new Intent();
				i.putExtra(Globals.EXTRA_KEY_GROUP_NAME, name);
				setResult(RESULT_OK, i);
				
				finish();
			}
		}
		else if (INTENT_REQ_GROUP_JOIN == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				String name = data.getStringExtra(Globals.EXTRA_KEY_GROUP_NAME);
				Intent i = new Intent();
				i.putExtra(Globals.EXTRA_KEY_GROUP_NAME, name);
				setResult(RESULT_OK, i);

				finish();
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	
}
