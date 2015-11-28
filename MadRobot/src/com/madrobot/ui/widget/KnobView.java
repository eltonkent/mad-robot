package com.madrobot.ui.widget;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.madrobot.R;
import com.madrobot.geom.Angle;
import com.madrobot.geom.AngleUtils;

/**
 * Used for creating a rotary knob
 * 
 * @author ekent4
 * 
 */
public class KnobView extends View {
	private float angle = 0f;
	private float theta_old = 0f;
	private Drawable bitmapDraw;
	private RotaryKnobListener listener;

	public interface RotaryKnobListener {
		public void onKnobChanged(int arg);
	}

	public void setKnobListener(RotaryKnobListener l) {
		listener = l;
	}

	public KnobView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public KnobView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public KnobView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public void initialize() {
		bitmapDraw = new BitmapDrawable(BitmapFactory.decodeResource(getResources(),
				R.drawable.jog));
		bitmapDraw.setBounds(0, 0, getWidth(), getHeight());
		// this.setImageResource(R.drawable.jog);
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				int actionCode = action & MotionEvent.ACTION_MASK;
				if (actionCode == MotionEvent.ACTION_POINTER_DOWN) {
					float x = event.getX(0);
					float y = event.getY(0);
					theta_old = AngleUtils.getAngleOfPoint(x, y,getWidth(),getHeight());
				} else if (actionCode == MotionEvent.ACTION_MOVE) {
					invalidate();

					float x = event.getX(0);
					float y = event.getY(0);

					float theta = AngleUtils.getAngleOfPoint(x, y,getWidth(),getHeight());
					float delta_theta = theta - theta_old;

					theta_old = theta;

					int direction = (delta_theta > 0) ? 1 : -1;
					angle += 5 * direction;

					notifyListener(direction);

				}
				return true;
			}

		});
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text
			result = specSize;
		}

		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text (beware: ascent is a negative number)
			result = specSize;
		}
		return result;
	}

	/**
	 * @see android.view.View#measure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int finalWidth, finalHeight;
		finalWidth = measureWidth(widthMeasureSpec);
		finalHeight = measureHeight(heightMeasureSpec);
		bitmapDraw.setBounds(0, 0, finalWidth, finalHeight);
		setMeasuredDimension(finalWidth, finalHeight);
	}

	private void notifyListener(int arg) {
		if (null != listener)
			listener.onKnobChanged(arg);
	}

	protected void onDraw(Canvas c) {
		c.rotate(angle, getWidth() / 2, getHeight() / 2);
		// bitmapDraw.setBounds(0, 0, getWidth(), getHeight());
		bitmapDraw.draw(c);
		super.onDraw(c);
	}
}
