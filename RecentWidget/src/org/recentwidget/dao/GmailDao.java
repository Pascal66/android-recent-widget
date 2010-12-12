package org.recentwidget.dao;

import java.util.StringTokenizer;

import org.recentwidget.EventListBuilder;
import org.recentwidget.android.WidgetPreferenceActivity;
import org.recentwidget.compat.gmail.Gmail;
import org.recentwidget.compat.gmail.Gmail.ConversationColumns;
import org.recentwidget.compat.gmail.Gmail.MessageColumns;
import org.recentwidget.model.RecentContact;
import org.recentwidget.model.RecentEvent;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class GmailDao extends ContentResolverTemplate {

	private static final String GMAIL_ACCOUNT_TYPE = "com.google";

	private static final String TAG = "RW:GmailDao";

	private static String accountName;
	private static final boolean useConversations = true;

	public GmailDao() {
		super();

		preferenceEnabledName = WidgetPreferenceActivity.PREF_PROVIDER_GMAIL;

		if (useConversations) {
			projection = Gmail.CONVERSATION_PROJECTION;
		} else {
			projection = Gmail.MESSAGE_PROJECTION;
		}
		sortOrder = null; // MUST be empty
	}

	@Override
	protected Uri getContentUri() {
		// 2.0+ only!

		if (context != null && accountName == null) {
			accountName = getAccountName(context);
		}

		if (useConversations) {
			return Uri.parse(Gmail.AUTHORITY_PLUS_CONVERSATIONS + accountName);
		} else {
			return Uri.parse(Gmail.AUTHORITY_PLUS_MESSAGES + accountName);
			// return Uri.parse("content://gmail-ls/unread/^i");
		}
	}

	public static final String getAccountName(Context context) {

		if (accountName != null) {
			return accountName;
		}

		Account[] accounts = AccountManager.get(context).getAccounts();

		for (Account account : accounts) {
			if (GMAIL_ACCOUNT_TYPE.equals(account.type)) {
				Log.d(TAG, "Using account: " + accountName);
				return account.name;
			}
		}

		return null;
	}

	@Override
	protected String getQuery() {
		if (!useConversations) {
			return "label:" + Gmail.LABEL_INBOX;
		} else {
			return null;
		}
	}

	@Override
	public boolean supports(String intentAction) {

		// MAYBE? Should just listen to this intent and store it locally?!?
		/* TODO: Should also check that data type = Gmail.AUTHORITY
		// Example:
		Intent {
			action=android.intent.action.PROVIDER_CHANGED
			data=content://gmail-ls/unread/^i type=gmail-ls flags=0x4000000
			comp={com.google.android.gm/com.google.android.gm.ConversationListActivity}
			(has extras) }
		*/
		return Intent.ACTION_PROVIDER_CHANGED.equals(intentAction);

	}

	@Override
	protected long extractEvent(EventListBuilder builder, Cursor cursor) {

		if (useConversations) {

			long threadId = cursor.getLong(cursor
					.getColumnIndex(ConversationColumns.ID));

			// TODO: Address to be found may actually be the TO field, for
			// messages sent!?

			String emailDisplayName = null;
			String address = cursor.getString(cursor
					.getColumnIndex(ConversationColumns.FROM));

			// The address is in the form: "0\n0\nLASTNAME First name\n"
			// or "n\n7\n0\n0\n\n0\n2\nFranco\n0\n1\nFscafidi\n"
			// TODO: Use TextUtils.StringSplitter instead?
			StringTokenizer tokenizer = new StringTokenizer(address, "\n");
			while (tokenizer.hasMoreElements()) {
				emailDisplayName = tokenizer.nextToken();
				if (emailDisplayName.length() > 1) {
					break;
				}
			}

			long date = cursor.getLong(cursor
					.getColumnIndex(ConversationColumns.DATE));

			String body = cursor.getString(cursor
					.getColumnIndex(ConversationColumns.SUBJECT))
					+ ": "
					+ cursor.getString(cursor
							.getColumnIndex(ConversationColumns.SNIPPET));

			Log.v(TAG, "Fetched email recent event: #" + threadId + " from "
					+ emailDisplayName + "(" + date + "): " + body);

			RecentEvent newEvent = new RecentEvent();
			newEvent.setId(threadId); // Might be null
			newEvent.setDate(date);
			newEvent.setType(RecentEvent.TYPE_EMAIL);
			newEvent.setSubType(RecentEvent.SUBTYPE_INCOMING);
			newEvent.setDetails(body);

			if (emailDisplayName != null) {
				// What to do with this emailDisplayName?!?!?!
				builder.add(context, null, emailDisplayName, null, newEvent);
			}

			return date;

		} else {

			// Not working...

			long threadId = cursor.getLong(cursor
					.getColumnIndex(MessageColumns.ID));

			String address = cursor.getString(cursor
					.getColumnIndex(MessageColumns.FROM));

			long date = cursor.getLong(cursor
					.getColumnIndex(MessageColumns.DATE_RECEIVED_MS));

			String body = cursor.getString(cursor
					.getColumnIndex(MessageColumns.SUBJECT))
					+ ": "
					+ cursor.getString(cursor
							.getColumnIndex(MessageColumns.SNIPPET));

			Log.v(TAG, "Fetched email recent event: #" + threadId + " from "
					+ address + "(" + date + "): " + body);

			return date;
		}
	}

	@Override
	protected int getTargetType() {
		return RecentEvent.TYPE_EMAIL;
	}

	@Override
	public Integer getResourceForWidget(RecentContact contact) {
		// Just show an icon if there was an SMS conversation
		RecentEvent event = contact.getMostRecentEvent(RecentEvent.TYPE_EMAIL);
		if (event != null) {
			return android.R.drawable.ic_dialog_email;
		} else {
			return null;
		}
	}

}
