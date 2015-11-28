package com.madrobot.graphics;

import java.util.Random;

import android.graphics.Color;

/**
 * Pixel level manipulation utils
 */
public class ColorUtils {

	public final static int REPLACE = 0;
	public final static int NORMAL = 1;
	public final static int MIN = 2;
	public final static int MAX = 3;
	public final static int ADD = 4;
	public final static int SUBTRACT = 5;
	public final static int DIFFERENCE = 6;
	public final static int MULTIPLY = 7;
	public final static int HUE = 8;
	public final static int SATURATION = 9;
	public final static int VALUE = 10;
	public final static int COLOR = 11;
	public final static int SCREEN = 12;
	public final static int AVERAGE = 13;
	public final static int OVERLAY = 14;
	public final static int CLEAR = 15;
	public final static int EXCHANGE = 16;
	public final static int DISSOLVE = 17;
	public final static int DST_IN = 18;
	public final static int ALPHA = 19;
	public final static int ALPHA_TO_GRAY = 20;

	private static Random randomGenerator = new Random();

	/**
	 * Clamp a value to the range 0..255
	 */
	public static int clamp(int c) {
		if (c < 0)
			return 0;
		if (c > 255)
			return 255;
		return c;
	}

	public static int interpolate(int v1, int v2, float f) {
		return clamp((int) (v1 + f * (v2 - v1)));
	}

	public static int brightness(int rgb) {
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		return (r + g + b) / 3;
	}

	/**
	 * Check if the colors are near each other in terms of the defined tolerance
	 * 
	 * @param rgb1
	 * @param rgb2
	 * @param tolerance
	 * @return
	 */
	public static boolean nearColors(int rgb1, int rgb2, int tolerance) {
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = rgb1 & 0xff;
		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = rgb2 & 0xff;
		return Math.abs(r1 - r2) <= tolerance && Math.abs(g1 - g2) <= tolerance
				&& Math.abs(b1 - b2) <= tolerance;
	}

	private final static float hsb1[] = new float[3];// FIXME-not thread safe
	private final static float hsb2[] = new float[3];// FIXME-not thread safe

	// Return rgb1 painted onto rgb2
	public static int combinePixels(int rgb1, int rgb2, int op) {
		return combinePixels(rgb1, rgb2, op, 0xff);
	}

	public static int combinePixels(int rgb1, int rgb2, int op, int extraAlpha, int channelMask) {
		return (rgb2 & ~channelMask) | combinePixels(rgb1 & channelMask, rgb2, op, extraAlpha);
	}

