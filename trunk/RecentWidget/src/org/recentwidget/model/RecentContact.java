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

	// Are arrays more appropriate for mobile development?
	private List<RecentEvent> recentEvents = new ArrayList<RecentEvent>();

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
		} else {
			return number;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result
				+ ((personId == null) ? 0 : personId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecentContact other = (RecentContact) obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		if (number.equals(other.number)) {
			// Shortcut: both numbers equal -> true
			return true;
		}
		if (personId == null) {
			if (other.personId != null)
				return false;
		} else if (!personId.equals(other.personId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RecentContact [number=" + number + ", person=" + person
				+ ", personId=" + personId + ", mostRecentDate="
				+ mostRecentDate + ", " + recentEvents.size() + " event(s)]";
	}

	public RecentEvent getOldestEvent(int type) {

		if (recentEvents != null) {

			long minDate = Long.MAX_VALUE;
			int minIndex = 0;

			for (int i = 0; i < recentEvents.size(); i++) {

				RecentEvent recentEvent = recentEvents.get(i);

				if (recentEvent.getType() == type
						&& recentEvent.getDate() < minDate) {
					minDate = recentEvent.getDate();
					minIndex = i;
				}

			}

			if (minDate != Long.MAX_VALUE) {
				return recentEvents.get(minIndex);
			}

		}

		return null;
	}
}
