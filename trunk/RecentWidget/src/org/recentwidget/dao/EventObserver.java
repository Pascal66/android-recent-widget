package org.recentwidget.dao;

import java.util.List;

import org.recentwidget.RecentEvent;

import android.content.ContentResolver;
import android.content.Intent;

public interface EventObserver {

	boolean supports(String intentAction);

	List<RecentEvent> update(List<RecentEvent> recentEvents, Intent intent,
			ContentResolver contentResolver);

}
