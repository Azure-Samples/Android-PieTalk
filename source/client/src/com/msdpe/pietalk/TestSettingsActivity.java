package com.msdpe.pietalk;

import android.app.Activity;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.msdpe.pietalk.util.PieTalkLogger;

public class TestSettingsActivity extends Activity {
	private final String TAG = "TestSettingsActivity";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_test_settings);
		// Show the Up button in the action bar.
		setupActionBar();
		
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();
	}
	
	public static class SettingsFragment extends PreferenceFragment {
		private final String TAG = "TestSettingsActivity";
		private PieTalkService mPieTalkService;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			
			addPreferencesFromResource(R.xml.preferences);
			PieTalkApplication app = (PieTalkApplication) getActivity().getApplication();
			mPieTalkService = app.getPieTalkService();
			Preference usernamePref = (Preference) findPreference(getString(R.string.username));
			usernamePref.setSummary(mPieTalkService.getUsername());
			usernamePref.setSelectable(false);
			
			EditTextPreference emailPref = (EditTextPreference) findPreference(getString(R.string.email_address));
			emailPref.setSummary(mPieTalkService.getEmail());
			emailPref.setText(mPieTalkService.getEmail());
			
			Preference logoutPref = (Preference) findPreference(getString(R.string.log_out));
			logoutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					mPieTalkService.logout(true);
					return true;
				}
			});
			
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
