package com.socialwalk;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity
{	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create utility class
        Utils.CreateDefaultTool(this);
        
        // autologin proc
//        ServerRequestManager.defaultManager.AutoLogin(this);
        
        // slide 서비스를 등록
        if (!LockService.IsRegisted)
        {
	        // start lock service
			startService(new Intent(this, LockService.class));
        }

		// show intro activity
		Intent i = new Intent(this, IntroActivity.class);
		startActivityForResult(i, Globals.INTENT_REQ_INTRO);
        
        // start button action
        Button btnStart = (Button)findViewById(R.id.btnWalkStart);
        btnStart.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
				if (!Utils.defaultTool.IsGpsAvailable())
				{
					Utils.defaultTool.showGpsSettingWithDialog();
		        }
		        else
		        {
		        	// start walking service
		        	Intent svcIntent = new Intent(getBaseContext(), WalkService.class);
		        	startService(svcIntent);		        	
		        	
		        	// load walking state activity
					Intent viewIntent = new Intent(getBaseContext(), WalkingActivity.class);
					startActivity(viewIntent);
		        }
			}
		});
        
        
        ImageButton btnHistory = (ImageButton)findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(new OnClickListener()
        {			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WalkHistoryActivity.class);
				startActivity(i);
			}
		});
        
        // test button
        Button btnTest = (Button)findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new OnClickListener()
        {	
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), GroupSelectionActivity.class);
				startActivityForResult(i, Globals.INTENT_REQ_GROUP_SELECT);
				
		        Intent lockIntent = new Intent(getBaseContext(), SlideActivity.class);
		        startActivity(lockIntent);
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    
	@Override
	public void onBackPressed()
	{
		AlertDialog.Builder exitDlg = new AlertDialog.Builder(this);
		exitDlg.setCancelable(true);
		exitDlg.setTitle(R.string.TITLE_INFORMATION);
		exitDlg.setMessage(R.string.MSG_EXIT_CONFIRM);
		exitDlg.setPositiveButton(R.string.EXIT, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				MainActivity.this.finish();
			}
		});
		exitDlg.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		exitDlg.show();
	}


	@Override
	protected void onDestroy()
	{
		ServerRequestManager.IsLogin = false;
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
		Utils.defaultTool.SetBaseActivity(this);
		
        if (WalkService.IsStarted)
        {
        	// 걷기 모드로 시작일 경우 걷기 상태 화면으로 바로 이동
        	Intent i = new Intent(this, WalkingActivity.class);
        	startActivity(i);
        }
		
		super.onResume();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Globals.INTENT_REQ_LOGIN == requestCode)
		{ 
			if (RESULT_OK == resultCode)
			{
				Toast.makeText(this, "login result ok", Toast.LENGTH_SHORT).show();
				
			}
			else
			{
				Utils.defaultTool.showFinishDialog(R.string.MSG_NO_LOGIN_EXIT);
			}
		}
		else if (Globals.INTENT_REQ_GROUP_SELECT == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				Toast.makeText(this, data.getStringExtra("sel_group"), Toast.LENGTH_SHORT).show();
			}
		}
		else if (Globals.INTENT_REQ_INTRO == requestCode)
		{
			StartupProc();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void StartupProc()
	{
        // check network connection
        if (!Utils.defaultTool.IsNetworkAvailable())
        {
        	Utils.defaultTool.showFinishDialog(R.string.MSG_NETWORK_NOT_CONNECTED);
			return;
        }
        
        // 위치기반 광고의 위치정보는 네트워크를 이용한 위치 정보를 활용한다.
        
        // login
        if (!ServerRequestManager.IsLogin)
        {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, Globals.INTENT_REQ_LOGIN);
        }

	}
	
    
}
