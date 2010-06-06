/**
 * 
 */
package org.recentwidget.android;

import org.recentwidget.R;
import org.recentwidget.RecentWidgetUtils;
import org.recentwidget.dao.CallLogDao;
import org.recentwidget.dao.EventObserver;
import org.recentwidget.dao.SmsDao;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RecentWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "RW:RecentWidgetProvider";

	public static final String BUTTON_PRESSED = "org.recentwidget.BUTTON_PRESSED";

	static final int defaultContactImage = android.R.drawable.picture_frame;

	static final EventObserver[] eventObservers = new EventObserver[] {
			new SmsDao(), new CallLogDao() };

	/**
	 * The buttons available on the widget.
	 */
	static int[] buttonMap = new int[] { R.id.contactButton01,
			R.id.contactButton02, R.id.contactButton03, R.id.contactButton04 };

	/**
	 * The contact images available on the widget.
	 */
	static int[] imageMap = new int[] { R.id.contactSrc01, R.id.contactSrc02,
			R.id.contactSrc03, R.id.contactSrc04 };

	@Override
	// Note: not called when using a ConfigurationActivity
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		// Nothing to do. If everything is correct, the widget always displays
		// the right information; i.e. the events are always pushed to the
		// widget.

		Log.d(TAG, "Called onUpdate");

		// Still, check that process was not killed and update everything...
		// (use AlarmManager
		// instead?)

		if (!RecentWidgetHolder.isAlive()) {
			Log.d(TAG, "Holder was empty. Rebuild the list.");
			startUpdateService(context, RecentWidgetUtils.ACTION_UPDATE_ALL);
		} else {
			Log.d(TAG, "Holder still filled.");
		}

	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		// Proxy: Recent Event list should be rebuilt/refreshed
		// To prevent any ANR timeouts, we perform the update in a service

		for (String acceptedAction : RecentWidgetUtils.ACTION_UPDATE_TYPES) {

			if (acceptedAction.equals(action)) {

				startUpdateService(context, action);

				return;

			}
		}

		// TODO: workaround for onDelete in 1.5
		// http://groups.google.com/group/android-developers/msg/e405ca19df2170e2?pli=1

		super.onReceive(context, intent);
	}

	private void startUpdateService(Context context, String action) {

		Intent serviceIntent = new Intent(context,
				RecentWidgetUpdateService.class);

		serviceIntent.putExtra(RecentWidgetUpdateService.ORIGINAL_ACTION,
				action);

		context.startService(serviceIntent);
	}

}
