package org.recentwidget.compat;

import java.io.ByteArrayInputStream;
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
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.QuickContact;
import android.util.Log;
import android.widget.ImageView;
import android.widget.QuickContactBadge;

// Inspired from file:///C:/KV/dev/android/android-sdk-windows-1.6_r1/docs/resources/articles/backward-compatibility.html
// but does not embed an class instance.
public class ContactsContractAccessor extends AbstractContactAccessor {

	private static final String SQLLITE_WILDCARD = "%";
	private static final int PREFIX_LENGTH = 4; // "+352"
	private static final int LOCAL_PREFIX_LENGTH = 3; // "052"
	/**
	 * MIN_NUM_LOOKUP_LENGTH must be greater than PREFIX_LENGTH Min number of
	 * characters used for pattern matching.
	 */
	private static final int MIN_NUM_LOOKUP_LENGTH = 5; // "219006"

	// TODO: possible performance gain with
	// ContactsContract.Contacts.CONTENT_FILTER_URI ?

	private int displayNameIndex;
	private int personIdIndex;
	private boolean initialized = false;
	private int contactIdIndex;
	private final String[] projection;

	public ContactsContractAccessor() {
		contentUri = ContactsContract.Contacts.CONTENT_URI;
		displayNameColumn = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
		personIdColumn = PhoneLookup.LOOKUP_KEY;

		projection = new String[] { personIdColumn, displayNameColumn,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Photo.PHOTO_ID };
	}

