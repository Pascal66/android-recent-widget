package org.recentwidget;

import org.recentwidget.compat.AbstractContactAccessor;
import org.recentwidget.compat.ContactsContractAccessor;
import org.recentwidget.compat.PeopleAccessor;

import android.util.Log;

public class RecentWidgetUtils {

	private static final String TAG = "RW:Utils";

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

	/* ContactsAccessor; depends on SDK version.
	 * First establish whether the "new" class is available to us: */
	static {
		try {
			Class.forName("android.provider.ContactsContract");
			CONTACTS_API = new ContactsContractAccessor();
			Log.d(TAG, "ContactsContract available");
		} catch (Throwable e) {
			// Unfortunately not using API 5
			CONTACTS_API = new PeopleAccessor();
			Log.d(TAG, "ContactsContract not available");
		}
	}

	// public static boolean contactsContractAvailable = false;

	public static AbstractContactAccessor CONTACTS_API;
}
