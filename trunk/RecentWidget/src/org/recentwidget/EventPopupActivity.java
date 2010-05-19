package org.recentwidget;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;

public class EventPopupActivity extends Activity {

	private static final double WIDTH_RATIO = 0.9;
	private static final int MAX_WIDTH = 640;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.eventpopup);

		// Resize to make it look like a popup
		// (taken from SMSPopup::SmsPopupActivity)

		Display d = getWindowManager().getDefaultDisplay();

		View mainLayout = findViewById(R.id.eventpopupMain);

		int width = d.getWidth() > MAX_WIDTH ? MAX_WIDTH
				: (int) (d.getWidth() * WIDTH_RATIO);
		mainLayout.setMinimumWidth(width);
		mainLayout.invalidate();

	}
}
