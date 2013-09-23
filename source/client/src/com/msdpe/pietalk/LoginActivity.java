package com.msdpe.pietalk;

import com.msdpe.pietalk.SignupActivity.DatePickerFragment;
import com.msdpe.pietalk.util.TextValidator;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoginActivity extends Activity {
	
	private final String TAG = "LoginActivity";
	private EditText mTxtUsername;
	private EditText mTxtPassword;
	private Button mBtnLogin;
	private ProgressBar mProgressLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		mTxtUsername = (EditText) findViewById(R.id.txtUsername);
		mTxtPassword = (EditText) findViewById(R.id.txtPassword);
		mBtnLogin = (Button) findViewById(R.id.btnLogin);
		mProgressLogin = (ProgressBar) findViewById(R.id.progressLogin);
		
		mTxtUsername.addTextChangedListener(new TextValidator(mTxtUsername) {			
			@Override
			public void validate(TextView textView, String text) {
				checkValid();
			}
		});
		mTxtPassword.addTextChangedListener(new TextValidator(mTxtPassword) {			
			@Override
			public void validate(TextView textView, String text) {
				checkValid();
				
			}
		});
		
		mBtnLogin.setOnClickListener(loginListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	private OnClickListener loginListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
 
		}
	};
	
	private void checkValid() {
		if (this.isValid()) {
			mBtnLogin.setBackgroundResource(R.drawable.sign_up_button_style);
		} else {
			mBtnLogin.setBackgroundResource(R.drawable.second_sign_up_button_style);
		}
	}
	
	private boolean isValid() {
		if (mTxtUsername.getText().toString().length() < Constants.MIN_PASSWORD_LENGTH)
			return false;
		if (mTxtPassword.getText().toString().equals(""))
			return false;
		
		return true;
	}

}
