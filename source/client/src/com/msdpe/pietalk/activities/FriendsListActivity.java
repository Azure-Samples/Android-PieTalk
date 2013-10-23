package com.msdpe.pietalk.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.SearchManager;
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
import android.widget.Filter.FilterListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.msdpe.pietalk.Constants;
import com.msdpe.pietalk.R;
import com.msdpe.pietalk.base.BaseActivity;
import com.msdpe.pietalk.util.PieTalkAlert;
import com.msdpe.pietalk.util.PieTalkLogger;
import com.msdpe.pietalk.util.PieTalkRegisterResponse;

public class FriendsListActivity extends BaseActivity {
	
	private final String TAG = "FriendsListActivity";
	private ListView mLvFriends;
	private ArrayAdapter<String> mAdapter;
	private LinearLayout mLayoutAddFriend;
	private TextView mLblNewFriendName;
	private String mCurrentName;
	private ImageButton mBtnAddFriend;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		super.onCreate(savedInstanceState, true);
		setContentView(R.layout.activity_friends_list);
		// Show the Up button in the action bar.
		setupActionBar();
		
		mLayoutAddFriend = (LinearLayout) findViewById(R.id.layoutAddFriend);
		mLayoutAddFriend.setVisibility(View.GONE);
		
		mLblNewFriendName = (TextView) findViewById(R.id.lblNewFriendName);
		mBtnAddFriend = (ImageButton) findViewById(R.id.btnAddFriend);
		
		mLvFriends = (ListView) findViewById(R.id.lvFriends);
		mLvFriends.setOverScrollMode(View.OVER_SCROLL_NEVER);
		
//		final ArrayList<String> list = new ArrayList<String>();
//	    for (int i = 0; i < values.length; ++i) {
//	      list.add(values[i]);
//	    }
	    
	    //mAdapter = new ArrayAdapter<String>(this,
	    //    android.R.layout.simple_list_item_1, list);
	    mAdapter = new ArrayAdapter<String>(this,
	    	        android.R.layout.simple_list_item_1, mPieTalkService.getLocalFriendNames());
	    mLvFriends.setAdapter(mAdapter);
	}
	
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_FRIENDS_UPDATED);
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
			PieTalkLogger.i(TAG, "Broadcast received");
			mAdapter.clear();

			for (String item : mPieTalkService.getLocalFriendNames()) {
				mAdapter.add(item);
			}		
		}
	};

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friends_list, menu);
		
		// Associate searchable configuration with the SearchView
	    SearchManager searchManager =
	           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView =
	            (SearchView) menu.findItem(R.id.menuSearch).getActionView();
	    searchView.setSearchableInfo(
	            searchManager.getSearchableInfo(getComponentName()));
	    //searchView.setQueryHint("test");
	    searchView.setOnQueryTextListener(new OnQueryTextListener() {			
	    		@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(final String newText) {
				PieTalkLogger.i(TAG, "Text: " + newText);
				mCurrentName = newText;
				mBtnAddFriend.setEnabled(true);
				mBtnAddFriend.setVisibility(View.VISIBLE);				
				mAdapter.getFilter().filter(newText, new FilterListener() {					
					@Override
					public void onFilterComplete(int count) {
						if (mAdapter.getCount() > 0) 
							mLvFriends.setVisibility(View.VISIBLE);
						else
							mLvFriends.setVisibility(View.GONE);
						
						if (!mCurrentName.equals("")) {
							mLayoutAddFriend.setVisibility(View.VISIBLE);
						} else {
							mLayoutAddFriend.setVisibility(View.GONE);
						}
						
						if (mPieTalkService.getLocalFriendNames().contains(newText))
							mLayoutAddFriend.setVisibility(View.GONE);
					}
				});
				mLblNewFriendName.setText(mCurrentName);
				
				
				return true;
			}
		});		
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
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
			return true;
		case R.id.menuSearch:
			
			return true;
		case R.id.menuAddFriends:
			PieTalkLogger.i(TAG, "Need to implement adding friends");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);				
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            PieTalkLogger.i(TAG, "Search for: " + query);
        }
	}
	
	public void tappedAddFriend(View view) {
		Toast.makeText(this, "Adding " + mCurrentName +"...", Toast.LENGTH_SHORT).show();
		mBtnAddFriend.setEnabled(false);
		
		mPieTalkService.requestFriend(mCurrentName, new ApiOperationCallback<PieTalkRegisterResponse>() {			
			@Override
			public void onCompleted(PieTalkRegisterResponse response, Exception ex,
					ServiceFilterResponse arg2) {
				PieTalkLogger.i(TAG, "Response received");
				if (ex != null || response.Error != null) {
					mBtnAddFriend.setEnabled(true);										
					//Display error					
					if (ex != null)
						PieTalkAlert.showSimpleErrorDialog(mActivity, ex.getCause().getMessage());
					else 
						Toast.makeText(mActivity, response.Error, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mActivity, response.Status, Toast.LENGTH_SHORT).show();
					mBtnAddFriend.setVisibility(View.GONE);
					mPieTalkService.getFriends();					
				}
			}
		});
		
		//Make friend request
		//if error, show error in toast
		//if success, save friend request, show this toast
		//"username is private.  Friend request sent."
		//remove button
		//when pulling down friends, pull down pending too, show "pending..." under username in friends list
		
		//On other side, don't show friend in friend list, show request in message list
	}
}
