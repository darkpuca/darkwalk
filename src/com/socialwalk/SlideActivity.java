package com.socialwalk;


import java.util.Calendar;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.socialwalk.MyXmlParser.SlideAdData;
import com.socialwalk.request.ImageCacheManager;
import com.socialwalk.request.ServerRequestManager;

public class SlideActivity extends Activity implements Response.Listener<String>, Response.ErrorListener
{
	private static final String TAG = "SW-SLIDE";
	private static final int SLIDER_OUTOFBOUNDS = 0;
	private static final int SLIDER_IN_AD = 1;
	private static final int SLIDER_IN_HOME = 2;
	private static final int SLIDER_IN_START = 3;
	private static final int SLIDER_IN_STOP = 4;
	private static final int SLIDER_AREA_OFFSET = 32;
	
	private static SlideAdData CurrentAdData = null;
	private static final int AD_LIMIT = 3;
	private static int CURRENT_AD_INDEX = 0;
	private static final long AD_POINT_INTERVAL = 60 * 60 * 1000;
	private static boolean IsPhoneCalling = false;

	KeyguardManager.KeyguardLock keyLock;
	boolean isDragmode;
	
	LayoutParams layoutParams;
	int m_windowWidth, m_windowHeight;
	int m_sliderWidth, m_sliderHeight;
	ImageView imgCenter, imgAd, imgHome, imgStart, imgStop;
	RelativeLayout sliderLayout;
	
	private ServerRequestManager m_server = null;
	private NetworkImageView m_adImage;

	@Override
	public void onAttachedToWindow()
	{
//		getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.TYPE_KEYGUARD);
		getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG | WindowManager.LayoutParams.TYPE_KEYGUARD);

		super.onAttachedToWindow();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
//		int flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_FULLSCREEN;
		int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		getWindow().addFlags(flags);
		
		setContentView(R.layout.activity_slide);
		
		m_server = new ServerRequestManager();
		
		m_adImage = (NetworkImageView)findViewById(R.id.imgAd);
		
		sliderLayout = (RelativeLayout)findViewById(R.id.sliderLayout);
		imgCenter = (ImageView)findViewById(R.id.imgSlideCenter);
		imgHome = (ImageView)findViewById(R.id.imgSlideHome);
		imgAd = (ImageView)findViewById(R.id.imgSlideAd);
		imgStart = (ImageView)findViewById(R.id.imgSlideStart);
		imgStop = (ImageView)findViewById(R.id.imgSlideStop);
		
		BitmapDrawable bd=(BitmapDrawable) this.getResources().getDrawable(R.drawable.ic_launcher);
		m_sliderWidth = bd.getBitmap().getHeight();
		m_sliderHeight = bd.getBitmap().getWidth();
		
		// slider 레이아웃 위치조정
		m_windowWidth = getWindowManager().getDefaultDisplay().getWidth();
		m_windowHeight = getWindowManager().getDefaultDisplay().getHeight();
		Log.d(TAG, "window w:" + m_windowWidth +", h:" + m_windowHeight);

		MoveSliderToStartPosition();
		
		DisableKeyguard();
		
		if (getIntent() != null && getIntent().hasExtra("kill") && getIntent().getExtras().getInt("kill") == 1)
			finish();

