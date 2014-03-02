package com.socialwalk;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.request.ServerRequestManager;

public class CommunityNameDialog extends DialogFragment
{
	private ServerRequestManager server;
	private EditText nameField;
	private boolean isValidName;
	
	private int reqType = 0;
	private static int REQUEST_NAME_CHECK = 100;
	private VolleyListener serverListener;

	public interface CommunityNameDialogListener
	{
		void onFinishNameDialog(String input);
	}
	
	private class VolleyListener implements Response.Listener<String>, Response.ErrorListener
	{
		@Override
		public void onErrorResponse(VolleyError err)
		{
			err.printStackTrace();
		}

		@Override
		public void onResponse(String response)
		{
			if (0 == response.length()) return;
			MyXmlParser parser = new MyXmlParser(response);
			SWResponse result = parser.GetResponse();
			if (null == result) return;
			
			if (REQUEST_NAME_CHECK == reqType)
			{
				if (Globals.ERROR_NO_RESULT == result.Code)
				{
					isValidName = true;
					
					Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_AVAILABLE_NAME);
				}
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_community_name, container);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		this.server = new ServerRequestManager();
		this.serverListener = new VolleyListener();
		this.isValidName = false;
		
		Button checkButton = (Button)view.findViewById(R.id.checkButton);
		checkButton.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				String input_name = nameField.getText().toString();
				if (input_name.isEmpty())
				{
					Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_EMPTY_EMAIL);
					return;
				}

				reqType = REQUEST_NAME_CHECK;
				server.IsExistCommunityName(serverListener, serverListener, input_name);
			}
		});
		
		Button okButton = (Button)view.findViewById(R.id.okButton);
		okButton.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				String strEmail = nameField.getText().toString();
				if (0 == strEmail.length())
				{
					Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_EMPTY_EMAIL);
					return;
				}
				
				if (false == isValidName)
				{
					Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_DUPLICATE_CHECK);
					return;
				}
				
				CommunityNameDialogListener listener = (CommunityNameDialogListener)getActivity();
				listener.onFinishNameDialog(nameField.getText().toString());
				dismiss();
			}
		});
		
		Button cancelButton = (Button)view.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener()
		{				
			@Override
			public void onClick(View v)
			{
				dismiss();
			}
		});
		
		this.nameField = (EditText)view.findViewById(R.id.name);
		this.nameField.requestFocus();
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		return view;
	}
}
