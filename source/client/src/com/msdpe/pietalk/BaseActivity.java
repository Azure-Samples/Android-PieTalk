package com.msdpe.pietalk;

import android.app.Activity;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {

	protected PieTalkService mPieTalkService;
	protected BaseActivity mActivity;
	protected PieTalkApplication mApplication;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mActivity = this;
		
		mApplication = (PieTalkApplication) getApplication();
		mApplication.setCurrentActivity(this);
		mPieTalkService = mApplication.getPieTalkService();
	}
}
