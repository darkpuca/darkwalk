package com.socialwalk;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TextInputDialog extends DialogFragment
{
	public interface TextInputDialogListener
	{
		void onFinishInputDialog(String input);
	}
	
	private String title, hint;
	private EditText inputText;
	private boolean isPassword = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_text_input, container);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		this.inputText = (EditText)view.findViewById(R.id.input);
		
		Button okButton = (Button)view.findViewById(R.id.okButton);
		okButton.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if (0 == inputText.getText().length())
				{
					Utils.GetDefaultTool().ShowMessageDialog(getActivity(), R.string.MSG_EMPTY_CONTENTS);
					return;
				}
				
				TextInputDialogListener listener = (TextInputDialogListener)getActivity();
				listener.onFinishInputDialog(inputText.getText().toString());
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
		
		this.inputText.requestFocus();
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		if (null != this.title)
		{
			TextView tvTitle = (TextView)view.findViewById(R.id.title);
			tvTitle.setText(this.title);
		}
		
		if (null != this.hint)
		{
			this.inputText.setHint(this.hint);
		}
		
		if (this.isPassword)
			this.inputText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		
		return view;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public void setHint(String hint)
	{
		this.hint = hint;
	}

	public void setIsPassword(boolean isPassword)
	{
		this.isPassword = isPassword;
	}
}
