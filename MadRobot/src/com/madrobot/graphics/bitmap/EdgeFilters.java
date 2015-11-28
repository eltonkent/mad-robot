package com.madrobot.graphics.bitmap;

import android.graphics.Bitmap;

import com.madrobot.graphics.ColorUtils;

/**
 * Edge detection filters
 * <p>
 * <b>Edge Detection</b><br/>
 * <img src="../../../../resources/wholeimage_edge.png" ><br/>
 * 
 * <b>La place Edge Detection</b><br/>
 * <img src="../../../../resources/laplaceedge.png" ><br/>
 * 
 * <b>Simple Emboss</b><br/>
 * <img src="../../../../resources/simpleEmboss.png" ><br/>
 * <table>
 * <tr>
 * <th>Emboss with <code>emboss</code> set to false and <code>bumpHeight</code>
 * of 3</th>
 * <th>Emboss with <code>emboss</code> set to true and <code>bumpHeight</code>
 * of 3</th>
 * </tr>
 * <tr>
 * <td><img src="../../../../resources/emboss_false.png" ></td>
 * <td><img src="../../../../resources/emboss_true.png" ></td>
 * </tr>
 * </table>
 * </p>
 * 
 */
public class EdgeFilters {

	private final static float R2 = (float) Math.sqrt(2);
	public static float[] FREI_CHEN_H = { -1, -R2, -1, 0, 0, 0, 1, R2, 1, };

	public final static float[] FREI_CHEN_V = { -1, 0, 1, -R2, 0, R2, -1, 0, 1, };

	public final static float[] PREWITT_H = { -1, -1, -1, 0, 0, 0, 1, 1, 1, };

	public final static float[] PREWITT_V = { -1, 0, 1, -1, 0, 1, -1, 0, 1, };

	public final static float[] ROBERTS_H = { -1, 0, 0, 0, 1, 0, 0, 0, 0, };

	public final static float[] ROBERTS_V = { 0, 0, -1, 0, 1, 0, 0, 0, 0, };
	public static float[] SOBEL_H = { -1, -2, -1, 0, 0, 0, 1, 2, 1, };
	public final static float[] SOBEL_V = { -1, 0, 1, -2, 0, 2, -1, 0, 1, };