	/**
	 * Combine pixels using the specified operation
	 * 
	 * @param rgb1
	 *            first pixel to combine
	 * @param rgb2
	 *            second pixel to combine
	 * @param operation
	 *            see {@link ColorUtils#NORMAL} , {@link ColorUtils#MIN},
	 *            {@link ColorUtils#MAX}, {@link ColorUtils#MAX} ,
	 *            {@link ColorUtils#ADD},{@link ColorUtils#SUBTRACT} ,
	 *            {@link ColorUtils#DIFFERENCE}, {@link ColorUtils#DISSOLVE} ,
	 *            {@link ColorUtils#DST_IN}, {@link ColorUtils#MULTIPLY},
	 *            {@link ColorUtils#AVERAGE},{@link ColorUtils#ALPHA_TO_GRAY}
	 *            and so on.
	 * @param extraAlpha
	 *            level of alpha to extract. Maximum value: 255, Min Value:0
	 * @return
	 */
	public static int combinePixels(int rgb1, int rgb2, int operation, int extraAlpha) {
		if (operation == REPLACE)
			return rgb1;
		int a1 = (rgb1 >> 24) & 0xff;
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = rgb1 & 0xff;
		int a2 = (rgb2 >> 24) & 0xff;
		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = rgb2 & 0xff;

		switch (operation) {
		case NORMAL:
			break;
		case MIN:
			r1 = Math.min(r1, r2);
			g1 = Math.min(g1, g2);
			b1 = Math.min(b1, b2);
			break;
		case MAX:
			r1 = Math.max(r1, r2);
			g1 = Math.max(g1, g2);
			b1 = Math.max(b1, b2);
			break;
		case ADD:
			r1 = clamp(r1 + r2);
			g1 = clamp(g1 + g2);
			b1 = clamp(b1 + b2);
			break;
		case SUBTRACT:
			r1 = clamp(r2 - r1);
			g1 = clamp(g2 - g1);
			b1 = clamp(b2 - b1);
			break;
		case DIFFERENCE:
			r1 = clamp(Math.abs(r1 - r2));
			g1 = clamp(Math.abs(g1 - g2));
			b1 = clamp(Math.abs(b1 - b2));
			break;
		case MULTIPLY:
			r1 = clamp(r1 * r2 / 255);
			g1 = clamp(g1 * g2 / 255);
			b1 = clamp(b1 * b2 / 255);
			break;
		case DISSOLVE:
			if ((randomGenerator.nextInt() & 0xff) <= a1) {
				r1 = r2;
				g1 = g2;
				b1 = b2;
			}
			break;
		case AVERAGE:
			r1 = (r1 + r2) / 2;
			g1 = (g1 + g2) / 2;
			b1 = (b1 + b2) / 2;
			break;
		case HUE:
		case SATURATION:
		case VALUE:
		case COLOR:
			Color.RGBToHSV(r1, g1, b1, hsb1);
			Color.RGBToHSV(r2, g2, b2, hsb2);
			switch (operation) {
			case HUE:
				hsb2[0] = hsb1[0];
				break;
			case SATURATION:
				hsb2[1] = hsb1[1];
				break;
			case VALUE:
				hsb2[2] = hsb1[2];
				break;
			case COLOR:
				hsb2[0] = hsb1[0];
				hsb2[1] = hsb1[1];
				break;
			}
			rgb1 = Color.HSVToColor(hsb2);// (hsb2[0], hsb2[1], hsb2[2]);
			r1 = (rgb1 >> 16) & 0xff;
			g1 = (rgb1 >> 8) & 0xff;
			b1 = rgb1 & 0xff;
			break;
		case SCREEN:
			r1 = 255 - ((255 - r1) * (255 - r2)) / 255;
			g1 = 255 - ((255 - g1) * (255 - g2)) / 255;
			b1 = 255 - ((255 - b1) * (255 - b2)) / 255;
			break;
		case OVERLAY:
			int m,
			s;
			s = 255 - ((255 - r1) * (255 - r2)) / 255;
			m = r1 * r2 / 255;
			r1 = (s * r1 + m * (255 - r1)) / 255;
			s = 255 - ((255 - g1) * (255 - g2)) / 255;
			m = g1 * g2 / 255;
			g1 = (s * g1 + m * (255 - g1)) / 255;
			s = 255 - ((255 - b1) * (255 - b2)) / 255;
			m = b1 * b2 / 255;
			b1 = (s * b1 + m * (255 - b1)) / 255;
			break;
		case CLEAR:
			r1 = g1 = b1 = 0xff;
			break;
		case DST_IN:
			r1 = clamp((r2 * a1) / 255);
			g1 = clamp((g2 * a1) / 255);
			b1 = clamp((b2 * a1) / 255);
			a1 = clamp((a2 * a1) / 255);
			return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
		case ALPHA:
			a1 = a1 * a2 / 255;
			return (a1 << 24) | (r2 << 16) | (g2 << 8) | b2;
		case ALPHA_TO_GRAY:
			int na = 255 - a1;
			return (a1 << 24) | (na << 16) | (na << 8) | na;
		}
		if (extraAlpha != 0xff || a1 != 0xff) {
			a1 = a1 * extraAlpha / 255;
			int a3 = (255 - a1) * a2 / 255;
			r1 = clamp((r1 * a1 + r2 * a3) / 255);
			g1 = clamp((g1 * a1 + g2 * a3) / 255);
			b1 = clamp((b1 * a1 + b2 * a3) / 255);
			a1 = clamp(a1 + a3);
		}
		return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
	}

	/**
	 * Change the brightness of a color
	 * 
	 * @param argb
	 * @param percent
	 *            to increase brightness. Negative value indicates decrease
	 *            brightness
	 * @return Hex color with the adjusted brightness
	 */
	public static int adjustBrightness(int argb, int percent) {
		int nChange = 255 * percent / 100;
		int r = ((argb & 0x00FF0000) >> 16) + nChange;
		int g = ((argb & 0x0000FF00) >> 8) + nChange;
		int b = (argb & 0x000000FF) + nChange;

		r = r < 0 ? 0 : (r > 255 ? 255 : r);
		g = g < 0 ? 0 : (g > 255 ? 255 : g);
		b = b < 0 ? 0 : (b > 255 ? 255 : b);

		return (r << 16) | (g << 8) | b;
	}

