package com.madrobot.ui.drawable;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class CircleDrawable extends Drawable {

	public CircleDrawable(int radius, boolean soft) {
		mRadius = radius;
		mOvalRect = new RectF(0.0F, 0.0F, radius, radius);
		mPaint = new Paint(1);
		mPaint.setColor(0xff000000);
		if (soft)
			mPaint.setMaskFilter(new BlurMaskFilter(2.0F,
					android.graphics.BlurMaskFilter.Blur.NORMAL));
	}

	@Override
	public void draw(Canvas canvas) {
		RectF rect = new RectF(getBounds());
		canvas.drawColor(mBackgroundColor);
		canvas.drawCircle(rect.centerX(), rect.centerY(), mRadius / 2, mPaint);
	}

	public void setBackgroundColor(int color) {
		mBackgroundColor = color;
	}

	@Override
	public int getOpacity() {
		return -1;
	}

	@Override
	public void setAlpha(int i) {
	}

	@Override
	public void setColorFilter(ColorFilter colorfilter) {
	}

	Paint mPaint;
	int mRadius;
	int mBackgroundColor;
	RectF mOvalRect;
}
