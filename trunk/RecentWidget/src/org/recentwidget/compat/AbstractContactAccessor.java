package org.recentwidget.compat;

import org.recentwidget.model.RecentContact;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

/**
 * Accessors are actually stateless. But since we need to instance one (the one
 * fit for the given SDK); might as well make instance methods.
 * 
 */
public abstract class AbstractContactAccessor {

	protected static String TAG = "RW:ContactAccessor";

	public Uri contentUri;

	public String displayNameColumn;
	public String personIdColumn;

	public RecentContact fetchContactInfo(Context context,
			RecentContact recentContact) {

		// Try to fetch the Contact name and personId with the given info in
		// recentContact.

		if (recentContact.getPersonId() != null
				&& recentContact.getPerson() != null) {

			// Everything already fetched. Skip.
			// TODO: unknown contact will still create a query...

			Log.v(TAG, "No info to fetch for " + recentContact);
			return recentContact;

		}

		Cursor contactCursor;
		if (recentContact.getPersonId() != null) {
			Log.v(TAG, "Fetching contact info by id");
			contactCursor = getContactCursorById(context, recentContact);
		} else {
			Log.v(TAG, "Searching for contact " + recentContact);
			contactCursor = getContactCursorBySearch(context, recentContact);
		}

		if (contactCursor.getCount() >= 1) {

			// Just take the 1st result even if there are several
			// matches

			contactCursor.moveToFirst();

			// Set the fetched information, so next time we might not need to
			// repeat this query...

			recentContact.setPerson(getDisplayName(contactCursor));

			Long personId = getPersonId(contactCursor);

			recentContact.setPersonId(personId);

		} else {
			Log.v(TAG, "No contact found");
		}

		contactCursor.close();

		return recentContact;
	}

	public abstract Bitmap loadContactPhoto(Context context,
			RecentContact recentContact);

	public abstract Cursor getContactCursorById(Context context,
			RecentContact recentContact);

	/**
	 * Retrieves the cursor on the contact without knowing its ID.
	 */
	public abstract Cursor getContactCursorBySearch(Context context,
			RecentContact recentContact);

	public String getDisplayName(Cursor contactCursor) {
		return contactCursor.getString(contactCursor
				.getColumnIndex(displayNameColumn));
	}

	public Long getPersonId(Cursor contactCursor) {
		String personIdAsString = contactCursor.getString(contactCursor
				.getColumnIndex(personIdColumn));
		return Long.parseLong(personIdAsString);
	}

}