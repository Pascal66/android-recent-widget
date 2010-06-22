package org.recentwidget.compat;

import org.recentwidget.model.RecentContact;

import android.content.ContentResolver;
import android.content.Context;
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

		if (recentContact.getPersonId() != null) {
			Log.v(TAG, "Fetching contact info by id");
			return getContactCursorById(context, recentContact);
		} else {
			Log.v(TAG, "Searching for contact " + recentContact);
			return getContactCursorBySearch(context, recentContact);
		}

	}

	public abstract Bitmap loadContactPhoto(Context context,
			RecentContact recentContact);

	public abstract RecentContact getContactCursorById(Context context,
			RecentContact recentContact);

	/**
	 * Retrieves the cursor on the contact without knowing its ID.
	 */
	public abstract RecentContact getContactCursorBySearch(Context context,
			RecentContact recentContact);

	public String debugLookup(ContentResolver contentResolver,
			RecentContact recentContact) {
		return "";
	}

}