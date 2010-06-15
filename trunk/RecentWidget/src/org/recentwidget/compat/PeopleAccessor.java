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

@SuppressWarnings("deprecation")
public class PeopleAccessor extends AbstractContactAccessor {

	public PeopleAccessor() {
		contentUri = People.CONTENT_URI;
		displayNameColumn = Phones.DISPLAY_NAME;
		personIdColumn = Phones.PERSON_ID;
	}

	@Override
	public Cursor getContactCursor(Context context, RecentContact recentContact) {

		ContentResolver resolver = context.getContentResolver();

		return resolver.query(Contacts.Phones.CONTENT_URI, new String[] {
				personIdColumn, displayNameColumn }, Phones.NUMBER + " = ?",
				new String[] { recentContact.getNumber() }, null);

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
}
