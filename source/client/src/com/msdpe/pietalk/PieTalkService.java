package com.msdpe.pietalk;

import java.net.MalformedURLException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponseCallback;
import com.msdpe.pietalk.util.PieTalkRegisterResponse;
import com.msdpe.pietalk.util.PieTalkResponse;

public class PieTalkService {
	private MobileServiceClient mClient;
	private Context mContext;
	private final String TAG = "PieTalkService";
	private String mUsername;
	
	
	public PieTalkService(Context context) {
		mContext = context;
		try {
			mClient = new MobileServiceClient("https://pietalk.azure-mobile.net/", 
					"fPcjjyAxIIIGxPSxgzMfIHxOiQIKWA95", mContext)
					.withFilter(new MyServiceFilter());
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
	
	public void saveUsername(String username, ApiOperationCallback<PieTalkResponse> callback) {
		JsonObject user = new JsonObject();
		user.addProperty("username", username);
		//mClient.invokeApi("SaveUsername", user, callback);
		
		mClient.invokeApi("SaveUsername", user, PieTalkResponse.class, callback);
	}
	
	
	private class MyServiceFilter implements ServiceFilter {		
		@Override
		public void handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback,
				final ServiceFilterResponseCallback responseCallback) {				
			nextServiceFilterCallback.onNext(request, new ServiceFilterResponseCallback() {				
				@Override
				public void onResponse(ServiceFilterResponse response, Exception exception) {
					if (responseCallback != null)  responseCallback.onResponse(response, exception);
				}
			});
		}
	}
}
