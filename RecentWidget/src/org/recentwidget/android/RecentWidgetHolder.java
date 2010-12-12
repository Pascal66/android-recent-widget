package org.recentwidget.android;

import java.util.ArrayList;
import java.util.List;

import org.recentwidget.R;
import org.recentwidget.RecentWidgetUtils;
import org.recentwidget.dao.EventObserver;
import org.recentwidget.model.RecentContact;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

// Holds the recent events to be displayed
public class RecentWidgetHolder {

	private static final String TAG = "RW:RecentWidgetHolder";

	private static final int LABEL_MAX_LINES_WITH_PIC = 2;
	private static final int LABEL_MAX_LINES_NO_PIC = 4;

	private static final int OBSERVERS_COUNT = RecentWidgetProvider.eventObservers.length;

	/**
	 * List of the Events to be displayed on the widget.
	 */
	static List<RecentContact> recentContacts;

	static int[] firstImageMap = new int[] { R.id.contactEventLabel01_0,
			R.id.contactEventLabel02_0, R.id.contactEventLabel03_0,
			R.id.contactEventLabel04_0, R.id.contactEventLabel05_0,
			R.id.contactEventLabel06_0 };
	static int[] secondImageMap = new int[] { R.id.contactEventLabel01_1,
			R.id.contactEventLabel02_1, R.id.contactEventLabel03_1,
			R.id.contactEventLabel04_1, R.id.contactEventLabel05_1,
			R.id.contactEventLabel06_1 };
	static int[] thirdImageMap = new int[] { R.id.contactEventLabel01_2,
			R.id.contactEventLabel02_2, R.id.contactEventLabel03_2,
			R.id.contactEventLabel04_2, R.id.contactEventLabel05_2,
			R.id.contactEventLabel06_2 };
	static int[] fourthImageMap = new int[] { R.id.contactEventLabel01_3,
			R.id.contactEventLabel02_3, R.id.contactEventLabel03_3,
			R.id.contactEventLabel04_3, R.id.contactEventLabel05_3,
			R.id.contactEventLabel06_3 };

	static int[][] imageMap = new int[][] { firstImageMap, secondImageMap,
			thirdImageMap, fourthImageMap };

	/**
	 * 0-based index of the page of contacts to be displayed.
	 */
	protected static int currentPage = 0;
	private static int maxPage;

	public static int numPerPage;

