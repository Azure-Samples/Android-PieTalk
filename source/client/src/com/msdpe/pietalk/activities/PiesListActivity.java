package com.msdpe.pietalk.activities;

import java.io.InputStream;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.msdpe.pietalk.Constants;
import com.msdpe.pietalk.R;
import com.msdpe.pietalk.TestSettingsActivity;
import com.msdpe.pietalk.adapters.PiesArrayAdapter;
import com.msdpe.pietalk.base.BaseActivity;
import com.msdpe.pietalk.datamodels.Pie;
import com.msdpe.pietalk.util.PieTalkAlert;
import com.msdpe.pietalk.util.PieTalkLogger;
import com.msdpe.pietalk.util.PieTalkResponse;

public class PiesListActivity extends BaseActivity {
	
	private final String TAG = "PiesListActivity";
	private ListView mLvPies;
	private PiesArrayAdapter mAdapter;	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private boolean mIsViewingPicture;
	private boolean mIsViewingVideo;
	private Dialog mViewingDialog;
	private ImageView mImagePicture;
	private VideoView mVideoView;
	private GestureDetector mGestureDetector;
	

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
		
		mGestureDetector = new GestureDetector(this, new GestureListener());
	
//		mAdapter = new ArrayAdapter<String>(this,
//    	        android.R.layout.simple_list_item_1, mPieTalkService.getLocalPieUsernames());
//		mAdapter = new ArrayAdapter<String>(this,
//    	        R.layout.list_row_pie, R.id.text1, mPieTalkService.getLocalPieUsernames());		
		
		//mAdapter = new PiesArrayAdapter(this,  mPieTalkService.getLocalPieUsernames());
		mAdapter = new PiesArrayAdapter(this,  mPieTalkService.getLocalPies());
		mLvPies.setAdapter(mAdapter);
			
