package org.recentwidget;

import org.recentwidget.dao.EventObserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

// Doc: WordWidget$UpdateService
public class RecentWidgetUpdateService extends Service {

	public static final String ORIGINAL_ACTION = "ORIGINAL_ACTION";

	@Override
	public void onStart(Intent intent, int startId) {

		String originalAction = intent.getStringExtra(ORIGINAL_ACTION);

		if (RecentWidgetUtils.ACTION_UPDATE_ALL.equals(originalAction)) {

			// Shortcut intent for rebuilding the list

			RecentWidgetHolder.rebuildRecentEvents(this.getContentResolver());
			RecentWidgetHolder.updateWidgetLabels(this);

			return;
		}

		for (EventObserver observer : RecentWidgetProvider.eventObservers) {
			if (observer.supports(originalAction)) {

				// Update the recent events

				RecentWidgetHolder.recentEvents = observer.update(
						RecentWidgetHolder.recentEvents, intent, this
								.getContentResolver());

				// Update the widget

				RecentWidgetHolder.updateWidgetLabels(this);

				// Only support 1 observer for a given action

				break;
			}
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		// Binding for what?
		return null;
	}

}
