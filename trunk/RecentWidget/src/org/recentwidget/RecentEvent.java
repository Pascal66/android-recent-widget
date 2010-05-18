package org.recentwidget;

import java.io.Serializable;

public class RecentEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private String person;
	private String number;
	private int type;

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

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
