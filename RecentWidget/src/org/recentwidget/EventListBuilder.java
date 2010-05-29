package org.recentwidget;

import java.util.ArrayList;
import java.util.List;

import org.recentwidget.model.RecentContact;
import org.recentwidget.model.RecentEvent;

public class EventListBuilder {

	/**
	 * Number of recent events displayed on widget. Might be configurable?
	 */
	static final int maxRetrieved = 4;

	private static final String TAG = "EventListBuilder";

	// Temporary reference/variable
	private List<RecentContact> contacts;

	public EventListBuilder(List<RecentContact> previousContacts) {
		if (previousContacts != null) {
			this.contacts = previousContacts;
		} else {
			// May happen if the RWHolder was killed
			this.contacts = new ArrayList<RecentContact>(maxRetrieved);
		}
	}

	public void add(Long personId, String name, String number, int type,
			int subtype, long date) {

		// Unnecessary object creation?!?

		RecentContact contact = new RecentContact();
		contact.setNumber(number);
		contact.setPerson(name);
		contact.setPersonId(personId);

		// i = index of the contact to be removed/added

		int i = contacts.indexOf(contact);

		if (i >= 0) {
			contact = contacts.remove(i);
		}

		// Add the event, if needed

		RecentEvent newEvent = new RecentEvent();
		newEvent.setDate(date);
		newEvent.setType(type);
		newEvent.setSubType(subtype);

		contact.addEvent(newEvent);

		// Put this contact at the right place

		// i = index where to add the new/updated contact

		i = contacts.size();

		for (int a = 0; a < i; a++) {
			if (contacts.get(a).getMostRecentDate() < contact
					.getMostRecentDate()) {
				i = a;
				break;
			}

		}

		contacts.add(i, contact);

		// Truncate a bit (try to avoid object creation)

		cleanOldContacts();
	}

	private void cleanOldContacts() {

		// Truncate a bit (try to avoid object creation)

		while (contacts.size() > maxRetrieved) {
			contacts.remove(contacts.size() - 1);
		}
	}

	public List<RecentContact> build() {
		// Just return the list...
		// Note: Do we fetch the picture and everything now?
		return contacts;
	}

	/**
	 * @return Whether the list is already full (i.e. do we need to fetch more
	 *         entries?)
	 */
	public boolean isFull() {
		// TODO: WRONG! Should check whether the event has already been
		// recorded... or that the timestamp of the newly added event is older
		// than the older event in the list.
		return contacts.size() >= maxRetrieved;
	}

}
