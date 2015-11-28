package com.madrobot.graphics.bitmap;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.madrobot.geom.Rectangle;
import com.madrobot.graphics.ColorUtils;
import com.madrobot.graphics.bitmap.OutputConfiguration.BitmapMeta;

/**
 * Bitmap shapes/perception transforming filters.
 * <p>
 * <b>Water Ripple</b><br/>
 * Ripple effect with <code>wavelength</code> of 16, <code>amplitude</code> 10
 * and <code>radius</code> of 150. <br/>
 * <img src="../../../../resources/waterripple.png" ><br/>
 * 
 * <b>Lens</b><br/>
 * Lens with <code>refractionIndex</code> of 1.5 and <code>radius</code> of 150.
 * <br/>
 * <img src="../../../../resources/lens.png" ><br/>
 * 
 * <b>Reflection</b><br/>
 * <img src="../../../../resources/reflection.png" ><br/>
 * <b>Flush 3D</b><br/>
 * <img src="../../../../resources/flush3d.png" ><br/>
 * 
 * <b>Sketch</b><br/>
 * <img src="../../../../resources/sketch.png" ><br/>
 * <b>Stipple</b><br/>
 * <img src="../../../../resources/stipple.png" ><br/>
 * <b>Fade</b><br/>
 * Fade using the <code>fadeWidth</code> of 100<br/>
 * <img src="../../../../resources/fade.png"><br/>
 * <b>Dissolve</b><br/>
 * Dissolve using the <code>density</code> of 0.65 and <code>softness</code>
 * 0.50. This bitmap has a black background<br/>
 * <img src="../../../../resources/dissolve.png"><br/>
 * <b>Diffuse</b><br/>
 * Diffuse using the <code>scale</code> of 30 and <code>edgeAction</code> ZERO.
 * This bitmap has a black background<br/>
 * <img src="../../../../resources/dissolve.png"><br/>
 * <b>Twirl</b><br/>
 * Twirl using the <code>angle</code> of -2.5, <code>radius</code> 100 and
 * {@link #EDGE_ACTION_CLAMP}. <br/>
 * <img src="../../../../resources/twirl.png"><br/>
 * </p>
 * 
 * @author elton.stephen.kent
 * 
 */
public class AestheticTransformFilters extends TransformFilters {

