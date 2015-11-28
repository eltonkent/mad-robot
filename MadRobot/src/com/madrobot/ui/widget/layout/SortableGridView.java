package com.madrobot.ui.widget.layout;

import java.util.Collections;
import java.util.ArrayList;

import com.madrobot.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

/**
 * GridView layout that allows you to drag and sort children
 * 
 * <p>
 * <b>Attributes</b><br/>
 * <table cellspacing="1" cellpadding="3">
 * <tr>
 * <th><b>Attribute</b></td>
 * <td width="50"><b>Type</b></td>
 * <td><b>Default</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td><code>animationSpeed</code></td>
 * <td>integer</td>
 * <td>150</td>
 * <td>Animation time in milliseconds. Lower values result in faster shifting animations</td>
 * </tr>
 * </table>
 * <b>Layout</b><br/>
 * <code>
 * &lt;com.madrobot.ui.widget.layout.SortableGridView<br/>
 * android:layout_width="match_parent"<br/>
 * android:layout_height="match_parent"&gt;<br/>
 *  &lt;View <br/>
 *  ..<br/>
 *  ..<br/>
 *  /&gt;<br/>
 *  <br/>
 *  &lt;View<br/>
 *  ..<br/>
 *  ..<br/>
 *  /&gt;<br/>
 * 
 * &lt;/com.madrobot.ui.widget.layout.SortableGridView&gt;
 * </code> <br/>
 * <b>Demo</b><br/>
 * <center><OBJECT CLASSID="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
 * WIDTH="428" HEIGHT="716" CODEBASE
 * ="http://active.macromedia.com/flash5/cabs/swflash.cab#version=7,0,0,0">
 * <PARAM NAME=movie
 * VALUE="../../../../../resources/demos/draggablegridview.swf"> <PARAM
 * NAME=play VALUE=true> <PARAM NAME=loop VALUE=false> <PARAM NAME=wmode
 * VALUE=transparent> <PARAM NAME=quality VALUE=low> <EMBED
 * SRC="../../../../../resources/demos/draggablegridview.swf" WIDTH=428
 * HEIGHT=716 quality=low loop=false wmode=transparent
 * TYPE="application/x-shockwave-flash" PLUGINSPAGE=
 * "http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"
 * > </EMBED> </OBJECT></center> <SCRIPT
 * src='../../../../../resources/demos/pagecurl.js'></script>
 * </p>
 * 
 * @author elton.kent
 * 
 */
public class SortableGridView extends ViewGroup implements View.OnTouchListener,
		View.OnClickListener, View.OnLongClickListener {
	// layout vars
	public static float childRatio = .9f;
	private int colCount;
	private int childSize;
	private int padding;
	private int dpi;
	private int scroll = 0;
	private float lastDelta = 0;
	private final Handler handler = new Handler();
	// dragging vars
	private int dragged = -1;
	private int lastX = -1;
	private int lastY = -1;
	private int lastTarget = -1;
	private boolean enabled = true;
	private boolean touching = false;
	// anim vars
	private static int animTime = 50;
	private final ArrayList<Integer> newPositions = new ArrayList<Integer>();
	// listeners
	private OnRearrangeListener onRearrangeListener;
	private OnClickListener secondaryOnClickListener;
	private OnItemClickListener onItemClickListener;

	public SortableGridView(Context context) {
		this(context, null);
	}

	public SortableGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SortableGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SortableGridView);
		animTime = a.getInteger(R.styleable.SortableGridView_animationSpeed, 150);
		setListeners();
		handler.removeCallbacks(updateTask);
		handler.postAtTime(updateTask, SystemClock.uptimeMillis() + 500);
		setChildrenDrawingOrderEnabled(true);

		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		dpi = metrics.densityDpi;
	}

	protected void setListeners() {
		setOnTouchListener(this);
		super.setOnClickListener(this);
		setOnLongClickListener(this);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		secondaryOnClickListener = l;
	}

	protected Runnable updateTask = new Runnable() {
		public void run() {
			if (dragged != -1) {
				if (lastY < padding * 3 && scroll > 0)
					scroll -= 20;
				else if (lastY > getBottom() - getTop() - (padding * 3)
						&& scroll < getMaxScroll())
					scroll += 20;
			} else if (lastDelta != 0 && !touching) {
				scroll += lastDelta;
				lastDelta *= .9;
				if (Math.abs(lastDelta) < .25)
					lastDelta = 0;
			}
			clampScroll();
			onLayout(true, getLeft(), getTop(), getRight(), getBottom());

			handler.postDelayed(this, 25);
		}
	};

	@Override
	public void addView(View child) {
		super.addView(child);
		newPositions.add(-1);
	};

	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		newPositions.add(-1);
	}

	@Override
	public void removeViewAt(int index) {
		super.removeViewAt(index);
		newPositions.remove(index);
	};

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// compute width of view, in dp
		float w = (r - l) / (dpi / 160f);

		// determine number of columns, at least 2
		colCount = 2;
		int sub = 240;
		w -= 280;
		while (w > 0) {
			colCount++;
			w -= sub;
			sub += 40;
		}

