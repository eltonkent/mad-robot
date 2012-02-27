package com.madrobot.graphics.bitmap;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.madrobot.geom.Rectangle;
import com.madrobot.graphics.PixelUtils;

/**
 * Bitmap texture filters
 * <p>
 * * <b>Invert</b><br/>
 * <img src="../../../../resources/invert.png" width="300" height="224"><br/>
 * <b>Sepia</b><br/>
 * Sepia with the <code>depth</code> of 10. </br> <img src="../../../../resources/sepia.png" width="300" height="224"><br/>
 * 
 * <b>Quantize</b><br/>
 * <table>
 * <tr>
 * <th>With 128 Colors</th>
 * <th>With 256 Colors</th>
 * </tr>
 * <tr>
 * <td><img src="../../../../resources/quantize128.png" width="300" height="224"></td>
 * <td><img src="../../../../resources/quantize256.png" width="300" height="224"></td>
 * </tr>
 * </table>
 * <br/>
 * <b>Glow</b><br/>
 * Glow using the <code>glowAmount</code> of <code>0.5</code> and <code>glowRadius</code> of <code>2</code><br/>
 * <img src="../../../../resources/glow.png"><br/>
 * 
 * <b>Oil Paint</b><br/>
 * <img src="../../../../resources/wholeimage_oilpaint.png" width="300" height="224"><br/>
 * </p>
 */
public class ColorFilters {

	private static class BitmapMeta {
		int x;
		int y;
		int bitmapWidth;
		int bitmapHeight;
		/**
		 * the target area width affected by the filter
		 */
		int targetWidth;
		/**
		 * the target area height  affected by the filter
		 */
		int targetHeight;

	}

