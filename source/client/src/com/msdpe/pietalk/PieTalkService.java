package com.msdpe.pietalk;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.StatusLine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponseCallback;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;
import com.msdpe.pietalk.activities.SplashScreenActivity;
import com.msdpe.pietalk.datamodels.Friend;
import com.msdpe.pietalk.datamodels.Pie;
import com.msdpe.pietalk.datamodels.PieFile;
import com.msdpe.pietalk.util.PieTalkLogger;
import com.msdpe.pietalk.util.PieTalkRegisterResponse;
import com.msdpe.pietalk.util.PieTalkResponse;

public class PieTalkService {	
	private Context mContext;
	private final String TAG = "PieTalkService";
	private String mUsername;
	private String mEmail;
	public int     mCheckCount;
	private String[] mTempRecipientUserIds;
	private GoogleCloudMessaging mGcm;
	private NotificationHub      mHub;
	private String mRegistrationId;
	
	//Mobile Services objects
	private MobileServiceClient mClient;
	private MobileServiceTable<Friend> mFriendTable;	
	private MobileServiceTable<Pie> mPieTable;
	private MobileServiceTable<PieFile> mPieFileTable;
	
	//Local data
	private List<Friend> mFriends;
	private List<String> mFriendNames;
	private List<Pie> mPies;
	
	public PieTalkService(Context context) {
		mContext = context;
		try {
			mClient = new MobileServiceClient("https://pietalk.azure-mobile.net/",
					//TODO: change key to constant
					//TODO: get rid of service filter for keynote demo					
					"fPcjjyAxIIIGxPSxgzMfIHxOiQIKWA95", mContext)
					.withFilter(new MyServiceFilter());
			
			mFriendTable = mClient.getTable("Friends", Friend.class);
			mPieTable = mClient.getTable("Messages", Pie.class);
			mPieFileTable = mClient.getTable("PieFile", PieFile.class);
			
			mFriends = new ArrayList<Friend>();
			mFriendNames = new ArrayList<String>();
			mPies = new ArrayList<Pie>();
			
			mCheckCount = 0;
		} catch (MalformedURLException e) {
			Log.e(TAG, "There was an error creating the Mobile Service.  Verify the URL");
		}
	}
	
	public void setContext(Context context) {
		mClient.setContext(context);
	}
	
	public String getUserId() {
		return mClient.getCurrentUser().getUserId();
	}
	
	public String getUsername() {
		return mUsername; 
	}
	
	public String getEmail() {
		return mEmail;
	}
	
	public List<Friend> getLocalFriends() {
		return mFriends;
	}
	
	
	public void increaseCheckCount() { mCheckCount++; }
	public void decreaseCheckCount() { mCheckCount--; }
	public int  getCheckCount() { return mCheckCount; }
	
	public void uncheckFriends() {
		mCheckCount = 0;
		for (Friend friend : mFriends) {
			friend.setChecked(false);
		}
	}
	
	public List<Pie> getLocalPies() {
		return mPies;
	}
	
	public List<String> getLocalPieUsernames() {
		List<String> mPieNames = new ArrayList<String>();
		for (int i = 0; i < mPies.size(); i++) {
			mPieNames.add(mPies.get(i).getFromUsername() + ":ttl: " + mPies.get(i).getTimeToLive());
		}
		return mPieNames;
	}
	
	public List<String> getLocalFriendNames() {
//		return mFriendNames;
		mFriendNames = new ArrayList<String>();
		PieTalkLogger.i(TAG, "Processing " + mFriends.size() + " friends");
		for (int i = 0; i < mFriends.size(); i++) {
			mFriendNames.add(mFriends.get(i).getToUsername());
		}
		return mFriendNames;
	}
	
