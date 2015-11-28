package com.madrobot.graphics.bitmap;

import com.madrobot.graphics.ColorUtils;

/**
 * Gaussian theory utils
 * 
 * @author elton.stephen.kent
 * 
 */
class GaussianUtils {

	/**
	 * Blur and transpose a block of ARGB pixels.
	 * 
	 * @param kernel
	 *            the blur kernel
	 * @param inPixels
	 *            the input pixels
	 * @param outPixels
	 *            the output pixels
	 * @param width
	 *            the width of the pixel array
	 * @param height
	 *            the height of the pixel array
	 * @param alpha
	 *            whether to blur the alpha channel
	 * @param edgeAction
	 *            what to do at the edges
	 */
	static void convolveAndTranspose(Kernel kernel, int[] inPixels, int[] outPixels,
			int width, int height, boolean alpha, boolean premultiply, boolean unpremultiply,
			int edgeAction) {
		float[] matrix = kernel.getKernelData(null);
		int cols = kernel.getWidth();
		int cols2 = cols / 2;

		for (int y = 0; y < height; y++) {
			int index = y;
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
						int pa = (rgb >> 24) & 0xff;
						int pr = (rgb >> 16) & 0xff;
						int pg = (rgb >> 8) & 0xff;
						int pb = rgb & 0xff;
						if (premultiply) {
							float a255 = pa * (1.0f / 255.0f);
							pr *= a255;
							pg *= a255;
							pb *= a255;
						}
						a += f * pa;
						r += f * pr;
						g += f * pg;
						b += f * pb;
					}
				}
				if (unpremultiply && a != 0 && a != 255) {
					float f = 255.0f / a;
					r *= f;
					g *= f;
					b *= f;
				}
				int ia = alpha ? ColorUtils.clamp((int) (a + 0.5)) : 0xff;
				int ir = ColorUtils.clamp((int) (r + 0.5));
				int ig = ColorUtils.clamp((int) (g + 0.5));
				int ib = ColorUtils.clamp((int) (b + 0.5));
				outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
				index += height;
			}
		}
	}

	/**
	 * Make a Gaussian blur kernel.
	 * 
	 * @param radius
	 *            the blur radius
	 * @return the kernel
	 */
	static Kernel makeKernel(float radius) {
		int r = (int) Math.ceil(radius);
		int rows = r * 2 + 1;
		float[] matrix = new float[rows];
		float sigma = radius / 3;
		float sigma22 = 2 * sigma * sigma;
		float sigmaPi2 = 2 * ImageMath.PI * sigma;
		float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
		float radius2 = radius * radius;
		float total = 0;
		int index = 0;
		for (int row = -r; row <= r; row++) {
			float distance = row * row;
			if (distance > radius2)
				matrix[index] = 0;
			else
				matrix[index] = (float) Math.exp(-(distance) / sigma22) / sqrtSigmaPi2;
			total += matrix[index];
			index++;
		}
		for (int i = 0; i < rows; i++)
			matrix[i] /= total;

		return new Kernel(rows, 1, matrix);
	}

}
