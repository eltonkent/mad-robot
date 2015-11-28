package com.madrobot.graphics.bitmap;

import android.graphics.Bitmap;

import com.madrobot.graphics.ColorUtils;

class ConvolveUtils {
	static Bitmap doConvolve(float[] matrix, Bitmap src, int edgeAction, boolean processAlpha,
			boolean premultiplyAlpha, Bitmap.Config outputConfig) {
		Kernel kernel = new Kernel(3, 3, matrix);
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		if (premultiplyAlpha)
			ImageMath.premultiply(inPixels, 0, inPixels.length);
		convolve(kernel, inPixels, outPixels, width, height, processAlpha, edgeAction);
		if (premultiplyAlpha)
			ImageMath.unpremultiply(outPixels, 0, outPixels.length);

		return Bitmap.createBitmap(outPixels, src.getWidth(), src.getHeight(), outputConfig);
	}

	/**
	 * Convolve a block of pixels.
	 * 
	 * @param kernel
	 *            the kernel
	 * @param inPixels
	 *            the input pixels
	 * @param outPixels
	 *            the output pixels
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param alpha
	 *            include alpha channel
	 * @param edgeAction
	 *            what to do at the edges
	 */
	static void convolve(Kernel kernel, int[] inPixels, int[] outPixels, int width,
			int height, boolean alpha, int edgeAction) {
		if (kernel.getHeight() == 1)
			convolveH(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
		else if (kernel.getWidth() == 1)
			convolveV(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
		else
			convolveHV(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
	}

	/**
	 * Convolve with a 2D kernel.
	 * 
	 * @param kernel
	 *            the kernel
	 * @param inPixels
	 *            the input pixels
	 * @param outPixels
	 *            the output pixels
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param alpha
	 *            include alpha channel
	 * @param edgeAction
	 *            what to do at the edges
	 */
	static void convolveHV(Kernel kernel, int[] inPixels, int[] outPixels, int width,
			int height, boolean alpha, int edgeAction) {
		int index = 0;
		float[] matrix = kernel.getKernelData(null);
		int rows = kernel.getHeight();
		int cols = kernel.getWidth();
		int rows2 = rows / 2;
		int cols2 = cols / 2;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float r = 0, g = 0, b = 0, a = 0;

				for (int row = -rows2; row <= rows2; row++) {
					int iy = y + row;
					int ioffset;
					if (0 <= iy && iy < height)
						ioffset = iy * width;
					else if (edgeAction == BitmapFilters.CLAMP_EDGES)
						ioffset = y * width;
					else if (edgeAction == BitmapFilters.WRAP_EDGES)
						ioffset = ((iy + height) % height) * width;
					else
						continue;
					int moffset = cols * (row + rows2) + cols2;
					for (int col = -cols2; col <= cols2; col++) {
						float f = matrix[moffset + col];

						if (f != 0) {
							int ix = x + col;
							if (!(0 <= ix && ix < width)) {
								if (edgeAction == BitmapFilters.CLAMP_EDGES)
									ix = x;
								else if (edgeAction == BitmapFilters.WRAP_EDGES)
									ix = (x + width) % width;
								else
									continue;
							}
							int rgb = inPixels[ioffset + ix];
							a += f * ((rgb >> 24) & 0xff);
							r += f * ((rgb >> 16) & 0xff);
							g += f * ((rgb >> 8) & 0xff);
							b += f * (rgb & 0xff);
						}
					}
				}
				int ia = alpha ? ColorUtils.clamp((int) (a + 0.5)) : 0xff;
				int ir = ColorUtils.clamp((int) (r + 0.5));
				int ig = ColorUtils.clamp((int) (g + 0.5));
				int ib = ColorUtils.clamp((int) (b + 0.5));
				outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
			}
		}
	}

	/**
	 * Convolve with a kernel consisting of one row.
	 * 
	 * @param kernel
	 *            the kernel
	 * @param inPixels
	 *            the input pixels
	 * @param outPixels
	 *            the output pixels
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param alpha
	 *            include alpha channel
	 * @param edgeAction
	 *            what to do at the edges
	 */
	static void convolveH(Kernel kernel, int[] inPixels, int[] outPixels, int width,
			int height, boolean alpha, int edgeAction) {
		int index = 0;
		float[] matrix = kernel.getKernelData(null);
		int cols = kernel.getWidth();
		int cols2 = cols / 2;

		for (int y = 0; y < height; y++) {
			int ioffset = y * width;
			for (int x = 0; x < width; x++) {
				float r = 0, g = 0, b = 0, a = 0;
				int moffset = cols2;
				for (int col = -cols2; col <= cols2; col++) {
					float f = matrix[moffset + col];

					if (f != 0) {
						int ix = x + col;
						if (ix < 0) {
							if (edgeAction == BitmapFilters.CLAMP_EDGES)
								ix = 0;
							else if (edgeAction == BitmapFilters.WRAP_EDGES)
								ix = (x + width) % width;
						} else if (ix >= width) {
							if (edgeAction == BitmapFilters.CLAMP_EDGES)
								ix = width - 1;
							else if (edgeAction == BitmapFilters.WRAP_EDGES)
								ix = (x + width) % width;
						}
						int rgb = inPixels[ioffset + ix];
						a += f * ((rgb >> 24) & 0xff);
						r += f * ((rgb >> 16) & 0xff);
						g += f * ((rgb >> 8) & 0xff);
						b += f * (rgb & 0xff);
					}
				}
				int ia = alpha ? ColorUtils.clamp((int) (a + 0.5)) : 0xff;
				int ir = ColorUtils.clamp((int) (r + 0.5));
				int ig = ColorUtils.clamp((int) (g + 0.5));
				int ib = ColorUtils.clamp((int) (b + 0.5));
				outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
			}
		}
	}

	/**
	 * Convolve with a kernel consisting of one column.
	 * 
	 * @param kernel
	 *            the kernel
	 * @param inPixels
	 *            the input pixels
	 * @param outPixels
	 *            the output pixels
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param alpha
	 *            include alpha channel
	 * @param edgeAction
	 *            what to do at the edges
	 */
	static void convolveV(Kernel kernel, int[] inPixels, int[] outPixels, int width,
			int height, boolean alpha, int edgeAction) {
		int index = 0;
		float[] matrix = kernel.getKernelData(null);
		int rows = kernel.getHeight();
		int rows2 = rows / 2;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float r = 0, g = 0, b = 0, a = 0;

				for (int row = -rows2; row <= rows2; row++) {
					int iy = y + row;
					int ioffset;
					if (iy < 0) {
						if (edgeAction == BitmapFilters.CLAMP_EDGES)
							ioffset = 0;
						else if (edgeAction == BitmapFilters.WRAP_EDGES)
							ioffset = ((y + height) % height) * width;
						else
							ioffset = iy * width;
					} else if (iy >= height) {
						if (edgeAction == BitmapFilters.CLAMP_EDGES)
							ioffset = (height - 1) * width;
						else if (edgeAction == BitmapFilters.WRAP_EDGES)
							ioffset = ((y + height) % height) * width;
						else
							ioffset = iy * width;
					} else
						ioffset = iy * width;

					float f = matrix[row + rows2];

					if (f != 0) {
						int rgb = inPixels[ioffset + x];
						a += f * ((rgb >> 24) & 0xff);
						r += f * ((rgb >> 16) & 0xff);
						g += f * ((rgb >> 8) & 0xff);
						b += f * (rgb & 0xff);
					}
				}
				int ia = alpha ? ColorUtils.clamp((int) (a + 0.5)) : 0xff;
				int ir = ColorUtils.clamp((int) (r + 0.5));
				int ig = ColorUtils.clamp((int) (g + 0.5));
				int ib = ColorUtils.clamp((int) (b + 0.5));
				outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
			}
		}
	}
}
