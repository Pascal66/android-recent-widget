package org.recentwidget.dao;

import java.util.Date;

import org.recentwidget.EventListBuilder;
import org.recentwidget.R;
import org.recentwidget.model.RecentContact;
import org.recentwidget.model.RecentEvent;

import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

// TODO: What about reminders? http://hi-android.info/src/android/provider/Calendar.java.html
// -> filter events here on whether they have reminders -> no double-fetch
public class CalendarDao extends ContentResolverTemplate {

	private static final String TAG = "RW:CalendarDao";

	private static final String CALENDAR_DESCRIPTION = "description";
	private static final String CALENDAR_EVENT_START = "dtstart";
	private static final String CALENDAR_TITLE = "title";
	private static final String CALENDAR_ID = "_id";
	/**
	 * The image views holding the CallLog icons.
	 */
	static int[] imageMap = new int[] { R.id.contactEventLabel01_0,
			R.id.contactEventLabel02_0, R.id.contactEventLabel03_0 };

	public CalendarDao() {
		super();

		projection = new String[] { CALENDAR_ID, CALENDAR_TITLE,
				CALENDAR_EVENT_START, CALENDAR_DESCRIPTION };
		sortOrder = CALENDAR_EVENT_START + " desc";
	}

	@Override
	public boolean supports(String intentAction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer getResourceForWidget(RecentContact contact) {
		return null;
	}

	@Override
	public int[] getWidgetLabels() {
		return imageMap;
	}

	@Override
	protected long extractEvent(EventListBuilder builder, Cursor cursor) {

		long id = cursor.getLong(cursor.getColumnIndex(CALENDAR_ID));
		String title = cursor.getString(cursor.getColumnIndex(CALENDAR_TITLE));
		long start = cursor
				.getLong(cursor.getColumnIndex(CALENDAR_EVENT_START));
		String description = cursor.getString(cursor
				.getColumnIndex(CALENDAR_DESCRIPTION));

		RecentEvent newEvent = new RecentEvent();
		newEvent.setId(id); // Might be null
		newEvent.setDate(start);
		newEvent.setType(RecentEvent.TYPE_CALENDAR);
		newEvent.setSubType(RecentEvent.TYPE_CALENDAR);
		newEvent.setDetails(description);

		builder.add(context, null, title, null, newEvent);

		Log.v(TAG, "Fetched telephony recent event: " + title + " (" + start
				+ ")");

		return start;
	}

	@Override
	protected String getQuery() {
		return CALENDAR_EVENT_START + " <= ? AND " + CALENDAR_EVENT_START
				+ " > ?";
	}

	@Override
	protected String[] getQueryArgs() {
		Date now = new Date();
		return new String[] { Long.toString(now.getTime()),
				Long.toString(now.getTime() - (DateUtils.DAY_IN_MILLIS * 30)) };
	}

	@Override
	protected int getTargetType() {
		return RecentEvent.TYPE_CALENDAR;
	}

	@Override
	protected Uri getContentUri() {
		// content://com.android.calendar/reminders
		return Uri.parse("content://com.android.calendar/events");
	}

}
