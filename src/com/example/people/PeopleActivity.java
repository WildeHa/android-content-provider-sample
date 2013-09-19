package com.example.people;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PeopleActivity extends FragmentActivity implements
    LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

  private static final int PEOPLE_LOADER_ID = 0;

  private ListView mListView;
  // private SimpleCursorAdapter mAdapter;
  private PeopleListAdapter mAdapter;
  private StickyHeaderListView mStickyHeader;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.people);

    mListView = (ListView) findViewById(android.R.id.list);
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
    // mAdapter = new SimpleCursorAdapter(this,
    // android.R.layout.simple_list_item_2, null,
    // fromColumns, toViews, 0);
    mAdapter = new PeopleListAdapter(this);
    mListView.setAdapter(mAdapter);

    mStickyHeader = (StickyHeaderListView) findViewById(R.id.list_container);
    mStickyHeader.setAdapter(mAdapter);

    //mListView.setOnScrollListener(
        new OnScrollListener() {

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
          int totalItemCount) {
        if (addedView) {
          return;
        }
//        TextView v = new TextView(PeopleActivity.this);
        if (mAdapter.isEmpty()) {
          return;
        }
        if (mListView.getChildAt(0).getTop() == 0) {
          return;
        }
        TextView v = (TextView) mAdapter.getView(0, null, mListView);
        v.measure(MeasureSpec.makeMeasureSpec(mListView.getWidth(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(mListView.getHeight(), MeasureSpec.AT_MOST));
        v.setTextColor(Color.BLUE);
        v.setText("----------");
        v.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        v.setVisibility(View.VISIBLE);
        mStickyHeader.addView(v);
        addedView = true;
      }
    };

    getSupportLoaderManager().initLoader(PEOPLE_LOADER_ID, null, this);
  }

  boolean addedView = false;

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    switch (id) {
      case PEOPLE_LOADER_ID:
        return new CursorLoader(this, Person.PEOPLE_URI, Person.Columns.ALL, null, null,
            Person.Columns.FIRST);
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
    return false;
  }

  private void newPerson() {
    Intent intent = new Intent(this, EditPersonActivity.class);
    intent.setAction(Intent.ACTION_INSERT);
    intent.setData(Person.PEOPLE_URI);
    startActivity(intent);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    editPerson(id);
  }

  private void editPerson(long id) {
    Intent intent = new Intent(this, EditPersonActivity.class);
    intent.setAction(Intent.ACTION_EDIT);
    intent.setData(ContentUris.withAppendedId(Person.PEOPLE_ID_URI_BASE, id));
    startActivity(intent);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    Person person = (Person) mListView.getItemAtPosition(info.position);
    menu.setHeaderTitle(person.first + " " + person.last);
    getMenuInflater().inflate(R.menu.people_context, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    if (item.getItemId() == R.id.menu_delete) {
      deletePerson(info.id);
      return true;
    }
    return false;
  }

  private void deletePerson(long id) {
    Uri uri = ContentUris.withAppendedId(Person.PEOPLE_ID_URI_BASE, id);
    getContentResolver().delete(uri, null, null);
  }
}