	/**
	 * Apply sepia to the given color
	 * 
	 * @param argb
	 *            argb value
	 * @param depth
	 *            sepia depth , optimal is 20
	 * @return
	 */
	public static int applySepia(int argb, int depth) {
		int a = (argb >> 24) & 0xff;
		int r = (argb >> 16) & 0xff;
		int g = (argb >> 8) & 0xff;
		int b = argb & 0xff;

		int gry = (r + g + b) / 3;
		r = g = b = gry;

		r = r + (depth * 2);
		g = g + depth;
		if (r > 255) {
			r = 255;
		}
		if (g > 255) {
			g = 255;
		}
		return toRGB(a, r, g, b);
	}

	/**
	 * Blend two colors
	 * 
	 * @param argb1
	 * @param argb2
	 * @param f
	 *            percentage
	 * @return Blended color
	 */
	public static int blend(int argb1, int argb2, float f) {
		return mix(argb1 >>> 24, argb2 >>> 24, f) << 24
				| mix((argb1 >> 16) & 0xff, (argb2 >> 16) & 0xff, f) << 16
				| mix((argb1 >> 8) & 0xff, (argb2 >> 8) & 0xff, f) << 8
				| mix(argb1 & 0xff, argb2 & 0xff, f);
	}

	/**
	 * Clear the transparency in a color
	 * 
	 * @param argb
	 * @return
	 */
	public static int clearTransparency(int argb) {
		return argb & 0x00FFFFFF;
	}

	/**
	 * Convert color array to HSV color model
	 * 
	 * @param argb
	 */
	public static void convertToHSV(int[] argb) {
		for (int i = 0; i < argb.length; i++) {

			int nR = Color.red(argb[i]);
			int nG = Color.green(argb[i]);
			int nB = Color.blue(argb[i]);
			int nMax, nMid, nMin;
			int nHueOffset;
			// determine color order
			if ((nR > nG) && (nR > nB)) {
				// red is max
				nMax = nR;
				nHueOffset = 0;
				if (nG > nB) {
					nMid = nG;
					nMin = nB;
				} else {
					nMid = nB;
					nMin = nG;
				}
			} else if ((nG > nR) && (nG > nB)) {
				// green is max
				nMax = nG;
				nHueOffset = 80;
				if (nR > nB) {
					nMid = nR;
					nMin = nB;
				} else {
					nMid = nB;
					nMin = nR;
				}
			} else {
				// blue is max
				nMax = nB;
				nHueOffset = 160;
				if (nR > nG) {
					nMid = nR;
					nMin = nG;
				} else {
					nMid = nG;
					nMin = nR;
				}
			}
			// if the max value is Byte.MIN_VALUE the RGB value
			// = 0 so the HSV value = 0 and needs no change.
			if (nMax > Byte.MIN_VALUE) {
				if (nMax == nMin) {
					// color is gray. Hue, saturation are 0.
					argb[i] = Color.rgb(Byte.MIN_VALUE, Byte.MIN_VALUE, (byte) nMax);
				} else {
					// compute hue scaled from 0-240.
					int nHue = Math
							.min(239, nHueOffset + (40 * (nMid - nMin)) / (nMax - nMin));
					// compute saturation scaled from 0-255.
					int nSat = Math.min(255, (256 * (nMax - nMin)) / (nMax - Byte.MIN_VALUE));
					argb[i] = Color.rgb((byte) (nHue + Byte.MIN_VALUE),
							(byte) (nSat + Byte.MIN_VALUE), (byte) nMax);
				}
			}
		}
	}

	/**
	 * Convert a color to grayscale
	 * 
	 * @param argb
	 * @return
	 */
	public static final int covertToGrayscale(int argb) {
		int a = (argb >> 24) & 0xff;
		int r = (argb >> 16) & 0xff;
		int g = (argb >> 8) & 0xff;
		int b = argb & 0xff;
		int gray = (r + g + b) / 3;
		return (a << 24) | (gray << 16) | (gray << 8) | gray;
	}

	public static int getAlpha(int argb) {
		return Color.alpha(argb);
	}

	public static int getBlue(int argb) {
		return Color.blue(argb);
	}

