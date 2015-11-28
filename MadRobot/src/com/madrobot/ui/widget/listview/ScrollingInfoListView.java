package com.madrobot.ui.widget.listview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.madrobot.R;

/**
 * Listview that attaches a custom view to the scrollbar as the list is
 * scrolling
 * <p>
 * <code>
 * <pre>
 * <img src="../../../../../resources/demo_capture.png"> <br/>
 * <b>Layout</b><br/>
 * &lt;com.madrobot.ui.widgets.listview.ExtendedListView<br/>
 * android:id="@android:id/list" android:layout_height="fill_parent"<br/>
 * android:layout_width="fill_parent"<br/>
 * app:scrollBarPanel="@layout/<YOUR_SCROLLBARPANEL_LAYOUT>"<br/>
 * app:scrollBarPanelInAnimation="@anim/YOUR_ANIMATION"<br/>
 * app:scrollBarPanelOutAnimation="@anim/YOUR_ANIMATION" /><br/>
 * </pre>
 * </code><br/>
 * <b>Code</b><br/>
 * 
 * <pre>
 * <code>
 * // Set your scrollBarPanel
 * ScrollingInfoListView listView = (ScrollingInfoListView) findViewById(android.R.id.list);
 * // Attach a position changed listener on the listview and play with your
 * // scrollBarPanel
 * // when you need to update its content
 * mListView.setOnPositionChangedListener(new OnPositionChangedListener() {
 * 	&#064;Override
 * 	public void onPositionChanged(ExtendedListView listView, int firstVisiblePosition,View scrollBarPanel) {
 * 		((TextView) scrollBarPanel).setText(&quot;Position &quot; + firstVisiblePosition);
 * 	}
 * });
 * 
 * </code>
 * </pre>
 * 
 * </p>
 * 
 * @author elton.stephen.kent
 * 
 */
public class ScrollingInfoListView extends ListView implements OnScrollListener {

	public static interface OnPositionChangedListener {

		public void onPositionChanged(ScrollingInfoListView listView, int position,
				View scrollBarPanel);

	}

	private OnScrollListener mOnScrollListener = null;

	private View mScrollBarPanel = null;
	private int mScrollBarPanelPosition = 0;

	private OnPositionChangedListener mPositionChangedListener;
	private int mLastPosition = -1;

	private Animation mInAnimation = null;
	private Animation mOutAnimation = null;

	private final Handler mHandler = new Handler();

	private final Runnable mScrollBarPanelFadeRunnable = new Runnable() {

		@Override
		public void run() {
			if (mOutAnimation != null) {
				mScrollBarPanel.startAnimation(mOutAnimation);
			}
		}
	};

	/*
	 * keep track of Measure Spec
	 */
	private int mWidthMeasureSpec;
	private int mHeightMeasureSpec;

	public ScrollingInfoListView(Context context) {
		this(context, null);
	}

	public ScrollingInfoListView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public ScrollingInfoListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		super.setOnScrollListener(this);

