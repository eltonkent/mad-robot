package com.madrobot.graphics.bitmap;

import android.graphics.Bitmap;
import android.graphics.PointF;

import com.madrobot.geom.AffineTransform;
import com.madrobot.graphics.ColorUtils;

/**
 * Collection of various blur filters
 * <p>
 * <b>Gaussian Blur</b><br/>
 * Gaussian using the <code>radius</code> of <code>2</code><br/>
 * <img src="../../../../resources/gaussian.png"><br/>
 * 
 * <b>Motion Blur</b><br/>
 * Motion blur with <code>angle</code> 1.0f and <code>distance</code> 5.0f.<br/>
 * <img src="../../../../resources/motionBlur.png"><br/>
 * <b>Box Blur</b><br/>
 * Box blur with <code>hRadius</code> and <code>vRadius</code> of 5.0f. with 1
 * <code>iteration</code><br/>
 * <img src="../../../../resources/boxblur.png"><br/>
 * <b>Maximum</b><br/>
 * <img src="../../../../resources/maximum.png"><br/>
 * <b>Minimum</b><br/>
 * <img src="../../../../resources/minimum.png"><br/>
 * <b>Median</b><br/>
 * <img src="../../../../resources/median.png"><br/>
 * </p>
 * 
 * @author elton.stephen.kent
 * 
 */
public class BlurFilters {

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
	public static final Bitmap fastGaussianBlur(Bitmap bitmap, int brightness,
			Bitmap.Config outputConfig) {
		byte[][] filter = { { 1, 2, 1 }, { 2, 4, 2 }, { 1, 2, 1 } };
		return BitmapFilters.applyFilter(bitmap, brightness, filter, outputConfig);
	}

	/**
	 * Apply gaussian filter
	 * 
	 * @param src
	 * @param radius
	 *            filter radius. min:0 max:100
	 * @param convolveAlpha
	 * @param premultiplyAlpha
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap gaussianBlur(Bitmap src, int radius, boolean convolveAlpha,
			boolean premultiplyAlpha, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		Kernel kernel = GaussianUtils.makeKernel(radius);
		if (radius > 0) {
			GaussianUtils.convolveAndTranspose(kernel, inPixels, outPixels, width, height,
					convolveAlpha, convolveAlpha && premultiplyAlpha, false,
					BitmapFilters.CLAMP_EDGES);
			GaussianUtils.convolveAndTranspose(kernel, outPixels, inPixels, height, width,
					convolveAlpha, false, convolveAlpha && premultiplyAlpha,
					BitmapFilters.CLAMP_EDGES);
		}
		return Bitmap.createBitmap(inPixels, src.getWidth(), src.getHeight(), outputConfig);
	}

	/**
	 * Produces motion blur the slow, but higher-quality way.
	 * 
	 * @param src
	 * @param angle
	 *            the angle of blur. min:0 max:360
	 * @param distance
	 *            the distance of blur. min:0 max:200
	 * @param rotation
	 *            the blur rotation. min:-180 max:180
	 * @param zoom
	 *            the blur zoom. min:0 max:100
	 * @param premultiplyAlpha
	 *            whether to premultiply the alpha channel.
	 * @param wrapEdges
	 *            Set whether to wrap at the image edges
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap motionBlur(Bitmap src, float angle, float distance, float rotation,
			float zoom, boolean premultiplyAlpha, boolean wrapEdges, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];

		int cx = width / 2;
		int cy = height / 2;
		int index = 0;

		float imageRadius = (float) Math.sqrt(cx * cx + cy * cy);
		float translateX = (float) (distance * Math.cos(angle));
		float translateY = (float) (distance * -Math.sin(angle));
		float maxDistance = distance + Math.abs(rotation * imageRadius) + zoom * imageRadius;
		int repetitions = (int) maxDistance;
		AffineTransform t = new AffineTransform();
		PointF p = new PointF();

		if (premultiplyAlpha)
			ImageMath.premultiply(inPixels, 0, inPixels.length);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int a = 0, r = 0, g = 0, b = 0;
				int count = 0;
				for (int i = 0; i < repetitions; i++) {
					int newX = x, newY = y;
					float f = (float) i / repetitions;

					p.x = x;
					p.y = y;
					t.setToIdentity();
					t.translate(cx + f * translateX, cy + f * translateY);
					float s = 1 - zoom * f;
					t.scale(s, s);
					if (rotation != 0)
						t.rotate(-rotation * f);
					t.translate(-cx, -cy);
					t.transform(p, p);
					newX = (int) p.x;
					newY = (int) p.y;

					if (newX < 0 || newX >= width) {
						if (wrapEdges)
							newX = ImageMath.mod(newX, width);
						else
							break;
					}
					if (newY < 0 || newY >= height) {
						if (wrapEdges)
							newY = ImageMath.mod(newY, height);
						else
							break;
					}

					count++;
					int rgb = inPixels[newY * width + newX];
					a += (rgb >> 24) & 0xff;
					r += (rgb >> 16) & 0xff;
					g += (rgb >> 8) & 0xff;
					b += rgb & 0xff;
				}
				if (count == 0) {
					outPixels[index] = inPixels[index];
				} else {
					a = ColorUtils.clamp((a / count));
					r = ColorUtils.clamp((r / count));
					g = ColorUtils.clamp((g / count));
					b = ColorUtils.clamp((b / count));
					outPixels[index] = (a << 24) | (r << 16) | (g << 8) | b;
				}
				index++;
			}
		}
		if (premultiplyAlpha)
			ImageMath.unpremultiply(outPixels, 0, inPixels.length);

		return Bitmap.createBitmap(outPixels, src.getWidth(), src.getHeight(), outputConfig);
	}

	/**
	 * Apply image blur filter on the given image data
	 * 
	 * @param argbData
	 *            of the image
	 * 
	 * @param bitmapWidth
	 *            of the image
	 * @param height
	 *            of the image
	 */
	public static Bitmap simpleBlur(Bitmap bitmap, Bitmap.Config outputConfig) {
		byte[][] filter = { { -1, -1, -1 }, { -1, 0, -1 }, { -1, -1, -1 } };
		return BitmapFilters.applyFilter(bitmap, 100, filter, outputConfig);
	}

