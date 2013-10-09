package com.msdpe.pietalk.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
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
import android.widget.VideoView;

import com.msdpe.pietalk.CameraPreview;
import com.msdpe.pietalk.Constants;
import com.msdpe.pietalk.PreferencesHandler;
import com.msdpe.pietalk.R;
import com.msdpe.pietalk.base.BaseActivity;
import com.msdpe.pietalk.util.PieTalkLogger;

public class RecordActivity extends BaseActivity {
	
	private final String TAG = "RecordActivity";
	private Camera mCamera;
	private CameraPreview mCameraPreview;
	private VideoView mVideoView;
	private MediaRecorder mMediaRecorder;
	private ImageButton mBtnSwitchCamera;
	private ImageButton mBtnFlash;
	private ImageButton mBtnTakePicture;
	private ImageButton mBtnPies;
	private ImageButton mBtnFriends;
	private int mCameraNumber;
	private boolean mFlashOn;
	private boolean mTakingVideo;
	private boolean mReviewingPicture;
	private boolean mReviewingVideo;
	private String  mVideoFileName;
	private File mMediaStorageDir;
	private FrameLayout mFrameLayout;

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
		mBtnPies = (ImageButton) findViewById(R.id.btnPies);
		mBtnFriends = (ImageButton) findViewById(R.id.btnFriends);
		mVideoView = (VideoView) findViewById(R.id.videoView);
		
		mBtnTakePicture.setOnClickListener(takePictureListener);
		mBtnTakePicture.setOnLongClickListener(takeVideoListener);
		mBtnTakePicture.setOnTouchListener(touchListener);
		
		mPieTalkService.getFriends();
		mPieTalkService.getPies();
		
		mTakingVideo = false;
		mReviewingPicture = false;
		mReviewingVideo = false;
	}
	
	private OnClickListener takePictureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			PieTalkLogger.i(TAG, "TakePic");
			mCamera.takePicture(null, null, mPictureCallback);
			//Hide UI
			setUIMode(Constants.CameraUIMode.UI_MODE_TAKING_PICTURE);
		}	
	};
	
	
	
	private void setUIMode(Constants.CameraUIMode uiMode) {
		switch (uiMode) {
		case UI_MODE_PRE_PICTURE:
			mBtnFlash.setVisibility(View.VISIBLE);
			mBtnFriends.setVisibility(View.VISIBLE);
			mBtnPies.setVisibility(View.VISIBLE);
			mBtnSwitchCamera.setVisibility(View.VISIBLE);
			mBtnTakePicture.setVisibility(View.VISIBLE);
			break;
		case UI_MODE_REVIEW_PICTURE:
			mBtnFlash.setVisibility(View.GONE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPies.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);
			mBtnTakePicture.setVisibility(View.GONE);
			break;
		case UI_MODE_REVIEW_VIDEO:
			mBtnFlash.setVisibility(View.GONE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPies.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);
			mBtnTakePicture.setVisibility(View.GONE);
			
			if (mVideoView == null)
				mVideoView = new VideoView(this);
			
			
		
			
			mVideoView.setVideoPath(mMediaStorageDir.getPath() + File.separator +
			        mVideoFileName);
			
			
			mCameraPreview.setVisibility(View.GONE);
			//mFrameLayout.removeAllViews();
			//Todo add this back in when necessary
//			mFrameLayout.addView(mVideoView);
//			DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
//		    android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) mVideoView.getLayoutParams();
//		    params.width =  metrics.widthPixels;
//		    params.height = metrics.heightPixels;
//		    params.rightMargin = 00;
		    //params.gravity = Gravity.CENTER;
//		    mVideoView.setLayoutParams(params);
		    
		    mVideoView.setOnPreparedListener(new OnPreparedListener() {				
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);
					
					
//					Display display = getWindowManager().getDefaultDisplay(); 
//				    int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(display.getWidth(),
//				            MeasureSpec.UNSPECIFIED);
//				    int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(display.getHeight(),
//				            MeasureSpec.UNSPECIFIED);
//				    mVideoView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
//					
//					int videoWidth = mp.getVideoWidth();
//				    int videoHeight = mp.getVideoHeight();
//				    float videoProportion = (float) videoWidth / (float) videoHeight;       
//				    int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//				    int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
//				    float screenProportion = (float) screenWidth / (float) screenHeight;
//				    android.view.ViewGroup.LayoutParams lp = mFrameLayout.getLayoutParams();
//
//				    if (videoProportion > screenProportion) {
//				        lp.width = screenWidth;
//				        lp.height = (int) ((float) screenWidth / videoProportion);
//				    } else {
//				        lp.width = (int) (videoProportion * (float) screenHeight);
//				        lp.height = screenHeight;
//				    }
//				    mFrameLayout.setLayoutParams(lp);
					
					
					
					
					
					
				}
			});
		    
		    
			mVideoView.start();
			
			
			break;
		case UI_MODE_TAKING_PICTURE:
			mBtnFlash.setVisibility(View.GONE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPies.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);
			mBtnTakePicture.setVisibility(View.GONE);
			break;
		case UI_MODE_TAKING_VIDEO:
			mBtnFlash.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);			
			break;
		}
	}
	
	private OnLongClickListener takeVideoListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			PieTalkLogger.i(TAG, "Video start");			
			mTakingVideo = true;
			if (prepareVideoRecorder()) {
				
				mMediaRecorder.start();
				setUIMode(Constants.CameraUIMode.UI_MODE_TAKING_VIDEO);
			} else {
				//TODO: show an error to the user
				releaseMediaRecorder();
			}
			return true;
		}		
	};
	
	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			
		}
	}
	
	private boolean prepareVideoRecorder() {
		
		mMediaRecorder = new MediaRecorder();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		
		mMediaRecorder.setProfile(CamcorderProfile.get(mCameraNumber, CamcorderProfile.QUALITY_HIGH));
		mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
		mMediaRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());
		mMediaRecorder.setOrientationHint(90);
		mMediaRecorder.setVideoSize(720,480);
