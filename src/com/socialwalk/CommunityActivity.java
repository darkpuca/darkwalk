package com.socialwalk;

import java.util.Date;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.CommunityDetail;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.request.ServerRequestManager;

public class CommunityActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener
{
	private CommunityDetail m_detail;
	private CommunityPosts m_posts;
	private CommunityPostAdapter m_adapter;
	private ServerRequestManager m_server;
	private int m_communityId = 0, m_reqType = 0, m_pageIndex = 0;
	private TextView noResult;
	
	private static final int REQUEST_TYPE_INFO = 1;
	private static final int REQUEST_TYPE_FIRST = 2;
	private static final int REQUEST_TYPE_MORE = 3;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community);
		
		m_posts = new CommunityPosts();
		m_adapter = new CommunityPostAdapter(this, m_posts.Items);
		m_server = new ServerRequestManager();
		
		ListView postList = (ListView)findViewById(R.id.postList);
		postList.setAdapter(m_adapter);
		
		Button btnWrite = (Button)findViewById(R.id.btnWrite);
		btnWrite.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), CommunityPostingActivity.class);
				int commId = getIntent().getExtras().getInt(Globals.EXTRA_KEY_COMMUNITY_ID);
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_ID, commId);
				startActivityForResult(i, Globals.INTENT_REQ_POSTING);
			}
		});
		
		if (getIntent().hasExtra(Globals.EXTRA_KEY_COMMUNITY_ID))
		{
			m_communityId = getIntent().getExtras().getInt(Globals.EXTRA_KEY_COMMUNITY_ID);
			m_reqType = REQUEST_TYPE_INFO;
			m_server.CommunityDetail(this, this, m_communityId);
		}
		
		noResult = (TextView)findViewById(R.id.noResult);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.community, menu);
		return true;
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
		
		if (REQUEST_TYPE_INFO == m_reqType)
		{
			if (Globals.ERROR_NONE != result.Code) return;

			m_detail = parser.GetCommunityDetail();
			
			TextView name = (TextView)findViewById(R.id.name);
			name.setText(m_detail.Name);
			noResult.setVisibility(View.INVISIBLE);
			
			m_pageIndex = 0;
			m_reqType = REQUEST_TYPE_FIRST;
			m_server.CommunityPosts(this, this, m_communityId, m_pageIndex);
		}
		else if (REQUEST_TYPE_FIRST == m_reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				
			}
			else if (Globals.ERROR_NO_RESULT == result.Code)
			{
				noResult.setVisibility(View.VISIBLE);
			}
		}
	}
	
	
	


	public class CommunityPostItem
	{
		public int PostId;
		public String UserSequence, Writer, Subject, Content;
		public int ViewCount, ReplyCount;
		public Date RegDate;
	}
	
	public class CommunityPosts
	{
		public int TotalCount;
		Vector<CommunityPostItem> Items;
		
		public CommunityPosts()
		{
			this.Items = new Vector<CommunityPostItem>();
		}
	}
	
	private class CommunityPostContainer
	{
		public TextView Subject, Writer, ViewCount, ReplyCount;
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
				container.Subject = (TextView)rowView.findViewById(R.id.subject);
				container.Writer = (TextView)rowView.findViewById(R.id.writer);
				container.ReplyCount = (TextView)rowView.findViewById(R.id.replyCount);
				container.ViewCount = (TextView)rowView.findViewById(R.id.viewCount);
				
				rowView.setTag(container);
			}
			else
			{
				container = (CommunityPostContainer)rowView.getTag();
			}
			
			CommunityPostItem item = m_items.get(position);
			
			container.Subject.setText(item.Subject);
			container.Writer.setText(item.Writer);
			container.ViewCount.setText(Integer.toString(item.ViewCount));
			container.ReplyCount.setText(Integer.toString(item.ReplyCount));
			
			return rowView;
		}		
	}	
}
