package org.recentwidget.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecentContact implements Serializable {

	private static final long serialVersionUID = 1L;

	private String person;
	private String number;
	private Long personId = null;

	/**
	 * The most recent event date for this contact.
	 */
	private long mostRecentDate;

	/**
	 * The least recent event date for this contact.
	 */
	public long oldestEventDate = Long.MAX_VALUE;

	// Are arrays more appropriate for mobile development?
	private final List<RecentEvent> recentEvents = new ArrayList<RecentEvent>();

	/**
	 * @param event
	 *            The event to add, if not already present.
	 * @return Whether the event was added.
	 */
	public boolean addEvent(RecentEvent event) {
		if (!recentEvents.contains(event)) {
			recentEvents.add(event);
			if (event.getDate() > mostRecentDate) {
				mostRecentDate = event.getDate();
			}
			if (event.getDate() < oldestEventDate) {
				oldestEventDate = event.getDate();
			}
			return true;
		}
		return false;
	}

	public List<RecentEvent> getRecentEvents() {
		return recentEvents;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Long getPersonId() {
		return personId;
	}

	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	public long getMostRecentDate() {
		return mostRecentDate;
	}

	/**
	 * @return Whether the contact is an actual phonebook contact (or just a
	 *         number).
	 */
	public boolean hasContactInfo() {
		return personId != null && personId > 0;
	}

	public String getDisplayName() {
		if (person != null) {
			return person;
		} else if (number != null) {
			return number;
		} else {
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		// No hashcode possible because we might only have 1 of those 3
		// information.
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecentContact other = (RecentContact) obj;
		if (personId != null && other.personId != null) {
			return personId.equals(other.personId);
		}
		if (number != null && other.number != null) {
			// Shortcut: both numbers equal -> true
			return number.equals(other.number);
		}
		if (person != null && other.person != null) {
			return person.equals(other.person);
		}
		return false;
	}

	@Override
	public String toString() {
		return "RecentContact [number=" + number + ", person=" + person
				+ ", personId=" + personId + ", mostRecentDate="
				+ mostRecentDate + ", " + recentEvents.size() + " event(s)]";
	}

	public RecentEvent getMostRecentEvent(int type) {
		// TODO: Since it's a list, the first one is always the most recent one?
		long mostRecentDate = 0;
		RecentEvent mostRecentEvent = null;
		for (RecentEvent recentEvent : recentEvents) {
			if (recentEvent.getType() == type
					&& recentEvent.getDate() > mostRecentDate) {
				mostRecentDate = recentEvent.getDate();
				mostRecentEvent = recentEvent;
			}
		}
		return mostRecentEvent;
	}

}
