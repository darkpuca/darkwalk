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
import android.widget.RelativeLayout;

public class RankingActivity extends Activity implements OnClickListener
{
	private boolean IsTotalRanking = false;
	private RelativeLayout memberButtonLayout, globalButtonLayout;
	
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


}
