package com.msdpe.pietalk;

import android.app.Activity;
import android.app.Application;

public class PieTalkApplication extends Application {
	private PieTalkService mPieTalkService;
	private Activity mCurrentActivity;
	
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
}
