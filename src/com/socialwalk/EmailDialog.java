package com.socialwalk;

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

public class EmailDialog extends DialogFragment
implements Response.Listener<String>, Response.ErrorListener
{
	private ServerRequestManager server;
	private EditText email;
	private boolean isValidEmail;
	
	public interface EmailDialogListener
	{
		void onFinishEmailDialog(String email);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_email, container);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		this.server = new ServerRequestManager();
		this.isValidEmail = false;
		
		Button checkButton = (Button)view.findViewById(R.id.checkButton);
		checkButton.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				String strEmail = email.getText().toString();
				if (0 == strEmail.length())
				{
					Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_EMPTY_EMAIL);
					return;
				}

			}
		});
		
		Button okButton = (Button)view.findViewById(R.id.okButton);
		okButton.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				String strEmail = email.getText().toString();
				if (0 == strEmail.length())
				{
					Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_EMPTY_EMAIL);
					return;
				}
				
				if (false == isValidEmail)
				{
					Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_DUPLICATE_CHECK);
					return;
				}
				
				EmailDialogListener listener = (EmailDialogListener)getActivity();
				listener.onFinishEmailDialog(email.getText().toString());
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
		
		this.email = (EditText)view.findViewById(R.id.email);
		this.email.requestFocus();
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		return view;
	}

	@Override
	public void onErrorResponse(VolleyError e)
	{
		e.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (0 == response.length()) return;
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (Globals.ERROR_NONE == result.Code)
		{
		}
		
	}

}