	/**
	 * Get a brighter shade
	 * 
	 * @param argb
	 * @return
	 */
	public static int getBrighterColor(int argb) {
		int i = getRed(argb);
		int j = getGreen(argb);
		int k = getBlue(argb);
		byte byte0 = 3;
		if ((i == 0) && (j == 0) && (k == 0)) {
			return toRGB(255, byte0, byte0, byte0);
		}
		if ((i > 0) && (i < byte0)) {
			i = byte0;
		}
		if ((j > 0) && (j < byte0)) {
			j = byte0;
		}
		if ((k > 0) && (k < byte0)) {
			k = byte0;
		}
		return toRGB(255, Math.min((int) (i / 0.69999999999999996D), 255),
				Math.min((int) (j / 0.69999999999999996D), 255),
				Math.min((int) (k / 0.69999999999999996D), 255));

	}

	/**
	 * 
	 * @param argb
	 * @param shades
	 *            The number of shades brighter
	 * @return
	 */
	public static int getBrighterColor(int argb, int shades) {
		for (int i = 0; i < shades; i++) {
			argb = getBrighterColor(argb);
		}
		return argb;
	}

	/**
	 * 
	 * @param argb
	 * @return
	 */
	public static int getDarkerColor(int argb) {
		return toRGB(255, Math.max((int) (getRed(argb) * 0.69999999999999996D), 0),
				Math.max((int) (getGreen(argb) * 0.69999999999999996D), 0),
				Math.max((int) (getBlue(argb) * 0.69999999999999996D), 0));

	}

	/**
	 * 
	 * @param argb
	 * @param shades
	 *            The number of shades brighter
	 * @return
	 */
	public static int getDarkerColor(int argb, int shades) {
		for (int i = 0; i < shades; i++) {
			argb = getDarkerColor(argb);
		}
		return argb;
	}

	/**
	 * Get the distance between two colors
	 * 
	 * @param r1
	 *            Red component of the first color
	 * @param g1
	 *            Green component of the first color
	 * @param b1
	 *            Blue component of the first color
	 * @param r2
	 *            Red component of the second color
	 * @param g2
	 *            Green component of the second color
	 * @param b2
	 *            Blue component of the second color
	 * @return Distance bwetween colors
	 */
	public static double getDistance(int r1, int g1, int b1, int r2, int g2, int b2) {
		double a = r2 - r1;
		double b = g2 - g1;
		double c = b2 - b1;

		return Math.sqrt(a * a + b * b + c * c);
	}

	/**
	 * Creates a gradient of colors. This method is highly optimized and only
	 * uses bit-shifting and additions (no multitplication nor devision), but it
	 * will create a new integer array in each call.
	 * 
	 * @param startColor
	 *            the first color
	 * @param endColor
	 *            the last color
	 * @param steps
	 *            the number of colors in the gradient, when 2 is given, the
	 *            first one will be the startColor and the second one will the
	 *            endColor.
	 * @return an int array with the gradient.
	 * 
	 */
	public static final int[] getGradient(int startColor, int endColor, int steps) {
		int[] gradient = new int[steps];
		getGradient(startColor, endColor, gradient);
		return gradient;
	}

	/**
	 * Creates a gradient of colors. This method is highly optimized and only
	 * uses bit-shifting and additions (no multitplication nor devision).
	 * 
	 * @param startColor
	 *            The first color
	 * @param endColor
	 *            The last color
	 * @param gradient
	 *            The array in which the gradient colors are stored. length of
	 *            the array is the number of steps used in the gradient
	 * 
	 */
	public static final void getGradient(final int startColor, final int endColor,
			final int[] gradient) {
		int steps = gradient.length;
		if (steps == 0) {
			return;
		} else if (steps == 1) {
			gradient[0] = startColor;
			return;
		}
		int startAlpha = startColor >>> 24;
		int startRed = (startColor >>> 16) & 0x00FF;
		int startGreen = (startColor >>> 8) & 0x0000FF;
		int startBlue = startColor & 0x00000FF;

		final int endAlpha = endColor >>> 24;
		final int endRed = (endColor >>> 16) & 0x00FF;
		final int endGreen = (endColor >>> 8) & 0x0000FF;
		final int endBlue = endColor & 0x00000FF;

		final int stepAlpha = ((endAlpha - startAlpha) << 8) / (steps - 1);
		final int stepRed = ((endRed - startRed) << 8) / (steps - 1);
		final int stepGreen = ((endGreen - startGreen) << 8) / (steps - 1);
		final int stepBlue = ((endBlue - startBlue) << 8) / (steps - 1);
		startAlpha <<= 8;
		startRed <<= 8;
		startGreen <<= 8;
		startBlue <<= 8;

		gradient[0] = startColor;
		for (int i = 1; i < steps; i++) {
			startAlpha += stepAlpha;
			startRed += stepRed;
			startGreen += stepGreen;
			startBlue += stepBlue;

			gradient[i] = ((startAlpha << 16) & 0xFF000000) | ((startRed << 8) & 0x00FF0000)
					| (startGreen & 0x0000FF00) | (startBlue >>> 8);
			// | (( startBlue >>> 8) & 0x000000FF);
		}
	}

