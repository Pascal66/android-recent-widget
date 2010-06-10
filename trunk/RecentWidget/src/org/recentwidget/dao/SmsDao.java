package org.recentwidget.dao;

import org.recentwidget.EventListBuilder;
import org.recentwidget.RecentWidgetUtils;
import org.recentwidget.model.RecentEvent;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SmsDao extends ContentResolverTemplate {

	private static final String TAG = "RW:SmsDao";

	public static final Uri SMS_CONTENT_URI = Uri
			.parse("content://mms-sms/conversations/");

	public SmsDao() {
		super();

		contentUri = SMS_CONTENT_URI;
		projection = new String[] { "_id", "thread_id", "address", "person",
				"date", "body" };
		sortOrder = "date DESC";

	}

	protected long extractEvent(EventListBuilder builder, Cursor messageCursor) {

		long threadId = messageCursor.getLong(1);

		String address = messageCursor.getString(2);

		long personId = messageCursor.getLong(3);

		long date = messageCursor.getLong(4);

		// String body = messageCursor.getString(5);
		// Log.d(TAG, address + "--" + personId + "--" + date + "--" + body);

		Log.v(TAG, "Fetched sms recent event: " + address + " (" + date + ")");

		if (personId == 0) {
			builder.add(null, null, address, threadId, RecentEvent.TYPE_SMS,
					RecentEvent.SUBTYPE_INCOMING, date);
		} else {
			builder.add(personId, null, address, threadId,
					RecentEvent.TYPE_SMS, RecentEvent.SUBTYPE_INCOMING, date);
		}

		return date;
	}

	@Override
	public boolean supports(String intentAction) {
		return RecentWidgetUtils.ACTION_UPDATE_SMS.equals(intentAction);
	}

	@Override
	protected int getTargetType() {
		return RecentEvent.TYPE_SMS;
	}
}
