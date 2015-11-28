package com.madrobot.graphics.bitmap;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.madrobot.geom.Rectangle;

/**
 * Configuration settings for the output bitmap when using any of the bitmap
 * filters.
 * 
 * @author elton.stephen.kent
 * 
 */
public class OutputConfiguration {

	static class BitmapMeta {
		int bitmapHeight;
		int bitmapWidth;
		/**
		 * the target area height affected by the filter
		 */
		int targetHeight;
		/**
		 * the target area width affected by the filter
		 */
		int targetWidth;
		int x;
		int y;

	}

	Rectangle affectedArea;

	boolean canRecycleSrc;
	Bitmap.Config config = Config.ARGB_8888;

	void checkRectangleBounds(int width, int height) {

		if (!new Rectangle(0, 0, width, height).contains(affectedArea)) {

			throw new IllegalArgumentException(
					"affected area bounds exceeds the bounds of the image ");
		}
	}

	/**
	 * Area of the bitmap that is affected by the filter
	 * 
	 * @return
	 */
	public Rectangle getAffectedArea() {
		return affectedArea;
	}

	/**
	 * res[0]= bitmap width<br/>
	 * res[1]=bitmap height<br/>
	 * res[2]=target width where the filter acts res[3]=target height where the
	 * filter acts
	 * 
	 * @param bitmap
	 * @param outputConfig
	 * @return
	 */
	BitmapMeta getBitmapMeta(Bitmap bitmap) {

		BitmapMeta meta = new BitmapMeta();
		meta.bitmapWidth = bitmap.getWidth();
		meta.bitmapHeight = bitmap.getHeight();

		if (affectedArea != null) {
			checkRectangleBounds(meta.bitmapWidth, meta.bitmapHeight);
		}

		meta.targetWidth = meta.bitmapWidth;
		meta.targetHeight = meta.bitmapHeight;

		if (affectedArea != null) {
			meta.targetWidth = affectedArea.x + affectedArea.width;
			if (meta.targetWidth > meta.bitmapWidth) {
				meta.targetWidth = meta.bitmapWidth;
			}
			meta.targetHeight = affectedArea.y + affectedArea.height;
			if (meta.targetHeight > meta.bitmapHeight) {
				meta.targetHeight = meta.bitmapHeight;
			}
		}

		meta.x = affectedArea != null ? affectedArea.x : 0;
		meta.y = affectedArea != null ? affectedArea.y : 0;
		return meta;
	}

	/**
	 * Get the configuration of the o/p bitmap.
	 * 
	 * @return
	 */
	public Bitmap.Config getConfig() {
		return config;
	}

	public boolean isCanRecycleSrc() {
		return canRecycleSrc;
	}

	/**
	 * Set the area of the bitmap that should be affected by the filter.
	 * 
	 * @param affectedArea
	 *            if null, the filter is applied to the whole bitmap.
	 */
	public void setAffectedArea(Rectangle affectedArea) {
		this.affectedArea = affectedArea;
	}

	/**
	 * Recycle the src image.
	 * <p>
	 * The default is false.
	 * </p>
	 * 
	 * @param canRecycleSrc
	 */
	public void setCanRecycleSrc(boolean canRecycleSrc) {
		this.canRecycleSrc = canRecycleSrc;
	}

	/**
	 * Set the configuration of the o/p bitmap.
	 * <p>
	 * The default configuration is ARGB_8888.<br/>
	 * This field is not used if {@link #setModifySource(boolean)} is set to
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param config
	 */
	public void setConfig(Bitmap.Config config) {
		this.config = config;
	}
}
