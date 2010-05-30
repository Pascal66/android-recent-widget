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

}
