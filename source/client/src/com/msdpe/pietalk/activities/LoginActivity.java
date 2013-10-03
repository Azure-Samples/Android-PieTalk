package com.msdpe.pietalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.msdpe.pietalk.Constants;
import com.msdpe.pietalk.R;
import com.msdpe.pietalk.R.drawable;
import com.msdpe.pietalk.R.id;
import com.msdpe.pietalk.R.layout;
import com.msdpe.pietalk.base.BaseActivity;
import com.msdpe.pietalk.util.PieTalkAlert;
import com.msdpe.pietalk.util.PieTalkRegisterResponse;
import com.msdpe.pietalk.util.TextValidator;

public class LoginActivity extends BaseActivity {
	
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
			mBtnLogin.setVisibility(View.GONE);
			mProgressLogin.setVisibility(View.VISIBLE);
			
			mPieTalkService.loginUser(mTxtUsername.getText().toString(), 
					mTxtPassword.getText().toString(), new ApiOperationCallback<PieTalkRegisterResponse>() {
				
				@Override
				public void onCompleted(PieTalkRegisterResponse response, Exception exc,
						ServiceFilterResponse arg2) {
					if (exc != null || response.Error != null) {
						mBtnLogin.setVisibility(View.VISIBLE);
						mProgressLogin.setVisibility(View.GONE);
						//Display error
						
						if (exc != null) 
							PieTalkAlert.showSimpleErrorDialog(mActivity, exc.getCause().getMessage());
						else
							PieTalkAlert.showSimpleErrorDialog(mActivity, response.Error);									
						
					} else {
						mPieTalkService.setUserAndSaveData(response);
						//mPieTalkService.setUserAndSaveData(jsonData);
							
						finish();
						//Launch application
						Intent intent = new Intent(mActivity, RecordActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						
					}
					
				}
			});
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
		if (mTxtUsername.getText().toString().length() < Constants.MIN_USERNAME_LENGTH)
			return false;
		if (mTxtPassword.getText().toString().length() < Constants.MIN_PASSWORD_LENGTH)
			return false;
		if (mTxtPassword.getText().toString().equals(""))
			return false;
		
		return true;
	}

}
