package org.recentwidget;

public class RecentWidgetUtils {

	// Some constants

	public static final String ACTION_SHOW_POPUP = "org.recentwidget.SHOW_POPUP";
	public static final String ACTION_UPDATE_ALL = "org.recentwidget.UPDATE_ALL";
	public static final String ACTION_UPDATE_TELEPHONY = "org.recentwidget.UPDATE_TELEPHONY";

	// Android constants

	public static final String ACTION_UPDATE_SMS = "android.provider.Telephony.SMS_RECEIVED";
	public static final String ACTION_UPDATE_CALL = "android.intent.action.PHONE_STATE";

	public static final String[] ACTION_UPDATE_TYPES = new String[] {
			ACTION_UPDATE_ALL, ACTION_UPDATE_TELEPHONY, ACTION_UPDATE_SMS,
			ACTION_UPDATE_CALL };

	public static final String MESSAGE_SENT_ACTION = "com.android.mms.transaction.MESSAGE_SENT";
}
