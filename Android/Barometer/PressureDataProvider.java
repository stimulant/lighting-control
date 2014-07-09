/*
 * Copyright 2011 Sony Ericsson Mobile Communications AB
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * This is a standard content provider based upon an SQLite.
 * It is used to store the air pressure at the selected reference point.
 * It may be enhanced for logging possibilities if the application developer 
 * would choose to do so.  
 */


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class PressureDataProvider extends ContentProvider {
    public static final String LOG_TAG = "PressureSensorDataProvider";

    /*** Data to build the database table and content URI */
    private static final String DATABASE_NAME = "pressuresample.db";
    private static final int DATABASE_VERSION = 1;
    private static final String AUTHORITY = "com.sonyericsson.developerworld.barometricaltitudespeed.db";
    private static final String CONTENT_URI_BASE = "content://" + AUTHORITY
            + "/";

    /** Reference point table name **/
    public static final String REFERENCEPOINT_TABLE_NAME = "referencepointtable";
    /** Reference point column **/
    public static final String REFERENCEPOINT_COLUMN_NAME = "referencepoint";
    /** channel id column name */
    public static final String REFERENCE_ID_COLUMN = "_id";

    public static final Uri REFERENCE_CONTENT_URI = Uri.parse(CONTENT_URI_BASE
            + REFERENCEPOINT_TABLE_NAME);
    private static final int MAX_REFERENCE_ROWS = 1;

    /** The data base helper used. **/
    private DatabaseHelper mOpenHelper;

    /** -- Uri matcher data used only to query -- */
    private static final int REFERENCE_NAME_ONLY = 1;
    private static final int REFERENCE_ID_INCLUDED = 2;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, REFERENCEPOINT_TABLE_NAME,
                REFERENCE_NAME_ONLY);
        uriMatcher.addURI(AUTHORITY, REFERENCEPOINT_TABLE_NAME + "/#",
                REFERENCE_ID_INCLUDED);
    }

    /**
     * DatabaseHelper. Handles the creation and upgrade of the SQLite database.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub

            db.execSQL("CREATE TABLE  IF NOT EXISTS "
                    + REFERENCEPOINT_TABLE_NAME + " (" + REFERENCE_ID_COLUMN
                    + " INTEGER PRIMARY KEY," + REFERENCEPOINT_COLUMN_NAME
                    + " REAL" + ");");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + REFERENCEPOINT_TABLE_NAME);
            onCreate(db);

        }

    }

    /**
     * The standard onCreate Overwrite. Creates the database entry.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * @param uri
     * @param where
     * @param whereArgs
     * 
     *            Deletes the specified database entry/entries.
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count = 0;
        String table = uri.getPathSegments().get(0);
        if (REFERENCEPOINT_TABLE_NAME.equals(table)) {
            count = db.delete(REFERENCEPOINT_TABLE_NAME, where, whereArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Inserts the initialValues into the content provider database.
     * 
     * @param uri
     *            : Uri of the database table
     * @values: Content values to insert into the database.
     * @return the uri to the element inserted.
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values;
        if (null != initialValues) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        String table = uri.getPathSegments().get(0);
        if (REFERENCEPOINT_TABLE_NAME.equals(table)) {
            return insertReferencePoint(uri, values);
        }
        Log.v("Database", "Not able to cope with " + table);
        return null;
    }

    /**
     * This is the internal helper function used to insert the reference value
     * to the data base.
     * 
     * @param uri
     *            : Uri of the database table
     * @values: Content values to insert into the database.
     * @return the uri to the element inserted.
     */
    private Uri insertReferencePoint(Uri uri, ContentValues values) {
        Uri refPointUri = null;
        Cursor cursor = query(uri, new String[] { REFERENCE_ID_COLUMN }, null,
                null, null);
        int count = cursor.getCount();
        cursor.close();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (count >= MAX_REFERENCE_ROWS) {
            count = db.delete(REFERENCEPOINT_TABLE_NAME, null, null);
            Log.v("Database",
                    "One reference point already exists - remove current");
        } // Ensure only one proxy server;
        long rowId = db.insert(REFERENCEPOINT_TABLE_NAME, null, values);
        if (0 < rowId) {
            db.update(REFERENCEPOINT_TABLE_NAME, values, REFERENCE_ID_COLUMN
                    + "=" + rowId, null);

            refPointUri = ContentUris.withAppendedId(REFERENCE_CONTENT_URI,
                    rowId);
            getContext().getContentResolver().notifyChange(refPointUri, null);

        }

        return refPointUri;

    }

    /**
     * @param uri
     *            : Uri to look into
     * @param projection
     *            : Where in the table to look.
     * @param selection
     *            : SQL parameters to use when querying
     * @param selectionArgs
     *            : SQL parameters to use when querying
     * @return Cursor to the requested elements.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
        case REFERENCE_NAME_ONLY:
            cursor = db.query(REFERENCEPOINT_TABLE_NAME, projection, selection,
                    selectionArgs, null, null, sortOrder);
            break;
        case REFERENCE_ID_INCLUDED:
            Log.v("PRESSUREDATAPROVIDER",
                    "JHAB CP - REFPOINT including ID not coped with");
        default:
            Log.v("PRESSUREDATAPROVIDER", "WRONG URI MATCHER");
            break;
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    /**
     * @param uri
     *            : Uri to look into
     * @param projection
     *            : Where in the table to look.
     * @param selection
     *            : SQL parameters to use when querying
     * @param selectionArgs
     *            : SQL parameters to use when querying
     * @return Number of updated rows in the database.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count = 0;
        String table = uri.getPathSegments().get(0);
        if (REFERENCEPOINT_TABLE_NAME.equals(table)) {
            count = db.update(REFERENCEPOINT_TABLE_NAME, values,
                    REFERENCE_ID_COLUMN
                            + "="
                            + uri.getPathSegments().get(1)
                            + (!TextUtils.isEmpty(selection) ? " AND ("
                                    + selection + ")" : ""), selectionArgs);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