	/**
	 * An edge-detection filter.
	 * 
	 * @param src
	 * @param vEdgeMatrix
	 *            can be {@link #ROBERTS_V}, {@link #PREWITT_V},{@link #SOBEL_V}
	 *            or {@link #FREI_CHEN_V}. recommended: {@link #SOBEL_V}
	 * @param hEdgeMatrix
	 *            can be {@link #ROBERTS_H}, {@link #PREWITT_H},{@link #SOBEL_H}
	 *            or {@link #FREI_CHEN_H}. recommended: {@link #SOBEL_H}
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap detectEdge(Bitmap src, float[] vEdgeMatrix, float[] hEdgeMatrix,
			Bitmap.Config outputConfig) {
		int index = 0;
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int r = 0, g = 0, b = 0;
				int rh = 0, gh = 0, bh = 0;
				int rv = 0, gv = 0, bv = 0;
				int a = inPixels[y * width + x] & 0xff000000;

				for (int row = -1; row <= 1; row++) {
					int iy = y + row;
					int ioffset;
					if (0 <= iy && iy < height)
						ioffset = iy * width;
					else
						ioffset = y * width;
					int moffset = 3 * (row + 1) + 1;
					for (int col = -1; col <= 1; col++) {
						int ix = x + col;
						if (!(0 <= ix && ix < width))
							ix = x;
						int rgb = inPixels[ioffset + ix];
						float h = hEdgeMatrix[moffset + col];
						float v = vEdgeMatrix[moffset + col];

						r = (rgb & 0xff0000) >> 16;
						g = (rgb & 0x00ff00) >> 8;
						b = rgb & 0x0000ff;
						rh += (int) (h * r);
						gh += (int) (h * g);
						bh += (int) (h * b);
						rv += (int) (v * r);
						gv += (int) (v * g);
						bv += (int) (v * b);
					}
				}
				r = (int) (Math.sqrt(rh * rh + rv * rv) / 1.8);
				g = (int) (Math.sqrt(gh * gh + gv * gv) / 1.8);
				b = (int) (Math.sqrt(bh * bh + bv * bv) / 1.8);
				r = ColorUtils.clamp(r);
				g = ColorUtils.clamp(g);
				b = ColorUtils.clamp(b);
				outPixels[index++] = a | (r << 16) | (g << 8) | b;
			}

		}
		return Bitmap.createBitmap(outPixels, src.getWidth(), src.getHeight(), outputConfig);
	}

	/**
	 * Emboss the given bitmap
	 * 
	 * @param src
	 * @param lightDirection
	 *            Direction of the light. min:0 max:360
	 * @param lightElevation
	 *            brightness. min:0 max:90
	 * 
	 * @param bumpHeight
	 *            Height of the emobssed parts of the image. min:0 max:100
	 * @param emboss
	 *            if true, the bitmap is embossed retaining its color. else a
	 *            grayscale image is embossed.
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap emboss(Bitmap src, float lightDirection, float lightElevation,
			float bumpHeight, boolean emboss, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		int index = 0;
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] outPixels = new int[inPixels.length];
		int[] bumpPixels;
		int bumpMapWidth, bumpMapHeight;
		float pixelScale = 255.9f;
		float width45 = 3 * bumpHeight;

		bumpMapWidth = width;
		bumpMapHeight = height;
		bumpPixels = new int[bumpMapWidth * bumpMapHeight];
		for (int i = 0; i < inPixels.length; i++)
			bumpPixels[i] = ColorUtils.brightness(inPixels[i]);

		int Nx, Ny, Nz, Lx, Ly, Lz, Nz2, NzLz, NdotL;
		int shade, background;

		Lx = (int) (Math.cos(lightDirection) * Math.cos(lightElevation) * pixelScale);
		Ly = (int) (Math.sin(lightDirection) * Math.cos(lightElevation) * pixelScale);
		Lz = (int) (Math.sin(lightElevation) * pixelScale);

		Nz = (int) (6 * 255 / width45);
		Nz2 = Nz * Nz;
		NzLz = Nz * Lz;

		background = Lz;

		int bumpIndex = 0;

		for (int y = 0; y < height; y++, bumpIndex += bumpMapWidth) {
			int s1 = bumpIndex;
			int s2 = s1 + bumpMapWidth;
			int s3 = s2 + bumpMapWidth;

			for (int x = 0; x < width; x++, s1++, s2++, s3++) {
				if (y != 0 && y < height - 2 && x != 0 && x < width - 2) {
					Nx = bumpPixels[s1 - 1] + bumpPixels[s2 - 1] + bumpPixels[s3 - 1]
							- bumpPixels[s1 + 1] - bumpPixels[s2 + 1] - bumpPixels[s3 + 1];
					Ny = bumpPixels[s3 - 1] + bumpPixels[s3] + bumpPixels[s3 + 1]
							- bumpPixels[s1 - 1] - bumpPixels[s1] - bumpPixels[s1 + 1];

					if (Nx == 0 && Ny == 0)
						shade = background;
					else if ((NdotL = Nx * Lx + Ny * Ly + NzLz) < 0)
						shade = 0;
					else
						shade = (int) (NdotL / Math.sqrt(Nx * Nx + Ny * Ny + Nz2));
				} else
					shade = background;

				if (emboss) {
					int rgb = inPixels[index];
					int a = rgb & 0xff000000;
					int r = (rgb >> 16) & 0xff;
					int g = (rgb >> 8) & 0xff;
					int b = rgb & 0xff;
					r = (r * shade) >> 8;
					g = (g * shade) >> 8;
					b = (b * shade) >> 8;
					outPixels[index++] = a | (r << 16) | (g << 8) | b;
				} else
					outPixels[index++] = 0xff000000 | (shade << 16) | (shade << 8) | shade;
			}
		}
		return Bitmap.createBitmap(outPixels, src.getWidth(), src.getHeight(), outputConfig);
	}

	/**
	 * Apply emboss filter to the given image data
	 * 
	 * @param bitmap
	 *            of the image
	 * @param outputConfig
	 *            Bitmap configuration of the output bitmap
	 * @return
	 */
	public static Bitmap simpleEmboss(Bitmap bitmap, Bitmap.Config outputConfig) {

		byte[][] filter = { { -2, 0, 0 }, { 0, 1, 0 }, { 0, 0, 2 } };
		return BitmapFilters.applyFilter(bitmap, 100, filter, outputConfig);
	}

