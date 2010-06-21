package org.recentwidget.android;

import org.recentwidget.R;
import org.recentwidget.RecentWidgetUtils;
import org.recentwidget.dao.SmsDao;
import org.recentwidget.model.RecentContact;
import org.recentwidget.model.RecentEvent;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class EventPopupActivity extends Activity {

	private static final String TAG = "RW:EventPopupActivity";

	private GestureDetector gestureDetector;

	private int buttonPressed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Get the contact to be displayed

		buttonPressed = getIntent().getIntExtra(
				RecentWidgetProvider.BUTTON_PRESSED, -1);

		if (buttonPressed == -1) {
			Log.w(TAG,
					"Started EventPopupActivity without ButtonPressed extra!");
			finish();
		}

		RecentContact recentContact = RecentWidgetHolder.getRecentEventPressed(
				buttonPressed, this);

		if (recentContact == null) {
			Log.w(TAG, "ButtonPressed extra correspond to no RecentEvent!");
			finish();
			return;
		}

		// Setup the dialog window

		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		setContentView(R.layout.eventpopup);

		if (recentContact.getPersonId() != null) {
			Bitmap contactPhoto = RecentWidgetUtils.CONTACTS_API
					.loadContactPhoto(this, recentContact);
			BitmapDrawable contactDrawable = new BitmapDrawable(contactPhoto);

			getWindow().setFeatureDrawable(Window.FEATURE_LEFT_ICON,
					contactDrawable);
		} else {
			// Display default icon
			getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
					RecentWidgetProvider.defaultContactImage);
		}

		// Set the header content

		TextView textView = (TextView) findViewById(R.id.popupText);
		textView.setText(recentContact.getDisplayName());

		if (recentContact.hasContactInfo()) {

			// Contact button
			bindIntentToButton(R.id.popupAction, ContentUris.withAppendedId(
					RecentWidgetUtils.CONTACTS_API.contentUri, recentContact
							.getPersonId()));
			// Show the Contact Info icon
			((ImageButton) findViewById(R.id.popupAction))
					.setImageResource(R.drawable.ic_launcher_contacts);
		} else {

			// Bring up the dialer since no Contact registered
			bindIntentToButton(R.id.popupAction, Uri.parse("tel:"
					+ recentContact.getNumber()));
			// The image is already the dialer icon
		}

		// Call Log button

		RecentEvent callLogEvent = recentContact
				.getMostRecentEvent(RecentEvent.TYPE_CALL);
		if (callLogEvent != null) {
			bindIntentToButton(R.id.popupTableAction1, ContentUris
					.withAppendedId(Calls.CONTENT_URI, callLogEvent.getId()));
		} else if (recentContact.getNumber() != null) {
			// Provide a way to dial anyways
			bindIntentToButton(R.id.popupTableAction1, Uri.parse("tel:"
					+ recentContact.getNumber()));
		}

		// SMS button

		RecentEvent smsEvent = recentContact
				.getMostRecentEvent(RecentEvent.TYPE_SMS);
		if (smsEvent != null) {
			bindIntentToButton(R.id.popupTableAction2, ContentUris
					.withAppendedId(SmsDao.SMS_CONTENT_URI, smsEvent.getId()));
		} else if (recentContact.getNumber() != null) {
			// Provide a way to compose a new message
			bindIntentToButton(R.id.popupTableAction2, Uri.parse("sms:"
					+ recentContact.getNumber()));
		}

		// Show the recent events
		// TODO: Make it more dynamic... less magic-number based.

		TableLayout telLayout = (TableLayout) findViewById(R.id.popupTable1);
		TableLayout smsLayout = (TableLayout) findViewById(R.id.popupTable2);

		for (RecentEvent recentEvent : recentContact.getRecentEvents()) {

			Log.d(TAG, "Drawing event: " + recentEvent);

			CharSequence date = DateFormat.format("MMM dd, h:mmaa", recentEvent
					.getDate());

			TableRow row = new TableRow(this);
			row.setGravity(Gravity.CENTER_VERTICAL);

			TextView eventText = new TextView(this);
			eventText.setText(date);

			row.addView(eventText);

			Integer subtypeIconRef = null;

			switch (recentEvent.getType()) {
			case RecentEvent.TYPE_CALL:
				switch (recentEvent.getSubType()) {
				case RecentEvent.SUBTYPE_INCOMING:
					subtypeIconRef = R.drawable.ic_incoming_call;
					break;
				case RecentEvent.SUBTYPE_MISSED:
					subtypeIconRef = R.drawable.ic_missed_call;
					break;
				case RecentEvent.SUBTYPE_OUTGOING:
					subtypeIconRef = R.drawable.ic_outgoing_call;
					break;
				default:
					break;
				}
				if (subtypeIconRef != null) {
					ImageView subtypeIcon = new ImageView(this);
					subtypeIcon.setPadding(3, 0, 0, 0);
					subtypeIcon.setImageResource(subtypeIconRef);
					row.addView(subtypeIcon);
				}
				telLayout.addView(row);
				break;
			case RecentEvent.TYPE_SMS:
				smsLayout.addView(row);
				break;
			default:
				break;
			}

		}

		// Fling detector

		gestureDetector = new GestureDetector(new SimpleOnGestureListener() {

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {

				if (Math.abs(velocityX) > Math.abs(velocityY)) {
					int offset = 0;
					// Horizontal fling
					if (velocityX > 0) {
						// right fling (show previous)
						offset = -1;
					} else {
						// left fling (show next)
						offset = +1;
					}

					int simulatedButtonPosition = RecentWidgetProvider
							.getButtonPosition(buttonPressed)
							+ offset;

					if (simulatedButtonPosition < 0) {

						// If touched on first in the list, we need to decrease
						// the page

						if (!RecentWidgetHolder.previousPage()) {
							// Ignore if we are already on the first page
							simulatedButtonPosition = -1;
						} else {
							// start from last
							simulatedButtonPosition = RecentWidgetProvider.numContactsDisplayed - 1;
						}

						RecentWidgetHolder
								.updateWidgetLabels(getApplicationContext());

					} else if (simulatedButtonPosition >= RecentWidgetProvider.numContactsDisplayed) {

						// If touching the last one, next page please.

						if (!RecentWidgetHolder.nextPage(null)) {
							simulatedButtonPosition = -1;
						} else {
							simulatedButtonPosition = 0;
						}

						RecentWidgetHolder
								.updateWidgetLabels(getApplicationContext());
					}

					finish();

					if (simulatedButtonPosition != -1) {

						simulatedButtonPosition %= RecentWidgetProvider.numContactsDisplayed;

						Intent intent = new Intent(
								RecentWidgetUtils.ACTION_SHOW_POPUP);
						intent
								.putExtra(
										RecentWidgetProvider.BUTTON_PRESSED,
										RecentWidgetProvider.buttonMap[simulatedButtonPosition]);
						startActivity(intent);

					}

					return true;
				}

				return false;
			}
		});

		// Also set fling listener on the Data View

		findViewById(R.id.eventPopupDataView).setOnTouchListener(
				new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// Same as activity.onTouchEvent...
						if (gestureDetector.onTouchEvent(event)) {
							return true;
						} else {
							return false;
						}
					}
				});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) {
			return true;
		} else {
			return false;
		}
	}

	private void bindIntentToButton(int buttonId, Uri contentUriData) {

		final Intent actionIntent = new Intent(Intent.ACTION_VIEW);

		actionIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		actionIntent.setData(contentUriData);

		((ImageButton) findViewById(buttonId))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						startActivity(actionIntent);
						finish();
					}
				});
	}

}
