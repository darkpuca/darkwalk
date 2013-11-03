package com.socialwalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebPageActivity extends Activity
{
	private ProgressDialog progDlg;
	
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
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		
		webView.setWebChromeClient(new WebChromeClient()
		{
		    @Override
		    public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)
		    {
		        new AlertDialog.Builder(WebPageActivity.this)
		            .setTitle(R.string.TITLE_INFORMATION)
		            .setMessage(message)
		            .setPositiveButton(android.R.string.ok,
		                    new AlertDialog.OnClickListener()
		                    {
		                        public void onClick(DialogInterface dialog, int which)
		                        {
		                            result.confirm();
		                        }
		                    })
		            .setCancelable(false)
		            .create()
		            .show();
		 
		        return true;
		    };
		});
		
		// get target url
		String urlString = getIntent().getStringExtra(Globals.EXTRA_KEY_URL);

		// prepare progress dialog
		progDlg = new ProgressDialog(WebPageActivity.this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(true);

		webView.loadUrl(urlString);

	}
	
	private class WebCallBack extends WebViewClient
	{

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
//			Toast.makeText(getBaseContext(), url, Toast.LENGTH_SHORT).show();
			String[] names = url.split("/");
			String targetName = names[names.length-1];
			System.out.println(targetName);
			
			if (targetName.equalsIgnoreCase("join_success.html"))
			{
				setResult(RESULT_OK);
				finish();
				return false;
			}
			
			//return false;
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			if (!progDlg.isShowing())
			{
				progDlg.setMessage(getResources().getString(R.string.MSG_PAGE_LOADING));
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