	/**
	 * res[0]= bitmap width<br/>
	 * res[1]=bitmap height<br/>
	 * res[2]=target width where the filter acts res[3]=target height where the filter acts
	 * 
	 * @param bitmap
	 * @param outputConfig
	 * @return
	 */
	private static BitmapMeta getMeta(Bitmap bitmap, OutputConfiguration outputConfig) {

		BitmapMeta meta = new BitmapMeta();
		meta.bitmapWidth = bitmap.getWidth(); // res[0]
		meta.bitmapHeight = bitmap.getHeight();// res[1]

		if (outputConfig.getAffectedArea() != null) {
			outputConfig.checkRectangleBounds(meta.bitmapWidth, meta.bitmapHeight);
		}

		meta.targetWidth = meta.bitmapWidth;// res[2]
		meta.targetHeight = meta.bitmapHeight;// res[3]

		if (outputConfig.affectedArea != null) {
			meta.targetWidth = outputConfig.affectedArea.x + outputConfig.affectedArea.width;
			if (meta.targetWidth > meta.bitmapWidth) {
				meta.targetWidth = meta.bitmapWidth;
			}
			meta.targetHeight = outputConfig.affectedArea.y + outputConfig.affectedArea.height;
			if (meta.targetHeight > meta.bitmapHeight) {
				meta.targetHeight = meta.bitmapHeight;
			}
		}

		meta.x = outputConfig.affectedArea != null ? outputConfig.affectedArea.x : 0;// res[4]
		meta.y = outputConfig.affectedArea != null ? outputConfig.affectedArea.y : 0;// res[5]
		return meta;
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
	public static final Bitmap posterize(Bitmap bitmap, int depth, OutputConfiguration outputConfig) {
		int[] argb = BitmapUtils.getPixels(bitmap);

		BitmapMeta meta = getMeta(bitmap, outputConfig);
		for (int ver = meta.y; ver < meta.targetHeight; ver++) {
			for (int hr = meta.x; hr < meta.targetWidth; hr++) {
				int position = (ver * meta.bitmapWidth) + hr;
				argb[position] = PixelUtils.posterizePixel(argb[position], depth);
			}
		}
		if (outputConfig.canRecycleSrc) {
			bitmap.recycle();
		}
		return Bitmap.createBitmap(argb, meta.bitmapWidth, meta.bitmapHeight, outputConfig.config);
	}

	/**
	 * Invert the bitmap's colors.
	 * 
	 * @param bitmap
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * @return
	 */
	public static final Bitmap invert(Bitmap bitmap, OutputConfiguration outputConfig) {

		int[] argb = BitmapUtils.getPixels(bitmap);

		BitmapMeta meta = getMeta(bitmap, outputConfig);
		for (int ver = meta.y; ver < meta.targetHeight; ver++) {
			for (int hr = meta.x; hr < meta.targetWidth; hr++) {

				int position = (ver * meta.bitmapWidth) + hr;
				argb[position] = PixelUtils.invertColor(argb[position]);
			}
		}
		if (outputConfig.canRecycleSrc) {
			bitmap.recycle();
		}
		return Bitmap.createBitmap(argb, meta.bitmapWidth, meta.bitmapHeight, outputConfig.config);
	}

	/**
	 * Apply sepia (brown) filters
	 * 
	 * @param bitmap
	 * @param depth
	 *            Sepia depth. values between 1-100 provide an optimal output.
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * @return
	 */
	public static final Bitmap applySepia(Bitmap bitmap, Integer depth, OutputConfiguration outputConfig) {
		int[] argb = BitmapUtils.getPixels(bitmap);

		BitmapMeta meta = getMeta(bitmap, outputConfig);
		for (int ver = meta.y; ver < meta.targetHeight; ver++) {
			for (int hr = meta.x; hr < meta.targetWidth; hr++) {

				int position = (ver * meta.bitmapWidth) + hr;
				argb[position] = PixelUtils.applySepia(argb[position], depth);
			}
		}
		if (outputConfig.canRecycleSrc) {
			bitmap.recycle();
		}
		return Bitmap.createBitmap(argb, meta.bitmapWidth, meta.bitmapHeight, outputConfig.config);
	}

	/**
	 * Saturate the given bitmap
	 * 
	 * @param bitmap
	 * @param percent
	 * @param outputConfig
	 * @return
	 */
	public static final Bitmap saturate(Bitmap bitmap, int percent, OutputConfiguration outputConfig) {
		int[] argb = BitmapUtils.getPixels(bitmap);

		BitmapMeta meta = getMeta(bitmap, outputConfig);
		for (int ver = meta.y; ver < meta.targetHeight; ver++) {
			for (int hr = meta.x; hr < meta.targetWidth; hr++) {

				int position = (ver * meta.bitmapWidth) + hr;
				argb[position] = PixelUtils.setSaturation(argb[position], percent);
			}
		}
		if (outputConfig.canRecycleSrc) {
			bitmap.recycle();
		}
		return Bitmap.createBitmap(argb, meta.bitmapWidth, meta.bitmapHeight, outputConfig.config);
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
		return BitmapFilters.applyFilter(bitmap, 100, filter, outputConfig);
	}

	/**
	 * A filter which quantizes an image to a set number of colors
	 * <p>
	 * useful for producing images which are to be encoded using an index color model. The filter can perform
	 * Floyd-Steinberg error-diffusion dithering if required. At present, the quantization is done using an octtree
	 * algorithm but I eventually hope to add more quantization methods such as median cut. Note: at present, the filter
	 * produces an image which uses the RGB color model (because the application it was written for required it).
	 * </p>
	 * 
	 * @param src
	 * @param numColors
	 *            the number of colors to quantize to. Usually:256
	 * @param dither
	 *            Set whether to use dithering or not. If not, the image is posterized.
	 * @param serpentine
	 *            Set whether to use a serpentine pattern for return or not. This can reduce 'avalanche' artifacts in
	 *            the output.
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap quantize(Bitmap src, int numColors, boolean dither, boolean serpentine, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		int sum = 3 + 5 + 7 + 1;
		/**
		 * Floyd-Steinberg dithering matrix.
		 */
		int[] matrix = { 0, 0, 0, 0, 0, 7, 3, 5, 1, };

		int count = width * height;
		OctTreeQuantizer quantizer = new OctTreeQuantizer();
		quantizer.setup(numColors);
		quantizer.addPixels(inPixels, 0, count);
		int[] table = quantizer.buildColorTable();

		if (!dither) {
			for (int i = 0; i < count; i++)
				outPixels[i] = table[quantizer.getIndexForColor(inPixels[i])];
		} else {
			int index = 0;
			for (int y = 0; y < height; y++) {
				boolean reverse = serpentine && (y & 1) == 1;
				int direction;
				if (reverse) {
					index = y * width + width - 1;
					direction = -1;
				} else {
					index = y * width;
					direction = 1;
				}
				for (int x = 0; x < width; x++) {
					int rgb1 = inPixels[index];
					int rgb2 = table[quantizer.getIndexForColor(rgb1)];

					outPixels[index] = rgb2;

					int r1 = (rgb1 >> 16) & 0xff;
					int g1 = (rgb1 >> 8) & 0xff;
					int b1 = rgb1 & 0xff;

					int r2 = (rgb2 >> 16) & 0xff;
					int g2 = (rgb2 >> 8) & 0xff;
					int b2 = rgb2 & 0xff;

					int er = r1 - r2;
					int eg = g1 - g2;
					int eb = b1 - b2;

					for (int i = -1; i <= 1; i++) {
						int iy = i + y;
						if (0 <= iy && iy < height) {
							for (int j = -1; j <= 1; j++) {
								int jx = j + x;
								if (0 <= jx && jx < width) {
									int w;
									if (reverse)
										w = matrix[(i + 1) * 3 - j + 1];
									else
										w = matrix[(i + 1) * 3 + j + 1];
									if (w != 0) {
										int k = reverse ? index - j : index + j;
										rgb1 = inPixels[k];
										r1 = (rgb1 >> 16) & 0xff;
										g1 = (rgb1 >> 8) & 0xff;
										b1 = rgb1 & 0xff;
										r1 += er * w / sum;
										g1 += eg * w / sum;
										b1 += eb * w / sum;
										inPixels[k] = (PixelUtils.clamp(r1) << 16) | (PixelUtils.clamp(g1) << 8)
												| PixelUtils.clamp(b1);
									}
								}
							}
						}
					}
					index += direction;
				}
			}
		}
		return Bitmap.createBitmap(outPixels, src.getWidth(), src.getHeight(), outputConfig);
	}

