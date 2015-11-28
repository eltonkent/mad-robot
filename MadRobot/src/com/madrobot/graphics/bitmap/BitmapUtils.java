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
package com.madrobot.graphics.bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.madrobot.io.IOUtils;
import com.madrobot.math.MathUtils;

/**
 * Common bitmap utilities like scaling, rounding corners,transparency
 * 
 * @author ekent4
 * 
 */
public final class BitmapUtils {

	private static final int bottom = 8 * 1024;

	public static void addBitmapLayer(Bitmap src, Bitmap layer, int left, int top)
			throws IllegalArgumentException {
		if (!src.isMutable()) {
			throw new IllegalArgumentException("Bitmap is not mutable");
		}
		Canvas canvas = new Canvas(src);
		canvas.drawBitmap(layer, left, top, null);
	}

	/**
	 * crop the given bitmap
	 * 
	 * @param bitmap
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap crop(Bitmap bitmap, int x, int y, int width, int height,
			Bitmap.Config outputConfig) {
		return Bitmap.createBitmap(bitmap, x, y, width, height, null, false);
	}

	/**
	 * Compute the inSampleSize value for BitmapFactory.Options.
	 * <p>
	 * The <code>stream</code> is consumed. Hence it cannot be used again.
	 * </p>
	 * 
	 * @param stream
	 *            The Bitmap input stream
	 * @param maxW
	 *            max width of the target bitmap
	 * @param maxH
	 *            max height of the target bitmap
	 * @return
	 */
	public static int computeInSampleSize(InputStream stream, int maxW, int maxH) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(stream, null, options);
		double w = options.outWidth;
		double h = options.outHeight;
		int sampleSize = (int) Math.ceil(Math.max(w / maxW, h / maxH));
		return sampleSize;
	}

	/**
	 * Compute the inSampleSize for the BitmapFactory.Options used for image
	 * decoding
	 * 
	 * @param srcW
	 *            The width of the src image to be decoded.
	 * @param srcH
	 *            The height of the src image to be decoded.
	 * @param maxW
	 *            The maximum width of the image after decoding
	 * @param maxH
	 *            The maximum height of the image after decoding
	 * @return
	 */
	public static int computeInSampleSize(int srcW, int srcH, int maxW, int maxH) {

		int sampleSize = (int) Math.ceil(Math.max(srcW / maxW, srcH / maxH));
		return sampleSize;
	}

	/**
	 * Resizes a bitmap to the given destination width and height
	 * 
	 * @param input
	 * @param destWidth
	 * @param destHeight
	 * @return
	 */
	public static Bitmap resizeBitmap(Bitmap input, int destWidth, int destHeight) {
		int srcWidth = input.getWidth();
		int srcHeight = input.getHeight();
		boolean needsResize = false;
		float p;
		if (srcWidth > destWidth || srcHeight > destHeight) {
			needsResize = true;
			if (srcWidth > srcHeight && srcWidth > destWidth) {
				p = (float) destWidth / (float) srcWidth;
				destHeight = (int) (srcHeight * p);
			} else {
				p = (float) destHeight / (float) srcHeight;
				destWidth = (int) (srcWidth * p);
			}
		} else {
			destWidth = srcWidth;
			destHeight = srcHeight;
		}
		if (needsResize) {
			Bitmap output = Bitmap.createScaledBitmap(input, destWidth, destHeight, true);
			return output;
		} else {
			return input;
		}
	}

	/**
	 * Convert an image into a 2D array of pixels.
	 * 
	 * @param image
	 * @return
	 * @throws NullPointerException
	 *             if the image is null
	 */
	public static final int[][] convertTo2D(Bitmap bitmap) {
		if (bitmap == null) {
			throw new NullPointerException("Image is null");
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int[] argb = getPixels(bitmap);
		int[][] twoD = new int[h][w];
		convertTo2D(argb, w, h, twoD);
		return twoD;
	}

	/**
	 * 
	 * @param argb
	 *            data
	 * @param srcWidth
	 *            argb image width
	 * @param srcHeight
	 *            argb image height
	 * @param dest
	 *            the converted array int[argb.getHeight()][argb.getWidth()];
	 */
	public static final void convertTo2D(int[] argb, int srcWidth, int srcHeight, int[][] dest) {
		for (int i = 0; i < srcHeight; i++) {
			for (int j = 0; j < srcWidth; j++) {
				dest[i][j] = argb[i * srcWidth + j];
			}
		}
	}

	public static Bitmap createBitmap(InputStream is) {
		Bitmap bm = BitmapFactory.decodeStream(is);
		return bm;
	}

	public static Bitmap createBitmapFromAsset(Context context, String name) {
		try {
			return createBitmap(context.getAssets().open(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getBitmap(InputStream is) throws IOException {
		byte[] data = IOUtils.toByteArray(is);
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}

	public static Bitmap getBitmap(int[] pixels, int width, int height, Bitmap.Config config) {
		return Bitmap.createBitmap(pixels, width, height, config);
	}

	/**
	 * Get the size of the given bitmap in KB
	 * 
	 * @param bitmap
	 * @param bitsPerPixel
	 * @return
	 */
	public static long getBitmapSize(Bitmap bitmap, int bitsPerPixel) {
		long top = bitmap.getHeight() * bitmap.getWidth() * bitsPerPixel;
		return top / bottom;
	}

	/**
	 * Get the byte[] for the given bitmap
	 * 
	 * @param bitmap
	 * @param compressionFormat
	 * @return
	 */
	public static byte[] getByteArray(Bitmap bitmap, Bitmap.CompressFormat compressionFormat) {
		java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
		bitmap.compress(compressionFormat, 100, stream);
		return stream.toByteArray();

	}

	/**
	 * Get the pixels for the given bitmap
	 * 
	 * @param bitmap
	 * @return
	 */
	public static int[] getPixels(Bitmap bitmap) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int[] pixels = new int[w * h];
		bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
		return pixels;
	}

	public static int[] getPixelRow(Bitmap bitmap, int width, int columnNumber) {
		int[] sub = new int[width];
		bitmap.getPixels(sub, 0, width, 0, columnNumber, width, 1);
		return sub;
	}

	public static int[] getPixelRow(int[] argb, int x, int y, int width) {
		int[] row = new int[width];
		System.arraycopy(argb, (y * width) + x, row, 0, width);
		return row;
	}

	/**
	 * Returns the new height for the given degree. The new heigth symbols the
	 * heigth for an rotated rgb data array with the same degree rotation.
	 * 
	 * @param degree
	 *            the degree value of the rotation.
	 * @param width
	 *            the width of the rgb source.
	 * @param heigth
	 *            the heigth of the rgb source
	 * @return the new height of the rgb data.
	 */
	public static int getRotatedHeight(int degree, int width, int heigth, double angle) {
		double degreeCos = Math.cos(Math.PI * angle / 180);
		double degreeSin = Math.sin(Math.PI * angle / 180);
		if (degree == -90 || degree == 90 || degree == 270 || degree == -270) {
			return width;
		} else if (degree == 360 || degree == 180 || degree == 0) {
			return heigth;
		}
		long pointY1 = MathUtils.round(0 * degreeSin + 0 * degreeCos);
		long pointY2 = MathUtils.round(width * degreeSin + 0 * degreeCos);
		long pointY3 = MathUtils.round(0 * degreeSin + heigth * degreeCos);
		long pointY4 = MathUtils.round(width * degreeSin + heigth * degreeCos);
		long minY = pointY1;
		if (pointY2 < minY) {
			minY = pointY2;
		}
		if (pointY3 < minY) {
			minY = pointY3;
		}
		if (pointY4 < minY) {
			minY = pointY4;
		}
		long maxY = pointY1;
		if (pointY2 > maxY) {
			maxY = pointY2;
		}
		if (pointY3 > maxY) {
			maxY = pointY3;
		}
		if (pointY4 > maxY) {
			maxY = pointY4;
		}
		return (int) (maxY - minY);
	}

	/**
	 * Returns the new width for the given degree. The new width symbols the
	 * width for an rotated rgb data array with the same degree rotation.
	 * 
	 * @param degree
	 *            the degree value of the rotation.
	 * @param width
	 *            the width of the rgb source.
	 * @param heigth
	 *            the heigth of the rgb source
	 * @return the new width of the rgb data.
	 */
	public static int getRotatedWidth(int degree, int width, int heigth, double angle) {
		double degreeCos = Math.cos(Math.PI * angle / 180);
		double degreeSin = Math.sin(Math.PI * angle / 180);
		if (degree == -90 || degree == 90 || degree == 270 || degree == -270) {
			return heigth;
		} else if (degree == 360 || degree == 180 || degree == 0) {
			return width;
		}
		long pointX1 = 0; // MathUtil.round(0 * degreeCos - 0 * degreeSin);
		long pointX2 = MathUtils.round(width * degreeCos); // MathUtil.round(width
		// * degreeCos - 0
		// *degreeSin);
		long pointX3 = MathUtils.round(-heigth * degreeSin); // MathUtil.round(0
		// *degreeCos -
		// heigth
		// *degreeSin);
		long pointX4 = MathUtils.round(width * degreeCos - heigth * degreeSin);
		long minX = pointX1;
		if (pointX2 < minX) {
			minX = pointX2;
		}
		if (pointX3 < minX) {
			minX = pointX3;
		}
		if (pointX4 < minX) {
			minX = pointX4;
		}
		long maxX = pointX1;
		if (pointX2 > maxX) {
			maxX = pointX2;
		}
		if (pointX3 > maxX) {
			maxX = pointX3;
		}
		if (pointX4 > maxX) {
			maxX = pointX4;
		}
		return (int) (maxX - minX);
	}

	/**
	 * Scale a bitmap and give it specific values for each rounded corner.
	 * <p>
	 * The flexibility to individually specify the corner radius for each corner
	 * is illustrated below.<br/>
	 * <img src="../../../../resources/bitmapcorner.png"><br/>
	 * This bitmap has the following values for the corners</br>
	 * <ul>
	 * <li>Upper Left -10.0f</li>
	 * <li>Upper Right -25.0f</li>
	 * <li>Lower Right -15f</li>
	 * <li>Lower Left-5.0f</li>
	 * </ul>
	 * 
	 * </p>
	 * 
	 * @param context
	 *            Context object used to ascertain display density.
	 * @param bitmap
	 *            The original bitmap that will be scaled and have rounded
	 *            corners applied to it.
	 * @param upperLeft
	 *            Corner radius for upper left.
	 * @param upperRight
	 *            Corner radius for upper right.
	 * @param lowerRight
	 *            Corner radius for lower right.
	 * @param lowerLeft
	 *            Corner radius for lower left.
	 * @param endWidth
	 *            Width to which to scale original bitmap. If the width of the
	 *            ImageView is 50dp, just give 50.
	 * @param endHeight
	 *            Height to which to scale original bitmap.If the height of the
	 *            ImageView is 50dp, just give 50.
	 * @return Scaled bitmap with rounded corners.
	 */
	public static Bitmap getRoundedCornerBitmap(Context context, Bitmap bitmap,
			float upperLeft, float upperRight, float lowerRight, float lowerLeft,
			int endWidth, int endHeight) {
		float densityMultiplier = context.getResources().getDisplayMetrics().density;

		// scale incoming bitmap to appropriate px size given arguments and
		// display dpi
		bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(endWidth * densityMultiplier),
				Math.round(endHeight * densityMultiplier), true);

		// create empty bitmap for drawing
		Bitmap output = Bitmap.createBitmap(Math.round(endWidth * densityMultiplier),
				Math.round(endHeight * densityMultiplier), Config.ARGB_8888);

		// get canvas for empty bitmap
		Canvas canvas = new Canvas(output);
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// scale the rounded corners appropriately given dpi
		upperLeft *= densityMultiplier;
		upperRight *= densityMultiplier;
		lowerRight *= densityMultiplier;
		lowerLeft *= densityMultiplier;

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);

		// fill the canvas with transparency
		canvas.drawARGB(0, 0, 0, 0);

		// draw the rounded corners around the image rect. clockwise, starting
		// in upper left.
		canvas.drawCircle(upperLeft, upperLeft, upperLeft, paint);
		canvas.drawCircle(width - upperRight, upperRight, upperRight, paint);
		canvas.drawCircle(width - lowerRight, height - lowerRight, lowerRight, paint);
		canvas.drawCircle(lowerLeft, height - lowerLeft, lowerLeft, paint);

		// fill in all the gaps between circles. clockwise, starting at top.
		RectF rectT = new RectF(upperLeft, 0, width - upperRight, height / 2);
		RectF rectR = new RectF(width / 2, upperRight, width, height - lowerRight);
		RectF rectB = new RectF(lowerLeft, height / 2, width - lowerRight, height);
		RectF rectL = new RectF(0, upperLeft, width / 2, height - lowerLeft);

		canvas.drawRect(rectT, paint);
		canvas.drawRect(rectR, paint);
		canvas.drawRect(rectB, paint);
		canvas.drawRect(rectL, paint);

		// set up the rect for the image
		Rect imageRect = new Rect(0, 0, width, height);

		// set up paint object such that it only paints on Color.WHITE
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		// draw resized bitmap onto imageRect in canvas, using paint as
		// configured above
		canvas.drawBitmap(bitmap, imageRect, imageRect, paint);

		return output;
	}

	/**
	 * Convert a bitmap to a series of bitmap tiles
	 * 
	 * @param bitmap
	 *            to extract the tiles from
	 * @param number
	 *            number of tiles to extract.
	 * @param tileWidth
	 * @param tileHeight
	 * @param fromHorizontal
	 *            if true, extract tiles horizontally from the image. Else
	 *            extract vertically.
	 * @return
	 */
	public static Bitmap[] getTiles(Bitmap bitmap, int number, int tileWidth, int tileHeight,
			boolean fromHorizontal) {
		Bitmap[] tiles = new Bitmap[number];
		for (int i = 0; i < number; i++) {
			tiles[i] = Bitmap.createBitmap(bitmap, fromHorizontal ? i * tileWidth : 0,
					fromHorizontal ? 0 : i * tileHeight, tileWidth, tileHeight);
		}
		return tiles;
	}

	/**
	 * Read the given file as a bitmap
	 * 
	 * @param file
	 *            file to create a bitmap from
	 */
	public static Bitmap readBitmapFromFS(File file) {
		final BitmapFactory.Options option = new Options();
		option.inSampleSize = 1;
		Bitmap image = null;
		try {
			image = BitmapFactory.decodeStream(new FileInputStream(file), null, option);
			Thread.sleep(4);
		} catch (Exception e) {
			Log.e("Blog/DM", "Execption : " + e);
			e.printStackTrace();
		}
		return image;
	}

	/**
	 * Scales an rgb data unproportional in every new size you want bigger or
	 * smaller than the given original.
	 * 
	 * @param opacity
	 *            the maximum alpha value, 255 (0xFF) means fully opaque, 0
	 *            fully transparent. Lower alpha values will be preserved.
	 * @param rgbData
	 *            the original rgbdata
	 * @param newWidth
	 *            the new width for the new rgbdata
	 * @param newHeight
	 *            the new height for the new rgbdata
	 * @param oldWidth
	 *            the width from the oiginal rgbdata
	 * @param oldHeight
	 *            the height from the oiginal rgbdata
	 * @param newRgbData
	 *            the new rgbdata has to be initialised
	 */
	public static void scale(int opacity, int[] rgbData, int newWidth, int newHeight,
			int oldWidth, int oldHeight, int[] newRgbData) {
		int currentX = 0, currentY = 0;
		int oldLenght = rgbData.length;
		int newLength = newRgbData.length;
		int targetArrayIndex;
		int verticalShrinkFactorPercent = ((newHeight * 100) / oldHeight);
		int horizontalScaleFactorPercent = ((newWidth * 100) / oldWidth);
		int alpha = (opacity << 24); // is now 0xAA000000
		for (int i = 0; i < newLength; i++) {
			currentX = (currentX + 1) % newWidth;
			if (currentX == 0) {
				currentY++;
			}
			targetArrayIndex = ((currentX * 100) / horizontalScaleFactorPercent)
					+ (oldWidth * ((currentY * 100) / verticalShrinkFactorPercent));
			if (targetArrayIndex >= oldLenght) {
				targetArrayIndex = oldLenght - 1;
			}
			if (targetArrayIndex < 0) {
				targetArrayIndex = 0;
			}
			int pixel = rgbData[targetArrayIndex];
			if (opacity != 255) {
				int pixelAlpha = (pixel & 0xff000000) >>> 24;
				if (pixelAlpha > opacity) {
					pixel = (pixel & 0x00ffffff) | alpha;
				}
			}
			newRgbData[i] = pixel;
		}
	}

	/**
	 * Scales an rgb data unproportional in every new size you want bigger or
	 * smaller than the given original.
	 * 
	 * @param rgbData
	 *            the original rgbdata
	 * @param newWidth
	 *            the new width for the new rgbdata
	 * @param newHeight
	 *            the new height for the new rgbdata
	 * @param oldWidth
	 *            the width from the oiginal rgbdata
	 * @param oldHeight
	 *            the height from the oiginal rgbdata
	 * @param newRgbData
	 *            the new rgbdata has to be initialised
	 */
	public static void scale(int[] rgbData, int newWidth, int newHeight, int oldWidth,
			int oldHeight, int[] newRgbData) {

		int x, y, dy;
		int srcOffset;
		int destOffset;

		// Calculate the pixel ratio ( << 10 )
		final int pixelRatioWidth = (1024 * oldWidth) / newWidth;
		final int pixelRatioHeight = (1024 * oldHeight) / newHeight;

		y = 0;
		destOffset = 0;
		while (y < newHeight) {
			dy = ((pixelRatioHeight * y) >> 10) * oldWidth;
			srcOffset = 0;

			x = 0;
			while (x < newWidth) {
				newRgbData[destOffset + x] = rgbData[dy + (srcOffset >> 10)];
				srcOffset += pixelRatioWidth;
				x++;
			}

			destOffset += newWidth;
			y++;
		}
	}

	/**
	 * Returns the one scaled Pixel for the given new heigth and width.
	 * 
	 * @param oldLength
	 *            length of the rgb data source.
	 * @param oldWidth
	 *            the old width of the rgb data.
	 * @param oldHeigth
	 *            the old heigth of the rgb data.
	 * @param newWidth
	 *            the new width of the rgb data.
	 * @param newHeigth
	 *            the new heigth of the rgb data.
	 * @param currentX
	 *            the x position of the pixel to be scaled.
	 * @param currentY
	 *            the y position of the pixel to be scaled.
	 * @return position of the scaled pixel in the old rgb data array.
	 */
	public static int scaledPixel(int oldLength, int oldWidth, int oldHeigth, int newWidth,
			int newHeigth, int currentX, int currentY) {
		int targetArrayIndex;
		int verticalShrinkFactorPercent = ((newHeigth * 100) / oldHeigth);
		int horizontalScaleFactorPercent = ((newWidth * 100) / oldWidth);
		targetArrayIndex = ((currentX * 100) / horizontalScaleFactorPercent)
				+ (oldWidth * ((currentY * 100) / verticalShrinkFactorPercent));
		if (targetArrayIndex >= oldLength) {
			targetArrayIndex = oldLength - 1;
		}
		if (targetArrayIndex < 0) {
			targetArrayIndex = 0;
		}
		return targetArrayIndex;
	}

	/**
	 * Scales an image to fit into the specified available width and height
	 * while maintaining the ratio.
	 * 
	 * @param source
	 *            the source bitmap
	 * @param availableWidth
	 *            the new width for the new rgbdata
	 * @param availableHeight
	 *            the new height for the new rgbdata
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * @return the resulting image
	 */
	public static Bitmap scaleToFit(Bitmap source, int availableWidth, int availableHeight,
			Bitmap.Config outputConfig) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();

		if (sourceWidth > availableWidth || sourceHeight > availableHeight) {
			int[] rgbData = getPixels(source);

			double availableRatio = (double) availableWidth / (double) availableHeight;
			double sourceRatio = (double) sourceWidth / (double) sourceHeight;

			int targetWidth = availableWidth;
			int targetHeight = availableHeight;

			if (availableRatio < sourceRatio) {
				targetHeight = (int) (targetWidth / sourceRatio);
			} else {
				targetWidth = (int) (targetHeight * sourceRatio);
			}

			int[] newRgbData = new int[targetWidth * targetHeight];
			scale(rgbData, targetWidth, targetHeight, sourceWidth, sourceHeight, newRgbData);
			return getBitmap(newRgbData, targetWidth, targetHeight, outputConfig);
		}

		return source;
	}

	/**
	 * Stretches the rgb data horizontal to the given left and rigth stretch
	 * factor and returns the new rgb data array.
	 * 
	 * @param argbArray
	 *            the rgb data to be stretched.
	 * @param leftStrechFactor
	 *            the stretch factor of the left.
	 * @param rightStrechFactor
	 *            the stretch factor of the rigth.
	 * @param width
	 *            the source width of the rgb data.
	 * @param heigth
	 *            the source heigth of the rgb data.
	 * @return stretched rgb data array.
	 */
	public static int[] stretchHorizontal(int[] argbArray, int leftStrechFactor,
			int rightStrechFactor, int width, int heigth) {
		int newHeigthLeft = (heigth * leftStrechFactor) / 100;
		int newHeigthRight = (heigth * rightStrechFactor) / 100;
		int procentualScalingWidth;
		int biggerHeigth;
		if (newHeigthLeft < newHeigthRight) {
			procentualScalingWidth = (newHeigthRight - newHeigthLeft) / width;
			biggerHeigth = newHeigthRight;
		} else {
			procentualScalingWidth = (newHeigthLeft - newHeigthRight) / width;
			biggerHeigth = newHeigthLeft;
		}
		int[] newArgbArray = new int[biggerHeigth * width];
		return stretchHorizontal(argbArray, newHeigthLeft, newHeigthRight, biggerHeigth,
				width, heigth, procentualScalingWidth, newArgbArray);
	}

	/**
	 * Stretches the rgb data horizontal to the given left and heigth width and
	 * returns the new rgb data array.
	 * 
	 * 
	 * @param argbArray
	 *            the rgb data to be stretched.
	 * @param newLeftHeigth
	 *            the new left heigth of the rgb data.
	 * @param newRigthHeigth
	 *            the new rigth heigth of the rgb data.
	 * @param biggerHeigth
	 *            the bigger heigth of left and rigth heigth.
	 * @param width
	 *            the source width of the rgb data.
	 * @param heigth
	 *            the source heigth of the rgb data.
	 * @param procentualScalingHeight
	 *            the procentual scaling heigth(biggerHeigth -
	 *            smallerSmaller)/widthOfTheOriginalImage).
	 * @param newArgbArray
	 *            the new rgb data where the changes getting in.
	 * @return return the filled newArgbArray with stretched changes.
	 */
	public static int[] stretchHorizontal(int[] argbArray, int newLeftHeigth,
			int newRigthHeigth, int biggerHeigth, int width, int heigth,
			int procentualScalingHeight, int[] newArgbArray) {
		if (procentualScalingHeight == 0) {
			procentualScalingHeight++;
		}
		int length = newArgbArray.length;
		int oldLength = argbArray.length;
		// x and y position int the new array
		int idX = 0, idY = 0;
		// x and y position of the old array
		int x = 0, y = 0;
		// position in the new array
		int whereIamAt = 0;
		// Heighth for goal
		int newHeigth = newLeftHeigth;
		// start Heigth to goal
		int startColumn = (biggerHeigth - newHeigth) / 2;
		int endColumn = biggerHeigth - ((biggerHeigth - newHeigth) / 2);

		for (int i = 0; i < length; i++) {

			if (startColumn <= idY && endColumn >= idY) {
				newArgbArray[whereIamAt] = argbArray[scaledPixel(oldLength, width, heigth,
						width, newHeigth, x, y)];
				y = (y + 1) % newHeigth;
			} else {
				newArgbArray[whereIamAt] = 000000;
			}
			idY = (idY + 1) % (biggerHeigth);
			whereIamAt = idX + (idY * width);
			if (idY == 0) {
				idX++;
				x++;
				y = 0;
				if (newLeftHeigth < newRigthHeigth) {
					newHeigth += procentualScalingHeight;
				} else if (newLeftHeigth > newRigthHeigth) {
					newHeigth -= procentualScalingHeight;
				}
				startColumn = (biggerHeigth - newHeigth) / 2;
				endColumn = biggerHeigth - ((biggerHeigth - newHeigth) / 2);
			}
		}
		return newArgbArray;
	}

	/**
	 * Stretches the rgb data vertical to the given top and bottom stretch
	 * factor and returns the new rgb data array.
	 * 
	 * @param argbArray
	 *            the rgb data to be stretched.
	 * @param topStrechFactor
	 *            the stretch factor of the top.
	 * @param bottomStrechFactor
	 *            the stretch factor of the bottom.
	 * @param width
	 *            the source width of the rgb data.
	 * @param heigth
	 *            the source heigth of the rgb data.
	 * @return stretched rgb data array.
	 */
	public static int[] stretchVertical(int[] argbArray, int topStrechFactor,
			int bottomStrechFactor, int width, int heigth) {
		int newWidthTop = (width * topStrechFactor) / 100;
		int newWidthBottom = (width * bottomStrechFactor) / 100;
		int procentualScalingHeight;
		int biggerWidth;
		if (newWidthTop < newWidthBottom) {
			procentualScalingHeight = (newWidthBottom - newWidthTop) / heigth;
			biggerWidth = newWidthBottom;
		} else {
			procentualScalingHeight = (newWidthTop - newWidthBottom) / heigth;
			biggerWidth = newWidthTop;
		}
		int[] newArgbArray = new int[biggerWidth * heigth];
		return stretchVertical(argbArray, newWidthTop, newWidthBottom, biggerWidth, width,
				heigth, procentualScalingHeight, newArgbArray);
	}

	/**
	 * Stretches the rgb data vertical to the given top and bottom width and
	 * returns the new rgb data array.
	 * 
	 * 
	 * @param argbArray
	 *            the rgb data to be stretched.
	 * @param newWidthTop
	 *            the new top width of the rgb data.
	 * @param newWidthBottom
	 *            the new bottom width of the rgb data.
	 * @param biggerWidth
	 *            the bigger width of top and bottom width.
	 * @param width
	 *            the source width of the rgb data.
	 * @param heigth
	 *            the source heigth of the rgb data.
	 * @param procentualScalingHeight
	 *            the procentual scaling heigth(biggerWidth -
	 *            smallerWidth)/heigthOfTheOriginalImage).
	 * @param newArgbArray
	 *            the new rgb data where the changes getting in.
	 * @return return filled the newArgbArray with stretched changes.
	 */
	public static int[] stretchVertical(int[] argbArray, int newWidthTop, int newWidthBottom,
			int biggerWidth, int width, int heigth, int procentualScalingHeight,
			int[] newArgbArray) {
		if (procentualScalingHeight == 0) {
			procentualScalingHeight++;
		}
		int length = newArgbArray.length;
		int oldLength = argbArray.length;
		int insideCurrentY = 0;
		int insideCurrentX = 0, outsideCurrentX = 0;
		int sum1 = (biggerWidth - newWidthTop) / 2;
		int sum2 = biggerWidth - ((biggerWidth - newWidthTop) / 2);
		for (int i = 0; i < length; i++) {

			outsideCurrentX = (outsideCurrentX + 1) % biggerWidth;
			if (outsideCurrentX == 0) {
				if (newWidthTop < newWidthBottom) {
					newWidthTop += procentualScalingHeight;
					sum1 = (biggerWidth - newWidthTop) / 2;
					sum2 = biggerWidth - ((biggerWidth - newWidthTop) / 2);
				} else if (newWidthTop > newWidthBottom) {
					newWidthTop -= procentualScalingHeight;
					sum1 = (biggerWidth - newWidthTop) / 2;
					sum2 = biggerWidth - ((biggerWidth - newWidthTop) / 2);
				}
				insideCurrentY++;
				insideCurrentX = 0;
			}
			if (outsideCurrentX >= sum1 && outsideCurrentX < sum2) {
				insideCurrentX = (insideCurrentX + 1) % newWidthTop;
				newArgbArray[i] = argbArray[scaledPixel(oldLength, width, heigth, newWidthTop,
						heigth, insideCurrentX, insideCurrentY)];
			} else {
				newArgbArray[i] = 000000;
			}
		}
		return newArgbArray;
	}

	// /**
	// *
	// * @param image
	// * @param xOffset
	// * of the shadow
	// * @param yOffset
	// * of the shadow
	// * @param size
	// * @param innerColor
	// * of the shadow
	// * @param outerColor
	// * of the shadow
	// * @return
	// */
	// public static final Bitmap dropShadow(Bitmap bitmap, int xOffset, int
	// yOffset, int size,
	// int innerColor, int outerColor) {
	// int w = bitmap.getWidth();
	// int h = bitmap.getHeight();
	// int[] data = getPixels(bitmap);
	// dropShadow(data, w, h, xOffset, yOffset, size, innerColor, outerColor);
	// return Bitmap.createBitmap(data, bitmap.getWidth(), bitmap.getHeight(),
	// Bitmap.Config.ARGB_8888);
	// }
	//
	// /**
	// * <p>
	// * Paints a dropshadow behind a given ARGB-Array, whereas you are able to
	// * specify the shadows inner and outer color.
	// * </p>
	// * <p>
	// * Note that the dropshadow just works for fully opaque pixels and that it
	// * needs a transparent margin to draw the shadow.
	// * </p>
	// * <p>
	// * Choosing the same inner and outer color and varying the transparency is
	// * recommended. Dropshadow just works for fully opaque pixels.
	// * </p>
	// *
	// * @param argbData
	// * of the image
	// * @param width
	// * of the image
	// * @param height
	// * of the image
	// * @param xOffset
	// * use this for finetuning the shadow's horizontal position.
	// * Negative values move the shadow to the left.
	// * @param yOffset
	// * use this for finetuning the shadow's vertical position.
	// * Negative values move the shadow to the top.
	// * @param size
	// * use this for finetuning the shadows radius.
	// * @param innerColor
	// * the inner color of the shadow, which should be less opaque
	// * than the text.
	// * @param outerColor
	// * the outer color of the shadow, which should be less than
	// * opaque the inner color.
	// */
	// private final static void dropShadow(int[] argbData, int width, int
	// height, int xOffset, int yOffset,
	// int size, int innerColor, int outerColor) {
	// // additional Margin for the image because of the shadow
	// int iLeft = size - xOffset < 0 ? 0 : size - xOffset;
	// int iRight = size + xOffset < 0 ? 0 : size + xOffset;
	// int iTop = size - yOffset < 0 ? 0 : size - yOffset;
	// int iBottom = size + yOffset < 0 ? 0 : size + yOffset;
	//
	// // set colors
	// int[] gradient = ColorUtil.getGradient(innerColor, outerColor, size);
	//
	// // walk over the text and look for non-transparent Pixels
	// for(int ix = -size + 1; ix < size; ix++){
	// for(int iy = -size + 1; iy < size; iy++){
	//
	// int r = (Math.abs(ix) + Math.abs(iy)) / 2;
	// // }
	// if(r < size){
	// int gColor = gradient[r];
	//
	// for(int col = iLeft, row; col < width/* +iLeft */- iRight; col++){
	// for(row = iTop; row < height - iBottom/* +iTop */- 1; row++){
	//
	// // draw if an opaque pixel is found and the
	// // destination is less opaque then the shadow
	// if(argbData[row * (width /* + size2 */) + col] >>> 24 == 0xFF
	// && argbData[(row + yOffset + iy) * (width /*
	// * size2
	// */) + col + xOffset + ix] >>> 24 < gColor >>> 24){
	// argbData[(row + yOffset + iy) * (width /*
	// * +
	// * size2
	// */) + col + xOffset + ix] = gColor;
	// }
	// }
	// }
	// }
	// }
	// }
	// }

	/**
	 * Write a bitmap to the file specified
	 * 
	 * @param bitmap
	 * @param file
	 *            The full path of the file to write to including filename and
	 *            extension.
	 * @throws IOException
	 * @throws llegalArgumentException
	 *             if the file specified is a directory
	 */
	public static void writeBitmapToFile(Bitmap bitmap, File file) throws IOException {
		if (!file.isDirectory()) {
			throw new IllegalArgumentException("File is a directory");
		}
		Bitmap.CompressFormat format = (file.getPath().endsWith("jpg") || file.getPath()
				.endsWith("JPG")) ? CompressFormat.JPEG : CompressFormat.PNG;
		final FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(format, 100, fos);
		fos.close();
	}

	/**
	 * Set a row of pixels in the given argb array
	 * 
	 * @param row
	 *            to be set
	 * @param column
	 *            the current column
	 * @param width
	 *            of the bitmap
	 * @param argb
	 *            the argb array of the bitmap
	 */
	public static void setPixelRow(int[] row, int column, int width, int[] argb) {
		for (int i = 0; i < row.length; i++) {
			int nRow = column * width;
			argb[nRow + i] = row[i];
		}
	}

	public static void recycleBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			bitmap.recycle();
		}
	}

}
