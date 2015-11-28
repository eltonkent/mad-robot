package com.madrobot.ui.widget.imageview;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * 
 * @author elton.stephen.kent
 * @hide
 * @exclude
 */
class ZoomableImageViewRotateBitmap {

	private Bitmap mBitmap;
	private int mRotation;
	private int mWidth;
	private int mHeight;
	private int mBitmapWidth;
	private int mBitmapHeight;

	ZoomableImageViewRotateBitmap(Bitmap bitmap, int rotation) {
		mRotation = rotation % 360;
		setBitmap(bitmap);
	}

	void setRotation(int rotation) {
		mRotation = rotation;
		invalidate();
	}

	int getRotation() {
		return mRotation % 360;
	}

	Bitmap getBitmap() {
		return mBitmap;
	}

	void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;

		if (mBitmap != null) {
			mBitmapWidth = bitmap.getWidth();
			mBitmapHeight = bitmap.getHeight();
			invalidate();
		}
	}

	void invalidate() {
		Matrix matrix = new Matrix();
		int cx = mBitmapWidth / 2;
		int cy = mBitmapHeight / 2;
		matrix.preTranslate(-cx, -cy);
		matrix.postRotate(mRotation);
		matrix.postTranslate(cx, cx);

		RectF rect = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
		matrix.mapRect(rect);
		mWidth = (int) rect.width();
		mHeight = (int) rect.height();
	}

	Matrix getRotateMatrix() {
		Matrix matrix = new Matrix();
		if (mRotation != 0) {
			int cx = mBitmapWidth / 2;
			int cy = mBitmapHeight / 2;
			matrix.preTranslate(-cx, -cy);
			matrix.postRotate(mRotation);
			matrix.postTranslate(mWidth / 2, mHeight / 2);
		}

		return matrix;
	}

	int getHeight() {
		return mHeight;
	}

	int getWidth() {
		return mWidth;
	}

	void recycle() {
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}
}
