package com.socialwalk;


import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.NeoClickItems;
import com.socialwalk.dataclass.NeoClickItems.NeoClickItem;
import com.socialwalk.request.ImageCacheManager;
import com.socialwalk.request.ServerRequestManager;
import com.socialwalk.request.ServerRequestManager.ServerRequestListener;

public class SlideActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener, ServerRequestListener
{
	private static final String TAG = "SW-SLIDE";
	private static final int SLIDER_OUTOFBOUNDS = 0;
	private static final int SLIDER_IN_AD = 1;
//	private static final int SLIDER_IN_HOME = 2;
	private static final int SLIDER_IN_START = 3;
	private static final int SLIDER_IN_STOP = 4;
	private static final int SLIDER_AREA_OFFSET = 40;
	
	private static final int REQUEST_NEOCLICK = 100;
	private static final int REQUEST_ACCUMULATE_VISIT = 101;
	private static final int REQUEST_ACCUMULATE_START = 102;

	private NeoClickItem currentNeoClick = null;
	
//	private KeyguardManager.KeyguardLock keyLock;
	private int reqType = 0;
	
	LayoutParams layoutParams;
	private int m_windowWidth, m_windowHeight;
	private int m_sliderWidth, m_sliderHeight, m_marginBottom, m_startHeight;
	ImageView imgCenter, imgAd, imgStart, imgStop;  // imgHome
	RelativeLayout sliderLayout;
	
	private ServerRequestManager m_server = null;
	private NetworkImageView m_adImage;
	private TextView heartPoint;
	
	private int loginReqType;
	private static final int LOGIN_REQ_START = 300;
	private static final int LOGIN_REQ_AD = 301;

	
	@Override
	public void onAttachedToWindow()
	{
		getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG | WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		getWindow().addFlags(flags);
		
		setContentView(R.layout.activity_slide);
		
		m_server = new ServerRequestManager();
		
		m_adImage = (NetworkImageView)findViewById(R.id.imgAd);
		
		sliderLayout = (RelativeLayout)findViewById(R.id.sliderLayout);
		imgCenter = (ImageView)findViewById(R.id.imgSlideCenter);
		imgAd = (ImageView)findViewById(R.id.imgSlideAd);
//		imgHome = (ImageView)findViewById(R.id.imgSlideHome);
		imgStart = (ImageView)findViewById(R.id.imgSlideStart);
		imgStop = (ImageView)findViewById(R.id.imgSlideStop);
		
		this.heartPoint = (TextView)findViewById(R.id.heartPoint);
		this.heartPoint.setText("+" + Integer.toString(Globals.AD_POINT_SLIDE_VISIT));
		this.heartPoint.setVisibility(View.INVISIBLE);
		
		this.m_marginBottom = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, this.getResources().getDisplayMetrics());
		
		int centerSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, this.getResources().getDisplayMetrics());
		this.m_sliderWidth = m_sliderHeight = centerSize;
		this.m_startHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, this.getResources().getDisplayMetrics());
		
		// slider 레이아웃 위치조정
		m_windowWidth = getWindowManager().getDefaultDisplay().getWidth();
		m_windowHeight = getWindowManager().getDefaultDisplay().getHeight();
		
		MoveSliderToStartPosition();
		
