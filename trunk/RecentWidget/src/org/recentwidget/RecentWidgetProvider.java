/**
 * 
 */
package org.recentwidget;

import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.widget.RemoteViews;

public class RecentWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "RW:RecentWidgetProvider";

	/**
	 * Number of recent events displayed on widget. Might be configurable?
	 */
	static final int maxRetrieved = 4;

	/**
	 * List of the Events to be displayed on the widget.
	 */
	static List<RecentEvent> recentEvents = new ArrayList<RecentEvent>(
			maxRetrieved);

	@Override
	// Note: not called when using a ConfigurationActivity
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		// TODO: WordWidget says that updates should be done by a service?

	}

	@Override
	public void onReceive(Context context, Intent intent) {

		if (RecentWidgetUtils.ACTION_UPDATE_TELEPHONY
				.equals(intent.getAction())) {

			Log.d(TAG, "Received broadcasted ACTION_UPDATE_TELEPHONY");

			// Retrieve the last telephony events

			// TODO: Use template pattern if sms, email, etc also use a
			// ContentProvider?

			List<RecentEvent> telRecentEvents = new ArrayList<RecentEvent>(
					maxRetrieved);

			// Do not use managedQuery because we will unload it ourselves

			ContentResolver dataProvider = context.getContentResolver();
			Cursor callsCursor = dataProvider.query(Calls.CONTENT_URI,
					new String[] { Calls.CACHED_NAME, Calls.NUMBER, Calls.NEW,
							Calls.TYPE }, null, null, Calls.DEFAULT_SORT_ORDER);

			String name;
			String number;
			int type;
			int counter = maxRetrieved;

			// Note: we might not want to retrieve all the maxRetrieved events
			// (maybe just the last one is enough?)... depends on the current
			// state of the recentEvents list.

			if (callsCursor.moveToFirst()) {
				while (!callsCursor.isAfterLast() && counter > 0) {

					name = callsCursor.getString(callsCursor
							.getColumnIndex(Calls.CACHED_NAME));
					number = callsCursor.getString(callsCursor
							.getColumnIndex(Calls.NUMBER));
					type = callsCursor.getInt(callsCursor
							.getColumnIndex(Calls.TYPE));

					RecentEvent event = new RecentEvent();
					event.setPerson(name);
					event.setNumber(number);
					event.setType(type);

					Log.v(TAG, "Pushing telephony recent event");
					telRecentEvents.add(event);

					callsCursor.moveToNext();
					counter--;
				}
			}

			callsCursor.close();

			// Cheat: all recent events are telephony events for now

			recentEvents = telRecentEvents;

			// Update the widget

			updateWidgetLabels(context);

		} else {

			// TODO: workaround for onDelete in 1.5
			// http://groups.google.com/group/android-developers/msg/e405ca19df2170e2?pli=1

			super.onReceive(context, intent);
		}
	}

	private void updateWidgetLabels(Context context) {
		// Update labels on widget

		if (recentEvents.size() > 0) {

			Log.d(TAG, "Updating widget labels");

			String label = "N/A";

			RemoteViews views = RecentWidgetMainActivity
					.buildWidgetView(context);

			int[] imageMap = new int[] { R.id.image01, R.id.image02,
					R.id.image03, R.id.image04 };

			for (int i = 0; i < maxRetrieved; i++) {

				if (recentEvents.get(i).getPerson() != null) {
					label = recentEvents.get(i).getPerson();
				} else if (recentEvents.get(i).getNumber() != null) {
					label = recentEvents.get(i).getNumber();
				}

				Log.d(TAG, "Setting button label");

				views.setCharSequence(imageMap[i], "setText", label);

			}

			// Push update for this widget to the home screen

			Log.d(TAG, "Pushing updated widget to provider");

			ComponentName thisWidget = new ComponentName(context,
					RecentWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			manager.updateAppWidget(thisWidget, views);

		} else {

			Log.d(TAG, "No recent events to set on widget");

		}
	}
}
