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
			final Preference myPref = findPreference(key);
			
			
			final Resources resources = getActivity().getResources();
			final UserPreferences localPreferences = mPieTalkService.getLocalPreferences();
			
			if (key == resources.getString(R.string.email_address)) {
				String oldEmail = localPreferences.getEmail();
				String newEmail = sharedPreferences.getString(key, "");
				if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(key, oldEmail);
					PieTalkAlert.showToast(getActivity(), "That email address is invalid!");
					editor.commit();
					EditTextPreference editPref = (EditTextPreference) myPref;
					editPref.setText(oldEmail);
					return;
				} else {
					//Save email address
					localPreferences.setEmail(newEmail);
				}
			}			
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
					if (ex != null) {
						if (NoNetworkConnectivityException.class.isInstance(ex))
							return;	
						PieTalkAlert.showSimpleErrorDialog(getActivity(), ex.getCause().getMessage());
						SharedPreferences.Editor editor = sharedPreferences.edit();
						String oldEmail = localPreferences.getEmail();
						editor.putString(key, oldEmail);
						PieTalkAlert.showToast(getActivity(), "That email address is invalid!");
						editor.commit();
						EditTextPreference editPref = (EditTextPreference) myPref;
						editPref.setText(oldEmail);						
					} else {
						
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
