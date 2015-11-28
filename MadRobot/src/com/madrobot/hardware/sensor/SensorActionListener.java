package com.madrobot.hardware.sensor;

import android.hardware.SensorEvent;

public interface SensorActionListener {
	public int DIRECTION_LEFT = 1;
	public int DIRECTION_RIGHT = 2;
	public int DIRECTION_UP = 3;
	public int DIRECTION_DOWN = 4;

	public boolean onTilt(SensorEvent event, int direction);

	public boolean onFlip(SensorEvent event);

	public boolean onLongFlip(SensorEvent event);

	public boolean onShake(SensorEvent event, float speed);

	public boolean onFaceUp(SensorEvent event);

	public boolean onFaceDown(SensorEvent event);
}
