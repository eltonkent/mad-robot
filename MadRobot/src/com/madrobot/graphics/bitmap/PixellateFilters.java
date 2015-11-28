package com.madrobot.graphics.bitmap;

import java.util.Random;

import android.graphics.Bitmap;

public class PixellateFilters {

	private static class Point {
		int index;
		float x, y;
		float dx, dy;
		float cubeX, cubeY;
		float distance;
	}

	private static float[] coefficients = { 1, 0, 0, 0 };
	private static float angleCoefficient;
	private static float gradientCoefficient;

	/**
	 * Random pixellate shape
	 */
	public final static int GRID_TYPE_RANDOM = 0;
	public final static int GRID_TYPE_SQUARE = 1;
	public final static int GRID_TYPE_HEXAGONAL = 2;
	public final static int GRID_TYPE_OCTAGONAL = 3;
	public final static int GRID_TYPE_TRIANGULAR = 4;

	public static final Bitmap crystallize(Bitmap bitmap, int gridType, boolean fadeEdges,
			float angle, float scale, float stretch, float randomNess, float edgeThickness,
			int edgeColor, Bitmap.Config outputConfig) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		float m00 = cos;
		float m01 = sin;
		float m10 = -sin;
		float m11 = cos;
		Point[] results = new Point[3];
		int[] argb = BitmapUtils.getPixels(bitmap);
		int[] outPixels = new int[width * height];
		for (int j = 0; j < results.length; j++)
			results[j] = new Point();
		int index = 0;
		Random random = new Random();

