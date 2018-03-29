/**
 * 
 */
package com.nils.electricitywatercuts;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * @author NilS
 * 
 */
public class CutsProvider extends ContentProvider {


	ElectricityWaterCutsDatabaseHelper dbHelper;

	@Override
	public boolean onCreate() {
		Context context = getContext();

		dbHelper = new ElectricityWaterCutsDatabaseHelper(context,
				CutsConstants.DATABASE_NAME, null,
				CutsConstants.DATABASE_VERSION);

		return true;
	}

	private static final HashMap<String, String> SEARCH_PROJECTION_MAP;
	static {
		SEARCH_PROJECTION_MAP = new HashMap<String, String>();
		SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1,
				CutsConstants.KEY_DETAIL + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
		SEARCH_PROJECTION_MAP.put("_id", CutsConstants.KEY_ID + " AS " + "_id");
	}

	// Create the constants used to differentiate between the different URI
	// requests.
	private static final int CUTS = 1;
	private static final int CUTS_ID = 2;
	private static final int SEARCH = 3;

	private static final UriMatcher uriMatcher;

	// Allocate the UriMatcher object, where a URI ending in 'earthquakes' will
	// correspond to a request for all earthquakes, and 'earthquakes' with a
	// trailing '/[rowID]' will represent a single earthquake row.
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.nils.electricitywatercuts", "cuts", CUTS);
		uriMatcher.addURI("com.nils.electricitywatercuts", "cuts/#", CUTS_ID);
		uriMatcher.addURI("com.nils.electricitywatercuts",
				SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
		uriMatcher.addURI("com.nils.electricitywatercuts",
				SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
		uriMatcher.addURI("com.nils.electricitywatercuts",
				SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH);
		uriMatcher.addURI("com.nils.electricitywatercuts",
				SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH);
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case CUTS:
			return "vnd.android.cursor.dir/vnd.nils.cuts";
		case CUTS_ID:
			return "vnd.android.cursor.item/vnd.nils.cuts";
		case SEARCH:
			return SearchManager.SUGGEST_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sort) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(CutsConstants.CUTS_TABLE);

		// If this is a row query, limit the result set to the passed in row.
		switch (uriMatcher.match(uri)) {
			case CUTS_ID:
				qb.appendWhere(CutsConstants.KEY_ID + "=" + uri.getPathSegments().get(1));
				break;
			case SEARCH:
				qb.appendWhere(CutsConstants.KEY_DETAIL + " LIKE \"%"
						+ uri.getPathSegments().get(1) + "%\"");
				qb.setProjectionMap(SEARCH_PROJECTION_MAP);
				break;
			default:
				break;
		}

		// If no sort order is specified, sort by date / time
		String orderBy;
		if (TextUtils.isEmpty(sort)) {
			orderBy = CutsConstants.KEY_END_DATE;
		} else {
			orderBy = sort;
		}

		// Apply the query to the underlying database.
		Cursor c = qb.query(database, projection, selection, selectionArgs,
				null, null, orderBy);

		// Register the contexts ContentResolver to be notified if
		// the cursor result set changes.
		c.setNotificationUri(getContext().getContentResolver(), uri);

		// Return a cursor to the query result.
		return c;
	}

	@Override
	public Uri insert(Uri _uri, ContentValues _initialValues) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		// Insert the new row. The call to database.insert will return the row
		// number
		// if it is successful.
		long rowID = database.insert(
				CutsConstants.CUTS_TABLE, "cut",
				_initialValues);

		// Return a URI to the newly inserted row on success.
		if (rowID > 0) {
			Uri uri = ContentUris.withAppendedId(CutsConstants.CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}

		throw new SQLException("Failed to insert row into " + _uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();

		int count;
		switch (uriMatcher.match(uri)) {
			case CUTS:
				count = database.delete(
						CutsConstants.CUTS_TABLE, where,
						whereArgs);
				break;
			case CUTS_ID:
				String segment = uri.getPathSegments().get(1);
				count = database.delete(
						CutsConstants.CUTS_TABLE, CutsConstants.KEY_ID
								+ "="
								+ segment
								+ (!TextUtils.isEmpty(where) ? " AND (" + where
										+ ')' : ""), whereArgs);
				break;
	
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();

		int count;
		switch (uriMatcher.match(uri)) {
		case CUTS:
			count = database.update(
					CutsConstants.CUTS_TABLE, values,
					where, whereArgs);
			break;
		case CUTS_ID:
			String segment = uri.getPathSegments().get(1);
			count = database.update(
					CutsConstants.CUTS_TABLE, values,
					CutsConstants.KEY_ID
							+ "="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	// Helper class for opening, creating, and managing database version control
	private static class ElectricityWaterCutsDatabaseHelper extends
			SQLiteOpenHelper {
		
		private static final String CUTS_DATABASE_CREATE = "create table "
				+ CutsConstants.CUTS_TABLE + " (" + CutsConstants.KEY_ID
				+ " integer primary key autoincrement, " + CutsConstants.KEY_OPERATOR_NAME
				+ " TEXT, " + CutsConstants.KEY_START_DATE + " TEXT, " + CutsConstants.KEY_END_DATE
				+ " TEXT, " + CutsConstants.KEY_LOCATION + " TEXT, " + CutsConstants.KEY_REASON
				+ " TEXT, " + CutsConstants.KEY_DETAIL + " TEXT, " + CutsConstants.KEY_TYPE
				+ " TEXT, "  + CutsConstants.KEY_SEARCH_TEXT + " TEXT, " + CutsConstants.KEY_ORDER_START_DATE
				+ " TEXT, " + CutsConstants.KEY_ORDER_END_DATE + " TEXT, " + CutsConstants.KEY_IS_CURRENT + " TEXT);";

		public ElectricityWaterCutsDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CUTS_DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL("DROP TABLE IF EXISTS " + CutsConstants.CUTS_TABLE);
			onCreate(db);
			
			// upgrade from v1
//			if(CutsConstants.getDatabaseVersion() <= 1) {
//			    db.execSQL("ALTER TABLE "+ CutsConstants.getCutsTable() +" ADD COLUMN pnumber INTEGER;");
//			}
		}
	}

}
