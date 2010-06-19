package org.recentwidget.compat;

import java.io.InputStream;

import org.recentwidget.model.RecentContact;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

// Inspired from file:///C:/KV/dev/android/android-sdk-windows-1.6_r1/docs/resources/articles/backward-compatibility.html
// but does not embed an class instance.
public class ContactsContractAccessor extends AbstractContactAccessor {

	// TODO: possible performance gain with
	// ContactsContract.Contacts.CONTENT_FILTER_URI ?

	public ContactsContractAccessor() {
		contentUri = ContactsContract.Contacts.CONTENT_URI;
		displayNameColumn = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
		personIdColumn = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
	}

	@Override
	public Cursor getContactCursorBySearch(Context context,
			RecentContact recentContact) {

		ContentResolver resolver = context.getContentResolver();

		if (recentContact.getNumber() != null
				&& recentContact.getPerson() != null) {

			// Search by name and number

			return resolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					new String[] { personIdColumn, displayNameColumn },
					ContactsContract.CommonDataKinds.Phone.NUMBER + " = ? OR "
							+ displayNameColumn + " = ?", new String[] {
							recentContact.getNumber(),
							recentContact.getPerson() }, null);

		} else if (recentContact.getPerson() != null) {

			// Search by name

			return resolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					new String[] { personIdColumn, displayNameColumn },
					displayNameColumn + " = ?", new String[] { recentContact
							.getPerson() }, null);

		} else {

			// Search by number by default

			return resolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					new String[] { personIdColumn, displayNameColumn },
					ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
					new String[] { recentContact.getNumber() }, null);

		}
	}

	@Override
	public Cursor getContactCursorById(Context context,
			RecentContact recentContact) {
		ContentResolver resolver = context.getContentResolver();

		return resolver.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] { personIdColumn, displayNameColumn },
				personIdColumn + " = ? ", new String[] { recentContact
						.getPersonId().toString() }, null);
	}

	@Override
	public Bitmap loadContactPhoto(Context context, RecentContact recentContact) {

		Uri contactUri = ContentUris.withAppendedId(contentUri, recentContact
				.getPersonId());

		InputStream photoInputStream = ContactsContract.Contacts
				.openContactPhotoInputStream(context.getContentResolver(),
						contactUri);

		if (photoInputStream != null) {

			// Use new ContactsContract... but still no Facebook image!?!

			Log.v(TAG, "Found photo using ContactsContract");
			return BitmapFactory.decodeStream(photoInputStream);

		} else {

			return null;

		}

	}

}
