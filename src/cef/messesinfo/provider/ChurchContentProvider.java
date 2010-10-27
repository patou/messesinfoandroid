package cef.messesinfo.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import cef.messesinfo.MessesInfo;

public class ChurchContentProvider extends ContentProvider {
    private static final String TAG = "NotePadProvider";

    private static final String DATABASE_NAME = "messesinfo.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CHURCH_TABLE_NAME = "church";

    private static HashMap<String, String> sMassFavoritesProjectionMap;

    private static final int CHURCH_FAVORITE = 1;
    private static final int CHURCH_FAVORITE_CODE = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + CHURCH_TABLE_NAME + " ("
                    + Church._ID + " INTEGER PRIMARY KEY,"
                    + Church.ID + " VARCHAR(8),"
                    + Church.NAME + " VARCHAR(80),"
                    + Church.ZIPCODE + " VARCHAR(10),"
                    + Church.CITY + " VARCHAR(80),"
                    + Church.COMMUNITY + " VARCHAR(80),"
                    + Church.LAT + " REAL,"
                    + Church.LNG + " REAL,"
                    + Church.FAVORITE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + CHURCH_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case CHURCH_FAVORITE:
            qb.setTables(CHURCH_TABLE_NAME);
            qb.setProjectionMap(sMassFavoritesProjectionMap);
            qb.appendWhere(Church.FAVORITE + " = 1");
            break;

        case CHURCH_FAVORITE_CODE:
            qb.setTables(CHURCH_TABLE_NAME);
            qb.setProjectionMap(sMassFavoritesProjectionMap);
            qb.appendWhere(Church.ID + "=");
            qb.appendWhereEscapeString(uri.getPathSegments().get(1));
            
            
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Church._ID + " ASC";
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case CHURCH_FAVORITE:
            return Church.CONTENT_TYPE;

        case CHURCH_FAVORITE_CODE:
            return Church.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != CHURCH_FAVORITE) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        // Make sure that the fields are all set
        if (values.containsKey(Church.ID) == false) {
            values.put(Church.ID, "");
        }
        
        if (values.containsKey(Church.ZIPCODE) == false) {
            values.put(Church.ZIPCODE, "");
        }
        
        if (values.containsKey(Church.CITY) == false) {
            values.put(Church.CITY, "");
        }
        
        if (values.containsKey(Church.NAME) == false) {
            values.put(Church.NAME, "");
        }
        
        if (values.containsKey(Church.COMMUNITY) == false) {
            values.put(Church.COMMUNITY, "");
        }
        
        if (values.containsKey(Church.LNG) == false) {
            values.put(Church.LNG, "");
        }
        
        if (values.containsKey(Church.LAT) == false) {
            values.put(Church.LAT, 0.0);
        }
        
        if (values.containsKey(Church.LAT) == false) {
            values.put(Church.LAT, 0.0);
        }
        
        if (values.containsKey(Church.FAVORITE) == false) {
            values.put(Church.FAVORITE, 1);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(CHURCH_TABLE_NAME, Church.ID, values);
        if (rowId > 0) {
            Uri churchUri = ContentUris.withAppendedId(Church.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(churchUri, null);
            return churchUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case CHURCH_FAVORITE:
            count = db.delete(CHURCH_TABLE_NAME, Church.FAVORITE + " = '1' "
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        case CHURCH_FAVORITE_CODE:
            String churchCode =  uri.getPathSegments().get(1);
            count = db.delete(CHURCH_TABLE_NAME, Church.ID + "= '" + churchCode + "' AND " + Church.FAVORITE + " = '1' "
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case CHURCH_FAVORITE:
            count = db.update(CHURCH_TABLE_NAME, values, where, whereArgs);
            break;

        case CHURCH_FAVORITE_CODE:
            String churchCode = uri.getPathSegments().get(1);
            count = db.update(CHURCH_TABLE_NAME, values, Church.ID + "=" + churchCode
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MessesInfo.AUTHORITY, Church.CONTENT_URI_NAME, CHURCH_FAVORITE);
        sUriMatcher.addURI(MessesInfo.AUTHORITY, Church.CONTENT_URI_NAME + "/*", CHURCH_FAVORITE_CODE);

        sMassFavoritesProjectionMap = new HashMap<String, String>();
        sMassFavoritesProjectionMap.put(Church._ID, Church._ID);
        sMassFavoritesProjectionMap.put(Church.ID, Church.ID);
        sMassFavoritesProjectionMap.put(Church.NAME, Church.NAME);
        sMassFavoritesProjectionMap.put(Church.ZIPCODE, Church.ZIPCODE);
        sMassFavoritesProjectionMap.put(Church.CITY, Church.CITY);
        sMassFavoritesProjectionMap.put(Church.COMMUNITY, Church.COMMUNITY);
        sMassFavoritesProjectionMap.put(Church.LAT, Church.LAT);
        sMassFavoritesProjectionMap.put(Church.LNG, Church.LNG);
    }

}
