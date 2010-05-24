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

	public void add(String name, String number, int type) {

		boolean alreadyExists = false;

		// Check whether the person is already there, to avoid duplicates

		for (RecentEvent recentEvent : events) {
			if (recentEvent.getNumber() != null
					&& recentEvent.getNumber().equals(number)) {

				Log.v(TAG, "Number already in list");
				alreadyExists = true;

				// TODO: How to track that?
			}
		}

		if (!alreadyExists) {

			RecentEvent newEvent = new RecentEvent();
			newEvent.setNumber(number);
			newEvent.setPerson(name);
			newEvent.setType(type);

			events.add(0, newEvent);

		}
	}

	public List<RecentEvent> build() {
		// Just return the list...
		// TODO Do we fetch the picture and everything now?
		return events;
	}

	/**
	 * @return Whether the list is already full (do we need to fetch more
	 *         entries?)
	 */
	public boolean isFull() {
		return events.size() >= maxRetrieved;
	}

}
