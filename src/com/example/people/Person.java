package com.example.people;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public final class Person implements Parcelable {

  public static final String TABLE = "people";

  public static final class Columns implements BaseColumns {
    public static final String FIRST = "first";
    public static final String LAST = "last";

    public static final String[] ALL = {
        _ID, FIRST, LAST
    };
  }



  public long id;
  public String first;
  public String last;

  public Person() {
    id = -1;
  }

  public Person(Cursor c) {
    id = c.getLong(c.getColumnIndexOrThrow(Columns._ID));
    first = c.getString(c.getColumnIndexOrThrow(Columns.FIRST));
    last = c.getString(c.getColumnIndexOrThrow(Columns.LAST));
  }

  public void populateValues(ContentValues values) {
    values.put(Columns.FIRST, first);
    values.put(Columns.LAST, last);
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

  public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
    public Person createFromParcel(Parcel in) {
      return new Person(in);
    }

    public Person[] newArray(int size) {
      return new Person[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }
}