	public static int getGreen(final int argb) {
		return Color.green(argb);
	}

	/**
	 * Return the hex name of a specified color.
	 * 
	 * @param argb
	 *            Color to get hex name of.
	 * @return Hex name of color: "rrggbb".
	 */
	public static String getHexName(int argb) {
		int r = getRed(argb);
		int g = getGreen(argb);
		int b = getBlue(argb);

		String rHex = Integer.toString(r, 16);
		String gHex = Integer.toString(g, 16);
		String bHex = Integer.toString(b, 16);

		return (rHex.length() == 2 ? "" + rHex : "0" + rHex)
				+ (gHex.length() == 2 ? "" + gHex : "0" + gHex)
				+ (bHex.length() == 2 ? "" + bHex : "0" + bHex);
	}

	public static int getRed(final int argb) {
		return Color.red(argb);
		// return argb >> 16 & 0xff;
	}

	/**
	 * Convert from HSB color to RGB
	 * 
	 * @param hue
	 * @param saturation
	 * @param brightness
	 * @return RGB value of given hue, saturation, brightness.
	 */
	public static int HSBtoRGB(final float hue, final float saturation, final float brightness) {
		int i = 0;
		int j = 0;
		int k = 0;
		if (saturation == 0.0F) {
			i = j = k = (int) (brightness * 255F + 0.5F);
		} else {
			float f3 = (hue - (float) Math.floor(hue)) * 6F;
			float f4 = f3 - (float) Math.floor(f3);
			float f5 = brightness * (1.0F - saturation);
			float f6 = brightness * (1.0F - saturation * f4);
			float f7 = brightness * (1.0F - saturation * (1.0F - f4));
			switch ((int) f3) {
			case 0: // '\0'
				i = (int) (brightness * 255F + 0.5F);
				j = (int) (f7 * 255F + 0.5F);
				k = (int) (f5 * 255F + 0.5F);
				break;

			case 1: // '\001'
				i = (int) (f6 * 255F + 0.5F);
				j = (int) (brightness * 255F + 0.5F);
				k = (int) (f5 * 255F + 0.5F);
				break;

			case 2: // '\002'
				i = (int) (f5 * 255F + 0.5F);
				j = (int) (brightness * 255F + 0.5F);
				k = (int) (f7 * 255F + 0.5F);
				break;

			case 3: // '\003'
				i = (int) (f5 * 255F + 0.5F);
				j = (int) (f6 * 255F + 0.5F);
				k = (int) (brightness * 255F + 0.5F);
				break;

			case 4: // '\004'
				i = (int) (f7 * 255F + 0.5F);
				j = (int) (f5 * 255F + 0.5F);
				k = (int) (brightness * 255F + 0.5F);
				break;

			case 5: // '\005'
				i = (int) (brightness * 255F + 0.5F);
				j = (int) (f5 * 255F + 0.5F);
				k = (int) (f6 * 255F + 0.5F);
				break;
			}
		}
		return 0xff000000 | i << 16 | j << 8 | k << 0;
	}

	/**
	 * Invert the color
	 * 
	 * @param color
	 * @return inverted color
	 */
	public static int invertColor(int color) {
		int a = (color >> 24) & 0xff;
		int r = (color >> 16) & 0xff;
		int g = (color >> 8) & 0xff;
		int b = color & 0xff;

		r = 255 - r;
		g = 255 - g;
		b = 255 - b;
		return toRGB(r, g, b, a);
	}

	/**
	 * Check if a color is more dark than light. Useful if an entity of this
	 * color is to be labeled: Use white label on a "dark" color and black label
	 * on a "light" color.
	 * 
	 * @param argb
	 *            Color to check.
	 * @return True if this is a "dark" color, false otherwise.
	 */
	public static boolean isDark(int argb) {
		int r = getRed(argb) / 255;
		int g = getGreen(argb) / 255;
		int b = getBlue(argb) / 255;

		return isDark(r, g, b);
	}