	@Override
	public RecentContact getContactCursorBySearch(Context context,
			RecentContact recentContact) {

		ContentResolver resolver = context.getContentResolver();
		Cursor lookupCursor = null;

		String lookupNumber = recentContact.getNumber();
		if (lookupNumber != null) {

			int numberLength = lookupNumber.length();

			int prefixLength = PREFIX_LENGTH;
			if (lookupNumber.startsWith("+")) {
				prefixLength = LOCAL_PREFIX_LENGTH;
			}

			// Only do wildcard matching if string has a min. length
			if (numberLength >= MIN_NUM_LOOKUP_LENGTH + prefixLength) {

				lookupNumber = SQLLITE_WILDCARD
						+ lookupNumber
								.substring(prefixLength + 1, numberLength);
			}
		}
		Log.d(TAG, "Searching with number: " + lookupNumber);

		if (lookupNumber != null && recentContact.getPerson() != null) {

			lookupCursor = resolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					projection, displayNameColumn + " = ? OR "
							+ ContactsContract.CommonDataKinds.Phone.NUMBER
							+ " LIKE ?",
					new String[] { recentContact.getPerson(), lookupNumber },
					null);

		} else if (lookupNumber != null) {

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

			/*
			lookupCursor = resolver.query(Uri.withAppendedPath(
					ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
					lookupNumber), new String[] { personIdColumn,
					displayNameColumn,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
					ContactsContract.CommonDataKinds.Photo.PHOTO_ID }, null,
					null, null);
			*/

			lookupCursor = resolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					projection, ContactsContract.CommonDataKinds.Phone.NUMBER
							+ " LIKE ?", new String[] { lookupNumber }, null);

		} else if (recentContact.getPerson() != null) {

			// Search by name

			lookupCursor = resolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					projection, displayNameColumn + " = ?",
					new String[] { recentContact.getPerson() }, null);

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
	public ImageView createPopupBadge(Context context,
			RecentContact recentContact) {
		QuickContactBadge badge = new QuickContactBadge(context);
		if (recentContact.getPersonId() != null
				&& recentContact.getLookupKey() != null) {
			badge.assignContactUri(Contacts.getLookupUri(
					recentContact.getPersonId(), recentContact.getLookupKey()));
		} else {
			badge.assignContactFromPhone(recentContact.getNumber(), false);
		}
		badge.setMode(QuickContact.MODE_LARGE);
		badge.setBackgroundResource(android.R.drawable.btn_default);
		return badge;
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

		Uri contactUri = ContentUris.withAppendedId(contentUri,
				recentContact.getPersonId());

		InputStream photoInputStream = ContactsContract.Contacts
				.openContactPhotoInputStream(context.getContentResolver(),
						contactUri);

		if (photoInputStream != null) {

			// Use new ContactsContract... but still no Facebook image!?!

			Log.v(TAG, "Found photo using ContactsContract");
			return BitmapFactory.decodeStream(photoInputStream);

		} else {

			// Must return null instead of the default photo. Widget cannot
			// directly use this BitmapFactory otherwise.
			return null;

		}

	}

	protected void initContactFromCursor(RecentContact recentContact,
			Cursor lookupCursor) {

		if (!initialized) {
			displayNameIndex = lookupCursor.getColumnIndex(displayNameColumn);
			personIdIndex = lookupCursor.getColumnIndex(personIdColumn);
			contactIdIndex = lookupCursor
					.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
			initialized = true;
		}

		recentContact.setPerson(lookupCursor.getString(displayNameIndex));
		recentContact.setPersonKey(lookupCursor.getString(personIdIndex));

		if (contactIdIndex >= 0) {
			recentContact.setPersonId(lookupCursor.getLong(contactIdIndex));
		}
	}

	@Override
	public String debugLookup(ContentResolver contentResolver,
			RecentContact recentContact) {

		String debugString = "";

		Uri phoneLookupUri = Uri.withAppendedPath(
				PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(recentContact.getNumber()));

		Cursor phoneLookup = contentResolver.query(phoneLookupUri,
				new String[] { PhoneLookup.LOOKUP_KEY, PhoneLookup.TYPE,
						PhoneLookup.DISPLAY_NAME, PhoneLookup.PHOTO_ID }, null,
				null, null);

		while (phoneLookup.moveToNext()) {
			debugString += " :: ";
			debugString += phoneLookup.getString(0) + " : ";
			debugString += phoneLookup.getString(1) + " : ";
			debugString += phoneLookup.getString(2) + " : ";
			debugString += phoneLookup.getString(3);
		}

		return debugString;
	}

	/**
	 * Returns an InputStream for the person's photo
	 * 
	 * Credits:
	 * http://android-smspopup.googlecode.com/svn-history/trunk/SMSPopup
	 * /src/net/everythingandroid/smspopup/wrappers/ContactWrapper.java
	 * 
	 * @param id
	 *            the id of the person
	 */
	public InputStream openContactPhotoInputStream(ContentResolver cr, String id) {
		if (id == null)
			return null;
		if ("0".equals(id))
			return null;

		Cursor cursor;

		/*
		 * User is using Eclair or beyond
		 */

		try {
			InputStream photoStream = ContactsContract.Contacts
					.openContactPhotoInputStream(cr,
							Uri.withAppendedPath(contentUri, id));

			return photoStream;
			/*
			    if (photoStream != null) {
			      if (Log.DEBUG) Log.v("openContactPhotoInputStream(): contact photo found using Eclair SDK");
			      return photoStream;
			    }
			 */
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Unable to fetch contact photo using Anroid 2.0+ SDK: "
					+ e.toString());
		}

		// This tries to look for other contact photos directly in the
		// contacts database

		Log.v(TAG, "Looking for contact photo in Data table");
		cursor = cr.query(ContactsContract.Data.CONTENT_URI,
		// new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO,
		// ContactsContract.Data.MIMETYPE},
		// ContactsContract.Data.MIMETYPE + "== '" +
		// ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE +
		// "' AND " +
		// ContactsContract.Data.CONTACT_ID + " == " + id, null, null);
				new String[] { "data15", "mimetype" }, "mimetype" + "== '"
						+ "vnd.android.cursor.item/photo" + "' AND "
						+ "contact_id" + " == " + id, null, null);
		if (cursor == null)
			return null;

		try {
			Log.v(TAG, "CURSOR COUNT = " + cursor.getCount());
			while (cursor.moveToNext()) {
				byte[] data = cursor.getBlob(0);
				if (data != null) {
					Log.v(TAG,
							"openContactPhotoInputStream(): contact photo found using Data table");
					return new ByteArrayInputStream(data);
				} else {
					Log.v(TAG, "PHOTO DATA WAS NULL");
				}
			}
		} finally {
			cursor.close();
		}

		return null;
	}

}