	/**
	 * Checks to see if we have userId and token stored on the device and sets them if so
	 * @return
	 */
	public boolean isUserAuthenticated() {			
		SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
		if (settings != null) {
			String userId = settings.getString("userid", null);
			String token = settings.getString("token", null);
			String username = settings.getString("username", null);
			String email = settings.getString("email", null);
			String registrationId = settings.getString("registrationId", null);
			if (userId != null && !userId.equals("")) {
				setUserData(userId, token, username, email, registrationId);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a nwe MobileServiceUser using a userId and token passed in.
	 * Also sets the current provider
	 * @param userId
	 * @param token
	 */
	public void setUserData(String userId, String token, String username, String email, String registrationId) {
		MobileServiceUser user = new MobileServiceUser(userId);
		user.setAuthenticationToken(token);
		mClient.setCurrentUser(user);		
		mUsername = username;
		mEmail = email;
		mRegistrationId = registrationId;
	}
	
	/***
	 * Pulls the user ID and token out of a json object from the server
	 * @param jsonObject
	 */
	public void setUserAndSaveData(JsonElement jsonData) {
		JsonObject userData = jsonData.getAsJsonObject();
		String userId = userData.get("userId").getAsString();
		String token = userData.get("token").getAsString();
		String email = userData.get("email").getAsString();
		setUserData(userId, token, null, email, null);	
		saveUserData();
	}
	
	public void setUserAndSaveData(PieTalkRegisterResponse registerData) {
		//JsonObject userData = jsonData.getAsJsonObject();
		String userId = registerData.userId;
		String token = registerData.token;		
		String username = registerData.username;
		String email = registerData.email;
		setUserData(userId, token, username, email, null);	
		saveUserData();
	}
	
	/**
	 * Saves userId and token to SharedPreferences.
	 * NOTE:  This is not secure and is just used as a storage mechanism.  In reality, you would want to 
	 * come up with a more secure way of storing this information.
	 */
	public void saveUserData() {
		SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.putString("userid", mClient.getCurrentUser().getUserId());
        preferencesEditor.putString("token", mClient.getCurrentUser().getAuthenticationToken());
        preferencesEditor.putString("username", mUsername);
        preferencesEditor.putString("email", mEmail);
        preferencesEditor.commit();
	}
	
	/**
	 * Saves username SharedPreferences.
	 * NOTE:  This is not secure and is just used as a storage mechanism.  In reality, you would want to 
	 * come up with a more secure way of storing this information.
	 */
	public void saveUsername(String username) {
		mUsername = username;
		SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.putString("username", username);        
        preferencesEditor.commit();
	}
	
	/**
	 * Register the user if they're creating a custom auth account
	 * @param password
	 * @param email
	 * @param dob
	 * @param callback
	 */
	public void registerUser(String password, String dob,
			String email,
			//ApiJsonOperationCallback callback) {
			ApiOperationCallback<PieTalkRegisterResponse> callback) {
		JsonObject newUser = new JsonObject();
		newUser.addProperty("password", password);
		newUser.addProperty("email", email);
		newUser.addProperty("dob", dob);			
		mClient.invokeApi("Register", newUser, PieTalkRegisterResponse.class, callback);
	}
	
	public void loginUser(String emailOrUsername, String password, ApiOperationCallback<PieTalkRegisterResponse> callback) {
		JsonObject user = new JsonObject();
		user.addProperty("emailOrUsername", emailOrUsername);
		user.addProperty("password", password);
		mClient.invokeApi("Login", user, PieTalkRegisterResponse.class, callback);
	}
	
	public void saveUsername(String username, ApiOperationCallback<PieTalkResponse> callback) {
		JsonObject user = new JsonObject();
		user.addProperty("username", username);
		//mClient.invokeApi("SaveUsername", user, callback);
		
		mClient.invokeApi("SaveUsername", user, PieTalkResponse.class, callback);
	}
	
	public Activity getActivityContext() {
		return (Activity) mClient.getContext();
	}
	
	public void logout(boolean shouldRedirectToLogin) {
		//Clear the cookies so they won't auto login to a provider again
		CookieSyncManager.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();			
		//Clear the user id and token from the shared preferences
		SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.clear();
        preferencesEditor.commit();						
		mClient.logout();			
		//Take the user back to the splash screen activity to relogin if requested
		if (shouldRedirectToLogin) {
			Intent logoutIntent = new Intent(mContext, SplashScreenActivity.class);
			logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(logoutIntent);		
		}
	}
	
	public void requestFriend(String username, ApiOperationCallback<PieTalkRegisterResponse> callback) {
		JsonObject friendRequest = new JsonObject();	
		friendRequest.addProperty("username", username);
		mClient.invokeApi("RequestFriend", friendRequest, PieTalkRegisterResponse.class, callback);
	}
	
	public void getFriends() {
		mFriendTable.where().execute(new TableQueryCallback<Friend>() {			
			@Override
			public void onCompleted(List<Friend> results, int count, Exception ex,
					ServiceFilterResponse response) {
				if (ex != null) {
					Toast.makeText(mContext, "Error getting friends", Toast.LENGTH_SHORT).show();
					PieTalkLogger.e(TAG, "Error getting friends: " + ex.getCause().getMessage());
				} else {
					PieTalkLogger.i(TAG, "Friends received");
					mFriends = results;
					//Loop through and pull out names
					//TODO: remove this when we switch to custom adapter
//					mFriendNames.clear();
//					PieTalkLogger.i(TAG, "Processing " + results.size() + " friends");
//					for (int i = 0; i < results.size(); i++) {
//						mFriendNames.add(results.get(i).getToUsername());
//					}
					PieTalkLogger.i(TAG, "Sending broadcast");
					//Insert self as friend
					Friend self = Friend.getSelfFriend(mUsername, mClient.getCurrentUser().getUserId());
					mFriends.add(0, self);
					
					//Broadcast that we've updated our friends list
					Intent broadcast = new Intent();
					broadcast.setAction(Constants.BROADCAST_FRIENDS_UPDATED);
					mContext.sendBroadcast(broadcast);					
				}				
			}
		});
	}
	
	public void getPies() {
		PieTalkLogger.i(TAG, "Getting pies from server");
		mPieTable.where().execute(new TableQueryCallback<Pie>() {			
			@Override
			public void onCompleted(List<Pie> results, int count, Exception ex,
					ServiceFilterResponse response) {
				if (ex != null) {
					Toast.makeText(mContext, "Error getting pies", Toast.LENGTH_SHORT).show();
					PieTalkLogger.e(TAG, "Error getting pies: " + ex.getCause().getMessage());
				} else {
					PieTalkLogger.i(TAG, "Pies received");
					mPies = results;
					
					PieTalkLogger.i(TAG, "Sending broadcast");
					//Broadcast that we've updated our pies list
					Intent broadcast = new Intent();
					broadcast.setAction(Constants.BROADCAST_PIES_UPDATED);
					mContext.sendBroadcast(broadcast);					
				}				
			}
		});
	}
	
	public void acceptFriendRequest(Pie friendRequestPie, 
			ApiOperationCallback<PieTalkResponse> callback) {		
		mClient.invokeApi("AcceptFriendRequest", friendRequestPie, PieTalkResponse.class, callback);
	}
	
	public void sendPie(final String fileFullPath, final boolean isPicture, final boolean isVideo, int selectedSeconds) {
		//Get User IDs
		mTempRecipientUserIds = new String[mCheckCount];		
		int count = 0;
		for (Friend friend : mFriends) {
			if (friend.getChecked())
				mTempRecipientUserIds[count++] = friend.getToUserId();
		}
		//add new message to local pies
		final Pie sentPie = Pie.newSentPie(mClient.getCurrentUser().getUserId(), mUsername, selectedSeconds, isPicture, isVideo);
		mPies.add(0, sentPie);
		//save new pie as from
		mPieTable.insert(sentPie, new TableOperationCallback<Pie>() {			
			@Override
			public void onCompleted(final Pie pieReturned, Exception ex, ServiceFilterResponse serviceFilterResponse) {
				if (ex != null) {
					PieTalkLogger.e(TAG, "Error inserting pie: " + ex.getMessage());
				}
				if (pieReturned == null) {
					PieTalkLogger.i(TAG, "Pie returned is null");
				} else
					PieTalkLogger.i(TAG, "PIe returned ID: " + pieReturned.getId());
				//update local pie
				//mPies.set(mPies.indexOf(sentPie), pieReturned);
				//Todo: check to make sure this works right.  was looking (using indexOf) for the
				//sentPie object but that occasionally returned a -1 for index
				//Now just assuming we're still dealing with entry 0
				mPies.set(0, pieReturned);
				//Callback from saving new pie
				//save file and get sas
				PieFile pieFile = new PieFile(isPicture, isVideo, mUsername, pieReturned.getId(), fileFullPath);
				mPieFileTable.insert(pieFile, new TableOperationCallback<PieFile>() {					
					@Override
					public void onCompleted(PieFile pieFileReturned, Exception ex,
							ServiceFilterResponse serviceFilterResponse) {
						//callback:  upload file
						(new BlobUploaderTask(pieFileReturned.getBlobPath(), 
								fileFullPath, isPicture, isVideo, 
								mTempRecipientUserIds, pieReturned, pieFileReturned)).execute();
					}
				});
			}
		});	
	}
	
	private class MyServiceFilter implements ServiceFilter {		
		@Override
		public void handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback,
				final ServiceFilterResponseCallback responseCallback) {				
			nextServiceFilterCallback.onNext(request, new ServiceFilterResponseCallback() {				
				@Override
				public void onResponse(ServiceFilterResponse response, Exception ex) {
					if (ex == null) { 
						StatusLine status = response.getStatus();
						int statusCode = status.getStatusCode();						
						if (statusCode == 401) {
							//Kick user out 
							PieTalkLogger.i(TAG, "401 received, forcing logout");
							//TODO force logout
						}
					} else {
						if (ex.getCause() != null)
							PieTalkLogger.e(TAG, "Error in handle request: " + ex.getCause().getMessage());
						else
							PieTalkLogger.e(TAG, "Error in handle request: " + ex.getMessage());
					}
					
					if (responseCallback != null)  responseCallback.onResponse(response, ex);
				}
			});
		}
	}

	/***
 	 * Handles uploading a blob to a specified url
 	 */
 	class BlobUploaderTask extends AsyncTask<Void, Void, Boolean> {
	    private String mBlobUrl;
	    private String mFilePath;
	    private boolean mIsPicture, mIsVideo;
	    private String[] mRecipientUserIds;
	    private Pie mPie;
	    private PieFile mPieFile;
	    public BlobUploaderTask(String blobUrl, String filePath, boolean isPicture, boolean isVideo,
	    							String[] recipientUserIds, Pie pieReturned, PieFile pieFileReturned) {
	    		mBlobUrl = blobUrl;
	    		mFilePath = filePath;
	    		mIsPicture = isPicture;
	    		mIsVideo = isVideo;
	    		mRecipientUserIds = recipientUserIds;
	    		mPie = pieReturned;
	    		mPieFile = pieFileReturned;
	    }

	    @Override
	    protected Boolean doInBackground(Void... params) {	         
		    	try {
		    		//Get the image data
				FileInputStream fis = new FileInputStream(mFilePath);
				int bytesRead = 0;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] b = new byte[1024];
				while ((bytesRead = fis.read(b)) != -1) {
					bos.write(b, 0, bytesRead);
				}
				byte[] bytes = bos.toByteArray();
				// Post our image data (byte array) to the server
				URL url = new URL(mBlobUrl.replace("\"", ""));
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod("PUT");
				if (mIsPicture)
					urlConnection.addRequestProperty("Content-Type", "image/jpeg");
				else if (mIsVideo)
					urlConnection.addRequestProperty("Content-Type", "video/mp4");
				urlConnection.setRequestProperty("Content-Length", ""+ bytes.length);
				// Write image data to server
				DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
				wr.write(bytes);
				wr.flush();
				wr.close();
				int response = urlConnection.getResponseCode();
				//If we successfully uploaded, return true
				if (response == 201
						&& urlConnection.getResponseMessage().equals("Created")) {
					return true;
				}
		    	} catch (Exception ex) {
		    		Log.e(TAG, ex.getMessage());
		    	}
	        return false;	    	
	    }

	    @Override
	    protected void onPostExecute(Boolean uploaded) {
	        if (uploaded) {
	        		PieTalkLogger.i(TAG, "Upload successful");	   									
        			//delete local file
	        		File file = new File(mFilePath);
	        		if (!file.delete()) {
	        			PieTalkLogger.e(TAG, "Unable to delete file");
	        		}
				//callback: send messages to each recipient user id
	        		sendPiesToRecipients(mPie, mRecipientUserIds, mPieFile);
	        }
	    }
 	}
 	
 	public void sendPiesToRecipients(Pie sentPie, String[] recipientUserIds,
 			PieFile savedPieFile) {
		JsonObject sendPiesRequest = new JsonObject();
		String serializedRecipients = new Gson().toJson(recipientUserIds);
		PieTalkLogger.d(TAG, "Recipients: " + serializedRecipients);
		if (recipientUserIds.length == 0) {
			PieTalkLogger.e(TAG, "There are no recipient user ids.  INVESTIGATE!");
		}
		
		sendPiesRequest.add("recipients", new JsonPrimitive(serializedRecipients));
		sendPiesRequest.addProperty("timeToLive", sentPie.getTimeToLive());
		sendPiesRequest.addProperty("fromUserId", sentPie.getFromUserId());
		sendPiesRequest.addProperty("fromUsername", sentPie.getFromUsername());
		sendPiesRequest.addProperty("isPicture", sentPie.getIsPicture());
		sendPiesRequest.addProperty("isVideo", sentPie.getIsVideo());
		sendPiesRequest.addProperty("originalSentPieId", sentPie.getId());
		sendPiesRequest.addProperty("pieFileId", savedPieFile.getId());
		mClient.invokeApi("SendPiesToFriends", sendPiesRequest, PieTalkResponse.class, new ApiOperationCallback<PieTalkResponse>() {
			@Override
			public void onCompleted(PieTalkResponse response, Exception ex,
					ServiceFilterResponse serviceFilterResponse) {
				//callback: broadcast to receiver that messages sent
				Intent broadcast = new Intent();
				broadcast.setAction(Constants.BROADCAST_PIE_SENT);
				
				if (ex != null || response.Error != null) {										
					//Display error					
					if (ex != null)
						PieTalkLogger.e(TAG, "Unexpected error sending pies: " + ex.getCause().getMessage());
					else 
						PieTalkLogger.e(TAG,  "Error sending pies: " + response.Error);
					broadcast.putExtra("Success", false);
				} else {
					broadcast.putExtra("Success", true);
				}
				mContext.sendBroadcast(broadcast);		
			}
		});
	}
 	
 	public void getPieForRecipient(Pie pie, ApiOperationCallback<PieTalkResponse> callback) {
 		mClient.invokeApi("getPieForRecipient", pie, PieTalkResponse.class, callback);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void registerForPush() {
 		mGcm = GoogleCloudMessaging.getInstance(mContext);
 		mHub = new NotificationHub(Constants.NOTIFICATIN_HUB_NAME, Constants.NOTIFICATION_HUB_CONNECTION_STRING, mContext);
 		new AsyncTask() {
 		      @Override
 		      protected Object doInBackground(Object... params) {
 		         try {
 		        	 	PieTalkLogger.i(TAG, "Registering for push notifications");
 		            String regId = mGcm.register(Constants.SENDER_ID);
 		            PieTalkLogger.i(TAG, "Registration ID: " + regId);
 		            if (!regId.equals(mRegistrationId)) {
 		            		PieTalkLogger.i(TAG, "Registerin with NotHubs");
 		            		mRegistrationId = regId;
 		            		SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
 		            		SharedPreferences.Editor preferencesEditor = settings.edit();
 		            		preferencesEditor.putString("registrationId", mRegistrationId);        
 		            		preferencesEditor.commit();
 		            	
 		            		mHub.registerTemplate(mRegistrationId, "alertTemplate", "{\"data\":{\"message\":\"$(message)\"}}", mClient.getCurrentUser().getUserId(), "AllUsers", "AndroidUser");
 		            }
 		         } catch (Exception e) {
 		        	 	PieTalkLogger.e(TAG, "Unable to register for push notifications: " + e.getMessage());
 		            return e;
 		         }
 		         return null;
 		     }
 		   }.execute(null, null, null);
 	}
}
