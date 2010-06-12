package org.recentwidget;

import org.recentwidget.android.RecentWidgetProvider;
import org.recentwidget.model.RecentContact;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Contacts.People;

public class RecentWidgetUtils {

	// Some constants

	public static final String ACTION_SHOW_POPUP = "org.recentwidget.SHOW_POPUP";
	public static final String ACTION_UPDATE_ALL = "org.recentwidget.UPDATE_ALL";
	public static final String ACTION_UPDATE_TELEPHONY = "org.recentwidget.UPDATE_TELEPHONY";
	public static final String ACTION_NEXT_CONTACTS = "org.recentwidget.NEXT_CONTACTS";

	// Android constants

	public static final String ACTION_UPDATE_SMS = "android.provider.Telephony.SMS_RECEIVED";
	public static final String ACTION_UPDATE_CALL = "android.intent.action.PHONE_STATE";

	public static final String[] ACTION_UPDATE_TYPES = new String[] {
			ACTION_UPDATE_ALL, ACTION_UPDATE_TELEPHONY, ACTION_UPDATE_SMS,
			ACTION_UPDATE_CALL, ACTION_NEXT_CONTACTS };

	public static final String MESSAGE_SENT_ACTION = "com.android.mms.transaction.MESSAGE_SENT";

	public static boolean contactsContractAvailable = false;

	public static Bitmap loadContactPhoto(Context context,
			RecentContact recentContact) {

		/* Do not use ContactsContract until we actually have a purpose for it
		 * Or else, desynchronization with IDs...

			contactPhoto = ContactAccessor.loadContactPhoto(
					context, recentContact,
					RecentWidgetProvider.defaultContactImage);
		 */

		if (recentContact != null && recentContact.hasContactInfo()) {

			return People.loadContactPhoto(context, ContentUris.withAppendedId(
					People.CONTENT_URI, recentContact.getPersonId()),
					RecentWidgetProvider.defaultContactImage, null);
		} else {

			return BitmapFactory.decodeResource(context.getResources(),
					RecentWidgetProvider.defaultContactImage);

		}

	}
}
