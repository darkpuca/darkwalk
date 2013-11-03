package com.socialwalk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.WalkHistory;
import com.socialwalk.dataclass.WalkHistoryManager;
import com.socialwalk.request.ServerRequestManager;

public class WalkResultActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener
{
	private ServerRequestManager server;
	private int reqType;
	private ProgressDialog progDlg;

	private static final int REQUEST_WALK_RESULT = 100;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk_result);
		
		this.server = new ServerRequestManager();
		
		Button btnClose = (Button)findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_SEND_WALK_DATA));
		
		// 걷기 상세 정보 업데이트
		final WalkHistory lastWalk = WalkHistoryManager.LastWalking();
		if(null != lastWalk)
		{
			if (0 < lastWalk.RedHearts()) // 하트 적립이 0인 경우는 전송하지 않음. 
			{
				progDlg.show();

				this.reqType = REQUEST_WALK_RESULT;
				this.server.WalkResult(this, this, lastWalk);
			}
			
			TextView distance = (TextView)findViewById(R.id.distance);
			TextView walkTime = (TextView)findViewById(R.id.walkTime);
			TextView walkSpeed = (TextView)findViewById(R.id.walkSpeed);
			TextView walkHearts = (TextView)findViewById(R.id.walkHearts);
			TextView touchHearts = (TextView)findViewById(R.id.touchHearts);
			TextView calories = (TextView)findViewById(R.id.calories);
			TextView hearts = (TextView)findViewById(R.id.resultHeart);

			distance.setText(lastWalk.TotalDistanceString());
			walkTime.setText(lastWalk.TotalWalkingTimeString());
			walkSpeed.setText(lastWalk.AverageSpeed());
			walkHearts.setText(lastWalk.RedHeartStringByWalk());
			touchHearts.setText(lastWalk.RedHeartStringByTouch());
			calories.setText(lastWalk.TotalCalories() + " " + getResources().getString(R.string.CALORIES_UNIT));
			hearts.setText(lastWalk.RedHeartString());
		}
		
		Button btnDetail = (Button)findViewById(R.id.btnDetail);
		btnDetail.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if (null != lastWalk)
				{
					Intent i = new Intent(getBaseContext(), WalkDetailActivity.class);
					i.putExtra(Globals.EXTRA_KEY_FILENAME, lastWalk.FileName);
					startActivity(i);
				}
			}
		});
		

	}

	@Override
	public void onBackPressed() 
	{
		return;
	}



	@Override
	public void onErrorResponse(VolleyError e)
	{
		if (progDlg.isShowing())
			progDlg.dismiss();

		e.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (progDlg.isShowing())
			progDlg.dismiss();

		if (0 == response.length()) return;
		
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (REQUEST_WALK_RESULT == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				WalkHistory lastWalk = WalkHistoryManager.LastWalking();
				if(null != lastWalk)
				{
					
				}
			}
		}
	}	
}
