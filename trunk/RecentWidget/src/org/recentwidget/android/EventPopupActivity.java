package org.recentwidget.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.recentwidget.R;
import org.recentwidget.model.RecentContact;
import org.recentwidget.model.RecentEvent;

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
import android.widget.TableLayout;
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

		// Set the header content

		TextView textView = (TextView) findViewById(R.id.popupText);
		textView.setText(recentContact.getDisplayName());

		ImageButton actionButton = (ImageButton) findViewById(R.id.popupAction);
		final Intent actionIntent = new Intent(Intent.ACTION_VIEW);
		actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		if (recentContact.hasContactInfo()) {
			// Show the contact page
			actionIntent.setData(ContentUris.withAppendedId(People.CONTENT_URI,
					recentContact.getPersonId()));
		} else {
			// Bring up the dialer since no Contact registered
			actionIntent.setData(Uri.parse("tel:" + recentContact.getNumber()));
		}

		actionButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(actionIntent);
				finish();
			}
		});

		// Show the recent events
		// TODO: Make it more dynamic... less magic-number based.

		TableLayout telLayout = (TableLayout) findViewById(R.id.popupTable1);
		TableLayout smsLayout = (TableLayout) findViewById(R.id.popupTable2);

		for (RecentEvent recentEvent : recentContact.getRecentEvents()) {

			DateFormat dateFormat = SimpleDateFormat
					.getDateInstance(DateFormat.FULL);
			String date = dateFormat.format(new Date(recentEvent.getDate()));

			// No buttons yet, so we can just add a TextView instead of a
			// TextRow...

			TextView eventText = new TextView(this);
			eventText.setText("Event: " + date + " (type "
					+ recentEvent.getSubType() + ")");

			switch (recentEvent.getType()) {
			case RecentEvent.TYPE_CALL:
				telLayout.addView(eventText);
				break;
			case RecentEvent.TYPE_SMS:
				smsLayout.addView(eventText);
				break;
			default:
				break;
			}

		}
	}
}
