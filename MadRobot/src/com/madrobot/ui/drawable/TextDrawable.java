package com.madrobot.ui.drawable;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class TextDrawable extends Drawable

{

	public TextDrawable(String text, float textSize) {
		mText = "";
		mEditing = false;
		mNow = 0L;
		mShowCursor = false;
		minTextSize = 16F;
		mTextHint = false;
		metrics = new android.graphics.Paint.FontMetrics();
		mPaint.setDither(true);
		mPaint.setColor(-1);
		mPaint.setStyle(android.graphics.Paint.Style.FILL);
		textSize = textSize >= minTextSize ? textSize : minTextSize;
		mPaint.setTextSize(textSize);
		mStrokePaint = new Paint(mPaint);
		mStrokePaint.setStyle(android.graphics.Paint.Style.STROKE);
		mStrokePaint.setStrokeWidth(textSize / 10F);
		mWidth = 0;
		mHeight = 0;
		setText(text);
		computeMinSize();
	}

	public void setTextHint(CharSequence text) {
		setTextHint((String) text);
	}

	public void setTextHint(String text) {
		mText = text;
		mTextHint = true;
		invalidate();
	}

	public boolean isTextHint() {
		return mTextHint;
	}

	void computeMinSize() {
		mMinWidth = getMinWidth();
		mMinHeight = minTextSize;
	}

	protected float getMinWidth() {
		float widths[] = new float[1];
		mPaint.getTextWidths(" ", widths);
		return widths[0] / 2.0F;
	}

	protected void computeMinWidth() {
		mMinTextWidth = (int) getMinWidth();
	}

	protected float getTotal(float array[]) {
		float total = 0.0F;
		float af[];
		int j = (af = array).length;
		for (int i = 0; i < j; i++) {
			float v = af[i];
			total += v;
		}

		return total;
	}

	protected void computeSize() {
		computeMinWidth();
		computeTextWidth();
		computeTextHeight();
	}

	protected void computeTextHeight() {
		mHeight = (int) Math.max(getTextSize(), getNumLines() * getTextSize());
	}

	protected void computeTextWidth() {
		int maxWidth = 0;
		if (mText.length() > 0)
			if (getNumLines() == 1) {
				maxWidth = (int) getTextWidth(0, mText.length());
			} else {
				int start = 0;
				for (int i = 0; i < linesBreak.size(); i++) {
					int nextBreak = ((Integer) linesBreak.get(i)).intValue();
					maxWidth = (int) Math.max(maxWidth, getTextWidth(start, nextBreak));
					start = nextBreak + 1;
				}

			}
		mWidth = maxWidth + mMinTextWidth;
	}

	protected float getTextWidth(int start, int stop) {
		float w[] = new float[stop - start];
		mPaint.getTextWidths(mText, start, stop, w);
		return getTotal(w);
	}

	protected void copyBounds(RectF rect) {
		rect.set(mBoundsF);
	}

	@Override
	public void draw(Canvas canvas) {
		RectF dstRect = new RectF();
		copyBounds(dstRect);
		int numLines = getNumLines();
		float textSize = getTextSize();
		getFontMetrics(metrics);
		if (numLines == 1) {
			if (!mTextHint)
				canvas.drawText(mText, dstRect.left, dstRect.top - metrics.top
						- metrics.bottom, mStrokePaint);
			canvas.drawText(mText, dstRect.left, dstRect.top - metrics.top - metrics.bottom,
					mPaint);
		} else {
			int start = 0;
			float top = dstRect.top;
			float left = dstRect.left;
			for (int i = 0; i < linesBreak.size(); i++) {
				int nextBreak = ((Integer) linesBreak.get(i)).intValue();
				String text = mText.substring(start, nextBreak);
				if (!mTextHint)
					canvas.drawText(text, left, top, mStrokePaint);
				canvas.drawText(text, left, top, mPaint);
				start = nextBreak + 1;
				top += textSize;
			}

		}
		if (mEditing) {
			long now = System.currentTimeMillis();
			if (now - mNow > 300L) {
				mShowCursor = !mShowCursor;
				mNow = now;
			}
			if (mShowCursor) {
				Rect lastRect = new Rect();
				getLineBounds(getNumLines() - 1, lastRect);
				float left = dstRect.left + lastRect.width() + 2.0F;
				float top = dstRect.top;
				float right = dstRect.left + lastRect.width() + 4F;
				float bottom = dstRect.top - metrics.top * (numLines - 1) - metrics.top
						- metrics.bottom;
				canvas.drawRect(left, top, right, bottom, mStrokePaint);
				canvas.drawRect(left, top, right, bottom, mPaint);
			}
		}
	}

	public void beginEdit() {
		mEditing = true;
	}

	public void endEdit() {
		mEditing = false;
	}

	@Override
	public int getIntrinsicHeight() {
		return mHeight;
	}

	@Override
	public int getIntrinsicWidth() {
		return mWidth;
	}

	public void getLineBounds(int line, Rect outBounds) {
		if (mText.length() > 0) {
			if (getNumLines() == 1) {
				mPaint.getTextBounds(mText, 0, mText.length(), outBounds);
				outBounds.left = 0;
				outBounds.right = (int) getTextWidth(0, mText.length());
			} else {
				mPaint.getTextBounds(mText,
						((Integer) linesBreak.get(line - 1)).intValue() + 1,
						((Integer) linesBreak.get(line)).intValue(), outBounds);
				outBounds.left = 0;
				outBounds.right = (int) getTextWidth(
						((Integer) linesBreak.get(line - 1)).intValue() + 1,
						((Integer) linesBreak.get(line)).intValue());
			}
		} else {
			mPaint.getTextBounds(mText, 0, mText.length(), outBounds);
			outBounds.left = 0;
			outBounds.right = 0;
		}
		if (outBounds.width() < mMinTextWidth)
			outBounds.right = mMinTextWidth;
		outBounds.offset(0, (int) (getTextSize() * getNumLines()));
	}

	protected int getNumLines() {
		return Math.max(linesBreak.size(), 1);
	}

	@Override
	public int getOpacity() {
		return mPaint.getAlpha();
	}

	public CharSequence getText() {
		return mText;
	}

	public int getTextColor() {
		return mPaint.getColor();
	}

	public int getTextStrokeColor() {
		return mStrokePaint.getColor();
	}

	public void setTextStrokeColor(int color) {
		mStrokePaint.setColor(color);
	}

	public float getTextSize() {
		return mPaint.getTextSize();
	}

	protected void invalidate() {
		linesBreak.clear();
		int start = 0;
		for (int last = -1; (last = mText.indexOf('\n', start)) > -1;) {
			start = last + 1;
			linesBreak.add(Integer.valueOf(last));
		}

		linesBreak.add(Integer.valueOf(mText.length()));
		computeSize();
	}

	public boolean isEditing() {
		return mEditing;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	public void setBounds(float left, float top, float right, float bottom) {
		if (left != mBoundsF.left || top != mBoundsF.top || right != mBoundsF.right
				|| bottom != mBoundsF.bottom) {
			mBoundsF.set(left, top, right, bottom);
			setTextSize(bottom - top);
		}
	}

	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		super.setBounds(left, top, right, bottom);
		setBounds(left, top, right, bottom);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
		mStrokePaint.setColorFilter(cf);
	}

	public void setStrokeColor(int color) {
		mStrokePaint.setColor(color);
	}

	public void setText(CharSequence text) {
		setText((String) text);
	}

	public void setText(String text) {
		mText = text;
		mTextHint = false;
		invalidate();
	}

	public void setTextColor(int color) {
		mPaint.setColor(color);
	}

	public void setTextSize(float size) {
		if (size / getNumLines() != mPaint.getTextSize()) {
			int lines = getNumLines();
			mPaint.setTextSize(size / lines);
			mStrokePaint.setTextSize(size / lines);
			mStrokePaint.setStrokeWidth(size / lines / 10F);
		}
	}

	public boolean validateSize(RectF rect) {
		float h = rect.height();
		return h / getNumLines() >= mMinHeight && mText.length() >= 1;
	}

	public void setMinSize(float f, float f1) {
	}

	public void setMinTextSize(float value) {
		minTextSize = value;
	}

	public float getFontMetrics(android.graphics.Paint.FontMetrics metrics) {
		return mPaint.getFontMetrics(metrics);
	}

	protected final Paint mPaint = new Paint(451);
	protected final Paint mStrokePaint;
	protected String mText;
	protected final RectF mBoundsF = new RectF(0.0F, 0.0F, 0.0F, 0.0F);
	protected boolean mEditing;
	protected long mNow;
	protected boolean mShowCursor;
	protected int mWidth;
	protected int mHeight;
	protected int mMinTextWidth;
	protected float mMinWidth;
	protected float mMinHeight;
	protected final List linesBreak = new ArrayList();
	protected float minTextSize;
	protected boolean mTextHint;
	android.graphics.Paint.FontMetrics metrics;
}
