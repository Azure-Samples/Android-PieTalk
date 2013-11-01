package com.msdpe.pietalk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.msdpe.pietalk.datamodels.UserPreferences;
import com.msdpe.pietalk.util.NoNetworkConnectivityException;
import com.msdpe.pietalk.util.PieTalkAlert;
import com.msdpe.pietalk.util.PieTalkLogger;

public class TestSettingsActivity extends Activity {
	private final String TAG = "TestSettingsActivity";
	private SettingsFragment mSettingsFragment;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_test_settings);
		// Show the Up button in the action bar.
		setupActionBar();
		mSettingsFragment = new SettingsFragment();
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, mSettingsFragment)
		.commit();
	}	
	
	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
		private final String TAG = "TestSettingsActivity";
		private PieTalkService mPieTalkService;
		private boolean mIsResettingValue;
		
		@Override
		public void onResume() {
			super.onResume();
			PieTalkLogger.d(TAG, "settings fragment on resume");
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void onPause() {
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}
		 
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			mIsResettingValue = false;
			addPreferencesFromResource(R.xml.preferences);
			PieTalkApplication app = (PieTalkApplication) getActivity().getApplication();
			mPieTalkService = app.getPieTalkService();
			
			//emailPref.setText(mPieTalkService.getEmail());
			//PieTalkLogger.i(TAG, "Setting email in preferences");
			
			initializeToDefaults();
//			emailPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//				
//				@Override
//				public boolean onPreferenceChange(Preference preference, Object newValue) {
//					// TODO Auto-generated method stub
//					PieTalkLogger.i(TAG, "pref changed: " + newValue);
//					return true;
//				}
//			});
			
			Preference logoutPref = (Preference) findPreference(getString(R.string.log_out));
			logoutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					mPieTalkService.logout(true);
					return true;
				}
			});
			
		}

		@Override
		public void onSharedPreferenceChanged(
				final SharedPreferences sharedPreferences, final String key) {
			PieTalkLogger.d(TAG, "Preference changed for key: " + key);
			if (mIsResettingValue) {
				mIsResettingValue = false;
				return;
			}
			
			final Preference myPref = findPreference(key);
			
			
			final Resources resources = getActivity().getResources();
			final UserPreferences localPreferences = mPieTalkService.getLocalPreferences();
			//final String oldValue= "";
			String oldValue = "";
			String newValue = "";
			int preferenceId = 0;
			
			if (key == resources.getString(R.string.email_address)) {
				preferenceId = R.string.email_address;
				oldValue = localPreferences.getEmail();
				newValue = sharedPreferences.getString(key, "");
				if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newValue).matches()) {
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(key, oldValue);
					PieTalkAlert.showToast(getActivity(), "That email address is invalid!");
					mIsResettingValue = true;
					editor.commit();
					EditTextPreference editPref = (EditTextPreference) myPref;
					editPref.setText(oldValue);
					return;
				}
			} else if (key == resources.getString(R.string.receive_pies_from)) {
				preferenceId = R.string.receive_pies_from;
				oldValue = localPreferences.getReceiveFrom();
				newValue = sharedPreferences.getString(key, "");
			} else if (key == resources.getString(R.string.share_stories_to)) {
				preferenceId = R.string.share_stories_to;
				oldValue = localPreferences.getShareTo();
				newValue = sharedPreferences.getString(key, "");
			}
			//localPreferences.setEmail(newValue);
			localPreferences.setValueForPreference(preferenceId, newValue);
			myPref.setSummary(sharedPreferences.getString(key, ""));
			
			mPieTalkService.updatePreferences(localPreferences, new TableOperationCallback<UserPreferences>() {				
				@Override
				public void onCompleted(UserPreferences resultsPreferences, Exception ex,
						ServiceFilterResponse serviceFilterResponse) {	
					//reset preferences if needed, otherwise say they've been saved
					//if (key == resources.getString(R.string.email_address)) {
					//	PieTalkAlert.showToast(getActivity(), "OH NO EMAIL ERROR");
					//}
					//Display error	
					PieTalkLogger.e(TAG, "Callback!");
					if (ex != null) {
						PieTalkLogger.i(TAG, "ERrror in callback");
						if (NoNetworkConnectivityException.class.isInstance(ex))
							return;	
						mIsResettingValue = true;
						UserPreferences backupPrefs = mPieTalkService.getBackupPreferences();
						//PieTalkAlert.showSimpleErrorDialog(getActivity(), ex.getCause().getMessage().replace("\"", ""));
						String error = ex.getCause().getMessage();
						
						//Check for unexpected 500 errors
						if (error.contains("500")) {
							//PieTalkAlert.showToast(getActivity(), "There was an error.  Please try again later.");
							PieTalkAlert.showToast(mPieTalkService.getActivityContext(), "There was an error.  Please try again later.");
						} else {
//							PieTalkAlert.showToast(getActivity(), ex.getCause().getMessage().replace("\"", ""));
							PieTalkAlert.showToast(mPieTalkService.getActivityContext(), ex.getCause().getMessage().replace("\"", ""));
						}
						SharedPreferences.Editor editor = sharedPreferences.edit();
						String oldValue = "";
						if (key == resources.getString(R.string.email_address))
							oldValue = backupPrefs.getEmail();
						editor.putString(key, oldValue);						
						editor.commit();
						myPref.setSummary(oldValue);
						
						if (EditTextPreference.class.isInstance(myPref)) {
							EditTextPreference editPref = (EditTextPreference) myPref;
							editPref.setText(oldValue);	
						}
						
					} else {
						if (getActivity() != null)
							PieTalkAlert.showToast(getActivity(), "Setting updated");
					}
				}
			});
		}
		
		private void initializeToDefaults() {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			//Set Username
			Preference usernamePref = (Preference) findPreference(getString(R.string.username));
			usernamePref.setSummary(mPieTalkService.getUsername());
			usernamePref.setSelectable(false);
			//Set Email
			EditTextPreference emailPref = (EditTextPreference) findPreference(getString(R.string.email_address));
			//emailPref.setSummary(mPieTalkService.getEmail());
			emailPref.setSummary(sharedPrefs.getString(getActivity().getResources().getString(R.string.email_address), ""));
			//Set receive from and share to
			Preference receiveFromPref = (Preference) findPreference(getString(R.string.receive_pies_from));
			receiveFromPref.setSummary(sharedPrefs.getString(getActivity().getResources().getString(R.string.receive_pies_from), ""));
			Preference shareToPref = (Preference) findPreference(getString(R.string.share_stories_to));
			shareToPref.setSummary(sharedPrefs.getString(getActivity().getResources().getString(R.string.share_stories_to), ""));
			
		}
		
		
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_settings, menu);
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
