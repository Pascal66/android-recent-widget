/**
 * 
 */
package org.recentwidget.android;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.recentwidget.R;
import org.recentwidget.RecentWidgetUtils;
import org.recentwidget.dao.CalendarDao;
import org.recentwidget.dao.CallLogDao;
import org.recentwidget.dao.EventObserver;
import org.recentwidget.dao.GmailDao;
import org.recentwidget.dao.SmsDao;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RecentWidgetProvider extends AppWidgetProvider {

	private static final int RESET_PAGE_AFTER_MINUTES = 15;

	private static final String TAG = "RW:RecentWidgetProvider";

	public static final String BUTTON_PRESSED = "org.recentwidget.BUTTON_PRESSED";

	public static final int defaultContactImage = R.drawable.ic_contacts_details;

	/**
	 * Default observers. GMail observer can be added as a preference.
	 */
	static final EventObserver[] eventObservers;

	static {
		if (RecentWidgetUtils.HAS_ACCOUNT_MANAGER) {
			eventObservers = new EventObserver[] { new SmsDao(),
					new CallLogDao(), new GmailDao(), new CalendarDao() };
		} else {
			// Check preference
			eventObservers = new EventObserver[] { new SmsDao(),
					new CallLogDao() };
		}
	}

	/**
	 * The buttons available on the widget.
	 */
	static int[] buttonMap = new int[] { R.id.contactButton01,
			R.id.contactButton02, R.id.contactButton03, R.id.contactButton04,
			R.id.contactButton05, R.id.contactButton06 };

	/**
	 * The contact images available on the widget.
	 */
	static int[] imageMap = buttonMap;

	/**
	 * The text views holding the contact name labels.
	 */
	static int[] labelMap = new int[] { R.id.contactLabel01,
			R.id.contactLabel02, R.id.contactLabel03, R.id.contactLabel04,
			R.id.contactLabel05, R.id.contactLabel06 };

	static Date lastUpdated = new Date(0);

	@Override
	// Note: not called when using a ConfigurationActivity
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.d(TAG, "Called onUpdate");

		// Still, check that process was not killed and update everything...
		// (use AlarmManager instead? maybe less resources used than widget
		// timer?)

		if (!RecentWidgetHolder.isAlive()) {
			Log.d(TAG, "Holder was empty. Rebuild the list.");

			startService(context, appWidgetManager, appWidgetIds,
					RecentWidgetUtils.ACTION_UPDATE_ALL);

		} else {
			Log.d(TAG, "Holder still filled.");

			// Nothing to do. If everything is correct, the widget always
			// displays the right information; i.e. the events are always pushed
			// to the widget.

			// BUT maybe it's the first time we display the widget, so update
			// the graphics, or inactivity resets the page num to 0.

			Calendar calendar = GregorianCalendar.getInstance();
			calendar.add(Calendar.MINUTE, -RESET_PAGE_AFTER_MINUTES);

			if (lastUpdated.getTime() == 0
					|| lastUpdated.before(calendar.getTime())) {
				RecentWidgetHolder.currentPage = 0;
			}

			startService(context, appWidgetManager, appWidgetIds,
					RecentWidgetUtils.ACTION_REFRESH_DISPLAY);

		}

	}

	private void startService(Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds,
			String serviceAction) {

		Intent serviceIntent = new Intent(context,
				RecentWidgetUpdateService.class);

		serviceIntent.putExtra(RecentWidgetUpdateService.ORIGINAL_ACTION,
				serviceAction);

		serviceIntent.putExtra(RecentWidgetUpdateService.UPDATED_WIDGETS_IDS,
				appWidgetIds);

		context.startService(serviceIntent);
	}

	private void startService(Context context, Intent originalIntent) {

		Intent serviceIntent = new Intent(context,
				RecentWidgetUpdateService.class);

		serviceIntent.putExtra(RecentWidgetUpdateService.ORIGINAL_INTENT,
				originalIntent);

		context.startService(serviceIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		// Proxy: Recent Event list should be rebuilt/refreshed
		// To prevent any ANR timeouts, we perform the update in a service

		for (String acceptedAction : RecentWidgetUtils.ACTION_UPDATE_TYPES) {

			lastUpdated = new Date();

			if (acceptedAction.equals(action)) {

				Intent serviceIntent = new Intent(context,
						RecentWidgetUpdateService.class);

				startService(context, serviceIntent);

				return;

			}
		}

		// TODO: workaround for onDelete in 1.5
		// http://groups.google.com/group/android-developers/msg/e405ca19df2170e2?pli=1

		super.onReceive(context, intent);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		RecentWidgetHolder.clean();
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		RecentWidgetHolder.clean();
		super.onDisabled(context);
	}

	public static int getButtonPosition(int buttonId) {
		for (int index = 0; index < RecentWidgetHolder.numPerPage; index++) {
			if (buttonMap[index] == buttonId) {
				return index;
			}
		}
		return -1;
	}
}
