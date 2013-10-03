package com.msdpe.pietalk.base;

import com.msdpe.pietalk.PieTalkApplication;
import com.msdpe.pietalk.PieTalkService;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public abstract class BaseActivity extends Activity {

	protected PieTalkService mPieTalkService;
	protected BaseActivity mActivity;
	protected PieTalkApplication mApplication;
	
	@Override	
	protected void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, false);
	}
		
	protected void onCreate(Bundle savedInstanceState, boolean showTitleBar) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (!showTitleBar)
			requestWindowFeature(Window.FEATURE_NO_TITLE); 
		mActivity = this;
		
		mApplication = (PieTalkApplication) getApplication();
		mApplication.setCurrentActivity(this);
		mPieTalkService = mApplication.getPieTalkService();
		mPieTalkService.setContext(this);
	}
}
