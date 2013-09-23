package com.msdpe.pietalk;

import android.app.Activity;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {

	protected PieTalkService mPieTalkService;
	protected BaseActivity mActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mActivity = this;
		
		PieTalkApplication myApp = (PieTalkApplication) getApplication();
		myApp.setCurrentActivity(this);
		mPieTalkService = myApp.getPieTalkService();
	}
}
