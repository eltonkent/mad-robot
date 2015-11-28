package com.madrobot.ui.anim;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationUtils {
	public static Animation createHeightChangeAnimation(int oldHeight, int newHeight,
			int duration, AnimationListener listener) {
		float targetScale = (float) newHeight / (float) oldHeight;
		ScaleAnimation anim = new ScaleAnimation(1, 1, 1.0f, targetScale);
		anim.setDuration(duration);
		anim.setAnimationListener(listener);
		return anim;
	}

	public Animation createFadeInAnimation(int duration,
			AnimationListener listener){
		return createFadeAnimation(false, duration, listener);
	}
	
	public Animation createFadeOutAnimation(int duration,
			AnimationListener listener){
		return createFadeAnimation(true, duration, listener);
	}
	private static Animation createFadeAnimation(boolean out, int duration,
			AnimationListener listener) {
		AlphaAnimation anim = new AlphaAnimation((out ? 1.0f : 0.0f), (out ? 0.0f : 1.0f));
		anim.setDuration(duration);
		anim.setAnimationListener(listener);
		return anim;
	}

	public static Animation createSlideAnimation(boolean relToParent, boolean horizontal,
			boolean fromLeftOrTop, boolean exiting, int duration) {
		int rel = (relToParent ? Animation.RELATIVE_TO_PARENT : Animation.RELATIVE_TO_SELF);
		float movingFrom = (exiting ? 0f : (fromLeftOrTop ? -1f : 1f));
		float movingTo = (exiting ? (fromLeftOrTop ? 1f : -1f) : 0f);
		TranslateAnimation anim;
		if (horizontal) {
			anim = new TranslateAnimation(rel, movingFrom, rel, movingTo, rel, 0, rel, 0);
		} else {
			anim = new TranslateAnimation(rel, 0, rel, 0, rel, movingFrom, rel, movingTo);
		}
		anim.setDuration(duration);
		return anim;
	}

	private static Animation moveAnimation(final float fromX, final float toX,
			final float fromY, final float toY) {
		final long moveDuration = 200, alphaDuration = 100;

		final AnimationSet main = new AnimationSet(false);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(alphaDuration);
		main.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromX,
				Animation.RELATIVE_TO_SELF, toX, Animation.RELATIVE_TO_SELF, fromY,
				Animation.RELATIVE_TO_SELF, toY);
		animation.setDuration(moveDuration);
		main.addAnimation(animation);

		return main;
	}

	/**
	 * @return an animation controller
	 */
	public static Animation createGoDownAnimation() {
		return moveAnimation(0, 0, -1, 0);
	}

	/**
	 * @return an animation controller
	 */
	public static Animation createGoUpAnimation() {
		final Animation a = moveAnimation(0, 0, 0, -1);
		a.setFillBefore(true);
		return a;
	}

	public static Animation createSlideInDownAnimation() {
		Animation slideInDownAnimation = new TranslateAnimation(0, 0, -50, 0);
		slideInDownAnimation.setDuration(400);
		return slideInDownAnimation;
	}

	public static Animation createSlideOutUpAnimation() {
		Animation slideOutUpAnimation = new TranslateAnimation(0, 0, 0, -50);
		slideOutUpAnimation.setDuration(400);
		return slideOutUpAnimation;
	}

}
