package com.example.people;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditPersonActivity extends Activity {

  private Person mPerson;

  private EditText mFirstEditText;
  private EditText mLastEditText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mPerson = initializePerson();
    if (mPerson == null) {
      Toast.makeText(this, "Could not load person", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    setContentView(R.layout.edit_person);
    mFirstEditText = (EditText) findViewById(R.id.first);
    mLastEditText = (EditText) findViewById(R.id.last);

    populateForm(mPerson);
  }

  private Person initializePerson() {
    Intent intent = getIntent();
    if (Intent.ACTION_INSERT.equals(intent.getAction())) {
      return new Person();
    } else {
      // Load existing person from content provider
      long id;
      try {
        id = ContentUris.parseId(intent.getData());
      } catch (NumberFormatException e) {
        return null;
      }
      Uri uri = ContentUris.withAppendedId(PeopleProvider.PEOPLE_ID_URI_BASE, id);
      Cursor cursor = getContentResolver().query(uri, Person.Columns.ALL, null, null, null);
      if (cursor == null || !cursor.moveToFirst()) {
        return null;
      }
      return new Person(cursor);
    }
  }

  private void populateForm(Person person) {
    mFirstEditText.setText(person.first);
    mLastEditText.setText(person.last);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.edit_person, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_save:
        save();
        break;
      case R.id.menu_discard:
        discard();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void save() {
    mPerson.first = mFirstEditText.getText().toString();
    mPerson.last = mLastEditText.getText().toString();

    ContentValues values = new ContentValues();
    mPerson.populateValues(values);

    if (mPerson.id == -1) {
      getContentResolver().insert(PeopleProvider.PEOPLE_URI, values);
    } else {
      Uri uri = ContentUris.withAppendedId(PeopleProvider.PEOPLE_ID_URI_BASE, mPerson.id);
      getContentResolver().update(uri, values, null, null);
    }

    setResult(Activity.RESULT_OK);
    finish();
  }

  private void discard() {
    setResult(Activity.RESULT_CANCELED);
    finish();
  }
}
