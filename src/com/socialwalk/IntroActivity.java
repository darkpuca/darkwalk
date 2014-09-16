package com.socialwalk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.request.ServerRequestManager;

public class IntroActivity extends Activity
{
	private Handler autoCloseHandler = null;
	private Runnable autoCloseRunnable = new Runnable()
	{	
		@Override
		public void run()
		{
			setResult(RESULT_OK);
			finish();
		}
	};
	
	private ImageView imgAd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		imgAd = (ImageView)findViewById(R.id.imgAd);
//		Bitmap newBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.intro_bg);
		Bitmap newBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.intro_silicon_power);
		imgAd.setImageBitmap(newBitmap );
		
		autoCloseHandler = new Handler();
		autoCloseHandler.postDelayed(autoCloseRunnable, Globals.INTRO_WAITING);

//		RelativeLayout buttonLayout = (RelativeLayout)findViewById(R.id.layoutHeartButton);
//		buttonLayout.setOnClickListener(new OnClickListener()
//		{			
//			@Override
//			public void onClick(View v)
//			{
//				// �ڵ� ���� ��� ����
//				autoCloseHandler.removeCallbacks(autoCloseRunnable);
//				
//				Intent i = new Intent(getBaseContext(), WebPageActivity.class);
//				i.putExtra(Globals.EXTRA_KEY_URL, Globals.URL_SERVER_DOMAIN);
//				startActivity(i);
//				
//				Intent retData = new Intent();
//				retData.putExtra(Globals.EXTRA_KEY_INTRO_AD_VISIT, true);
//				setResult(RESULT_OK, retData);
//				finish();
//			}
//		});
	}

	
	@Override
	public void onBackPressed()
	{
		return;
	}


	@Override
	protected void onDestroy()
	{
		((BitmapDrawable)imgAd.getDrawable()).getBitmap().recycle();
		super.onDestroy();
	}
}
