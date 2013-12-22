package com.socialwalk;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
	
	private static final int REQUEST_ITEMS = 200;
	private static final int REQUEST_MORE_ITEMS = 201;
	private static final int REQUEST_SEARCH = 202;
	private static final int REQUEST_MORE_SEARCH = 203;
	
	private String keyword;
	private EditText tvKeyword;
	private ListView resultList;
	private Button btnCreate, btnSearch;
	private TextView noResultMessage;
	
	private CommunityListAdapter communityAdapter, searchAdapter;
	private int reqType = 0, commPageIndex, searchPageIndex, commCount, searchCount;
	private ServerRequestManager server = new ServerRequestManager();

	private ProgressDialog progDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_selection);
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));
		
		communityAdapter = new CommunityListAdapter(this, new Vector<Community>());
		searchAdapter = new CommunityListAdapter(this, new Vector<Community>());
		
		resultList = (ListView)findViewById(R.id.resultList);
		resultList.setAdapter(communityAdapter);
		resultList.setOnItemClickListener(this);
		
		tvKeyword = (EditText)findViewById(R.id.searchKeyword);
		
		btnCreate = (Button)findViewById(R.id.btnCreateGroup);
		btnCreate.setOnClickListener(this);
		
		btnSearch = (Button)findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(this);
		
		noResultMessage = (TextView)findViewById(R.id.noResultMessage);
		
		requestItems();
	}

	private void requestItems()
	{
		if (!progDlg.isShowing()) progDlg.show();
		
		// request community list
		this.reqType = REQUEST_ITEMS;
		this.commPageIndex = 1;
		this.server.CommunityGroups(this, this, this.commPageIndex);
	}
	
	private void requestMoreItems()
	{
		if (!progDlg.isShowing()) progDlg.show();
		
		// request community list
		this.reqType = REQUEST_MORE_ITEMS;
		this.commPageIndex++;
		this.server.CommunityGroups(this, this, this.commPageIndex);
	}

	private void requestSearches(String keyword)
	{
		if (null == keyword) return;
		if (0 == keyword.length()) return;
		
		if (!progDlg.isShowing()) progDlg.show();
		
		this.reqType = REQUEST_SEARCH;
		this.searchPageIndex = 1;
		this.server.CommunitySearch(this, this, keyword, this.searchPageIndex);
	}

	private void requestMoreSearches(String keyword)
	{
		if (null == keyword) return;
		if (0 == keyword.length()) return;
		
		if (!progDlg.isShowing()) progDlg.show();
		
		this.reqType = REQUEST_MORE_SEARCH;
		this.searchPageIndex++;
		this.server.CommunitySearch(this, this, keyword, this.searchPageIndex);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	{
//		Toast.makeText(this, "list item selected. idx: " + position, Toast.LENGTH_SHORT).show();
		Community selItem = (Community)parent.getItemAtPosition(position);
		
		int commSeq = selItem.Sequence;
		Intent i = new Intent(this, CommunityJoinActivity.class);
		i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, commSeq);
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
//				Toast.makeText(getBaseContext(), "id:" + commSeq + ", name:" + name + ", desc:" + desc, Toast.LENGTH_SHORT).show();
				
				Intent i = new Intent();
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, commSeq);
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_NAME, name);
				setResult(RESULT_OK, i);
				
				finish();
			}
		}
		else if (INTENT_REQ_COMMUNITY_JOIN == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				int commSeq = data.getIntExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, 0);
				Intent i = new Intent();
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, commSeq);
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
			if (0 < tvKeyword.getText().length())
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_NAME, tvKeyword.getText().toString());
			
			startActivityForResult(i, INTENT_REQ_CREATE_COMMUNITY);
		}
		else if (btnSearch.equals(v))
		{
			if (0 == tvKeyword.getText().length())
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_EMPTY_KEYWORD);
			}
			else
			{
				this.keyword = this.tvKeyword.getText().toString();
//				noResultMessage.setVisibility(View.GONE);
				requestSearches(this.keyword);
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
		private boolean isSearchResult = false;
		
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
			
			if (!isSearchResult)
			{
				if (position == (this.getCount()-1) && position < (commCount-1))
					requestMoreItems();
			}
			else
			{
				if (position == (this.getCount()-1) && position < (searchCount-1))
					requestMoreSearches(keyword);
			}
			
			return rowView;
		}
		
		public void IsSearchResult(boolean isSearch)
		{
			this.isSearchResult = isSearch;
		}
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
		
		if (REQUEST_ITEMS == reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				Communities recvItems = parser.GetCommunities();
				if (null == recvItems) return;
				
				this.communityAdapter.clear();
				
				this.commCount = recvItems.TotalCount;
				
				for (Community item : recvItems.Items)
					this.communityAdapter.add(item);
				
				this.resultList.setAdapter(communityAdapter);
			}
		}
		else if (REQUEST_MORE_ITEMS == reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				Communities recvItems = parser.GetCommunities();
				if (null == recvItems) return;
				
				for (Community item : recvItems.Items)
					this.communityAdapter.add(item);
			}
		}
		else if (REQUEST_SEARCH == reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				Communities recvItems = parser.GetCommunities();
				if (null == recvItems) return;

//				this.noResultMessage.setVisibility(View.INVISIBLE);
				
				this.searchCount = recvItems.TotalCount;
				
				this.searchAdapter.clear();

				for (Community item : recvItems.Items)
					this.searchAdapter.add(item);

				this.resultList.setAdapter(searchAdapter);
			}
			else if (Globals.ERROR_NO_RESULT == result.Code)
			{
//				if (0 == this.communityAdapter.getCount())
//					noResultMessage.setVisibility(View.VISIBLE);
				
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_NO_SEARCH_RESULT);
			}
		}
		else if (REQUEST_MORE_SEARCH == reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				Communities recvItems = parser.GetCommunities();
				if (null == recvItems) return;

				for (Community item : recvItems.Items)
					this.searchAdapter.add(item);
			}
		}
	}

	
	
}
