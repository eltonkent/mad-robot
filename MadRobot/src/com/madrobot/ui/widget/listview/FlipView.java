package com.madrobot.ui.widget.listview;

import java.util.LinkedList;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.madrobot.R;

/**
 * Adapter view that scrolls by flipping either horizontally or vertically.
 * Similar to Flipboard's page flipping mechanism.
 * <p>
 * <table cellspacing="1" cellpadding="3">
 * <tr>
 * <th><b>Attribute</b></td>
 * <td width="50"><b>Type</b></td>
 * <td><b>Default</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td><code>flipMode</code></td>
 * <td>HORIZONTAL,<br/>
 * VERTICAL</td>
 * <td>VERTICAL</td>
 * <td>Direction of flipping.</td>
 * </tr>
 * <tr>
 * <td><code>animationBitmapFormat</code></td>
 * <td>ARGB_4444,<br/>
 * RGB_565,<br/>
 * ARGB_8888</td>
 * <td>ARGB_8888</td>
 * <td>Bitmap format used when animating the flips.<br/>
 * Heavy formats consume more memory but are more visually appealing</td>
 * </tr>
 * <tr>
 * <td><code>flipAcceleration</code></td>
 * <td>float</td>
 * <td>0.65</td>
 * <td>The acceleration of flipping pages.<b> its a value between 0 and 1 </b></td>
 * </tr>
 * <tr>
 * <td><code>movementRate</code></td>
 * <td>float</td>
 * <td>1.5</td>
 * <td>The movement rate of flipping pages.</td>
 * </tr>
 * <tr>
 * <td><code>maxTipAngle</code></td>
 * <td>integer</td>
 * <td>60</td>
 * <td>Angle at which the flipping page tips over</td>
 * </tr>
 * 
 * <tr>
 * <td><code>minimunMovement</code></td>
 * <td>float</td>
 * <td>4.0</td>
 * <td>Pixels to move to trigger a flip event</td>
 * </tr>
 * <tr>
 * <td><code>minimumMovement</code></td>
 * <td>float</td>
 * <td>4.0</td>
 * <td>Pixels to move to trigger a flip event</td>
 * </tr>
 * <tr>
 * <td><code>touchMovementAngle</code></td>
 * <td>integer</td>
 * <td>15</td>
 * <td>Movement angle of touch interception</td>
 * </tr>
 * </table>
 * <br/>
 * <b>Using FlipView</b><br/>
 * Using the flipview is simple as creating a layout and setting any adapter<br/>
 * <code>
 * &lt;com.madrobot.ui.widgets.listview.FlipView<br/>
 * &emsp;android:id="@+id/flipper"<br/>
 * &emsp;android:layout_width="300dp"<br/>
 * &emsp;android:layout_height="300dp"/&gt;
 * </code> <br/>.. in your code set the adapter <br/>
 * <code>
 * FlipView flipView=(FlipView)findViewById(R.id.flipper);
 * <font color="green">//now set any adapter </font
 * flipView.setAdapter(new ArrayListAdapter());
 * </code>
 * </p>
 * 
 * @author elton.kent
 * @see ViewFlipListener
 * 
 */
public class FlipView extends AdapterView<Adapter> {

	public static final int VERTICAL = 0;
	public static final int HORIZONTAL = 1;

	/**
	 * View flip listener
	 * 
	 * @author ekent4
	 * 
	 */
	public static interface ViewFlipListener {
		/**
		 * Trigerred when a view is flipped.
		 * 
		 * @param view
		 *            that was flipped
		 * @param position
		 *            in the adapter
		 */
		void onViewFlipped(View view, int position);
	}

	private static final int MAX_RELEASED_VIEW_SIZE = 1;

