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
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.Contacts;
import android.provider.Contacts.People;
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

			for (int i = 0; i < recentContacts.size(); i++) {

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

				views.setCharSequence(RecentWidgetProvider.buttonMap[i],
						"setText", label);

				if (recentContact.hasContactInfo()) {

					// Also try to set the picture

					Bitmap contactPhoto;

					if (RecentWidgetUtils.contactsContractAvailable) {

						contactPhoto = ContactAccessor.loadContactPhoto(
								context, recentContact,
								RecentWidgetProvider.defaultContactImage);

					} else {

						// ContactsContract not available

						contactPhoto = People.loadContactPhoto(context,
								ContentUris.withAppendedId(People.CONTENT_URI,
										recentContact.getPersonId()),
								RecentWidgetProvider.defaultContactImage, null);
					}

					views.setBitmap(RecentWidgetProvider.imageMap[i],
							"setImageBitmap", contactPhoto);
				} else {
					views.setImageViewResource(
							RecentWidgetProvider.imageMap[i],
							RecentWidgetProvider.defaultContactImage);
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

		return views;

	}

	protected static RecentContact getRecentEventPressed(int buttonPressed,
			Context context) {

		boolean found = false;
		int index = 0;

		for (index = 0; index < RecentWidgetProvider.buttonMap.length; index++) {
			if (RecentWidgetProvider.buttonMap[index] == buttonPressed) {
				found = true;
				break;
			}
		}

		if (!isAlive()) {

			// Button pressed but no recentEvent attached! Surely
			// garbage-collected so let's create a new list...

			rebuildRecentEvents(context.getContentResolver());
			updateWidgetLabels(context);
		}

		if (found && recentContacts.size() - 1 >= index) {
			return recentContacts.get(index);
		} else {
			Log.e(TAG, "Button pressed but no corresponding recent events.");
			return null;
		}
	}

	public static void rebuildRecentEvents(ContentResolver contentResolver) {
		recentContacts = new ArrayList<RecentContact>();

		for (EventObserver observer : RecentWidgetProvider.eventObservers) {
			recentContacts = observer.update(recentContacts, null,
					contentResolver);
		}
	}

	public static boolean isAlive() {
		return recentContacts != null;
	}
}
