package com.madrobot.ui.widget.imageview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.widget.ImageView;

/**
 * ImageView that can be zoomed with Pinch and double tap gestures
 * 
 * @author elton.stephen.kent
 * 
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class ZoomableImageView extends ImageView {
	private enum Command {
		Center, Layout, Move, Reset, Zoom,
	};

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			float scale = getScale();
			float targetScale = scale;
			targetScale = onDoubleTapPost(scale, getMaxZoom());
			targetScale = Math.min(getMaxZoom(), Math.max(targetScale, MIN_ZOOM));
			mCurrentScaleFactor = targetScale;
			zoomTo(targetScale, e.getX(), e.getY(), 200);
			invalidate();
			return super.onDoubleTap(e);
		}

		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1)
				return false;
			if (mScaleDetector.isInProgress())
				return false;

			float diffX = e2.getX() - e1.getX();
			float diffY = e2.getY() - e1.getY();

			if (Math.abs(velocityX) > 800 || Math.abs(velocityY) > 800) {
				scrollBy(diffX / 2, diffY / 2, 300);
				invalidate();
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			if (e1 == null || e2 == null)
				return false;
			if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1)
				return false;
			if (mScaleDetector.isInProgress())
				return false;
			if (getScale() == 1f)
				return false;
			scrollBy(-distanceX, -distanceY);
			invalidate();
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	};

	public interface OnBitmapChangedListener {

		void onBitmapChanged(Bitmap bitmap);
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		@TargetApi(Build.VERSION_CODES.FROYO)
		@SuppressWarnings("unused")
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float span = detector.getCurrentSpan() - detector.getPreviousSpan();
			float targetScale = mCurrentScaleFactor * detector.getScaleFactor();
			if (true) {
				targetScale = Math.min(getMaxZoom(), Math.max(targetScale, MIN_ZOOM));
				zoomTo(targetScale, detector.getFocusX(), detector.getFocusY());
				mCurrentScaleFactor = Math.min(getMaxZoom(), Math.max(targetScale, MIN_ZOOM));
				mDoubleTapDirection = 1;
				invalidate();
				return true;
			}
			return false;
		}
	}

	static final float MIN_ZOOM = 0.9f;
	final private float MAX_ZOOM = 2.0f;
	private Matrix mBaseMatrix = new Matrix();
	final private ZoomableImageViewRotateBitmap mBitmapDisplayed = new ZoomableImageViewRotateBitmap(
			null, 0);
	private float mCurrentScaleFactor;
	private final Matrix mDisplayMatrix = new Matrix();

	private int mDoubleTapDirection;
	private GestureDetector mGestureDetector;

	private GestureListener mGestureListener;
	private Handler mHandler = new Handler();
	private OnBitmapChangedListener mListener;
	private final float[] mMatrixValues = new float[9];
	private float mMaxZoom;
	private Runnable mOnLayoutRunnable = null;
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor;
	private ScaleListener mScaleListener;
	private Matrix mSuppMatrix = new Matrix();

	private int mThisWidth = -1, mThisHeight = -1;

	private int mTouchSlop;

	public ZoomableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void center(boolean horizontal, boolean vertical) {
		if (mBitmapDisplayed.getBitmap() == null)
			return;
		RectF rect = getCenter(horizontal, vertical);
		if (rect.left != 0 || rect.top != 0) {
			postTranslate(rect.left, rect.top);
		}
	}

	public void clear() {
		setImageBitmapReset(null, true);
	}

	public void dispose() {
		if (mBitmapDisplayed.getBitmap() != null) {
			if (!mBitmapDisplayed.getBitmap().isRecycled()) {
				mBitmapDisplayed.getBitmap().recycle();
			}
		}
		clear();
	}

	private float easeOut(float time, float start, float end, float duration) {
		return end * ((time = time / duration - 1) * time * time + 1) + start;
	}

	private RectF getBitmapRect() {
		if (mBitmapDisplayed.getBitmap() == null)
			return null;
		Matrix m = getImageViewMatrix();
		RectF rect = new RectF(0, 0, mBitmapDisplayed.getBitmap().getWidth(), mBitmapDisplayed
				.getBitmap().getHeight());
		m.mapRect(rect);
		return rect;
	}

	private RectF getCenter(boolean horizontal, boolean vertical) {
		if (mBitmapDisplayed.getBitmap() == null)
			return new RectF(0, 0, 0, 0);
		RectF rect = getBitmapRect();
		float height = rect.height();
		float width = rect.width();
		float deltaX = 0, deltaY = 0;
		if (vertical) {
			int viewHeight = getHeight();
			if (height < viewHeight) {
				deltaY = (viewHeight - height) / 2 - rect.top;
			} else if (rect.top > 0) {
				deltaY = -rect.top;
			} else if (rect.bottom < viewHeight) {
				deltaY = getHeight() - rect.bottom;
			}
		}
		if (horizontal) {
			int viewWidth = getWidth();
			if (width < viewWidth) {
				deltaX = (viewWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) {
				deltaX = -rect.left;
			} else if (rect.right < viewWidth) {
				deltaX = viewWidth - rect.right;
			}
		}
		return new RectF(deltaX, deltaY, 0, 0);
	}

	public ZoomableImageViewRotateBitmap getDisplayBitmap() {
		return mBitmapDisplayed;
	}

	public Matrix getImageViewMatrix() {
		mDisplayMatrix.set(mBaseMatrix);
		mDisplayMatrix.postConcat(mSuppMatrix);
		return mDisplayMatrix;
	}

	public float getMaxZoom() {
		return mMaxZoom;
	}

	/**
	 * Setup the base matrix so that the image is centered and scaled properly.
	 * 
	 * @param bitmap
	 * @param matrix
	 */
	private void getProperBaseMatrix(ZoomableImageViewRotateBitmap bitmap, Matrix matrix) {
		float viewWidth = getWidth();
		float viewHeight = getHeight();
		float w = bitmap.getWidth();
		float h = bitmap.getHeight();
		matrix.reset();
		float widthScale = Math.min(viewWidth / w, MAX_ZOOM);
		float heightScale = Math.min(viewHeight / h, MAX_ZOOM);
		float scale = Math.min(widthScale, heightScale);
		matrix.postConcat(bitmap.getRotateMatrix());
		matrix.postScale(scale, scale);
		matrix.postTranslate((viewWidth - w * scale) / MAX_ZOOM, (viewHeight - h * scale)
				/ MAX_ZOOM);
	}

	public float getScale() {
		return getScale(mSuppMatrix);
	}

	private float getScale(Matrix matrix) {
		return getValue(matrix, Matrix.MSCALE_X);
	}

	private float getValue(Matrix matrix, int whichValue) {
		matrix.getValues(mMatrixValues);
		return mMatrixValues[whichValue];
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private void init() {
		setScaleType(ImageView.ScaleType.MATRIX);
		mTouchSlop = ViewConfiguration.getTouchSlop();
		mGestureListener = new GestureListener();
		mScaleListener = new ScaleListener();

		mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
		mGestureDetector = new GestureDetector(getContext(), mGestureListener, null, true);
		mCurrentScaleFactor = 1f;
		mDoubleTapDirection = 1;
	}

	private float maxZoom() {
		if (mBitmapDisplayed.getBitmap() == null) {
			return 1F;
		}
		float fw = (float) mBitmapDisplayed.getWidth() / (float) mThisWidth;
		float fh = (float) mBitmapDisplayed.getHeight() / (float) mThisHeight;
		float max = Math.max(fw, fh) * 4;
		return max;
	}

	private float onDoubleTapPost(float scale, float maxZoom) {
		if (mDoubleTapDirection == 1) {
			if ((scale + (mScaleFactor * 2)) <= maxZoom) {
				return scale + mScaleFactor;
			} else {
				mDoubleTapDirection = -1;
				return maxZoom;
			}
		} else {
			mDoubleTapDirection = 1;
			return 1f;
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mThisWidth = right - left;
		mThisHeight = bottom - top;
		Runnable r = mOnLayoutRunnable;
		if (r != null) {
			mOnLayoutRunnable = null;
			r.run();
		}
		if (mBitmapDisplayed.getBitmap() != null) {
			getProperBaseMatrix(mBitmapDisplayed, mBaseMatrix);
			setImageMatrix(Command.Layout, getImageViewMatrix());
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);
		if (!mScaleDetector.isInProgress())
			mGestureDetector.onTouchEvent(event);
		int action = event.getAction();
		int mask = action & MotionEvent.ACTION_MASK;
		if (mask == MotionEvent.ACTION_UP) {
			if (getScale() < 1f) {
				zoomTo(1f, 50);
			}
		}
		return true;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private void onZoom(float scale) {
		// super.onZoom( scale );
		if (!mScaleDetector.isInProgress())
			mCurrentScaleFactor = scale;
	}

	private void panBy(float dx, float dy) {
		RectF rect = getBitmapRect();
		RectF srect = new RectF(dx, dy, 0, 0);
		updateRect(rect, srect);
		postTranslate(srect.left, srect.top);
		center(true, true);
	}

	private void postScale(float scale, float centerX, float centerY) {
		mSuppMatrix.postScale(scale, scale, centerX, centerY);
		setImageMatrix(Command.Zoom, getImageViewMatrix());
	}

	private void postTranslate(float deltaX, float deltaY) {
		mSuppMatrix.postTranslate(deltaX, deltaY);
		setImageMatrix(Command.Move, getImageViewMatrix());
	}

	public void scrollBy(float x, float y) {
		panBy(x, y);
	}

	private void scrollBy(float distanceX, float distanceY, final float durationMs) {
		final float dx = distanceX;
		final float dy = distanceY;
		final long startTime = System.currentTimeMillis();
		mHandler.post(new Runnable() {

			float old_x = 0;
			float old_y = 0;

			@Override
			public void run() {
				long now = System.currentTimeMillis();
				float currentMs = Math.min(durationMs, now - startTime);
				float x = easeOut(currentMs, 0, dx, durationMs);
				float y = easeOut(currentMs, 0, dy, durationMs);
				panBy((x - old_x), (y - old_y));
				old_x = x;
				old_y = y;
				if (currentMs < durationMs) {
					mHandler.post(this);
				} else {
					RectF centerRect = getCenter(true, true);
					if (centerRect.left != 0 || centerRect.top != 0)
						scrollBy(centerRect.left, centerRect.top);
				}
			}
		});
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		setImageBitmap(bitmap, 0);
	}

	/**
	 * This is the ultimate method called when a new bitmap is set
	 * 
	 * @param bitmap
	 * @param rotation
	 */
	public void setImageBitmap(Bitmap bitmap, int rotation) {
		super.setImageBitmap(bitmap);
		Drawable d = getDrawable();
		if (d != null) {
			d.setDither(true);
		}
		mBitmapDisplayed.setBitmap(bitmap);
		mBitmapDisplayed.setRotation(rotation);
	}

	public void setImageBitmapReset(final Bitmap bitmap, final boolean reset) {
		setImageRotateBitmapReset(new ZoomableImageViewRotateBitmap(bitmap, 0), reset);
	}

	// protected void onZoom(float scale) {
	// }

	public void setImageBitmapReset(final Bitmap bitmap, final int rotation,
			final boolean reset) {
		setImageRotateBitmapReset(new ZoomableImageViewRotateBitmap(bitmap, rotation), reset);
	}

	private void setImageMatrix(Command command, Matrix matrix) {
		setImageMatrix(matrix);
	}

	private void setImageRotateBitmapReset(final ZoomableImageViewRotateBitmap bitmap,
			final boolean reset) {

		final int viewWidth = getWidth();
		if (viewWidth <= 0) {
			mOnLayoutRunnable = new Runnable() {

				@Override
				public void run() {
					setImageBitmapReset(bitmap.getBitmap(), bitmap.getRotation(), reset);
				}
			};
			return;
		}

		if (bitmap.getBitmap() != null) {
			getProperBaseMatrix(bitmap, mBaseMatrix);
			setImageBitmap(bitmap.getBitmap(), bitmap.getRotation());
		} else {
			mBaseMatrix.reset();
			setImageBitmap(null);
		}

		if (reset) {
			mSuppMatrix.reset();
		}

		setImageMatrix(Command.Reset, getImageViewMatrix());
		mMaxZoom = maxZoom();

		if (mListener != null) {
			mListener.onBitmapChanged(bitmap.getBitmap());
		}
		mScaleFactor = getMaxZoom() / 3;
	}

	public void setOnBitmapChangedListener(OnBitmapChangedListener listener) {
		mListener = listener;
	}

	private void updateRect(RectF bitmapRect, RectF scrollRect) {
		float width = getWidth();
		float height = getHeight();

		if (bitmapRect.top >= 0 && bitmapRect.bottom <= height)
			scrollRect.top = 0;
		if (bitmapRect.left >= 0 && bitmapRect.right <= width)
			scrollRect.left = 0;
		if (bitmapRect.top + scrollRect.top >= 0 && bitmapRect.bottom > height)
			scrollRect.top = (int) (0 - bitmapRect.top);
		if (bitmapRect.bottom + scrollRect.top <= (height - 0) && bitmapRect.top < 0)
			scrollRect.top = (int) ((height - 0) - bitmapRect.bottom);
		if (bitmapRect.left + scrollRect.left >= 0)
			scrollRect.left = (int) (0 - bitmapRect.left);
		if (bitmapRect.right + scrollRect.left <= (width - 0))
			scrollRect.left = (int) ((width - 0) - bitmapRect.right);
		// Log.d( LOG_TAG, "scrollRect(2): " + scrollRect.toString() );
	}

	public void zoomTo(float scale) {
		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;
		zoomTo(scale, cx, cy);
	}

	public void zoomTo(float scale, float durationMs) {
		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;
		zoomTo(scale, cx, cy, durationMs);
	}

	public void zoomTo(float scale, float centerX, float centerY) {
		if (scale > mMaxZoom)
			scale = mMaxZoom;
		float oldScale = getScale();
		float deltaScale = scale / oldScale;
		postScale(deltaScale, centerX, centerY);
		onZoom(getScale());
		center(true, true);
	}

	private void zoomTo(float scale, final float centerX, final float centerY,
			final float durationMs) {
		// Log.d( LOG_TAG, "zoomTo: " + scale + ", " + centerX + ": " + centerY
		// );
		final long startTime = System.currentTimeMillis();
		final float incrementPerMs = (scale - getScale()) / durationMs;
		final float oldScale = getScale();
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				long now = System.currentTimeMillis();
				float currentMs = Math.min(durationMs, now - startTime);
				float target = oldScale + (incrementPerMs * currentMs);
				zoomTo(target, centerX, centerY);
				if (currentMs < durationMs) {
					mHandler.post(this);
				} else {
					// if ( getScale() < 1f ) {}
				}
			}
		});
	}
}
