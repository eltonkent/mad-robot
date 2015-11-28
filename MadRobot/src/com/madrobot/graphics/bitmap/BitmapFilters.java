package com.madrobot.graphics.bitmap;

import android.graphics.Bitmap;

/**
 * Commonly used bitmap filters
 * <p>
 * 
 * 
 * 
 * @author elton.stephen.kent
 * 
 */
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

	public static Bitmap applyFilter(Bitmap bitmap, int value, byte[][] filter,
			Bitmap.Config outputConfig) {
		int[] argbData = BitmapUtils.getPixels(bitmap);
		applyFilter(filter, value, argbData, bitmap.getWidth(), bitmap.getHeight());
		return Bitmap.createBitmap(argbData, bitmap.getWidth(), bitmap.getHeight(),
				outputConfig);

	}

	/**
	 * The weights specified are scaled so that the image average brightness
	 * should not change after the halftoning.
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
	public static final Bitmap deBlurHorizontalHalftone(Bitmap bitmap, int n0, int n1, int n2,
			int n3, int n4, Bitmap.Config outputConfig) {
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
	 * Performs a convolution of an image with a given matrix.
	 * 
	 * @param filterMatrix
	 *            a matrix, which should have odd rows an colums (not
	 *            neccessarily a square). The matrix is used for a 2-dimensional
	 *            convolution. Negative values are possible.
	 * 
	 * @param brightness
	 *            you can vary the brightness of the image measured in percent.
	 *            Note that the algorithm tries to keep the original brightness
	 *            as far as is possible.
	 * 
	 * @param argbData
	 *            the image (RGB+transparency)
	 * 
	 * @param width
	 *            of the given Image
	 * 
	 * @param height
	 *            of the given Image Be aware that the computation time depends
	 *            on the size of the matrix.
	 * @throws IllegalArgumentException
	 *             if the filter matrix length is an even number
	 */
	private final static void applyFilter(byte[][] filterMatrix, int brightness,
			int[] argbData, int width, int height) {
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
							newTran += filterMatrix[fRow][fCol]
									* ((currentPixel >>> 24) & COLOR_BIT_MASK);
							newRed += filterMatrix[fRow][fCol]
									* ((currentPixel >>> 16) & COLOR_BIT_MASK);
							newGreen += filterMatrix[fRow][fCol]
									* ((currentPixel >>> 8) & COLOR_BIT_MASK);
							newBlue += filterMatrix[fRow][fCol]
									* (currentPixel & COLOR_BIT_MASK);
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
				System.arraycopy(argbData, width * (row + fhRadius), tmpRect, width
						* (filterMatrix.length - 1), width); // add
				// new
				// data
			}
		}
		// return Image.createRGBImage(argbData, width, height, true);
	}

}
