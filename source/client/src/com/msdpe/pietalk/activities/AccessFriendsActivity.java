package com.msdpe.pietalk.activities;

import com.msdpe.pietalk.R;
import com.msdpe.pietalk.R.layout;
import com.msdpe.pietalk.base.BaseActivity;
import com.msdpe.pietalk.util.PieTalkAlert;

import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class AccessFriendsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_access_friends);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.access_friends, menu);
		return true;
	}
	
	public void tappedAllowAccess(View view) {
		PieTalkAlert.showSimpleErrorDialog(this, "Need to import friends now!");
	}
	
	public void tappedSkip(View view) {
		goToCamera();
	}
	@Override
	public void onBackPressed() {
		goToCamera();
	}
	
	private void goToCamera() {
		Intent intent = new Intent(mActivity, RecordActivity.class);
		startActivity(intent);
		finish();
		//Show the camera!
		
	}
}
