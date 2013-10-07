package com.msdpe.pietalk.activities;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.msdpe.pietalk.Constants;
import com.msdpe.pietalk.R;
import com.msdpe.pietalk.SettingsActivity;
import com.msdpe.pietalk.base.BaseActivity;
import com.msdpe.pietalk.util.PieTalkLogger;

public class PiesListActivity extends BaseActivity {
	
	private final String TAG = "PiesListActivity";
	private ListView mLvPies;
	private ArrayAdapter<String> mAdapter;	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);		
		super.onCreate(savedInstanceState, true);
		setContentView(R.layout.activity_pies_list);
		// Show the Up button in the action bar.
		setupActionBar();
		
		mLvPies = (ListView) findViewById(R.id.lvPies);
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
		mPullToRefreshAttacher.addRefreshableView(mLvPies, new OnRefreshListener() {			
			@Override
			public void onRefreshStarted(View arg0) {
				// TODO Auto-generated method stub
				mPieTalkService.getPies();
				
			}
		});
		
		mAdapter = new ArrayAdapter<String>(this,
    	        android.R.layout.simple_list_item_1, mPieTalkService.getLocalPieUsernames());
		mLvPies.setAdapter(mAdapter);
	}
	
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		//filter.addAction(Constants.BROADCAST_PIES_UPDATED);
		filter.addAction(Constants.BROADCAST_PIES_UPDATED);
		registerReceiver(receiver, filter);
		super.onResume();	
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			mAdapter.clear();			
			for (String item : mPieTalkService.getLocalPieUsernames()) {
				mAdapter.add(item);
			}		
			PieTalkLogger.i(TAG, "Refresh complete");
			mPullToRefreshAttacher.setRefreshComplete();
			mPullToRefreshAttacher.setRefreshing(false);
			
		}
	};

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);	
		ActionBar bar = getActionBar();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pies_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
			return true;		
		case R.id.menuSettings:
			PieTalkLogger.i(TAG, "Need to implement settings");
			//Intent intent = new Intent(mActivity, SettingsActivity.class);
			//startActivity(intent);
			//finish();
			//mPieTalkService.getPies();
			//mPullToRefreshAttacher.setRefreshing(true);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}

}
