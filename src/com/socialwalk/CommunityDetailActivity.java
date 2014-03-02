package com.socialwalk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.CommunityNameDialog.CommunityNameDialogListener;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.TextInputDialog.TextInputDialogListener;
import com.socialwalk.dataclass.CommunityDetail;
import com.socialwalk.request.ServerRequestManager;

public class CommunityDetailActivity extends FragmentActivity
implements Response.Listener<String>, Response.ErrorListener, OnClickListener,
TextInputDialogListener, CommunityNameDialogListener
{
	private ServerRequestManager server;
	private int reqType;
	private static final int REQUEST_COMMUNITY_DETAIL = 400;
	private static final int REQUEST_COMMUNITY_SECESSION = 401;
	private static final int REQUEST_COMMUNITY_MODIFY = 402;
	
	private int communitySeq = 0;
	private CommunityDetail detailData = null;
	private Button manageButton, secessionButton, nameEditButton, descEditButton;
	private ProgressDialog progDlg;
	
	private int dialogType = 0;
	private static final int DIALOG_TYPE_NAME = 300;
	private static final int DIALOG_TYPE_DESC = 301;
	private String modifyName, modifyDesc;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_detail);
		
		this.server = new ServerRequestManager();
		
		// prepare progress dialog
		progDlg = new ProgressDialog(this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);
		progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));
		
		this.manageButton = (Button)findViewById(R.id.manageButton);
		this.manageButton.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), CommunityManageActivity.class);
				i.putExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, communitySeq);
				startActivityForResult(i, Globals.INTENT_REQ_COMMUNITY_DELETE);				
			}
		});
		
		this.secessionButton = (Button)findViewById(R.id.secessionButton);
		this.secessionButton.setOnClickListener(this);
		
		this.nameEditButton = (Button)findViewById(R.id.nameEditButton);
		this.nameEditButton.setOnClickListener(this);
		
		this.descEditButton = (Button)findViewById(R.id.descEditButton);
		this.descEditButton.setOnClickListener(this);
		
		this.communitySeq = getIntent().getIntExtra(Globals.EXTRA_KEY_COMMUNITY_SEQUENCE, 0);
		if (0 < this.communitySeq)
		{
			progDlg.show();
			this.reqType = REQUEST_COMMUNITY_DETAIL;
			this.server.CommunityDetail(this, this, this.communitySeq);
		}
	}

	private void updateCommunityDetail(CommunityDetail detail)
	{
		TextView tvName = (TextView)findViewById(R.id.communityName);
		TextView tvDesc = (TextView)findViewById(R.id.description);
		
		tvName.setText(detail.Name);
		tvDesc.setText(detail.Description);
		
		if (detail.Mastersequence.equalsIgnoreCase(ServerRequestManager.LoginAccount.Sequence))
		{
			manageButton.setVisibility(View.VISIBLE);
			nameEditButton.setVisibility(View.VISIBLE);
			descEditButton.setVisibility(View.VISIBLE);
			secessionButton.setVisibility(View.INVISIBLE);
		}
		else
		{
			manageButton.setVisibility(View.INVISIBLE);
			nameEditButton.setVisibility(View.INVISIBLE);
			descEditButton.setVisibility(View.INVISIBLE);
			secessionButton.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Globals.INTENT_REQ_COMMUNITY_DELETE == requestCode)
		{
			if (RESULT_OK == resultCode)
			{
				setResult(RESULT_OK);
				finish();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onErrorResponse(VolleyError error)
	{
		if (progDlg.isShowing()) progDlg.dismiss();
		
		error.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (progDlg.isShowing()) progDlg.dismiss();

		if (0 == response.length()) return;
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (REQUEST_COMMUNITY_DETAIL == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				this.detailData = parser.GetCommunityDetail();
				if (null == detailData) return;
				updateCommunityDetail(detailData);
			}
		}
		else if (REQUEST_COMMUNITY_SECESSION == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				setResult(RESULT_OK);
				finish();
			}
			else
			{
				Utils.GetDefaultTool().ShowMessageDialog(this, R.string.MSG_API_FAIL);
			}
		}
		else if (REQUEST_COMMUNITY_MODIFY == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				detailData.Name = modifyName;
				detailData.Description = modifyDesc;
				updateCommunityDetail(detailData);
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		if (this.secessionButton.equals(v))
		{
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			dlg.setCancelable(false);
			dlg.setTitle(R.string.TITLE_INFORMATION);
			dlg.setMessage(R.string.MSG_COMMUNITY_SECESSION_CONFIRM);
			dlg.setPositiveButton(R.string.CONTINUE, new DialogInterface.OnClickListener()
			{	
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					if (!progDlg.isShowing()) progDlg.show();
					reqType = REQUEST_COMMUNITY_SECESSION;
					server.CommunitySecession(CommunityDetailActivity.this, CommunityDetailActivity.this);
				}
			});
			
			dlg.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener()
			{				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			dlg.show();
		}
		else if (nameEditButton.equals(v))
		{
			showNameEditDialog();
		}
		else if (descEditButton.equals(v))
		{
			showDescriptionEditDialog();
		}
	}

	private void showNameEditDialog()
	{
		dialogType = DIALOG_TYPE_NAME;
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		CommunityNameDialog dialog = new CommunityNameDialog();
		dialog.setCancelable(true);
		dialog.show(fragmentManager, "NAME");
	}
	
	private void showDescriptionEditDialog()
	{
		if (null == this.detailData) return;
		
		dialogType = DIALOG_TYPE_DESC;
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		TextInputDialog dialog = new TextInputDialog();
		dialog.setCancelable(true);
		dialog.setTitle(getResources().getString(R.string.GROUP_DESCRIPTION));
		dialog.setHint(getResources().getString(R.string.GROUP_DESCRIPTION));
		dialog.show(fragmentManager, "DESC");
	}
	
	private void communityModify(String name, String description)
	{
		if (false == progDlg.isShowing()) progDlg.show();
		
		reqType = REQUEST_COMMUNITY_MODIFY;
		server.CommunityModify(this, this, communitySeq, name, description);
	}
	
	@Override
	public void onFinishInputDialog(String input)
	{
		if (DIALOG_TYPE_DESC == this.dialogType)
		{
			modifyName = this.detailData.Name;
			modifyDesc = input;
			communityModify(modifyName, modifyDesc);
		}
	}

	@Override
	public void onFinishNameDialog(String input)
	{
		if (DIALOG_TYPE_NAME == this.dialogType)
		{
			modifyName = input;
			modifyDesc = this.detailData.Description;
			communityModify(modifyName, modifyDesc);
		}
	}
	
	
}
