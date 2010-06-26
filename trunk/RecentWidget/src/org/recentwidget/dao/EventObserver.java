package org.recentwidget.dao;

import java.util.List;

import org.recentwidget.model.RecentContact;

import android.content.Context;
import android.content.Intent;

public interface EventObserver {

	boolean supports(String intentAction);

	List<RecentContact> update(List<RecentContact> recentContacts,
			Intent intent, Context context);

	Integer getResourceForWidget(RecentContact contact);

	int[] getWidgetLabels();

}
