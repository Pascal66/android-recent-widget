package org.recentwidget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

public class RecentWidgetMainActivity extends Activity {

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

			Intent defineIntent2 = new Intent(Intent.ACTION_DEFAULT, Uri
					.parse("http://www.google.com"));

			PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0 // no
					// requestCode
					, defineIntent2, 0 // no flags
					);

			views.setOnClickPendingIntent(R.id.image02, pendingIntent2);
			views.setOnClickPendingIntent(R.id.image03, pendingIntent2);
			views.setOnClickPendingIntent(R.id.image04, pendingIntent2);

			// Set it all and leave it be

			appWidgetManager.updateAppWidget(mAppWidgetId, views);

			// Make sure we pass back the original appWidgetId

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
		}
	};

}