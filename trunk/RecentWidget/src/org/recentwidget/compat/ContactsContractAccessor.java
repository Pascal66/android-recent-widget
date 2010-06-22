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
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

// Inspired from file:///C:/KV/dev/android/android-sdk-windows-1.6_r1/docs/resources/articles/backward-compatibility.html
// but does not embed an class instance.
public class ContactsContractAccessor extends AbstractContactAccessor {

	// TODO: possible performance gain with
	// ContactsContract.Contacts.CONTENT_FILTER_URI ?

	public ContactsContractAccessor() {
		contentUri = ContactsContract.Contacts.CONTENT_URI;
		displayNameColumn = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
		personIdColumn = PhoneLookup.LOOKUP_KEY;
	}

	@Override
	public RecentContact getContactCursorBySearch(Context context,
			RecentContact recentContact) {

		ContentResolver resolver = context.getContentResolver();
		Cursor lookupCursor = null;

		if (recentContact.getNumber() != null) {

			// Search by number

			/*
			 * Does not give us the contact ID ?!
			Uri phoneLookupUri = Uri.withAppendedPath(
					PhoneLookup.CONTENT_FILTER_URI, Uri.encode(recentContact
							.getNumber()));

			lookupCursor = resolver.query(phoneLookupUri, new String[] {
					personIdColumn, PhoneLookup.TYPE, displayNameColumn,
					PhoneLookup.PHOTO_ID }, null, null, null);
			 */

			lookupCursor = resolver.query(Uri.withAppendedPath(
					ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
					recentContact.getNumber()), new String[] { personIdColumn,
					displayNameColumn,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
					ContactsContract.CommonDataKinds.Photo.PHOTO_ID }, null,
					null, null);

		} else if (recentContact.getPerson() != null) {

			// Search by name

			lookupCursor = resolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					new String[] { personIdColumn, displayNameColumn,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
							ContactsContract.CommonDataKinds.Photo.PHOTO_ID },
					displayNameColumn + " = ?", new String[] { recentContact
							.getPerson() }, null);

		} else {

			Log.w(TAG, "No basic info to find contact: " + recentContact);
			return recentContact;

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

		// ID received from SmsDao actually is:
		// ContactsContract.CommonDataKinds.Phone.CONTACT_ID

		ContentResolver resolver = context.getContentResolver();

		Cursor cursor = resolver.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] { personIdColumn, displayNameColumn },
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ",
				new String[] { recentContact.getPersonId().toString() }, null);

		initContactFromCursor(recentContact, cursor);

		return recentContact;
	}

	@Override
	public Bitmap loadContactPhoto(Context context, RecentContact recentContact) {

		// ?! tested with no result (for facebook pic)
		// http://stackoverflow.com/questions/2610786/contacts-quey-with-name-and-picture-uri
		// http://developer.android.com/reference/android/provider/ContactsContract.Contacts.Photo.html

		/*
		Uri phoneLookupUri = Uri.withAppendedPath(
				PhoneLookup.CONTENT_FILTER_URI, Uri.encode(recentContact
						.getNumber()));

		Uri lookupUri = Contacts.lookupContact(context.getContentResolver(),
				phoneLookupUri);

		long contactId = ContentUris.parseId(lookupUri);

		lookupUri = Uri.withAppendedPath(ContentUris.withAppendedId(
				Contacts.CONTENT_URI, contactId),
				Contacts.Data.CONTENT_DIRECTORY);

		Cursor cursor = context
				.getContentResolver()
				.query(
						ContactsContract.Data.CONTENT_URI,
						new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO },
						ContactsContract.Data._ID + " = ? AND "
								+ ContactsContract.Data.MIMETYPE + " = ? ",
						new String[] {
								Long.toString(recentContact.getPhotoId()),
								ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE },
						null);

		*/

		// From
		// http://android.git.kernel.org/?p=platform/packages/apps/Contacts.git;a=blob;f=src/com/android/contacts/ui/QuickContactWindow.java;h=756dd1e2cec58e2d35ec021d7b6a0a7fc84b42d9;hb=735e8b11d8e370f24e9b8ac5329a1985c879bbf2

		/*
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.RawContacts.CONTENT_URI,
				new String[] { ContactsContract.RawContacts._ID },
				ContactsContract.RawContacts._ID + " = ?",
				new String[] { Long.toString(3499) // recentContact.getPersonId()),
				// , ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
				}, null);

		Log.v(TAG, "----------------- " + cursor.getCount());

		if (cursor.moveToFirst()) {
			Log.v(TAG, "------- " + cursor.getString(0));
			Log.v(TAG, "------- " + cursor.getString(1));
		//			byte[] blob = cursor.getBlob(0);
		//			if (blob != null && blob.length > 0) {
		//				// Log.v(TAG, "----------------- YES");
		//				return BitmapFactory.decodeByteArray(blob, 0, blob.length);
		//			}
		}
		// Log.v(TAG, "----------------- NO");
		return null;

		*/

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

	protected void initContactFromCursor(RecentContact recentContact,
			Cursor lookupCursor) {

		recentContact.setPerson(lookupCursor.getString(lookupCursor
				.getColumnIndex(displayNameColumn)));

		recentContact.setPersonKey(lookupCursor.getString(lookupCursor
				.getColumnIndex(personIdColumn)));

		int contactIdColumn = lookupCursor
				.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
		if (contactIdColumn >= 0) {
			recentContact.setPersonId(lookupCursor.getLong(contactIdColumn));
		}
	}

	@Override
	public String debugLookup(ContentResolver contentResolver,
			RecentContact recentContact) {

		String debugString = "";

		Uri phoneLookupUri = Uri.withAppendedPath(
				PhoneLookup.CONTENT_FILTER_URI, Uri.encode(recentContact
						.getNumber()));

		Cursor phoneLookup = contentResolver.query(phoneLookupUri,
				new String[] { PhoneLookup.LOOKUP_KEY, PhoneLookup.TYPE,
						PhoneLookup.DISPLAY_NAME, PhoneLookup.PHOTO_ID }, null,
				null, null);

		while (phoneLookup.moveToNext()) {
			debugString += " :: ";
			debugString += phoneLookup.getString(0) + " : ";
			debugString += phoneLookup.getString(1) + " : ";
			debugString += phoneLookup.getString(2);
			debugString += phoneLookup.getString(3);
		}

		return debugString;
	}
}
