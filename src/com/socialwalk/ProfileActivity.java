package com.socialwalk;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.TextInputDialog.TextInputDialogListener;
import com.socialwalk.dataclass.AccountData;
import com.socialwalk.dataclass.AreaItem;
import com.socialwalk.request.ServerRequestManager;
import com.socialwalk.request.SocialWalkRequest;

public class ProfileActivity extends FragmentActivity
implements TextInputDialogListener, OnClickListener, Response.Listener<String>, Response.ErrorListener, OnDateSetListener
{
	private ServerRequestManager server;
	private int reqType, inputType;
	
	private static final int REQUEST_USER_PROFILE = 100;
	private static final int REQUEST_UPDATE_PROFILE = 101;
	private static final int REQUEST_AREAITEM = 102;
	private static final int REQUEST_SUBAREAITEM = 103;
	private static final int REQUEST_SECESSION_PASSWORD = 104;
	private static final int REQUEST_SECESSION = 105;
	
	private static final int INPUT_NAME = 300;
	private static final int INPUT_PASSWORD = 301;
	
	private AccountData userProfile;
	private boolean isModified;
	private Vector<AreaItem> areaItems, subAreaItems;
	private RelativeLayout nameLayout, genderLayout, weightLayout, birthdayLayout, passwordLayout, groupLayout;
	private RelativeLayout areaLayout, subAreaLayout, secessionLayout;
	private Button applyButton;
	
	private ProgressDialog progDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		this.server = new ServerRequestManager();
		this.isModified = false;
		
		this.areaItems = new Vector<AreaItem>();
		this.subAreaItems = new Vector<AreaItem>();
		
		this.nameLayout = (RelativeLayout)findViewById(R.id.nameLayout);
		this.genderLayout = (RelativeLayout)findViewById(R.id.sexLayout);
		this.birthdayLayout = (RelativeLayout)findViewById(R.id.birthdayLayout);
		this.weightLayout = (RelativeLayout)findViewById(R.id.weightLayout);
		this.passwordLayout = (RelativeLayout)findViewById(R.id.passwordButtonLayout);
		this.groupLayout = (RelativeLayout)findViewById(R.id.groupLayout);
		this.areaLayout = (RelativeLayout)findViewById(R.id.areaLayout);
		this.subAreaLayout = (RelativeLayout)findViewById(R.id.subAreaLayout);
		this.secessionLayout = (RelativeLayout)findViewById(R.id.secessionButtonLayout);
		
		this.nameLayout.setOnClickListener(this);
		this.genderLayout.setOnClickListener(this);
		this.birthdayLayout.setOnClickListener(this);
		this.weightLayout.setOnClickListener(this);
		this.passwordLayout.setOnClickListener(this);
		this.groupLayout.setOnClickListener(this);
		this.areaLayout.setOnClickListener(this);
		this.subAreaLayout.setOnClickListener(this);
		this.secessionLayout.setOnClickListener(this);
		
		this.applyButton = (Button)findViewById(R.id.applyButton);
		this.applyButton.setVisibility(View.INVISIBLE);
		this.applyButton.setOnClickListener(this);
		
		TextView tvAreaLabel = (TextView)findViewById(R.id.areaLabel);
		TextView tvSubAreaLabel = (TextView)findViewById(R.id.subAreaLabel);
		
		ImageView areaLockedIcon = (ImageView)findViewById(R.id.areaLockedIcon);
		if (ServerRequestManager.LoginAccount.IsGroupUser)
		{
			tvAreaLabel.setText(R.string.ORGANIZAION);
			tvSubAreaLabel.setText(R.string.MEMBER);
			this.areaLayout.setEnabled(false);
			areaLockedIcon.setVisibility(View.VISIBLE);
		}
		else
		{
			tvAreaLabel.setText(R.string.LOCAL1);
			tvSubAreaLabel.setText(R.string.LOCAL2);
			this.areaLayout.setEnabled(true);
			areaLockedIcon.setVisibility(View.INVISIBLE);
		}
		
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));

		requestUserProfile();
	}

	private void requestUserProfile()
	{
		if (!progDlg.isShowing()) progDlg.show();
		this.reqType = REQUEST_USER_PROFILE;
		this.server.GetUserProfile(this, this);
	}

	@Override
	public void onClick(View v)
	{
		if (nameLayout.equals(v))
			showNameDialog();
		else if (genderLayout.equals(v))
			showGenderDialog();
		else if (weightLayout.equals(v))
			showWeightDialog();
		else if (passwordLayout.equals(v))
			showPasswordDialog();
		else if (birthdayLayout.equals(v))
			showDatePickerDialog();
		else if (groupLayout.equals(v))
			showGroupConfig();
		else if (areaLayout.equals(v))
			requestAreaItems();
		else if (subAreaLayout.equals(v))
			requestSubAreaItems();
		else if (applyButton.equals(v))
			requestProfileUpdate();
		else if (secessionLayout.equals(v))
			userSecession();
	}

	private void showNameDialog()
	{
		if (null == userProfile) return;
		
		inputType = INPUT_NAME;
		FragmentManager fragmentManager = getSupportFragmentManager();
		TextInputDialog dialog = new TextInputDialog();
		dialog.setCancelable(true);
		dialog.setTitle(getResources().getString(R.string.SET_NAME));
		dialog.setHint(getResources().getString(R.string.MSG_EMPTY_NAME));
		dialog.show(fragmentManager, "NAME");
	}
	
	private void showGenderDialog()
	{
		if (null == userProfile) return;
		
		final String genders[] = { getResources().getString(R.string.MAN), getResources().getString(R.string.WOMAN) };
		
	    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
	    dialog.setTitle(R.string.SET_GENDER);
	    dialog.setSingleChoiceItems(genders, this.userProfile.Gender, new DialogInterface.OnClickListener()
	    {			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				isModified = true;
				userProfile.Gender = which;
				updateProfileInformation();
				dialog.dismiss();
			}
		});
	    dialog.show();
	}
	
	private void showWeightDialog()
	{
		if (null == userProfile) return;
		
		final Vector<String> weights = new Vector<String>();
		final int minWeight = 35, maxWeight = 120;
		
		for (int i = minWeight; i <= maxWeight; i++)
			weights.add(String.format(Locale.US, "%dkg", i));
		
		String[] items = weights.toArray(new String[weights.size()]);
		
	    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
	    dialog.setTitle(R.string.SET_WEIGHT);
	    
	    int position;
	    if (0 == userProfile.Weight)
	    	position = Globals.DEFAULT_WEIGHT - minWeight;
	    else
	    	position = userProfile.Weight - minWeight;

	    dialog.setSingleChoiceItems(items, position, new DialogInterface.OnClickListener()
	    {			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				isModified = true;
				userProfile.Weight = minWeight + which;
				updateProfileInformation();
				dialog.dismiss();
			}
		});
	    dialog.show();	
	}
	
	private void showDatePickerDialog()
	{
		if (null == userProfile) return;
		
		int year = userProfile.Birthday.getYear() + 1900;
		int month = userProfile.Birthday.getMonth();
		int day = userProfile.Birthday.getDate();
		
		DatePickerDialog dlg = new DatePickerDialog(this, this, year, month, day);
		dlg.show();
	}
	
	private void showGroupConfig()
	{
		if (null == userProfile) return;
		
		if (0 == userProfile.CommunitySeq)
		{
			// 그룹에 가입하지 않은 경우 그룹 검색 화면 이동
			Intent i = new Intent(this, CommunitySelectionActivity.class);
			startActivityForResult(i, Globals.INTENT_REQ_GROUP_SELECT);
		}
		else
		{
			// 그룹 상세 정보 표시(그룹 탈퇴 가능)
			Intent i = new Intent(this, CommunityDetailActivity.class);
			i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, userProfile.CommunitySeq);
			startActivityForResult(i, Globals.INTENT_REQ_GROUP_SELECT);
		}
	}
	
	private void showAreaSelection()
	{
		if (null == userProfile) return;
		
		final Vector<String> vecItems = new Vector<String>();
		
	    int position = 0;
		for (int i = 0; i < this.areaItems.size(); i++)
		{
			AreaItem item = this.areaItems.get(i);
			vecItems.add(item.Name);
			if (item.Code == userProfile.AreaCode)
				position = i;
		}
		
		String[] items = vecItems.toArray(new String[vecItems.size()]);
		
	    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
	    dialog.setTitle(userProfile.IsGroupUser ? R.string.SET_ORGANIZATION : R.string.SET_LOCAL);
	    
	    dialog.setSingleChoiceItems(items, position, new DialogInterface.OnClickListener()
	    {			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				isModified = true;
				userProfile.AreaCode = areaItems.get(which).Code;
				userProfile.AreaName = areaItems.get(which).Name;
				userProfile.AreaSubCode = 0;
				userProfile.AreaSubName = "";
				updateProfileInformation();

				dialog.dismiss();
			}
		});
	    dialog.show();	

	}
	
	private void showSubAreaSelection()
	{
		if (null == userProfile) return;
		
		final Vector<String> vecItems = new Vector<String>();
		
	    int position = 0;
		for (int i = 0; i < this.subAreaItems.size(); i++)
		{
			AreaItem item = this.subAreaItems.get(i);
			vecItems.add(item.Name);
			if (item.Code == userProfile.AreaSubCode)
				position = i;
		}
		
		String[] items = vecItems.toArray(new String[vecItems.size()]);
		
	    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
	    dialog.setTitle(userProfile.IsGroupUser ? R.string.SET_ORGANIZATION : R.string.SET_LOCAL);
	    
	    dialog.setSingleChoiceItems(items, position, new DialogInterface.OnClickListener()
	    {			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				isModified = true;
				userProfile.AreaSubCode = subAreaItems.get(which).Code;
				userProfile.AreaSubName = subAreaItems.get(which).Name;
				updateProfileInformation();
				
				dialog.dismiss();
			}
		});
	    dialog.show();	
	}
	
	private void showPasswordDialog()
	{
		if (null == userProfile) return;
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		PasswordDialog dialog = new PasswordDialog();
		dialog.setCancelable(true);
		dialog.show(fragmentManager, "PASSWORD");
	}
	
	private void showSecessionPasswordDialog()
	{
		if (null == userProfile) return;
		
		inputType = INPUT_PASSWORD;
		FragmentManager fragmentManager = getSupportFragmentManager();
		TextInputDialog dialog = new TextInputDialog();
		dialog.setCancelable(true);
		dialog.setTitle(getResources().getString(R.string.SET_PASSWORD));
		dialog.setHint(getResources().getString(R.string.PASSWORD));
		dialog.setIsPassword(true);
		dialog.show(fragmentManager, "SECESSION");
	}
	
	private void requestAreaItems()
	{
		if (!progDlg.isShowing()) progDlg.show();
		this.reqType = REQUEST_AREAITEM;
		this.server.AreaItems(this, this, 0);
	}
	
	private void requestSubAreaItems()
	{
		if (!progDlg.isShowing()) progDlg.show();
		this.reqType = REQUEST_SUBAREAITEM;
		this.server.AreaItems(this, this, userProfile.AreaCode);
	}
	
	private void requestProfileUpdate()
	{
		if (0 == userProfile.AreaCode || 0 == userProfile.AreaSubCode)
		{
			Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_INVALID_AREA);
			return;
		}
		
		if (!progDlg.isShowing()) progDlg.show();
		this.reqType = REQUEST_UPDATE_PROFILE;
		this.server.UpdateProfile(this, this, this.userProfile);
	}
	
	private void userSecession()
	{
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setCancelable(true);
		dlg.setTitle(R.string.TITLE_INFORMATION);
		dlg.setMessage(R.string.MSG_SECESSION_CONFIRM);
		dlg.setPositiveButton(R.string.CONTINUE, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				showSecessionPasswordDialog();
			}
		});
		dlg.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		dlg.show();

	}

	private void updateProfileInformation()
	{
		if (null == this.userProfile) return;
		
		TextView tvEmail = (TextView)findViewById(R.id.email);
		TextView tvName = (TextView)findViewById(R.id.name);
		TextView tvSex = (TextView)findViewById(R.id.sex);
		TextView tvBirthday = (TextView)findViewById(R.id.birthday);
		TextView tvWeight = (TextView)findViewById(R.id.weight);
		TextView tvJoinGroup = (TextView)findViewById(R.id.groupName);
		TextView tvArea = (TextView)findViewById(R.id.areaName);
		TextView tvSubArea = (TextView)findViewById(R.id.subAreaName);
		
		tvEmail.setText(userProfile.UserId);
		tvName.setText(userProfile.Name);
		tvArea.setText(userProfile.AreaName);
		tvSubArea.setText(userProfile.AreaSubName);
		
		if (0 == userProfile.Gender)
			tvSex.setText(R.string.MAN);
		else
			tvSex.setText(R.string.WOMAN);
		
		if (null == userProfile.Birthday)
		{
			tvBirthday.setText(R.string.UNSET);
		}
		else
		{
			SimpleDateFormat formatter = new SimpleDateFormat(Globals.DATE_FORMAT_FOR_SERVER, Locale.US);
			tvBirthday.setText(formatter.format(userProfile.Birthday));
		}
		
		if (0 == userProfile.Weight)
			tvWeight.setText(R.string.UNSET);
		else			
			tvWeight.setText(String.format(Locale.US, "%skg", userProfile.Weight));
		
		if (null == userProfile.CommunityName)
			tvJoinGroup.setText(R.string.NO_GROUP);
		else
			tvJoinGroup.setText(userProfile.CommunityName);
		
		if (isModified)
			this.applyButton.setVisibility(View.VISIBLE);
		else
			this.applyButton.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onFinishInputDialog(String input)
	{
		if (INPUT_NAME == this.inputType)
		{
			if (null == userProfile) return;
			
			this.isModified = true;
			userProfile.Name = input;
			
			updateProfileInformation();
		}
		else if (INPUT_PASSWORD == this.inputType)
		{
			if (!progDlg.isShowing()) progDlg.show();
			
			reqType = REQUEST_SECESSION_PASSWORD;
			server.UserSecessionPassword(ProfileActivity.this, ProfileActivity.this, input);
		}
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
	{
		if (null == this.userProfile) return;
		
		this.isModified = true;
		
		this.userProfile.Birthday.setYear(year-1900);
		this.userProfile.Birthday.setMonth(monthOfYear);
		this.userProfile.Birthday.setDate(dayOfMonth);
		
		updateProfileInformation();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Globals.INTENT_REQ_GROUP_SELECT == requestCode)
		{
			if (RESULT_OK == resultCode)
				requestUserProfile();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onErrorResponse(VolleyError e)
	{
		if (this.progDlg.isShowing()) progDlg.dismiss();
		Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_API_FAIL);
		e.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (this.progDlg.isShowing()) progDlg.dismiss();

		if (0 == response.length()) return;
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (REQUEST_USER_PROFILE == this.reqType)
		{
			this.reqType = 0;
			if (Globals.ERROR_NONE == result.Code)
			{
				this.userProfile = parser.GetAccountData();
				if (null != this.userProfile)
					updateProfileInformation();
			}
			else
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_API_FAIL);
				finish();
			}
		}
		else if (REQUEST_AREAITEM == this.reqType)
		{
			this.reqType = 0;
			if (Globals.ERROR_NONE == result.Code)
			{
				Vector<AreaItem> items = parser.GetAreaItems();
				this.areaItems.clear();
				this.areaItems.addAll(items);
				showAreaSelection();
			}			
		}
		else if (REQUEST_SUBAREAITEM == this.reqType)
		{
			this.reqType = 0;
			if (Globals.ERROR_NONE == result.Code)
			{
				Vector<AreaItem> items = parser.GetAreaItems();
				this.subAreaItems.clear();
				this.subAreaItems.addAll(items);
				showSubAreaSelection();
				
			}
		}
		else if (REQUEST_UPDATE_PROFILE == this.reqType)
		{
			this.reqType = 0;
			if (Globals.ERROR_NONE == result.Code)
			{
				this.isModified = false;
				this.applyButton.setVisibility(View.INVISIBLE);

				ServerRequestManager.LoginAccount.Name = userProfile.Name;
				ServerRequestManager.LoginAccount.AreaCode = userProfile.AreaCode;
				ServerRequestManager.LoginAccount.AreaName = userProfile.AreaName;
				ServerRequestManager.LoginAccount.AreaSubCode = userProfile.AreaSubCode;
				ServerRequestManager.LoginAccount.AreaSubName = userProfile.AreaSubName;
				ServerRequestManager.LoginAccount.Birthday = userProfile.Birthday;
				ServerRequestManager.LoginAccount.CommunitySeq = userProfile.CommunitySeq;
				ServerRequestManager.LoginAccount.CommunityName = userProfile.CommunityName;
				ServerRequestManager.LoginAccount.Gender = userProfile.Gender;
				ServerRequestManager.LoginAccount.Weight = userProfile.Weight;
			}
			else
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_API_FAIL);
			}
		}
		else if (REQUEST_SECESSION_PASSWORD == this.reqType)
		{
			this.reqType = 0;
			if (Globals.ERROR_NONE == result.Code)
			{
				if (!progDlg.isShowing()) progDlg.show();
				this.reqType = REQUEST_SECESSION;
				this.server.UserSecession(this, this);
			}
			else if (Globals.ERROR_PASSWORD_NOT_MATCH == result.Code)
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_PASSWORD_MISSMATCH);
			}
		}
		else if (REQUEST_SECESSION == this.reqType)
		{
			this.reqType = 0;
			if (Globals.ERROR_NONE == result.Code)
			{
				ServerRequestManager.IsLogin = false;
				SocialWalkRequest.ClearSessionInformation();
				
				SharedPreferences loginPrefs = this.getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = loginPrefs.edit();
				editor.putBoolean(Globals.PREF_KEY_AUTOLOGIN, false);
				editor.commit();

				finish();
			}
			else 
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_API_FAIL);
			}
		}
	}

}
