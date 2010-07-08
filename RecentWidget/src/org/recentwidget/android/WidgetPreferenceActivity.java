package org.recentwidget.android;

import org.recentwidget.R;
import org.recentwidget.RecentWidgetUtils;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class WidgetPreferenceActivity extends PreferenceActivity {

	private static final String PREF_PROVIDER_GMAIL = "provider_email";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.widget_preference);

		if (!RecentWidgetUtils.HAS_ACCOUNT_MANAGER) {
			// Disable the GMail provider
			Preference preference = findPreference(PREF_PROVIDER_GMAIL);
			preference.setSelectable(false);
			preference.setEnabled(false);
		}
	}
}
