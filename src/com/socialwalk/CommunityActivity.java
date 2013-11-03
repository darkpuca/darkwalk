package com.socialwalk;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.CommunityPosts;
import com.socialwalk.dataclass.CommunityPosts.CommunityPostItem;
import com.socialwalk.request.ServerRequestManager;

public class CommunityActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener
{
	private CommunityPostAdapter m_adapter;
	private ServerRequestManager m_server;
	private int communitySequence = 0, reqType = 0, pageIndex = 1, totalCount = 0;
	private TextView noResult;
	
//	private static final int REQUEST_INFO = 100;
	private static final int REQUEST_POSTS = 101;
	private static final int REQUEST_MORE_POSTS = 102;
	
	private ProgressDialog progDlg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community);
		
		m_adapter = new CommunityPostAdapter(this, new Vector<CommunityPostItem>());
		m_server = new ServerRequestManager();
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));

		this.noResult = (TextView)findViewById(R.id.noResult);

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
			refreshPosts();
		}
		
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Globals.INTENT_REQ_POSTING == requestCode)
		{
			refreshPosts();
		}
		else if (Globals.INTENT_REQ_REFRESH == requestCode)
		{
			if (RESULT_OK == resultCode)
				refreshPosts();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void refreshPosts()
	{
		if (!progDlg.isShowing()) progDlg.show();
		pageIndex = 1;
		reqType = REQUEST_POSTS;
		m_server.CommunityPosts(this, this, communitySequence, pageIndex);
	}
	
	private void requestMorePosts()
	{
		if (!progDlg.isShowing()) progDlg.show();
		pageIndex++;
		reqType = REQUEST_MORE_POSTS;
		m_server.CommunityPosts(this, this, communitySequence, pageIndex);
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
		
		if (REQUEST_POSTS == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityPosts posts = parser.GetCommunityPosts();
				if (null == posts) return;

				noResult.setVisibility(View.INVISIBLE);

				if (1 == this.pageIndex)
					m_adapter.clear();
				
				for (CommunityPostItem item : posts.Items)
					m_adapter.add(item);

				this.totalCount = posts.TotalCount;
			}
			else if (Globals.ERROR_NO_RESULT == result.Code)
			{
				this.m_adapter.clear();
				noResult.setVisibility(View.VISIBLE);
			}
		}
		else if (REQUEST_MORE_POSTS == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityPosts posts = parser.GetCommunityPosts();
				if (null == posts) return;

				for (CommunityPostItem item : posts.Items)
					m_adapter.add(item);
			}
			else
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_API_FAIL);
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
			
			if (position == (m_adapter.getCount()-1) && position < (totalCount-1))
				requestMorePosts();
			
			return rowView;
		}		
	}	
}
