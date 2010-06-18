package org.recentwidget.android;

import org.recentwidget.RecentWidgetUtils;
import org.recentwidget.dao.EventObserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

// Doc: WordWidget$UpdateService
public class RecentWidgetUpdateService extends Service {

	private static final String TAG = "RW:UpdateService";

	public static final String ORIGINAL_ACTION = "ORIGINAL_ACTION";
	public static final String ORIGINAL_INTENT = "ORIGINAL_INTENT";

	private static final long OBSERVER_DELAY_MILIS = 2000;

	private final Handler observerHandler = new Handler();

	private EventObserver triggerObserver;
	private Intent triggerIntent;
	private Context originalContext;

	@Override
	public void onStart(Intent intent, int startId) {

		String originalAction;

		if (intent.getParcelableExtra(ORIGINAL_INTENT) != null) {
			intent = intent.getParcelableExtra(ORIGINAL_INTENT);
			originalAction = intent.getAction();
		} else {
			originalAction = intent.getStringExtra(ORIGINAL_ACTION);
		}

		if (originalAction == null) {
			Log.d(TAG, "Could not handle intent.");
		}

		boolean updateWidget = false;

		if (RecentWidgetUtils.ACTION_UPDATE_ALL.equals(originalAction)) {

			// Shortcut intent for rebuilding the whole list

			RecentWidgetHolder.rebuildRecentEvents(this);

			updateWidget = true;

		} else if (RecentWidgetUtils.ACTION_NEXT_CONTACTS
				.equals(originalAction)) {

			// Show the next page of contacts

			RecentWidgetHolder.nextPage(this);
			updateWidget = true;

		} else if (RecentWidgetUtils.ACTION_REFRESH_DISPLAY
				.equals(originalAction)) {

			updateWidget = true;

		} else {

			for (EventObserver observer : RecentWidgetProvider.eventObservers) {
				if (observer.supports(originalAction)) {

					// Update the recent events

					Log.d(TAG, "Handling intent: " + intent);
					if (intent.getExtras() != null) {
						Log.v(TAG, "Intent extras: "
								+ intent.getExtras().keySet());
					}

					// Need to delay the update because the CallLog for example
					// takes time to be updated.

					triggerIntent = intent;
					triggerObserver = observer;
					originalContext = this;

					observerHandler.removeCallbacks(observerUpdate);
					observerHandler.postDelayed(observerUpdate,
							OBSERVER_DELAY_MILIS);

					// Only support 1 observer for a given action, so break

					break;
				}
			}

		}

		// Update the widget

		if (updateWidget) {
			RecentWidgetHolder.updateWidgetLabels(this);
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		// Binding for what?
		return null;
	}

	private final Runnable observerUpdate = new Runnable() {

		@Override
		public void run() {
			// Do the actual list building, etc...

			RecentWidgetHolder.recentContacts = triggerObserver.update(
					RecentWidgetHolder.recentContacts, triggerIntent,
					originalContext);

			RecentWidgetHolder.updateWidgetLabels(originalContext);
		}
	};
}
