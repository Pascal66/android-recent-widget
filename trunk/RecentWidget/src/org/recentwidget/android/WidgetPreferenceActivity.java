package org.recentwidget.android;

import org.recentwidget.R;
import org.recentwidget.RecentWidgetUtils;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class WidgetPreferenceActivity extends PreferenceActivity {

	private final class OnPreferenceChangeListenerImplementation implements
			OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {

			// Update the widget when we'll leave the screen.

			hasChanged = true;

			// Always update the preference.

			return true;
		}
	}

	public static final String PREF_PROVIDER_GMAIL = "provider_email";
	public static final String PREF_PROVIDER_CALENDAR = "provider_calendar";
	public static final String PREF_MAX_RETRIEVED = "num_retrieved";
	public static final String PREF_NUM_PER_PAGE = "num_per_page";

	private static final String TAG = "RW:PrefActivity";

	private static boolean hasChanged = false;
	private OnPreferenceChangeListenerImplementation reloadAfterPreferenceChanged;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.widget_preference);
		reloadAfterPreferenceChanged = new OnPreferenceChangeListenerImplementation();

		initProviderPreferenceWith2x(PREF_NUM_PER_PAGE, false);
		initProviderPreferenceWith2x(PREF_MAX_RETRIEVED, false);

		initProviderPreferenceWith2x(PREF_PROVIDER_CALENDAR, true);
		initProviderPreferenceWith2x(PREF_PROVIDER_GMAIL, true);
	}

	private void initProviderPreferenceWith2x(String preferenceName,
			boolean requires2x) {
		Preference preference = findPreference(preferenceName);

		if (requires2x && !RecentWidgetUtils.HAS_ACCOUNT_MANAGER) {
			// Disable the GMail provider
			preference.setSelectable(false);
			preference.setEnabled(false);
		}

		preference.setOnPreferenceChangeListener(reloadAfterPreferenceChanged);
	}

	@Override
	protected void onPause() {

		if (hasChanged) {

			// We got out of the pref screen, so we can update the widget now!

			Log.d(TAG,
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
