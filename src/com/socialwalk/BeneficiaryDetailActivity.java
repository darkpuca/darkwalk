package com.socialwalk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.Beneficiary;
import com.socialwalk.request.ImageCacheManager;
import com.socialwalk.request.RequestManager;
import com.socialwalk.request.ServerRequestManager;

public class BeneficiaryDetailActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener
{
	ServerRequestManager server;
	private String beneficiarySequence;
	private ProgressDialog progDlg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beneficiary_detail);
		
		this.server = new ServerRequestManager();
		
		this.beneficiarySequence = getIntent().getStringExtra(Globals.EXTRA_KEY_SEQUENCE);
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));

		if (0 < this.beneficiarySequence.length())
		{
			progDlg.show();
			this.server.BeneficiaryDetail(this, this, this.beneficiarySequence);
		}
	}
	
	
	
	@Override
	protected void onDestroy()
	{
		RequestManager.getRequestQueue().cancelAll(this);
		super.onDestroy();
	}



	private void updateDetails(Beneficiary detail)
	{
		NetworkImageView profileImage = (NetworkImageView)findViewById(R.id.profileImage);
//		NetworkImageView descImage = (NetworkImageView)findViewById(R.id.descriptionImage);
//		NetworkImageView reviewImage = (NetworkImageView)findViewById(R.id.reviewImage);
		TextView name = (TextView)findViewById(R.id.name);
		TextView age = (TextView)findViewById(R.id.age);
		TextView gender = (TextView)findViewById(R.id.gender);
		TextView targetMoney = (TextView)findViewById(R.id.targetMoney);
		TextView currentMoney = (TextView)findViewById(R.id.currentMoney);
		TextView participants = (TextView)findViewById(R.id.participants);
		TextView descriptions = (TextView)findViewById(R.id.description);
		
		ProgressBar moneyProgress = (ProgressBar)findViewById(R.id.moneyProgress);
		int progress = (int)(detail.CurrentMoney / detail.TargetMoney *100);
		moneyProgress.setProgress(progress);

		if (0 < detail.ProfileUrl.length())
		{
			profileImage.setImageUrl(null, null);
			profileImage.setImageUrl(detail.ProfileUrl, ImageCacheManager.getInstance().getImageLoader());
		}
		
		name.setText(detail.Name);
		age.setText(Integer.toString(detail.Age) + getResources().getString(R.string.AGE_UNIT));
		gender.setText(0 == detail.Gender ? R.string.MAN : R.string.WOMAN);
		targetMoney.setText(String.format("%s " + getResources().getString(R.string.MONEY_UNIT), Utils.GetDefaultTool().DecimalNumberString(detail.TargetMoney)));
		currentMoney.setText(String.format("%s " + getResources().getString(R.string.MONEY_UNIT), Utils.GetDefaultTool().DecimalNumberString(detail.CurrentMoney)));
		participants.setText(String.format("%s " + getResources().getString(R.string.MAN_UNIT), Utils.GetDefaultTool().DecimalNumberString(detail.Participants)));
		descriptions.setText(detail.Description);

//		if (0 < detail.DescriptionUrl.length())
//		{
//			descImage.setImageUrl(null, null);
//			descImage.setImageUrl(detail.DescriptionUrl, ImageCacheManager.getInstance().getImageLoader());
//		}
//		
//		if (0 < detail.ReviewsUrl.length())
//		{
//			reviewImage.setImageUrl(null, null);
//			reviewImage.setImageUrl(detail.ReviewsUrl, ImageCacheManager.getInstance().getImageLoader());
//		}
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
		
		if (Globals.ERROR_NONE == result.Code)
		{
			Beneficiary detail = parser.GetBeneficiary();
			if (null == detail) return;
			
			updateDetails(detail);
		}
	}

	
}
