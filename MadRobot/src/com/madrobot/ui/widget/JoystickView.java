package com.madrobot.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.madrobot.R;

/**
 * Joystick view. Behaves just an analog joystick.
 * <p>
 * Requires the vibrate permission to be set (android.permission.VIBRATE).<br/>
 * <img src="../../../../resources/JoystickView.png"><br/>
 * The following are the JoystickView attributes (attrs.xml)
 * <table cellspacing="1" cellpadding="3">
 * <tr>
 * <th><b>Attribute</b></td>
 * <td><b>Type</b></td>
 * <td><b>Default</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td><code>bgColor</code></td>
 * <td>Color</td>
 * <td>0xff888888 (Gray)</td>
 * <td>The background color of the joystick</td>
 * </tr>
 * <tr>
 * <td><code>bgAlpha</code></td>
 * <td>Integer</td>
 * <td>255</td>
 * <td>Transparency of the joystick background</td>
 * </tr>
 * <tr>
 * <td><code>handleColor</code></td>
 * <td>Color</td>
 * <td>0xff444444 (Dark Gray)</td>
 * <td>Joystick handle color</td>
 * </tr>
 * <tr>
 * <td><code>clickThreshold</code></td>
 * <td>float</td>
 * <td>0.4f (should be from 0 to 1)</td>
 * <td>Pressure for which an event should be considered as a click</td>
 * </tr>
 * <tr>
 * <td><code>moveResolution</code></td>
 * <td>float</td>
 * <td>1.0f</td>
 * <td>Number of pixels movement required between reporting to the listener</td>
 * </tr>
 * <tr>
 * <td><code>autoReturnToCenter</code></td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Return the joystick handle to the center after release.</td>
 * </tr>
 * <tr>
 * <td><code>coordinateSystem</code></td>
 * <td>cartesian<br/>
 * differential</td>
 * <td>cartesian</td>
 * <td>Coordinate System the user prefers</td>
 * </tr>
 * <tr>
 * <td><code>yAxisInverted</code></td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Use inverted Y axis</td>
 * </tr>
 * <tr>
 * <td><code>vibrate</code></td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Vibrate on click and released events</td>
 * </tr>
 * <tr>
 * <td><code>vibrateDuration</code></td>
 * <td>integer</td>
 * <td>300</td>
 * <td>duration to vibrate</td>
 * </tr>
 * </table>
 * 
 * </p>
 * 
 * @author elton.stephen.kent
 * 
 */
public class JoystickView extends View {
	public interface JoystickClickedListener {
		public void OnClicked();

		public void OnReleased();
	}

	public interface JoystickMovedListener {
		public void OnMoved(int pan, int tilt);

		public void OnReleased();

		public void OnReturnedToCenter();
	}

	// Max range of movement in user coordinate system
	public final static int CONSTRAIN_BOX = 0;

	public final static int CONSTRAIN_CIRCLE = 1;
	public final static int COORDINATE_CARTESIAN = 0; // Regular cartesian
														// coordinates

	public final static int COORDINATE_DIFFERENTIAL = 1; // Uses polar rotation
															// of 45 degrees to
															// calc differential
															// drive
	public static final int INVALID_POINTER_ID = -1;

	private double angle;
	private boolean autoReturnToCenter = true;

	private int bgAlpha = 255;
	private int bgColor = Color.GRAY;
	private Paint bgPaint;
	private int bgRadius;
	private boolean canVibrate;

	// Cartesian coordinates of last touch point - joystick center is (0,0)
	private int cartX, cartY;
	private boolean clicked;

	private JoystickClickedListener clickListener;

	private float clickThreshold = 0.4f;
	// Center of the view in view coordinates
	private int cX, cY;

	// =========================================
	// Private Members
	// =========================================
	private final boolean D = false;
	private Paint dbgPaint1;
	private Paint dbgPaint2;
	// Size of the view in view coordinates
	private int dimX, dimY;

	private int handleColor = Color.DKGRAY;
	private int handleInnerBoundaries;
	private Paint handlePaint;

	private int handleRadius;
	// Handle center in view coordinates
	private float handleX, handleY;
	private int innerPadding;

	private JoystickMovedListener moveListener;
	private int movementConstraint;

	private int movementRadius;

	private float movementRange = 10;

	// # of pixels movement required between reporting to the listener
	private float moveResolution = 1.0f;

	// Offset co-ordinates (used when touch events are received from parent's
	// coordinate origin)
	private int offsetX;

	private int offsetY;

	// Last touch point in view coordinates
	private int pointerId = INVALID_POINTER_ID;
	// Polar coordinates of the touch point from joystick center
	private double radial;

	// Last reported position in view coordinates (allows different reporting
	// sensitivities)
	private float reportX, reportY;

