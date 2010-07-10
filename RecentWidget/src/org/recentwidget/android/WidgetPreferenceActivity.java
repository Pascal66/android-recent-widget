package org.recentwidget.android;

import org.recentwidget.R;
import org.recentwidget.RecentWidgetUtils;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class WidgetPreferenceActivity extends PreferenceActivity {

	public static final String PREF_PROVIDER_GMAIL = "provider_email";
	private static final String TAG = "RW:PrefActivity";

	private static boolean hasChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.widget_preference);

		Preference gmailPreference = findPreference(PREF_PROVIDER_GMAIL);

		if (!RecentWidgetUtils.HAS_ACCOUNT_MANAGER) {
			// Disable the GMail provider
			gmailPreference.setSelectable(false);
			gmailPreference.setEnabled(false);
		}

		gmailPreference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						// Update the widget when we'll leave the screen.

						hasChanged = true;

						// Always update the preference.

						return true;
					}
				});
	}

	@Override
	protected void onPause() {

		if (hasChanged) {

			// We got out of the pref screen, so we can update the widget now!

			Log
					.d(TAG,
							"Pref Screen exited. Rebuild the list if any pref changed.");

			Intent serviceIntent = new Intent(this,
					RecentWidgetUpdateService.class);

			serviceIntent.putExtra(RecentWidgetUpdateService.ORIGINAL_ACTION,
					RecentWidgetUtils.ACTION_UPDATE_ALL);

			startService(serviceIntent);

		}

		hasChanged = false;

		// finish();
		super.onPause();
	}
}
