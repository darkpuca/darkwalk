package com.socialwalk;

import java.text.SimpleDateFormat;
import java.util.Locale;
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
import com.socialwalk.dataclass.Beneficiaries;
import com.socialwalk.dataclass.Beneficiary;
import com.socialwalk.request.ServerRequestManager;

public class BeneficiariesActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener, OnClickListener
{
	private RelativeLayout localButtonLayout, totalButtonLayout;
	private ListView benefitList;
	private boolean isGlobal = false;
	
	private ServerRequestManager server;
	private int reqType, pageIndex = 0, totalCount = 0;
	private static final int REQUEST_BENEFICIARIES = 100;
	
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
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));

		refreshBeneficiaries();
	}

	@Override
	public void onClick(View v)
	{
		this.isGlobal = totalButtonLayout.equals(v);
		updateButtonState();
		refreshBeneficiaries();
	}

	private void updateButtonState()
	{
		localButtonLayout.setSelected(!isGlobal);
		totalButtonLayout.setSelected(isGlobal);
	}
	
	private void refreshBeneficiaries()
	{
		progDlg.show();

		this.pageIndex = 0;
		this.reqType = REQUEST_BENEFICIARIES;
		this.server.Beneficiaries(this, this, isGlobal, pageIndex);
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
		
		if (REQUEST_BENEFICIARIES == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				Beneficiaries items = parser.GetBeneficiaries();
				if (null == items) return;
				
				if (0 == pageIndex)
					this.adapter.clear();
				
				this.totalCount = items.TotalCount;
				
				for (Beneficiary item : items.Items)
					this.adapter.add(item);

			}
		}
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
			container.AreaName.setText(item.AreaName);
			
			SimpleDateFormat formatter = new SimpleDateFormat(Globals.DATE_FORMAT_FOR_SERVER, Locale.US);
			container.BenefitDate.setText(formatter.format(item.StartDate));
			container.BenefitMoney.setText(Utils.GetDefaultTool().DecimalNumberString(item.TargetMoney));
			
			return rowView;
		}		
	}	
	
	
	
	
	
}
