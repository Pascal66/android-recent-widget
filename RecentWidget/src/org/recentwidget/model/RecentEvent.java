package org.recentwidget.model;

import java.io.Serializable;

import android.provider.CallLog;

public class RecentEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	// Constant strings = easier than polymorphism

	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_SMS = 1;
	public static final int TYPE_CALL = 2;

	public static final int SUBTYPE_UNKNOWN = 0;
	public static final int SUBTYPE_INCOMING = CallLog.Calls.INCOMING_TYPE;
	public static final int SUBTYPE_OUTGOING = CallLog.Calls.OUTGOING_TYPE;
	public static final int SUBTYPE_MISSED = CallLog.Calls.MISSED_TYPE;

	private int type = TYPE_UNKNOWN;
	private int subType = SUBTYPE_UNKNOWN;
	private long date;
	private Long id;

	private String details;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public int getSubType() {
		return subType;
	}

	public void setSubType(int subType) {
		this.subType = subType;
	}

	@Override
	public String toString() {
		return "RecentEvent [date=" + date + ", subType=" + subType + ", type="
				+ type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (date ^ (date >>> 32));
		result = prime * result + subType;
		result = prime * result + type;
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
		RecentEvent other = (RecentEvent) obj;
		if (date != other.date)
			return false;
		if (subType != other.subType)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public void setDetails(String details) {
		if (details != null && details.trim().length() > 0) {
			this.details = details;
		}
	}

	public String getDetails() {
		return details;
	}
}