//		mMediaRecorder.setVideoSize(480, 720);
//		mMediaRecorder.setVideoSize(1184,768);
//		mMediaRecorder.setVideoSize(768, 1184);
		
		
		
		
		DisplayMetrics metrics = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		PieTalkLogger.d(TAG, "Width: " + metrics.widthPixels);
		PieTalkLogger.d(TAG, "Height: " + metrics.heightPixels);
//	    params.width =  metrics.widthPixels;
//	    params.height = metrics.heightPixels;
		
		mMediaRecorder.setOnErrorListener(new OnErrorListener() {							
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				PieTalkLogger.e(TAG, "MediaRecorder error");
				
			}
		});
		mMediaRecorder.setOnInfoListener(new OnInfoListener() {
			
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				PieTalkLogger.i(TAG, "MediaRecorer info");
			}
		});
		
		
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException ex) {
			PieTalkLogger.e(TAG, "IllegalStateException preparing MediaRecorder: " + ex.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException ex) {
			PieTalkLogger.e(TAG, "IOException preparing MediaRecorder: " + ex.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}
	
	private OnTouchListener touchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
	            //PieTalkLogger.i(TAG, "Down");
	        } else if (event.getAction() == MotionEvent.ACTION_UP) {
	        		
	        		if (mTakingVideo) {
	        			PieTalkLogger.i(TAG, "Finished video");
	        			mTakingVideo = false;
	        			mReviewingVideo = true;
	        			mMediaRecorder.stop();
	        			releaseMediaRecorder();
	        			
	        			setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_VIDEO);
	        		}
	        }
			return false;
		}		
	};
	
	private PictureCallback mPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			PieTalkLogger.i(TAG, "pic taken");
			if (mTakingVideo) {
				mReviewingPicture = true;
				setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_VIDEO);
			}
			else {
				mReviewingVideo = true;
				setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_PICTURE);
			}
		}
	};
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		int numberOfCams = Camera.getNumberOfCameras();
		mCameraNumber = 0;		
		if (numberOfCams <2 )
			mBtnSwitchCamera.setVisibility(View.GONE);
		else 
			mCameraNumber = PreferencesHandler.GetCameraPreference(getApplicationContext());
		
		
		
		mCamera = getCameraInstance(mCameraNumber);
		mCameraPreview = new CameraPreview(this, mCamera);
		mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
		//mFrameLayout.removeAllViews();
		mFrameLayout.addView(mCameraPreview);
		
		mFlashOn = PreferencesHandler.GetFlashPreference(getApplicationContext());
		Camera.Parameters params = mCamera.getParameters();
		List<String> flashModes = params.getSupportedFlashModes();
		
		
//		List<Size> sizes = params.getSupportedVideoSizes();
//		for (Size size : sizes) {
//			PieTalkLogger.i(TAG, "Wid: " + size.width + "  Hei: " + size.height);
//		}
		
		if (mFlashOn)
			mBtnFlash.setImageResource(R.drawable.device_access_flash_on);
			//mBtnFlash.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_access_flash_on));
		else
			mBtnFlash.setImageResource(R.drawable.device_access_flash_off);
			//mBtnFlash.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_access_flash_off));
		if (flashModes == null || flashModes.size() == 0) {
			mBtnFlash.setVisibility(View.GONE);
		} else {
			setCameraFlash(params);
		}
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
		
		Camera.Parameters params = mCamera.getParameters();
		setCameraFlash(params);
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
		
		Camera.Parameters params = mCamera.getParameters();
		List<String> flashModes = params.getSupportedFlashModes();
		if (flashModes == null || flashModes.size() == 0) {
			mBtnFlash.setVisibility(View.GONE);
		} else {
			mBtnFlash.setVisibility(View.VISIBLE);
			setCameraFlash(params);
		}
	}
	
	private void setCameraFlash(Camera.Parameters parameters) {
		if (mFlashOn) {
			parameters.setFlashMode(parameters.FLASH_MODE_ON);
			mCamera.setParameters(parameters);
		} else {
			parameters.setFlashMode(parameters.FLASH_MODE_OFF);
			mCamera.setParameters(parameters);
		}			
	}
	
	public void tappedPies(View view) {
		startActivity(new Intent(getApplicationContext(), PiesListActivity.class));		
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}
	
	public void tappedFriendsList(View view) {
		startActivity(new Intent(getApplicationContext(), FriendsListActivity.class));		
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}
	
	@Override
	public void onBackPressed() {
		if (mReviewingPicture || mReviewingVideo) {
			mCamera.startPreview();
			mReviewingPicture = false;
			mReviewingVideo = false;
			setUIMode(Constants.CameraUIMode.UI_MODE_PRE_PICTURE);
			
		} else {
			finish();
		}
	}
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a file Uri for saving an image or video */
	private  Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private  File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		
	    mMediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), getResources().getString(R.string.app_name));
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mMediaStorageDir.exists()){
	        if (! mMediaStorageDir.mkdirs()){
	            PieTalkLogger.d(TAG, "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	    		String imageFileName = "IMG_"+ timeStamp + ".jpg";
	        mediaFile = new File(mMediaStorageDir.getPath() + File.separator +
	        imageFileName);
	    } else if(type == MEDIA_TYPE_VIDEO) {
	    		mVideoFileName = "VID_"+ timeStamp + ".mp4";
	        mediaFile = new File(mMediaStorageDir.getPath() + File.separator +
	        mVideoFileName);
	        
	    } else {
	        return null;
	    }

	    return mediaFile;
	}

}
