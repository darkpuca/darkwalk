package com.socialwalk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
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
	
	private String historyFileName;
	private WalkHistory history;
	private ProgressDialog progDlg;

	private static final int REQUEST_WALK_RESULT = 100;
	
	private Handler logUpdateHandler = null;
	
	private Runnable logUpdate = new Runnable()
	{
		@Override
		public void run()
		{
			File logDir = getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);
			File logFile = new File(logDir, historyFileName);

			while (!logFile.exists())
				SystemClock.sleep(500);
			
			history = Utils.GetDefaultTool().WalkHistoryFromFile(WalkResultActivity.this, historyFileName);
			if(null != history)
			{
				if (0 < history.RedHearts()) // 하트 적립이 0인 경우는 전송하지 않음. 
				{
					progDlg.show();

					reqType = REQUEST_WALK_RESULT;
					server.WalkResult(WalkResultActivity.this, WalkResultActivity.this, history);
				}
				
				updateDetails();
			}

		}
	};
	
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
		this.historyFileName = WalkHistoryManager.LastWalking().FileName;
		
		
		this.logUpdateHandler = new Handler();
		this.logUpdateHandler.post(this.logUpdate);
		
		
		Button btnDetail = (Button)findViewById(R.id.btnDetail);
		btnDetail.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if (null != history)
				{
					Intent i = new Intent(getBaseContext(), WalkDetailActivity.class);
					i.putExtra(Globals.EXTRA_KEY_FILENAME, history.FileName);
					startActivity(i);
				}
			}
		});
		

	}

	private void updateDetails()
	{
		TextView distance = (TextView)findViewById(R.id.distance);
		TextView walkTime = (TextView)findViewById(R.id.walkTime);
		TextView walkSpeed = (TextView)findViewById(R.id.walkSpeed);
		TextView walkHearts = (TextView)findViewById(R.id.walkHearts);
		TextView touchHearts = (TextView)findViewById(R.id.touchHearts);
		TextView calories = (TextView)findViewById(R.id.calories);
		TextView hearts = (TextView)findViewById(R.id.resultHeart);

		distance.setText(history.TotalDistanceString());
		walkTime.setText(history.TotalWalkingTimeString());
		walkSpeed.setText(history.AverageSpeed());
		walkHearts.setText(history.RedHeartStringByWalk());
		touchHearts.setText(history.RedHeartStringByTouch());
		calories.setText(history.TotalCalories() + " " + getResources().getString(R.string.CALORIES_UNIT));
		hearts.setText(history.RedHeartString());
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

		SaveUnregistedHistory();
		
		e.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (progDlg.isShowing())
			progDlg.dismiss();

		if (0 == response.length())
		{
			SaveUnregistedHistory();
			return;
		}
		
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result)
		{
			SaveUnregistedHistory();
			return;
		}
		
		if (REQUEST_WALK_RESULT == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				ServerRequestManager.LoginAccount.Hearts.addRedPointByWalk(this.history.RedHeartByWalk());
				ServerRequestManager.LoginAccount.Hearts.addRedPointByTouch(this.history.RedHeartByTouch());
				ServerRequestManager.LoginAccount.Hearts.addGreenPoint(this.history.GreedHeartByTouch());
			}
			else
			{
				SaveUnregistedHistory();
			}
		}
	}

	private void SaveUnregistedHistory()
	{
		this.history.IsUploaded = false;

		String strXml = this.history.GetXML();
		try
		{
			File logDir = this.getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);
			File logFile = new File(logDir, this.history.FileName);

			FileOutputStream fos = new FileOutputStream(logFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			osw.write(strXml);
			osw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}	
}
