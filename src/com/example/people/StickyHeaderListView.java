package com.example.people;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StickyHeaderListView extends FrameLayout implements OnScrollListener {

  protected boolean mChildViewsCreated = false;

  protected Context mContext = null;
  protected Adapter mAdapter = null;
  protected HeaderIndexer mIndexer = null;
  protected TextView mStickyHeader = null;
  protected ListView mListView = null;

  protected int mCurrentSectionPos = -1; // Position of section that has its header on the top of the view
  protected int mNextSectionPosition = -1; // Position of next section's header
  protected int mListViewHeadersCount = 0;

  public interface HeaderIndexer {
    /**
     * Calculates the position of the header of a specific item in the adapter's
     * data set. For example: Assuming you have a list with albums and songs
     * names: Album A, song 1, song 2, ...., song 10, Album B, song 1, ..., song
     * 7. A call to this method with the position of song 5 in Album B, should
     * return the position of Album B.
     * 
     * @param position - Position of the item in the ListView dataset
     * @return Position of header. -1 if the is no header
     */

    int getHeaderPositionFromItemPosition(int position);

    /**
     * Calculates the number of items in the section defined by the header (not
     * including the header). For example: A list with albums and songs, the
     * method should return the number of songs names (without the album name).
     * 
     * @param headerPosition - the value returned by
     *          'getHeaderPositionFromItemPosition'
     * @return Number of items. -1 on error.
     */
    int getHeaderItemsNumber(int headerPosition);
  }

  public void setAdapter(Adapter adapter) {
    if (adapter != null) {
      mAdapter = adapter;
      mIndexer = (HeaderIndexer) adapter;
    }
  }

  public void setListView(ListView lv) {
    mListView = lv;
    mListView.setOnScrollListener(this);
    mListViewHeadersCount = mListView.getHeaderViewsCount();
  }

  public StickyHeaderListView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
    // Ignore
  }

  /**
   * Scroll events listener
   * 
   * @param view - the scrolled view
   * @param firstVisibleItem - the index (in the list's adapter) of the top
   *          visible item.
   * @param visibleItemCount - the number of visible items in the list
   * @param totalItemCount - the total number items in the list
   */
  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
      int totalItemCount) {
    updateStickyHeader(firstVisibleItem);
  }

  protected void updateStickyHeader(int firstVisibleItem) {
    firstVisibleItem -= mListViewHeadersCount;
    if (mAdapter != null && mIndexer != null) {

      // Get the section header position
      int sectionPos = mIndexer.getHeaderPositionFromItemPosition(firstVisibleItem);
      int sectionSize = mIndexer.getHeaderItemsNumber(sectionPos);

      if (sectionPos == -1 || sectionSize == -1) {
        return;
      }

      if (mStickyHeader == null) {
        float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        int leftMargin = (int) (8 * scale + 0.5f);
        int rightMargin = (int) (32 * scale + 0.5f);
        
        mStickyHeader = (TextView) LayoutInflater.from(mContext).inflate(R.layout.section_header,
            this, false);
        //mStickyHeader.setTextColor(Color.RED);
        mStickyHeader.setBackgroundResource(R.drawable.sticky_header);
        mStickyHeader.setPadding(leftMargin, 0, 0, 0);
        mStickyHeader.measure(MeasureSpec.makeMeasureSpec(mListView.getWidth(),
            MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mListView.getHeight(),
                    MeasureSpec.AT_MOST));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        
        layoutParams.setMargins(leftMargin, 0, rightMargin, 0);
        this.addView(mStickyHeader, layoutParams);
      }

      // New section
      if (sectionPos != mCurrentSectionPos) {
        mCurrentSectionPos = sectionPos;
        mNextSectionPosition = sectionSize + sectionPos + 1;
        String header = (String) mAdapter.getItem(sectionPos);
        mStickyHeader.setText(header);
      }

      // Do transitions
      // If position of bottom of last item in a section is smaller than the
      // height of the sticky header - shift drawable of header.
      int sectionLastItemPosition = mNextSectionPosition - firstVisibleItem - 1;
      int stickyHeaderHeight = mStickyHeader.getHeight();
      if (stickyHeaderHeight == 0) {
        stickyHeaderHeight = mStickyHeader.getMeasuredHeight();
      }

      View sectionLastView = mListView.getChildAt(sectionLastItemPosition);
      if (sectionLastView != null && sectionLastView.getBottom() <= stickyHeaderHeight) {
        int lastViewBottom = sectionLastView.getBottom();
        mStickyHeader.setTranslationY(lastViewBottom - stickyHeaderHeight);
      } else {
        mStickyHeader.setTranslationY(0);
      }
    }
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    if (!mChildViewsCreated) {
      setChildViews();
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (!mChildViewsCreated) {
      setChildViews();
    }
  }

  private void setChildViews() {
    // Find a child ListView (if any)
    int iChildNum = getChildCount();
    for (int i = 0; i < iChildNum; i++) {
      Object v = getChildAt(i);
      if (v instanceof ListView) {
        setListView((ListView) v);
      }
    }

    // No child ListView - add one
    if (mListView == null) {
      setListView(new ListView(mContext));
    }

    mChildViewsCreated = true;
  }

}