	static final void updateWidgetLabels(Context context) {

		if (recentContacts == null) {
			Log.e(TAG, "Trying to update widget without recentContacts! "
					+ "Skipping updateWidgetLabels.");
			return;
		}

		// Update labels/text/photos on widget

		if (recentContacts.size() > 0) {

			Log.d(TAG, "Updating widget labels");

			String label = "N/A";

			RemoteViews views = buildWidgetView(context);

			int i = currentPage * RecentWidgetHolder.numPerPage;
			// End index not included
			int endIndex = i + RecentWidgetHolder.numPerPage;

			for (; i < recentContacts.size() && i < endIndex; i++) {

				RecentContact recentContact = recentContacts.get(i);

				// Set the button label on widget

				if (recentContact.getDisplayName() != null) {
					label = recentContact.getDisplayName();
				}

				views.setViewVisibility(RecentWidgetProvider.labelMap[i
						% RecentWidgetHolder.numPerPage], View.VISIBLE);

				views.setCharSequence(RecentWidgetProvider.labelMap[i
						% RecentWidgetHolder.numPerPage], "setText", label);

				// Also try to set the picture

				Bitmap contactPhoto = null;

				if (recentContact != null && recentContact.hasContactInfo()) {

					contactPhoto = RecentWidgetUtils.CONTACTS_API
							.loadContactPhoto(context, recentContact);

				}

				if (contactPhoto != null) {

					views.setImageViewBitmap(RecentWidgetProvider.imageMap[i
							% RecentWidgetHolder.numPerPage], contactPhoto);

					views.setInt(RecentWidgetProvider.labelMap[i
							% RecentWidgetHolder.numPerPage], "setMaxLines",
							LABEL_MAX_LINES_WITH_PIC);

				} else {

					// No photo found or no contact associated

					// Note that we cannot create bitmap from R.drawable.* and
					// display it on widget. Maybe a matter of classloader or
					// things like that. So we need to use a ViewResource...

					views.setImageViewResource(RecentWidgetProvider.imageMap[i
							% RecentWidgetHolder.numPerPage],
							RecentWidgetProvider.defaultContactImage);

					// When no photo, make the text wrap on more lines

					views.setInt(RecentWidgetProvider.labelMap[i
							% RecentWidgetHolder.numPerPage], "setMaxLines",
							LABEL_MAX_LINES_NO_PIC);

				}

				views.setViewVisibility(RecentWidgetProvider.imageMap[i
						% RecentWidgetHolder.numPerPage], View.VISIBLE);

				// Show each last event type for this contact

				int count = 0;
				for (EventObserver observer : RecentWidgetProvider.eventObservers) {
					Integer resource = observer
							.getResourceForWidget(recentContact);
					if (resource != null) {
						views.setImageViewResource(imageMap[count][i
								% RecentWidgetHolder.numPerPage], resource);
						views.setViewVisibility(imageMap[count][i
								% RecentWidgetHolder.numPerPage], View.VISIBLE);
						count++;
					} else {
						views.setViewVisibility(imageMap[count][i
								% RecentWidgetHolder.numPerPage],
								View.INVISIBLE);
					}
				}

			}

			// Show blank areas if not enough events to be shown

			for (; i < endIndex; i++) {
				views.setViewVisibility(RecentWidgetProvider.labelMap[i
						% RecentWidgetHolder.numPerPage], View.INVISIBLE);
				views.setViewVisibility(RecentWidgetProvider.imageMap[i
						% RecentWidgetHolder.numPerPage], View.INVISIBLE);
				for (int j = 0; j < OBSERVERS_COUNT; j++) {
					views.setViewVisibility(imageMap[j][i
							% RecentWidgetHolder.numPerPage], View.INVISIBLE);
				}
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

	/**
	 * Create the RemoteViews on which the widget is based. Sets up the click
	 * listeners and the interaction widgets.
	 * 
	 * @param context
	 * @return
	 */
	static final RemoteViews buildWidgetView(final Context context) {

		Log.d(TAG, "Creating widget view");

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.recentwidget);

		// Set the number of contacts per page

		final int maxPerPage = 6;

		int[] frames = { R.id.widgetFrame1, R.id.widgetFrame2,
				R.id.widgetFrame3, R.id.widgetFrame4, R.id.widgetFrame5,
				R.id.widgetFrame6 };

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		numPerPage = Integer.parseInt(prefs.getString(
				WidgetPreferenceActivity.PREF_NUM_PER_PAGE, "3"));

		int curPerPage = maxPerPage - 1;
		for (; curPerPage >= numPerPage; curPerPage--) {
			views.setViewVisibility(frames[curPerPage], View.GONE);
		}
		for (; curPerPage > 0; curPerPage--) {
			views.setViewVisibility(frames[curPerPage], View.VISIBLE);
		}

		// What to do when onClick

		for (int buttonIdNum = 0; buttonIdNum < numPerPage; buttonIdNum++) {

			int buttonId = RecentWidgetProvider.buttonMap[buttonIdNum];

			Intent defineIntent = new Intent(
					RecentWidgetUtils.ACTION_SHOW_POPUP);

			// TODO: bad idea, give index of RecentEvent rather?
			defineIntent
					.putExtra(RecentWidgetProvider.BUTTON_PRESSED, buttonId);

			// buttonId as requestCode = hack to make sure that each intent is
			// created as a new instance (instead of re-used or overwritten)

			PendingIntent pendingIntent = PendingIntent.getActivity(context,
					buttonId // requestCode
					, defineIntent, Intent.FLAG_ACTIVITY_NEW_TASK // no flags
					);

			views.setOnClickPendingIntent(buttonId, pendingIntent);

		}

		// The Next button

		Intent defineIntent = new Intent(RecentWidgetUtils.ACTION_NEXT_CONTACTS);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, -1 // requestCode
				, defineIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.nextButton, pendingIntent);

		// Pager info

		updatePager();

		int maxPageShown = maxPage + 1;
		int currentPageShown = (currentPage % maxPageShown) + 1;

		views.setTextViewText(R.id.widgetPager, currentPageShown + "/"
				+ maxPageShown);

		return views;

	}

	/**
	 * @param buttonPressed
	 * @param context
	 * @return !!! Resource intensive!
	 */
	protected static RecentContact getRecentEventPressed(int buttonPressed,
			Context context) {

		// First find which button was pressed

		int index = RecentWidgetProvider.getButtonPosition(buttonPressed);

		resetIfKilled(context);

		// Find which contact by shifting with the current paging

		if (index >= 0) {
			index += currentPage * RecentWidgetHolder.numPerPage;
			if (index < recentContacts.size()) {
				return recentContacts.get(index);
			} else {
				Log.e(TAG, "Button pressed but "
						+ "no corresponding recent events.");
				return null;
			}
		} else {
			Log.e(TAG, "No button with given id.");
			return null;
		}
	}

	private static void resetIfKilled(Context context) {
		if (!isAlive()) {

			// Button pressed but no recentEvent attached! Surely
			// garbage-collected so let's create a new list...

			rebuildRecentEvents(context);
			updateWidgetLabels(context);
		}
	}

	public static void rebuildRecentEvents(Context context) {
		recentContacts = new ArrayList<RecentContact>();

		for (EventObserver observer : RecentWidgetProvider.eventObservers) {
			recentContacts = observer.update(recentContacts, null, context);
		}

		updatePager();
	}

	public static boolean isAlive() {
		return recentContacts != null;
	}

	public static void clean() {
		// Just delete cache
		Log.v(TAG, "Clearing cached contacts");
		recentContacts = null;
	}

	private static boolean updatePager() {

		maxPage = (int) Math.floor(((double) recentContacts.size() - 1)
				/ RecentWidgetHolder.numPerPage);
		if (maxPage < 0) {
			maxPage = 0;
		}

		// Just check if we are on an existing page

		if (currentPage > maxPage || currentPage < 0) {
			currentPage = 0;
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Go to next page. Rebuild the contact list if it was killed.
	 * 
	 * @return Whether there is actually a next page and we are cycling through
	 *         the page.
	 */
	public static boolean nextPage(Context context) {

		if (context != null) {
			resetIfKilled(context);
		}

		if (recentContacts == null || recentContacts.size() == 0) {
			currentPage = 0;
			maxPage = 0;
			return false;
		} else {
			currentPage++;
			return updatePager();
		}

	}

	public static boolean previousPage() {
		// No parameter as nextPage because we never call it where there might
		// not

		if (recentContacts == null || recentContacts.size() == 0) {
			currentPage = 0;
			maxPage = 0;
			return false;
		} else {
			currentPage--;
			return updatePager();
		}
	}
}