	String TAG = "JoystickView";
	// Records touch pressure for click handling
	private float touchPressure;

	// =========================================
	// Constructors
	// =========================================

	private float touchX, touchY;

	// paramaters
	private int userCoordinateSystem = COORDINATE_CARTESIAN;

	// User coordinates of last touch point
	private int userX, userY;

	private long vibrateDuration;
	private Vibrator vibrator;
	private boolean yAxisInverted = true;

	public JoystickView(Context context) {
		super(context);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttributes(context, attrs);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAttributes(context, attrs);
		initJoystickView();
	}

	private void calcUserCoordinates() {
		// First convert to cartesian coordinates
		cartX = (int) (touchX / movementRadius * movementRange);
		cartY = (int) (touchY / movementRadius * movementRange);

		radial = Math.sqrt((cartX * cartX) + (cartY * cartY));
		angle = Math.atan2(cartY, cartX);

		// Invert Y axis if requested
		if (!yAxisInverted)
			cartY *= -1;

		if (userCoordinateSystem == COORDINATE_CARTESIAN) {
			userX = cartX;
			userY = cartY;
		} else if (userCoordinateSystem == COORDINATE_DIFFERENTIAL) {
			userX = cartY + cartX / 4;
			userY = cartY - cartX / 4;

			if (userX < -movementRange)
				userX = (int) -movementRange;
			if (userX > movementRange)
				userX = (int) movementRange;

			if (userY < -movementRange)
				userY = (int) -movementRange;
			if (userY > movementRange)
				userY = (int) movementRange;
		}

	}

	// =========================================
	// Initialization
	// =========================================

	// Constrain touch within a box
	private void constrainBox() {
		touchX = Math.max(Math.min(touchX, movementRadius), -movementRadius);
		touchY = Math.max(Math.min(touchY, movementRadius), -movementRadius);
	}

	// Constrain touch within a circle
	private void constrainCircle() {
		float diffX = touchX;
		float diffY = touchY;
		double radial = Math.sqrt((diffX * diffX) + (diffY * diffY));
		if (radial > movementRadius) {
			touchX = (int) ((diffX / radial) * movementRadius);
			touchY = (int) ((diffY / radial) * movementRadius);
		}
	}

	public float getClickThreshold() {
		return clickThreshold;
	}

	public int getMovementConstraint() {
		return movementConstraint;
	}

	public float getMovementRange() {
		return movementRange;
	}

	public float getMoveResolution() {
		return moveResolution;
	}

	public int getPointerId() {
		return pointerId;
	}

	public int getUserCoordinateSystem() {
		return userCoordinateSystem;
	}

