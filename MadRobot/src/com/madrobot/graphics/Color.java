package com.madrobot.graphics;

public class Color {
	public int argb;

	public Color(int argb) {
		this.argb = argb;
	}

	public int getRed() {
		return android.graphics.Color.red(argb);
	}

	public int getGreen() {
		return android.graphics.Color.green(argb);
	}

	public int getBlue() {
		return android.graphics.Color.blue(argb);
	}

	public void setColor(int alpha, int red, int green, int blue) {
		argb = android.graphics.Color.argb(alpha, red, green, blue);
	}

	public void getHSVColor(float[] hsv) {
		android.graphics.Color.RGBToHSV(getRed(), getGreen(), getBlue(), hsv);
	}

}
