package org.recentwidget.compat;

import org.recentwidget.android.RecentWidgetProvider;
import org.recentwidget.model.RecentContact;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

@SuppressWarnings("deprecation")
public class PeopleAccessor extends AbstractContactAccessor {

	public PeopleAccessor() {
		contentUri = People.CONTENT_URI;
		displayNameColumn = Phones.DISPLAY_NAME;
		personIdColumn = Phones.PERSON_ID;
	}

	@Override
	public RecentContact getContactCursorBySearch(Context context,
			RecentContact recentContact) {

		ContentResolver resolver = context.getContentResolver();
		Cursor lookupCursor = null;

		if (recentContact.getNumber() != null
				&& recentContact.getPerson() != null) {

			lookupCursor = resolver.query(Contacts.Phones.CONTENT_URI,
					new String[] { personIdColumn, displayNameColumn },
					Phones.NUMBER + " = ? OR " + displayNameColumn + " = ?",
					new String[] { recentContact.getNumber(),
							recentContact.getPerson() }, null);

		} else if (recentContact.getPerson() != null) {

			// Search by contact name

			lookupCursor = resolver.query(Contacts.Phones.CONTENT_URI,
					new String[] { personIdColumn, displayNameColumn },
					displayNameColumn + " = ?", new String[] { recentContact
							.getPerson() }, null);

		} else {

			// Search by number by default

			lookupCursor = resolver.query(Contacts.Phones.CONTENT_URI,
					new String[] { personIdColumn, displayNameColumn },
					Phones.NUMBER + " = ?", new String[] { recentContact
							.getNumber() }, null);
		}

		if (lookupCursor != null && lookupCursor.getCount() >= 1) {
			try {
				if (lookupCursor.moveToFirst()) {

					initContactFromCursor(recentContact, lookupCursor);

					return recentContact;

				}
			} finally {
				lookupCursor.close();
			}
		}

		Log.d(TAG, "No contact found for " + recentContact);

		return recentContact;
	}

	@Override
	public RecentContact getContactCursorById(Context context,
			RecentContact recentContact) {

		ContentResolver resolver = context.getContentResolver();

		Cursor cursor = resolver.query(Contacts.Phones.CONTENT_URI,
				new String[] { personIdColumn, displayNameColumn },
				Phones.NUMBER + " = ?", new String[] { recentContact
						.getNumber() }, null);

		initContactFromCursor(recentContact, cursor);

		return recentContact;
	}

	@Override
	public ImageView createPopupBadge(Context context,
			RecentContact recentContact) {

		ImageButton badge = new ImageButton(context);
		badge.setScaleType(ScaleType.FIT_CENTER);

		// onClick can only be set by the client...
		return badge;
	}

	@Override
	public Bitmap loadContactPhoto(Context context, RecentContact recentContact) {

		/*
		 * Does not work either, returned stream is apparently not null.
		InputStream contactPhotoInputStream = People
				.openContactPhotoInputStream(context.getContentResolver(),
						ContentUris.withAppendedId(People.CONTENT_URI,
								recentContact.getPersonId()));

		if (contactPhotoInputStream != null) {
			return BitmapFactory.decodeStream(contactPhotoInputStream);
		}

		*/

		return People.loadContactPhoto(context, ContentUris.withAppendedId(
				People.CONTENT_URI, recentContact.getPersonId()),
				RecentWidgetProvider.defaultContactImage, null);
	}

	protected void initContactFromCursor(RecentContact recentContact,
			Cursor lookupCursor) {

		recentContact.setPerson(lookupCursor.getString(lookupCursor
				.getColumnIndex(displayNameColumn)));

		String personIdAsString = lookupCursor.getString(lookupCursor
				.getColumnIndex(personIdColumn));

		recentContact.setPersonId(Long.parseLong(personIdAsString));
	}

}
