package org.recentwidget;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class EventPopupActivity extends Activity {

	// private static final double WIDTH_RATIO = 0.9;
	// private static final int MAX_WIDTH = 640;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		setContentView(R.layout.eventpopup);

		// Different icons: sym_call_incoming ic_dialog_info
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				android.R.drawable.ic_dialog_info);
	}

}
