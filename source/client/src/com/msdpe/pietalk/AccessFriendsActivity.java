package com.msdpe.pietalk;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

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

}
