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
import android.text.TextUtils.TruncateAt;
import android.text.format.DateUtils;
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
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class EventPopupActivity extends Activity {

	private static final int BADGE_ID = 4567;

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

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.eventpopup);

		// Setup the fling/swipe info toast

		((ImageButton) findViewById(R.id.flingIcon))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast toast = Toast.makeText(v.getContext(),
								R.string.popup_fling_text, Toast.LENGTH_SHORT);
						toast.show();
					}
				});

		// Contact badge

		RelativeLayout header = (RelativeLayout) findViewById(R.id.popupHeader);

		ImageView badge = RecentWidgetUtils.CONTACTS_API.createPopupBadge(this,
				recentContact);
		badge.setId(BADGE_ID);

		// Set image

		Bitmap contactPhoto = null;

		if (recentContact.getPersonId() != null) {

			contactPhoto = RecentWidgetUtils.CONTACTS_API.loadContactPhoto(
					this, recentContact);

			if (contactPhoto != null) {
				BitmapDrawable contactDrawable = new BitmapDrawable(
						contactPhoto);
				badge.setImageDrawable(contactDrawable);
			}

		}

		if (contactPhoto == null) {
			// Display default icon
			badge.setImageResource(RecentWidgetProvider.defaultContactImage);
			badge.setBackgroundResource(android.R.drawable.btn_default);
		}

		// If not using QuickContactBadge:

		if (badge instanceof ImageButton) {

			// Set button (cannot be done before because we need references to
			// the activity

			if (recentContact.hasContactInfo()) {

				// Contact button
				bindIntentToButton(badge, ContentUris.withAppendedId(
						RecentWidgetUtils.CONTACTS_API.contentUri,
						recentContact.getPersonId()));

			} else {

				// Bring up the dialer since no Contact registered
				bindIntentToButton(badge, Uri.parse("tel:"
						+ recentContact.getNumber()));
			}

		}

		RelativeLayout.LayoutParams badgeLayout = new RelativeLayout.LayoutParams(
				50, 50);
		badgeLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
				RelativeLayout.TRUE);
		header.addView(badge, 0, badgeLayout);

		// Set the display name in header

		TextView textView = (TextView) findViewById(R.id.popupText);
		textView.setText(recentContact.getDisplayName());

		RelativeLayout.LayoutParams textLayout = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textLayout.addRule(RelativeLayout.RIGHT_OF, badge.getId());
		textLayout.addRule(RelativeLayout.LEFT_OF, R.id.flingIcon);
		textLayout.addRule(RelativeLayout.ALIGN_BASELINE, badge.getId());
		// textLayout.addRule(RelativeLayout.CENTER_VERTICAL,
		// RelativeLayout.TRUE);

		header.updateViewLayout(textView, textLayout);

		// Display of the recent events

		// Call Log button

		RecentEvent lastEvent = recentContact
				.getMostRecentEvent(RecentEvent.TYPE_CALL);
		View eventTypeButton = findViewById(R.id.popupTableAction1);
		if (recentContact.getNumber() != null) {
			bindIntentToButton(Intent.ACTION_CALL, eventTypeButton, Uri
					.parse("tel:" + recentContact.getNumber()));
		}

		// SMS button

		lastEvent = recentContact.getMostRecentEvent(RecentEvent.TYPE_SMS);
		eventTypeButton = findViewById(R.id.popupTableAction2);
		if (lastEvent != null) {
			bindIntentToButton(eventTypeButton, ContentUris.withAppendedId(
					SmsDao.SMS_CONTENT_URI, lastEvent.getId()));
		} else if (recentContact.getNumber() != null) {
			// Provide a way to compose a new message
			bindIntentToButton(eventTypeButton, Uri.parse("sms:"
					+ recentContact.getNumber()));
		}

		// Gmail button

		lastEvent = recentContact.getMostRecentEvent(RecentEvent.TYPE_EMAIL);
		eventTypeButton = findViewById(R.id.popupTableAction3);
		// TODO: How to fetch contact's email?
		if (lastEvent != null) {
			findViewById(R.id.popupLayout3).setVisibility(View.VISIBLE);
			/* DOES NOT WORK!
			bindIntentToButton(eventTypeButton, ContentUris.withAppendedId(Uri
					.withAppendedPath(Gmail.CONVERSATIONS_URI, GmailDao
							.getAccountName(this)), lastEvent.getId()));
			*/
		} else {
			findViewById(R.id.popupLayout3).setVisibility(View.GONE);
		}

		// Show the recent events
		// TODO: Make it more dynamic... less magic-number based.

		TableLayout telLayout = (TableLayout) findViewById(R.id.popupTable1);
		TableLayout smsLayout = (TableLayout) findViewById(R.id.popupTable2);
		TableLayout emailLayout = (TableLayout) findViewById(R.id.popupTable3);

		for (RecentEvent recentEvent : recentContact.getRecentEvents()) {

			Log.d(TAG, "Drawing event: " + recentEvent);

			TableRow row = new TableRow(this);
			row.setGravity(Gravity.CENTER_VERTICAL);

			CharSequence date = DateUtils.getRelativeDateTimeString(this,
					recentEvent.getDate(), DateUtils.SECOND_IN_MILLIS,
					DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
							| DateUtils.FORMAT_NUMERIC_DATE
							| DateUtils.FORMAT_SHOW_WEEKDAY
							| DateUtils.FORMAT_ABBREV_WEEKDAY);

			TextView eventText = new TextView(this);
			eventText.setText(date);
			eventText.setMaxLines(3);
			eventText.setEllipsize(TruncateAt.END);
			if (recentEvent.getDetails() != null) {
				eventText.append(": " + recentEvent.getDetails());
			}

			row.addView(eventText, new android.widget.TableRow.LayoutParams(
					android.widget.TableRow.LayoutParams.WRAP_CONTENT,
					android.widget.TableRow.LayoutParams.WRAP_CONTENT));

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
			case RecentEvent.TYPE_EMAIL:
				emailLayout.addView(row);
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

		if (RecentWidgetUtils.DEBUG) {

			String debugString = RecentWidgetUtils.CONTACTS_API.debugLookup(
					getContentResolver(), recentContact);

			textView.append(debugString);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) {
			return true;
		} else {
			return false;
		}
	}

	private void bindIntentToButton(View button, Uri contentUriData) {
		bindIntentToButton(Intent.ACTION_VIEW, button, contentUriData);
	}

	private void bindIntentToButton(String action, View button,
			Uri contentUriData) {

		final Intent actionIntent = new Intent(action);

		actionIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		actionIntent.setData(contentUriData);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(actionIntent);
				finish();
			}
		});
	}

}
