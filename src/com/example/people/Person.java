package com.example.people;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public final class Person {

  private static final String SCHEME = "content://";
  public static final String AUTHORITY = "com.example.people.provider";
  private static final String PEOPLE_PATH = "/people";
  private static final String PEOPLE_ID_PATH_BASE = "/people/";  
  
  public static final Uri PEOPLE_URI =  Uri.parse(SCHEME + AUTHORITY + PEOPLE_PATH);
  public static final Uri PEOPLE_ID_URI_BASE =  Uri.parse(SCHEME + AUTHORITY + PEOPLE_ID_PATH_BASE);
  
  public static final String TABLE = "people";
  
  public static final class Columns implements BaseColumns {
    public static final String FIRST = "first";
    public static final String LAST = "last";
    
    public static final String[] ALL = { _ID, FIRST, LAST };
  }
  
  long id;
  String first;
  String last;
  
  public Person(Cursor c) {
    id = c.getLong(c.getColumnIndexOrThrow(Columns._ID));
    first = c.getString(c.getColumnIndexOrThrow(Columns.FIRST));
    last = c.getString(c.getColumnIndexOrThrow(Columns.LAST));
  }
}
