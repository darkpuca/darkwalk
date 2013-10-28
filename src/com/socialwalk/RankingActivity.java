package com.socialwalk;

import java.util.Vector;

import android.app.Activity;
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

import com.socialwalk.dataclass.Beneficiary;
import com.socialwalk.dataclass.RankingItem;

public class RankingActivity extends Activity implements OnClickListener
{
	private boolean IsTotalRanking = false;
	private RelativeLayout memberButtonLayout, globalButtonLayout;
	
	private RankingItemAdapter adapter;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ranking);
		
		this.memberButtonLayout = (RelativeLayout)findViewById(R.id.memberButtonLayout);
		this.globalButtonLayout = (RelativeLayout)findViewById(R.id.globalButtonLayout);
		memberButtonLayout.setOnClickListener(this);
		globalButtonLayout.setOnClickListener(this);
		
		UpdateTypeButtons();
		
		ListView rankingList = (ListView)findViewById(R.id.rankingList);
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Globals.sampleStrings);
		rankingList.setAdapter(aa);
	}
	
	private void UpdateTypeButtons()
	{
		if (null == memberButtonLayout || null == globalButtonLayout) return;
		
		memberButtonLayout.setSelected(!IsTotalRanking);
		globalButtonLayout.setSelected(IsTotalRanking);
	}

	@Override
	public void onClick(View v)
	{
		IsTotalRanking = globalButtonLayout.equals(v);
		UpdateTypeButtons();
	}
	
	
	
	
	
	
	
	
	
	

	
	private class RankingItemContainer
	{
		public TextView Name, GroupName, Hearts;
	}

	private class RankingItemAdapter extends ArrayAdapter<RankingItem>
	{
		private Vector<RankingItem> m_items;
		private Activity m_context;
		
		public RankingItemAdapter(Activity context, Vector<RankingItem> items)
		{
			super(context, R.layout.listitem_ranking, items);

			this.m_context = context;
			this.m_items = items;
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
					rowView = inflater.inflate(R.layout.listitem_beneficiary, null, true);					
				} catch (Exception e) {
					Log.d("SW", e.getLocalizedMessage());
					return null;
				}

				container = new RankingItemContainer();
				container.Name = (TextView)rowView.findViewById(R.id.name);
				container.GroupName = (TextView)rowView.findViewById(R.id.groupName);
				container.Hearts = (TextView)rowView.findViewById(R.id.hearts);
				
				rowView.setTag(container);
			}
			else
			{
				container = (RankingItemContainer)rowView.getTag();
			}
			
			RankingItem item = m_items.get(position);
			
			container.Name.setText(item.Name);
			container.GroupName.setText(item.GroupName);
			
			container.Hearts.setText(Utils.GetDefaultTool().DecimalNumberString(item.Hearts));
			
			return rowView;
		}		
	}	
	
	
	


}