//		 //determine number of columns for 50dp children
//        colCount = w / 50;
		
		// determine childSize and padding, in px
		childSize = (r - l) / colCount;
		childSize = Math.round(childSize * childRatio);
		padding = ((r - l) - (childSize * colCount)) / (colCount + 1);

		for (int i = 0; i < getChildCount(); i++)
			if (i != dragged) {
				Point xy = getCoordinateFromIndex(i);
				getChildAt(i).layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
			}
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if (dragged == -1)
			return i;
		else if (i == childCount - 1)
			return dragged;
		else if (i >= dragged)
			return i + 1;
		return i;
	}

	public int getIndexFromCoordinate(int x, int y) {
		int col = getColorRowFromCoor(x), row = getColorRowFromCoor(y + scroll);
		if (col == -1 || row == -1) // touch is between columns or rows
			return -1;
		int index = row * colCount + col;
		if (index >= getChildCount())
			return -1;
		return index;
	}

	private int getColorRowFromCoor(int coor) {
		coor -= padding;
		for (int i = 0; coor > 0; i++) {
			if (coor < childSize)
				return i;
			coor -= (childSize + padding);
		}
		return -1;
	}

	private int getTargetFromCoordinate(int x, int y) {
		if (getColorRowFromCoor(y + scroll) == -1) // touch is between rows
			return -1;
		// if (getIndexFromCoor(x, y) != -1) //touch on top of another visual
		// return -1;

		int leftPos = getIndexFromCoordinate(x - (childSize / 4), y);
		int rightPos = getIndexFromCoordinate(x + (childSize / 4), y);
		if (leftPos == -1 && rightPos == -1) // touch is in the middle of
												// nowhere
			return -1;
		if (leftPos == rightPos) // touch is in the middle of a visual
			return -1;

		int target = -1;
		if (rightPos > -1)
			target = rightPos;
		else if (leftPos > -1)
			target = leftPos + 1;
		if (dragged < target)
			return target - 1;

		// Toast.makeText(getContext(), "Target: " + target + ".",
		// Toast.LENGTH_SHORT).show();
		return target;
	}

	protected Point getCoordinateFromIndex(int index) {
		int col = index % colCount;
		int row = index / colCount;
		return new Point(padding + (childSize + padding) * col, padding
				+ (childSize + padding) * row - scroll);
	}

	public int getIndexOf(View child) {
		for (int i = 0; i < getChildCount(); i++)
			if (getChildAt(i) == child)
				return i;
		return -1;
	}

	// EVENT HANDLERS
	public void onClick(View view) {
		if (enabled) {
			if (secondaryOnClickListener != null)
				secondaryOnClickListener.onClick(view);
			if (onItemClickListener != null && getLastIndex() != -1)
				onItemClickListener.onItemClick(null, getChildAt(getLastIndex()),
						getLastIndex(), getLastIndex() / colCount);
		}
	}

	public boolean onLongClick(View view) {
		if (!enabled)
			return false;
		int index = getLastIndex();
		if (index != -1) {
			dragged = index;
			animateDragged();
			return true;
		}
		return false;
	}

	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			enabled = true;
			lastX = (int) event.getX();
			lastY = (int) event.getY();
			touching = true;
			break;
		case MotionEvent.ACTION_MOVE:
			int delta = lastY - (int) event.getY();
			if (dragged != -1) {
				// change draw location of dragged visual
				int x = (int) event.getX(), y = (int) event.getY();
				int l = x - (3 * childSize / 4), t = y - (3 * childSize / 4);
				getChildAt(dragged).layout(l, t, l + (childSize * 3 / 2),
						t + (childSize * 3 / 2));

				// check for new target hover
				int target = getTargetFromCoordinate(x, y);
				if (lastTarget != target) {
					if (target != -1) {
						animateGap(target);
						lastTarget = target;
					}
				}
			} 
			else {
				scroll += delta;
				clampScroll();
				if (Math.abs(delta) > 2)
					enabled = false;
				onLayout(true, getLeft(), getTop(), getRight(), getBottom());
			}
			lastX = (int) event.getX();
			lastY = (int) event.getY();
			lastDelta = delta;
			break;
		case MotionEvent.ACTION_UP:
			if (dragged != -1) {
				View v = getChildAt(dragged);
				if (lastTarget != -1)
					reorderChildren();
				else {
					Point xy = getCoordinateFromIndex(dragged);
					v.layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
				}
				v.clearAnimation();
				if (v instanceof ImageView)
					((ImageView) v).setAlpha(255);
				lastTarget = -1;
				dragged = -1;
			}
			touching = false;
			break;
		}
		if (dragged != -1)
			return true;
		return false;
	}

	// EVENT HELPERS
	protected void animateDragged() {
		View v = getChildAt(dragged);
		int x = getCoordinateFromIndex(dragged).x + childSize / 2, y = getCoordinateFromIndex(dragged).y
				+ childSize / 2;
		int l = x - (3 * childSize / 4), t = y - (3 * childSize / 4);
		v.layout(l, t, l + (childSize * 3 / 2), t + (childSize * 3 / 2));
		AnimationSet animSet = new AnimationSet(true);
		ScaleAnimation scale = new ScaleAnimation(.667f, 1, .667f, 1, childSize * 3 / 4,
				childSize * 3 / 4);
		scale.setDuration(animTime);
		AlphaAnimation alpha = new AlphaAnimation(1, .5f);
		alpha.setDuration(animTime);

		animSet.addAnimation(scale);
		animSet.addAnimation(alpha);
		animSet.setFillEnabled(true);
		animSet.setFillAfter(true);

		v.clearAnimation();
		v.startAnimation(animSet);
	}

	protected void animateGap(int target) {
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			if (i == dragged)
				continue;
			int newPos = i;
			if (dragged < target && i >= dragged + 1 && i <= target)
				newPos--;
			else if (target < dragged && i >= target && i < dragged)
				newPos++;

			// animate
			int oldPos = i;
			if (newPositions.get(i) != -1)
				oldPos = newPositions.get(i);
			if (oldPos == newPos)
				continue;

			Point oldXY = getCoordinateFromIndex(oldPos);
			Point newXY = getCoordinateFromIndex(newPos);
			Point oldOffset = new Point(oldXY.x - v.getLeft(), oldXY.y - v.getTop());
			Point newOffset = new Point(newXY.x - v.getLeft(), newXY.y - v.getTop());

			TranslateAnimation translate = new TranslateAnimation(Animation.ABSOLUTE,
					oldOffset.x, Animation.ABSOLUTE, newOffset.x, Animation.ABSOLUTE,
					oldOffset.y, Animation.ABSOLUTE, newOffset.y);
			translate.setDuration(animTime);
			translate.setFillEnabled(true);
			translate.setFillAfter(true);
			v.clearAnimation();
			v.startAnimation(translate);

			newPositions.set(i, newPos);
		}
	}

	protected void reorderChildren() {
		// FIGURE OUT HOW TO REORDER CHILDREN WITHOUT REMOVING THEM ALL AND
		// RECONSTRUCTING THE LIST!!!
		if (onRearrangeListener != null)
			onRearrangeListener.onRearrange(dragged, lastTarget);
		ArrayList<View> children = new ArrayList<View>();
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).clearAnimation();
			children.add(getChildAt(i));
		}
		removeAllViews();
		while (dragged != lastTarget)
			if (lastTarget == children.size()) // dragged and dropped to the
												// right of the last element
			{
				children.add(children.remove(dragged));
				dragged = lastTarget;
			} else if (dragged < lastTarget) // shift to the right
			{
				Collections.swap(children, dragged, dragged + 1);
				dragged++;
			} else if (dragged > lastTarget) // shift to the left
			{
				Collections.swap(children, dragged, dragged - 1);
				dragged--;
			}
		for (int i = 0; i < children.size(); i++) {
			newPositions.set(i, -1);
			addView(children.get(i));
		}
		onLayout(true, getLeft(), getTop(), getRight(), getBottom());
	}

	public void scrollToTop() {
		scroll = 0;
	}

	public void scrollToBottom() {
		scroll = Integer.MAX_VALUE;
		clampScroll();
	}

	protected void clampScroll() {
		int stretch = 3, overreach = getHeight() / 2;
		int max = getMaxScroll();
		max = Math.max(max, 0);

		if (scroll < -overreach) {
			scroll = -overreach;
			lastDelta = 0;
		} else if (scroll > max + overreach) {
			scroll = max + overreach;
			lastDelta = 0;
		} else if (scroll < 0) {
			if (scroll >= -stretch)
				scroll = 0;
			else if (!touching)
				scroll -= scroll / stretch;
		} else if (scroll > max) {
			if (scroll <= max + stretch)
				scroll = max;
			else if (!touching)
				scroll += (max - scroll) / stretch;
		}
	}

	protected int getMaxScroll() {
		int rowCount = (int) Math.ceil((double) getChildCount() / colCount), max = rowCount
				* childSize + (rowCount + 1) * padding - getHeight();
		return max;
	}

	public int getLastIndex() {
		return getIndexFromCoordinate(lastX, lastY);
	}

	// OTHER METHODS
	public void setOnRearrangeListener(OnRearrangeListener l) {
		this.onRearrangeListener = l;
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		this.onItemClickListener = l;
	}

	public interface OnRearrangeListener {
		public void onRearrange(int oldIndex, int newIndex);
	}
}