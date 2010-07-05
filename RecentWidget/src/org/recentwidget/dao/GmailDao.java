package org.recentwidget.dao;

import java.util.StringTokenizer;

import org.recentwidget.EventListBuilder;
import org.recentwidget.compat.gmail.Gmail;
import org.recentwidget.compat.gmail.Gmail.ConversationColumns;
import org.recentwidget.compat.gmail.Gmail.MessageColumns;
import org.recentwidget.model.RecentContact;
import org.recentwidget.model.RecentEvent;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class GmailDao extends ContentResolverTemplate {

	private static final String TAG = "RW:GmailDao";

	private static String accountName;
	private static final boolean useConversations = true;

	public GmailDao() {
		super();

		if (useConversations) {
			projection = Gmail.CONVERSATION_PROJECTION;
		} else {
			projection = Gmail.MESSAGE_PROJECTION;
		}
		sortOrder = null; // MUST be empty
	}

	@Override
	protected Uri getContentUri() {
		// 2.x only!

		if (context != null && accountName == null) {

			Account[] accounts = AccountManager.get(context).getAccounts();

			for (Account account : accounts) {
				if (account.type != null && account.type.equals("com.google")) {
					accountName = account.name;
					Log.d(TAG, "Using account: " + accountName);
					break;
				}
			}

		}

		if (useConversations) {
			return Uri.parse(Gmail.AUTHORITY_PLUS_CONVERSATIONS + accountName);
		} else {
			return Uri.parse(Gmail.AUTHORITY_PLUS_MESSAGES + accountName);
			// return Uri.parse("content://gmail-ls/unread/^i");
		}
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getWidgetLabels() {
		// TODO Auto-generated method stub
		// Steal from other DAO
		return SmsDao.contactSmsMap;
	}
}
