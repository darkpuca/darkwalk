package com.socialwalk;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.HeartRanks;
import com.socialwalk.dataclass.HeartRanks.RankItem;
import com.socialwalk.request.ServerRequestManager;

public class RankingsActivity extends ExpandableListActivity 
implements OnClickListener, Response.Listener<String>, Response.ErrorListener
{
	private boolean IsTotalRanking = false;
	private RelativeLayout memberButtonLayout, globalButtonLayout;
	
	private ArrayList<HeartRanks.RankItem> personalRankings, groupSumRankings, groupAvgRankings;

	private ServerRequestManager server;
	private int reqType;
	private static final int REQUEST_RANKINGS = 100;
	
	private ProgressDialog progDlg;
	
    private ArrayList<String> rankingTitles = new ArrayList<String>();
    private ArrayList<ArrayList<HeartRanks.RankItem>> rankingsItems = new ArrayList<ArrayList<HeartRanks.RankItem>>();
    
    private ExpandableListView rankList;
    private RankingsExpandableAdapter rankingsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rankings);
		
		this.server = new ServerRequestManager();
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));
		
		this.memberButtonLayout = (RelativeLayout)findViewById(R.id.memberButtonLayout);
		this.globalButtonLayout = (RelativeLayout)findViewById(R.id.globalButtonLayout);
		memberButtonLayout.setOnClickListener(this);
		globalButtonLayout.setOnClickListener(this);
		
		TextView localLabel = (TextView)findViewById(R.id.labelLocal);
		if (ServerRequestManager.LoginAccount.IsGroupUser)
			localLabel.setText(R.string.BY_GROUP);
		else
			localLabel.setText(R.string.BY_LOCAL);
		
		updateTypeButtons();
		
		// Create Expandable List and set it's properties
        rankList = getExpandableListView(); 
        rankList.setDividerHeight(2);
        rankList.setGroupIndicator(null);
        rankList.setClickable(false);

        // set ranking titles
		this.rankingTitles.add(getResources().getString(R.string.PERSONAL_RANKING));
		this.rankingTitles.add(getResources().getString(R.string.GROUP_RANKING));
		this.rankingTitles.add(getResources().getString(R.string.AVERAGE_RANKING));
		
		this.personalRankings = new ArrayList<HeartRanks.RankItem>();
		this.groupSumRankings = new ArrayList<HeartRanks.RankItem>();
		this.groupAvgRankings = new ArrayList<HeartRanks.RankItem>();
		
		this.rankingsItems.add(personalRankings);
		this.rankingsItems.add(groupSumRankings);
		this.rankingsItems.add(groupAvgRankings);
		
        // Create the Adapter
        this.rankingsAdapter = new RankingsExpandableAdapter(this.rankingTitles, this.rankingsItems);

        rankingsAdapter.setInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), this);
        
        // Set the Adapter to expandableList
        rankList.setAdapter(rankingsAdapter);
        rankList.setOnChildClickListener(this);

		requestRankings();
	}
	
	private void requestRankings()
	{
		if (!progDlg.isShowing()) progDlg.show();
		this.reqType = REQUEST_RANKINGS;
		this.server.Rankings(this, this, this.IsTotalRanking);
	}
	
	private void updateTypeButtons()
	{
		if (null == memberButtonLayout || null == globalButtonLayout) return;
		
		memberButtonLayout.setSelected(!IsTotalRanking);
		globalButtonLayout.setSelected(IsTotalRanking);
	}

	@Override
	public void onClick(View v)
	{
		IsTotalRanking = globalButtonLayout.equals(v);
		updateTypeButtons();
		requestRankings();
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
		
		if (REQUEST_RANKINGS == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				HeartRanks ranks = parser.GetRanks();
				if (null == ranks) return;
				
				this.personalRankings.clear();
				this.groupSumRankings.clear();
				this.groupAvgRankings.clear();
				
				this.personalRankings.addAll(ranks.PersonalItems);
				this.groupSumRankings.addAll(ranks.GroupSumItems);
				this.groupAvgRankings.addAll(ranks.GroupAvgItems);
				
				this.rankingsAdapter.ShowLocalField(IsTotalRanking);
								
				this.rankingsAdapter.notifyDataSetChanged();
				this.rankList.smoothScrollToPosition(0);
				
				this.rankList.expandGroup(0);
//				this.rankList.expandGroup(1);
//				this.rankList.expandGroup(2);
			}
			else
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_API_FAIL);
			}
		}
	}	
	
	
	
	
	
	
	
	

	
	private class RankingItemContainer
	{
		public TextView Ranking, Name, LocalName, CommunityName, MemberCount, Hearts;
	}
	
	
	public class RankingsExpandableAdapter extends BaseExpandableListAdapter 
	{
		private boolean show_local = false, is_group = false;
	    private Activity activity;
	    private ArrayList<ArrayList<HeartRanks.RankItem>> rankingsItems;
	    private LayoutInflater inflater;
	    private ArrayList<String> titles;
	    private ArrayList<HeartRanks.RankItem> rankings;

	    // constructor
	    public RankingsExpandableAdapter(ArrayList<String> titles, ArrayList<ArrayList<HeartRanks.RankItem>> rankingsItems)
	    {
	        this.titles = titles;
	        this.rankingsItems = rankingsItems;
	    }

	    public void setInflater(LayoutInflater inflater, Activity activity) 
	    {
	        this.inflater = inflater;
	        this.activity = activity;
	    }
	    
	    public void ShowLocalField(boolean is_show)
	    {
	    	this.show_local = is_show;
	    }
	    
	    public void IsGroupList(boolean is_group)
	    {
	    	this.is_group = is_group;
	    }
	    
	    // method getChildView is called automatically for each child view.
	    //  Implement this method as per your requirement
	    @Override
	    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
	    {
	        rankings = rankingsItems.get(groupPosition);
	        
			RankingItemContainer container;
			View rowView = convertView;
			
			if (null == rowView)
			{
				try {
					rowView = inflater.inflate(R.layout.listitem_ranking, null, true);					
				} catch (Exception e) {
					Log.d("SW", e.getLocalizedMessage());
					return null;
				}

				container = new RankingItemContainer();
				container.Ranking = (TextView)rowView.findViewById(R.id.ranking);
				container.Name = (TextView)rowView.findViewById(R.id.name);
				container.LocalName = (TextView)rowView.findViewById(R.id.localName);
				container.CommunityName = (TextView)rowView.findViewById(R.id.communityName);
				container.MemberCount = (TextView)rowView.findViewById(R.id.memberCount);
				container.Hearts = (TextView)rowView.findViewById(R.id.hearts);
				
				rowView.setTag(container);
			}
			else
			{
				container = (RankingItemContainer)rowView.getTag();
			}
			
			RankItem item = rankings.get(childPosition);
			container.Ranking.setText(Integer.toString(childPosition+1));
			container.LocalName.setText(item.CommunityName);
			container.LocalName.setText(item.LocalName);
			
			this.is_group = (0 != groupPosition);
			
			if (this.is_group)
			{
				container.Hearts.setText(Utils.GetDefaultTool().DecimalNumberString(item.GroupPoint));
				container.MemberCount.setVisibility(View.VISIBLE);
				container.CommunityName.setVisibility(View.GONE);

				if (null == item.CommunityName || 0 == item.CommunityName.length())
					container.Name.setText(R.string.NO_GROUP);
				else
					container.Name.setText(item.CommunityName);
				
				String memberCount = String.format(getResources().getString(R.string.MEMBER_COUNT) + " %s" + getResources().getString(R.string.MAN_UNIT), Utils.GetDefaultTool().DecimalNumberString(item.Members));
				container.MemberCount.setText(memberCount);
				
			}
			else
			{
				container.Hearts.setText(Utils.GetDefaultTool().DecimalNumberString(item.UserPoint));
				container.MemberCount.setVisibility(View.GONE);
				container.CommunityName.setVisibility(View.VISIBLE);

				container.Name.setText(item.Name);
				if (null == item.CommunityName || 0 == item.CommunityName.length())
					container.CommunityName.setText(R.string.NO_GROUP);
				else
					container.CommunityName.setText(item.CommunityName);
			}
			
			if (this.show_local)
				container.LocalName.setVisibility(View.VISIBLE);
			else
				container.LocalName.setVisibility(View.GONE);
			
			return rowView;
	    }

	    // method getGroupView is called automatically for each parent item
	    // Implement this method as per your requirement
	    @Override
	    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) 
	    {

	        if (convertView == null) {
	            convertView = inflater.inflate(R.layout.listitem_ranking_title, null);
	        }
	        
	        CheckedTextView titleView = (CheckedTextView)convertView.findViewById(R.id.ranking_title);
	        titleView.setText(this.titles.get(groupPosition));
	        titleView.setChecked(isExpanded);
	        
	        ImageView expandImage = (ImageView)convertView.findViewById(R.id.expand_state);
	        
	        if (isExpanded)
	        	expandImage.setImageResource(R.drawable.rank_expanded);
	        else
	        	expandImage.setImageResource(R.drawable.rank_collapsed);
	        
	        return convertView;
	    }

	    @Override
	    public Object getChild(int groupPosition, int childPosition) 
	    {
	        return null;
	    }

	    @Override
	    public long getChildId(int groupPosition, int childPosition) 
	    {
	        return 0;
	    }

	    @Override
	    public int getChildrenCount(int groupPosition) 
	    {
	    	ArrayList<HeartRanks.RankItem> rankings = rankingsItems.get(groupPosition);
	        return rankings.size();
	    }

	    @Override
	    public Object getGroup(int groupPosition) 
	    {
	        return null;
	    }

	    @Override
	    public int getGroupCount() 
	    {
	        return titles.size();
	    }

	    @Override
	    public void onGroupCollapsed(int groupPosition) 
	    {
	    	
	        super.onGroupCollapsed(groupPosition);
	    }

	    @Override
	    public void onGroupExpanded(int groupPosition)
	    {
	        super.onGroupExpanded(groupPosition);
	    }

	    @Override
	    public long getGroupId(int groupPosition) 
	    {
	        return 0;
	    }

	    @Override
	    public boolean hasStableIds() 
	    {
	        return false;
	    }

	    @Override
	    public boolean isChildSelectable(int groupPosition, int childPosition)
	    {
	        return false;
	    }

	}
	
	


}
