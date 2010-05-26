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

		setContentResolver(contentResolver);

		return fetchEvents(recentEvents);
	}

	protected List<RecentEvent> fetchEvents(List<RecentEvent> recentEvents) {

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

		return builder.build();
	}

	public void setContentResolver(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}
}
