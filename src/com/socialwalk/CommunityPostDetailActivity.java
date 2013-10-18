package com.socialwalk;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.CommunityPostReplies;
import com.socialwalk.dataclass.CommunityPostReplies.CommunityPostReply;
import com.socialwalk.dataclass.CommunityPosts.CommunityPostItem;
import com.socialwalk.request.ServerRequestManager;

public class CommunityPostDetailActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener
{
	private String writerSequence;
	private int communitySequence, postSequence;
	private ServerRequestManager m_server;
	private CommunityReplyAdapter m_adapter;
	private int reqType, pageIndex;
	private ListView repliesList;
	
	private static final int REQUEST_POST_DETAIL = 100;
	private static final int REQUEST_REPLIES = 101;
	private static final int REQUEST_POST_DELETE = 102;
	private static final int REQUEST_REPLY_DELETE = 103;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_post_detail);
		
		m_server = new ServerRequestManager();
		
		m_adapter = new CommunityReplyAdapter(this, new Vector<CommunityPostReply>());
		repliesList = (ListView)findViewById(R.id.repliesList);
		repliesList.setAdapter(m_adapter);
		registerForContextMenu(repliesList);
		
		this.writerSequence = getIntent().getStringExtra(Globals.EXTRA_KEY_WRITER_SEQUENCE);
		this.communitySequence = getIntent().getIntExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, 0);
		this.postSequence = getIntent().getIntExtra(Globals.EXTRA_KEY_POST_SEQUENCE, 0);
		
		if (0 == this.communitySequence || 0 == this.postSequence)
		{
			new AlertDialog.Builder(this)
			.setTitle(R.string.TITLE_INFORMATION)
			.setMessage(R.string.MSG_INVALID_POST)
			.setNeutralButton(R.string.CLOSE, null)
			.show();
		}
		else
		{
			reqType = REQUEST_POST_DETAIL;
			m_server.CommunityPostDetail(this, this, this.postSequence);
		}
		
		Button btnReply = (Button)findViewById(R.id.btnReply);
		btnReply.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), ReplyPostingActivity.class);
				i.putExtra(Globals.EXTRA_KEY_POST_SEQUENCE, postSequence);
				startActivityForResult(i, Globals.INTENT_REQ_POSTING);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.community_post_detail, menu);
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		if (null != ServerRequestManager.LoginAccount)
		{
			if (ServerRequestManager.LoginAccount.Sequence.equalsIgnoreCase(writerSequence))
			{
				getMenuInflater().inflate(R.menu.reply_popup, menu);
				super.onCreateContextMenu(menu, v, menuInfo);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();

		if (R.id.action_delete == item.getItemId())
		{
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			dlg.setCancelable(false);
			dlg.setTitle(R.string.TITLE_INFORMATION);
			dlg.setMessage(R.string.MSG_REPLY_DELETE_CONFIRM);
			dlg.setPositiveButton(R.string.CONTINUE, new DialogInterface.OnClickListener()
			{	
				@Override
				public void onClick(DialogInterface dialog, int which)
				{	
					CommunityPostReply reply = m_adapter.getItem(menuInfo.position);
					reqType = REQUEST_REPLY_DELETE;
					m_server.ReplyDelete(CommunityPostDetailActivity.this, CommunityPostDetailActivity.this, reply.Sequence);
				}
			});
			
			dlg.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener()
			{				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			dlg.show();
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.getItem(0).setEnabled(true);

		if (null != ServerRequestManager.LoginAccount)
		{
			if (ServerRequestManager.LoginAccount.Sequence.equalsIgnoreCase(writerSequence))
			{
				menu.getItem(0).setEnabled(true);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (R.id.action_post_delete == item.getItemId())
		{
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			dlg.setCancelable(false);
			dlg.setTitle(R.string.TITLE_INFORMATION);
			dlg.setMessage(R.string.MSG_POST_DELETE_CONFIRM);
			dlg.setPositiveButton(R.string.CONTINUE, new DialogInterface.OnClickListener()
			{	
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					reqType = REQUEST_POST_DELETE;
					m_server.CommunityPostDelete(CommunityPostDetailActivity.this, CommunityPostDetailActivity.this, postSequence);
				}
			});
			
			dlg.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener()
			{				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			dlg.show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Globals.INTENT_REQ_POSTING == requestCode && RESULT_OK == resultCode)
		{
			RefreshReplies();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void UpdateContents(CommunityPostItem item)
	{
		if (null == item) return;
		
		TextView writer = (TextView)findViewById(R.id.writer);
		TextView regDate = (TextView)findViewById(R.id.regDate);
		TextView contents = (TextView)findViewById(R.id.contents);
		TextView viewCount = (TextView)findViewById(R.id.viewCount);
		TextView replyCount = (TextView)findViewById(R.id.replyCount);
		
		DateFormat sdf = new SimpleDateFormat("yyyy. MM.dd", Locale.US);
		String dateString = sdf.format(item.RegDate);
		
		writer.setText(item.Writer);
		regDate.setText(dateString);
		contents.setText(item.Contents);
		viewCount.setText(Integer.toString(item.ViewCount));
		replyCount.setText(Integer.toString(item.ReplyCount));
	}
	
	private void RefreshReplies()
	{
		pageIndex = 0;
		reqType = REQUEST_REPLIES;
		m_server.CommunityReplies(this, this, this.postSequence, pageIndex);
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
		
		if (REQUEST_POST_DETAIL == reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityPostItem postItem = parser.GetCommunityPostItem();
				if (null != postItem)
					UpdateContents(postItem);
				RefreshReplies();
			}
		}
		else if (REQUEST_REPLIES == reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityPostReplies replies = parser.GetCommunityReplies();
				if (null == replies) return;
				
				if (0 == pageIndex)
					m_adapter.clear();
					
				for (CommunityPostReply item : replies.Items)
					m_adapter.add(item);
				
				TextView replyCount = (TextView)findViewById(R.id.replyCount);
				replyCount.setText(Integer.toString(m_adapter.getCount()));
			}
			else if (Globals.ERROR_NO_RESULT == result.Code)
			{
				m_adapter.clear();
			}
		}
		else if (REQUEST_POST_DELETE == reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				setResult(RESULT_OK);
				finish();
			}
		}
		else if (REQUEST_REPLY_DELETE == reqType)
		{
			if (Globals.ERROR_NONE == result.Code || Globals.ERROR_NO_RESULT == result.Code)
				RefreshReplies();
		}
	}
	
	
	private class CommunityReplyContainer
	{
		public TextView Contents, Writer, RegDate;
	}
	
	private class CommunityReplyAdapter extends ArrayAdapter<CommunityPostReply>
	{
		private Vector<CommunityPostReply> m_items;
		private Activity m_context;
		
		public CommunityReplyAdapter(Activity context, Vector<CommunityPostReply> items)
		{
			super(context, R.layout.listitem_community_search, items);

			this.m_context = context;
			this.m_items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			CommunityReplyContainer container;
			View rowView = convertView;
			
			if (null == rowView)
			{
				try {
					LayoutInflater inflater = m_context.getLayoutInflater();
					rowView = inflater.inflate(R.layout.listitem_community_reply, null, true);					
				} catch (Exception e) {
					Log.d("SW", e.getLocalizedMessage());
					return null;
				}

				container = new CommunityReplyContainer();
				container.Contents = (TextView)rowView.findViewById(R.id.contents);
				container.Writer = (TextView)rowView.findViewById(R.id.writer);
				container.RegDate = (TextView)rowView.findViewById(R.id.regDate);
				
				rowView.setTag(container);
			}
			else
			{
				container = (CommunityReplyContainer)rowView.getTag();
			}
			
			CommunityPostReply item = m_items.get(position);
			
			container.Contents.setText(item.Contents);
			container.Writer.setText(item.Writer);
			
			DateFormat sdf = new SimpleDateFormat("yyyy. MM.dd", Locale.US);
			String dateString = sdf.format(item.RegDate);
			container.RegDate.setText(dateString);
			
			return rowView;
		}		
	}	
	
	
}
