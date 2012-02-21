package com.madrobot.graphics.bitmap;

import com.madrobot.graphics.PixelUtils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class BitmapFilters {
	/**
	 * Treat pixels off the edge as zero.
	 */
	public static int ZERO_EDGES = 0;

	/**
	 * Clamp pixels off the edge to the nearest edge.
	 */
	public static int CLAMP_EDGES = 1;

	/**
	 * Wrap pixels off the edge to the opposite edge.
	 */
	public static int WRAP_EDGES = 2;

	private static Bitmap applyFilter(Bitmap bitmap, int value, byte[][] filter, Bitmap.Config outputConfig) {
		int[] argbData = BitmapUtils.getPixels(bitmap);
		applyFilter(filter, value, argbData, bitmap.getWidth(), bitmap.getHeight());
		return Bitmap.createBitmap(argbData, bitmap.getWidth(), bitmap.getHeight(), outputConfig);

	}

	/**
	 * Apply sepia filters
	 * 
	 * @param bitmap
	 * @param depth
	 *            Sepia depth. values between 1-100 provide an optimal output.
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * @return
	 */
	public static final Bitmap applySepia(Bitmap bitmap, int depth, Bitmap.Config outputConfig) {
		/* Tested Apr 27 2011 */
		int[] argb = BitmapUtils.getPixels(bitmap);
		for (int i = 0; i < argb.length; i++) {
			argb[i] = PixelUtils.applySepia(argb[i], depth);
		}
		return Bitmap.createBitmap(argb, bitmap.getWidth(), bitmap.getHeight(), outputConfig);
	}

	/**
	 * Create a reflection of an image
	 * <p>
	 * Reflection with <code>bgColor=0xffffff</code> and <code> reflectionHeight=20</code>
	 * <table border="0">
	 * <tr>
	 * <td>
	 * <img src="../../../resources/gray.png"></td>
	 * <td><img src="../../../resources/reflection.png"></td>
	 * </tr>
	 * </table>
	 * </p>
	 * 
	 * @param image
	 *            source image
	 * @param reflectionHeight
	 *            height of the reflection
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * 
	 * @return Image
	 */
	public static final Bitmap createReflection(final Bitmap image, final int reflectionHeight, Bitmap.Config outputConfig) {
		/* Tested Apr 27 2011 */
		int w = image.getWidth();
		int h = image.getHeight();

		final Bitmap reflectedImage = Bitmap.createBitmap(w, h + reflectionHeight, outputConfig);
		reflectedImage.setDensity(image.getDensity());
		Canvas canvas = new Canvas(reflectedImage);

		Paint bitmapPaint2 = new Paint();
		bitmapPaint2.setAntiAlias(true);

		canvas.drawBitmap(image, 0, 0, bitmapPaint2);

		int[] rgba = new int[w];
		int currentY = -1;

		for (int i = 0; i < reflectionHeight; i++) {
			int y = (h - 1) - (i * h / reflectionHeight);

			if (y != currentY) {
				image.getPixels(rgba, 0, w, 0, y, w, 1);
			}

			int alpha = 0xff - (i * 0xff / reflectionHeight);

			for (int j = 0; j < w; j++) {
				int origAlpha = (rgba[j] >> 24);
				int newAlpha = (alpha & origAlpha) * alpha / 0xff;

				rgba[j] = (rgba[j] & 0x00ffffff);
				rgba[j] = (rgba[j] | (newAlpha << 24));
			}
			canvas.drawBitmap(rgba, 0, w, 0, h + i, w, 1, true, bitmapPaint2);
		}
		return reflectedImage;
	}

	/**
	 * The weights specified are scaled so that the image average brightness should not change after the halftoning.
	 * 
	 * @param n0
	 *            the weight for pixel (r,c+1)
	 * @param n1
	 *            the weight for pixel (r,c+2)
	 * @param n2
	 *            the weight for pixel (r,c+3)
	 * @param n3
	 *            the weight for pixel (r,c+4)
	 * @param n4
	 *            the weight for pixel (r,c+5)
	 */
	public static final Bitmap deBlurHorizontalHalftone(Bitmap bitmap, int n0, int n1, int n2, int n3, int n4, Bitmap.Config outputConfig) {
		int sum = n0 + n1 + n2 + n3 + n4;
		n0 = 8 * n0 / sum;
		n1 = 8 * n1 / sum;
		n2 = 8 * n2 / sum;
		n3 = 8 * n3 / sum;
		n4 = 8 * n4 / sum;
		// integer division may make the sum not quite 8. correct this
		// in the weight for pixel (r,c+1)
		n0 = 8 - (n0 + n1 + n2 + n3 + n4);

		int[] bData = BitmapUtils.getPixels(bitmap);
		for (int i = 0; i < bitmap.getHeight(); i++) {
			int nRow = i * bitmap.getWidth();
			for (int j = 0; j < bitmap.getWidth(); j++) {
				int bVal = bData[nRow + j];
				int bNewVal;
				if (bVal >= 0) {
					bNewVal = Integer.MAX_VALUE;
				} else {
					bNewVal = Integer.MIN_VALUE;
				}
				int nDiff = bVal - bNewVal;
				if (j < bitmap.getWidth() - 1) {
					bData[nRow + j + 1] = Math.max(Integer.MIN_VALUE,
							Math.min(Integer.MAX_VALUE, bData[nRow + j + 1] + n0 * nDiff / 8));
				}
				if (j < bitmap.getWidth() - 2) {
					bData[nRow + j + 2] = Math.max(Integer.MIN_VALUE,
							Math.min(Integer.MAX_VALUE, bData[nRow + j + 2] + n1 * nDiff / 8));

				}
				if (j < bitmap.getWidth() - 3) {
					bData[nRow + j + 3] = Math.max(Integer.MIN_VALUE,
							Math.min(Integer.MAX_VALUE, bData[nRow + j + 3] + n2 * nDiff / 8));

				}
				if (j < bitmap.getWidth() - 4) {
					bData[nRow + j + 4] = Math.max(Integer.MIN_VALUE,
							Math.min(Integer.MAX_VALUE, bData[nRow + j + 4] + n3 * nDiff / 8));

				}
				if (j < bitmap.getWidth() - 5) {
					bData[nRow + j + 5] = Math.max(Integer.MIN_VALUE,
							Math.min(Integer.MAX_VALUE, bData[nRow + j + 5] + n4 * nDiff / 8));

				}
			}
		}
		return Bitmap.createBitmap(bData, bitmap.getWidth(), bitmap.getHeight(), outputConfig);

	}

	/**
	 * Decrease the color depth of the given bitmap
	 * 
	 * @param pixel
	 * @param bitOffset
	 * @return
	 */
	public static Bitmap decreaseColorDepth(final Bitmap bitmap, final int bitOffset, Bitmap.Config config) {
		int A, R, G, B;
		int[] pixels = BitmapUtils.getPixels(bitmap);
		for (int i = 0; i < pixels.length; i++) {
			A = Color.alpha(pixels[i]);
			R = Color.red(pixels[i]);
			G = Color.green(pixels[i]);
			B = Color.blue(pixels[i]);
			// round-off color offset
			R = ((R + (bitOffset / 2)) - ((R + (bitOffset / 2)) % bitOffset) - 1);
			if (R < 0) {
				R = 0;
			}
			G = ((G + (bitOffset / 2)) - ((G + (bitOffset / 2)) % bitOffset) - 1);
			if (G < 0) {
				G = 0;
			}
			B = ((B + (bitOffset / 2)) - ((B + (bitOffset / 2)) % bitOffset) - 1);
			if (B < 0) {
				B = 0;
			}

			pixels[i] = Color.argb(A, R, G, B);
		}
		return Bitmap.createBitmap(pixels, bitmap.getWidth(), bitmap.getHeight(), config);
	}

	/**
	 * Invert the bitmap's colors.
	 * 
	 * @param bitmap
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * @return
	 */
	public static final Bitmap invert(Bitmap bitmap, Bitmap.Config outputConfig) {
		/* Tested Apr 27 2011 */
		int[] argb = BitmapUtils.getPixels(bitmap);
		for (int i = 0; i < argb.length; i++) {
			argb[i] = PixelUtils.invertColor(argb[i]);
		}
		return Bitmap.createBitmap(argb, bitmap.getWidth(), bitmap.getHeight(), outputConfig);
	}

	/**
	 * Poseterize the given bitmap
	 * 
	 * @param bitmap
	 * @param depth
	 *            Posterization depth
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * @return
	 */
	public static final Bitmap posterize(Bitmap bitmap, int depth, Bitmap.Config outputConfig) {
		int[] argb = BitmapUtils.getPixels(bitmap);
		for (int i = 0; i < argb.length; i++) {
			argb[i] = PixelUtils.posterizePixel(argb[i], depth);
		}
		return Bitmap.createBitmap(argb, bitmap.getWidth(), bitmap.getHeight(), outputConfig);
	}

	/**
	 * Tint the given bitmap
	 * 
	 * @param bitmap
	 * @param tintDegree
	 *            degree to tint the bitmap
	 * @param config
	 *            for the output bitmap
	 * @return
	 */
	public static Bitmap tint(Bitmap bitmap, int tintDegree, Bitmap.Config config) {
		int pich = bitmap.getHeight();
		int picw = bitmap.getWidth();
		int[] pix = BitmapUtils.getPixels(bitmap);
		int RY, BY, RYY, GYY, BYY, R, G, B, Y;
		double angle = (3.14159d * tintDegree) / 180.0d;
		int S = (int) (256.0d * Math.sin(angle));
		int C = (int) (256.0d * Math.cos(angle));

		for (int i = 0; i < pix.length; i++) {
			int r = (pix[i] >> 16) & 0xff;
			int g = (pix[i] >> 8) & 0xff;
			int b = pix[i] & 0xff;
			RY = (70 * r - 59 * g - 11 * b) / 100;
			// GY = (-30 * r + 41 * g - 11 * b) / 100;
			BY = (-30 * r - 59 * g + 89 * b) / 100;
			Y = (30 * r + 59 * g + 11 * b) / 100;
			RYY = (S * BY + C * RY) / 256;
			BYY = (C * BY - S * RY) / 256;
			GYY = (-51 * RYY - 19 * BYY) / 100;
			R = Y + RYY;
			R = (R < 0) ? 0 : ((R > 255) ? 255 : R);
			G = Y + GYY;
			G = (G < 0) ? 0 : ((G > 255) ? 255 : G);
			B = Y + BYY;
			B = (B < 0) ? 0 : ((B > 255) ? 255 : B);
			pix[i] = 0xff000000 | (R << 16) | (G << 8) | B;
		}
		// for (int y = 0; y < pich; y++)
		// for (int x = 0; x < picw; x++) {
		// int index = y * picw + x;
		//
		// }

		return Bitmap.createBitmap(pix, picw, pich, config);

	}

	/**
	 * Saturate the given bitmap
	 * 
	 * @param bitmap
	 * @param percent
	 * @param outputConfig
	 * @return
	 */
	public static final Bitmap saturate(Bitmap bitmap, int percent, Bitmap.Config outputConfig) {
		int[] argb = BitmapUtils.getPixels(bitmap);
		for (int i = 0; i < argb.length; i++) {
			argb[i] = PixelUtils.setSaturation(argb[i], percent);
		}
		return Bitmap.createBitmap(argb, bitmap.getWidth(), bitmap.getHeight(), outputConfig);
	}

	/**
	 * Performs a convolution of an image with a given matrix.
	 * 
	 * @param filterMatrix
	 *            a matrix, which should have odd rows an colums (not neccessarily a square). The matrix is used for a
	 *            2-dimensional convolution. Negative values are possible.
	 * 
	 * @param brightness
	 *            you can vary the brightness of the image measured in percent. Note that the algorithm tries to keep
	 *            the original brightness as far as is possible.
	 * 
	 * @param argbData
	 *            the image (RGB+transparency)
	 * 
	 * @param width
	 *            of the given Image
	 * 
	 * @param height
	 *            of the given Image Be aware that the computation time depends on the size of the matrix.
	 * @throws IllegalArgumentException
	 *             if the filter matrix length is an even number
	 */
	private final static void applyFilter(byte[][] filterMatrix, int brightness, int[] argbData, int width, int height) {
		// ############ tested by $t3p#3n on 29-july-08 #################//
		int COLOR_BIT_MASK = 0x000000FF;
		// check whether the matrix is ok
		if ((filterMatrix.length % 2 != 1) || (filterMatrix[0].length % 2 != 1)) {
			throw new IllegalArgumentException();
		}

		int fhRadius = filterMatrix.length / 2 + 1;
		int fwRadius = filterMatrix[0].length / 2 + 1;
		int currentPixel = 0;
		int newTran, newRed, newGreen, newBlue;

		// compute the brightness
		int divisor = 0;
		for (int fCol, fRow = 0; fRow < filterMatrix.length; fRow++) {
			for (fCol = 0; fCol < filterMatrix[0].length; fCol++) {
				divisor += filterMatrix[fRow][fCol];
			}
		}
		// TODO: if (divisor==0), because of negativ matrixvalues
		if (divisor == 0) {
			return; // no brightness
		}

		// copy the neccessary imagedata into a small buffer
		int[] tmpRect = new int[width * (filterMatrix.length)];
		System.arraycopy(argbData, 0, tmpRect, 0, width * (filterMatrix.length));

		for (int fCol, fRow, col, row = fhRadius - 1; row + fhRadius < height + 1; row++) {
			for (col = fwRadius - 1; col + fwRadius < width + 1; col++) {

				// perform the convolution
				newTran = 0;
				newRed = 0;
				newGreen = 0;
				newBlue = 0;

				for (fRow = 0; fRow < filterMatrix.length; fRow++) {

					for (fCol = 0; fCol < filterMatrix[0].length; fCol++) {

						// take the Data from the little buffer and skale the
						// color
						currentPixel = tmpRect[fRow * width + col + fCol - fwRadius + 1];
						if (((currentPixel >>> 24) & COLOR_BIT_MASK) != 0) {
							newTran += filterMatrix[fRow][fCol] * ((currentPixel >>> 24) & COLOR_BIT_MASK);
							newRed += filterMatrix[fRow][fCol] * ((currentPixel >>> 16) & COLOR_BIT_MASK);
							newGreen += filterMatrix[fRow][fCol] * ((currentPixel >>> 8) & COLOR_BIT_MASK);
							newBlue += filterMatrix[fRow][fCol] * (currentPixel & COLOR_BIT_MASK);
						}

					}
				}

				// calculate the color
				newTran = newTran * brightness / 100 / divisor;
				newRed = newRed * brightness / 100 / divisor;
				newGreen = newGreen * brightness / 100 / divisor;
				newBlue = newBlue * brightness / 100 / divisor;

				newTran = Math.max(0, Math.min(255, newTran));
				newRed = Math.max(0, Math.min(255, newRed));
				newGreen = Math.max(0, Math.min(255, newGreen));
				newBlue = Math.max(0, Math.min(255, newBlue));
				argbData[(row) * width + col] = (newTran << 24 | newRed << 16 | newGreen << 8 | newBlue);

			}

			// shift the buffer if we are not near the end
			if (row + fhRadius != height) {
				System.arraycopy(tmpRect, width, tmpRect, 0, width * (filterMatrix.length - 1)); // shift
				// it
				// back
				System.arraycopy(argbData, width * (row + fhRadius), tmpRect, width * (filterMatrix.length - 1), width); // add
				// new
				// data
			}
		}
		// return Image.createRGBImage(argbData, width, height, true);
	}

	/**
	 * Apply Gaussian blur Filter to the given image data
	 * <p>
	 * <table border="0">
	 * <tr>
	 * <td><b>Before</b></td>
	 * <td><b>After</b></td>
	 * </tr>
	 * <tr>
	 * <td>
	 * <img src="../../../resources/before.png"></td>
	 * <td><img src="../../../resources/gaussian.png"></td>
	 * </tr>
	 * </table>
	 * </p>
	 * 
	 * @param bitmap
	 * @param brightness
	 *            of the result. Optimum values are within 200
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 */
	public static final Bitmap doGaussianBlurFilter(Bitmap bitmap, int brightness, Bitmap.Config outputConfig) {
		byte[][] filter = { { 1, 2, 1 }, { 2, 4, 2 }, { 1, 2, 1 } };
		return applyFilter(bitmap, brightness, filter, outputConfig);
	}

	/**
	 * Generates only the border pixels of the given image
	 * 
	 * @param bitmap
	 * 
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 */
	public static Bitmap doImageRim(Bitmap bitmap, Bitmap.Config outputConfig) {
		byte[][] filter = { { 0, -1, 0 }, { -1, 5, -1 }, { 0, -1, 0 } };
		return applyFilter(bitmap, 0, filter, outputConfig);
	}

	/**
	 * Apply image blur filter on the given image data
	 * <p>
	 * <table border="0">
	 * 
	 * <tr>
	 * <td><b>Before</b></td>
	 * <td><b>After</b></td>
	 * </tr>
	 * <tr>
	 * <td>
	 * <img src="../../../resources/before.png"></td>
	 * <td><img src="../../../resources/blur.png"></td>
	 * </tr>
	 * </table>
	 * </p>
	 * 
	 * @param argbData
	 *            of the image
	 * 
	 * @param width
	 *            of the image
	 * @param height
	 *            of the image
	 */
	public static Bitmap doSimpleBlur(Bitmap bitmap, Bitmap.Config outputConfig) {
		byte[][] filter = { { -1, -1, -1 }, { -1, 0, -1 }, { -1, -1, -1 } };
		return applyFilter(bitmap, 100, filter, outputConfig);
	}

	/**
	 * Apply emboss filter to the given image data
	 * <p>
	 * <table border="0">
	 * <tr>
	 * <td><b>Before</b></td>
	 * <td><b>After</b></td>
	 * </tr>
	 * <tr>
	 * <td>
	 * <img src="../../../resources/before.png"></td>
	 * <td><img src="../../../resources/emboss.png"></td>
	 * </tr>
	 * </table>
	 * </p>
	 * 
	 * @param bitmap
	 *            of the image
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * @return
	 */
	public static Bitmap emboss(Bitmap bitmap, Bitmap.Config outputConfig) {

		byte[][] filter = { { -2, 0, 0 }, { 0, 1, 0 }, { 0, 0, 2 } };
		return applyFilter(bitmap, 100, filter, outputConfig);
	}


	/**
	 * Converts
	 * 
	 * @param src
	 * @param saturation
	 *            Grayscale saturation level
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * @return
	 */
	public static final Bitmap doGrayscaleFilter(Bitmap src, int saturation, Bitmap.Config outputConfig) {
		int[] rgbInput = BitmapUtils.getPixels(src);
		int[] rgbOutput = new int[src.getWidth() * src.getHeight()];

		int alpha, red, green, blue;
		int output_red, output_green, output_blue;

		// We will use the standard NTSC color quotiens, multiplied by 1024
		// in order to be able to use integer-only math throughout the code.
		int RW = 306; // 0.299 * 1024
		int RG = 601; // 0.587 * 1024
		int RB = 117; // 0.114 * 1024

		// Define and calculate matrix quotients
		final int a, b, c, d, e, f, g, h, i;
		a = (1024 - saturation) * RW + saturation * 1024;
		b = (1024 - saturation) * RW;
		c = (1024 - saturation) * RW;
		d = (1024 - saturation) * RG;
		e = (1024 - saturation) * RG + saturation * 1024;
		f = (1024 - saturation) * RG;
		g = (1024 - saturation) * RB;
		h = (1024 - saturation) * RB;
		i = (1024 - saturation) * RB + saturation * 1024;

		int pixel = 0;
		for (int p = 0; p < rgbOutput.length; p++) {
			pixel = rgbInput[p];
			alpha = (0xFF000000 & pixel);
			red = (0x00FF & (pixel >> 16));
			green = (0x0000FF & (pixel >> 8));
			blue = pixel & (0x000000FF);

			// Matrix multiplication
			output_red = ((a * red + d * green + g * blue) >> 4) & 0x00FF0000;
			output_green = ((b * red + e * green + h * blue) >> 12) & 0x0000FF00;
			output_blue = (c * red + f * green + i * blue) >> 20;

			rgbOutput[p] = alpha | output_red | output_green | output_blue;
		}
		return BitmapUtils.getBitmap(rgbOutput, src.getWidth(), src.getHeight(), outputConfig);
	}

}