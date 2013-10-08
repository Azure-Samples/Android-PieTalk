package com.msdpe.pietalk.activities;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.msdpe.pietalk.CameraPreview;
import com.msdpe.pietalk.PreferencesHandler;
import com.msdpe.pietalk.R;
import com.msdpe.pietalk.R.anim;
import com.msdpe.pietalk.R.drawable;
import com.msdpe.pietalk.R.id;
import com.msdpe.pietalk.R.layout;
import com.msdpe.pietalk.base.BaseActivity;
import com.msdpe.pietalk.util.PieTalkLogger;

public class RecordActivity extends BaseActivity {
	
	private final String TAG = "RecordActivity";
	private Camera mCamera;
	private CameraPreview mCameraPreview;
	private ImageButton mBtnSwitchCamera;
	private ImageButton mBtnFlash;
	private ImageButton mBtnTakePicture;
	private int mCameraNumber;
	private boolean mFlashOn;
	private boolean mTakingVideo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_record);
		
		mBtnSwitchCamera = (ImageButton) findViewById(R.id.btnSwitchCameras);
		mBtnFlash = (ImageButton) findViewById(R.id.btnFlash);
		mBtnTakePicture = (ImageButton) findViewById(R.id.btnTakePicture);
		
		mBtnTakePicture.setOnClickListener(takePictureListener);
		mBtnTakePicture.setOnLongClickListener(takeVideoListener);
		mBtnTakePicture.setOnTouchListener(touchListener);
		
		mPieTalkService.getFriends();
		mPieTalkService.getPies();
		
		mTakingVideo = false;
	}
	
	private OnClickListener takePictureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			PieTalkLogger.i(TAG, "TakePic");
		}	
	};
	
	private OnLongClickListener takeVideoListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			PieTalkLogger.i(TAG, "Video start");
			mTakingVideo = true;
			return true;
		}		
	};
	
	private OnTouchListener touchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
	            //PieTalkLogger.i(TAG, "Down");
	        } else if (event.getAction() == MotionEvent.ACTION_UP) {
	        		
	        		if (mTakingVideo) {
	        			PieTalkLogger.i(TAG, "Finished video");
	        			mTakingVideo = false;
	        		}
	        }
			return false;
		}
		
	};
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		int numberOfCams = Camera.getNumberOfCameras();
		mCameraNumber = 0;
		mFlashOn = PreferencesHandler.GetFlashPreference(getApplicationContext());
		if (mFlashOn)
			mBtnFlash.setImageResource(R.drawable.device_access_flash_on);
			//mBtnFlash.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_access_flash_on));
		else
			mBtnFlash.setImageResource(R.drawable.device_access_flash_off);
			//mBtnFlash.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_access_flash_off));
		if (numberOfCams <2 )
			mBtnSwitchCamera.setVisibility(View.GONE);
		else 
			mCameraNumber = PreferencesHandler.GetCameraPreference(getApplicationContext());
		
		
		
		mCamera = getCameraInstance(mCameraNumber);
		mCameraPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.removeAllViews();
		preview.addView(mCameraPreview);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mCamera.release();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.record, menu);
		return true;
	}
	
	public Camera getCameraInstance(int cameraNumber) {
		Camera c = null;
		try { 
			c = Camera.open(cameraNumber);
		} catch (Exception ex) {
			PieTalkLogger.e(TAG, ex.getMessage());
		}
		return c;
	}
	
	public void tappedFlash(View view) {
		mFlashOn = !mFlashOn;
		PreferencesHandler.SaveFlashPreference(getApplicationContext(), mFlashOn);
		
		if (mFlashOn)
			mBtnFlash.setImageResource(R.drawable.device_access_flash_on);
			//mBtnFlash.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_access_flash_on));			
		else
			mBtnFlash.setImageResource(R.drawable.device_access_flash_off);
			//mBtnFlash.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_access_flash_off));
	}
	
	public void tappedSwitchCamera(View view) {
		mCamera.stopPreview();
		mCamera.release();

		if (mCameraNumber == 0)
			mCameraNumber = 1;
		else
			mCameraNumber = 0;
		mCamera = getCameraInstance(mCameraNumber);
		PreferencesHandler.SaveCameraPreference(getApplicationContext(), mCameraNumber);
		mCameraPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.removeAllViews();
		preview.addView(mCameraPreview);
	}
	
	public void tappedPies(View view) {
		startActivity(new Intent(getApplicationContext(), PiesListActivity.class));		
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}
	
	public void tappedFriendsList(View view) {
		startActivity(new Intent(getApplicationContext(), FriendsListActivity.class));		
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}

}
