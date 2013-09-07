
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

  private static final String EXTRA_ORIGINAL_PERSON = "com.example.people.EXTRAS.originalPerson";

  private boolean mDeleted;
  private Uri mUri;

  // The Person as it existed when this activity first started.
  // Revert to this if the user taps Cancel.
  private Person mOriginalPerson;

  private EditText mFirstEditText;
  private EditText mLastEditText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Get the person id (if available) from the intent data URI.
    Intent intent = getIntent();
    long id = -1;
    try {
      id = ContentUris.parseId(intent.getData());
    } catch (NumberFormatException e) {
      // That's ok; the caller didn't send a person id because
      // they are creating a new one. It's somewhat bad form to
      // use exceptions for flow control, but whatever, this is
      // just a sample app.
    }

    if (id == -1) {
      // Need to create a new Person.
      ContentValues defaultValues = new ContentValues();
      defaultValues.put(Person.Columns.FIRST, "");
      defaultValues.put(Person.Columns.LAST, "");
      mUri = getContentResolver().insert(intent.getData(), defaultValues);
      if (mUri == null) {
        Toast.makeText(this, "Error creating person", Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
      // Update the intent data URI so it includes the new person id.
      // Then if the activity is restarted (e.g. the screen is rotated),
      // we preserve the id of the person we just created above.
      intent.setData(mUri);
      setIntent(intent);
    } else {
      // We're editing an existing person.
      mUri = intent.getData();
    }

    // Load the Person from the content provider.
    Cursor cursor = getContentResolver().query(mUri, Person.Columns.ALL, null, null, null);
    if (!cursor.moveToFirst()) {
      Toast.makeText(this, "Missing person", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    Person person = new Person(cursor);

    // Save/restore the state of the Person as it existed when the activity
    // first started. We revert to it if the user taps Cancel.
    if (savedInstanceState != null) {
      mOriginalPerson = savedInstanceState.getParcelable(EXTRA_ORIGINAL_PERSON);
    } else {
      mOriginalPerson = person;
    }

    setContentView(R.layout.edit_person);
    mFirstEditText = (EditText) findViewById(R.id.first);
    mLastEditText = (EditText) findViewById(R.id.last);

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
    // The actual save operation happens in onPause.
    // All we need to do here is finish the activity.
    setResult(Activity.RESULT_OK);
    finish();
  }

  private void discard() {
    if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
      // We've already created a new Person; now we need to delete it.
      delete();
    } else {
      // Revert to the original Person before we save.
      mFirstEditText.setText(mOriginalPerson.first);
      mLastEditText.setText(mOriginalPerson.last);
    }
    setResult(Activity.RESULT_CANCELED);
    finish();
  }

  private void delete() {
    getContentResolver().delete(mUri, null, null);
    mDeleted = true;
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mDeleted) {
      return;
    }
    // Save the Person any time the activity goes into the background,
    // even if the user has not explicitly saved his changes. For example,
    // he might have tapped the Home button to start another activity, or he
    // might have received an incoming phone call.
    savePerson();
  }

  private void savePerson() {
    ContentValues values = new ContentValues();
    values.put(Person.Columns.FIRST, mFirstEditText.getText().toString());
    values.put(Person.Columns.LAST, mLastEditText.getText().toString());
    getContentResolver().update(mUri, values, null, null);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(EXTRA_ORIGINAL_PERSON, mOriginalPerson);
  }
}
