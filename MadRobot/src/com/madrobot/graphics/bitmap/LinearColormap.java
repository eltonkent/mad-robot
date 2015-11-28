package com.madrobot.graphics.bitmap;

/**
 * A colormap which interpolates linearly between two colors.
 */
class LinearColormap implements Colormap {

	private int color1;
	private int color2;

	/**
	 * Construct a color map with a grayscale ramp from black to white.
	 */
	LinearColormap() {
		this(0xff000000, 0xffffffff);
	}

	/**
	 * Construct a linear color map.
	 * 
	 * @param color1
	 *            the color corresponding to value 0 in the colormap
	 * @param color2
	 *            the color corresponding to value 1 in the colormap
	 */
	LinearColormap(int color1, int color2) {
		this.color1 = color1;
		this.color2 = color2;
	}

	/**
	 * Set the first color.
	 * 
	 * @param color1
	 *            the color corresponding to value 0 in the colormap
	 */
	void setColor1(int color1) {
		this.color1 = color1;
	}

	/**
	 * Get the first color.
	 * 
	 * @return the color corresponding to value 0 in the colormap
	 */
	int getColor1() {
		return color1;
	}

	/**
	 * Set the second color.
	 * 
	 * @param color2
	 *            the color corresponding to value 1 in the colormap
	 */
	void setColor2(int color2) {
		this.color2 = color2;
	}

	/**
	 * Get the second color.
	 * 
	 * @return the color corresponding to value 1 in the colormap
	 */
	int getColor2() {
		return color2;
	}

	/**
	 * Convert a value in the range 0..1 to an RGB color.
	 * 
	 * @param v
	 *            a value in the range 0..1
	 * @return an RGB color
	 */
	@Override
	public int getColor(float v) {
		return ImageMath.mixColors(ImageMath.clamp(v, 0, 1.0f), color1, color2);
	}

}
