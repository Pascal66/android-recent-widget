package org.recentwidget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class RecentWidgetMainActivity extends Activity {

	private static final String TAG = "READ_PHONE_STATE";

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	/**
	 * Called when the activity is first created or when the widget is created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Set the result to CANCELED. This will cause the widget host to cancel
		// out of the widget placement if they press the back button.
		setResult(RESULT_CANCELED);

		setContentView(R.layout.main);

		// Bind the action for the save button.
		// TODO: This should be done somewhere else...

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
		}
	}

	PhoneStateListener phoneListener = new PhoneStateListener() {

		private static final String TAG = "READ_PHONE_STATE";

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			// Just log that we had a Telephony state change

			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				Log.d(TAG, "Listened to incoming call: " + incomingNumber);
				break;

			default:
				break;
			}

			// Then we'll update the widget later... how?

			super.onCallStateChanged(state, incomingNumber);
		}
	};

	View.OnClickListener mOnClickListener = new View.OnClickListener() {
		public void onClick(View v) {

			final Context context = RecentWidgetMainActivity.this;

			// This does not belong here, but let's do it anyways...
			// Note: Should be done after all the configuration has been stored.

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.recentwidget);

			// What to do when onClick

			Intent defineIntent = new Intent(
					RecentWidgetUtils.ACTION_SHOW_POPUP);

			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 // no
					// requestCode
					, defineIntent, Intent.FLAG_ACTIVITY_NEW_TASK // no flags
					);

			views.setOnClickPendingIntent(R.id.image01, pendingIntent);
			views.setOnClickPendingIntent(R.id.image02, pendingIntent);
			views.setOnClickPendingIntent(R.id.image03, pendingIntent);
			views.setOnClickPendingIntent(R.id.image04, pendingIntent);

			// Set it all and leave it be

			appWidgetManager.updateAppWidget(mAppWidgetId, views);

			// Set the listeners...

			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			telManager.listen(phoneListener,
					PhoneStateListener.LISTEN_CALL_STATE);

			// Make sure we pass back the original appWidgetId

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
		}
	};

}