	/**
	 * Check if a color is more dark than light. Useful if an entity of this
	 * color is to be labeled: Use white label on a "dark" color and black label
	 * on a "light" color.
	 * 
	 * @param r
	 *            ,g,b Color to check.
	 * @return True if this is a "dark" color, false otherwise.
	 */
	public static boolean isDark(int r, int g, int b) {
		// Measure distance to white and black respectively
		double dWhite = getDistance(r, g, b, 255, 255, 255);
		double dBlack = getDistance(r, g, b, 0, 0, 0);

		return dBlack < dWhite;
	}

	/**
	 * Color between two colors
	 * 
	 * @param argb1
	 *            argb value of first color
	 * @param argb2
	 *            argb value of second color
	 * @param prop
	 * @param max
	 * @return Middle color
	 */
	public static final int middleColor(int argb1, int argb2, int prop, int max) {
		int red = (((argb1 >> 16) & 0xff) * prop + ((argb2 >> 16) & 0xff) * (max - prop))
				/ max;
		int green = (((argb1 >> 8) & 0xff) * prop + ((argb2 >> 8) & 0xff) * (max - prop))
				/ max;
		int blue = (((argb1 >> 0) & 0xff) * prop + ((argb2 >> 0) & 0xff) * (max - prop)) / max;
		int color = red << 16 | green << 8 | blue;
		return color;
	}

	private static int mix(int a, int b, float f) {
		return (int) (a + (b - a) * f);
	}

	/**
	 * Posterize a given pixel
	 * 
	 * @param argb
	 *            color
	 * @param depth
	 *            of posterization
	 * @return
	 */
	public static int posterizePixel(int argb, int depth) {
		int noOfAreas = 256 / depth;
		int noOfValues = 256 / (depth - 1);

		int a = (argb >> 24) & 0xff;
		int r = (argb >> 16) & 0xff;
		int g = (argb >> 8) & 0xff;
		int b = argb & 0xff;

		int redArea = r / noOfAreas;
		r = noOfValues * redArea;

		int greenArea = g / noOfAreas;
		g = noOfValues * greenArea;

		int blue = b / noOfAreas;
		b = noOfValues * blue;

		return toRGB(a, r, g, b);
	}

	/**
	 * Convert from RGB to HSB color
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param hsbvals
	 *            the array used to return the three HSB values, or null
	 * 
	 * @return a float array containing hue, saturation, brightness.
	 */
	public static float[] RGBtoHSB(int r, int g, int b, float hsbvals[]) {
		if (hsbvals == null) {
			hsbvals = new float[3];
		}
		int l = r <= g ? g : r;
		if (b > l) {
			l = b;
		}
		int i1 = r >= g ? g : r;
		if (b < i1) {
			i1 = b;
		}
		float f2 = l / 255F;
		float f1;
		if (l != 0) {
			f1 = (float) (l - i1) / (float) l;
		} else {
			f1 = 0.0F;
		}
		float f;
		if (f1 == 0.0F) {
			f = 0.0F;
		} else {
			float f3 = (float) (l - r) / (float) (l - i1);
			float f4 = (float) (l - g) / (float) (l - i1);
			float f5 = (float) (l - b) / (float) (l - i1);
			if (r == l) {
				f = f5 - f4;
			} else if (g == l) {
				f = (2.0F + f3) - f5;
			} else {
				f = (4F + f4) - f3;
			}
			f /= 6F;
			if (f < 0.0F) {
				f++;
			}
		}
		hsbvals[0] = f;
		hsbvals[1] = f1;
		hsbvals[2] = f2;
		return hsbvals;
	}

	/**
	 * set the luminosity of a ARGB value
	 * 
	 * @param argb
	 *            argb value
	 * @param percent
	 *            percent always less than or equal to 100
	 * @return
	 */
	public static int setLuminosity(int argb, int percent) {
		int a = (argb >> 24) & 0xff;
		int r = (argb >> 16) & 0xff;
		int g = (argb >> 8) & 0xff;
		int b = argb & 0xff;

		int t = (r * percent) / 100;
		r -= t;
		t = (g * percent) / 100;
		g -= t;
		t = (b * percent) / 100;
		b -= t;
		return toRGB(a, r, g, b);
	}

