package com.msdpe.pietalk;

import com.msdpe.pietalk.activities.SplashScreenActivity;

import android.app.Activity;
import android.app.Application;

public class PieTalkApplication extends Application {
	private PieTalkService mPieTalkService;
	private Activity mCurrentActivity;
	private SplashScreenActivity mSplashScreenActivity;
	
	public PieTalkApplication() {}
	
	public PieTalkService getPieTalkService() {
		if (mPieTalkService == null) {
			mPieTalkService = new PieTalkService(this);
		}
		return mPieTalkService;
	}	
	
	public void setCurrentActivity(Activity activity) {
		mCurrentActivity = activity;
	}
	
	public Activity getCurrentActivity() {
		return mCurrentActivity;
	}
	
	public void setSplashScreenActivity(SplashScreenActivity activity) {
		mSplashScreenActivity = activity;
	}
	
	public SplashScreenActivity getSplashScreenActivity() {
		return mSplashScreenActivity;
	}
}
