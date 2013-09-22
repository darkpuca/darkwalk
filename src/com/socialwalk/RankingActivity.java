package com.socialwalk;

import android.os.Bundle;
import android.R.anim;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class RankingActivity extends Activity implements OnClickListener
{
	private boolean IsTotalRanking = false;
	private Button m_btnByGroup, m_btnTotal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ranking);
		
		m_btnByGroup = (Button)findViewById(R.id.btnByGroup);
		m_btnTotal = (Button)findViewById(R.id.btnTotal);
		m_btnByGroup.setOnClickListener(this);
		m_btnTotal.setOnClickListener(this);
		
		UpdateTypeButtons();
		
		ListView rankingList = (ListView)findViewById(R.id.rankingList);
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Globals.sampleStrings);
		rankingList.setAdapter(aa);
	}
	
	private void UpdateTypeButtons()
	{
		if (null == m_btnByGroup || null == m_btnTotal) return;
		
		m_btnByGroup.setSelected(!IsTotalRanking);
		m_btnTotal.setSelected(IsTotalRanking);
	}

	@Override
	public void onClick(View v)
	{
		IsTotalRanking = m_btnTotal.equals(v);
		UpdateTypeButtons();
	}


}
