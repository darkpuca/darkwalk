package com.socialwalk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebPageActivity extends Activity
{
	ProgressDialog progDlg;
	
	private static final String TAG = "SW-WEBLINK";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_page);
		
		// set default webpage setting
		WebView webView = (WebView)findViewById(R.id.webView);
		webView.setWebViewClient(new WebCallBack());
		WebSettings webSettings = webView.getSettings();
		webSettings.setBuiltInZoomControls(true);
		
		// get target url
		String urlString = getIntent().getStringExtra(Globals.EXTRA_KEY_URL);
		Toast.makeText(this, "received url:" + urlString, Toast.LENGTH_LONG).show();

		// prepare progress dialog
		progDlg = new ProgressDialog(WebPageActivity.this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);

		webView.loadUrl(urlString);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_page, menu);
		return true;
	}
	
	private class WebCallBack extends WebViewClient
	{

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			//return false;
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			Log.d(TAG, "started url: " + url);
			
			if (!progDlg.isShowing())
			{
				progDlg.setTitle("page load");
				progDlg.setMessage("please wait...");
				progDlg.setButton("Cancel", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
					
				progDlg.show();
			}

			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url)
		{
			Log.d(TAG, "finishied url: " + url);
			
			if (progDlg.isShowing())
				progDlg.dismiss();
			
			super.onPageFinished(view, url);
		}


	}

}
