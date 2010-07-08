package org.recentwidget.android;

import org.recentwidget.R;
import org.recentwidget.RecentWidgetUtils;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

// TODO: Extend PreferenceActivity for easier management?
// TODO: DELETE THIS ONE in favor of WidgetPreference(no s)Activity!
public class WidgetPreferencesActivity extends Activity {

	private static final String TAG = "RW:WidgetPreferencesActivity";

	// Set as protected so we can use it in the click listener
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	/**
	 * Called when the activity is first created or when the widget is created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Log.v(TAG, "onCreate");

		// Set the result to CANCELED. This will cause the widget host to cancel
		// out of the widget placement if they press the back button.
		setResult(RESULT_CANCELED);

		setContentView(R.layout.preferences);

		// Bind the action for the save button.

		findViewById(R.id.saveButton).setOnClickListener(mOnClickListener);

		// Find the widget id from the intent.

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.

		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		} else {

			// Update the widget for the 1st time

			Log.d(TAG, "Broadcasting ACTION_UPDATE_ALL");

			Intent rebuildIntent = new Intent(
					RecentWidgetUtils.ACTION_UPDATE_ALL);
			sendBroadcast(rebuildIntent);

			// Make sure we pass back the original appWidgetId (for what?)

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			setResult(RESULT_OK, resultValue);

			Log.d(TAG, "Finished registering widget");

			// TODO: No configuration so skip this activity!

			// Buggy because process can be killed, use a broadcast receiver
			// instead.
			// bindTelephonyListener();

			finish();
		}
	}

	View.OnClickListener mOnClickListener = new View.OnClickListener() {

		private static final String TAG = "RecentWidgetMainActivity:mOnClickListener";

		public void onClick(View v) {

			Log.d(TAG, "Saving configuration");

			// bindTelephonyListener();

			finish();
		}

	};

}