	/**
	 * A filter which adds Gaussian blur to Bitmap, producing a glowing effect.
	 * 
	 * @param src
	 *            Source Bitmap
	 * @param glowAmount
	 *            Amount of glow. Should be from 0 to 1.Recommended:0.5f
	 * @param blurRadius
	 *            recommended :2
	 * @param processAlpha
	 *            process alpha for this image, recommended:true
	 * @param premultiplyAlpha
	 *            premulitply alpha. recommended:true
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap applyGlow(Bitmap src, float glowAmount, float blurRadius, boolean processAlpha, boolean premultiplyAlpha, Bitmap.Config outputConfig) {

		int width = src.getWidth();
		int height = src.getHeight();
		int[] argb = BitmapUtils.getPixels(src);
		int[] dest = new int[argb.length];
		Kernel kernel = GaussianUtils.makeKernel(blurRadius);
		if (blurRadius > 0) {
			GaussianUtils.convolveAndTranspose(kernel, argb, dest, width, height, processAlpha, processAlpha
					&& premultiplyAlpha, false, BitmapFilters.CLAMP_EDGES);
			GaussianUtils.convolveAndTranspose(kernel, dest, argb, height, width, processAlpha, false, processAlpha
					&& premultiplyAlpha, BitmapFilters.CLAMP_EDGES);
		}
		dest = BitmapUtils.getPixels(src);
		float a = 4 * glowAmount;
		int index = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb1 = dest[index];
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;

				int rgb2 = argb[index];
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;

				r1 = PixelUtils.clamp((int) (r1 + a * r2));
				g1 = PixelUtils.clamp((int) (g1 + a * g2));
				b1 = PixelUtils.clamp((int) (b1 + a * b2));

				argb[index] = (rgb1 & 0xff000000) | (r1 << 16) | (g1 << 8) | b1;
				index++;
			}
		}

		return Bitmap.createBitmap(argb, src.getWidth(), src.getHeight(), outputConfig);
	}

	/**
	 * Gaussian high pass filter.
	 * 
	 * @param src
	 * @param radius
	 *            recommended: 10
	 * @param processAlpha
	 * @param premultiplyAlpha
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap applyHighPass(Bitmap src, float radius, boolean processAlpha, boolean premultiplyAlpha, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		Kernel kernel = GaussianUtils.makeKernel(radius);
		if (radius > 0) {
			GaussianUtils.convolveAndTranspose(kernel, inPixels, outPixels, width, height, processAlpha, processAlpha
					&& premultiplyAlpha, false, BitmapFilters.CLAMP_EDGES);
			GaussianUtils.convolveAndTranspose(kernel, outPixels, inPixels, height, width, processAlpha, false,
					processAlpha && premultiplyAlpha, BitmapFilters.CLAMP_EDGES);
		}
		outPixels = BitmapUtils.getPixels(src);

		int index = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb1 = outPixels[index];
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;

				int rgb2 = inPixels[index];
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;

				r1 = (r1 + 255 - r2) / 2;
				g1 = (g1 + 255 - g2) / 2;
				b1 = (b1 + 255 - b2) / 2;

				inPixels[index] = (rgb1 & 0xff000000) | (r1 << 16) | (g1 << 8) | b1;
				index++;
			}
		}
		return Bitmap.createBitmap(inPixels, src.getWidth(), src.getHeight(), outputConfig);
	}

	private static int randomRGB(int[] inPixels, int x, int y, boolean useImageColors, Rectangle originalSpace, Random randomGenerator) {
		if (useImageColors) {
			return inPixels[y * originalSpace.width + x];
		} else {
			int r = (int) (255 * randomGenerator.nextFloat());
			int g = (int) (255 * randomGenerator.nextFloat());
			int b = (int) (255 * randomGenerator.nextFloat());
			return 0xff000000 | (r << 16) | (g << 8) | b;
		}
	}

	private static boolean doPlasmaPixel(int x1, int y1, int x2, int y2, int[] pixels, int stride, int depth, int scale, float turbulence) {
		int mx, my;

		if (depth == 0) {
			int ml, mr, mt, mb, mm, t;

			int tl = PixelUtils.getPixel(x1, y1, pixels, stride);
			int bl = PixelUtils.getPixel(x1, y2, pixels, stride);
			int tr = PixelUtils.getPixel(x2, y1, pixels, stride);
			int br = PixelUtils.getPixel(x2, y2, pixels, stride);

			float amount = (256.0f / (2.0f * scale)) * turbulence;

			mx = (x1 + x2) / 2;
			my = (y1 + y2) / 2;

			if (mx == x1 && mx == x2 && my == y1 && my == y2)
				return true;

			if (mx != x1 || mx != x2) {
				ml = PixelUtils.average(tl, bl);
				ml = PixelUtils.displace(ml, amount);
				PixelUtils.putPixel(x1, my, ml, pixels, stride);

				if (x1 != x2) {
					mr = PixelUtils.average(tr, br);
					mr = PixelUtils.displace(mr, amount);
					PixelUtils.putPixel(x2, my, mr, pixels, stride);
				}
			}

			if (my != y1 || my != y2) {
				if (x1 != mx || my != y2) {
					mb = PixelUtils.average(bl, br);
					mb = PixelUtils.displace(mb, amount);
					PixelUtils.putPixel(mx, y2, mb, pixels, stride);
				}

				if (y1 != y2) {
					mt = PixelUtils.average(tl, tr);
					mt = PixelUtils.displace(mt, amount);
					PixelUtils.putPixel(mx, y1, mt, pixels, stride);
				}
			}

			if (y1 != y2 || x1 != x2) {
				mm = PixelUtils.average(tl, br);
				t = PixelUtils.average(bl, tr);
				mm = PixelUtils.average(mm, t);
				mm = PixelUtils.displace(mm, amount);
				PixelUtils.putPixel(mx, my, mm, pixels, stride);
			}

			if (x2 - x1 < 3 && y2 - y1 < 3)
				return false;
			return true;
		}

		mx = (x1 + x2) / 2;
		my = (y1 + y2) / 2;

		doPlasmaPixel(x1, y1, mx, my, pixels, stride, depth - 1, scale + 1, turbulence);
		doPlasmaPixel(x1, my, mx, y2, pixels, stride, depth - 1, scale + 1, turbulence);
		doPlasmaPixel(mx, y1, x2, my, pixels, stride, depth - 1, scale + 1, turbulence);
		return doPlasmaPixel(mx, my, x2, y2, pixels, stride, depth - 1, scale + 1, turbulence);
	}

	/**
	 * 
	 * @param src
	 * @param turbulence
	 *            Specifies the turbulence of the texture. Min Value: 0,Max value: 10. recommended:1.0
	 * @param useImageColors
	 *            recommended: false.
	 * @param useColormap
	 *            Use color map for this filter. recommended:false.
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap plasma(Bitmap src, float turbulence, boolean useImageColors, boolean useColormap, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		Random randomGenerator = new Random();
		randomGenerator.setSeed(System.currentTimeMillis());
		Colormap colormap = new LinearColormap();
		Rectangle originalSpace = new Rectangle(0, 0, width, height);
		int w1 = width - 1;
		int h1 = height - 1;
		PixelUtils.putPixel(0, 0, randomRGB(inPixels, 0, 0, useImageColors, originalSpace, randomGenerator), outPixels,
				width);
		PixelUtils.putPixel(w1, 0, randomRGB(inPixels, w1, 0, useImageColors, originalSpace, randomGenerator),
				outPixels, width);
		PixelUtils.putPixel(0, h1, randomRGB(inPixels, 0, h1, useImageColors, originalSpace, randomGenerator),
				outPixels, width);
		PixelUtils.putPixel(w1, h1, randomRGB(inPixels, w1, h1, useImageColors, originalSpace, randomGenerator),
				outPixels, width);
		PixelUtils.putPixel(w1 / 2, h1 / 2,
				randomRGB(inPixels, w1 / 2, h1 / 2, useImageColors, originalSpace, randomGenerator), outPixels, width);
		PixelUtils.putPixel(0, h1 / 2, randomRGB(inPixels, 0, h1 / 2, useImageColors, originalSpace, randomGenerator),
				outPixels, width);
		PixelUtils.putPixel(w1, h1 / 2,
				randomRGB(inPixels, w1, h1 / 2, useImageColors, originalSpace, randomGenerator), outPixels, width);
		PixelUtils.putPixel(w1 / 2, 0, randomRGB(inPixels, w1 / 2, 0, useImageColors, originalSpace, randomGenerator),
				outPixels, width);
		PixelUtils.putPixel(w1 / 2, h1,
				randomRGB(inPixels, w1 / 2, h1, useImageColors, originalSpace, randomGenerator), outPixels, width);
		int depth = 1;
		while (doPlasmaPixel(0, 0, width - 1, height - 1, outPixels, width, depth, 0, turbulence))
			depth++;

		if (useColormap && colormap != null) {
			int index = 0;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					outPixels[index] = colormap.getColor((outPixels[index] & 0xff) / 255.0f);
					index++;
				}
			}
		}
		return Bitmap.createBitmap(outPixels, src.getWidth(), src.getHeight(), outputConfig);
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
	 * Converts
	 * 
	 * @param src
	 * @param saturation
	 *            Grayscale saturation level
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * @return
	 */
	public static final Bitmap grayScale(Bitmap src, int saturation, OutputConfiguration outputConfig) {
		int[] rgbInput = BitmapUtils.getPixels(src);
		// int[] rgbOutput = new int[src.getWidth() * src.getHeight()];

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
		int position;

		BitmapMeta meta = getMeta(src, outputConfig);
		for (int ver = meta.y; ver < meta.targetHeight; ver++) {
			for (int hr = meta.x; hr < meta.targetWidth; hr++) {

				position = (ver * meta.bitmapWidth) + hr;
				pixel = rgbInput[position];
				alpha = (0xFF000000 & pixel);
				red = (0x00FF & (pixel >> 16));
				green = (0x0000FF & (pixel >> 8));
				blue = pixel & (0x000000FF);

				// Matrix multiplication
				output_red = ((a * red + d * green + g * blue) >> 4) & 0x00FF0000;
				output_green = ((b * red + e * green + h * blue) >> 12) & 0x0000FF00;
				output_blue = (c * red + f * green + i * blue) >> 20;

				rgbInput[position] = alpha | output_red | output_green | output_blue;
			}
		}

		if (outputConfig.canRecycleSrc) {
			src.recycle();
		}
		return Bitmap.createBitmap(rgbInput, meta.bitmapWidth, meta.bitmapHeight, outputConfig.config);
	}

