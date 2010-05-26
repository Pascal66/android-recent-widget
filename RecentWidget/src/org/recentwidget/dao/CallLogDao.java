package org.recentwidget.dao;

import org.recentwidget.EventListBuilder;
import org.recentwidget.RecentWidgetUtils;

import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.util.Log;

public class CallLogDao extends ContentResolverTemplate {

	private static final String TAG = "RW:CallLogDao";

	public CallLogDao() {
		super();

		contentUri = Calls.CONTENT_URI;
		projection = new String[] { Calls.CACHED_NAME, Calls.NUMBER, Calls.NEW,
				Calls.TYPE, Calls.DATE };
		sortOrder = Calls.DEFAULT_SORT_ORDER;

	}

	protected void extractEvent(EventListBuilder builder, Cursor callsCursor) {

		// Cached name, may not be the name as in the Contacts

		String name = callsCursor.getString(callsCursor
				.getColumnIndex(Calls.CACHED_NAME));

		String number = callsCursor.getString(callsCursor
				.getColumnIndex(Calls.NUMBER));

		// The type of the call (incoming, outgoing or missed).

		int type = callsCursor.getInt(callsCursor.getColumnIndex(Calls.TYPE));

		// The date the call occured in ms since the epoch

		long date = callsCursor.getLong(callsCursor.getColumnIndex(Calls.DATE));

		/*
		 * RecentEvent event = new RecentEvent(); event.setPerson(name);
		 * event.setNumber(number); event.setType(type);
		 */

		Log.v(TAG, "Fetched telephony recent event");

		builder.add(name, number, type, date);
	}

	@Override
	public boolean supports(String intentAction) {
		return RecentWidgetUtils.ACTION_UPDATE_TELEPHONY.equals(intentAction);
	}

}
