
package com.example.people;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.zumero.sqlite.SQLiteDatabase;
import com.zumero.sqlite.SQLiteOpenHelper;
import com.zumero.sqlite.SQLiteQueryBuilder;

public class PeopleProvider extends ContentProvider {

  private static class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
      super(context, "people.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE VIRTUAL TABLE people using zumero ("
          + Person.Columns._ID + " INTEGER PRIMARY KEY,"
          + Person.Columns.FIRST + " TEXT,"
          + Person.Columns.LAST + " TEXT);");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
      // TODO: in API 16+, call setForeignKeyConstraintsEnabled in onConfigure
      if (!db.isReadOnly()) {
        db.execSQL("PRAGMA foreign_keys=ON;");
      }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (oldVersion < 2 && newVersion >= 2) {
        upgradeToVersion2(db);
      }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
      ZumeroBackend.adoptTable(db, "people");
    }
  }

  public static final String SCHEME = "content://";
  public static final String AUTHORITY = "com.example.people.provider";

  private static final String PEOPLE_PATH = "/people";
  private static final String PEOPLE_ID_PATH_BASE = "/people/";
  private static final String SYNC_PATH_BASE = "/sync/";
  private static final String USERS_PATH = "/users";

  public static final Uri PEOPLE_URI = Uri.parse(SCHEME + AUTHORITY + PEOPLE_PATH);
  public static final Uri PEOPLE_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PEOPLE_ID_PATH_BASE);
  public static final Uri SYNC_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SYNC_PATH_BASE);
  public static final Uri USERS_URI = Uri.parse(SCHEME + AUTHORITY + USERS_PATH);

  public static final String KEY_USER_ACCOUNT_NAME = "userAccountName";

  private static final int PEOPLE_OP = 1;
  private static final int PEOPLE_ID_OP = 2;
  private static final int SYNC_OP = 3;
  private static final int USERS_OP = 4;

  private static final UriMatcher sUriMatcher;
  private static HashMap<String, String> sProjectionMap;

  static {
    sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    sUriMatcher.addURI(AUTHORITY, "people", PEOPLE_OP);
    sUriMatcher.addURI(AUTHORITY, "people/#", PEOPLE_ID_OP);
    sUriMatcher.addURI(AUTHORITY, "sync/*", SYNC_OP);
    sUriMatcher.addURI(AUTHORITY, "users", USERS_OP);

    sProjectionMap = new HashMap<String, String>();
    sProjectionMap.put(Person.Columns._ID, Person.Columns._ID);
    sProjectionMap.put(Person.Columns.FIRST, Person.Columns.FIRST);
    sProjectionMap.put(Person.Columns.LAST, Person.Columns.LAST);
  }

  private DatabaseHelper mDbHelper;

  @Override
  public boolean onCreate() {
    mDbHelper = new DatabaseHelper(getContext());

    // Open the db for writing and then immediately close it. This ensures that
    // if we have to upgrade the database, it happens on a writable db handle.
    // Otherwise, we may get an error about the upgrade failing on read-only db.
    mDbHelper.getWritableDatabase();
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    qb.setTables(Person.TABLE);
    qb.setProjectionMap(sProjectionMap);

    switch (sUriMatcher.match(uri)) {
      case PEOPLE_OP:
        break;
      case PEOPLE_ID_OP:
        qb.appendWhere(Person.Columns._ID + "=" + ContentUris.parseId(uri));
        break;
      case SYNC_OP:
        String accountName = uri.getLastPathSegment();
        Cursor c = ZumeroBackend.sync(getContext(), mDbHelper.getWritableDatabase(), accountName);
        getContext().getContentResolver().notifyChange(PEOPLE_URI, null);
        return c;
      default:
        // If the URI doesn't match any of the known patterns, throw an
        // exception.
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    Cursor c = qb.query(mDbHelper.getReadableDatabase(), projection, // The
                                                                     // columns
                                                                     // to
                                                                     // return
                                                                     // from the
                                                                     // query
        selection, // The columns for the where clause
        selectionArgs, // The values for the where clause
        null, // don't group the rows
        null, // don't filter by row groups
        sortOrder // The sort order
        );
    c.setNotificationUri(getContext().getContentResolver(), uri);
    return c;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    switch (sUriMatcher.match(uri)) {
      case PEOPLE_OP:
        break;
      case USERS_OP:
        String accountName = values.getAsString(KEY_USER_ACCOUNT_NAME);
        ZumeroBackend.addUser(mDbHelper.getWritableDatabase(), accountName);
        return USERS_URI.buildUpon().appendPath(accountName).build();
      default:
        throw new IllegalArgumentException("Invalid URI " + uri);
    }

    long rowId = mDbHelper.getWritableDatabase().insert(Person.TABLE, null, values);
    if (rowId == -1) {
      return null;
    }

    Uri personUri = ContentUris.withAppendedId(PEOPLE_ID_URI_BASE, rowId);
    getContext().getContentResolver().notifyChange(personUri, null);
    return personUri;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    if (sUriMatcher.match(uri) != PEOPLE_ID_OP) {
      throw new IllegalArgumentException("Invalid URI " + uri);
    }

    long rowId = ContentUris.parseId(uri);
    int count = mDbHelper.getWritableDatabase().update(Person.TABLE, values,
        Person.Columns._ID + "=" + rowId, null);

    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    if (sUriMatcher.match(uri) != PEOPLE_ID_OP) {
      throw new IllegalArgumentException("Invalid URI " + uri);
    }

    long rowId = ContentUris.parseId(uri);
    int count = mDbHelper.getWritableDatabase().delete(Person.TABLE,
        Person.Columns._ID + "=" + rowId, null);

    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }
}