	private static final int MSG_SURFACE_CREATED = 1;

	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if (msg.what == MSG_SURFACE_CREATED) {
				contentWidth = 0;
				contentHeight = 0;
				requestLayout();
				return true;
			}
			return false;
		}
	});

	private GLSurfaceView surfaceView;
	private FlipRenderer renderer;
	private FlipCards cards;

	private int contentWidth;
	private int contentHeight;

	@ViewDebug.ExportedProperty
	private int flipOrientation;

	private boolean inFlipAnimation = false;

	// AdapterView Related
	private Adapter adapter;

	private int adapterDataCount = 0;

	private DataSetObserver adapterDataObserver;

	private final LinkedList<View> bufferedViews = new LinkedList<View>();
	private final LinkedList<View> releasedViews = new LinkedList<View>();
	private int bufferIndex = -1;
	private int adapterIndex = -1;
	private int sideBufferSize = 1;

	private float touchSlop;

	private ViewFlipListener onViewFlipListener;

	@ViewDebug.ExportedProperty
	private Bitmap.Config animationBitmapFormat = Bitmap.Config.ARGB_8888;

	public FlipView(Context context) {
		this(context, VERTICAL);
	}

	public FlipView(Context context, int flipOrientation) {
		super(context);
		init(context, flipOrientation, 0.65f, 1.5f, 60, 4f, 15);
	}

	/**
	 * Constructor required for XML inflation.
	 */
	public FlipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		int orientation = VERTICAL;

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlipView,
				0, 0);

		try {
			int value = a.getInteger(R.styleable.FlipView_flipMode, VERTICAL);
			if (value == HORIZONTAL)
				orientation = HORIZONTAL;

			value = a.getInteger(R.styleable.FlipView_animationBitmapFormat, 0);
			if (value == 1)
				setAnimationBitmapFormat(Bitmap.Config.ARGB_4444);
			else if (value == 2)
				setAnimationBitmapFormat(Bitmap.Config.RGB_565);
			else
				setAnimationBitmapFormat(Bitmap.Config.ARGB_8888);
		} finally {
			a.recycle();
		}
		float acceleration = a.getFloat(R.styleable.FlipView_flipAcceleration, 0.65f);
		float movementRate = a.getFloat(R.styleable.FlipView_movementRate, 1.5f);
		int maxTipAngle = a.getInteger(R.styleable.FlipView_maxTipAngle, 60);
		float minimumMovement = a.getFloat(R.styleable.FlipView_minimumMovement, 4f);
		int touchMoveAngle = a.getInteger(R.styleable.FlipView_touchMovementAngle, 15);
		init(context, orientation, acceleration, movementRate, maxTipAngle, minimumMovement,
				touchMoveAngle);
	}

	/**
	 * Constructor required for XML inflation.
	 */
	public FlipView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	private void init(Context context, int orientation, float acceleration,
			float movementRate, int maxTipAngle, float minMovement, int touchMoveAngle) {
		ViewConfiguration configuration = ViewConfiguration.get(getContext());
		touchSlop = configuration.getScaledTouchSlop();
		this.flipOrientation = orientation;
		setupSurfaceView(context, acceleration, movementRate, maxTipAngle, minMovement,
				touchMoveAngle);
	}

	public Bitmap.Config getAnimationBitmapFormat() {
		return animationBitmapFormat;
	}

	/**
	 * Set the bitmap config for the animation, default is ARGB_8888, which
	 * provides the best quality with large peak memory consumption.
	 * 
	 * @param animationBitmapFormat
	 *            ALPHA_8 is not supported and will throw exception when binding
	 *            textures
	 */
	public void setAnimationBitmapFormat(Bitmap.Config animationBitmapFormat) {
		this.animationBitmapFormat = animationBitmapFormat;
	}

	public ViewFlipListener getOnViewFlipListener() {
		return onViewFlipListener;
	}

	public void setOnViewFlipListener(ViewFlipListener onViewFlipListener) {
		this.onViewFlipListener = onViewFlipListener;
	}

	public void onResume() {
		surfaceView.onResume();
	}

	public void onPause() {
		surfaceView.onPause();
	}

	/**
	 * Request the animator to update display if the pageView has been
	 * preloaded.
	 * <p/>
	 * If the pageView is being used in the animation or its content has been
	 * buffered, the animator forcibly reloads it.
	 * <p/>
	 * The reloading process is a bit heavy for an active page, so please don't
	 * invoke it too frequently for an active page. The cost is trivial for
	 * inactive pages.
	 * 
	 * @param pageView
	 */
	public void refreshPage(View pageView) {
		if (cards.refreshPageView(pageView))
			requestLayout();
	}

	/**
	 * @param pageIndex
	 * @see #refreshPage(android.view.View)
	 */
	public void refreshPage(int pageIndex) {
		if (cards.refreshPage(pageIndex))
			requestLayout();
	}

	/**
	 * Force the animator reload all preloaded pages
	 */
	public void refreshAllPages() {
		cards.refreshAllPages();
		requestLayout();
	}

	// --------------------------------------------------------------------------------------------------------------------
	// Touch Event
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return cards.handleTouchEvent(event, false);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return cards.handleTouchEvent(event, true);
	}

	// --------------------------------------------------------------------------------------------------------------------
	// Orientation
	// protected void onConfigurationChanged(Configuration newConfig) {
	// super.onConfigurationChanged(newConfig);
	// }

	// --------------------------------------------------------------------------------------------------------------------
	// AdapterView<Adapter>
	@Override
	public Adapter getAdapter() {
		return adapter;
	}

	@Override
	public void setAdapter(Adapter adapter) {
		setAdapter(adapter, 0);
	}

	public void setAdapter(Adapter adapter, int initialPosition) {
		if (this.adapter != null)
			this.adapter.unregisterDataSetObserver(adapterDataObserver);

		this.adapter = adapter;
		adapterDataCount = adapter.getCount();

		adapterDataObserver = new MyDataSetObserver();
		this.adapter.registerDataSetObserver(adapterDataObserver);
		if (adapterDataCount > 0)
			setSelection(initialPosition);
	}

	@Override
	public View getSelectedView() {
		return (bufferIndex < bufferedViews.size() && bufferIndex >= 0) ? bufferedViews
				.get(bufferIndex) : null;
	}

	@Override
	public void setSelection(int position) {
		if (adapter == null)
			return;

		releaseViews();

		View selectedView = viewFromAdapter(position, true);
		bufferedViews.add(selectedView);

		for (int i = 1; i <= sideBufferSize; i++) {
			int previous = position - i;
			int next = position + i;

			if (previous >= 0)
				bufferedViews.addFirst(viewFromAdapter(previous, false));
			if (next < adapterDataCount)
				bufferedViews.addLast(viewFromAdapter(next, true));
		}

		bufferIndex = bufferedViews.indexOf(selectedView);
		adapterIndex = position;

		requestLayout();
		updateVisibleView(inFlipAnimation ? -1 : bufferIndex);

		cards.resetSelection(position, adapterDataCount);
	}

	@Override
	public int getSelectedItemPosition() {
		return adapterIndex;
	}

	// --------------------------------------------------------------------------------------------------------------------
	// Layout
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		for (View child : bufferedViews)
			child.layout(0, 0, r - l, b - t);

		if (changed || contentWidth == 0) {
			int w = r - l;
			int h = b - t;
			surfaceView.layout(0, 0, w, h);

			if (contentWidth != w || contentHeight != h) {
				contentWidth = w;
				contentHeight = h;
			}
		}

		if (bufferedViews.size() >= 1) {
			View frontView = bufferedViews.get(bufferIndex);
			View backView = null;
			if (bufferIndex < bufferedViews.size() - 1)
				backView = bufferedViews.get(bufferIndex + 1);
			renderer.updateTexture(adapterIndex, frontView, backView == null ? -1
					: adapterIndex + 1, backView);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		for (View child : bufferedViews)
			child.measure(widthMeasureSpec, heightMeasureSpec);

		surfaceView.measure(widthMeasureSpec, heightMeasureSpec);
	}

	// --------------------------------------------------------------------------------------------------------------------
	// internal exposed properties & methods
	float getTouchSlop() {
		return touchSlop;
	}

	GLSurfaceView getSurfaceView() {
		return surfaceView;
	}

	FlipRenderer getRenderer() {
		return renderer;
	}

	int getContentWidth() {
		return contentWidth;
	}

	int getContentHeight() {
		return contentHeight;
	}

	void reloadTexture() {
		handler.sendMessage(Message.obtain(handler, MSG_SURFACE_CREATED));
	}

	// --------------------------------------------------------------------------------------------------------------------
	// Internals
	private void setupSurfaceView(Context context, float acceleration, float movementRate,
			int maxTipAngle, float minMovement, int touchMoveAngle) {
		surfaceView = new GLSurfaceView(getContext());

		cards = new FlipCards(this, flipOrientation == VERTICAL, acceleration, movementRate,
				maxTipAngle, minMovement, touchMoveAngle);// 0.65f,1.5f,10,4f,15);
		renderer = new FlipRenderer(this, cards);

		surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		surfaceView.setZOrderOnTop(true);
		surfaceView.setRenderer(renderer);
		surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		addViewInLayout(surfaceView, -1, new AbsListView.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT), false);
	}

	private void releaseViews() {
		for (View view : bufferedViews)
			releaseView(view);
		bufferedViews.clear();
		bufferIndex = -1;
		adapterIndex = -1;
	}

	private void releaseView(View view) {
		detachViewFromParent(view);
		addReleasedView(view);
	}

	private void addReleasedView(View view) {
		if (releasedViews.size() < MAX_RELEASED_VIEW_SIZE)
			releasedViews.add(view);
	}

	private View viewFromAdapter(int position, boolean addToTop) {

		View releasedView = releasedViews.isEmpty() ? null : releasedViews.removeFirst();

		View view = adapter.getView(position, releasedView, this);
		if (releasedView != null && view != releasedView)
			addReleasedView(releasedView);

		setupAdapterView(view, addToTop, view == releasedView);
		return view;
	}

	private void setupAdapterView(View view, boolean addToTop, boolean isReusedView) {
		LayoutParams params = view.getLayoutParams();
		if (params == null) {
			params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 0);
		}

		if (isReusedView)
			attachViewToParent(view, addToTop ? 0 : 1, params);
		else
			addViewInLayout(view, addToTop ? 0 : 1, params, true);
	}

	private void updateVisibleView(int index) {
		/*
		 * if (AphidLog.ENABLE_DEBUG)
		 * AphidLog.d("Update visible views, index %d, buffered: %d, adapter %d"
		 * , index, bufferedViews.size(), adapterIndex);
		 */

		for (int i = 0; i < bufferedViews.size(); i++)
			bufferedViews.get(i).setVisibility(index == i ? VISIBLE : INVISIBLE);
	}

	void postFlippedToView(final int indexInAdapter) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				flippedToView(indexInAdapter, true);
			}
		});
	}

	void flippedToView(final int indexInAdapter, boolean isPost) {

		if (indexInAdapter >= 0 && indexInAdapter < adapterDataCount) {

			if (indexInAdapter == adapterIndex + 1) { // forward one page
				if (adapterIndex < adapterDataCount - 1) {
					adapterIndex++;
					View old = bufferedViews.get(bufferIndex);
					if (bufferIndex > 0)
						releaseView(bufferedViews.removeFirst());
					if (adapterIndex + sideBufferSize < adapterDataCount)
						bufferedViews.addLast(viewFromAdapter(adapterIndex + sideBufferSize,
								true));
					bufferIndex = bufferedViews.indexOf(old) + 1;
					requestLayout();
					updateVisibleView(inFlipAnimation ? -1 : bufferIndex);
				}
			} else if (indexInAdapter == adapterIndex - 1) {
				if (adapterIndex > 0) {
					adapterIndex--;
					View old = bufferedViews.get(bufferIndex);
					if (bufferIndex < bufferedViews.size() - 1)
						releaseView(bufferedViews.removeLast());
					if (adapterIndex - sideBufferSize >= 0)
						bufferedViews.addFirst(viewFromAdapter(adapterIndex - sideBufferSize,
								false));
					bufferIndex = bufferedViews.indexOf(old) - 1;
					requestLayout();
					updateVisibleView(inFlipAnimation ? -1 : bufferIndex);
				}
			}
		}
		// debugBufferedViews();
	}

	void showFlipAnimation() {
		if (!inFlipAnimation) {
			inFlipAnimation = true;

			cards.setVisible(true);
			surfaceView.requestRender();

			handler.postDelayed(new Runnable() { // use a delayed message to
													// avoid flicker, the
													// perfect solution would be
													// sending a message from
													// the GL thread
						@Override
						public void run() {
							if (inFlipAnimation)
								updateVisibleView(-1);
						}
					}, 100);
		}
	}

	void postHideFlipAnimation() {
		if (inFlipAnimation) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					hideFlipAnimation();
				}
			});
		}
	}

	private void hideFlipAnimation() {
		if (inFlipAnimation) {
			inFlipAnimation = false;

			updateVisibleView(bufferIndex);

			if (onViewFlipListener != null)
				onViewFlipListener.onViewFlipped(bufferedViews.get(bufferIndex), adapterIndex);

			handler.post(new Runnable() {
				@Override
				public void run() {
					if (!inFlipAnimation) {
						cards.setVisible(false);
						surfaceView.requestRender(); // ask OpenGL to clear its
														// display
					}
				}
			});
		}
	}

	private void onDataChanged() {
		adapterDataCount = adapter.getCount();
		int activeIndex;
		if (adapterIndex < 0)
			activeIndex = 0;
		else
			activeIndex = Math.min(adapterIndex, adapterDataCount - 1);

		releaseViews();
		setSelection(activeIndex);
	}

	private class MyDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			onDataChanged();
		}

		@Override
		public void onInvalidated() {
			onDataChanged();
		}
	}
}
