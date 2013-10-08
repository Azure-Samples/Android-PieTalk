package com.msdpe.pietalk.activities;

import com.msdpe.pietalk.R;
import com.msdpe.pietalk.R.id;
import com.msdpe.pietalk.R.layout;
import com.msdpe.pietalk.base.BaseActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SplashScreenActivity extends BaseActivity {
	
	private Button mBtnSignup;
	private Button mBtnLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		super.onCreate(savedInstanceState);
		mApplication.setSplashScreenActivity(this);			
		if (mPieTalkService.isUserAuthenticated()) {			
			//Launch application
			Intent intent = new Intent(mActivity, RecordActivity.class);
			//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
		
		setContentView(R.layout.activity_splash_screen);
		
		
		
		mBtnSignup = (Button) findViewById(R.id.btnSignUp);
		mBtnLogin = (Button) findViewById(R.id.btnLogin);
		
		mBtnSignup.setOnClickListener(signupListener);
		mBtnLogin.setOnClickListener(loginListener);
	}
	
	private OnClickListener signupListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			startActivity(new Intent(getApplicationContext(), SignupActivity.class));
		}
	};
	
	private OnClickListener loginListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			startActivity(new Intent(getApplicationContext(), LoginActivity.class));
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}

}
