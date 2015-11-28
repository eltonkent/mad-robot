package com.madrobot.hardware.sensor;

import android.hardware.SensorManager;

public class SensorUtils {

	/**
	 * Get the force from a ACCELEROMETER sensor when the device is swung or any
	 * similar movement
	 * 
	 * @param values
	 *            the values given by the accelerometer sensor in the
	 *            SensorEvent object.
	 * @return
	 * @see android.hardware.SensorEvent#values
	 */
	public static double getForce(float[] values) {
		double netForce = values[0] * values[0];
		netForce += values[1] * values[1]; // Y axis
		netForce += (values[2]) * (values[2]); // Z axis (upwards)
		netForce = Math.sqrt(netForce) - SensorManager.GRAVITY_EARTH;
		return netForce;
	}
}
