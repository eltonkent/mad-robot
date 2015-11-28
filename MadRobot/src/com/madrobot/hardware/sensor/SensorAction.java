package com.madrobot.hardware.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/**
 * Determine the movement of the sensors using this class. The callbacks are
 * registered using the {@link SensorActionListener} listener.
 * <p>
 * The Accelerometer sensor is used to determine the flip and shake and the
 * Orientation sensor is used to determine the tilt. </br/> <b>Implement a
 * SensorActionListener</b><br/>
 * 
 * <pre>
 * <code>
 *  SensorActionListener actionListener=new SensorActionListener() {
 * 		
 * 		@Override
 * 		public boolean onTilt(SensorEvent event, int direction) {
 * 		//return true if TILT was consumed
 * 			return false;
 * 		}
 * 		
 * 		@Override
 * 		public boolean onShake(SensorEvent event, float speed) {
 * 			//return true if SHAKE was consumed
 * 			return false;
 * 		}
 * 		
 * 		@Override
 * 		public boolean onLongFlip(SensorEvent event) {
 * 			//return true if LONG FLIP was consumed
 * 			return false;
 * 		}
 * 		
 * 		@Override
 * 		public boolean onFlip(SensorEvent event) {
 * 			//return true if FLIP was consumed
 * 			return false;
 * 		}
 * 		
 * 		@Override
 * 		public boolean onFaceUp(SensorEvent event) {
 * 			return false;
 * 		}
 * 		
 * 		@Override
 * 		public boolean onFaceDown(SensorEvent event) {
 * 			return false;
 * 		}
 * 	};
 * </code>
 * </pre>
 * 
 * <b>Register the listener with the SensorAction instance</b><br/>
 * 
 * <pre>
 * <code>
 * SensorAction action=new SensorAction(actionListener);
 * </code>
 * </pre>
 * 
 * <b>Determine the sensor action by the values returned by the sensor
 * action.</b>
 * 
 * <pre>
 *  <code>
 * 	SensorManager mgr=(SensorManager)ctxt.getSystemService(Context.SENSOR_SERVICE);
 *  mgr.registerListener(listener,
 *  mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 *  SensorManager.SENSOR_DELAY_UI);
 *  
 *  private SensorEventListener listener=new SensorEventListener() {
 *         public void onSensorChanged(SensorEvent e) {
 *          </code>
 * </pre>
 * 
 * <b>action.determineSensorAction(e);</b> <code>
 * 			<pre>
 *  		..
 *  		..
 *  		 }
 *   		}
 * 			</code> </pre>
 * 
 * </p>
 * 
 * @author elton.stephen.kent
 * 
 */
public class SensorAction {

	private static final int TIME_THRESHOLD = 100;

	private static final int FLIP_HOLD_THRESHOLD = 800;

	private static final int SPEED_THRESHOLD = 800; // 350

	private static final int SHAKE_TIMEOUT = 500;
	private static final int SHAKE_DURATION = 1000;
	private static final int SHAKE_COUNT = 3;

	private float lastX = -0.0f;

	private float lastY = -0.0f;

	private float lastZ = -0.0f;

	private long lastTime;

	private int shakeCount = 0;

	private long lastShake;

	private long lastShakeUpdate;

	private SensorActionListener sal;

	private float lastRoll;

	private float lastPitch;

	private long lastFaceUpUpdate;

	private boolean faceUp = true;

	private boolean faceDown;

	private boolean flipped;

	public SensorAction(SensorActionListener sal) {
		this.sal = sal;
	}

	private void detectFlip(long now, long diff, float x, float y, float z, SensorEvent event) {

		if (diff < TIME_THRESHOLD) {
			return;
		}

		// if (Debug.DEBUG) {
		// Log.d(TAG, "*** detectFlip  elapse: " + diff + " lastTime: "
		// + lastTime + " now: " + now + "  x: " + x + " y: " + y
		// + " z: " + z);
		// }

		// flip
		if (z >= 0) {
			sal.onFaceUp(event);

			if (flipped) {
				sal.onFlip(event);
				flipped = false;
			}

			//
			faceUp = true;
			faceDown = false;
			lastFaceUpUpdate = now;
		} else if (z < 0) {
			sal.onFaceDown(event);

			flipped = flipped || faceUp;

			if (flipped && now - lastFaceUpUpdate > FLIP_HOLD_THRESHOLD) {
				sal.onLongFlip(event);
				flipped = false;
			}

			//
			faceDown = true;
			faceUp = false;
		}

		lastTime = now;
	}

	public void determineSensorAction(SensorEvent event) {
		final long now = System.currentTimeMillis();
		final long diff = (now - lastTime);

		final float[] values = event.values;

		final float x = values[SensorManager.DATA_X];
		final float y = values[SensorManager.DATA_Y];
		final float z = values[SensorManager.DATA_Z];

		final int type = event.sensor.getType();

		switch (type) {
		case Sensor.TYPE_ACCELEROMETER:
			detectFlip(now, diff, x, y, z, event);
			detectShake(now, diff, x, y, z, event);
			break;
		case Sensor.TYPE_ORIENTATION:
			detectTilt(now, diff, x, y, z, event);
			break;
		}
	}

	private void detectShake(long now, long diff, float x, float y, float z, SensorEvent event) {

		if (diff < TIME_THRESHOLD) {
			return;
		}

		// if (Debug.DEBUG) {
		// Log.d(TAG, "*** detectShake  elapse: " + diff + " lastTime: "
		// + lastTime + " now: " + now + "  x: " + x + " y: " + y
		// + " z: " + z);
		// }

		if ((now - lastShakeUpdate) > SHAKE_TIMEOUT) {
			shakeCount = 0;
		}

		final float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diff * 10000;
		//
		if (speed > SPEED_THRESHOLD) {
			if ((++shakeCount >= SHAKE_COUNT) && (now - lastShake > SHAKE_DURATION)) {
				lastShake = now;
				shakeCount = 0;
				//
				sal.onShake(event, speed);
			}

			lastShakeUpdate = now;
		}

		//
		lastX = x;
		lastY = y;
		lastZ = z;

		lastTime = now;
	}

	private void detectTilt(long now, long diff, float azimuth, float pitch, float roll,
			SensorEvent event) {

		final float resetThreshold = 3.0f;

		if (pitch > -resetThreshold && pitch < resetThreshold) {
			lastPitch = pitch;
			return;
		}

		if (roll > -resetThreshold && roll < resetThreshold) {
			lastRoll = roll;
			return;
		}

		if (diff < TIME_THRESHOLD) {
			return;
		}

		// if (Debug.DEBUG) {
		// Log.d(TAG, " @@@@@@@@@@@@@@@ detectTilt " +
		// azimuth+":"+pitch+":"+roll);
		// }

		final float threshold = 25.0f;

		if (pitch - lastPitch > threshold) {
			sal.onTilt(event, SensorActionListener.DIRECTION_DOWN);
			return;
		}

		if (pitch - lastPitch < -threshold) {
			sal.onTilt(event, SensorActionListener.DIRECTION_UP);
			return;
		}

		if (roll - lastRoll > threshold) {
			sal.onTilt(event, SensorActionListener.DIRECTION_LEFT);
			return;
		}

		if (roll - lastRoll < -threshold) {
			sal.onTilt(event, SensorActionListener.DIRECTION_RIGHT);
			return;
		}

		lastTime = now;
	}

}
