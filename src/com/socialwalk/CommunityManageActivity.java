package com.socialwalk;

import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.CommunityDetail;
import com.socialwalk.dataclass.CommunityMembers;
import com.socialwalk.dataclass.CommunityMembers.CommunityMemberItem;
import com.socialwalk.request.ServerRequestManager;

public class CommunityManageActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener
{
	private int communitySequence;
	
	private ServerRequestManager server;
	private int reqType, memberCount, memberPageIndex, applicantCount, applicantPageIndex;
	
	private static final int REQUEST_DETAIL = 100;
	private static final int REQUEST_MEMBERS = 101;
	private static final int REQUEST_MORE_MEMBERS = 102;
	private static final int REQUEST_APPLICANTS = 103;
	private static final int REQUEST_MORE_APPLICANTS = 104;
	
	private ListView membersList, applicantsList;
	private CommunityMemberAdapter memberAdapter, applicantAdapter;
	
	private ProgressDialog progDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_manage);
		
		this.server = new ServerRequestManager();
		
		this.communitySequence = getIntent().getIntExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, 0);
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));

		this.memberAdapter = new CommunityMemberAdapter(this, new Vector<CommunityMemberItem>());
		this.applicantAdapter = new CommunityMemberAdapter(this, new Vector<CommunityMemberItem>());
		this.applicantAdapter.IsApplicant = true;
		
		this.membersList = (ListView)findViewById(R.id.membersList);
		this.applicantsList = (ListView)findViewById(R.id.applicantsList);
		
		this.membersList.setAdapter(this.memberAdapter);
		this.applicantsList.setAdapter(this.applicantAdapter);
		
		if (0 < this.communitySequence)
		{
			progDlg.show();
			this.reqType = REQUEST_DETAIL;
			this.server.CommunityDetail(this, this, this.communitySequence);
		}
	}
	
	private void updateCommunityDetail(CommunityDetail detail)
	{
		TextView tvName = (TextView)findViewById(R.id.communityName);
		TextView tvMembers = (TextView)findViewById(R.id.communityMembers);
		
		tvName.setText(detail.Name);
		tvMembers.setText(Integer.toString(detail.MemberCount) + getResources().getString(R.string.MAN_UNIT));
	}
	
	

	@Override
	public void onErrorResponse(VolleyError error)
	{
		if (progDlg.isShowing()) progDlg.dismiss();

		error.printStackTrace();
		Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_API_FAIL);
	}

	@Override
	public void onResponse(String response)
	{
		if (progDlg.isShowing()) progDlg.dismiss();

		if (0 == response.length()) return;
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (REQUEST_DETAIL == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityDetail detail = parser.GetCommunityDetail();
				if (null != detail)
					updateCommunityDetail(detail);
			}
			
			requestMembers();
		}
		else if (REQUEST_MEMBERS == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityMembers members = parser.GetCommunityMembers();
				if (null != members)
				{
					this.memberCount = members.TotalCount;
					
					memberAdapter.clear();
					
					for (CommunityMemberItem item : members.Items)
						memberAdapter.add(item);
				}
			}
			requestApplicants();
		}
		else if (REQUEST_APPLICANTS == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityMembers members = parser.GetCommunityMembers();
				if (null != members)
				{
					this.applicantCount = members.TotalCount;
					
					applicantAdapter.clear();
					
					for (CommunityMemberItem item : members.Items)
						applicantAdapter.add(item);
				}
			}
		}
		else if (REQUEST_MORE_MEMBERS == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityMembers members = parser.GetCommunityMembers();
				if (null != members)
				{
					for (CommunityMemberItem item : members.Items)
						memberAdapter.add(item);
				}
			}
		}
		else if (REQUEST_MORE_APPLICANTS == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				CommunityMembers members = parser.GetCommunityMembers();
				if (null != members)
				{
					for (CommunityMemberItem item : members.Items)
						applicantAdapter.add(item);
				}
			}
		}
	}

	private void requestMembers()
	{
		if (!progDlg.isShowing()) progDlg.show();
		
		this.reqType = REQUEST_MEMBERS;
		this.memberPageIndex = 1;
		this.server.CommunityMembers(this, this, this.communitySequence, this.memberPageIndex, false);
	}

	private void requestApplicants()
	{
		if (!progDlg.isShowing()) progDlg.show();
		
		this.reqType = REQUEST_APPLICANTS;
		this.applicantPageIndex = 1;
		this.server.CommunityMembers(this, this, this.communitySequence, applicantPageIndex, true);
	}
	
	private void requestMoreMembers()
	{
		if (!progDlg.isShowing()) progDlg.show();
		
		this.reqType = REQUEST_MORE_MEMBERS;
		this.memberPageIndex++;
		this.server.CommunityMembers(this, this, this.communitySequence, this.memberPageIndex, false);
		
	}
	
	private void requestMoreApplicants()
	{
		if (!progDlg.isShowing()) progDlg.show();
		
		this.reqType = REQUEST_MORE_APPLICANTS;
		this.applicantPageIndex++;
		this.server.CommunityMembers(this, this, this.communitySequence, applicantPageIndex, true);
	}
	
	
	
	
	
	
	
	
	
	
	
	private class CommunityMemberContainer
	{
		public TextView Name, Email;
		public Button allowButton, denyButton;
	}

	private class CommunityMemberAdapter extends ArrayAdapter<CommunityMemberItem>
	{
		private Vector<CommunityMemberItem> m_items;
		private Activity m_context;
		
		public boolean IsApplicant;
		
		public CommunityMemberAdapter(Activity context, Vector<CommunityMemberItem> items)
		{
			super(context, R.layout.listitem_community_member, items);

			this.m_context = context;
			this.m_items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			CommunityMemberContainer container;
			View rowView = convertView;
			
			if (null == rowView)
			{
				try {
					LayoutInflater inflater = m_context.getLayoutInflater();
					rowView = inflater.inflate(R.layout.listitem_community_member, null, true);					
				} catch (Exception e) {
					Log.d("SW", e.getLocalizedMessage());
					return null;
				}

				container = new CommunityMemberContainer();
				container.Name = (TextView)rowView.findViewById(R.id.memberName);
				container.Email = (TextView)rowView.findViewById(R.id.memberEmail);
				container.allowButton = (Button)rowView.findViewById(R.id.allowButton);
				container.denyButton = (Button)rowView.findViewById(R.id.denyButton);
				
				rowView.setTag(container);
			}
			else
			{
				container = (CommunityMemberContainer)rowView.getTag();
			}
			
			CommunityMemberItem item = m_items.get(position);
			
			container.Name.setText(item.Name);
			container.Email.setText(item.Email);
			
			if (true == this.IsApplicant)
			{
				container.allowButton.setVisibility(View.VISIBLE);
				container.denyButton.setVisibility(View.VISIBLE);
				
				if (position == (this.getCount()-1) && position < (applicantCount-1))
					requestMoreApplicants();
			}
			else
			{
				container.allowButton.setVisibility(View.GONE);
				container.denyButton.setVisibility(View.GONE);

				if (position == (this.getCount()-1) && position < (memberCount-1))
					requestMoreMembers();
			}
			return rowView;
		}
	}	

	
	
	
	

}