		mLvPies.setOnItemClickListener(pieClickListener);
		mLvPies.setOnItemLongClickListener(pieLongClickListener);
//		mLvPies.setOnKeyListener(new View.OnKeyListener() {			
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				// TODO Auto-generated method stub
//				PieTalkLogger.i(TAG, "onKey");
//				return false;
//			}
//		});

		
		mLvPies.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
//				PieTalkLogger.i(TAG, "onTouch");
				mGestureDetector.onTouchEvent(event);
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (mIsViewingPicture || mIsViewingVideo) {
						mViewingDialog.dismiss();
						mIsViewingPicture = false;
						mIsViewingVideo = false;
						if (mImagePicture != null) {
							mImagePicture = null;
						} else if (mVideoView != null) {
							mVideoView.stopPlayback();
							mVideoView = null;
						}
						
					}
				}
				//Ensures we can still pull to refresh on this page
				mPullToRefreshAttacher.onTouch(v, event);
				return false;
			}
		});
	}
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		// TODO Auto-generated method stub
//		PieTalkLogger.i(TAG, "onkeydown");
//		return super.onKeyDown(keyCode, event);
//	}
	
	private OnItemClickListener pieClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//PieTalkLogger.i(TAG,  "onItemClick");
			
		}		
	};
	
	private OnItemLongClickListener pieLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			final Pie pie = mPieTalkService.getLocalPies().get(position);
			if (pie.getType().equals("FriendRequest")) {
				//Friend and update the pie
				mPieTalkService.acceptFriendRequest(pie, new ApiOperationCallback<PieTalkResponse>() {					
					@Override
					public void onCompleted(PieTalkResponse response, Exception ex,
							ServiceFilterResponse serviceFilterResponse) {
						PieTalkLogger.i(TAG, "Response received");
						if (ex != null || response.Error != null) {
																	
							//Display error					
							if (ex != null)
								PieTalkAlert.showSimpleErrorDialog(mActivity, ex.getCause().getMessage());
							else 
								Toast.makeText(mActivity, response.Error, Toast.LENGTH_SHORT).show();
						} else {
							mAdapter.remove(pie);
							mPieTalkService.getFriends();
						}
					}
				});
			} else if (pie.getType().equals("Pie")) {
				
				if (pie.getHasUserSeen()) {
					//Do nothing, they should double tap to reply
				} else {
					//Get SAS for pie
					mPieTalkService.getPieForRecipient(pie, new ApiOperationCallback<PieTalkResponse>() {
						@Override
						public void onCompleted(PieTalkResponse response,
								Exception ex, ServiceFilterResponse serviceFilterResponse) {
							if (ex != null || response.Error != null) {
								//Display error								
								if (ex != null) 
									PieTalkAlert.showSimpleErrorDialog(mActivity, ex.getCause().getMessage());
								else
									PieTalkAlert.showSimpleErrorDialog(mActivity, response.Error);																	
							} else {
								PieTalkLogger.d(TAG, response.PieUrl);
								//dislay the pie depending on the type
								mViewingDialog = new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar);
								if (pie.getIsPicture()) {									
									mIsViewingPicture = true;
									if (mImagePicture == null) 
										mImagePicture = new ImageView(mActivity);
									new DownloadPiePictureTask().execute(response.PieUrl);
									
									mViewingDialog.setContentView(mImagePicture);
									
								} else if (pie.getIsVideo()) {
									mIsViewingVideo = true;
									if (mVideoView == null) {
										mVideoView = new VideoView(mActivity);
									}
									RelativeLayout newLayout = new RelativeLayout(mActivity);
//									RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) newLayout.getLayoutParams();
//									layoutParams.width = LayoutParams.MATCH_PARENT;
//									layoutParams.height = LayoutParams.MATCH_PARENT;
									RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//									newLayout.setLayoutParams(layoutParams);
									
									layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
									layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
									layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
									layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
									mVideoView.setLayoutParams(layoutParams);
									
									mVideoView.setOnPreparedListener(new OnPreparedListener() {				
											@Override
											public void onPrepared(MediaPlayer mp) {
												mp.setLooping(true);											
											}
										});
									
									Uri uri = Uri.parse(response.PieUrl);
									mVideoView.setVideoURI(uri);
									mVideoView.start();
									newLayout.addView(mVideoView);
									mViewingDialog.setContentView(newLayout);
								}
								mViewingDialog.show();
								
								//Start countdown
								int position = mPieTalkService.getLocalPies().indexOf(pie);
								View view = mLvPies.getChildAt(position);
								
								final TextView lblTime = (TextView) view.findViewById(R.id.lblTime);
								//Only start a countdown if we haven't already
								if (lblTime.getText().equals("")) {
									final ImageView imgIndicator = (ImageView) view.findViewById(R.id.imgIndicator);
									final TextView lblInstructions = (TextView) view.findViewById(R.id.lblInstructions);
									int timeToLive = pie.getTimeToLive();
									lblTime.setText(timeToLive + "");
									
									new CountDownTimer(timeToLive * 1000, 1000) {
										public void onTick(long millisUntilFinished) {
											lblTime.setText(millisUntilFinished / 1000 + "");
										}
										
										public void onFinish() {
											imgIndicator.setImageResource(R.drawable.pie_seen);
											lblTime.setText(R.string.empty_string);
											lblInstructions.setText(R.string.instructions_seen_pie);
											pie.setHasUserSeen(true);
											if (mViewingDialog.isShowing())
												mViewingDialog.dismiss();
										}
									}.start();
								}
							}
						}						
					});
					
					//Update PIE as being seen at time
					//Show PIE in dialog (require hold down)
					//Start local countdown					
					//Change loal pie when countdown is up
					//Block access on server after time is up
				}
			}
			return false;
		}
		
	};
	
	private class DownloadPiePictureTask extends AsyncTask<String, Void, Bitmap> {
		public DownloadPiePictureTask() { }
		
		@Override
		protected Bitmap doInBackground(String... piePictureUrl) {
			Bitmap pieImage = null;
			try {
				InputStream in = new java.net.URL(piePictureUrl[0]).openStream();
				pieImage = BitmapFactory.decodeStream(in);			
			} catch (Exception ex) {
				PieTalkLogger.e(TAG, "Error pulling down pie for url: " + piePictureUrl[0]);				
			}
			return pieImage;
		}
		
		protected void onPostExecute(Bitmap pieImage) {
			mImagePicture.setImageBitmap(pieImage);
		}
	}
	

	
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		mIsViewingPicture = false;
		mIsViewingVideo = false;
		//filter.addAction(Constants.BROADCAST_PIES_UPDATED);
		filter.addAction(Constants.BROADCAST_PIES_UPDATED);
		filter.addAction(Constants.BROADCAST_PIE_SENT);
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
			if (intent.getAction().equals(Constants.BROADCAST_PIES_UPDATED)) {
				mAdapter.clear();			
				//for (String item : mPieTalkService.getLocalPieUsernames()) {
				for (Pie pie : mPieTalkService.getLocalPies()) {
					mAdapter.add(pie);
				}		
				PieTalkLogger.i(TAG, "Refresh complete");
				mPullToRefreshAttacher.setRefreshComplete();
				mPullToRefreshAttacher.setRefreshing(false);
			} else if (intent.getAction().equals(Constants.BROADCAST_PIE_SENT)) {
				mPieTalkService.getPies();
			}
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
			
			Intent intent = new Intent(mActivity, TestSettingsActivity.class);
			startActivity(intent);
			finish();
			//mPieTalkService.getPies();
			//mPullToRefreshAttacher.setRefreshing(true);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		NavUtils.navigateUpFromSameTask(this);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}
	
	public class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			PieTalkLogger.i(TAG, "DoubleTap");
			return super.onDoubleTap(e);
		}
	}

}
