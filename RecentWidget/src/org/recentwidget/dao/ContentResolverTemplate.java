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

public abstract class ContentResolverTemplate {

	private static final String TAG = "RW:ContentResolverTemplate";

	protected Uri contentUri;

	protected String[] projection;

	protected String sortOrder;

	protected Context context;

	protected abstract void extractEvent(EventListBuilder builder, Cursor cursor);

	public List<RecentEvent> update(List<RecentEvent> recentEvents,
			Intent intent, Context context) {

		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Received broadcasted " + intent.getAction());
		}

		// Note: Not concurrent-friendly!

		setContext(context);

		return fetchEvents(recentEvents);
	}

	protected List<RecentEvent> fetchEvents(List<RecentEvent> recentEvents) {

		EventListBuilder builder = new EventListBuilder(recentEvents);

		Cursor cursor = getCursor();

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

	public void setContext(Context context) {
		this.context = context;
	}

	private Cursor getCursor() {

		// Do not use managedQuery() because we will unload it ourselves

		ContentResolver dataProvider = context.getContentResolver();

		Cursor cursor = dataProvider.query(contentUri, projection, null, null,
				sortOrder);

		return cursor;
	}

}
