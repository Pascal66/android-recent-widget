package org.recentwidget.dao;

import java.util.List;

import org.recentwidget.model.RecentContact;

import android.content.ContentResolver;
import android.content.Intent;

public interface EventObserver {

	boolean supports(String intentAction);

	List<RecentContact> update(List<RecentContact> recentContacts,
			Intent intent, ContentResolver contentResolver);

}
