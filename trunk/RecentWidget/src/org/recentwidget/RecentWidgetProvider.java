/**
 * 
 */
package org.recentwidget;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.Contacts;
import android.provider.CallLog.Calls;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.util.Log;
import android.widget.RemoteViews;

public class RecentWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "RW:RecentWidgetProvider";

	/**
	 * Number of recent events displayed on widget. Might be configurable?
	 */
	static final int maxRetrieved = 4;

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

	static int defaultContactImage = android.R.drawable.picture_frame;

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

		// Note: Labels/Photos are better handled using ContactsContract (API-5)

		if (recentEvents.size() > 0) {

			Log.d(TAG, "Updating widget labels");

			String label = "N/A";

			RemoteViews views = RecentWidgetProvider.buildWidgetView(context);

			for (int i = 0; i < maxRetrieved; i++) {

				RecentEvent recentEvent = recentEvents.get(i);

				// Try to fetch the Contact
				// TODO: Skip it if we already have the info!

				ContentResolver resolver = context.getContentResolver();
				Cursor contactCursor = resolver.query(
						Contacts.Phones.CONTENT_URI, new String[] {
								Phones.PERSON_ID, Phones.DISPLAY_NAME },
						Phones.NUMBER + " = ?", new String[] { recentEvent
								.getNumber() }, Phones.DEFAULT_SORT_ORDER);

				if (contactCursor.getCount() >= 1) {

					// Just take the 1st result even if there are several
					// matches

					contactCursor.moveToFirst();

					label = contactCursor.getString(contactCursor
							.getColumnIndex(Phones.DISPLAY_NAME));

					// Set it, so next time we might not need to repeat this
					// query...

					recentEvent.setPerson(label);

					String personIdAsString = contactCursor
							.getString(contactCursor
									.getColumnIndex(Phones.PERSON_ID));

					recentEvent.setPersonId(Long.parseLong(personIdAsString));

				} else {

					// Defaults to the basic info we got

					if (recentEvent.getPerson() != null) {
						label = recentEvent.getPerson();
					} else if (recentEvent.getNumber() != null) {
						label = recentEvent.getNumber();
					}
				}

				contactCursor.close();

				Log.d(TAG, "Setting button label");

				views.setCharSequence(buttonMap[i], "setText", label);

				if (recentEvent.getPersonId() != null) {

					// Also try to set the picture

					Log.d(TAG, "Setting button photo");

					Bitmap contactPhoto = People.loadContactPhoto(context,
							ContentUris.withAppendedId(People.CONTENT_URI,
									recentEvent.getPersonId()),
							defaultContactImage, null);

					views
							.setBitmap(imageMap[i], "setImageBitmap",
									contactPhoto);
				} else {
					views
							.setImageViewResource(imageMap[i],
									defaultContactImage);
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
	 * Create the RemoteViews and
	 * 
	 * @param context
	 * @return
	 */
	static final RemoteViews buildWidgetView(final Context context) {

		Log.d(TAG, "Creating widget view");

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.recentwidget);

		// What to do when onClick

		Intent defineIntent = new Intent(RecentWidgetUtils.ACTION_SHOW_POPUP);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 // no
				// requestCode
				, defineIntent, Intent.FLAG_ACTIVITY_NEW_TASK // no flags
				);

		// Note: API-7 supports RemoteViews.addView !!!

		for (int buttonId : buttonMap) {
			views.setOnClickPendingIntent(buttonId, pendingIntent);
		}

		return views;

	}
}
