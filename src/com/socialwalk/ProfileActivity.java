package com.socialwalk;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.TextInputDialog.TextInputDialogListener;
import com.socialwalk.dataclass.AccountData;
import com.socialwalk.request.ServerRequestManager;

public class ProfileActivity extends FragmentActivity
implements TextInputDialogListener, OnClickListener, Response.Listener<String>, Response.ErrorListener, OnDateSetListener
{
	private ServerRequestManager server;
	private int reqType, inputType;
	private static final int REQUEST_USER_PROFILE = 100;
	private static final int INPUT_NAME = 300;
	
	private AccountData userProfile;
	private boolean isModified;
	private RelativeLayout nameLayout, genderLayout, weightLayout, birthdayLayout, passwordLayout;
	
	
	private ProgressDialog progDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		this.server = new ServerRequestManager();
		this.isModified = false;
		
		this.nameLayout = (RelativeLayout)findViewById(R.id.nameLayout);
		this.genderLayout = (RelativeLayout)findViewById(R.id.sexLayout);
		this.birthdayLayout = (RelativeLayout)findViewById(R.id.birthdayLayout);
		this.weightLayout = (RelativeLayout)findViewById(R.id.weightLayout);
		this.passwordLayout = (RelativeLayout)findViewById(R.id.passwordButtonLayout);
		
		this.nameLayout.setOnClickListener(this);
		this.genderLayout.setOnClickListener(this);
		this.birthdayLayout.setOnClickListener(this);
		this.weightLayout.setOnClickListener(this);
		this.passwordLayout.setOnClickListener(this);
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));
		progDlg.show();

		this.reqType = REQUEST_USER_PROFILE;
		this.server.getUserProfile(this, this);
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
	
	private void showPasswordDialog()
	{
		if (null == userProfile) return;
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		PasswordDialog dialog = new PasswordDialog();
		dialog.setCancelable(true);
		dialog.show(fragmentManager, "PASSWORD");
	}
	
	private void showDatePickerDialog()
	{
		if (null == userProfile) return;
		
		DatePickerDialog dlg = new DatePickerDialog(this, this, userProfile.Birthday.getYear(), userProfile.Birthday.getMonth(), userProfile.Birthday.getDay());
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
		
		tvEmail.setText(userProfile.UserId);
		tvName.setText(userProfile.Name);
		
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
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
	{
		if (null == this.userProfile) return;
		
		this.isModified = true;
		
		this.userProfile.Birthday.setYear(year);
		this.userProfile.Birthday.setMonth(monthOfYear);
		this.userProfile.Birthday.setDate(dayOfMonth);
		
		updateProfileInformation();
	}


	@Override
	public void onErrorResponse(VolleyError e)
	{
		if (this.progDlg.isShowing()) progDlg.dismiss();
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
		
		if (Globals.ERROR_NONE == result.Code)
		{
			this.userProfile = parser.GetAccountData();
			if (null != this.userProfile)
				updateProfileInformation();
		}
	}

}
