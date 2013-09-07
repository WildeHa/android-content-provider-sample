
package com.example.people;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public final class Person implements Parcelable {

  // Content provider constants

  private static final String SCHEME = "content://";
  public static final String AUTHORITY = "com.example.people.provider";
  private static final String PEOPLE_PATH = "/people";
  private static final String PEOPLE_ID_PATH_BASE = "/people/";

  public static final Uri PEOPLE_URI = Uri.parse(SCHEME + AUTHORITY + PEOPLE_PATH);
  public static final Uri PEOPLE_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PEOPLE_ID_PATH_BASE);

  // Database constants

  public static final String TABLE = "people";

  public static final class Columns implements BaseColumns {
    public static final String FIRST = "first";
    public static final String LAST = "last";

    public static final String[] ALL = {
        _ID, FIRST, LAST
    };
  }

  public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
    public Person createFromParcel(Parcel in) {
      return new Person(in);
    }

    public Person[] newArray(int size) {
      return new Person[size];
    }
  };

  public final long id;
  public String first;
  public String last;

  public Person(Cursor c) {
    id = c.getLong(c.getColumnIndexOrThrow(Columns._ID));
    first = c.getString(c.getColumnIndexOrThrow(Columns.FIRST));
    last = c.getString(c.getColumnIndexOrThrow(Columns.LAST));
  }

  private Person(Parcel in) {
    id = in.readLong();
    first = in.readString();
    last = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(id);
    dest.writeString(first);
    dest.writeString(last);
  }

  @Override
  public int describeContents() {
    return 0;
  }
}
