package com.example.people;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PeopleListAdapter extends BaseAdapter {

  private static final int TYPE_PERSON = 0;
  private static final int TYPE_HEADER = 1;

  private final LayoutInflater mLayoutInflater;
  private Object[] mItems;

  public PeopleListAdapter(Context context) {
    mLayoutInflater = LayoutInflater.from(context);
    mItems = new Object[0];
  }

  public void swapCursor(Cursor cursor) {
    if (cursor == null) {
      setItems(new Object[0]);
      return;
    }
    List<Object> items = new ArrayList<Object>();

    String lastHeader = null;
    cursor.moveToFirst();
    while (cursor.moveToNext()) {
      Person person = new Person(cursor);
      String header = null;
      if (person.first.isEmpty()) {
        header = "(NONE)";
      } else {
        header = Character.toString(person.first.charAt(0));
      }
      if (!header.equals(lastHeader)) {
        items.add(header);
        lastHeader = header;
      }
      items.add(person);
    }
    setItems(items.toArray());
  }

  public void setItems(Object[] items) {
    mItems = items;
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return mItems.length;
  }

  @Override
  public Object getItem(int position) {
    return mItems[position];
  }

  @Override
  public long getItemId(int position) {
    if (getItemViewType(position) == TYPE_PERSON) {
      return ((Person) getItem(position)).id;
    }
    return 1000 + ((String) getItem(position)).charAt(0);
  }

  @Override
  public int getViewTypeCount() {
    return 2;
  }

  @Override
  public int getItemViewType(int position) {
    return (getItem(position) instanceof Person) ? TYPE_PERSON : TYPE_HEADER;
  }

  @Override
  public boolean areAllItemsEnabled() {
    return false;
  }

  @Override
  public boolean isEnabled(int position) {
    return getItemViewType(position) == TYPE_PERSON;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    int type = getItemViewType(position);

    if (convertView == null) {
      int resource = (type == TYPE_PERSON) ? android.R.layout.simple_list_item_2
          : R.layout.section_header;
      convertView = mLayoutInflater.inflate(resource, parent, false);
    }

    if (type == TYPE_PERSON) {
      Person person = (Person) getItem(position);
      ((TextView) convertView.findViewById(android.R.id.text1)).setText(person.first);
      ((TextView) convertView.findViewById(android.R.id.text2)).setText(person.last);
    } else {
      String header = (String) getItem(position);
      ((TextView) convertView.findViewById(R.id.separator)).setText(header);
    }

    return convertView;
  }
}
