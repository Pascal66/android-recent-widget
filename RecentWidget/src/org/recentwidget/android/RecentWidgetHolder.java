package org.recentwidget.android;

import java.util.ArrayList;
import java.util.List;

import org.recentwidget.R;
import org.recentwidget.RecentWidgetUtils;
import org.recentwidget.compat.ContactAccessor;
import org.recentwidget.dao.EventObserver;
import org.recentwidget.model.RecentContact;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.Contacts;
import android.provider.Contacts.Phones;
import android.util.Log;
import android.widget.RemoteViews;

// Holds the recent events to be displayed
public class RecentWidgetHolder {

	private static final String TAG = "RW:RecentWidgetHolder";

	/**
	 * List of the Events to be displayed on the widget.
	 */
	static List<RecentContact> recentContacts;

	/**
	 * 0-based index of the page of contacts to be displayed.
	 */
	private static int currentPage;
	private static int maxPage;

	/* establish whether the "new" class is available to us */
	static {
		try {
			ContactAccessor.checkAvailable();
			RecentWidgetUtils.contactsContractAvailable = true;
		} catch (Throwable t) {
			RecentWidgetUtils.contactsContractAvailable = false;
		}
	}

	static final void updateWidgetLabels(Context context) {

		if (recentContacts == null) {
			return;
		}

		// Update labels on widget

		// Note: Labels/Photos are better handled using ContactsContract (API-5)

		if (recentContacts.size() > 0) {

			Log.d(TAG, "Updating widget labels");

			String label = "N/A";

			RemoteViews views = buildWidgetView(context);

			int i = currentPage * RecentWidgetProvider.numContactsDisplayed;
			// End index not included
			int endIndex = i + RecentWidgetProvider.numContactsDisplayed;

			for (; i < recentContacts.size() && i < endIndex; i++) {

				RecentContact recentContact = recentContacts.get(i);

				// Try to fetch the Contact
				// TODO: Skip it if we already have the info!

				ContentResolver resolver = context.getContentResolver();
				Cursor contactCursor = resolver.query(
						Contacts.Phones.CONTENT_URI, new String[] {
								Phones.PERSON_ID, Phones.DISPLAY_NAME },
						Phones.NUMBER + " = ?", new String[] { recentContact
								.getNumber() }, Phones.DEFAULT_SORT_ORDER);

				if (contactCursor.getCount() >= 1) {

					// Just take the 1st result even if there are several
					// matches

					contactCursor.moveToFirst();

					label = contactCursor.getString(contactCursor
							.getColumnIndex(Phones.DISPLAY_NAME));

					// Set it, so next time we might not need to repeat this
					// query...

					recentContact.setPerson(label);

					String personIdAsString = contactCursor
							.getString(contactCursor
									.getColumnIndex(Phones.PERSON_ID));

					recentContact.setPersonId(Long.parseLong(personIdAsString));

				} else {

					// Make sure that we flag the contact as having no contact
					// info. TODO: UGLY! Fix real cause.

					recentContact.setPersonId(null);

					// Defaults to the basic info we got

					if (recentContact.getPerson() != null) {
						label = recentContact.getPerson();
					} else if (recentContact.getNumber() != null) {
						label = recentContact.getNumber();
					}
				}

				contactCursor.close();

				views.setCharSequence(RecentWidgetProvider.buttonMap[i
						% RecentWidgetProvider.numContactsDisplayed],
						"setText", label);

				// Also try to set the picture

				Bitmap contactPhoto = RecentWidgetUtils.loadContactPhoto(
						context, recentContact);

				views.setBitmap(RecentWidgetProvider.imageMap[i
						% RecentWidgetProvider.numContactsDisplayed],
						"setImageBitmap", contactPhoto);

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

		// Note: API-7 supports RemoteViews.addView !!!

		// What to do when onClick

		for (int buttonId : RecentWidgetProvider.buttonMap) {

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

		int maxPageShown = maxPage + 1;
		int currentPageShown = (currentPage % maxPageShown) + 1;

		views.setTextViewText(R.id.widgetPager, currentPageShown + "/"
				+ maxPageShown);

		return views;

	}

	protected static RecentContact getRecentEventPressed(int buttonPressed,
			Context context) {

		boolean found = false;
		int index = 0;

		// First find which button was pressed

		for (index = 0; index < RecentWidgetProvider.numContactsDisplayed; index++) {
			if (RecentWidgetProvider.buttonMap[index] == buttonPressed) {
				found = true;
				break;
			}
		}

		resetIfKilled(context);

		// Find which contact by shifting with the current paging

		index += currentPage * RecentWidgetProvider.numContactsDisplayed;
		if (found && index < recentContacts.size()) {
			return recentContacts.get(index);
		} else {
			Log.e(TAG, "Button pressed but no corresponding recent events.");
			return null;
		}
	}

	private static void resetIfKilled(Context context) {
		if (!isAlive()) {

			// Button pressed but no recentEvent attached! Surely
			// garbage-collected so let's create a new list...

			rebuildRecentEvents(context.getContentResolver());
			updateWidgetLabels(context);
		}
	}

	public static void rebuildRecentEvents(ContentResolver contentResolver) {
		recentContacts = new ArrayList<RecentContact>();

		for (EventObserver observer : RecentWidgetProvider.eventObservers) {
			recentContacts = observer.update(recentContacts, null,
					contentResolver);
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

	private static void updatePager() {

		maxPage = (int) Math.floor(((double) recentContacts.size() - 1) / 3);

		// Just check if we are on an existing page

		if (currentPage > maxPage) {
			currentPage = 0;
		}
	}

	public static void nextPage(Context context) {

		resetIfKilled(context);

		if (recentContacts == null || recentContacts.size() == 0) {
			currentPage = 0;
			maxPage = 0;
		} else {
			currentPage++;
			updatePager();
		}

	}
}
