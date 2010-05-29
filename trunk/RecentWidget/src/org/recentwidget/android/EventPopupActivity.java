package org.recentwidget.android;

import org.recentwidget.R;
import org.recentwidget.model.RecentContact;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class EventPopupActivity extends Activity {

	private static final String TAG = "RW:EventPopupActivity";

	// private static final double WIDTH_RATIO = 0.9;
	// private static final int MAX_WIDTH = 640;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		int buttonPressed = getIntent().getIntExtra(
				RecentWidgetProvider.BUTTON_PRESSED, -1);
		if (buttonPressed == -1) {
			Log.w(TAG,
					"Started EventPopupActivity without ButtonPressed extra!");
			finish();
		}

		RecentContact recentContact = RecentWidgetHolder.getRecentEventPressed(
				buttonPressed, getContentResolver());

		if (recentContact == null) {
			Log.w(TAG, "ButtonPressed extra correspond to no RecentEvent!");
			finish();
		}

		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		setContentView(R.layout.eventpopup);

		// Different possible icons: sym_call_incoming ic_dialog_info

		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				android.R.drawable.ic_dialog_info);

		// Set the content

		TextView textView = (TextView) findViewById(R.id.popupText);
		textView.setText(recentContact.getPerson());

		ImageButton actionButton = (ImageButton) findViewById(R.id.popupAction);
		final Intent actionIntent = new Intent(Intent.ACTION_VIEW);
		actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		if (recentContact.hasContactInfo()) {
			// Bring up the dialer since no Contact registered
			actionIntent.setData(Uri.parse("tel:" + recentContact.getNumber()));
		} else {
			// Show the contact page
			actionIntent.setData(ContentUris.withAppendedId(People.CONTENT_URI,
					recentContact.getPersonId()));
		}

		actionButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(actionIntent);
				finish();
			}
		});
	}
}
