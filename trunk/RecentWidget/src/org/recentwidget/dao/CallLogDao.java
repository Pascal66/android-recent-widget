package org.recentwidget.dao;

import org.recentwidget.EventListBuilder;
import org.recentwidget.RecentWidgetUtils;
import org.recentwidget.model.RecentEvent;

import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.util.Log;

public class CallLogDao extends ContentResolverTemplate {

	private static final String TAG = "RW:CallLogDao";

	public CallLogDao() {
		super();

		contentUri = Calls.CONTENT_URI;
		projection = new String[] { Calls._ID, Calls.CACHED_NAME, Calls.NUMBER,
				Calls.NEW, Calls.TYPE, Calls.DATE };
		sortOrder = Calls.DEFAULT_SORT_ORDER;

	}

	protected long extractEvent(EventListBuilder builder, Cursor callsCursor) {

		// Cached name, may not be the name as in the Contacts

		String name = callsCursor.getString(callsCursor
				.getColumnIndex(Calls.CACHED_NAME));

		String number = callsCursor.getString(callsCursor
				.getColumnIndex(Calls.NUMBER));

		// The unique CallLog id

		long id = callsCursor.getLong(callsCursor.getColumnIndex(Calls._ID));

		// The type of the call (incoming, outgoing or missed).

		int type = callsCursor.getInt(callsCursor.getColumnIndex(Calls.TYPE));

		// The date the call occured in ms since the epoch

		long date = callsCursor.getLong(callsCursor.getColumnIndex(Calls.DATE));

		/*
		 * RecentEvent event = new RecentEvent(); event.setPerson(name);
		 * event.setNumber(number); event.setType(type);
		 */

		Log.v(TAG, "Fetched telephony recent event: " + name + " (" + date
				+ ")");

		builder.add(null, name, number, id, RecentEvent.TYPE_CALL, type, date);

		return date;
	}

	@Override
	public boolean supports(String intentAction) {
		// return
		// RecentWidgetUtils.ACTION_UPDATE_TELEPHONY.equals(intentAction);
		return RecentWidgetUtils.ACTION_UPDATE_CALL.equals(intentAction);
	}

	@Override
	protected int getTargetType() {
		return RecentEvent.TYPE_CALL;
	}
}
