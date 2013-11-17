package com.socialwalk;

import android.content.Context;
import android.content.SharedPreferences;
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

public class PasswordDialog extends DialogFragment
implements Response.Listener<String>, Response.ErrorListener, View.OnClickListener
{
	private ServerRequestManager server;
	private EditText password, confirmPassword;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_password, container);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		this.server = new ServerRequestManager();
		
		this.password = (EditText)view.findViewById(R.id.password);
		this.confirmPassword = (EditText)view.findViewById(R.id.passwordConfirm);
		
		Button okButton = (Button)view.findViewById(R.id.okButton);
		okButton.setOnClickListener(this);
		
		Button cancelButton = (Button)view.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener()
		{				
			@Override
			public void onClick(View v)
			{
				dismiss();
			}
		});
		
		this.password.requestFocus();
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		return view;
	}
	

	@Override
	public void onClick(View v)
	{
		if (0 == password.getText().length())
		{
			Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_EMPTY_PASSWORD);
			return;
		}

		if (0 == confirmPassword.getText().length())
		{
			Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_EMPTY_PASSWORD_CONFIRM);
			return;
		}
		
		if (false == password.getText().toString().equalsIgnoreCase(confirmPassword.getText().toString()))
		{
			Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_PASSWORD_MISSMATCH);
			return;
		}
		
		// TODO: 서버 통신
		server.ChangePassword(this, this, password.getText().toString());
	}
	

	@Override
	public void onErrorResponse(VolleyError e)
	{
		e.printStackTrace();
		Utils.GetDefaultTool().ShowMessageDialog(getDialog().getContext(), R.string.MSG_API_FAIL);
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
			SharedPreferences loginPrefs = getDialog().getContext().getSharedPreferences(Globals.PREF_NAME_LOGIN, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = loginPrefs.edit();
			editor.putString(Globals.PREF_KEY_PASSWORD, password.getText().toString());
			editor.commit();

			dismiss();
		}
		else
		{
			Utils.GetDefaultTool().ShowMessageDialog(getDialog().getContext(), R.string.MSG_API_FAIL);
		}
	}
}
