package com.example.people;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class PeopleProvider extends ContentProvider {
  
  private static class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
      super(context, "people.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + Person.TABLE + " ("
          + Person.Columns._ID + " INTEGER PRIMARY KEY,"
          + Person.Columns.FIRST + " TEXT,"
          + Person.Columns.LAST + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // Upgrade logic goes here
    }
  }
  
  private static final int PEOPLE_OP = 1;  // List, Insert
  private static final int PEOPLE_ID_OP = 2;  // Get, Update, Delete
  
  private static final UriMatcher sUriMatcher;
  private static HashMap<String, String> sProjectionMap;

  static {
    sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    sUriMatcher.addURI(Person.AUTHORITY, "people", PEOPLE_OP);
    sUriMatcher.addURI(Person.AUTHORITY, "people/#", PEOPLE_ID_OP);

    // Content provider exposes the same columns as underlying table
    sProjectionMap = new HashMap<String, String>();
    sProjectionMap.put(Person.Columns._ID, Person.Columns._ID);
    sProjectionMap.put(Person.Columns.FIRST, Person.Columns.FIRST);
    sProjectionMap.put(Person.Columns.LAST, Person.Columns.LAST);
  }
  
  private DatabaseHelper mDbHelper;
  
  @Override
  public boolean onCreate() {
    mDbHelper = new DatabaseHelper(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    qb.setTables(Person.TABLE);
    qb.setProjectionMap(sProjectionMap);

    // Return a specific person if the URI has an id; otherwise return everyone.
    switch (sUriMatcher.match(uri)) {
    case PEOPLE_OP:
      break;
    case PEOPLE_ID_OP:
      qb.appendWhere(Person.Columns._ID + "=" + ContentUris.parseId(uri));
      break;
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }

    Cursor c = qb.query(
        mDbHelper.getReadableDatabase(),
        projection,    // The columns to return from the query
        selection,     // The columns for the where clause
        selectionArgs, // The values for the where clause
        null,          // don't group the rows
        null,          // don't filter by row groups
        sortOrder      // The sort order
    );

    // Callers will get notified if the content at this URI changes.
    // For example, if a new person is inserted or an existing one is updated.
    c.setNotificationUri(getContext().getContentResolver(), uri);
    return c;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    if (sUriMatcher.match(uri) != PEOPLE_OP) {
      throw new IllegalArgumentException("Invalid URI " + uri);
    }

    long rowId = mDbHelper.getWritableDatabase().insert(Person.TABLE, null, values);
    if (rowId == -1) {
      return null;
    }
    
    Uri personUri = ContentUris.withAppendedId(Person.PEOPLE_ID_URI_BASE, rowId);
    getContext().getContentResolver().notifyChange(personUri, null);
    return personUri;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
    if (sUriMatcher.match(uri) != PEOPLE_ID_OP) {
      throw new IllegalArgumentException("Invalid URI " + uri);
    }
    
    long rowId = ContentUris.parseId(uri);
    int count = mDbHelper.getWritableDatabase().update(
        Person.TABLE,
        values,
        Person.Columns._ID + "=" + rowId,
        null);
    
    getContext().getContentResolver().notifyChange(uri, null);
    return count;    
  }
  
  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    if (sUriMatcher.match(uri) != PEOPLE_ID_OP) {
      throw new IllegalArgumentException("Invalid URI " + uri);
    }
    
    long rowId = ContentUris.parseId(uri);
    int count = mDbHelper.getWritableDatabase().delete(
        Person.TABLE,
        Person.Columns._ID + "=" + rowId,
        null
    );
    
    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }
}
