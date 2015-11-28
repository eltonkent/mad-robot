package com.madrobot.graphics.bitmap;

public class TransformFilters {

	/**
	 * Treat pixels off the edge as zero.
	 */
	public final static int EDGE_ACTION_ZERO = 0;

	/**
	 * Clamp pixels to the image edges.
	 */
	public final static int EDGE_ACTION_CLAMP = 1;

	/**
	 * Wrap pixels off the edge onto the oppsoite edge.
	 */
	public final static int EDGE_ACTION_WRAP = 2;

	/**
	 * Clamp pixels RGB to the image edges, but zero the alpha. This prevents
	 * gray borders on your image.
	 */
	public final static int EDGE_ACTION_RGB_CLAMP = 3;

	/**
	 * Use nearest-neighbout interpolation.
	 */
	public final static int INTERPOLATION_NEAREST_NEIGHBOUR = 0;

	/**
	 * Use bilinear interpolation.
	 */
	public final static int INTERPOLATION_BILINEAR = 1;

	/**
	 * The action to take for pixels off the image edge.
	 */
	protected int edgeAction = EDGE_ACTION_RGB_CLAMP;

	static int getPixel(int[] pixels, int x, int y, int width, int height, int edgeAction) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			switch (edgeAction) {
			case EDGE_ACTION_ZERO:
			default:
				return 0;
			case EDGE_ACTION_WRAP:
				return pixels[(ImageMath.mod(y, height) * width) + ImageMath.mod(x, width)];
			case EDGE_ACTION_CLAMP:
				return pixels[(ImageMath.clamp(y, 0, height - 1) * width)
						+ ImageMath.clamp(x, 0, width - 1)];
			case EDGE_ACTION_RGB_CLAMP:
				return pixels[(ImageMath.clamp(y, 0, height - 1) * width)
						+ ImageMath.clamp(x, 0, width - 1)] & 0x00ffffff;
			}
		}
		return pixels[y * width + x];
	}
}
