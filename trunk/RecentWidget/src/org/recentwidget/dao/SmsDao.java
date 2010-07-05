package org.recentwidget.dao;

import org.recentwidget.EventListBuilder;
import org.recentwidget.R;
import org.recentwidget.RecentWidgetUtils;
import org.recentwidget.model.RecentContact;
import org.recentwidget.model.RecentEvent;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SmsDao extends ContentResolverTemplate {

	private static final String TAG = "RW:SmsDao";

	/**
	 * The image views holding the SMS icons.
	 */
	static int[] contactSmsMap = new int[] { R.id.contactEventLabel01_1,
			R.id.contactEventLabel02_1, R.id.contactEventLabel03_1 };

	public static final Uri SMS_CONTENT_URI = Uri
			.parse("content://mms-sms/conversations/");

	public SmsDao() {
		super();

		projection = new String[] { "_id", "thread_id", "address", "person",
				"date", "body" };
		sortOrder = "date DESC";

	}

	@Override
	protected Uri getContentUri() {
		return SMS_CONTENT_URI;
	}

	@Override
	protected long extractEvent(EventListBuilder builder, Cursor messageCursor) {

		long threadId = messageCursor.getLong(1);

		String address = messageCursor.getString(2);

		long personId = 0; // BUG: not the right personId:
		// messageCursor.getLong(3);

		long date = messageCursor.getLong(4);

		String body = messageCursor.getString(5);

		// String body = messageCursor.getString(5);
		// Log.d(TAG, address + "--" + personId + "--" + date + "--" + body);

		Log.v(TAG, "Fetched sms recent event: " + address + " (" + date + ")");

		// TODO: How to know if it's incoming or outgoing?

		RecentEvent newEvent = new RecentEvent();
		newEvent.setId(threadId); // Might be null
		newEvent.setDate(date);
		newEvent.setType(RecentEvent.TYPE_SMS);
		newEvent.setSubType(RecentEvent.SUBTYPE_INCOMING);
		newEvent.setDetails(body);

		if (personId == 0) {
			builder.add(context, null, null, address, newEvent);
		} else {
			builder.add(context, personId, null, address, newEvent);
		}

		return date;
	}

	@Override
	public Integer getResourceForWidget(RecentContact contact) {
		// Just show an icon if there was an SMS conversation
		RecentEvent event = contact.getMostRecentEvent(RecentEvent.TYPE_SMS);
		if (event != null) {
			return R.drawable.eclair_sms_72;
		} else {
			return null;
		}
	}

	@Override
	public boolean supports(String intentAction) {
		return RecentWidgetUtils.ACTION_UPDATE_SMS.equals(intentAction);
	}

	@Override
	protected int getTargetType() {
		return RecentEvent.TYPE_SMS;
	}

	@Override
	public int[] getWidgetLabels() {
		return contactSmsMap;
	}
}
