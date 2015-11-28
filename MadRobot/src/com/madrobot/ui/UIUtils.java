/*******************************************************************************
 * Copyright (c) 2011 MadRobot.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *  Elton Kent - initial API and implementation
 ******************************************************************************/
package com.madrobot.ui;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import com.madrobot.ui.drawable.ViewPressedDrawable;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class UIUtils {
	public abstract static class ClickSpan extends ClickableSpan {

	}

	/**
	 * Set part of the text in a textview clickable.
	 * <p>
	 * The entire text needs to set to the textview before calling this method
	 * </p>
	 * 
	 * @param view
	 *            Textview to assign the clickage text to.
	 * @param clickableText
	 *            Text in the textview that needs to be made clickable.
	 * @param span
	 */
	public static void clickify(TextView view, final String clickableText, final ClickSpan span) {

		CharSequence text = view.getText();
		String string = text.toString();

		int start = string.indexOf(clickableText);
		int end = start + clickableText.length();
		if (start == -1)
			return;

		if (text instanceof Spannable) {
			((Spannable) text).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		} else {
			SpannableString s = SpannableString.valueOf(text);
			s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			view.setText(s);
		}

		MovementMethod m = view.getMovementMethod();
		if ((m == null) || !(m instanceof LinkMovementMethod)) {
			view.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}

	/**
	 * Returns the scaled size for different resolutions.
	 * 
	 * @param fntSize
	 * @return
	 */
	public static int getDensityIndependentSize(int size, Context ctx) {
		int density = ctx.getResources().getDisplayMetrics().densityDpi;
		if (160 == density) {
			int newSize = (int) (size / (1.5));
			return newSize;
		} else if (density == 120) {
			int newSize = (size / (2));
			return newSize;
		}

		return size;
	}

	public static int getScaledPixels(Context ctx, int unscaled) {
		return (int) (unscaled * ctx.getResources().getDisplayMetrics().density + 0.5f);
	}

	/**
	 * Display a system Toast using a custom ui view.
	 * 
	 * @param viewResId
	 *            the view res id
	 * @param duration
	 *            the duration
	 * @param gravity
	 *            the gravity
	 */
	public static void showCustomToast(Context mContext, int viewResId, int duration,
			int gravity) {
		View layout = getLayoutInflater(mContext).inflate(viewResId, null);

		Toast toast = new Toast(mContext.getApplicationContext());

		toast.setGravity(gravity, 0, 0);
		toast.setDuration(duration);
		toast.setView(layout);
		toast.show();
	}

	public static LayoutInflater getLayoutInflater(Context mContext) {
		return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	/**
	 * Gets the optimal number of columns that can be used for the given width
	 * 
	 * @param drawable_width
	 *            the drawable_width in dip
	 * @return the screen optimal columns
	 */
	public static int getScreenOptimalColumns(Context mContext, int drawable_width) {
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		double a = (double) metrics.widthPixels / (double) metrics.densityDpi; // 2.25
		int b = (int) Math.ceil(a * 2.0); // 5

		if ((b * drawable_width) > metrics.widthPixels) {
			return metrics.widthPixels / drawable_width;
		}

		return Math.min(Math.max(b, 3), 10);
	}

	/**
	 * Get the optimal number of rows for the given height of an item
	 * 
	 * @param mContext
	 * @param drawable_height
	 *            in dip
	 * @return
	 */
	public static int getScreenOptimalRows(Context mContext, int drawable_height) {
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		double a = (double) metrics.heightPixels / (double) metrics.densityDpi; // 2.25
		int b = (int) Math.ceil(a * 2.0); // 5

		if ((b * drawable_height) > metrics.heightPixels) {
			return metrics.widthPixels / drawable_height;
		}

		return Math.min(Math.max(b, 3), 10);
	}

	/**
	 * Get the Centre of the View
	 * 
	 * @return The centre of the given view.
	 */
	public static int getCenterOfView(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}

	public static void makeGlow(TextView txtView, int glowColor) {
		txtView.setShadowLayer(2, 0, 0, glowColor);
	}

	/**
	 * Sets bitmap along with a text in a TextView
	 * <p>
	 * <img src="../../../../resources/bitmapinlined.png"><br/>
	 * </p>
	 * 
	 * @param tv
	 *            text view to set the text along with the image
	 * @param string
	 *            string to set to the textview
	 * @param bitmap
	 *            bitmap to insert in the string
	 * @param insertionIndex
	 *            index in the string at which to insert the <code>bitmap</code>
	 */
	public static void insertBitmapIntoTextView(TextView tv, String string, Bitmap bitmap,
			int insertionIndex) {
		StringBuilder appender = new StringBuilder(string);
		appender.insert(insertionIndex, ' ');
		SpannableStringBuilder builder = new SpannableStringBuilder(appender.toString());
		ImageSpan imageSpan = new ImageSpan(bitmap);
		builder.setSpan(imageSpan, insertionIndex, insertionIndex + 1,
				Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		tv.setText(builder, BufferType.SPANNABLE);
	}

	/**
	 * Convert to pix from DPI.
	 * 
	 * @param context
	 * @param dips
	 * @return
	 */
	public static int fromDip(Context context, int dips) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, context
				.getResources().getDisplayMetrics());
	}

	/**
	 * Hide soft keyboard.
	 * 
	 * @param textView
	 *            text view containing current window token
	 */
	public static void hideSoftInput(final View textView) {
		try {
			final InputMethodManager imm = (InputMethodManager) textView.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Show soft keyboard.
	 * 
	 * @param textView
	 *            text view containing current window token
	 */
	public static void showSoftInput(final View textView) {
		try {
			final InputMethodManager imm = (InputMethodManager) textView.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(textView, InputMethodManager.SHOW_FORCED);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Toggles keyboard visibility.
	 * 
	 * @param textView
	 *            text view containing current window token
	 */
	public static void toggleSoftInput(final View textView) {
		try {
			final InputMethodManager imm = (InputMethodManager) textView.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInputFromWindow(textView.getWindowToken(), 0, 0);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set text view value or change its visibility in case of empty value.
	 * 
	 * @param view
	 *            view instance
	 * @param text
	 *            text value
	 * @param hvisibility
	 *            visibility value
	 */
	public static void setTextOrHide(final TextView view, final CharSequence text,
			final int hvisibility) {
		if (TextUtils.isEmpty(text)) {
			view.setVisibility(hvisibility);
		} else {
			view.setText(text);
			view.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Get the given view copied onto a bitmap.
	 * 
	 * @param view
	 * @param outputConfig
	 *            for the bitmap that is returned
	 * @return Bitmap of the given view, <code>null</code> if the view has a
	 *         width or height of 0.
	 */
	public static Bitmap getSnap(View view, Bitmap.Config outputConfig) {
		int width = view.getWidth();
		int height = view.getHeight();
		if (view != null && width > 0 && height > 0) {
			Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
					outputConfig);
			android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
			view.draw(canvas);
			return bitmap;
		}
		return null;
	}

	/**
	 * Check if the device is a tablet
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * Shows a toast to the user - can be called from any thread, toast will be
	 * displayed using the UI-thread.
	 * <p>
	 * The important thing about the delayed aspect of the UI-thread code used
	 * by this method is that it may actually run <em>after</em> the associated
	 * activity has been destroyed - so it can not keep a reference to the
	 * activity. Calling methods on a destroyed activity may throw exceptions,
	 * and keeping a reference to it is technically a short-term memory-leak:
	 * http
	 * ://developer.android.com/resources/articles/avoiding-memory-leaks.html
	 * 
	 * @param activity
	 *            Activity to show it on
	 * @param message
	 *            Toast message
	 * @param duration
	 *            duration of the toast
	 */
	public static void toastOnUiThread(Activity activity, final String message,
			final int duration) {
		final Application application = activity.getApplication();
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(application, message, duration).show();
			}
		});
	}

	/**
	 * Check if the calling method is on the main thread.
	 * 
	 * @return
	 */
	public static boolean isInMainThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}

	public static <T> T callInMainThread(Callable<T> call) throws Exception {
		if (isInMainThread())
			return call.call();
		else {
			FutureTask<T> task = new FutureTask<T>(call);
			new Handler().post(task);
			return task.get();
		}
	}

	/**
	 * 
	 * @param view
	 */
	public static void setPressedState(View view) {
		Drawable bg = view.getBackground();
		if (bg == null) {
			throw new IllegalStateException("View needs to have a background drawable");
		}
		view.setBackgroundDrawable(new ViewPressedDrawable(bg));
	}
}
