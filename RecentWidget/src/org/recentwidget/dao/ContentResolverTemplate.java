package org.recentwidget.dao;

import java.util.List;

import org.recentwidget.EventListBuilder;
import org.recentwidget.RecentEvent;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public abstract class ContentResolverTemplate implements EventObserver {

	private static final String TAG = "RW:ContentResolverTplt";

	protected Uri contentUri;

	protected String[] projection;

	protected String sortOrder;

	protected Context context;

	private ContentResolver contentResolver;

	protected abstract void extractEvent(EventListBuilder builder, Cursor cursor);

	@Override
	public List<RecentEvent> update(List<RecentEvent> recentEvents,
			Intent intent, ContentResolver contentResolver) {

		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Received broadcasted " + intent.getAction());
		}

		// Note: Not concurrent-friendly!

		// Note: Make sure no unnecessary ArrayLists created in all those
		// params/returns.

		setContentResolver(contentResolver);

		return fetchEvents(recentEvents);
	}

	/**
	 * Only one-time use as the template's content resolver is set to null
	 * afterwards. (this is to avoid any long-lived reference / leaks)
	 * 
	 * Uses the specific Dao extractEvent to traverse (pattern?) the cursor.
	 * 
	 */
	protected List<RecentEvent> fetchEvents(List<RecentEvent> recentEvents) {

		// The builder is useful in order for the business logic to be stored
		// elsewhere.

		EventListBuilder builder = new EventListBuilder(recentEvents);

		// Do not use managedQuery() because we will unload it ourselves

		Cursor cursor = contentResolver.query(contentUri, projection, null,
				null, sortOrder);

		// Note: we might not want to retrieve all the maxRetrieved events
		// (maybe just the last one is enough?)... depends on the current
		// state of the recentEvents list -> the builder will tell...

		try {

			if (cursor.moveToFirst()) {

				// TODO: Wrong condition: should stop when the date of the
				// fetched
				// event is smaller than the small date in the builder list

				while (!cursor.isAfterLast() && !builder.isFull()) {

					extractEvent(builder, cursor);

					cursor.moveToNext();
				}
			}

		} catch (Exception e) {
			cursor.close();
		}

		// Avoid memory leaks
		// http://www.curious-creature.org/2008/12/18/avoid-memory-leaks-on-android/

		contentResolver = null;

		return builder.build();
	}

	public void setContentResolver(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}
}
