package com.socialwalk;

import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.HeartRanks;
import com.socialwalk.dataclass.HeartRanks.RankItem;
import com.socialwalk.request.ServerRequestManager;

public class RankingActivity extends Activity 
implements OnClickListener, Response.Listener<String>, Response.ErrorListener
{
	private boolean IsTotalRanking = false;
	private RelativeLayout memberButtonLayout, globalButtonLayout;
	
	private ListView personalList, groupSumList, groupAvgList;
	private RankingItemAdapter personalAdapter, groupSumAdapter, groupAvgAdapter;

	private ServerRequestManager server;
	private int reqType;
	private static final int REQUEST_RANKINGS = 100;
	
	private ProgressDialog progDlg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ranking);
		
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
		
		updateTypeButtons();
		
		requestRankings();
		
		
		this.personalAdapter = new RankingItemAdapter(this, new Vector<HeartRanks.RankItem>());
		this.groupSumAdapter = new RankingItemAdapter(this, new Vector<HeartRanks.RankItem>());
		this.groupAvgAdapter = new RankingItemAdapter(this, new Vector<HeartRanks.RankItem>());
		
		this.groupSumAdapter.IsGroupList(true);
		this.groupAvgAdapter.IsGroupList(true);
		
		this.personalList = (ListView)findViewById(R.id.personalList);
		this.groupSumList = (ListView)findViewById(R.id.groupSumList);
		this.groupAvgList = (ListView)findViewById(R.id.groupAvgList);

		this.personalList.setAdapter(personalAdapter);
		this.groupSumList.setAdapter(groupSumAdapter);
		this.groupAvgList.setAdapter(groupAvgAdapter);
		
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
				
				this.personalAdapter.clear();
				this.groupSumAdapter.clear();
				this.groupAvgAdapter.clear();
				
				for (RankItem item : ranks.PersonalItems)
					this.personalAdapter.add(item);
				
				for (RankItem item : ranks.GroupSumItems)
					this.groupSumAdapter.add(item);
				
				for (RankItem item : ranks.GroupAvgItems)
					this.groupAvgAdapter.add(item);
				
				this.personalAdapter.ShowLocalField(IsTotalRanking);
				this.groupSumAdapter.ShowLocalField(IsTotalRanking);
				this.groupAvgAdapter.ShowLocalField(IsTotalRanking);
			}
			else
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_API_FAIL);
			}
		}
	}	
	
	
	
	
	
	
	
	

	
	private class RankingItemContainer
	{
		public TextView Ranking, Name, LocalName, Hearts;
	}

	private class RankingItemAdapter extends ArrayAdapter<RankItem>
	{
		private Vector<RankItem> m_items;
		private Activity m_context;
		private boolean isGroupList, showLocal;
		
		public RankingItemAdapter(Activity context, Vector<RankItem> items)
		{
			super(context, R.layout.listitem_ranking, items);

			this.m_context = context;
			this.m_items = items;
			this.isGroupList = false;
			this.showLocal = false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			RankingItemContainer container;
			View rowView = convertView;
			
			if (null == rowView)
			{
				try {
					LayoutInflater inflater = m_context.getLayoutInflater();
					rowView = inflater.inflate(R.layout.listitem_ranking, null, true);					
				} catch (Exception e) {
					Log.d("SW", e.getLocalizedMessage());
					return null;
				}

				container = new RankingItemContainer();
				container.Ranking = (TextView)rowView.findViewById(R.id.ranking);
				container.Name = (TextView)rowView.findViewById(R.id.name);
				container.LocalName = (TextView)rowView.findViewById(R.id.localName);
				container.Hearts = (TextView)rowView.findViewById(R.id.hearts);
				
				rowView.setTag(container);
			}
			else
			{
				container = (RankingItemContainer)rowView.getTag();
			}
			
			RankItem item = m_items.get(position);
			container.Ranking.setText(Integer.toString(position+1));
//			container.LocalName.setText(item.CommunityName);
			container.Hearts.setText(Utils.GetDefaultTool().DecimalNumberString(item.UserPoint));
			container.LocalName.setText(item.LocalName);
			
			if (this.isGroupList)
				container.Name.setText(item.CommunityName);
			else
				container.Name.setText(item.Name);
			
			if (this.showLocal)
				container.LocalName.setVisibility(View.VISIBLE);
			else
				container.LocalName.setVisibility(View.INVISIBLE);
			
			return rowView;
		}
		
		public void IsGroupList(boolean isGroup)
		{
			this.isGroupList = isGroup;
		}
		
		public void ShowLocalField(boolean isShow)
		{
			this.showLocal = isShow;			
		}
		
	}
	
	
	
	
	


}
