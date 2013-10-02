package com.msdpe.pietalk;

import com.msdpe.pietalk.SignupActivity.DatePickerFragment;
import com.msdpe.pietalk.util.PieTalkAlert;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AccessFriendsActivity extends Activity {

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
		finish();
		//Show the camera!
		
	}
}
