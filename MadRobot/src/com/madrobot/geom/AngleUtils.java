package com.madrobot.geom;

public class AngleUtils {

	/**
	 * Get the distance between two angles
	 * 
	 * @param angleA
	 * 
	 * @param angleB
	 * @return The distance between angles a and b
	 */
	public static float angleDistance(float angleA, float angleB) {
		float diff = Math.abs(angleA - angleB);
		if (diff > Math.PI) {
			diff = (float) (2 * Math.PI - diff);
		}

		return diff;
	}

	public static float convertToDegrees(float rad) {
		return (float) ((rad / (2 * Math.PI)) * 360);
	}

	public static float convertToRadians(float degrees) {
		return (float) ((degrees / 360) * 2 * Math.PI);
	}

	/**
	 * Check if angle <code>n</code> is between <code>angleA</code> and
	 * <code>angleB</code>
	 * <p>
	 * 
	 * @param n
	 *            angle to find
	 * @param angleA
	 *            first angle
	 * @param angleB
	 *            second angle
	 * @return true if <code>n</code> is between angle<code> a</code> and angle
	 *         <code>b</code>
	 */
	public static boolean isAngleBetween(float n, float angleA, float angleB) {
		n = (360 + (n % 360)) % 360;
		angleA = (3600000 + angleA) % 360;
		angleB = (3600000 + angleB) % 360;

		if (angleA < angleB)
			return angleA <= n && n <= angleB;
		return 0 <= n && n <= angleB || angleA <= n && n < 360;
	}

	/**
	 * Get the angle of the point in the given area marked by the provided width
	 * and height (theta).
	 * <p>
	 * Given a area(width,height), this method returns the angle at which the
	 * point(x,y) exists from the center of the area.
	 * </p>
	 * 
	 * @param x
	 *            point x
	 * @param y
	 *            point
	 * @param width
	 * @param height
	 * @return angle of <code>x,y</code>
	 */
	public static float getAngleOfPoint(float x, float y, float width, float height) {
		float sx = x - (width / 2.0f);
		float sy = y - (height / 2.0f);
		float length = (float) Math.sqrt(sx * sx + sy * sy);
		float nx = sx / length;
		float ny = sy / length;
		float theta = (float) Math.atan2(ny, nx);
		final float rad2deg = (float) (180.0 / Math.PI);
		float theta2 = theta * rad2deg;
		return (theta2 < 0) ? theta2 + 360.0f : theta2;
	}
}