		final TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ScrollingInfoListView);
		final int scrollBarPanelLayoutId = a.getResourceId(
				R.styleable.ScrollingInfoListView_scrollBarPanel, -1);
		final int scrollBarPanelInAnimation = a.getResourceId(
				R.styleable.ScrollingInfoListView_scrollBarPanelInAnimation, -1);
		final int scrollBarPanelOutAnimation = a.getResourceId(
				R.styleable.ScrollingInfoListView_scrollBarPanelOutAnimation, -1);
		a.recycle();

		if (scrollBarPanelLayoutId != -1) {
			setScrollBarPanel(scrollBarPanelLayoutId);
		}

		final int scrollBarPanelFadeDuration = ViewConfiguration.getScrollBarFadeDuration();

		if (scrollBarPanelInAnimation > 0) {
			mInAnimation = AnimationUtils.loadAnimation(getContext(),
					scrollBarPanelInAnimation);
		}

		if (scrollBarPanelOutAnimation > 0) {
			mOutAnimation = AnimationUtils.loadAnimation(getContext(),
					scrollBarPanelOutAnimation);
			mOutAnimation.setDuration(scrollBarPanelFadeDuration);

			mOutAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (mScrollBarPanel != null) {
						mScrollBarPanel.setVisibility(View.GONE);
					}
				}
			});
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		if (null != mPositionChangedListener && null != mScrollBarPanel) {

			// Don't do anything if there is no itemviews
			if (totalItemCount > 0) {
				/*
				 * from android source code (ScrollBarDrawable.java)
				 */
				final int thickness = getVerticalScrollbarWidth();
				int height = Math.round((float) getMeasuredHeight()
						* computeVerticalScrollExtent() / computeVerticalScrollRange());
				int thumbOffset = Math.round((float) (getMeasuredHeight() - height)
						* computeVerticalScrollOffset()
						/ (computeVerticalScrollRange() - computeVerticalScrollExtent()));
				final int minLength = thickness * 2;
				if (height < minLength) {
					height = minLength;
				}
				thumbOffset += height / 2;

				/*
				 * find out which itemviews the center of thumb is on
				 */
				final int count = getChildCount();
				for (int i = 0; i < count; ++i) {
					final View childView = getChildAt(i);
					if (childView != null) {
						if (thumbOffset > childView.getTop()
								&& thumbOffset < childView.getBottom()) {
							/*
							 * we have our candidate
							 */
							if (mLastPosition != firstVisibleItem + i) {
								mLastPosition = firstVisibleItem + i;

								/*
								 * inform the position of the panel has changed
								 */
								mPositionChangedListener.onPositionChanged(this,
										mLastPosition, mScrollBarPanel);

								/*
								 * measure panel right now since it has just
								 * changed
								 * 
								 * INFO: quick hack to handle TextView has
								 * ScrollBarPanel (to wrap text in case
								 * TextView's content has changed)
								 */
								measureChild(mScrollBarPanel, mWidthMeasureSpec,
										mHeightMeasureSpec);
							}
							break;
						}
					}
				}

				/*
				 * update panel position
				 */
				mScrollBarPanelPosition = thumbOffset - mScrollBarPanel.getMeasuredHeight()
						/ 2;
				final int x = getMeasuredWidth() - mScrollBarPanel.getMeasuredWidth()
						- getVerticalScrollbarWidth();
				mScrollBarPanel.layout(x, mScrollBarPanelPosition,
						x + mScrollBarPanel.getMeasuredWidth(), mScrollBarPanelPosition
								+ mScrollBarPanel.getMeasuredHeight());
			}
		}

		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	public void setOnPositionChangedListener(
			OnPositionChangedListener onPositionChangedListener) {
		mPositionChangedListener = onPositionChangedListener;
	}

	@Override
	public void setOnScrollListener(OnScrollListener onScrollListener) {
		mOnScrollListener = onScrollListener;
	}

	public void setScrollBarPanel(View scrollBarPanel) {
		mScrollBarPanel = scrollBarPanel;
		mScrollBarPanel.setVisibility(View.GONE);
		requestLayout();
	}

	public void setScrollBarPanel(int resId) {
		setScrollBarPanel(LayoutInflater.from(getContext()).inflate(resId, this, false));
	}

	public View getScrollBarPanel() {
		return mScrollBarPanel;
	}

	@Override
	protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
		final boolean isAnimationPlayed = super.awakenScrollBars(startDelay, invalidate);

		if (isAnimationPlayed == true && mScrollBarPanel != null) {
			if (mScrollBarPanel.getVisibility() == View.GONE) {
				mScrollBarPanel.setVisibility(View.VISIBLE);
				if (mInAnimation != null) {
					mScrollBarPanel.startAnimation(mInAnimation);
				}
			}

			mHandler.removeCallbacks(mScrollBarPanelFadeRunnable);
			mHandler.postAtTime(mScrollBarPanelFadeRunnable,
					AnimationUtils.currentAnimationTimeMillis() + startDelay);
		}

		return isAnimationPlayed;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (mScrollBarPanel != null && getAdapter() != null) {
			mWidthMeasureSpec = widthMeasureSpec;
			mHeightMeasureSpec = heightMeasureSpec;
			measureChild(mScrollBarPanel, widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (mScrollBarPanel != null) {
			final int x = getMeasuredWidth() - mScrollBarPanel.getMeasuredWidth()
					- getVerticalScrollbarWidth();
			mScrollBarPanel.layout(x, mScrollBarPanelPosition,
					x + mScrollBarPanel.getMeasuredWidth(), mScrollBarPanelPosition
							+ mScrollBarPanel.getMeasuredHeight());
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (mScrollBarPanel != null && mScrollBarPanel.getVisibility() == View.VISIBLE) {
			drawChild(canvas, mScrollBarPanel, getDrawingTime());
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		mHandler.removeCallbacks(mScrollBarPanelFadeRunnable);
	}
}