	/**
	 * A simple embossing filter.
	 * 
	 * @param src
	 * @param edgeAction
	 *            use the EdgeAction constants defined in {@link BitmapFilters}
	 *            . Recommended: {@link BitmapFilters#CLAMP_EDGES}
	 * @param processAlpha
	 * @param premultiplyAlpha
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap bump(Bitmap src, int edgeAction, boolean processAlpha,
			boolean premultiplyAlpha, Bitmap.Config outputConfig) {
		float[] embossMatrix = { -1.0f, -1.0f, 0.0f, -1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f };
		return ConvolveUtils.doConvolve(embossMatrix, src, edgeAction, processAlpha,
				premultiplyAlpha, outputConfig);
	}

	private static void brightness(int[] row) {
		for (int i = 0; i < row.length; i++) {
			int rgb = row[i];
			int r = rgb >> 16 & 0xff;
			int g = rgb >> 8 & 0xff;
			int b = rgb & 0xff;
			row[i] = (r + g + b) / 3;
		}
	}

	/**
	 * Edge detection using the Laplacian operator
	 * 
	 * @param src
	 * @param outputConfig
	 * @return
	 */
	public static Bitmap laPlaceEdge(Bitmap src, Bitmap.Config outputConfig) {
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = BitmapUtils.getPixels(src);
		int[] row1 = new int[width];
		int[] row2 = new int[width];
		int[] row3 = new int[width];
		int[] pixels = new int[width];
		src.getPixels(row1, 0, width, 0, 0, width, 1);
		src.getPixels(row2, 0, width, 0, 1, width, 1);

		brightness(row1);
		brightness(row2);
		for (int y = 0; y < height; y++) {
			if (y < height - 1) {
				src.getPixels(row3, 0, width, 0, y + 1, width, 1);// getRGB(src,
																	// 0, y + 1,
																	// width, 1,
																	// row3);
				brightness(row3);
			}
			pixels[0] = pixels[width - 1] = 0xff000000;// FIXME
			for (int x = 1; x < width - 1; x++) {
				int l1 = row2[x - 1];
				int l2 = row1[x];
				int l3 = row3[x];
				int l4 = row2[x + 1];

				int l = row2[x];
				int max = Math.max(Math.max(l1, l2), Math.max(l3, l4));
				int min = Math.min(Math.min(l1, l2), Math.min(l3, l4));

				int gradient = (int) (0.5f * Math.max((max - l), (l - min)));

				int r = ((row1[x - 1] + row1[x] + row1[x + 1] + row2[x - 1] - (8 * row2[x])
						+ row2[x + 1] + row3[x - 1] + row3[x] + row3[x + 1]) > 0) ? gradient
						: (128 + gradient);
				pixels[x] = r;
			}
			BitmapUtils.setPixelRow(pixels, y, width, inPixels);
			// setRGB(dst, 0, y, width, 1, pixels);
			int[] t = row1;
			row1 = row2;
			row2 = row3;
			row3 = t;
		}

		row1 = BitmapUtils.getPixelRow(inPixels, 0, 0, width);// getRGB(dst, 0,
																// 0, width, 1,
																// row1);
		row2 = BitmapUtils.getPixelRow(inPixels, 0, 1, width);// getRGB(dst, 0,
																// 0, width, 1,
																// row2);
		for (int y = 0; y < height; y++) {
			if (y < height - 1) {
				row3 = BitmapUtils.getPixelRow(inPixels, 0, y + 1, width);// getRGB(dst,
																			// 0,
																			// y
																			// +
																			// 1,
																			// width,
																			// 1,
																			// row3);
			}
			pixels[0] = pixels[width - 1] = 0xff000000;// FIXME
			for (int x = 1; x < width - 1; x++) {
				int r = row2[x];
				r = (((r <= 128) && ((row1[x - 1] > 128) || (row1[x] > 128)
						|| (row1[x + 1] > 128) || (row2[x - 1] > 128) || (row2[x + 1] > 128)
						|| (row3[x - 1] > 128) || (row3[x] > 128) || (row3[x + 1] > 128))) ? ((r >= 128) ? (r - 128)
						: r)
						: 0);

				pixels[x] = 0xff000000 | (r << 16) | (r << 8) | r;
			}
			// setRGB(dst, 0, y, width, 1, pixels);
			BitmapUtils.setPixelRow(pixels, y, width, inPixels);
			int[] t = row1;
			row1 = row2;
			row2 = row3;
			row3 = t;
		}

		return Bitmap.createBitmap(inPixels, width, height, outputConfig);
	}

	// public static Bitmap chrome(Bitmap src, Config outputConfig) {
	//
	// }
}