		try
		{
	        // check and regist slider
			if (!LockService.IsRegisted)
	        {
		        // start lock service
				startService(new Intent(this, LockService.class));
				
	        }

			StateListener phoneListener = new StateListener();
			TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
			tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
			
			imgCenter.setOnTouchListener(new OnTouchListener()
			{	
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					layoutParams = (LayoutParams)v.getLayoutParams();
					
					switch (event.getAction())
					{
					case MotionEvent.ACTION_DOWN:
					{
						break;
					}						
					case MotionEvent.ACTION_MOVE:
					{
						int posX = (int)event.getRawX();
						int posY = (int)event.getRawY();
						
						layoutParams.leftMargin = (posX - m_sliderWidth/2);
						layoutParams.topMargin = (posY - m_sliderHeight);
						v.setLayoutParams(layoutParams);

						break;
					}						
					case MotionEvent.ACTION_UP:
					{
						int posX = (int)event.getRawX();
						int posY = (int)event.getRawY();
						
						int sliderPos = MesureSliderPosition(posX, posY - m_sliderHeight/2);
						switch (sliderPos)
						{
						case SLIDER_IN_AD:
						{
							Log.d(TAG, "slider in AD area.");
							ShowAdPage();
							break;
						}
						case SLIDER_IN_HOME:
						{
							Log.d(TAG, "slider in HOME area.");
							Intent i = new Intent(getBaseContext(), MainActivity.class);
							startActivity(i);
							finish();
							break;
						}
						case SLIDER_IN_START:
						{
							Log.d(TAG, "slider in START area.");
							StartWalking();
							break;
						}
						case SLIDER_IN_STOP:
						{
							Log.d(TAG, "slider in STOP area.");
							StopWalking();
							break;
						}
						default:
							Log.d(TAG, "slider is out-of-bounds");
							
							MoveSliderToStartPosition();
							break;
						}
						break;
					}	
					default:
						break;
					}
					
					return true;
				}
			});
		}
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
		}
		
		Utils.CreateDefaultTool(this);
		
		// auto-login
		m_server.AutoLogin(this);

	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	private void MoveSliderToStartPosition()
	{
		int layoutH = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, this.getResources().getDisplayMetrics());
		int centerX = (m_windowWidth - m_sliderWidth) / 2;
		int centerY = (m_windowHeight - layoutH) + (layoutH - m_sliderHeight) / 2;
		
		MarginLayoutParams centerMarginParams = new MarginLayoutParams(imgCenter.getLayoutParams());
		centerMarginParams.setMargins(centerX, centerY, 0, 0);
		RelativeLayout.LayoutParams centerLayout = new RelativeLayout.LayoutParams(centerMarginParams);
		imgCenter.setLayoutParams(centerLayout);
		Log.d(TAG, "centerX:" + centerX +", centerY:" + centerY);
	}
	
	private int MesureSliderPosition(int posX, int posY)
	{
		int[] posAd = new int[2];
		imgAd.getLocationOnScreen(posAd);
		int adW = imgAd.getWidth();
		int adH = imgAd.getHeight();
		
		int[] posHome = new int[2];
		imgHome.getLocationOnScreen(posHome);
		int homeW = imgHome.getWidth();
		int homeH = imgHome.getHeight();
		
		int[] posStart = new int[2];
		imgStart.getLocationOnScreen(posStart);
		int startW = imgStart.getWidth();
		int startH = imgStart.getHeight();
		
		int[] posStop = new int[2];
		imgStop.getLocationOnScreen(posStop);
		int stopW = imgStop.getWidth();
		int stopH = imgStop.getHeight();
		
		if (posX >= (posAd[0] - SLIDER_AREA_OFFSET) && 
				posX <= (posAd[0] + adW + SLIDER_AREA_OFFSET) && 
				posY >= (posAd[1] - SLIDER_AREA_OFFSET) &&
				posY <= (posAd[1] + adH + SLIDER_AREA_OFFSET))
			return SLIDER_IN_AD;
		else if (posX >= (posHome[0] - SLIDER_AREA_OFFSET) && 
				posX <= (posHome[0] + homeW + SLIDER_AREA_OFFSET) && 
				posY >= (posHome[1] - SLIDER_AREA_OFFSET) &&
				posY <= (posHome[1] + homeH + SLIDER_AREA_OFFSET))
			return SLIDER_IN_HOME;
		else if (posX >= (posStart[0] - SLIDER_AREA_OFFSET) && 
				posX <= (posStart[0] + startW + SLIDER_AREA_OFFSET) && 
				posY >= (posStart[1] - SLIDER_AREA_OFFSET) &&
				posY <= (posStart[1] + startH + SLIDER_AREA_OFFSET))
			return SLIDER_IN_START;
		else if (posX >= (posStop[0] - SLIDER_AREA_OFFSET) && 
				posX <= (posStop[0] + stopW + SLIDER_AREA_OFFSET) && 
				posY >= (posStop[1] - SLIDER_AREA_OFFSET) &&
				posY <= (posStop[1] + stopH + SLIDER_AREA_OFFSET))
			return SLIDER_IN_STOP;
		
		return SLIDER_OUTOFBOUNDS;
	}
	
	
	private void UpdateWalkingState()
	{
		TextView walkingState = (TextView)findViewById(R.id.tvWalkingState);

		if (WalkService.IsStarted)
			walkingState.setText(R.string.WALKING_PROGRESS);
		else
			walkingState.setText(R.string.WALKING_IDLE);
	}
	

	@Override
	public void onBackPressed()
	{
		return;
		//super.onBackPressed();
	}

	

	@Override
	protected void onStop()
	{
		super.onStop();
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_POWER ||
				keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
				keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
				keyCode == KeyEvent.KEYCODE_CAMERA)
		{
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_HOME)
			return false;
		
		return false;
		//return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		Log.d("SW-SLIDE", "KeyEvent code:" + event.getKeyCode());
		
		if (event.getKeyCode() == KeyEvent.KEYCODE_POWER ||
				event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN ||
				event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)
		{
			return false;
		}

		if (event.getKeyCode() == KeyEvent.KEYCODE_HOME)
		{
			Log.d(TAG, "HOMEKEY pressed.");
			return true;
		}
			
		return super.dispatchKeyEvent(event);
	}
	
	
	@Override
	protected void onResume()
	{
		MoveSliderToStartPosition();
		DisableKeyguard();
		
		UpdateWalkingState();
		
		Utils.defaultTool.SetBaseActivity(this);
		
		if (Utils.defaultTool.IsNetworkAvailable())
		{
			UpdateSlideAd();
		}
		
		super.onResume();
	}
	

	private void ShowAdPage()
	{
		if (null == CurrentAdData)
		{
			finish();
		}
		else
		{
			Intent i = new Intent(this, WebPageActivity.class);
			i.putExtra(Globals.EXTRA_KEY_URL, CurrentAdData.TargetUrl);
			startActivity(i);
			finish();

			Calendar c = Calendar.getInstance();
			long now = c.getTimeInMillis();
			long lastAccess = CurrentAdData.LastAccess.getTime();
			long gap = now - lastAccess;
			
			if (AD_POINT_INTERVAL < gap)
			{
				// TODO: 적립 코드 추가 필요
				CurrentAdData.LastAccess.setTime(c.getTimeInMillis());
			}
			
		}		
	}
	
	private void StartWalking()
	{
		if (!ServerRequestManager.IsLogin)
		{
			Intent i = new Intent(this, LoginActivity.class);
			startActivity(i);
			return;
		}
		
		if (!Utils.defaultTool.IsGpsAvailable())
		{
			MoveSliderToStartPosition();
			Utils.defaultTool.showGpsSettingWithDialog();
			return;
		}
		
		if (!WalkService.IsStarted)
		{
			Intent i = new Intent(getApplicationContext(), WalkService.class);
			startService(i);
		}
		finish();

	}
	
	private void StopWalking()
	{
		if (WalkService.IsStarted)
		{	
			Intent i = new Intent(getApplicationContext(), WalkService.class);
			stopService(i);
		}
		finish();
	}
	
	private void DisableKeyguard()
	{
		KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock kl = km.newKeyguardLock("IN");
		kl.disableKeyguard();
	}

	
	public void UpdateSlideAd()
	{
		if (CURRENT_AD_INDEX >= AD_LIMIT)
			CURRENT_AD_INDEX = 0;
		
		if ((CURRENT_AD_INDEX+1) > LockService.SlideAdList.size())
		{
			m_server.NeoClickItem(this, this);
		}
		else
		{
			SlideAdData adData = LockService.SlideAdList.get(CURRENT_AD_INDEX);
			UpdateControls(adData);
		}
		
		CURRENT_AD_INDEX++;
	}
	
	public void UpdateControls(SlideAdData adData)
	{
		if (null == adData) return;
		
		CurrentAdData = adData;
		
		// clear
		m_adImage.setImageUrl(null, null);
		m_adImage.setImageUrl(adData.ThumbnailUrl, ImageCacheManager.getInstance().getImageLoader());
	}
	
	
	private class StateListener extends PhoneStateListener
	{
		@Override
		public void onCallStateChanged(int state, String incomingNumber) 
		{
			super.onCallStateChanged(state, incomingNumber);
			
            switch(state){
                case TelephonyManager.CALL_STATE_RINGING:
                	Log.d(TAG, "phone called: " + incomingNumber);
                	IsPhoneCalling = true;
                	moveTaskToBack(true);
                	break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    System.out.println("call Activity off hook");
                	finish();
                	IsPhoneCalling = false;
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                	if (IsPhoneCalling)
                	{
                    	Log.d(TAG, "call state idle.");
                    	Intent i = new Intent(getApplicationContext(), SlideActivity.class);
            			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    	getApplicationContext().startActivity(i);
                	}
                    break;
            }
		}
	}

	@Override
	public void onErrorResponse(VolleyError error)
	{
	}

	@Override
	public void onResponse(String response)
	{
		MyXmlParser parser = new MyXmlParser(response);
		SlideAdData adData = parser.GetAdData();
		
		if (null != adData)
		{
			LockService.SlideAdList.add(adData);
			UpdateControls(adData);
		}
	}
}




