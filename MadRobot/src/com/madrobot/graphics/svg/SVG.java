/*******************************************************************************
 * Copyright (c) 2011 MadRobot.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *  Elton Kent - initial API and implementation
 ******************************************************************************/
package com.madrobot.graphics.svg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;

import com.madrobot.security.Base64.OutputStream;

/**
 * Describes a vector Picture object, and optionally its bounds.
 * 
 */
public class SVG {

	public static class MetaData {
		public final String title;
		public final String description;

		MetaData(String title, String desc) {
			this.title = title;
			this.description = desc;
		}

		@Override
		public String toString() {
			return "MetaData [title=" + title + ", description=" + description + "]";
		}

	}

	private MetaData data;

	public MetaData getMetaData() {
		return data;
	}

	/**
	 * These are the bounds for the SVG specified as a hidden "bounds" layer in
	 * the SVG.
	 */
	private RectF bounds;

	/**
	 * These are the estimated bounds of the SVG computed from the SVG elements
	 * while parsing. Note that this could be null if there was a failure to
	 * compute limits (ie. an empty SVG).
	 */
	private RectF limits = null;

	/**
	 * The parsed Picture object.
	 */
	private Picture picture;

	/**
	 * Construct a new SVG.
	 * 
	 * @param picture
	 *            the parsed picture object.
	 * @param bounds
	 *            the bounds computed from the "bounds" layer in the SVG.
	 */
	SVG(Picture picture, MetaData data, RectF bounds) {
		this.picture = picture;
		this.bounds = bounds;
		this.data = data;
	}

	/**
	 * Create a picture drawable from the SVG.
	 * 
	 * @return the PictureDrawable.
	 */
	public Drawable createDrawable() {
		return new PictureDrawable(picture);
		// return new PictureDrawable(picture) {
		// @Override
		// public int getIntrinsicWidth() {
		// if (bounds != null) {
		// return (int) bounds.width();
		// } else if (limits != null) {
		// return (int) limits.width();
		// } else {
		// return -1;
		// }
		// }
		//
		// @Override
		// public int getIntrinsicHeight() {
		// if (bounds != null) {
		// return (int) bounds.height();
		// } else if (limits != null) {
		// return (int) limits.height();
		// } else {
		// return -1;
		// }
		// }
		// };
	}

	public Bitmap createBitmap(Bitmap.Config config) {
		Drawable drawable = createDrawable();
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), config);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawPicture(picture);
		return bitmap;
	}

	/**
	 * Write the rendered image to an ouput stream
	 * 
	 * @param os
	 */
	public void compress(OutputStream os) {
		picture.writeToStream(os);
	}

	/**
	 * Gets the bounding rectangle for the SVG, if one was specified.
	 * 
	 * @return rectangle representing the bounds.
	 */
	public RectF getBounds() {
		return bounds;
	}

	/**
	 * Gets the bounding rectangle for the SVG that was computed upon parsing.
	 * It may not be entirely accurate for certain curves or transformations,
	 * but is often better than nothing.
	 * 
	 * @return rectangle representing the computed bounds.
	 */
	public RectF getLimits() {
		return limits;
	}

	/**
	 * Get the parsed SVG picture data.
	 * 
	 * @return the picture.
	 */
	public Picture getPicture() {
		return picture;
	}

	/**
	 * Set the limits of the SVG, which are the estimated bounds computed by the
	 * parser.
	 * 
	 * @param limits
	 *            the bounds computed while parsing the SVG, may not be entirely
	 *            accurate.
	 */
	void setLimits(RectF limits) {
		this.limits = limits;
	}
}