	private void initAttributes(Context context, AttributeSet attrs) {
		TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
				R.styleable.JoystickView);
		bgColor = styledAttrs.getColor(R.styleable.JoystickView_bgColor, bgColor);
		int alpha = styledAttrs.getInt(R.styleable.JoystickView_bgAlpha, bgAlpha);
		if (alpha < 0 || alpha > 255) {
			alpha = 255;
		}
		bgAlpha = alpha;
		handleColor = styledAttrs.getColor(R.styleable.JoystickView_handleColor, handleColor);
		float clickThresh = styledAttrs.getFloat(R.styleable.JoystickView_clickThreshold,
				clickThreshold);
		if (clickThresh < 0 || clickThresh > 1.0f) {
			clickThresh = clickThreshold;
		}
		clickThreshold = clickThresh;
		autoReturnToCenter = styledAttrs.getBoolean(
				R.styleable.JoystickView_autoReturnToCenter, autoReturnToCenter);
		userCoordinateSystem = styledAttrs.getInt(R.styleable.JoystickView_coordinateSystem,
				COORDINATE_CARTESIAN);
		yAxisInverted = styledAttrs.getBoolean(R.styleable.JoystickView_yAxisInverted, true);
		moveResolution = styledAttrs.getFloat(R.styleable.JoystickView_moveResolution,
				moveResolution);
		canVibrate = styledAttrs.getBoolean(R.styleable.JoystickView_vibrate, false);
		vibrateDuration = styledAttrs.getInt(R.styleable.JoystickView_vibrateDuration, 300);

	}

	private void initJoystickView() {
		vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
		setFocusable(true);

		dbgPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
		dbgPaint1.setColor(Color.RED);
		dbgPaint1.setStrokeWidth(1);
		dbgPaint1.setStyle(Paint.Style.STROKE);

		dbgPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		dbgPaint2.setColor(Color.GREEN);
		dbgPaint2.setStrokeWidth(1);
		dbgPaint2.setStyle(Paint.Style.STROKE);

		bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgPaint.setColor(bgColor);
		bgPaint.setStrokeWidth(1);
		bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		bgPaint.setAlpha(bgAlpha);

		handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		handlePaint.setColor(handleColor);
		handlePaint.setStrokeWidth(1);
		handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		innerPadding = 10;

		// setMovementRange(10);
		// setMoveResolution(1.0f);
		// setClickThreshold(0.4f);
		// setYAxisInverted(true);
		// setUserCoordinateSystem(COORDINATE_CARTESIAN);
		// setAutoReturnToCenter(true);
	}

	public boolean isAutoReturnToCenter() {
		return autoReturnToCenter;
	}

	public boolean isYAxisInverted() {
		return yAxisInverted;
	}

	private int measure(int measureSpec) {
		int result = 0;
		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 200 if no bounds are specified.
			result = 200;
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		// Draw the background
		canvas.drawCircle(cX, cY, bgRadius, bgPaint);

		// Draw the handle
		handleX = touchX + cX;
		handleY = touchY + cY;
		canvas.drawCircle(handleX, handleY, handleRadius, handlePaint);

		if (D) {
			canvas.drawRect(1, 1, getMeasuredWidth() - 1, getMeasuredHeight() - 1, dbgPaint1);

			canvas.drawCircle(handleX, handleY, 3, dbgPaint1);

			if (movementConstraint == CONSTRAIN_CIRCLE) {
				canvas.drawCircle(cX, cY, this.movementRadius, dbgPaint1);
			} else {
				canvas.drawRect(cX - movementRadius, cY - movementRadius, cX + movementRadius,
						cY + movementRadius, dbgPaint1);
			}

			// Origin to touch point
			canvas.drawLine(cX, cY, handleX, handleY, dbgPaint2);

			int baseY = (touchY < 0 ? cY + handleRadius : cY - handleRadius);
			canvas.drawText(String.format("%s (%.0f,%.0f)", TAG, touchX, touchY),
					handleX - 20, baseY - 7, dbgPaint2);
			canvas.drawText("(" + String.format("%.0f, %.1f", radial, angle * 57.2957795)
					+ (char) 0x00B0 + ")", handleX - 20, baseY + 15, dbgPaint2);
		}

		// Log.d(TAG, String.format("touch(%f,%f)", touchX, touchY));
		// Log.d(TAG, String.format("onDraw(%.1f,%.1f)\n\n", handleX, handleY));
		canvas.restore();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		int d = Math.min(getMeasuredWidth(), getMeasuredHeight());

		dimX = d;
		dimY = d;

		cX = d / 2;
		cY = d / 2;

		bgRadius = dimX / 2 - innerPadding;
		handleRadius = (int) (d * 0.25);
		handleInnerBoundaries = handleRadius;
		movementRadius = Math.min(cX, cY) - handleInnerBoundaries;
	}

	// =========================================
	// Public Methods
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we make sure that we have a perfect circle
		int measuredWidth = measure(widthMeasureSpec);
		int measuredHeight = measure(heightMeasureSpec);
		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE: {
			return processMoveEvent(ev);
		}
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			if (pointerId != INVALID_POINTER_ID) {
				// Log.d(TAG, "ACTION_UP");
				returnHandleToCenter();
				setPointerId(INVALID_POINTER_ID);
			}
			break;
		}
		case MotionEvent.ACTION_POINTER_UP: {
			if (pointerId != INVALID_POINTER_ID) {
				final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = ev.getPointerId(pointerIndex);
				if (pointerId == this.pointerId) {
					// Log.d(TAG, "ACTION_POINTER_UP: " + pointerId);
					returnHandleToCenter();
					setPointerId(INVALID_POINTER_ID);
					return true;
				}
			}
			break;
		}
		case MotionEvent.ACTION_DOWN: {
			if (pointerId == INVALID_POINTER_ID) {
				int x = (int) ev.getX();
				if (x >= offsetX && x < offsetX + dimX) {
					setPointerId(ev.getPointerId(0));
					// Log.d(TAG, "ACTION_DOWN: " + getPointerId());
					return true;
				}
			}
			break;
		}
		case MotionEvent.ACTION_POINTER_DOWN: {
			if (pointerId == INVALID_POINTER_ID) {
				final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = ev.getPointerId(pointerIndex);
				int x = (int) ev.getX(pointerId);
				if (x >= offsetX && x < offsetX + dimX) {
					// Log.d(TAG, "ACTION_POINTER_DOWN: " + pointerId);
					setPointerId(pointerId);
					return true;
				}
			}
			break;
		}
		}
		return false;
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	private boolean processMoveEvent(MotionEvent ev) {
		if (pointerId != INVALID_POINTER_ID) {
			final int pointerIndex = ev.findPointerIndex(pointerId);

			// Translate touch position to center of view
			float x = ev.getX(pointerIndex);
			touchX = x - cX - offsetX;
			float y = ev.getY(pointerIndex);
			touchY = y - cY - offsetY;

			// Log.d(TAG,
			// String.format("ACTION_MOVE: (%03.0f, %03.0f) => (%03.0f, %03.0f)",
			// x, y, touchX, touchY));

			reportOnMoved();
			invalidate();

			touchPressure = ev.getPressure(pointerIndex);
			reportOnPressure();

			return true;
		}
		return false;
	}

	private void reportOnMoved() {
		if (movementConstraint == CONSTRAIN_CIRCLE)
			constrainCircle();
		else
			constrainBox();

		calcUserCoordinates();

		if (moveListener != null) {
			boolean rx = Math.abs(touchX - reportX) >= moveResolution;
			boolean ry = Math.abs(touchY - reportY) >= moveResolution;
			if (rx || ry) {
				this.reportX = touchX;
				this.reportY = touchY;

				// Log.d(TAG, String.format("moveListener.OnMoved(%d,%d)",
				// (int)userX, (int)userY));
				moveListener.OnMoved(userX, userY);
			}
		}
	}

	// Simple pressure click
	private void reportOnPressure() {
		// Log.d(TAG, String.format("touchPressure=%.2f", this.touchPressure));
		if (clickListener != null) {
			if (clicked && touchPressure < clickThreshold) {
				if (canVibrate)
					vibrator.vibrate(vibrateDuration);
				clickListener.OnReleased();
				this.clicked = false;
				// Log.d(TAG, "reset click");
				invalidate();
			} else if (!clicked && touchPressure >= clickThreshold) {
				clicked = true;
				if (canVibrate)
					vibrator.vibrate(vibrateDuration);
				clickListener.OnClicked();
				// Log.d(TAG, "click");
				invalidate();
				performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			}
		}
	}

	private void returnHandleToCenter() {
		if (autoReturnToCenter) {
			final int numberOfFrames = 5;
			final double intervalsX = (0 - touchX) / numberOfFrames;
			final double intervalsY = (0 - touchY) / numberOfFrames;

			for (int i = 0; i < numberOfFrames; i++) {
				final int j = i;
				postDelayed(new Runnable() {
					@Override
					public void run() {
						touchX += intervalsX;
						touchY += intervalsY;

						reportOnMoved();
						invalidate();

						if (moveListener != null && j == numberOfFrames - 1) {
							moveListener.OnReturnedToCenter();
						}
					}
				}, i * 40);
			}

			if (moveListener != null) {
				moveListener.OnReleased();
			}
		}
	}

	public void setAutoReturnToCenter(boolean autoReturnToCenter) {
		this.autoReturnToCenter = autoReturnToCenter;
	}

	/**
	 * Set the pressure sensitivity for registering a click
	 * 
	 * @param clickThreshold
	 *            threshold 0...1.0f inclusive. 0 will cause clicks to never be
	 *            reported, 1.0 is a very hard click
	 */
	public void setClickThreshold(float clickThreshold) {
		if (clickThreshold < 0 || clickThreshold > 1.0f)
			Log.e(TAG, "clickThreshold must range from 0...1.0f inclusive");
		else
			this.clickThreshold = clickThreshold;
	}

	public void setMovementConstraint(int movementConstraint) {
		if (movementConstraint < CONSTRAIN_BOX || movementConstraint > CONSTRAIN_CIRCLE)
			Log.e(TAG, "invalid value for movementConstraint");
		else
			this.movementConstraint = movementConstraint;
	}

	public void setMovementRange(float movementRange) {
		this.movementRange = movementRange;
	}

	public void setMoveResolution(float moveResolution) {
		this.moveResolution = moveResolution;
	}

	public void setOnJostickClickedListener(JoystickClickedListener listener) {
		this.clickListener = listener;
	}

	public void setOnJostickMovedListener(JoystickMovedListener listener) {
		this.moveListener = listener;
	}

	public void setPointerId(int id) {
		this.pointerId = id;
	}

	public void setTouchOffset(int x, int y) {
		offsetX = x;
		offsetY = y;
	}

	public void setUserCoordinateSystem(int userCoordinateSystem) {
		if (userCoordinateSystem < COORDINATE_CARTESIAN
				|| movementConstraint > COORDINATE_DIFFERENTIAL)
			Log.e(TAG, "invalid value for userCoordinateSystem");
		else
			this.userCoordinateSystem = userCoordinateSystem;
	}

	public void setYAxisInverted(boolean yAxisInverted) {
		this.yAxisInverted = yAxisInverted;
	}
}
