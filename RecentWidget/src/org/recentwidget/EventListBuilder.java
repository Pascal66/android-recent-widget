package org.recentwidget;

import java.util.ArrayList;
import java.util.List;

import org.recentwidget.android.WidgetPreferenceActivity;
import org.recentwidget.model.RecentContact;
import org.recentwidget.model.RecentEvent;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class EventListBuilder {

	public static final int DEFAULT_MAX_RETRIEVED = 21;
	/**
	 * Number of recent events displayed on widget.
	 */
	private final int maxRetrieved;

	private static final String TAG = "EventListBuilder";

	// Temporary reference/variable
	private List<RecentContact> contacts;

	public EventListBuilder(Context context,
			List<RecentContact> previousContacts) {

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		maxRetrieved = sharedPreferences.getInt(
				WidgetPreferenceActivity.PREF_MAX_RETRIEVED,
				DEFAULT_MAX_RETRIEVED);

		if (previousContacts != null) {
			this.contacts = previousContacts;
		} else {
			// May happen if the RWHolder was killed
			this.contacts = new ArrayList<RecentContact>(maxRetrieved);
		}
	}

	public void add(Context context, Long personId, String name, String number,
			RecentEvent newEvent) {

		// Unnecessary object creation?!?

		RecentContact contact = new RecentContact();
		contact.setNumber(number);
		contact.setPerson(name);
		contact.setPersonId(personId);

		// Fetch more information, so we can group events on a given contact and
		// so we have all the info to be displayed later on.

		contact = RecentWidgetUtils.CONTACTS_API.fetchContactInfo(context,
				contact);

		// i = index of the contact to be removed/added

		int i = contacts.indexOf(contact);

		if (i >= 0) {
			contact = contacts.remove(i);

			// Contact removed might have less information than the info given
			// as parameters. Merge them.

			if (contact.getNumber() == null && number != null) {
				contact.setNumber(number);
			}
			if (contact.getPerson() == null && name != null) {
				contact.setPerson(name);
			}
			if (!contact.hasContactInfo() && personId != null) {
				contact.setPersonId(personId);
			}
		}

		// Add the event, if needed

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
	 * Presumes that the events are added in inverse chronological order.
	 * 
	 * @param type
	 * @param eventDate
	 * @return Whether the list is already full (i.e. do we need to fetch more
	 *         entries?)
	 */
	public boolean isFull(long eventDate, int type) {

		if (contacts.size() < maxRetrieved) {

			Log.d(TAG, "List has not reached max size yet.");
			return false;

		} else {

			// Check whether the timestamp of the newly added event is older
			// than the oldest event in the list.

			boolean hasOlderEvent = false;

			for (RecentContact contact : contacts) {
				// TODO: If the event relates to a contact in the list, then
				// allow more...
				if (contact.oldestEventDate < eventDate) {
					hasOlderEvent = true;
					break;
				}
			}

			// If no older event, the list must be full.

			return !hasOlderEvent;
		}
	}

}
