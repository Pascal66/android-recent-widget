package org.recentwidget.android;

import org.recentwidget.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

// Just used as an About page...
public class RecentWidgetMainActivity extends Activity {

	private static final String TAG = "RW:RecentWidgetMainActivity";

	/**
	 * Called when the activity is first created or when the widget is created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Bind the action for the Close button.

		findViewById(R.id.closeButton).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});

		// Hidden feature: clears the cache when this screen is up

		RecentWidgetHolder.clean();

	}

}