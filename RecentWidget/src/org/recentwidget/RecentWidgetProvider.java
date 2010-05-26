/**
 * 
 */
package org.recentwidget;

import java.util.ArrayList;
import java.util.List;

import org.recentwidget.dao.CallLogDao;
import org.recentwidget.dao.EventObserver;
import org.recentwidget.dao.SmsDao;

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
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.util.Log;
import android.widget.RemoteViews;

public class RecentWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "RW:RecentWidgetProvider";

	public static final String BUTTON_PRESSED = "org.recentwidget.BUTTON_PRESSED";

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
	static List<RecentEvent> recentEvents;

	static EventObserver[] eventObservers = new EventObserver[] {
			new CallLogDao(), new SmsDao() };

	@Override
	// Note: not called when using a ConfigurationActivity
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		// TODO: WordWidget says that updates should be done by a service?

	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		boolean consumed = false;

		for (EventObserver observer : eventObservers) {
			if (observer.supports(action)) {

				// Retrieve the last specific events and store in cache

				recentEvents = observer.update(recentEvents, intent, context
						.getContentResolver());

				// Update the widget

				updateWidgetLabels(context, recentEvents);

				consumed = true;
				break;
			}
		}

		if (!consumed) {

			// TODO: workaround for onDelete in 1.5
			// http://groups.google.com/group/android-developers/msg/e405ca19df2170e2?pli=1

			super.onReceive(context, intent);
		}
	}

	private void updateWidgetLabels(Context context, List<RecentEvent> events) {

		// Update labels on widget

		// Note: Labels/Photos are better handled using ContactsContract (API-5)

		if (events.size() > 0) {

			Log.d(TAG, "Updating widget labels");

			String label = "N/A";

			RemoteViews views = RecentWidgetProvider.buildWidgetView(context);

			for (int i = 0; i < events.size(); i++) {

				RecentEvent recentEvent = events.get(i);

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

		// Note: API-7 supports RemoteViews.addView !!!

		// What to do when onClick

		for (int buttonId : buttonMap) {

			Intent defineIntent = new Intent(
					RecentWidgetUtils.ACTION_SHOW_POPUP);

			// TODO: bad idea, give index of RecentEvent rather?
			defineIntent.putExtra(BUTTON_PRESSED, buttonId);

			// buttonId as requestCode = hack to make sure that each intent is
			// created as a new instance (instead of re-used or overwritten)

			PendingIntent pendingIntent = PendingIntent.getActivity(context,
					buttonId // no
					// requestCode
					, defineIntent, Intent.FLAG_ACTIVITY_NEW_TASK // no flags
					);

			views.setOnClickPendingIntent(buttonId, pendingIntent);

		}

		return views;

	}

	protected static RecentEvent getRecentEventPressed(int buttonPressed,
			ContentResolver contentResolver) {

		boolean found = false;
		int index = 0;

		for (index = 0; index < buttonMap.length; index++) {
			if (buttonMap[index] == buttonPressed) {
				found = true;
				break;
			}
		}

		if (recentEvents == null) {

			// Button pressed but no recentEvent attached! Surely
			// garbage-collected so let's create a new list...

			recentEvents = new ArrayList<RecentEvent>();

			for (EventObserver observer : eventObservers) {
				recentEvents = observer.update(recentEvents, null,
						contentResolver);
			}
		}

		if (found && recentEvents.size() - 1 >= index) {
			return recentEvents.get(index);
		} else {
			Log.e(TAG, "Button pressed but no corresponding recent events.");
			return null;
		}
	}
}
