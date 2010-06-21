package org.recentwidget.dao;

import java.util.List;

import org.recentwidget.EventListBuilder;
import org.recentwidget.model.RecentContact;

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

	protected abstract long extractEvent(EventListBuilder builder, Cursor cursor);

	protected abstract int getTargetType();

	@Override
	public List<RecentContact> update(List<RecentContact> recentContacts,
			Intent intent, Context context) {

		if (intent != null) {
			Log.d(TAG, "Received broadcasted " + intent.getAction());
		}

		// Note: Not concurrent-friendly!

		// Note: Make sure no unnecessary ArrayLists created in all those
		// params/returns.

		this.context = context;
		this.contentResolver = context.getContentResolver();

		return fetchEvents(recentContacts);
	}

	/**
	 * Only one-time use as the template's content resolver is set to null
	 * afterwards. (this is to avoid any long-lived reference / leaks)
	 * 
	 * Uses the specific Dao extractEvent to traverse (pattern?) the cursor.
	 * 
	 */
	protected List<RecentContact> fetchEvents(List<RecentContact> recentContacts) {

		// The builder is useful in order for the business logic to be stored
		// elsewhere.

		EventListBuilder builder = new EventListBuilder(recentContacts);

		// Do not use managedQuery() because we will unload it ourselves

		Cursor cursor = contentResolver.query(contentUri, projection, null,
				null, sortOrder);

		try {

			if (cursor.moveToFirst()) {

				while (!cursor.isAfterLast()) {

					// "extract" actually adds to the list

					long eventDate = extractEvent(builder, cursor);

					// At this point, the event was already added. Just check if
					// next event should be fetched; or do we have enough events
					// already.

					if (builder.isFull(eventDate, getTargetType())) {
						break;
					}

					cursor.moveToNext();
				}
			}

		} finally {
			cursor.close();
		}

		// Avoid memory leaks
		// http://www.curious-creature.org/2008/12/18/avoid-memory-leaks-on-android/

		contentResolver = null;
		context = null;

		return builder.build();
	}

}