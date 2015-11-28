package com.madrobot.ui.anim;

import android.view.animation.TranslateAnimation;

public class FloorBounceAnimation extends TranslateAnimation {

	public FloorBounceAnimation(float height) {
		this(height, 0);
	}

	public FloorBounceAnimation(float height, int dimensionType) {
		super(0, 0.0F, 0, 0.0F, 0, 0.0F, dimensionType, -height);
		setInterpolator(new BounceInterpolator());
	}
}