	/**
	 * Set the saturation for a given color
	 * <p>
	 * The saturation of a color is determined by a combination of light
	 * intensity and how much it is distributed across the spectrum of different
	 * wavelengths.<br/>
	 * <img src="../../../resources/saturation.png"> <small>Scale of saturation
	 * (0% at bottom).</small>
	 * </p>
	 * 
	 * @param argb
	 *            argb value
	 * @param percent
	 *            percent always less than or equal to 100
	 * @return color with the set saturation level
	 */
	public static int setSaturation(int argb, int percent) {
		int per = (128 * percent) / 100;
		int a = (argb >> 24) & 0xff;
		int r = (argb >> 16) & 0xff;
		int g = (argb >> 8) & 0xff;
		int b = argb & 0xff;
		r = (r + per) / 2;
		g = (g + per) / 2;
		b = (b + per) / 2;
		return toRGB(a, r, g, b);
	}

	/**
	 * 
	 * @param argb
	 *            argb value
	 * @param level
	 *            0(fully transparent) to 255(fully opaque)
	 * @return
	 */
	public static int setTransparency(int argb, int level) {
		level = (level << 24);
		return (argb & 0x00ffffff) | level;
	}

	public static void setTransparency(int[] argb, int level) {
		level = level << 24;
		for (int i = 0; i < argb.length; i++) {
			argb[i] = (argb[i] & 0x00ffffff) | level;
		}
	}

	/**
	 * 
	 * @param alpha
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	public static int toRGB(int alpha, int red, int green, int blue) {
		return (alpha & 0xff) << 24 | (red & 0xff) << 16 | (green & 0xff) << 8
				| (blue & 0xff) << 0;
	}

	/**
	 * 
	 * @param color
	 * @return
	 */
	public int getTransparency(int color) {
		int i = getAlpha(color);
		if (i == 255) {
			return 1;
		}
		return i != 0 ? 3 : 2;
	}

	// public static int toARGB(int a, int r, int g, int b) {
	// return (a << 24) + (r << 16) + (g << 8) + b;
	// }

