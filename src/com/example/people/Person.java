
package com.example.people;

import android.database.Cursor;
import android.provider.BaseColumns;

public final class Person {

  public static final String TABLE = "people";

  public static final class Columns implements BaseColumns {
    public static final String FIRST = "first";
    public static final String LAST = "last";

    public static final String[] ALL = {
        _ID, FIRST, LAST
    };
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
