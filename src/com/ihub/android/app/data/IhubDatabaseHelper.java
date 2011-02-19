package com.ihub.android.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * @author lisudza
 * 
 */

public class IhubDatabaseHelper extends SQLiteOpenHelper {
	private SQLiteDatabase myDataBase;
	private final Context myContext;
	public static final String KEY_ROWID = "id";
	public static final String KEY_FIRST_NAME = "firstName";
	public static final String KEY_LAST_NAME = "lastName";
	public static final String KEY_QRCODE = "qrCode";
	public static final String KEY_COUNTRY = "country";
	public static final String KEY_PROFILE_PIC = "profilePic";
	public static final String KEY_OCCUPATION = "occupation";
	public static final String KEY_USER_ID = "userID";
	public static final String KEY_MEMBER_TYPE = "memberType";
	public static final String KEY_TELEPHONE = "telephone";
	public static final String KEY_EMAIL_ADD = "emailAddress";
	
	private static final String DATABASE_NAME = "ihub";
	private static final String DATABASE_TABLE = "members";
	private static final String TAG = "IhubDatabaseHelper";
	private static int DATABASE_VERSION = 1;
	private static final String DATABASE_CREATE = "create table if not exists members  (id integer primary key autoincrement, "
			+ "firstName text not null, lastName text not null, qrCode text not null, occupation text not null, profilePic text not null, userID text not null, "
			+ "country text not null, memberType text not null, telephone text default 'N/A', emailAddress text default 'N/A', signInState integer default 0);";

	public IhubDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.myContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		myDataBase.execSQL(DATABASE_CREATE);
	}

	public void createTable() {
		myDataBase.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		myDataBase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		onCreate(myDataBase);
	}

	/**
	 * Create the database if it does not exist.
	 */
	public void createDatabase() {
		myDataBase = myContext.openOrCreateDatabase(DATABASE_NAME,
				SQLiteDatabase.CREATE_IF_NECESSARY, null);
	}

	public long addMember(String firstName, String lastName, String country,
			String qrCode, String profilePic, String occupation, String userID,
			String memberType, String telephone, String emailAddress) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_FIRST_NAME, firstName);
		initialValues.put(KEY_LAST_NAME, lastName);
		initialValues.put(KEY_COUNTRY, country);
		initialValues.put(KEY_QRCODE, qrCode);
		initialValues.put(KEY_PROFILE_PIC, profilePic);
		initialValues.put(KEY_OCCUPATION, occupation);
		initialValues.put(KEY_USER_ID, userID);
		initialValues.put(KEY_MEMBER_TYPE, memberType);
		initialValues.put(KEY_TELEPHONE, telephone);
		initialValues.put(KEY_EMAIL_ADD, emailAddress);
		return myDataBase.insert(DATABASE_TABLE, null, initialValues);
	}

	// ---deletes a particular record---
	public boolean deleteTitle(long rowId) {
		return myDataBase.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	// ---retrieves all the records---
	public Cursor getAllMembers() {
		return myDataBase.query(DATABASE_TABLE,
				new String[] { KEY_ROWID, KEY_FIRST_NAME, KEY_LAST_NAME,
						KEY_COUNTRY, KEY_QRCODE, KEY_PROFILE_PIC,
						KEY_OCCUPATION, KEY_USER_ID, KEY_MEMBER_TYPE, KEY_TELEPHONE, KEY_EMAIL_ADD }, null,
				null, null, null, null);
	}

	// ---retrieves a particular record---
	public Cursor getMember(long rowId) throws SQLException {
		Cursor mCursor = myDataBase.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_FIRST_NAME, KEY_LAST_NAME, KEY_COUNTRY,
				KEY_QRCODE, KEY_PROFILE_PIC, KEY_OCCUPATION, KEY_USER_ID,
				KEY_MEMBER_TYPE, KEY_TELEPHONE, KEY_EMAIL_ADD }, KEY_ROWID + "=" + rowId, null, null, null,
				null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor getMembersofType(String memberType) {
		Cursor mCursor = myDataBase.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_FIRST_NAME, KEY_LAST_NAME, KEY_COUNTRY,
				KEY_QRCODE, KEY_PROFILE_PIC, KEY_OCCUPATION, KEY_USER_ID,
				KEY_MEMBER_TYPE, KEY_TELEPHONE, KEY_EMAIL_ADD }, KEY_MEMBER_TYPE + " = '" + memberType + "'",
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// ---retrieves a particular record---
	public Cursor getMemberUserID(String userId) throws SQLException {
		Cursor mCursor = myDataBase.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_FIRST_NAME, KEY_LAST_NAME, KEY_COUNTRY,
				KEY_QRCODE, KEY_PROFILE_PIC, KEY_OCCUPATION, KEY_USER_ID,
				KEY_MEMBER_TYPE, KEY_TELEPHONE, KEY_EMAIL_ADD }, KEY_USER_ID + "=" + userId, null, null,
				null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// ---updates a record---
	public boolean updateMemberDetails(long rowId, String firstName,
			String lastName, String country, String qrCode, String profilePic,
			String occupation, String memberType, String telephone, String emailAddress) {
		ContentValues args = new ContentValues();
		args.put(KEY_FIRST_NAME, firstName);
		args.put(KEY_LAST_NAME, lastName);
		args.put(KEY_COUNTRY, country);
		args.put(KEY_QRCODE, qrCode);
		args.put(KEY_PROFILE_PIC, profilePic);
		args.put(KEY_OCCUPATION, occupation);
		args.put(KEY_MEMBER_TYPE, memberType);
		args.put(KEY_TELEPHONE, telephone);
		args.put(KEY_EMAIL_ADD, emailAddress);
		return myDataBase.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId,
				null) > 0;
	}

	public Cursor getOnlineMembersCount() {
		Cursor mCursor = myDataBase.rawQuery("select count(*) as hits, memberType from members group by memberType;", new String[] {});
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// ---closes the database---
	@Override
	public void close() {
		myDataBase.close();
		this.close();
	}

}
