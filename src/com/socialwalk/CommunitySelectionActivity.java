package com.socialwalk;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.Communities;
import com.socialwalk.dataclass.Community;
import com.socialwalk.request.ServerRequestManager;

public class CommunitySelectionActivity extends Activity
implements OnItemClickListener, Response.Listener<String>, Response.ErrorListener, OnClickListener
{
	private static final int INTENT_REQ_CREATE_COMMUNITY = 10;
	private static final int INTENT_REQ_COMMUNITY_JOIN = 11;
	
	private static final int REQUEST_COMMUNITY_ITEMS = 20;
	private static final int REQUEST_COMMUNITY_SEARCH = 21;
	
	private EditText keyword;
	private ListView resultList;
	private Button btnCreate, btnSearch;
	private TextView noResultMessage;
	
	private CommunityListAdapter m_communityAdapter, m_searchAdapter;
	private Communities m_communities = null, m_searchResults = null;
	private int m_requestType = 0;;
	private ServerRequestManager m_server = new ServerRequestManager();


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_selection);
		
		m_communities = new Communities();
		m_communityAdapter = new CommunityListAdapter(this, m_communities.Items);
		
		m_searchResults = new Communities();
		m_searchAdapter = new CommunityListAdapter(this, m_searchResults.Items);
		
		resultList = (ListView)findViewById(R.id.resultList);
		resultList.setAdapter(m_communityAdapter);
		resultList.setOnItemClickListener(this);
		
		keyword = (EditText)findViewById(R.id.searchKeyword);
		
		btnCreate = (Button)findViewById(R.id.btnCreateGroup);
		btnCreate.setOnClickListener(this);
		
		btnSearch = (Button)findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(this);
		
		noResultMessage = (TextView)findViewById(R.id.noResultMessage);
		
		// request community list
		m_requestType = REQUEST_COMMUNITY_ITEMS;
		m_server.CommunityGroups(this, this, m_communities.PageIndex);
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
		Intent i = new Intent(this, CommunityDetailActivity.class);
		i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, groupId);
		startActivityForResult(i, INTENT_REQ_COMMUNITY_JOIN);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (INTENT_REQ_CREATE_COMMUNITY == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				int commSeq = data.getIntExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, 0);
				String name = data.getStringExtra(Globals.EXTRA_KEY_COMMUNITY_NAME);
				String desc = data.getStringExtra(Globals.EXTRA_KEY_COMMUNITY_DESC);
				
				Toast.makeText(getBaseContext(), "id:" + commSeq + ", name:" + name + ", desc:" + desc, Toast.LENGTH_SHORT).show();
				
//				Intent i = new Intent();
//				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_ID, groupId);
//				setResult(RESULT_OK, i);
//				
//				finish();
			}
		}
		else if (INTENT_REQ_COMMUNITY_JOIN == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				String name = data.getStringExtra(Globals.EXTRA_KEY_COMMUNITY_NAME);
				Intent i = new Intent();
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_NAME, name);
				setResult(RESULT_OK, i);

				finish();
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v)
	{
		if (btnCreate.equals(v))
		{
			Intent i = new Intent(getBaseContext(), CreateCommunityActivity.class);
			if (0 < keyword.getText().length())
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_NAME, keyword.getText().toString());
			
			startActivityForResult(i, INTENT_REQ_CREATE_COMMUNITY);
		}
		else if (btnSearch.equals(v))
		{
			if (0 == keyword.getText().length())
			{
				AlertDialog.Builder dlg = new AlertDialog.Builder(this);
				dlg.setCancelable(true);
				dlg.setTitle(R.string.TITLE_INFORMATION);
				dlg.setMessage(R.string.MSG_EMPTY_KEYWORD);
				dlg.setNegativeButton(R.string.CLOSE, new DialogInterface.OnClickListener()
				{					
					@Override
					public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
				});
				dlg.show();
			}
			else
			{
				noResultMessage.setVisibility(View.GONE);
				
				m_requestType = REQUEST_COMMUNITY_SEARCH;
				m_server.CommunitySearch(this, this, keyword.getText().toString(), m_searchResults.PageIndex);
			}
		}
	}
	
	
	private static class CommunityItemContainer
	{
		public TextView Name;
		public TextView Description;
	}
	
	public class CommunityListAdapter extends ArrayAdapter<Community>
	{
		private Activity m_context = null;
		private Vector<Community> m_items = null;
		
		public CommunityListAdapter(Activity context, Vector<Community> items)
		{
			super(context, R.layout.listitem_community_search, items);

			this.m_context = context;
			this.m_items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			CommunityItemContainer container;
			View rowView = convertView;
			
			if (null == rowView)
			{
				try {
					LayoutInflater inflater = m_context.getLayoutInflater();
					rowView = inflater.inflate(R.layout.listitem_community_search, null, true);					
				} catch (Exception e) {
					Log.d("SW", e.getLocalizedMessage());
					return null;
				}

				container = new CommunityItemContainer();
				container.Name = (TextView)rowView.findViewById(R.id.name);
				container.Description = (TextView)rowView.findViewById(R.id.description);
				
				rowView.setTag(container);
			}
			else
			{
				container = (CommunityItemContainer)rowView.getTag();
			}
			
			Community item = m_items.get(position);
			
			container.Name.setText(item.Name);
			container.Description.setText(item.Description);
			
			return rowView;
		}
	}
	

	@Override
	public void onErrorResponse(VolleyError error)
	{
		System.out.println(error.getLocalizedMessage());
	}

	@Override
	public void onResponse(String response)
	{
		if (0 == response.length())
			Toast.makeText(getBaseContext(), R.string.MSG_NO_SEARCH_RESULT, Toast.LENGTH_SHORT).show();
		
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (REQUEST_COMMUNITY_ITEMS == m_requestType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				Communities recvItems = parser.GetCommunities();
				if (null == recvItems) return;
				
				m_communities.Items.addAll(recvItems.Items);
			}
		}
		else if (REQUEST_COMMUNITY_SEARCH == m_requestType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				Communities recvItems = parser.GetCommunities();
				if (null == recvItems) return;
				
				m_searchResults.Items.addAll(recvItems.Items);
				
			}
			else if (Globals.ERROR_NO_RESULT == result.Code)
			{
				noResultMessage.setVisibility(View.VISIBLE);			
			}
		}
	}

	
	
}
