package com.msdpe.pietalk;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.StatusLine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponseCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;
import com.msdpe.pietalk.activities.SplashScreenActivity;
import com.msdpe.pietalk.datamodels.Friend;
import com.msdpe.pietalk.util.PieTalkLogger;
import com.msdpe.pietalk.util.PieTalkRegisterResponse;
import com.msdpe.pietalk.util.PieTalkResponse;

public class PieTalkService {	
	private Context mContext;
	private final String TAG = "PieTalkService";
	private String mUsername;
	
	//Mobile Services objects
	private MobileServiceClient mClient;
	private MobileServiceTable<Friend> mFriendTable;	
	
	//Local data
	private List<Friend> mFriends;
	private List<String> mFriendNames;
	
	
	public PieTalkService(Context context) {
		mContext = context;
		try {
			mClient = new MobileServiceClient("https://pietalk.azure-mobile.net/", 
					"fPcjjyAxIIIGxPSxgzMfIHxOiQIKWA95", mContext)
					.withFilter(new MyServiceFilter());
			
			mFriendTable = mClient.getTable("Friends", Friend.class);
			
			mFriends = new ArrayList<Friend>();
			mFriendNames = new ArrayList<String>();
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
	
	public List<Friend> getLocalFriends() {
		return mFriends;
	}
	
	public List<String> getLocalFriendNames() {
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
			if (userId != null && !userId.equals("")) {
				setUserData(userId, token, username);
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
	public void setUserData(String userId, String token, String username) {
		MobileServiceUser user = new MobileServiceUser(userId);
		user.setAuthenticationToken(token);
		mClient.setCurrentUser(user);		
		mUsername = username;
	}
	
	/***
	 * Pulls the user ID and token out of a json object from the server
	 * @param jsonObject
	 */
	public void setUserAndSaveData(JsonElement jsonData) {
		JsonObject userData = jsonData.getAsJsonObject();
		String userId = userData.get("userId").getAsString();
		String token = userData.get("token").getAsString();			
		setUserData(userId, token, null);	
		saveUserData();
	}
	
	public void setUserAndSaveData(PieTalkRegisterResponse registerData) {
		//JsonObject userData = jsonData.getAsJsonObject();
		String userId = registerData.userId;
		String token = registerData.token;			
		setUserData(userId, token, null);	
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
					for (int i = 0; i < results.size(); i++) {
						mFriendNames.add(results.get(i).getToUsername());
					}
					PieTalkLogger.i(TAG, "Sending broadcast");
					//Broadcast that we've updated our friends list
					Intent broadcast = new Intent();
					broadcast.setAction(Constants.BROADCAST_FRIENDS_UPDATED);
					mContext.sendBroadcast(broadcast);					
				}				
			}
		});
	}
	
	private class MyServiceFilter implements ServiceFilter {		
		@Override
		public void handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback,
				final ServiceFilterResponseCallback responseCallback) {				
			nextServiceFilterCallback.onNext(request, new ServiceFilterResponseCallback() {				
				@Override
				public void onResponse(ServiceFilterResponse response, Exception exception) {
					StatusLine status = response.getStatus();
					int statusCode = status.getStatusCode();						
					if (statusCode == 401) {
						//Kick user out 
						PieTalkLogger.i(TAG, "401 received, forcing logout");
					}
					
					if (responseCallback != null)  responseCallback.onResponse(response, exception);
				}
			});
		}
	}
}