	/**
	 * Creates a water ripple effect on a bitmap
	 * 
	 * @param src
	 * @param wavelength
	 *            of the ripple waves. recommended: 16
	 * @param amplitude
	 *            of the ripple waves. recommended: 10
	 * @param phase
	 *            of the ripple waves. recommended:0
	 * @param centreX
	 *            x coordinate for the origin of the ripple. the value
	 *            <code>0.5</code> represents the centre of the bitmap
	 * @param centreY
	 *            Y coordinate for the origin of the ripple. the value
	 *            <code>0.5</code> represents the centre of the bitmap
	 * @param radius
	 *            radius of the ripple. could be any value that falls within the
	 *            bounds of the image
	 * @param edgeAction
	 *            for the effect. {@link TransformFilters#EDGE_ACTION_CLAMP},
	 *            {@link TransformFilters#EDGE_ACTION_RGB_CLAMP},
	 *            {@link TransformFilters#EDGE_ACTION_WRAP} and
	 *            {@link TransformFilters#EDGE_ACTION_ZERO}. recommended:
	 *            {@link TransformFilters#EDGE_ACTION_CLAMP}
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap waterRipple(Bitmap src, float wavelength, float amplitude,
			float phase, float centreX, float centreY, float radius, int edgeAction,
			Bitmap.Config outputConfig) {
		int width = src.getWidth();

		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);

		float icentreX = src.getWidth() * centreX;
		float icentreY = src.getHeight() * centreY;
		if (radius == 0)
			radius = Math.min(icentreX, icentreY);
		float radius2 = radius * radius;

		// Rectangle originalSpace = new Rectangle(0, 0, width, height);
		Rectangle transformedSpace = new Rectangle(0, 0, width, height);
		// transformSpace(transformedSpace);

		int srcWidth = width;
		int srcHeight = height;
		int srcWidth1 = width - 1;
		int srcHeight1 = height - 1;
		int outWidth = transformedSpace.width;
		int outHeight = transformedSpace.height;
		int outX, outY;
		int index = 0;
		int[] outPixels = new int[outWidth];
		int[] destPixels = new int[outHeight * outWidth];
		outX = transformedSpace.x;
		outY = transformedSpace.y;
		float[] out = new float[2];

		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				waterRippleTransformInverse(outX + x, outY + y, out, icentreX, icentreY,
						radius2, amplitude, wavelength, phase, radius);
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
			BitmapUtils.setPixelRow(outPixels, y, outWidth, destPixels);
		}

		return Bitmap.createBitmap(destPixels, outWidth, outHeight, outputConfig);
	}

	private static void waterRippleTransformInverse(int x, int y, float[] out, float icentreX,
			float icentreY, float radius2, float amplitude, float wavelength, float phase,
			float radius) {
		float dx = x - icentreX;
		float dy = y - icentreY;
		float distance2 = dx * dx + dy * dy;
		if (distance2 > radius2) {
			out[0] = x;
			out[1] = y;
		} else {
			float distance = (float) Math.sqrt(distance2);
			float amount = amplitude
					* (float) Math.sin(distance / wavelength * ImageMath.TWO_PI - phase);
			amount *= (radius - distance) / radius;
			if (distance != 0)
				amount *= wavelength / distance;
			out[0] = x + dx * amount;
			out[1] = y + dy * amount;
		}
	}

	/**
	 * Simulates a magnifying lens placed over the given coordinate of an image
	 * 
	 * @param src
	 * @param centreX
	 *            the X coordinate of the center of the lens. The value
	 *            <code>0.5</code> represents the centre of the bitmap
	 * @param centreY
	 *            the Y coordinate of the center of the lens. The value
	 *            <code>0.5</code> represents the centre of the bitmap
	 * @param radius
	 *            Radius of the lens
	 * @param refractionIndex
	 *            lens refraction index. recommended:1.5
	 * @param edgeAction
	 *            recommended: {@link #EDGE_ACTION_CLAMP}
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap lens(Bitmap src, float centreX, float centreY, float radius,
			float refractionIndex, int edgeAction, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		float a = radius;
		float b = radius;
		float a2 = 0;
		float b2 = 0;
		float icentreX = width * centreX;
		float icentreY = height * centreY;
		if (a == 0)
			a = width / 2;
		if (b == 0)
			b = height / 2;
		a2 = a * a;
		b2 = b * b;

		// Rectangle originalSpace = new Rectangle(0, 0, width, height);
		Rectangle transformedSpace = new Rectangle(0, 0, width, height);
		// transformSpace(transformedSpace);

		int[] inPixels = BitmapUtils.getPixels(src);
		int srcWidth = width;
		int srcHeight = height;
		int srcWidth1 = width - 1;
		int srcHeight1 = height - 1;
		int outWidth = transformedSpace.width;
		int outHeight = transformedSpace.height;
		int outX, outY;
		// int index = 0;
		int[] outPixels = new int[outWidth];
		int[] destPixels = new int[outHeight * outWidth];

		outX = transformedSpace.x;
		outY = transformedSpace.y;
		float[] out = new float[2];
		// Bitmap dest = Bitmap.createBitmap(outWidth, outHeight, outputConfig);
		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				transformInverse(outX + x, outY + y, out, icentreX, icentreY, refractionIndex,
						a, b, a2, b2);
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
			// set pixel
			BitmapUtils.setPixelRow(outPixels, y, outWidth, destPixels);
		}
		return Bitmap.createBitmap(destPixels, outWidth, outHeight, outputConfig);
	}

	private static void transformInverse(int x, int y, float[] out, float icentreX,
			float icentreY, float refractionIndex, float a, float b, float a2, float b2) {
		float dx = x - icentreX;
		float dy = y - icentreY;
		float x2 = dx * dx;
		float y2 = dy * dy;
		if (y2 >= (b2 - (b2 * x2) / a2)) {
			out[0] = x;
			out[1] = y;
		} else {
			float rRefraction = 1.0f / refractionIndex;

			float z = (float) Math.sqrt((1.0f - x2 / a2 - y2 / b2) * (a * b));
			float z2 = z * z;

			float xAngle = (float) Math.acos(dx / Math.sqrt(x2 + z2));
			float angle1 = ImageMath.HALF_PI - xAngle;
			float angle2 = (float) Math.asin(Math.sin(angle1) * rRefraction);
			angle2 = ImageMath.HALF_PI - xAngle - angle2;
			out[0] = x - (float) Math.tan(angle2) * z;

			float yAngle = (float) Math.acos(dy / Math.sqrt(y2 + z2));
			angle1 = ImageMath.HALF_PI - yAngle;
			angle2 = (float) Math.asin(Math.sin(angle1) * rRefraction);
			angle2 = ImageMath.HALF_PI - yAngle - angle2;
			out[1] = y - (float) Math.tan(angle2) * z;
		}
	}

	/**
	 * Create a reflection of an image
	 * 
	 * @param image
	 *            source image
	 * @param reflectionHeight
	 *            height of the reflection
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * 
	 * @return Image the height of the image is increased to accommodate the
	 *         reflection height.
	 */
	public static final Bitmap createReflection(final Bitmap image,
			final int reflectionHeight, Bitmap.Config outputConfig) {
		/* Tested Apr 27 2011 */
		int w = image.getWidth();
		int h = image.getHeight();

		final Bitmap reflectedImage = Bitmap.createBitmap(w, h + reflectionHeight,
				outputConfig);
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
	 * Generates only the border pixels of the given image
	 * 
	 * @param bitmap
	 * 
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 */
	public static Bitmap doImageRim(Bitmap bitmap, Bitmap.Config outputConfig) {
		byte[][] filter = { { 0, -1, 0 }, { -1, 5, -1 }, { 0, -1, 0 } };
		return BitmapFilters.applyFilter(bitmap, 0, filter, outputConfig);
	}

	/**
	 * This filter tries to apply the Swing "flush 3D" effect to the black lines
	 * in an image.
	 * 
	 * @param src
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap flush3D(Bitmap src, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		int index = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = inPixels[y * width + x];

				if (pixel != 0xff000000 && y > 0 && x > 0) {
					int count = 0;
					if (inPixels[y * width + x - 1] == 0xff000000)
						count++;
					if (inPixels[(y - 1) * width + x] == 0xff000000)
						count++;
					if (inPixels[(y - 1) * width + x - 1] == 0xff000000)
						count++;
					if (count >= 2)
						pixel = 0xffffffff;
				}
				outPixels[index++] = pixel;
			}

		}
		return Bitmap.createBitmap(outPixels, src.getWidth(), src.getHeight(), outputConfig);
	}

	/**
	 * Creates a sparkle effect from the middle of the bitmap
	 * 
	 * @param src
	 * @param rayColor
	 *            of the light rays.
	 * @param shineAmount
	 *            shine amount.
	 * @param noOfRays
	 *            number of light rays in the sparkle. min:0 max:300.
	 * @param sparkleRadius
	 *            of the sparkle. min:0 max:300.
	 * @param randomness
	 *            sparkle randomness. min:0 max:50.
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap sparkle(Bitmap src, int rayColor, int shineAmount, int noOfRays,
			int sparkleRadius, int randomness, OutputConfiguration outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();

		Random randomNumbers = new Random();
		int centreX = width / 2;
		int centreY = height / 2;
		randomNumbers.setSeed(371);
		float[] rayLengths = new float[noOfRays];
		for (int i = 0; i < noOfRays; i++)
			rayLengths[i] = sparkleRadius + randomness / 100.0f * sparkleRadius
					* (float) randomNumbers.nextGaussian();

		BitmapMeta meta = outputConfig.getBitmapMeta(src);
		int[] inPixels = BitmapUtils.getPixels(src);
		int position, rgb;
		for (int y = meta.y; y < meta.targetHeight; y++) {
			for (int x = meta.x; x < meta.targetWidth; x++) {
				position = (y * meta.bitmapWidth) + x;
				rgb = inPixels[position];

				float dx = x - centreX;
				float dy = y - centreY;
				float distance = dx * dx + dy * dy;
				float angle = (float) Math.atan2(dy, dx);
				float d = (angle + ImageMath.PI) / (ImageMath.TWO_PI) * noOfRays;
				int i = (int) d;
				float f = d - i;

				if (sparkleRadius != 0) {
					float length = ImageMath.lerp(f, rayLengths[i % noOfRays],
							rayLengths[(i + 1) % noOfRays]);
					float g = length * length / (distance + 0.0001f);
					g = (float) Math.pow(g, (100 - shineAmount) / 50.0);
					f -= 0.5f;
					// f *= amount/50.0f;
					f = 1 - f * f;
					f *= g;
				}
				f = ImageMath.clamp(f, 0, 1);
				inPixels[position] = ImageMath.mixColors(f, rgb, rayColor);
			}
		}
		return Bitmap.createBitmap(inPixels, meta.bitmapWidth, meta.bitmapHeight,
				outputConfig.config);
	}

	/**
	 * A filter which produces the stipple effect.
	 * 
	 * @param bitmap
	 * @param outputConfig
	 * @return
	 */
	public static final Bitmap stipple(Bitmap bitmap, OutputConfiguration outputConfig) {
		int[] argb = BitmapUtils.getPixels(bitmap);
		BitmapMeta meta = outputConfig.getBitmapMeta(bitmap);
		int position, rgb;
		for (int y = meta.y; y < meta.targetHeight; y++) {
			for (int x = meta.x; x < meta.targetWidth; x++) {
				position = (y * meta.bitmapWidth) + x;
				rgb = argb[position];
				argb[position] = ((x & 1) == (y & 1)) ? rgb : ImageMath.mixColors(0.25f,
						0xff999999, rgb);
			}
		}
		if (outputConfig.canRecycleSrc) {
			bitmap.recycle();
		}
		return Bitmap.createBitmap(argb, meta.bitmapWidth, meta.bitmapHeight,
				outputConfig.config);
	}

	/**
	 * Performs fading effect on the width specified
	 * 
	 * @param src
	 * @param angle
	 * @param fadeStart
	 * @param fadeWidth
	 *            width of the image that should be faded
	 * @param invert
	 *            the specified bitmap width is inverted in the fade
	 * @param sides
	 *            number of sides. recommended:0
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap fade(Bitmap src, float angle, float fadeStart, float fadeWidth,
			boolean invert, int sides, OutputConfiguration outputConfig) {

		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		float m00 = cos;
		float m01 = sin;
		float m10 = -sin;
		float m11 = cos;

		com.madrobot.graphics.bitmap.OutputConfiguration.BitmapMeta meta = outputConfig
				.getBitmapMeta(src);
		int[] inPixels = BitmapUtils.getPixels(src);
		int position, rgb;
		for (int y = meta.y; y < meta.targetHeight; y++) {
			for (int x = meta.x; x < meta.targetWidth; x++) {
				position = (y * meta.bitmapWidth) + x;
				rgb = inPixels[position];
				float nx = m00 * x + m01 * y;
				float ny = m10 * x + m11 * y;
				if (sides == 2)
					nx = (float) Math.sqrt(nx * nx + ny * ny);
				else if (sides == 3)
					nx = ImageMath.mod(nx, 16);
				else if (sides == 4)
					nx = symmetry(nx, 16);
				int alpha = (int) (ImageMath.smoothStep(fadeStart, fadeStart + fadeWidth, nx) * 255);
				if (invert)
					alpha = 255 - alpha;

				inPixels[position] = (alpha << 24) | (rgb & 0x00ffffff);
			}
		}
		if (outputConfig.canRecycleSrc) {
			src.recycle();
		}
		return Bitmap.createBitmap(inPixels, meta.bitmapWidth, meta.bitmapHeight,
				outputConfig.config);
	}

	private static float symmetry(float x, float b) {

		x = ImageMath.mod(x, 2 * b);
		if (x > b)
			return 2 * b - x;
		return x;
	}

	/**
	 * A filter which "dissolves" an image by thresholding the alpha channel
	 * with random numbers.
	 * 
	 * @param src
	 * @param density
	 *            the density of the image in the range 0..1.
	 * @param softness
	 *            the softness of the dissolve in the range 0..1.
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap dissolve(Bitmap src, float density, float softness,
			OutputConfiguration outputConfig) {
		float d = (1 - density) * (1 + softness);
		float minDensity = d - softness;
		float maxDensity = d;
		Random randomNumbers = new Random(0);
		com.madrobot.graphics.bitmap.OutputConfiguration.BitmapMeta meta = outputConfig
				.getBitmapMeta(src);
		int[] inPixels = BitmapUtils.getPixels(src);
		int position, rgb, a;
		float v, f;
		for (int y = meta.y; y < meta.targetHeight; y++) {
			for (int x = meta.x; x < meta.targetWidth; x++) {
				position = (y * meta.bitmapWidth) + x;
				rgb = inPixels[position];
				a = (rgb >> 24) & 0xff;
				v = randomNumbers.nextFloat();
				f = ImageMath.smoothStep(minDensity, maxDensity, v);
				inPixels[position] = ((int) (a * f) << 24) | rgb & 0x00ffffff;
			}
		}
		if (outputConfig.canRecycleSrc) {
			src.recycle();
		}
		return Bitmap.createBitmap(inPixels, meta.bitmapWidth, meta.bitmapHeight,
				outputConfig.config);
	}

	/**
	 * This filter diffuses an image by moving its pixels in random directions.
	 * 
	 * @param src
	 * @param scale
	 *            diffusion scale. min:1 max:100
	 * @param edgeAction
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap diffuse(Bitmap src, float scale, int edgeAction, Config outputConfig) {
		float[] sinTable = new float[256];
		float[] cosTable = new float[256];
		for (int i = 0; i < 256; i++) {
			float angle = ImageMath.TWO_PI * i / 256f;
			sinTable[i] = (float) (scale * Math.sin(angle));
			cosTable[i] = (float) (scale * Math.cos(angle));
		}
		int width = src.getWidth();
		int height = src.getHeight();

		Rectangle transformedSpace = new Rectangle(0, 0, width, height);

		int[] inPixels = BitmapUtils.getPixels(src);

		int srcWidth = width;
		int srcHeight = height;
		int srcWidth1 = width - 1;
		int srcHeight1 = height - 1;
		int outWidth = transformedSpace.width;
		int outHeight = transformedSpace.height;
		int outX, outY;
		int[] outPixels = new int[outWidth];
		int[] destPixels = new int[outHeight * outWidth];
		outX = transformedSpace.x;
		outY = transformedSpace.y;
		float[] out = new float[2];

		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				transformInverse(outX + x, outY + y, out, sinTable, cosTable);
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
					sw = TransformFilters.getPixel(inPixels, srcX, srcY + 1, srcWidth,
							srcHeight, edgeAction);
					se = TransformFilters.getPixel(inPixels, srcX + 1, srcY + 1, srcWidth,
							srcHeight, edgeAction);
				}
				outPixels[x] = ImageMath.bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
			}
			BitmapUtils.setPixelRow(outPixels, y, outWidth, destPixels);
		}

		return Bitmap.createBitmap(destPixels, outWidth, outHeight, outputConfig);
	}

	private static void transformInverse(int x, int y, float[] out, float[] sinTable,
			float[] cosTable) {
		int angle = (int) (Math.random() * 255);
		float distance = (float) Math.random();
		out[0] = x + distance * sinTable[angle];
		out[1] = y + distance * cosTable[angle];
	}

	public static Bitmap contour(Bitmap bitmap, int contourColor, float levels, float scale,
			float offset, Config outputConfig) {
		int[] inPixels = BitmapUtils.getPixels(bitmap);
		int width = bitmap.getWidth();

		int height = bitmap.getHeight();
		int index = 0;
		short[][] r = new short[3][width];
		int[] outPixels = new int[width * height];

		short[] table = new short[256];
		int offsetl = (int) (offset * 256 / levels);
		for (int i = 0; i < 256; i++)
			table[i] = (short) ColorUtils.clamp((int) (255
					* Math.floor(levels * (i + offsetl) / 256) / (levels - 1) - offsetl));

		for (int x = 0; x < width; x++) {
			int rgb = inPixels[x];
			r[1][x] = (short) ColorUtils.brightness(rgb);
		}
		for (int y = 0; y < height; y++) {
			boolean yIn = y > 0 && y < height - 1;
			int nextRowIndex = index + width;
			if (y < height - 1) {
				for (int x = 0; x < width; x++) {
					int rgb = inPixels[nextRowIndex++];
					r[2][x] = (short) ColorUtils.brightness(rgb);
				}
			}
			for (int x = 0; x < width; x++) {
				boolean xIn = x > 0 && x < width - 1;
				int w = x - 1;
				int e = x + 1;
				int v = 0;

				if (yIn && xIn) {
					short nwb = r[0][w];
					short neb = r[0][x];
					short swb = r[1][w];
					short seb = r[1][x];
					short nw = table[nwb];
					short ne = table[neb];
					short sw = table[swb];
					short se = table[seb];

					if (nw != ne || nw != sw || ne != se || sw != se) {
						v = (int) (scale * (Math.abs(nwb - neb) + Math.abs(nwb - swb)
								+ Math.abs(neb - seb) + Math.abs(swb - seb)));
						// v /= 255;
						if (v > 255)
							v = 255;
					}
				}

				if (v != 0)
					outPixels[index] = ColorUtils.combinePixels(inPixels[index], contourColor,
							ColorUtils.EXCHANGE, v);
				// outPixels[index] = PixelUtils.combinePixels( (contourColor &
				// 0xff)|(v << 24), inPixels[index],
				// PixelUtils.NORMAL );
				else
					outPixels[index] = inPixels[index];
				index++;
			}
			short[] t;
			t = r[0];
			r[0] = r[1];
			r[1] = r[2];
			r[2] = t;
		}
		return Bitmap.createBitmap(outPixels, width, height, outputConfig);
	}

	public static Bitmap sketch(Bitmap bitmap, Bitmap.Config outputConfig) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int[] image = BitmapUtils.getPixels(bitmap);
		int idx, p, r, g, b, x, y;
		for (y = 0; y < h; y++) {
			for (x = 0; x < w; x++) {
				idx = (y * w) + x;
				p = image[idx];
				r = p & 0x00FF0000 >> 16;
				g = p & 0x0000FF >> 8;
				b = p & 0x000000FF;
				image[idx] = (int) ((r + g + b) / 3.0);
			}
		}
		int convolutionSize = 3;
		int[][] convolution = { { 0, -1, 0 }, { -1, 4, -1 }, { 0, -1, 0 } };
		int[] newImage = new int[w * h];
		// Apply the convolution to the whole image, note that we start at
		// 1 instead 0 zero to avoid out-of-bounds access
		for (y = 1; y + 1 < h; y++) {
			for (x = 1; x + 1 < w; x++) {
				idx = (y * w) + x;

				// Apply the convolution
				for (int cy = 0; cy < convolutionSize; cy++) {
					for (int cx = 0; cx < convolutionSize; cx++) {
						int cIdx = (((y - 1) + cy) * w) + ((x - 1) + cx);
						newImage[idx] += convolution[cy][cx] * image[cIdx];
					}
				}

				// pixel value rounding
				if (newImage[idx] < 0) {
					newImage[idx] = -newImage[idx];
				} else {
					newImage[idx] = 0;
				}
				if (newImage[idx] > 0) {
					newImage[idx] = 120 - newImage[idx];
				} else {
					newImage[idx] = 255;
				}

			}
		}

		// Convert to "proper" grayscale
		for (y = 0; y < h; y++) {
			for (x = 0; x < w; x++) {
				idx = (y * w) + x;
				p = newImage[idx];
				newImage[idx] = 0xFF000000 | (p << 16) | (p << 8) | p;
			}
		}
		return Bitmap.createBitmap(newImage, w, h, outputConfig);
	}
}