		byte[] probabilities = new byte[8192];
		float factorial = 1;
		float total = 0;
		float mean = 2.5f;
		for (int i = 0; i < 10; i++) {
			if (i > 1)
				factorial *= i;
			float probability = (float) Math.pow(mean, i) * (float) Math.exp(-mean)
					/ factorial;
			int start = (int) (total * 8192);
			total += probability;
			int end = (int) (total * 8192);
			for (int j = start; j < end; j++)
				probabilities[j] = (byte) i;

		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float nx = m00 * x + m01 * y;
				float ny = m10 * x + m11 * y;
				nx /= scale;
				ny /= scale * stretch;
				nx += 1000;
				ny += 1000; // Reduce artifacts around 0,0
				float f = evaluate(nx, ny, results, random, gridType, probabilities,
						randomNess);

				float f1 = results[0].distance;
				float f2 = results[1].distance;
				int srcx = ImageMath
						.clamp((int) ((results[0].x - 1000) * scale), 0, width - 1);
				int srcy = ImageMath.clamp((int) ((results[0].y - 1000) * scale), 0,
						height - 1);
				int v = argb[srcy * width + srcx];
				f = (f2 - f1) / edgeThickness;
				f = ImageMath.smoothStep(0, edgeThickness, f);
				if (fadeEdges) {
					srcx = ImageMath
							.clamp((int) ((results[1].x - 1000) * scale), 0, width - 1);
					srcy = ImageMath.clamp((int) ((results[1].y - 1000) * scale), 0,
							height - 1);
					int v2 = argb[srcy * width + srcx];
					v2 = ImageMath.mixColors(0.5f, v2, v);
					v = ImageMath.mixColors(f, v2, v);
				} else
					v = ImageMath.mixColors(f, edgeColor, v);

				outPixels[index++] = v;
			}
		}

		return Bitmap.createBitmap(outPixels, width, height, outputConfig);
	}

	private static float evaluate(float x, float y, Point[] results, Random random,
			int gridType, byte[] probabilities, float randomNess) {
		for (int j = 0; j < results.length; j++)
			results[j].distance = Float.POSITIVE_INFINITY;

		int ix = (int) x;
		int iy = (int) y;
		float fx = x - ix;
		float fy = y - iy;

		float d = checkCube(fx, fy, ix, iy, results, random, gridType, probabilities,
				randomNess);
		if (d > fy)
			d = checkCube(fx, fy + 1, ix, iy - 1, results, random, gridType, probabilities,
					randomNess);
		if (d > 1 - fy)
			d = checkCube(fx, fy - 1, ix, iy + 1, results, random, gridType, probabilities,
					randomNess);
		if (d > fx) {
			checkCube(fx + 1, fy, ix - 1, iy, results, random, gridType, probabilities,
					randomNess);
			if (d > fy)
				d = checkCube(fx + 1, fy + 1, ix - 1, iy - 1, results, random, gridType,
						probabilities, randomNess);
			if (d > 1 - fy)
				d = checkCube(fx + 1, fy - 1, ix - 1, iy + 1, results, random, gridType,
						probabilities, randomNess);
		}
		if (d > 1 - fx) {
			d = checkCube(fx - 1, fy, ix + 1, iy, results, random, gridType, probabilities,
					randomNess);
			if (d > fy)
				d = checkCube(fx - 1, fy + 1, ix + 1, iy - 1, results, random, gridType,
						probabilities, randomNess);
			if (d > 1 - fy)
				d = checkCube(fx - 1, fy - 1, ix + 1, iy + 1, results, random, gridType,
						probabilities, randomNess);
		}

		float t = 0;
		for (int i = 0; i < 3; i++)
			t += coefficients[i] * results[i].distance;
		if (angleCoefficient != 0) {
			float angle = (float) Math.atan2(y - results[0].y, x - results[0].x);
			if (angle < 0)
				angle += 2 * (float) Math.PI;
			angle /= 4 * (float) Math.PI;
			t += angleCoefficient * angle;
		}
		if (gradientCoefficient != 0) {
			float a = 1 / (results[0].dy + results[0].dx);
			t += gradientCoefficient * a;
		}
		return t;
	}

	private static float checkCube(float x, float y, int cubeX, int cubeY, Point[] results,
			Random random, int gridType, byte[] probabilities, float randomness) {
		int numPoints;
		float distancePower = 2;
		random.setSeed(571 * cubeX + 23 * cubeY);
		switch (gridType) {
		case GRID_TYPE_RANDOM:
		default:
			numPoints = probabilities[random.nextInt() & 0x1fff];
			break;
		case GRID_TYPE_SQUARE:
			numPoints = 1;
			break;
		case GRID_TYPE_HEXAGONAL:
			numPoints = 1;
			break;
		case GRID_TYPE_OCTAGONAL:
			numPoints = 2;
			break;
		case GRID_TYPE_TRIANGULAR:
			numPoints = 2;
			break;
		}
		for (int i = 0; i < numPoints; i++) {
			float px = 0, py = 0;
			float weight = 1.0f;
			switch (gridType) {
			case GRID_TYPE_RANDOM:
				px = random.nextFloat();
				py = random.nextFloat();
				break;
			case GRID_TYPE_SQUARE:
				px = py = 0.5f;
				if (randomness != 0) {
					px += randomness * (random.nextFloat() - 0.5);
					py += randomness * (random.nextFloat() - 0.5);
				}
				break;
			case GRID_TYPE_HEXAGONAL:
				if ((cubeX & 1) == 0) {
					px = 0.75f;
					py = 0;
				} else {
					px = 0.75f;
					py = 0.5f;
				}
				if (randomness != 0) {
					px += randomness * Noise.noise2(271 * (cubeX + px), 271 * (cubeY + py));
					py += randomness
							* Noise.noise2(271 * (cubeX + px) + 89, 271 * (cubeY + py) + 137);
				}
				break;
			case GRID_TYPE_OCTAGONAL:
				switch (i) {
				case 0:
					px = 0.207f;
					py = 0.207f;
					break;
				case 1:
					px = 0.707f;
					py = 0.707f;
					weight = 1.6f;
					break;
				}
				if (randomness != 0) {
					px += randomness * Noise.noise2(271 * (cubeX + px), 271 * (cubeY + py));
					py += randomness
							* Noise.noise2(271 * (cubeX + px) + 89, 271 * (cubeY + py) + 137);
				}
				break;
			case GRID_TYPE_TRIANGULAR:
				if ((cubeY & 1) == 0) {
					if (i == 0) {
						px = 0.25f;
						py = 0.35f;
					} else {
						px = 0.75f;
						py = 0.65f;
					}
				} else {
					if (i == 0) {
						px = 0.75f;
						py = 0.35f;
					} else {
						px = 0.25f;
						py = 0.65f;
					}
				}
				if (randomness != 0) {
					px += randomness * Noise.noise2(271 * (cubeX + px), 271 * (cubeY + py));
					py += randomness
							* Noise.noise2(271 * (cubeX + px) + 89, 271 * (cubeY + py) + 137);
				}
				break;
			}
			float dx = Math.abs(x - px);
			float dy = Math.abs(y - py);
			float d;
			dx *= weight;
			dy *= weight;
			if (distancePower == 1.0f)
				d = dx + dy;
			else if (distancePower == 2.0f)
				d = (float) Math.sqrt(dx * dx + dy * dy);
			else
				d = (float) Math.pow(
						(float) Math.pow(dx, distancePower)
								+ (float) Math.pow(dy, distancePower), 1 / distancePower);

			// Insertion sort the long way round to speed it up a bit
			if (d < results[0].distance) {
				Point p = results[2];
				results[2] = results[1];
				results[1] = results[0];
				results[0] = p;
				p.distance = d;
				p.dx = dx;
				p.dy = dy;
				p.x = cubeX + px;
				p.y = cubeY + py;
			} else if (d < results[1].distance) {
				Point p = results[2];
				results[2] = results[1];
				results[1] = p;
				p.distance = d;
				p.dx = dx;
				p.dy = dy;
				p.x = cubeX + px;
				p.y = cubeY + py;
			} else if (d < results[2].distance) {
				Point p = results[2];
				p.distance = d;
				p.dx = dx;
				p.dy = dy;
				p.x = cubeX + px;
				p.y = cubeY + py;
			}
		}
		return results[2].distance;
	}
}
