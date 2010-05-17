/**
 * 
 */
package org.recentwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * @author Administrator
 * 
 */
public class RecentWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// Note: not called when using a ConfigurationActivity
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: workaround for onDelete in 1.5
		// http://groups.google.com/group/android-developers/msg/e405ca19df2170e2?pli=1
		super.onReceive(context, intent);
	}
}