	/**
	 * Decrease the color depth of the given bitmap
	 * 
	 * @param pixel
	 * @param bitOffset
	 * @return
	 */
	public static Bitmap decreaseColorDepth(final Bitmap bitmap, final int bitOffset, OutputConfiguration outputConfig) {
		int[] argb = BitmapUtils.getPixels(bitmap);
		int A, R, G, B;
		BitmapMeta meta = getMeta(bitmap, outputConfig);
		for (int ver = meta.y; ver < meta.targetHeight; ver++) {
			for (int hr = meta.x; hr < meta.targetWidth; hr++) {

				int position = (ver * meta.bitmapWidth) + hr;
				// argb[position] = PixelUtils.setSaturation(argb[position], percent);
				A = Color.alpha(argb[position]);
				R = Color.red(argb[position]);
				G = Color.green(argb[position]);
				B = Color.blue(argb[position]);
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

				argb[position] = Color.argb(A, R, G, B);
			}
		}
		if (outputConfig.canRecycleSrc) {
			bitmap.recycle();
		}
		return Bitmap.createBitmap(argb, meta.bitmapWidth, meta.bitmapHeight, outputConfig.config);
	}

	/**
	 * A filter which produces a "oil-painting" effect.
	 * <p>
	 * Extremely CPU intensive. Run on dedicated thread.<br/>
	 * 
	 * </p>
	 * 
	 * @param src
	 * @param range
	 *            Range of effect in pixels. Recommended:3.
	 * @param levels
	 *            Set the number of levels for the effect. Recommended:256
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap oilPaint(Bitmap src, int range, int levels, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		int index = 0;
		int[] rHistogram = new int[levels];
		int[] gHistogram = new int[levels];
		int[] bHistogram = new int[levels];
		int[] rTotal = new int[levels];
		int[] gTotal = new int[levels];
		int[] bTotal = new int[levels];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (int i = 0; i < levels; i++)
					rHistogram[i] = gHistogram[i] = bHistogram[i] = rTotal[i] = gTotal[i] = bTotal[i] = 0;

				for (int row = -range; row <= range; row++) {
					int iy = y + row;
					int ioffset;
					if (0 <= iy && iy < height) {
						ioffset = iy * width;
						for (int col = -range; col <= range; col++) {
							int ix = x + col;
							if (0 <= ix && ix < width) {
								int rgb = inPixels[ioffset + ix];
								int r = (rgb >> 16) & 0xff;
								int g = (rgb >> 8) & 0xff;
								int b = rgb & 0xff;
								int ri = r * levels / 256;
								int gi = g * levels / 256;
								int bi = b * levels / 256;
								rTotal[ri] += r;
								gTotal[gi] += g;
								bTotal[bi] += b;
								rHistogram[ri]++;
								gHistogram[gi]++;
								bHistogram[bi]++;
							}
						}
					}
				}

				int r = 0, g = 0, b = 0;
				for (int i = 1; i < levels; i++) {
					if (rHistogram[i] > rHistogram[r])
						r = i;
					if (gHistogram[i] > gHistogram[g])
						g = i;
					if (bHistogram[i] > bHistogram[b])
						b = i;
				}
				r = rTotal[r] / rHistogram[r];
				g = gTotal[g] / gHistogram[g];
				b = bTotal[b] / bHistogram[b];
				outPixels[index] = (inPixels[index] & 0xff000000) | (r << 16) | (g << 8) | b;
				index++;
			}
		}
		return Bitmap.createBitmap(outPixels, src.getWidth(), src.getHeight(), outputConfig);
	}
}