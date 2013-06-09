package com.example.people;

import android.app.Activity;
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

  public static final String EXTRA_PERSON_ID = "com.example.people.EXTRAS.personId";
  
  private static final String EXTRA_ORIGINAL_FIRST = "com.example.people.EXTRAS.originalFirst";
  private static final String EXTRA_ORIGINAL_LAST = "com.example.people.EXTRAS.originalLast";
  
  private boolean mIsNew;
  private boolean mDeleted;
  private Uri mUri;
  
  private String mOriginalFirst;
  private String mOriginalLast;
  
  private EditText mFirstEditText;
  private EditText mLastEditText;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    Intent intent = getIntent();
    mIsNew = intent.getAction().equals(Intent.ACTION_INSERT);
    if (mIsNew) {
      ContentValues defaultValues = new ContentValues();
      defaultValues.put(Person.Columns.FIRST, "");
      defaultValues.put(Person.Columns.LAST, "");
      mUri = getContentResolver().insert(intent.getData(), defaultValues);
      if (mUri == null) {
        Toast.makeText(this, "Error creating person", Toast.LENGTH_SHORT).show();
        finish();
      }
    } else {
      mUri = intent.getData();
    }

    Cursor cursor = getContentResolver().query(mUri, Person.Columns.ALL, null, null, null);
    cursor.moveToFirst();
    Person person = new Person(cursor);
    
    if (savedInstanceState != null) {
      mOriginalFirst = savedInstanceState.getString(EXTRA_ORIGINAL_FIRST);
      mOriginalLast = savedInstanceState.getString(EXTRA_ORIGINAL_LAST);
    } else {
      mOriginalFirst = person.first;
      mOriginalLast = person.last;
    }    
    
    setContentView(R.layout.edit_person);
    mFirstEditText = (EditText) findViewById(R.id.first);
    mLastEditText = (EditText) findViewById(R.id.last);
    
    mFirstEditText.setText(person.first);
    mLastEditText.setText(person.last);
  }
  
  @Override
  protected void onResume() {
    super.onResume();
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
    setResult(Activity.RESULT_OK);
    finish();
  }
  
  private void discard() {
    if (mIsNew) {
      delete();
    } else {
      mFirstEditText.setText(mOriginalFirst);
      mLastEditText.setText(mOriginalLast);
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
    savePerson();
  }
  
  private void savePerson() {
    ContentValues values = new ContentValues();
    values.put(Person.Columns.FIRST, mFirstEditText.getText().toString());
    values.put(Person.Columns.LAST, mLastEditText.getText().toString());

    // TODO: save in background thread
    getContentResolver().update(mUri, values, null, null);
  }
  
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(EXTRA_ORIGINAL_FIRST, mOriginalFirst);
    outState.putString(EXTRA_ORIGINAL_LAST, mOriginalLast);
  }
}
