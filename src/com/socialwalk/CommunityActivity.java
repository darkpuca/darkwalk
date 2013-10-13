package com.socialwalk;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.CommunityPosts;
import com.socialwalk.dataclass.CommunityPosts.CommunityPostItem;
import com.socialwalk.request.ServerRequestManager;

public class CommunityActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener
{
	private CommunityDetail m_detail;
	private CommunityPostAdapter m_adapter;
	private ServerRequestManager m_server;
	private int communitySequence = 0, reqType = 0, pageIndex = 0, totalCount = 0;
	private TextView noResult;
	
	private static final int REQUEST_TYPE_INFO = 1;
	private static final int REQUEST_TYPE_FIRST = 2;
	private static final int REQUEST_TYPE_MORE = 3;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community);
		
		m_adapter = new CommunityPostAdapter(this, new Vector<CommunityPostItem>());
		m_server = new ServerRequestManager();
		
		ListView postList = (ListView)findViewById(R.id.postList);
		postList.setAdapter(m_adapter);
		
		RelativeLayout writeLayout = (RelativeLayout)findViewById(R.id.writeButtonLayout);
		writeLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), CommunityPostingActivity.class);
				int commSeq = getIntent().getExtras().getInt(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE);
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, commSeq);
				startActivityForResult(i, Globals.INTENT_REQ_POSTING);
			}
		});
		
		if (getIntent().hasExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE))
		{
			communitySequence = getIntent().getExtras().getInt(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE);
			reqType = REQUEST_TYPE_INFO;
			m_server.CommunityDetail(this, this, communitySequence);
		}
		
		noResult = (TextView)findViewById(R.id.noResult);
		
		postList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adpater, View view, int position, long id)
			{
				CommunityPostItem item = m_adapter.getItem(position);
				if (null == item) return;

				Intent i = new Intent(getBaseContext(), CommunityPostDetailActivity.class);
				i.putExtra(Globals.EXTRA_KEY_WRITER_SEQUENCE, item.UserSequence);
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, communitySequence);
				i.putExtra(Globals.EXTRA_KEY_POST_SEQUENCE, item.Sequence);
				startActivityForResult(i, Globals.INTENT_REQ_REFRESH);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.community, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (RESULT_OK == resultCode)
		{
			if (Globals.INTENT_REQ_POSTING == requestCode || Globals.INTENT_REQ_REFRESH == requestCode)
				RefreshPosts();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void RefreshPosts()
	{
		pageIndex = 0;
		reqType = REQUEST_TYPE_FIRST;
		m_server.CommunityPosts(this, this, communitySequence, pageIndex);
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
		
		if (REQUEST_TYPE_INFO == reqType)
		{
			if (Globals.ERROR_NONE != result.Code) return;

			m_detail = parser.GetCommunityDetail();
			noResult.setVisibility(View.INVISIBLE);

			RefreshPosts();
		}
		else if (REQUEST_TYPE_FIRST == reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityPosts posts = parser.GetCommunityPosts();
				if (null == posts) return;
				
				if (0 == pageIndex)
					m_adapter.clear();
				
				for (CommunityPostItem item : posts.Items)
					m_adapter.add(item);
				
			}
			else if (Globals.ERROR_NO_RESULT == result.Code)
			{
				noResult.setVisibility(View.VISIBLE);
			}
		}
	}
	
	
	

	
	private class CommunityPostContainer
	{
		public TextView Contents, Writer, ViewCount, ReplyCount, RegDate;
	}
	
	private class CommunityPostAdapter extends ArrayAdapter<CommunityPostItem>
	{
		private Vector<CommunityPostItem> m_items;
		private Activity m_context;
		
		public CommunityPostAdapter(Activity context, Vector<CommunityPostItem> items)
		{
			super(context, R.layout.listitem_community_search, items);

			this.m_context = context;
			this.m_items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			CommunityPostContainer container;
			View rowView = convertView;
			
			if (null == rowView)
			{
				try {
					LayoutInflater inflater = m_context.getLayoutInflater();
					rowView = inflater.inflate(R.layout.listitem_community_post, null, true);					
				} catch (Exception e) {
					Log.d("SW", e.getLocalizedMessage());
					return null;
				}

				container = new CommunityPostContainer();
				container.Contents = (TextView)rowView.findViewById(R.id.postContents);
				container.Writer = (TextView)rowView.findViewById(R.id.writer);
				container.ReplyCount = (TextView)rowView.findViewById(R.id.replies);
				container.ViewCount = (TextView)rowView.findViewById(R.id.viewCount);
				container.RegDate = (TextView)rowView.findViewById(R.id.regDate);
				
				rowView.setTag(container);
			}
			else
			{
				container = (CommunityPostContainer)rowView.getTag();
			}
			
			CommunityPostItem item = m_items.get(position);
			
			container.Contents.setText(item.Contents);
			container.Writer.setText(item.Writer);
			container.ViewCount.setText(Integer.toString(item.ViewCount));
			container.ReplyCount.setText(Integer.toString(item.ReplyCount));
			
			DateFormat sdf = new SimpleDateFormat("yyyy. MM.dd", Locale.US);
			String dateString = sdf.format(item.RegDate);
			container.RegDate.setText(dateString);
			
			return rowView;
		}		
	}	
}
