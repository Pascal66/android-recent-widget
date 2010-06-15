package org.recentwidget.compat;

import org.recentwidget.model.RecentContact;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

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

	public abstract Bitmap loadContactPhoto(Context context,
			RecentContact recentContact);

	/**
	 * Retrieves the cursor on the contact without knowing its ID.
	 */
	public abstract Cursor getContactCursor(Context context,
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