	/**
	 * performs a box blur on an image.
	 * <p>
	 * The horizontal and vertical blurs can be specified separately and a
	 * number of iterations can be given which allows an approximation to
	 * Gaussian blur.
	 * </p>
	 * 
	 * @param bitmap
	 * @param hRadius
	 *            min:0 max:100
	 * @param vRadius
	 *            min:0 max:100
	 * @param iterations
	 *            the number of iterations the blur is performed. min:0 max:10.
	 *            Recommended:1.
	 * @param premultiplyAlpha
	 *            whether to premultiply the alpha channel. Recommended:true
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap boxBlur(Bitmap bitmap, float hRadius, float vRadius, int iterations,
			boolean premultiplyAlpha, Bitmap.Config outputConfig) {

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		int[] inPixels = BitmapUtils.getPixels(bitmap);
		int[] outPixels = new int[width * height];

		if (premultiplyAlpha)
			ImageMath.premultiply(inPixels, 0, inPixels.length);
		for (int i = 0; i < iterations; i++) {
			blur(inPixels, outPixels, width, height, hRadius);
			blur(outPixels, inPixels, height, width, vRadius);
		}
		blurFractional(inPixels, outPixels, width, height, hRadius);
		blurFractional(outPixels, inPixels, height, width, vRadius);
		if (premultiplyAlpha)
			ImageMath.unpremultiply(inPixels, 0, inPixels.length);

		return Bitmap.createBitmap(inPixels, width, height, outputConfig);
	}

	/**
	 * Blur and transpose a block of ARGB pixels.
	 * 
	 * @param in
	 *            the input pixels
	 * @param out
	 *            the output pixels
	 * @param width
	 *            the width of the pixel array
	 * @param height
	 *            the height of the pixel array
	 * @param radius
	 *            the radius of blur
	 */
	public static void blur(int[] in, int[] out, int width, int height, float radius) {
		int widthMinus1 = width - 1;
		int r = (int) radius;
		int tableSize = 2 * r + 1;
		int divide[] = new int[256 * tableSize];

		for (int i = 0; i < 256 * tableSize; i++)
			divide[i] = i / tableSize;

		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;
			int ta = 0, tr = 0, tg = 0, tb = 0;

			for (int i = -r; i <= r; i++) {
				int rgb = in[inIndex + ImageMath.clamp(i, 0, width - 1)];
				ta += (rgb >> 24) & 0xff;
				tr += (rgb >> 16) & 0xff;
				tg += (rgb >> 8) & 0xff;
				tb += rgb & 0xff;
			}

			for (int x = 0; x < width; x++) {
				out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8)
						| divide[tb];

				int i1 = x + r + 1;
				if (i1 > widthMinus1)
					i1 = widthMinus1;
				int i2 = x - r;
				if (i2 < 0)
					i2 = 0;
				int rgb1 = in[inIndex + i1];
				int rgb2 = in[inIndex + i2];

				ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
				tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
				tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
				tb += (rgb1 & 0xff) - (rgb2 & 0xff);
				outIndex += height;
			}
			inIndex += width;
		}
	}

	public static void blurFractional(int[] in, int[] out, int width, int height, float radius) {
		radius -= (int) radius;
		float f = 1.0f / (1 + 2 * radius);
		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;

			out[outIndex] = in[0];
			outIndex += height;
			for (int x = 1; x < width - 1; x++) {
				int i = inIndex + x;
				int rgb1 = in[i - 1];
				int rgb2 = in[i];
				int rgb3 = in[i + 1];

				int a1 = (rgb1 >> 24) & 0xff;
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;
				int a2 = (rgb2 >> 24) & 0xff;
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;
				int a3 = (rgb3 >> 24) & 0xff;
				int r3 = (rgb3 >> 16) & 0xff;
				int g3 = (rgb3 >> 8) & 0xff;
				int b3 = rgb3 & 0xff;
				a1 = a2 + (int) ((a1 + a3) * radius);
				r1 = r2 + (int) ((r1 + r3) * radius);
				g1 = g2 + (int) ((g1 + g3) * radius);
				b1 = b2 + (int) ((b1 + b3) * radius);
				a1 *= f;
				r1 *= f;
				g1 *= f;
				b1 *= f;
				out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
				outIndex += height;
			}
			out[outIndex] = in[width - 1];
			inIndex += width;
		}
	}

	/**
	 * A filter which replcaes each pixel by the maximum of itself and its eight
	 * neightbours.
	 * 
	 * @param src
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap maximum(Bitmap src, Bitmap.Config outputConfig) {
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		int width = src.getWidth();
		int height = src.getHeight();
		int index = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = 0xff000000;
				for (int dy = -1; dy <= 1; dy++) {
					int iy = y + dy;
					int ioffset;
					if (0 <= iy && iy < height) {
						ioffset = iy * width;
						for (int dx = -1; dx <= 1; dx++) {
							int ix = x + dx;
							if (0 <= ix && ix < width) {
								pixel = ColorUtils.combinePixels(pixel,
										inPixels[ioffset + ix], ColorUtils.MAX);
							}
						}
					}
				}
				outPixels[index++] = pixel;
			}
		}
		inPixels = null;
		return Bitmap.createBitmap(outPixels, width, height, outputConfig);
	}

	/**
	 * A filter which replcaes each pixel by the mimimum of itself and its eight
	 * neightbours.
	 * 
	 * @param src
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap minimum(Bitmap src, Bitmap.Config outputConfig) {
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		int width = src.getWidth();
		int height = src.getHeight();
		int index = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = 0xffffffff;
				for (int dy = -1; dy <= 1; dy++) {
					int iy = y + dy;
					int ioffset;
					if (0 <= iy && iy < height) {
						ioffset = iy * width;
						for (int dx = -1; dx <= 1; dx++) {
							int ix = x + dx;
							if (0 <= ix && ix < width) {
								pixel = ColorUtils.combinePixels(pixel,
										inPixels[ioffset + ix], ColorUtils.MIN);
							}
						}
					}
				}
				outPixels[index++] = pixel;
			}
		}
		inPixels = null;
		return Bitmap.createBitmap(outPixels, width, height, outputConfig);
	}

	/**
	 * A filter which performs a 3x3 median operation. Useful for removing dust
	 * and noise.
	 * 
	 * @param src
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap median(Bitmap src, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int index = 0;
		int[] argb = new int[9];
		int[] r = new int[9];
		int[] g = new int[9];
		int[] b = new int[9];
		int[] outPixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int k = 0;
				for (int dy = -1; dy <= 1; dy++) {
					int iy = y + dy;
					if (0 <= iy && iy < height) {
						int ioffset = iy * width;
						for (int dx = -1; dx <= 1; dx++) {
							int ix = x + dx;
							if (0 <= ix && ix < width) {
								int rgb = inPixels[ioffset + ix];
								argb[k] = rgb;
								r[k] = (rgb >> 16) & 0xff;
								g[k] = (rgb >> 8) & 0xff;
								b[k] = rgb & 0xff;
								k++;
							}
						}
					}
				}
				while (k < 9) {
					argb[k] = 0xff000000;
					r[k] = g[k] = b[k] = 0;
					k++;
				}
				outPixels[index++] = argb[rgbMedian(r, g, b)];
			}
		}
		return Bitmap.createBitmap(outPixels, width, height, outputConfig);
	}

	private static int rgbMedian(int[] r, int[] g, int[] b) {
		int sum, index = 0, min = Integer.MAX_VALUE;

		for (int i = 0; i < 9; i++) {
			sum = 0;
			for (int j = 0; j < 9; j++) {
				sum += Math.abs(r[i] - r[j]);
				sum += Math.abs(g[i] - g[j]);
				sum += Math.abs(b[i] - b[j]);
			}
			if (sum < min) {
				min = sum;
				index = i;
			}
		}
		return index;
	}
}
