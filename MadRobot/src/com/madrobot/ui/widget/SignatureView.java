package com.madrobot.ui.widget;

import com.madrobot.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * View that captures signatures
 * <p>
 * The default stroke color is black. Therefore one needs to change the stroke
 * color or background color to see the view rendering.
 * <br/>
 * <table cellspacing="1" cellpadding="3">
 * <tr>
 * <th><b>Attribute</b></td>
 * <td width="50"><b>Type</b></td>
 * <td><b>Default</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td><code>strokeColor</code></td>
 * <td>Color</td>
 * <td>Color.Black(0x000000)</td>
 * <td>Color of the signature stroke.</td>
 * </tr>
 * <tr>
 * <td><code>strokeWidth</code></td>
 * <td>float</td>
 * <td>5f</td>
 * <td>Width of signature stroke</td>
 * </tr>
 * </table><br/>
 * <b>Demo</b><br/>
 * <center><OBJECT CLASSID="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
 * WIDTH="428" HEIGHT="716" CODEBASE
 * ="http://active.macromedia.com/flash5/cabs/swflash.cab#version=7,0,0,0">
 * <PARAM NAME=movie VALUE="../../../../resources/demos/signatureview.swf"> <PARAM
 * NAME=play VALUE=true> <PARAM NAME=loop VALUE=false> <PARAM NAME=wmode
 * VALUE=transparent> <PARAM NAME=quality VALUE=low> <EMBED
 * SRC="../../../../resources/demos/signatureview.swf" WIDTH=428 HEIGHT=716
 * quality=low loop=false wmode=transparent TYPE="application/x-shockwave-flash"
 * PLUGINSPAGE=
 * "http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"
 * > </EMBED> </OBJECT></center> <SCRIPT
 * src='../../../../resources/demos/pagecurl.js'></script>
 * </p>
 * 
 * @author elton.kent
 * 
 */
public class SignatureView extends View {

	private final float STROKE_WIDTH = 5f;

	/** Need to track this so the dirty region can accommodate the stroke. **/
	private float HALF_STROKE_WIDTH;

	private Paint paint = new Paint();
	private Path path = new Path();

	/**
	 * Optimizes painting by invalidating the smallest possible area.
	 */
	private float lastTouchX;
	private float lastTouchY;
	private final RectF dirtyRect = new RectF();

	public SignatureView(Context context) {
		super(context);
		init(Color.BLACK, STROKE_WIDTH);
	}

	public SignatureView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SignatureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SignatureView);
		int color = a.getColor(R.styleable.SignatureView_strokeColor, Color.BLACK);
		float width = a.getFloat(R.styleable.SignatureView_strokeWidth, 5f);
		init(color, width);
	}

	private void init(int color, float width) {
		paint.setAntiAlias(true);
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(width);
		HALF_STROKE_WIDTH = width / 2;
	}

	/**
	 * Set the stroke color of the signature.
	 * <p>
	 * the default color is black.
	 * </p>
	 * 
	 * @param color
	 */
	public void setStrokeColor(int color) {
		paint.setColor(color);
	}

	public void setStrokeWidth(float width) {
		paint.setStrokeWidth(width);
	}

	/**
	 * Erases the signature.
	 */
	public void clear() {
		path.reset();

		// Repaints the entire view.
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawPath(path, paint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float eventX = event.getX();
		float eventY = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			path.moveTo(eventX, eventY);
			lastTouchX = eventX;
			lastTouchY = eventY;
			// There is no end point yet, so don't waste cycles invalidating.
			return true;

		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			// Start tracking the dirty region.
			resetDirtyRect(eventX, eventY);

			// When the hardware tracks events faster than they are delivered,
			// the
			// event will contain a history of those skipped points.
			int historySize = event.getHistorySize();
			for (int i = 0; i < historySize; i++) {
				float historicalX = event.getHistoricalX(i);
				float historicalY = event.getHistoricalY(i);
				expandDirtyRect(historicalX, historicalY);
				path.lineTo(historicalX, historicalY);
			}

			// After replaying history, connect the line to the touch point.
			path.lineTo(eventX, eventY);
			break;

		default:
			return false;
		}

		// Include half the stroke width to avoid clipping.
		invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
				(int) (dirtyRect.top - HALF_STROKE_WIDTH),
				(int) (dirtyRect.right + HALF_STROKE_WIDTH),
				(int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

		lastTouchX = eventX;
		lastTouchY = eventY;

		return true;
	}

	/**
	 * Get the captured signature as a bitmap
	 * 
	 * @param config
	 *            of the bitmap to be returned.
	 * @return Bitmap of the signature.
	 */
	public Bitmap getImage(Bitmap.Config config) {
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), config);

		Canvas canvas = new Canvas();
		canvas.setBitmap(bitmap);
		Drawable bg = getBackground();
		if (bg != null) {
			bg.draw(canvas);
		}
		canvas.drawPath(path, paint);
		return bitmap;
	}

	/**
	 * Called when replaying history to ensure the dirty region includes all
	 * points.
	 */
	private void expandDirtyRect(float historicalX, float historicalY) {
		if (historicalX < dirtyRect.left) {
			dirtyRect.left = historicalX;
		} else if (historicalX > dirtyRect.right) {
			dirtyRect.right = historicalX;
		}
		if (historicalY < dirtyRect.top) {
			dirtyRect.top = historicalY;
		} else if (historicalY > dirtyRect.bottom) {
			dirtyRect.bottom = historicalY;
		}
	}

	/**
	 * Resets the dirty region when the motion event occurs.
	 */
	private void resetDirtyRect(float eventX, float eventY) {

		// The lastTouchX and lastTouchY were set when the ACTION_DOWN
		// motion event occurred.
		dirtyRect.left = Math.min(lastTouchX, eventX);
		dirtyRect.right = Math.max(lastTouchX, eventX);
		dirtyRect.top = Math.min(lastTouchY, eventY);
		dirtyRect.bottom = Math.max(lastTouchY, eventY);
	}
}