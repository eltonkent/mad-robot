package com.madrobot.graphics.bitmap;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.madrobot.geom.Rectangle;

/**
 * Filters that transform the shape of the bitmap
 * <p>
 * <b>Rotate</b><br/>
 * Rotation with the <code>angle</code> of 2.<br/>
 * <img src="../../../../resources/rotate.png" ><br/>
 * </p>
 * 
 * @author elton.stephen.kent
 * 
 */
public class ShapeTransformFilters extends TransformFilters {

	/**
	 * Create a twirl effect .
	 * 
	 * @param bitmap
	 * @param centreX
	 *            the X coordinate of the twirl effect. the coordinates are
	 *            represented as a float. for instance 0.5 represents the centre
	 *            of the bitmap.
	 * @param centreY
	 *            the Y coordinate of the twirl effect. the coordinates are
	 *            represented as a float. for instance 0.5 represents the centre
	 *            of the bitmap
	 * @param angle
	 *            can be anything between -710 to 720. negative angles are used
	 *            for clockwise twirl rotation.
	 * @param radius
	 *            of the twirl
	 * @param edgeAction
	 *            for the effect. {@link TransformFilters#EDGE_ACTION_CLAMP},
	 *            {@link TransformFilters#EDGE_ACTION_RGB_CLAMP},
	 *            {@link TransformFilters#EDGE_ACTION_WRAP} and
	 *            {@link TransformFilters#EDGE_ACTION_ZERO}. recommended:
	 *            {@link TransformFilters#EDGE_ACTION_CLAMP}
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap twirl(Bitmap bitmap, float centreX, float centreY, float angle,
			float radius, int edgeAction, Config outputConfig) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float icentreX = width * centreX;
		float icentreY = height * centreY;
		if (radius == 0)
			radius = Math.min(icentreX, icentreY);
		float radius2 = radius * radius;

		Rectangle transformedSpace = new Rectangle(0, 0, width, height);
		int[] inPixels = BitmapUtils.getPixels(bitmap);
		int srcWidth = width;
		int srcHeight = height;
		int srcWidth1 = width - 1;
		int srcHeight1 = height - 1;
		int outWidth = transformedSpace.width;
		int outHeight = transformedSpace.height;
		int[] outPixels = new int[outWidth];
		int[] destPixels = new int[outHeight * outWidth];
		float[] out = new float[2];

		float dx, dy, distance, a;
		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {

				/**
				 * transform inverse
				 */
				dx = x - icentreX;
				dy = y - icentreY;
				distance = dx * dx + dy * dy;
				if (distance > radius2) {
					out[0] = x;
					out[1] = y;
				} else {
					distance = (float) Math.sqrt(distance);
					a = (float) Math.atan2(dy, dx) + angle * (radius - distance) / radius;
					out[0] = icentreX + distance * (float) Math.cos(a);
					out[1] = icentreY + distance * (float) Math.sin(a);
				}

				int srcX = (int) Math.floor(out[0]);
				int srcY = (int) Math.floor(out[1]);
				float xWeight = out[0] - srcX;
				float yWeight = out[1] - srcY;
				int nw, ne, sw, se;

