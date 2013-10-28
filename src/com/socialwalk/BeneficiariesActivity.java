package com.socialwalk;

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
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.BenefitSummary;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.Beneficiaries;
import com.socialwalk.dataclass.Beneficiary;
import com.socialwalk.dataclass.CommunityPosts.CommunityPostItem;
import com.socialwalk.request.ServerRequestManager;

public class BeneficiariesActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener, OnClickListener
{
	private RelativeLayout localButtonLayout, totalButtonLayout;
	private ListView benefitList;
	private boolean isGlobal = false;
	
	private ServerRequestManager server;
	private int reqType;
	private static final int REQUEST_BENEFIT_SUMMARY = 99;
	private static final int REQUEST_BENEFICIARIES = 100;
	private static final int REQUEST_CURRENT_BENEFICIARIES = 101;
	
	private BeneficiariesAdapter adapter;
	
	private ProgressDialog progDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beneficiaries);
		
		this.server = new ServerRequestManager();
		
		this.adapter = new BeneficiariesAdapter(this, new Vector<Beneficiary>());
		this.benefitList = (ListView)findViewById(R.id.itemList);
		this.benefitList.setAdapter(adapter);
		
		localButtonLayout = (RelativeLayout)findViewById(R.id.localButtonLayout);
		totalButtonLayout = (RelativeLayout)findViewById(R.id.totalButtonLayout);
		localButtonLayout.setSelected(true);
		
		localButtonLayout.setOnClickListener(this);
		totalButtonLayout.setOnClickListener(this);
		
		Button globalButton = (Button)findViewById(R.id.globalButton);
		globalButton.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), BeneficiariesListActivity.class);
				i.putExtra(Globals.EXTRA_KEY_IS_GLOBAL_PROJECT, true);
				startActivity(i);
			}
		});
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));

		updateBeneficiaries();
		
		this.benefitList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adpater, View view, int position, long id)
			{
				Beneficiary item = adapter.getItem(position);
				if (null == item) return;

				Intent i = new Intent(getBaseContext(), BeneficiaryDetailActivity.class);
				i.putExtra(Globals.EXTRA_KEY_SEQUENCE, item.Sequence);
				startActivity(i);
			}
		});
	}

	@Override
	public void onClick(View v)
	{
		boolean isGlobal = totalButtonLayout.equals(v);
		if (this.isGlobal == isGlobal) return;
		
		this.isGlobal = isGlobal;
		updateButtonState();
		refreshBeneficiaries();
	}

	private void updateButtonState()
	{
		localButtonLayout.setSelected(!isGlobal);
		totalButtonLayout.setSelected(isGlobal);
	}
	
	private void updateBeneficiaries()
	{
		if (!progDlg.isShowing()) progDlg.show();
		
		this.reqType = REQUEST_BENEFIT_SUMMARY;
		this.server.BenefitSummary(this, this);
	}
	
	private void refreshBeneficiaries()
	{
		if (!progDlg.isShowing()) progDlg.show();

		this.reqType = REQUEST_CURRENT_BENEFICIARIES;
		this.server.CurrentBeneficiaries(this, this);
	}
	

	@Override
	public void onErrorResponse(VolleyError error)
	{
		if (progDlg.isShowing()) progDlg.dismiss();
		
		error.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (progDlg.isShowing()) progDlg.dismiss();

		if (0 == response.length()) return;
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (REQUEST_BENEFIT_SUMMARY == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				BenefitSummary summary = parser.GetBenefitSummry();
				if (null == summary) return;
				
				updateCurrentItem(summary);
			}
			refreshBeneficiaries();
		}		
		else if (REQUEST_CURRENT_BENEFICIARIES == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				Beneficiaries items = parser.GetBeneficiaries();
				if (null == items) return;
				
				for (Beneficiary item : items.Items)
					this.adapter.add(item);
			}
			else if (Globals.ERROR_NO_RESULT == result.Code)
			{
				this.adapter.clear();
				Toast.makeText(this, "후원프로젝트 없음", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void updateCurrentItem(BenefitSummary summary)
	{
		if (null == summary) return;
		
		TextView tvTargetMoney = (TextView)findViewById(R.id.targetMoney);
		TextView tvCurrentMoney = (TextView)findViewById(R.id.currentMoney);
		
		tvTargetMoney.setText(Double.toString(summary.TargetMoney));
		tvCurrentMoney.setText(Double.toString(summary.CurrentMoney));
	}

	
	
	private class BeneficiaryContainer
	{
		public TextView Name, AreaName, BenefitDate, BenefitMoney;
	}

	private class BeneficiariesAdapter extends ArrayAdapter<Beneficiary>
	{
		private Vector<Beneficiary> m_items;
		private Activity m_context;
		
		public BeneficiariesAdapter(Activity context, Vector<Beneficiary> items)
		{
			super(context, R.layout.listitem_beneficiary, items);

			this.m_context = context;
			this.m_items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			BeneficiaryContainer container;
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

				container = new BeneficiaryContainer();
				container.Name = (TextView)rowView.findViewById(R.id.name);
				container.AreaName = (TextView)rowView.findViewById(R.id.areaName);
				container.BenefitDate = (TextView)rowView.findViewById(R.id.benefitDate);
				container.BenefitMoney = (TextView)rowView.findViewById(R.id.benefitMoney);
				
				rowView.setTag(container);
			}
			else
			{
				container = (BeneficiaryContainer)rowView.getTag();
			}
			
			Beneficiary item = m_items.get(position);
			
			container.Name.setText(item.Name);
			container.AreaName.setText(item.AreaSubName);
			
			SimpleDateFormat formatter = new SimpleDateFormat(Globals.DATE_FORMAT_FOR_SERVER, Locale.US);
			container.BenefitDate.setText(formatter.format(item.StartDate));
			container.BenefitMoney.setText(Utils.GetDefaultTool().DecimalNumberString(item.TargetMoney));
			
			return rowView;
		}		
	}	
	
	
	
	
	
}
