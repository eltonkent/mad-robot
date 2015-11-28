package com.madrobot.ui.widget.layout;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.getMode;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;

import com.madrobot.R;

/**
 * A special layout when measured in AT_MOST will take up a given percentage of
 * the available space.
 * <p>
 * The following are the WeightedLinearLayout attributes (attrs.xml)
 * <table cellspacing="1" cellpadding="3">
 * <tr>
 * <th><b>Attribute</b></td>
 * <td><b>Type</b></td>
 * <td><b>Default</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td><code>majorWeightMin</code></td>
 * <td>float</td>
 * <td>0.0</td>
 * <td>minimum major weight. in percentage</td>
 * </tr>
 * <tr>
 * <td><code>minorWeightMin</code></td>
 * <td>float</td>
 * <td>0.0</td>
 * <td>minimum minor weight. in percentage</td>
 * </tr>
 * <tr>
 * <td><code>majorWeightMax</code></td>
 * <td>float</td>
 * <td>0.0</td>
 * <td>maximum major weight. in percentage</td>
 * </tr>
 * <tr>
 * <td><code>minorWeightMax</code></td>
 * <td>float</td>
 * <td>0.0</td>
 * <td>maximum minor weight. in percentage</td>
 * </tr>
 * </table>
 * </p>
 * 
 */
public class WeightedLinearLayout extends LinearLayout {
	private float mMajorWeightMin;
	private float mMinorWeightMin;
	private float mMajorWeightMax;
	private float mMinorWeightMax;

	public WeightedLinearLayout(Context context) {
		super(context);
	}

	public WeightedLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeightedLinearLayout);

		mMajorWeightMin = a.getFloat(R.styleable.WeightedLinearLayout_majorWeightMin, 0.0f);
		mMinorWeightMin = a.getFloat(R.styleable.WeightedLinearLayout_minorWeightMin, 0.0f);
		mMajorWeightMax = a.getFloat(R.styleable.WeightedLinearLayout_majorWeightMax, 0.0f);
		mMinorWeightMax = a.getFloat(R.styleable.WeightedLinearLayout_minorWeightMax, 0.0f);

		a.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
		final int screenWidth = metrics.widthPixels;
		final boolean isPortrait = screenWidth < metrics.heightPixels;

		final int widthMode = getMode(widthMeasureSpec);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = getMeasuredWidth();
		boolean measure = false;

		widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, EXACTLY);

		final float widthWeightMin = isPortrait ? mMinorWeightMin : mMajorWeightMin;
		final float widthWeightMax = isPortrait ? mMinorWeightMax : mMajorWeightMax;
		if (widthMode == AT_MOST) {
			final int weightedMin = (int) (screenWidth * widthWeightMin);
			final int weightedMax = (int) (screenWidth * widthWeightMin);
			if (widthWeightMin > 0.0f && width < weightedMin) {
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(weightedMin, EXACTLY);
				measure = true;
			} else if (widthWeightMax > 0.0f && width > weightedMax) {
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(weightedMax, EXACTLY);
				measure = true;
			}
		}

		// TODO: Support height?

		if (measure) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