//		EnableKeyGuard(false);
		
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
//						case SLIDER_IN_HOME:
//						{
//							Log.d(TAG, "slider in HOME area.");
//							Intent i = new Intent(getBaseContext(), MainActivity.class);
//							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//							startActivity(i);
//							finish();
//							break;
//						}
						case SLIDER_IN_START:
						{
							Log.d(TAG, "slider in START area.");
							StartWalking();
							break;
						}
						case SLIDER_IN_STOP:
						{
							Log.d(TAG, "slider in STOP area.");
//							StopWalking();
//							EnableKeyGuard(true);
							SlideActivity.this.moveTaskToBack(true);
							finish();
							LockReceiver.UpdateAccessTime();
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
		
		// auto-login
		m_server.AutoLogin(this, this);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	private void MoveSliderToStartPosition()
	{
		Rect rectgle= new Rect();
		Window window= getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
		int statusBarHeight = rectgle.top;

		int centerX = (m_windowWidth - m_sliderWidth) / 2;
		int centerY = m_windowHeight - statusBarHeight - m_marginBottom - m_startHeight + (m_startHeight - m_sliderHeight)/2;
		
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
				posY <= (posAd[1] + adH))
			return SLIDER_IN_AD;
//		else if (posX >= (posHome[0] - SLIDER_AREA_OFFSET) && 
//				posX <= (posHome[0] + homeW + SLIDER_AREA_OFFSET) && 
//				posY >= (posHome[1] - SLIDER_AREA_OFFSET) &&
//				posY <= (posHome[1] + homeH + SLIDER_AREA_OFFSET))
//			return SLIDER_IN_HOME;
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
		if (!MainApplication.IsSlideActive) finish();
		
		int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		
		getWindow().addFlags(flags);

//		EnableKeyGuard(false);
		
		UpdateWalkingState();
		
		if (true == LockReceiver.IsPhoneCalling)
		{
			finish();
			return;
		}
		
		MainApplication application = (MainApplication)getApplication();
		application.IsNetworkAvailable();
		if (application.IsNetworkAvailable())
			UpdateSlideAd();
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{			
			@Override
			public void run()
			{
				MoveSliderToStartPosition();
			}
		}, 100);

		super.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Globals.INTENT_REQ_LOGIN == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				if (LOGIN_REQ_AD == this.loginReqType)
					ShowAdPage();
				else if (LOGIN_REQ_START == this.loginReqType)
					StartWalking();
				
				this.loginReqType = 0;
			}
		}
		else if (Globals.INTENT_REQ_WEBVIEW == requestCode)
		{
//			moveTaskToBack(true);
//			finish();
//			return;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void ShowAdPage()
	{
		if (null == this.currentNeoClick)
		{
			moveTaskToBack(true);
			finish();
		}
		else
		{
			if (!ServerRequestManager.IsLogin)
			{
				Intent i = new Intent(getBaseContext(), LoginActivity.class);
				this.loginReqType = LOGIN_REQ_AD;
				startActivityForResult(i, Globals.INTENT_REQ_LOGIN);
				return;
			}
			
			Intent i = new Intent(getApplicationContext(), WebPageActivity.class);
			i.putExtra(Globals.EXTRA_KEY_URL, currentNeoClick.TargetUrl);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			
			finish();
			
			if (currentNeoClick.IsBillingAvailable())
			{
				reqType = REQUEST_ACCUMULATE_VISIT;
				m_server.AccumulateHeart(this, this, Globals.AD_TYPE_SLIDE, currentNeoClick.Sequence, Globals.AD_POINT_SLIDE_VISIT);
			}
			else
			{
				Toast.makeText(this, "over clicked", Toast.LENGTH_SHORT).show();
			}
				
			currentNeoClick.SetAccessStamp();
			LockReceiver.UpdateAccessTime();
//			EnableKeyGuard(true);
		}		
	}
	
	private void StartWalking()
	{
		// 걷기 상태이면 그냥 슬라이드 화면 종료
		if (WalkService.IsStarted)
		{
			finish();
			moveTaskToBack(true);
			return;
		}
		
		if (!ServerRequestManager.IsLogin)
		{
			Intent i = new Intent(getBaseContext(), LoginActivity.class);
			this.loginReqType = LOGIN_REQ_START;
			startActivityForResult(i, Globals.INTENT_REQ_LOGIN);
			return;
		}
		
		MainApplication app = (MainApplication)getApplication();
		if (!app.IsGpsAvailable())
		{
			MoveSliderToStartPosition();
			Utils.GetDefaultTool().ShowGpsSettingWithDialog(this);
			return;
		}
		
		if (!WalkService.IsStarted)
		{
			reqType = REQUEST_ACCUMULATE_START;
			m_server.AccumulateHeart(this, this, Globals.AD_TYPE_SLIDE, "", Globals.AD_POINT_SLIDE_START);
			
			Intent i = new Intent(getApplicationContext(), WalkService.class);
			startService(i);
			finish();
		}

		LockReceiver.UpdateAccessTime();
	}
	
//	private void StopWalking()
//	{
//		if (WalkService.IsStarted)
//		{	
//			Intent i = new Intent(getApplicationContext(), WalkService.class);
//			stopService(i);
//		}
//		finish();
//	}
//	
//	private void EnableKeyGuard(boolean enable)
//	{
//		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
//		KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
//		
//		if (true == enable)
//			lock.reenableKeyguard();
//		else
//			lock.disableKeyguard();
//	}

	
	public void UpdateSlideAd()
	{
		// 광고 업데이트 5초 이하면 변경 안함
		if (null != MainApplication.NeoClickUpdateTime)
		{
			Date now = new Date();
			long diffInMs = now.getTime() - MainApplication.NeoClickUpdateTime.getTime();
			long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
			if (5 > diffInSeconds) return;
		}
		
		if (0 == MainApplication.NeoClickAds.Items.size())
		{
			reqType = REQUEST_NEOCLICK;
			m_server.UpdateNeoClickItems(this, this);
		}
		else
		{
			currentNeoClick = MainApplication.NeoClickAds.GetNextItem();
			UpdateControls();
		}
		
		MainApplication.NeoClickUpdateTime = new Date();
	}
	
	public void UpdateControls()
	{
		if (null == currentNeoClick) return;
		
		
		if (currentNeoClick.IsBillingAvailable())
			this.heartPoint.setVisibility(View.VISIBLE);
		else
			this.heartPoint.setVisibility(View.INVISIBLE);

		m_adImage.setImageUrl(null, null);
		m_adImage.setImageUrl(currentNeoClick.ThumbnailUrl, ImageCacheManager.getInstance().getImageLoader());
	}
	
	
	private class StateListener extends PhoneStateListener
	{
		@Override
		public void onCallStateChanged(int state, String incomingNumber) 
		{
			super.onCallStateChanged(state, incomingNumber);
			
            switch(state){
                case TelephonyManager.CALL_STATE_RINGING:
                	LockReceiver.IsPhoneCalling = true;
                	Log.d(TAG, "phone called: " + incomingNumber);
                	moveTaskToBack(true);
                	break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    System.out.println("call Activity off hook");
                    moveTaskToBack(true);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                	if (LockReceiver.IsPhoneCalling)
                	{
                    	LockReceiver.IsPhoneCalling = false;

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

		if (REQUEST_NEOCLICK == reqType)
		{
			System.out.println(response);
			
//			NeoClickItems items = parser.GetNeoClickItems();
			NeoClickItems items = parser.GetRobinhootItems();
			
			if (null == items) return;
			MainApplication.NeoClickAds.SetItems(items.Items);
			
			this.currentNeoClick = MainApplication.NeoClickAds.GetNextItem();
			UpdateControls();
		}
		else if (REQUEST_ACCUMULATE_VISIT == reqType ||
				REQUEST_ACCUMULATE_START == reqType)
		{
			SWResponse result = parser.GetResponse();
			if (null == result) return;
			
			if (Globals.ERROR_NONE == result.Code)
			{
				if (REQUEST_ACCUMULATE_VISIT == reqType)
					ServerRequestManager.LoginAccount.Hearts.addGreenPoint(Globals.AD_POINT_SLIDE_VISIT);
				else if (REQUEST_ACCUMULATE_START == reqType)
					ServerRequestManager.LoginAccount.Hearts.addGreenPoint(Globals.AD_POINT_SLIDE_START);
			}
			else
			{
				// TODO: 적립 실패할 경우 어떻게 처리하지?
				System.out.println("하트 적립 실패");
			}
		}
	}

	@Override
	public void onFinishAutoLogin(boolean isLogin)
	{
		
	}
}




