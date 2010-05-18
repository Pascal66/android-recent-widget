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
import android.widget.RemoteViews;

/**
 * @author Administrator
 * 
 */
public class RecentWidgetProvider extends AppWidgetProvider {

	static List<RecentEvent> recentEvents = new ArrayList<RecentEvent>();

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

			// Update the Recent Calls stack

			// Do not use managedQuery because we will unload it ourselves

			ContentResolver dataProvider = context.getContentResolver();
			Cursor callsCursor = dataProvider.query(Calls.CONTENT_URI,
					new String[] { Calls.CACHED_NAME, Calls.NUMBER, Calls.NEW,
							Calls.TYPE }, null, null, Calls.DEFAULT_SORT_ORDER);

			int maxRetrieved = 4;
			String name;
			String number;
			int type;

			if (callsCursor.moveToFirst()) {
				while (!callsCursor.isAfterLast() && maxRetrieved > 0) {

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

					recentEvents.add(event);

					callsCursor.moveToNext();
					maxRetrieved--;
				}
			}

			callsCursor.close();

			// Update widget
			// TODO: This should be done on first creation (but how to access
			// the recentEvents field? Maybe the Configurer should delegate to
			// the onUpdate...)

			// BUG! Does not include the current caller in the list!!!

			if (recentEvents.size() > 0) {
				String label = "N/A";

				RemoteViews views = RecentWidgetMainActivity
						.buildWidgetView(context);

				if (recentEvents.get(0).getPerson() != null) {
					label = recentEvents.get(0).getPerson();
				} else if (recentEvents.get(0).getNumber() != null) {
					label = recentEvents.get(0).getNumber();
				}

				views.setCharSequence(R.id.image01, "setText", label);

				// Push update for this widget to the home screen

				ComponentName thisWidget = new ComponentName(context,
						RecentWidgetProvider.class);
				AppWidgetManager manager = AppWidgetManager
						.getInstance(context);
				manager.updateAppWidget(thisWidget, views);

			}

		} else {

			// TODO: workaround for onDelete in 1.5
			// http://groups.google.com/group/android-developers/msg/e405ca19df2170e2?pli=1

			super.onReceive(context, intent);
		}
	}
}
