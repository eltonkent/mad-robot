package com.madrobot.ui.anim;

import android.view.animation.Interpolator;

public class BounceInterpolator implements Interpolator {

	@Override
	public float getInterpolation(float t) {
		return 1.0F - doInterpolation(t);
	}

	private float doInterpolation(float f) {
		if (f < 0.36363636363636365D)
			return 7.5625F * (f = (float) (f - 0.18181818181818182D)) * f * 4F;
		if (f < 0.72727272727272729D)
			return 7.5625F * (f = (float) (f - 0.54545454545454541D)) * f + 0.75F;
		if (f < 0.90909090909090917D)
			return 7.5625F * (f = (float) (f - 0.81818181818181823D)) * f + 0.9375F;
		else
			return 7.5625F * (f = (float) (f - 0.95454545454545459D)) * f + 0.984375F;
	}
}
