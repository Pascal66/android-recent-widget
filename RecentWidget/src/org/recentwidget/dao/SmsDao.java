package org.recentwidget.dao;

import org.recentwidget.EventListBuilder;
import org.recentwidget.RecentWidgetUtils;
import org.recentwidget.model.RecentEvent;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SmsDao extends ContentResolverTemplate {

	private static final String TAG = "RW:SmsDao";

	public SmsDao() {
		super();

		contentUri = Uri.parse("content://mms-sms/conversations/");
		projection = new String[] { "_id", "thread_id", "address", "person",
				"date", "body" };
		sortOrder = "date DESC";

	}

	protected void extractEvent(EventListBuilder builder, Cursor messageCursor) {

		String address = messageCursor.getString(2);

		long personId = messageCursor.getLong(3);

		long date = messageCursor.getLong(4);

		// String body = messageCursor.getString(5);
		// Log.d(TAG, address + "--" + personId + "--" + date + "--" + body);

		Log.v(TAG, "Fetched sms recent event");

		builder.add(personId, null, address, RecentEvent.TYPE_SMS,
				RecentEvent.SUBTYPE_INCOMING, date);
	}

	@Override
	public boolean supports(String intentAction) {
		return RecentWidgetUtils.ACTION_UPDATE_SMS.equals(intentAction);
	}
}
