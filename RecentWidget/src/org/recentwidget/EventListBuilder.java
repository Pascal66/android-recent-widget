package org.recentwidget;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class EventListBuilder {

	/**
	 * Number of recent events displayed on widget. Might be configurable?
	 */
	static final int maxRetrieved = 4;

	private static final String TAG = "EventListBuilder";

	// Temporary reference/variable
	private List<RecentEvent> events;

	public EventListBuilder(List<RecentEvent> events) {
		if (events != null) {
			this.events = events;
		} else {
			// May happen if the RWProvider was garbage-collected
			this.events = new ArrayList<RecentEvent>(maxRetrieved);
		}
	}

	public void add(String name, String number, int type, long date) {

		if (!isNumberInList(number)) {

			RecentEvent newEvent = new RecentEvent();
			newEvent.setNumber(number);
			newEvent.setPerson(name);
			newEvent.setType(type);

			// TODO: Should compare timestamps!

			events.add(0, newEvent);

		}

		// Truncate a bit (try to avoid object creation)

		cleanOldEvents();
	}

	public void add(long personId, String number, int type, long date) {

		// TODO: Copy/paste! How to refactor?

		if (!isNumberInList(number)) {

			RecentEvent newEvent = new RecentEvent();
			newEvent.setNumber(number);
			newEvent.setPersonId(personId);
			newEvent.setType(type);

			// TODO: Should compare timestamps!

			events.add(0, newEvent);

		}

		// Truncate a bit (try to avoid object creation)

		cleanOldEvents();
	}

	private void cleanOldEvents() {

		// Truncate a bit (try to avoid object creation)

		while (events.size() > maxRetrieved) {
			events.remove(events.size() - 1);
		}
	}

	private boolean isNumberInList(String number) {

		// Check whether the person is already there, to avoid duplicates

		for (RecentEvent recentEvent : events) {

			// TODO: Also check _id?!

			if (recentEvent.getNumber() != null
					&& recentEvent.getNumber().equals(number)) {

				Log.v(TAG, "Number already in list");

				return true;

				// TODO: How to track that?
				// + move the entry to the first place
			}
		}
		return false;
	}

	public List<RecentEvent> build() {
		// Just return the list...
		// Note: Do we fetch the picture and everything now?
		return events;
	}

	/**
	 * @return Whether the list is already full (i.e. do we need to fetch more
	 *         entries?)
	 */
	public boolean isFull() {
		return events.size() >= maxRetrieved;
	}

}
