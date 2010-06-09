package org.recentwidget.compat;

import java.io.InputStream;

import org.recentwidget.model.RecentContact;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

// Inspired from file:///C:/KV/dev/android/android-sdk-windows-1.6_r1/docs/resources/articles/backward-compatibility.html
// but does not embed an class instance.
public class ContactAccessor {

	private static String TAG = "RW:ContactAccessor";

	static {
		try {
			Class.forName("android.provider.ContactsContract");
			Log.d(TAG, "ContactsContract available");
		} catch (Exception e) {
			// Unfortunately not using API 5
			Log.d(TAG, "ContactsContract not available");
			throw new RuntimeException(e);
		}
	}

	/* calling here forces class initialization */
	public static void checkAvailable() {
	}

	public static Bitmap loadContactPhoto(Context context,
			RecentContact recentContact, int defaultcontactimage) {

		Uri contactUri = ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI, recentContact
						.getPersonId());
		InputStream photoInputStream = ContactsContract.Contacts
				.openContactPhotoInputStream(context.getContentResolver(),
						contactUri);
		if (photoInputStream != null) {
			// Use new ContactsContract... but still no Facebook image!!!
			Log.v(TAG, "Found photo using ContactsContract");
			return BitmapFactory.decodeStream(photoInputStream);
		} else {
			// Return photo passed as parameter
			Log.v(TAG, "No photo found using ContactsContract");
			return BitmapFactory.decodeResource(context.getResources(),
					defaultcontactimage);
		}

	}
}