	/**
	 * 
	 * @param x
	 * @param y
	 * @param rgb
	 * @param pixels
	 * @param stride
	 */
	public static void putPixel(int x, int y, int rgb, int[] pixels, int stride) {
		pixels[y * stride + x] = rgb;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param pixels
	 * @param stride
	 * @return
	 */
	public static int getPixel(int x, int y, int[] pixels, int stride) {
		return pixels[y * stride + x];
	}

	public static int average(int rgb1, int rgb2) {
		return combinePixels(rgb1, rgb2, ColorUtils.AVERAGE);
	}

	public static int displace(int rgb, float amount) {
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		r = clamp(r + (int) (amount * (randomGenerator.nextFloat() - 0.5)));
		g = clamp(g + (int) (amount * (randomGenerator.nextFloat() - 0.5)));
		b = clamp(b + (int) (amount * (randomGenerator.nextFloat() - 0.5)));
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}

	/**
	 * Get the pixel at the given location
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param argb
	 * @return
	 */
	public static int getPixel(int x, int y, int width, int height, int[] argb) {
		int nRow = y * width;
		return argb[nRow + x];
	}

	public static void setPixel(int pixel, int x, int y, int width, int height, int[] argb) {
		int nRow = y * width;
		argb[nRow + x] = pixel;
	}

	/**
	 * Get the temperature of the given pixel
	 * 
	 * @param argb
	 * @return
	 */
	public static float getTemperature(int rgb) {
		float r = rgb >> 16 & 0xff;
		float g = rgb >> 8 & 0xff;
		float b = rgb & 0xff;
		float rb = r / b;
		int start = 0;
		int end = blackBodyRGB.length / 3;
		int m = (start + end) / 2;
		start = 0;
		r = blackBodyRGB.length;
		for (m = (start + end) / 2; end - start > 1; m = (start + end) / 2) {
			int m3 = m * 3;
			if (blackBodyRGB[m3] / blackBodyRGB[m3 + 2] > rb)
				start = m;
			else
				end = m;
		}
		return m * 100F + 1000F;
	}

	/**
	 * convert 0xRGB into 0xRRGGBB
	 * 
	 * @param x
	 * @return
	 */
	public static int hex3Tohex6(int x) {
		return (x & 0xF00) << 8 | (x & 0xF00) << 12 | (x & 0xF0) << 4 | (x & 0xF0) << 8
				| (x & 0xF) << 4 | (x & 0xF);
	}

	public static final float blackBodyRGB[] = { 1.0F, 0.0337F, 0.0F, 1.0F, 0.0592F, 0.0F,
			1.0F, 0.0846F, 0.0F, 1.0F, 0.1096F, 0.0F, 1.0F, 0.1341F, 0.0F, 1.0F, 0.1578F,
			0.0F, 1.0F, 0.1806F, 0.0F, 1.0F, 0.2025F, 0.0F, 1.0F, 0.2235F, 0.0F, 1.0F,
			0.2434F, 0.0F, 1.0F, 0.2647F, 0.0033F, 1.0F, 0.2889F, 0.012F, 1.0F, 0.3126F,
			0.0219F, 1.0F, 0.336F, 0.0331F, 1.0F, 0.3589F, 0.0454F, 1.0F, 0.3814F, 0.0588F,
			1.0F, 0.4034F, 0.0734F, 1.0F, 0.425F, 0.0889F, 1.0F, 0.4461F, 0.1054F, 1.0F,
			0.4668F, 0.1229F, 1.0F, 0.487F, 0.1411F, 1.0F, 0.5067F, 0.1602F, 1.0F, 0.5259F,
			0.18F, 1.0F, 0.5447F, 0.2005F, 1.0F, 0.563F, 0.2216F, 1.0F, 0.5809F, 0.2433F,
			1.0F, 0.5983F, 0.2655F, 1.0F, 0.6153F, 0.2881F, 1.0F, 0.6318F, 0.3112F, 1.0F,
			0.648F, 0.3346F, 1.0F, 0.6636F, 0.3583F, 1.0F, 0.6789F, 0.3823F, 1.0F, 0.6938F,
			0.4066F, 1.0F, 0.7083F, 0.431F, 1.0F, 0.7223F, 0.4556F, 1.0F, 0.736F, 0.4803F,
			1.0F, 0.7494F, 0.5051F, 1.0F, 0.7623F, 0.5299F, 1.0F, 0.775F, 0.5548F, 1.0F,
			0.7872F, 0.5797F, 1.0F, 0.7992F, 0.6045F, 1.0F, 0.8108F, 0.6293F, 1.0F, 0.8221F,
			0.6541F, 1.0F, 0.833F, 0.6787F, 1.0F, 0.8437F, 0.7032F, 1.0F, 0.8541F, 0.7277F,
			1.0F, 0.8642F, 0.7519F, 1.0F, 0.874F, 0.776F, 1.0F, 0.8836F, 0.8F, 1.0F, 0.8929F,
			0.8238F, 1.0F, 0.9019F, 0.8473F, 1.0F, 0.9107F, 0.8707F, 1.0F, 0.9193F, 0.8939F,
			1.0F, 0.9276F, 0.9168F, 1.0F, 0.9357F, 0.9396F, 1.0F, 0.9436F, 0.9621F, 1.0F,
			0.9513F, 0.9844F, 0.9937F, 0.9526F, 1.0F, 0.9726F, 0.9395F, 1.0F, 0.9526F, 0.927F,
			1.0F, 0.9337F, 0.915F, 1.0F, 0.9157F, 0.9035F, 1.0F, 0.8986F, 0.8925F, 1.0F,
			0.8823F, 0.8819F, 1.0F, 0.8668F, 0.8718F, 1.0F, 0.852F, 0.8621F, 1.0F, 0.8379F,
			0.8527F, 1.0F, 0.8244F, 0.8437F, 1.0F, 0.8115F, 0.8351F, 1.0F, 0.7992F, 0.8268F,
			1.0F, 0.7874F, 0.8187F, 1.0F, 0.7761F, 0.811F, 1.0F, 0.7652F, 0.8035F, 1.0F,
			0.7548F, 0.7963F, 1.0F, 0.7449F, 0.7894F, 1.0F, 0.7353F, 0.7827F, 1.0F, 0.726F,
			0.7762F, 1.0F, 0.7172F, 0.7699F, 1.0F, 0.7086F, 0.7638F, 1.0F, 0.7004F, 0.7579F,
			1.0F, 0.6925F, 0.7522F, 1.0F, 0.6848F, 0.7467F, 1.0F, 0.6774F, 0.7414F, 1.0F,
			0.6703F, 0.7362F, 1.0F, 0.6635F, 0.7311F, 1.0F, 0.6568F, 0.7263F, 1.0F, 0.6504F,
			0.7215F, 1.0F, 0.6442F, 0.7169F, 1.0F, 0.6382F, 0.7124F, 1.0F, 0.6324F, 0.7081F,
			1.0F, 0.6268F, 0.7039F, 1.0F };

}
