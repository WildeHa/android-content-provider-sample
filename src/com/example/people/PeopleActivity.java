
package com.example.people;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class PeopleActivity extends FragmentActivity implements
    LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

  private static final int PEOPLE_LOADER_ID = 0;

  private static final int DIALOG_SELECT_ACCOUNT = 0;

  private Settings mSettings;
  private AccountManager mAccountManager;

  private ListView mListView;
  private SimpleCursorAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.people);

    mSettings = new Settings(this);
    mAccountManager = AccountManager.get(this);

    mListView = (ListView)findViewById(android.R.id.list);
    mListView.setOnItemClickListener(this);
    registerForContextMenu(mListView);

    String[] fromColumns = {
        Person.Columns.FIRST, Person.Columns.LAST
    };
    int[] toViews = {
        android.R.id.text1, android.R.id.text2
    };

    // Create an empty adapter we will use to display the loaded data.
    // We pass null for the cursor, then update it in onLoadFinished()
    mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null,
        fromColumns, toViews, 0);
    mListView.setAdapter(mAdapter);

    getSupportLoaderManager().initLoader(PEOPLE_LOADER_ID, null, this);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    switch (id) {
      case PEOPLE_LOADER_ID:
        return new CursorLoader(this, // context
            PeopleProvider.PEOPLE_URI, // content provider URI
            Person.Columns.ALL, // Columns
            null, // selection
            null, // selection args
            null); // order
      default:
        return null;
    }
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mAdapter.swapCursor(null);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.people, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_new) {
      newPerson();
      return true;
    }
    if (item.getItemId() == R.id.menu_sync) {
      syncInBackground();
      return true;
    }
    if (item.getItemId() == R.id.menu_settings) {
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
      return true;
    }
    return false;
  }

  private void newPerson() {
    Intent intent = new Intent(this, EditPersonActivity.class);
    intent.setAction(Intent.ACTION_INSERT);
    intent.setData(PeopleProvider.PEOPLE_URI);
    startActivity(intent);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Intent intent = new Intent(this, EditPersonActivity.class);
    intent.setAction(Intent.ACTION_EDIT);
    intent.setData(ContentUris.withAppendedId(PeopleProvider.PEOPLE_ID_URI_BASE, id));
    startActivity(intent);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
    Person person = new Person((Cursor)mListView.getItemAtPosition(info.position));
    menu.setHeaderTitle(person.first + " " + person.last);
    getMenuInflater().inflate(R.menu.people_context, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    if (item.getItemId() == R.id.menu_delete) {
      deletePerson(info.id);
      return true;
    }
    return false;
  }

  private void deletePerson(long id) {
    Uri uri = ContentUris.withAppendedId(PeopleProvider.PEOPLE_ID_URI_BASE, id);
    getContentResolver().delete(uri, null, null);
  }

  private void syncInBackground() {
    String accountName = mSettings.getSyncAccount2();
    if (accountName == null) {
      selectAccount();
      return;
    }
    new AsyncTask<String, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(String... params) {
        try {
          String accountName = params[0];
          sync(accountName);
          return true;
        } catch (Exception e) {
          // TODO: log
          return false;
        }
      }

      @Override
      protected void onPostExecute(Boolean synced) {
        if (synced) {
          Toast.makeText(PeopleActivity.this, "Synced!", Toast.LENGTH_SHORT).show();
        } else {
          // TODO: silent, but show error in settings
          Toast.makeText(PeopleActivity.this, "Sync error!", Toast.LENGTH_SHORT).show();
        }
      }
    }.execute(accountName);
  }

  private void sync(String accountName) {
    Uri syncUri = PeopleProvider.SYNC_URI_BASE.buildUpon().appendPath(accountName).build();
    // May take a lot of syncs (20+)
    while (true) {
      Cursor cursor = getContentResolver().query(syncUri, Person.Columns.ALL, null, null, null);
      cursor.moveToFirst();
      String result = cursor.getString(0);
      cursor.close();
      if (result.startsWith("0;")) {
        return; // All done
      }
    }
  }

  private void selectAccount() {
    Account[] accounts = mAccountManager.getAccountsByType("com.google");
    if (accounts.length == 0) {
      // TODO: error
    } else if (accounts.length == 1) {
      setAccountInBackground(accounts[0].name);
    } else {
      showDialog(DIALOG_SELECT_ACCOUNT);
    }
  }

  private Dialog createAccountPickerDialog() {
    Account[] accounts = mAccountManager.getAccountsByType("com.google");
    final String[] accountNames = new String[accounts.length];
    final int[] selectedAccountIndices = new int[1];
    for (int i = 0; i < accounts.length; i++) {
      accountNames[i] = accounts[i].name;
    }
    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    dialog.setTitle("Choose an account");
    dialog.setSingleChoiceItems(accountNames, 0, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        selectedAccountIndices[0] = which;
      }
    });
    dialog.setCancelable(true);
    dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        setAccountInBackground(accountNames[selectedAccountIndices[0]]);
      }
    });
    return dialog.create();
  }

  private void setAccountInBackground(String accountName) {
    new AsyncTask<String, Void, Void>() {
      @Override
      protected Void doInBackground(String... params) {
        String accountName = params[0];
        setAccount(accountName);
        return null;
      }

      @Override
      protected void onPostExecute(Void result) {
        syncInBackground();
      }
    }.execute(accountName);
  }

  private void setAccount(String accountName) {
    ContentValues values = new ContentValues();
    values.put(PeopleProvider.KEY_USER_ACCOUNT_NAME, accountName);
    getContentResolver().insert(PeopleProvider.USERS_URI, values);
    mSettings.setSyncAccount2(accountName);
  }

  @Override
  @Deprecated
  protected Dialog onCreateDialog(int id) {
    if (DIALOG_SELECT_ACCOUNT == id) {
      return createAccountPickerDialog();
    }
    return null;
  }
}