				if (srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
					// Easy case, all corners are in the image
					int i = srcWidth * srcY + srcX;
					nw = inPixels[i];
					ne = inPixels[i + 1];
					sw = inPixels[i + srcWidth];
					se = inPixels[i + srcWidth + 1];
				} else {
					// Some of the corners are off the image
					nw = TransformFilters.getPixel(inPixels, srcX, srcY, srcWidth, srcHeight,
							edgeAction);
					ne = TransformFilters.getPixel(inPixels, srcX + 1, srcY, srcWidth,
							srcHeight, edgeAction);
					sw = TransformFilters.getPixel(inPixels, srcX, srcY + 1, srcWidth,
							srcHeight, edgeAction);
					se = TransformFilters.getPixel(inPixels, srcX + 1, srcY + 1, srcWidth,
							srcHeight, edgeAction);
				}
				outPixels[x] = ImageMath.bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
			}
			// setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
			BitmapUtils.setPixelRow(outPixels, y, outWidth, destPixels);
		}
		return Bitmap.createBitmap(destPixels, outWidth, outHeight, outputConfig);
	}

	// private static void transformInverse(int x, int y, float[] out, float
	// sin, float cos) {
	// out[0] = (x * cos) - (y * sin);
	// out[1] = (y * cos) + (x * sin);
	// }

	private static void transformRotateSpace(Rectangle rect, boolean resize, float sin,
			float cos) {
		if (resize) {
			com.madrobot.geom.Point out = new com.madrobot.geom.Point(0, 0);
			int minx = Integer.MAX_VALUE;
			int miny = Integer.MAX_VALUE;
			int maxx = Integer.MIN_VALUE;
			int maxy = Integer.MIN_VALUE;
			int w = rect.width;
			int h = rect.height;
			int x = rect.x;
			int y = rect.y;

			for (int i = 0; i < 4; i++) {
				switch (i) {
				case 0:
					transform(x, y, out, sin, cos);
					break;
				case 1:
					transform(x + w, y, out, sin, cos);
					break;
				case 2:
					transform(x, y + h, out, sin, cos);
					break;
				case 3:
					transform(x + w, y + h, out, sin, cos);
					break;
				}
				minx = Math.min(minx, out.x);
				miny = Math.min(miny, out.y);
				maxx = Math.max(maxx, out.x);
				maxy = Math.max(maxy, out.y);
			}

			rect.x = minx;
			rect.y = miny;
			rect.width = maxx - rect.x;
			rect.height = maxy - rect.y;
		}
	}

	/**
	 * Rotate the bitmap by the given angle
	 * 
	 * @param src
	 * @param angle
	 * @param resize
	 *            allow resizing of the bitmap
	 * @param edgeAction
	 *            recommended : {@link #EDGE_ACTION_RGB_CLAMP}
	 * @param outputConfig
	 * 
	 * @return
	 */
	public static Bitmap rotate(Bitmap src, float angle, boolean resize, int edgeAction,
			Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		float cos, sin;
		cos = (float) Math.cos(angle);
		sin = (float) Math.sin(angle);

		Rectangle transformedSpace = new Rectangle(0, 0, width, height);
		transformRotateSpace(transformedSpace, resize, sin, cos);

		int[] inPixels = BitmapUtils.getPixels(src);// getRGB(src, 0, 0, width,
													// height, null);

		int srcWidth = width;
		int srcHeight = height;
		int srcWidth1 = width - 1;
		int srcHeight1 = height - 1;
		int outWidth = transformedSpace.width;
		int outHeight = transformedSpace.height;
		int outX, outY;
		int index = 0;
		int[] outPixels = new int[outWidth];
		int[] destPixels = new int[outWidth * outHeight];

		outX = transformedSpace.x;
		outY = transformedSpace.y;
		float[] out = new float[2];

		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				// transformInverse(outX + x, outY + y, out, sin, cos);

				out[0] = (outX + x * cos) - (outY + y * sin);
				out[1] = (outY + y * cos) + (outX + x * sin);

				int srcX = (int) Math.floor(out[0]);
				int srcY = (int) Math.floor(out[1]);
				float xWeight = out[0] - srcX;
				float yWeight = out[1] - srcY;
				int nw, ne, sw, se;

				if (srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
					// Easy case, all corners are in the image
					int i = srcWidth * srcY + srcX;
					nw = inPixels[i];
					ne = inPixels[i + 1];
					sw = inPixels[i + srcWidth];
					se = inPixels[i + srcWidth + 1];
				} else {
					// Some of the corners are off the image
					nw = getPixel(inPixels, srcX, srcY, srcWidth, srcHeight, edgeAction);
					ne = getPixel(inPixels, srcX + 1, srcY, srcWidth, srcHeight, edgeAction);
					sw = getPixel(inPixels, srcX, srcY + 1, srcWidth, srcHeight, edgeAction);
					se = getPixel(inPixels, srcX + 1, srcY + 1, srcWidth, srcHeight,
							edgeAction);
				}
				outPixels[x] = ImageMath.bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
			}
			BitmapUtils.setPixelRow(outPixels, y, transformedSpace.width, destPixels);
			// setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
		}
		return Bitmap.createBitmap(destPixels, transformedSpace.width,
				transformedSpace.height, outputConfig);
	}

	private static void transform(int x, int y, com.madrobot.geom.Point out, float sin,
			float cos) {
		out.x = (int) ((x * cos) + (y * sin));
		out.y = (int) ((y * cos) - (x * sin));
	}

	protected static float[] transformShearSpace(Rectangle r, float xangle, float yangle) {
		float tangent = (float) Math.tan(xangle);
		float xoffset = -r.height * tangent;
		if (tangent < 0.0)
			tangent = -tangent;
		r.width = (int) (r.height * tangent + r.width + 0.999999f);
		tangent = (float) Math.tan(yangle);
		float yoffset = -r.width * tangent;
		if (tangent < 0.0)
			tangent = -tangent;
		r.height = (int) (r.width * tangent + r.height + 0.999999f);
		return new float[] { xoffset, yoffset };
	}

	public static Bitmap shear(Bitmap src, float xangle, float yangle, int edgeAction,
			Config outputConfig) {
		float shx = (float) Math.sin(xangle);
		float shy = (float) Math.sin(yangle);

		int width = src.getWidth();
		int height = src.getHeight();

		// Rectangle originalSpace = new Rectangle(0, 0, width, height);
		Rectangle transformedSpace = new Rectangle(0, 0, width, height);
		float[] offset = transformShearSpace(transformedSpace, xangle, yangle);
		float xoffset = offset[0];
		float yoffset = offset[1];
		int[] inPixels = BitmapUtils.getPixels(src);

		int srcWidth = width;
		int srcHeight = height;
		int srcWidth1 = width - 1;
		int srcHeight1 = height - 1;
		int outWidth = transformedSpace.width;
		int outHeight = transformedSpace.height;
		int outX, outY, srcX, srcY;
		float xWeight, yWeight;
		// int index = 0;
		int[] outPixels = new int[outWidth];
		int[] destPixels = new int[outWidth * outHeight];
		outX = transformedSpace.x;
		outY = transformedSpace.y;
		float[] out = new float[2];

		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				transformInverse(outX + x, outY + y, out, xoffset, yoffset, shx, shy);
				srcX = (int) Math.floor(out[0]);
				srcY = (int) Math.floor(out[1]);
				xWeight = out[0] - srcX;
				yWeight = out[1] - srcY;
				int nw, ne, sw, se;

				if (srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
					// Easy case, all corners are in the image
					int i = srcWidth * srcY + srcX;
					nw = inPixels[i];
					ne = inPixels[i + 1];
					sw = inPixels[i + srcWidth];
					se = inPixels[i + srcWidth + 1];
				} else {
					// Some of the corners are off the image
					nw = getPixel(inPixels, srcX, srcY, srcWidth, srcHeight, edgeAction);
					ne = getPixel(inPixels, srcX + 1, srcY, srcWidth, srcHeight, edgeAction);
					sw = getPixel(inPixels, srcX, srcY + 1, srcWidth, srcHeight, edgeAction);
					se = getPixel(inPixels, srcX + 1, srcY + 1, srcWidth, srcHeight,
							edgeAction);
				}
				outPixels[x] = ImageMath.bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
			}
			BitmapUtils.setPixelRow(outPixels, y, transformedSpace.width, destPixels);
			// setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
		}
		return Bitmap.createBitmap(destPixels, transformedSpace.width,
				transformedSpace.height, outputConfig);
	}

	private static void transformInverse(int x, int y, float[] out, float xoffset,
			float yoffset, float shx, float shy) {
		out[0] = x + xoffset + (y * shx);
		out[1] = y + yoffset + (x * shy);
	}

}
