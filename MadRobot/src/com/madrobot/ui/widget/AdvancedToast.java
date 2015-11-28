package com.madrobot.ui.widget;



import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Toasts that can be hooked in ViewGroups and 
 * <p>
 * Displays information in a non-invasive context related manner. Like
 * {@link android.widget.Toast}, but better.
 * <p/>
 * <b>Important: </b> Call {@link AdvancedToast#clearToastsForActivity(Activity)}
 * within {@link android.app.Activity#onDestroy()} to avoid {@link Context}
 * leaks.
 */
public final class AdvancedToast {
	private static final int IMAGE_ID = 0x100;
	private static final int TEXT_ID = 0x101;
	private final CharSequence text;
	private final Style style;
	private final View customView;

	private Activity activity;
	private ViewGroup viewGroup;
	private FrameLayout croutonView;
	private Animation inAnimation;
	private Animation outAnimation;
	private AdvancedToastCallback lifecycleCallback = null;

	/**
	 * Callback methods for AdvancedToast
	 * @author ekent4
	 *
	 */
	public interface AdvancedToastCallback {
		public void onDisplayed();
		public void onRemoved();
	}
	/**
	 * Creates the {@link AdvancedToast}.
	 * 
	 * @param activity
	 *            The {@link Activity} that the {@link AdvancedToast} should be
	 *            attached to.
	 * @param text
	 *            The text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 */
	private AdvancedToast(Activity activity, CharSequence text, Style style) {
		if ((activity == null) || (text == null) || (style == null)) {
			throw new IllegalArgumentException("Null parameters are not accepted");
		}

		this.activity = activity;
		this.viewGroup = null;
		this.text = text;
		this.style = style;
		this.customView = null;
	}

	/**
	 * Creates the {@link AdvancedToast}.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param text
	 *            The text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * @param viewGroup
	 *            The {@link ViewGroup} that this {@link AdvancedToast} should be
	 *            added to.
	 */
	private AdvancedToast(Activity activity, CharSequence text, Style style, ViewGroup viewGroup) {
		if ((activity == null) || (text == null) || (style == null)) {
			throw new IllegalArgumentException("Null parameters are not accepted");
		}

		this.activity = activity;
		this.text = text;
		this.style = style;
		this.viewGroup = viewGroup;
		this.customView = null;
	}

	/**
	 * Creates the {@link AdvancedToast}.
	 * 
	 * @param activity
	 *            The {@link Activity} that the {@link AdvancedToast} should be
	 *            attached to.
	 * @param customView
	 *            The custom {@link View} to display
	 */
	private AdvancedToast(Activity activity, View customView) {
		if ((activity == null) || (customView == null)) {
			throw new IllegalArgumentException("Null parameters are not accepted");
		}

		this.activity = activity;
		this.viewGroup = null;
		this.customView = customView;
		this.style = new Style.Builder().build();
		this.text = null;
	}

	/**
	 * Creates the {@link AdvancedToast}.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param customView
	 *            The custom {@link View} to display
	 * @param viewGroup
	 *            The {@link ViewGroup} that this {@link AdvancedToast} should be
	 *            added to.
	 */
	private AdvancedToast(Activity activity, View customView, ViewGroup viewGroup) {
		if ((activity == null) || (customView == null)) {
			throw new IllegalArgumentException("Null parameters are not accepted");
		}

		this.activity = activity;
		this.customView = customView;
		this.viewGroup = viewGroup;
		this.style = new Style.Builder().build();
		this.text = null;
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text and style for a given
	 * activity.
	 * 
	 * @param activity
	 *            The {@link Activity} that the {@link AdvancedToast} should be
	 *            attached to.
	 * @param text
	 *            The text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * 
	 * @return The created {@link AdvancedToast}.
	 */
	public static AdvancedToast makeText(Activity activity, CharSequence text, Style style) {
		return new AdvancedToast(activity, text, style);
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text and style for a given
	 * activity.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param text
	 *            The text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * @param viewGroup
	 *            The {@link ViewGroup} that this {@link AdvancedToast} should be
	 *            added to.
	 * 
	 * @return The created {@link AdvancedToast}.
	 */
	public static AdvancedToast makeText(Activity activity, CharSequence text, Style style,
			ViewGroup viewGroup) {
		return new AdvancedToast(activity, text, style, viewGroup);
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text and style for a given
	 * activity.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param text
	 *            The text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * @param viewGroupResId
	 *            The resource id of the {@link ViewGroup} that this
	 *            {@link AdvancedToast} should be added to.
	 * 
	 * @return The created {@link AdvancedToast}.
	 */
	public static AdvancedToast makeText(Activity activity, CharSequence text, Style style,
			int viewGroupResId) {
		return new AdvancedToast(activity, text, style,
				(ViewGroup) activity.findViewById(viewGroupResId));
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity
	 *            The {@link Activity} that the {@link AdvancedToast} should be
	 *            attached to.
	 * @param textResourceId
	 *            The resource id of the text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * 
	 * @return The created {@link AdvancedToast}.
	 */
	public static AdvancedToast makeText(Activity activity, int textResourceId, Style style) {
		return makeText(activity, activity.getString(textResourceId), style);
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param textResourceId
	 *            The resource id of the text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * @param viewGroup
	 *            The {@link ViewGroup} that this {@link AdvancedToast} should be
	 *            added to.
	 * 
	 * @return The created {@link AdvancedToast}.
	 */
	public static AdvancedToast makeText(Activity activity, int textResourceId, Style style,
			ViewGroup viewGroup) {
		return makeText(activity, activity.getString(textResourceId), style, viewGroup);
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param textResourceId
	 *            The resource id of the text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * @param viewGroupResId
	 *            The resource id of the {@link ViewGroup} that this
	 *            {@link AdvancedToast} should be added to.
	 * 
	 * @return The created {@link AdvancedToast}.
	 */
	public static AdvancedToast makeText(Activity activity, int textResourceId, Style style,
			int viewGroupResId) {
		return makeText(activity, activity.getString(textResourceId), style,
				(ViewGroup) activity.findViewById(viewGroupResId));
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity
	 *            The {@link Activity} that the {@link AdvancedToast} should be
	 *            attached to.
	 * @param customView
	 *            The custom {@link View} to display
	 * 
	 * @return The created {@link AdvancedToast}.
	 */
	public static AdvancedToast make(Activity activity, View customView) {
		return new AdvancedToast(activity, customView);
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param customView
	 *            The custom {@link View} to display
	 * @param viewGroup
	 *            The {@link ViewGroup} that this {@link AdvancedToast} should be
	 *            added to.
	 * 
	 * @return The created {@link AdvancedToast}.
	 */
	public static AdvancedToast make(Activity activity, View customView, ViewGroup viewGroup) {
		return new AdvancedToast(activity, customView, viewGroup);
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param customView
	 *            The custom {@link View} to display
	 * @param viewGroupResId
	 *            The resource id of the {@link ViewGroup} that this
	 *            {@link AdvancedToast} should be added to.
	 * 
	 * @return The created {@link AdvancedToast}.
	 */
	public static AdvancedToast make(Activity activity, View customView, int viewGroupResId) {
		return new AdvancedToast(activity, customView,
				(ViewGroup) activity.findViewById(viewGroupResId));
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity
	 *            The {@link android.app.Activity} that the {@link AdvancedToast}
	 *            should be attached to.
	 * @param text
	 *            The text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 */
	public static void showText(Activity activity, CharSequence text, Style style) {
		makeText(activity, text, style).show();
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param text
	 *            The text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * @param viewGroup
	 *            The {@link ViewGroup} that this {@link AdvancedToast} should be
	 *            added to.
	 */
	public static void showText(Activity activity, CharSequence text, Style style,
			ViewGroup viewGroup) {
		makeText(activity, text, style, viewGroup).show();
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param text
	 *            The text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * @param viewGroupResId
	 *            The resource id of the {@link ViewGroup} that this
	 *            {@link AdvancedToast} should be added to.
	 */
	public static void showText(Activity activity, CharSequence text, Style style,
			int viewGroupResId) {
		makeText(activity, text, style, (ViewGroup) activity.findViewById(viewGroupResId))
				.show();
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity
	 *            The {@link android.app.Activity} that the {@link AdvancedToast}
	 *            should be attached to.
	 * @param customView
	 *            The custom {@link View} to display
	 */
	public static void show(Activity activity, View customView) {
		make(activity, customView).show();
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param customView
	 *            The custom {@link View} to display
	 * @param viewGroup
	 *            The {@link ViewGroup} that this {@link AdvancedToast} should be
	 *            added to.
	 */
	public static void show(Activity activity, View customView, ViewGroup viewGroup) {
		make(activity, customView, viewGroup).show();
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param customView
	 *            The custom {@link View} to display
	 * @param viewGroupResId
	 *            The resource id of the {@link ViewGroup} that this
	 *            {@link AdvancedToast} should be added to.
	 */
	public static void show(Activity activity, View customView, int viewGroupResId) {
		make(activity, customView, viewGroupResId).show();
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text-resource and style for a
	 * given activity and displays it directly.
	 * 
	 * @param activity
	 *            The {@link Activity} that the {@link AdvancedToast} should be
	 *            attached to.
	 * @param textResourceId
	 *            The resource id of the text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 */
	public static void showText(Activity activity, int textResourceId, Style style) {
		showText(activity, activity.getString(textResourceId), style);
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text-resource and style for a
	 * given activity and displays it directly.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param textResourceId
	 *            The resource id of the text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * @param viewGroup
	 *            The {@link ViewGroup} that this {@link AdvancedToast} should be
	 *            added to.
	 */
	public static void showText(Activity activity, int textResourceId, Style style,
			ViewGroup viewGroup) {
		showText(activity, activity.getString(textResourceId), style, viewGroup);
	}

	/**
	 * Creates a {@link AdvancedToast} with provided text-resource and style for a
	 * given activity and displays it directly.
	 * 
	 * @param activity
	 *            The {@link Activity} that represents the context in which the
	 *            Crouton should exist.
	 * @param textResourceId
	 *            The resource id of the text you want to display.
	 * @param style
	 *            The style that this {@link AdvancedToast} should be created with.
	 * @param viewGroupResId
	 *            The resource id of the {@link ViewGroup} that this
	 *            {@link AdvancedToast} should be added to.
	 */
	public static void showText(Activity activity, int textResourceId, Style style,
			int viewGroupResId) {
		showText(activity, activity.getString(textResourceId), style, viewGroupResId);
	}

	/**
	 * Cancels all queued {@link AdvancedToast}s. If there is a {@link AdvancedToast}
	 * displayed currently, it will be the last one displayed.
	 */
	public static void cancelAllToasts() {
		Manager.getInstance().clearCroutonQueue();
	}

	/**
	 * Clears (and removes from {@link Activity}'s content view, if necessary)
	 * all toasts for the provided activity
	 * 
	 * @param activity
	 *            - The {@link Activity} to clear the croutons for.
	 */
	public static void clearToastsForActivity(Activity activity) {
		Manager.getInstance().clearToastsForActivity(activity);
	}

	/**
	 * Cancels a {@link AdvancedToast} immediately.
	 */
	public void cancel() {
		Manager manager = Manager.getInstance();
		manager.removeCroutonImmediately(this);
	}

	/**
	 * Displays the {@link AdvancedToast}. If there's another {@link AdvancedToast} visible
	 * at the time, this {@link AdvancedToast} will be displayed afterwards.
	 */
	public void show() {
		Manager.getInstance().add(this);
	}

	public Animation getInAnimation() {
		if ((this.inAnimation == null) && (this.activity != null)) {
			if (getStyle().inAnimationResId > 0) {
				this.inAnimation = AnimationUtils.loadAnimation(getActivity(),
						getStyle().inAnimationResId);
			} else {
				this.inAnimation = com.madrobot.ui.anim.AnimationUtils.createSlideInDownAnimation();
			}
		}

		return inAnimation;
	}

	public Animation getOutAnimation() {
		if ((this.outAnimation == null) && (this.activity != null)) {
			if (getStyle().outAnimationResId > 0) {
				this.outAnimation = AnimationUtils.loadAnimation(getActivity(),
						getStyle().outAnimationResId);
			} else {
				this.outAnimation = com.madrobot.ui.anim.AnimationUtils.createSlideOutUpAnimation();
			}
		}

		return outAnimation;
	}

	/**
	 * @param lifecycleCallback
	 *            Callback object for notable events in the life of a Crouton.
	 */
	public void setLifecycleCallback(AdvancedToastCallback lifecycleCallback) {
		this.lifecycleCallback = lifecycleCallback;
	}

	/**
	 * @return <code>true</code> if the {@link AdvancedToast} is being displayed, else
	 *         <code>false</code>.
	 */
	boolean isShowing() {
		return (activity != null) && (croutonView != null)
				&& (croutonView.getParent() != null);
	}

	/**
	 * Removes the activity reference this {@link AdvancedToast} is holding
	 */
	void detachActivity() {
		activity = null;
	}

	/**
	 * Removes the viewGroup reference this {@link AdvancedToast} is holding
	 */
	void detachViewGroup() {
		viewGroup = null;
	}

	/**
	 * Removes the lifecycleCallback reference this {@link AdvancedToast} is holding
	 */
	void detachLifecycleCallback() {
		lifecycleCallback = null;
	}

	/**
	 * @return the lifecycleCallback
	 */
	AdvancedToastCallback getLifecycleCallback() {
		return lifecycleCallback;
	}

	/**
	 * @return the style
	 */
	Style getStyle() {
		return style;
	}

	/**
	 * @return the activity
	 */
	Activity getActivity() {
		return activity;
	}

	/**
	 * @return the viewGroup
	 */
	ViewGroup getViewGroup() {
		return viewGroup;
	}

	/**
	 * @return the text
	 */
	CharSequence getText() {
		return text;
	}

	/**
	 * @return the view
	 */
	View getView() {
		// return the custom view if one exists
		if (this.customView != null) {
			return this.customView;
		}

		// if already setup return the view
		if (this.croutonView == null) {
			initializeCroutonView();
		}

		return croutonView;
	}

	private void initializeCroutonView() {
		Resources resources = this.activity.getResources();

		this.croutonView = initializeCroutonViewGroup(resources);

		// create content view
		RelativeLayout contentView = initializeContentView(resources);
		this.croutonView.addView(contentView);
	}

	private FrameLayout initializeCroutonViewGroup(Resources resources) {
		FrameLayout croutonView = new FrameLayout(this.activity);

		final int height;
		if (this.style.heightDimensionResId > 0) {
			height = resources.getDimensionPixelSize(this.style.heightDimensionResId);
		} else {
			height = this.style.heightInPixels;
		}

		final int width;
		if (this.style.widthDimensionResId > 0) {
			width = resources.getDimensionPixelSize(this.style.widthDimensionResId);
		} else {
			width = this.style.widthInPixels;
		}

		croutonView.setLayoutParams(new FrameLayout.LayoutParams(width != 0 ? width
				: FrameLayout.LayoutParams.MATCH_PARENT, height));

		// set background
		if (this.style.backgroundColorValue != -1) {
			croutonView.setBackgroundColor(this.style.backgroundColorValue);
		} else {
			croutonView.setBackgroundColor(resources
					.getColor(this.style.backgroundColorResourceId));
		}

		// set the background drawable if set. This will override the background
		// color.
		if (this.style.backgroundDrawableResourceId != 0) {
			Bitmap background = BitmapFactory.decodeResource(resources,
					this.style.backgroundDrawableResourceId);
			BitmapDrawable drawable = new BitmapDrawable(resources, background);
			if (this.style.isTileEnabled) {
				drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			}
			croutonView.setBackgroundDrawable(drawable);
		}
		return croutonView;
	}

	private RelativeLayout initializeContentView(final Resources resources) {
		RelativeLayout contentView = new RelativeLayout(this.activity);
		contentView.setLayoutParams(new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));

		// set padding
		int padding = this.style.paddingInPixels;

		// if a padding dimension has been set, this will overwrite any padding
		// in pixels
		if (this.style.paddingDimensionResId > 0) {
			padding = resources.getDimensionPixelSize(this.style.paddingDimensionResId);
		}
		contentView.setPadding(padding, padding, padding, padding);

		// only setup image if one is requested
		ImageView image = null;
		if ((this.style.imageDrawable != null) || (this.style.imageResId != 0)) {
			image = initializeImageView();
			contentView.addView(image, image.getLayoutParams());
		}

		TextView text = initializeTextView(resources);

		RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		if (image != null) {
			textParams.addRule(RelativeLayout.RIGHT_OF, image.getId());
		}
		contentView.addView(text, textParams);
		return contentView;
	}

	private TextView initializeTextView(final Resources resources) {
		TextView text = new TextView(this.activity);
		text.setId(TEXT_ID);
		text.setText(this.text);
		text.setTypeface(Typeface.DEFAULT_BOLD);
		text.setGravity(this.style.gravity);

		// set the text color if set
		if (this.style.textColorResourceId != 0) {
			text.setTextColor(resources.getColor(this.style.textColorResourceId));
		}

		// Set the text size. If the user has set a text size and text
		// appearance, the text size in the text appearance
		// will override this.
		if (this.style.textSize != 0) {
			text.setTextSize(TypedValue.COMPLEX_UNIT_SP, this.style.textSize);
		}

		// Setup the shadow if requested
		if (this.style.textShadowColorResId != 0) {
			initializeTextViewShadow(resources, text);
		}

		// Set the text appearance
		if (this.style.textAppearanceResId != 0) {
			text.setTextAppearance(this.activity, this.style.textAppearanceResId);
		}
		return text;
	}

	private void initializeTextViewShadow(final Resources resources, final TextView text) {
		int textShadowColor = resources.getColor(this.style.textShadowColorResId);
		float textShadowRadius = this.style.textShadowRadius;
		float textShadowDx = this.style.textShadowDx;
		float textShadowDy = this.style.textShadowDy;
		text.setShadowLayer(textShadowRadius, textShadowDx, textShadowDy, textShadowColor);
	}

	private ImageView initializeImageView() {
		ImageView image;
		image = new ImageView(this.activity);
		image.setId(IMAGE_ID);
		image.setAdjustViewBounds(true);
		image.setScaleType(this.style.imageScaleType);

		// set the image drawable if not null
		if (this.style.imageDrawable != null) {
			image.setImageDrawable(this.style.imageDrawable);
		}

		// set the image resource if not 0. This will overwrite the drawable
		// if both are set
		if (this.style.imageResId != 0) {
			image.setImageResource(this.style.imageResId);
		}

		RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		imageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		imageParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

		image.setLayoutParams(imageParams);

		return image;
	}

	@Override
	public String toString() {
		return "Advanced Toast {" + "text=" + text + ", style=" + style + ", customView=" + customView
				+ ", activity=" + activity + ", viewGroup=" + viewGroup + ", croutonView="
				+ croutonView + ", inAnimation=" + inAnimation + ", outAnimation="
				+ outAnimation + ", lifecycleCallback=" + lifecycleCallback + '}';
	}
	
	public static class Style {
		  public static final Style ALERT;
		  public static final Style CONFIRM;
		  public static final Style INFO;

		  public static final int holoRedLight = 0xffff4444;
		  public static final int holoGreenLight = 0xff99cc00;
		  public static final int holoBlueLight = 0xff33b5e5;

		  static {
		    ALERT = new Builder().setDuration(5000).setBackgroundColorValue(holoRedLight).setHeight(LayoutParams.WRAP_CONTENT)
		      .build();
		    CONFIRM = new Builder().setDuration(3000).setBackgroundColorValue(holoGreenLight).setHeight(
		      LayoutParams.WRAP_CONTENT).build();
		    INFO = new Builder().setDuration(3000).setBackgroundColorValue(holoBlueLight).setHeight(LayoutParams.WRAP_CONTENT)
		      .build();
		  }

		  /**
		   * The durationInMilliseconds the {@link Toast} will be displayed in
		   * milliseconds.
		   */
		  final int durationInMilliseconds;

		  /**
		   * The resource id of the backgroundResourceId.
		   * <p/>
		   * 0 for no backgroundResourceId.
		   */
		  final int backgroundColorResourceId;

		  /**
		   * The resource id of the backgroundDrawableResourceId.
		   * <p/>
		   * 0 for no backgroundDrawableResourceId.
		   */
		  final int backgroundDrawableResourceId;

		  /**
		   * The backgroundColorResourceValue's e.g. 0xffff4444;
		   * <p/>
		   * -1 for no value.
		   */
		  final int backgroundColorValue;

		  /**
		   * Whether we should isTileEnabled the backgroundResourceId or not.
		   */
		  final boolean isTileEnabled;

		  /**
		   * The text colorResourceId's resource id.
		   * <p/>
		   * 0 sets the text colorResourceId to the system theme default.
		   */
		  final int textColorResourceId;

		  /**
		   * The height of the {@link Crouton} in pixels.
		   */
		  final int heightInPixels;

		  /**
		   * Resource ID for the height of the {@link Crouton}.
		   */
		  final int heightDimensionResId;
		  
		  /**
		   * The width of the {@link Crouton} in pixels.
		   */
		  final int widthInPixels;
		  
		  /**
		   * Resource ID for the width of the {@link Crouton}.
		   */
		  final int widthDimensionResId;

		  /**
		   * The text's gravity as provided by {@link Gravity}.
		   */
		  final int gravity;

		  /**
		   * An additional image to display in the {@link Crouton}.
		   */
		  final Drawable imageDrawable;

		  /**
		   * An additional image to display in the {@link Crouton}.
		   */
		  final int imageResId;

		  /**
		   * The {@link ImageView.ScaleType} for the image to display in the
		   * {@link Crouton}.
		   */
		  final ImageView.ScaleType imageScaleType;

		  /**
		   * The text size in sp
		   * <p/>
		   * 0 sets the text size to the system theme default
		   */
		  final int textSize;

		  /**
		   * The text shadow color's resource id
		   */
		  final int textShadowColorResId;

		  /**
		   * The text shadow radius
		   */
		  final float textShadowRadius;

		  /**
		   * The text shadow vertical offset
		   */
		  final float textShadowDy;

		  /**
		   * The text shadow horizontal offset
		   */
		  final float textShadowDx;

		  /**
		   * The text appearance resource id for the text.
		   */
		  final int textAppearanceResId;

		  /**
		   * The resource id for the in animation
		   */
		  final int inAnimationResId;

		  /**
		   * The resource id for the out animation
		   */
		  final int outAnimationResId;

		  /**
		   * The padding for the crouton view content in pixels
		   */
		  final int paddingInPixels;

		  /**
		   * The resource id for the padding for the view content
		   */
		  final int paddingDimensionResId;

		  private Style(final Builder builder) {
		    this.durationInMilliseconds = builder.durationInMilliseconds;
		    this.backgroundColorResourceId = builder.backgroundColorResourceId;
		    this.backgroundDrawableResourceId = builder.backgroundDrawableResourceId;
		    this.isTileEnabled = builder.isTileEnabled;
		    this.textColorResourceId = builder.textColorResourceId;
		    this.heightInPixels = builder.heightInPixels;
		    this.heightDimensionResId = builder.heightDimensionResId;
		    this.widthInPixels = builder.widthInPixels;
		    this.widthDimensionResId = builder.widthDimensionResId;
		    this.gravity = builder.gravity;
		    this.imageDrawable = builder.imageDrawable;
		    this.textSize = builder.textSize;
		    this.textShadowColorResId = builder.textShadowColorResId;
		    this.textShadowRadius = builder.textShadowRadius;
		    this.textShadowDx = builder.textShadowDx;
		    this.textShadowDy = builder.textShadowDy;
		    this.textAppearanceResId = builder.textAppearanceResId;
		    this.inAnimationResId = builder.inAnimationResId;
		    this.outAnimationResId = builder.outAnimationResId;
		    this.imageResId = builder.imageResId;
		    this.imageScaleType = builder.imageScaleType;
		    this.paddingInPixels = builder.paddingInPixels;
		    this.paddingDimensionResId = builder.paddingDimensionResId;
		    this.backgroundColorValue = builder.backgroundColorValue;
		  }

		  /**
		   * Builder for the {@link Style} object.
		   */
		  public static class Builder {
		    private int durationInMilliseconds;
		    private int backgroundColorValue;
		    private int backgroundColorResourceId;
		    private int backgroundDrawableResourceId;
		    private boolean isTileEnabled;
		    private int textColorResourceId;
		    private int heightInPixels;
		    private int heightDimensionResId;
		    private int widthInPixels;
		    private int widthDimensionResId;
		    private int gravity;
		    private Drawable imageDrawable;
		    private int textSize;
		    private int textShadowColorResId;
		    private float textShadowRadius;
		    private float textShadowDx;
		    private float textShadowDy;
		    private int textAppearanceResId;
		    private int inAnimationResId;
		    private int outAnimationResId;
		    private int imageResId;
		    private ImageView.ScaleType imageScaleType;
		    private int paddingInPixels;
		    private int paddingDimensionResId;

		    public Builder() {
		      durationInMilliseconds = 3000;
		      paddingInPixels = 10;
		      backgroundColorResourceId = Color.BLUE; //android.R.color.holo_blue_light;
		      backgroundDrawableResourceId = 0;
		      backgroundColorValue = -1;
		      isTileEnabled = false;
		      textColorResourceId = android.R.color.white;
		      heightInPixels = LayoutParams.WRAP_CONTENT;
		      widthInPixels = LayoutParams.MATCH_PARENT;
		      gravity = Gravity.CENTER;
		      imageDrawable = null;
		      inAnimationResId = 0;
		      outAnimationResId = 0;
		      imageResId = 0;
		      imageScaleType = ImageView.ScaleType.FIT_XY;
		    }

		    /**
		     * Set the durationInMilliseconds option of the {@link Crouton}.
		     *
		     * @param duration
		     *          The durationInMilliseconds the crouton will be displayed
		     *          {@link Crouton} in milliseconds.
		     * @return the {@link Builder}.
		     */
		    public Builder setDuration(int duration) {
		      this.durationInMilliseconds = duration;

		      return this;
		    }

		    /**
		     * Set the backgroundColorResourceId option of the {@link Crouton}.
		     *
		     * @param backgroundColorResourceId
		     *          The backgroundColorResourceId's resource id.
		     * @return the {@link Builder}.
		     */
		    public Builder setBackgroundColor(int backgroundColorResourceId) {
		      this.backgroundColorResourceId = backgroundColorResourceId;

		      return this;
		    }

		    /**
		     * Set the backgroundColorResourceValue option of the {@link Crouton}.
		     *
		     * @param backgroundColorValue
		     *          The backgroundColorResourceValue's e.g. 0xffff4444;
		     * @return the {@link Builder}.
		     */
		    public Builder setBackgroundColorValue(int backgroundColorValue) {
		      this.backgroundColorValue = backgroundColorValue;
		      return this;
		    }

		    /**
		     * Set the backgroundDrawableResourceId option for the {@link Crouton}.
		     *
		     * @param backgroundDrawableResourceId
		     *          Resource ID of a backgroundDrawableResourceId image drawable.
		     * @return the {@link Builder}.
		     */
		    public Builder setBackgroundDrawable(int backgroundDrawableResourceId) {
		      this.backgroundDrawableResourceId = backgroundDrawableResourceId;

		      return this;
		    }

		    /**
		     * Set the heightInPixels option for the {@link Crouton}.
		     *
		     * @param height
		     *          The height of the {@link Crouton} in pixel. Can also be
		     *          {@link LayoutParams#MATCH_PARENT} or
		     *          {@link LayoutParams#WRAP_CONTENT}.
		     * @return the {@link Builder}.
		     */
		    public Builder setHeight(int height) {
		      this.heightInPixels = height;

		      return this;
		    }

		    /**
		     * Set the resource id for the height option for the {@link Crouton}.
		     *
		     * @param heightDimensionResId
		     *          Resource ID of a dimension for the height of the {@link Crouton}.
		     * @return the {@link Builder}.
		     */
		    public Builder setHeightDimensionResId(int heightDimensionResId) {
		      this.heightDimensionResId = heightDimensionResId;

		      return this;
		    }

		    /**
		     * Set the widthInPixels option for the {@link Crouton}.
		     *
		     * @param width
		     *          The width of the {@link Crouton} in pixel. Can also be
		     *          {@link LayoutParams#MATCH_PARENT} or
		     *          {@link LayoutParams#WRAP_CONTENT}.
		     * @return the {@link Builder}.
		     */
		    public Builder setWidth(int width) {
		      this.widthInPixels = width;

		      return this;
		    }

		    /**
		     * Set the resource id for the width option for the {@link Crouton}.
		     *
		     * @param widthDimensionResId
		     *          Resource ID of a dimension for the width of the {@link Crouton}.
		     * @return the {@link Builder}.
		     */
		    public Builder setWidthDimensionResId(int widthDimensionResId) {
		      this.widthDimensionResId = widthDimensionResId;

		      return this;
		    }

		    /**
		     * Set the isTileEnabled option for the {@link Crouton}.
		     *
		     * @param isTileEnabled
		     *          <code>true</code> if you want the backgroundResourceId to be
		     *          tiled, else <code>false</code>.
		     * @return the {@link Builder}.
		     */
		    public Builder setTileEnabled(boolean isTileEnabled) {
		      this.isTileEnabled = isTileEnabled;

		      return this;
		    }

		    /**
		     * Set the textColorResourceId option for the {@link Crouton}.
		     *
		     * @param textColor
		     *          The resource id of the text colorResourceId.
		     * @return the {@link Builder}.
		     */
		    public Builder setTextColor(int textColor) {
		      this.textColorResourceId = textColor;

		      return this;
		    }

		    /**
		     * Set the gravity option for the {@link Crouton}.
		     *
		     * @param gravity
		     *          The text's gravity as provided by {@link Gravity}.
		     * @return the {@link Builder}.
		     */
		    public Builder setGravity(int gravity) {
		      this.gravity = gravity;

		      return this;
		    }

		    /**
		     * Set the image option for the {@link Crouton}.
		     *
		     * @param imageDrawable
		     *          An additional image to display in the {@link Crouton}.
		     * @return the {@link Builder}.
		     */
		    public Builder setImageDrawable(Drawable imageDrawable) {
		      this.imageDrawable = imageDrawable;

		      return this;
		    }

		    /**
		     * Set the image resource option for the {@link Crouton}.
		     *
		     * @param imageResId
		     *          An additional image to display in the {@link Crouton}.
		     * @return the {@link Builder}.
		     */
		    public Builder setImageResource(int imageResId) {
		      this.imageResId = imageResId;

		      return this;
		    }

		    /**
		     * The text size in sp
		     */
		    public Builder setTextSize(int textSize) {
		      this.textSize = textSize;
		      return this;
		    }

		    /**
		     * The text shadow color's resource id
		     */
		    public Builder setTextShadowColor(int textShadowColorResId) {
		      this.textShadowColorResId = textShadowColorResId;
		      return this;
		    }

		    /**
		     * The text shadow radius
		     */
		    public Builder setTextShadowRadius(float textShadowRadius) {
		      this.textShadowRadius = textShadowRadius;
		      return this;
		    }

		    /**
		     * The text shadow horizontal offset
		     */
		    public Builder setTextShadowDx(float textShadowDx) {
		      this.textShadowDx = textShadowDx;
		      return this;
		    }

		    /**
		     * The text shadow vertical offset
		     */
		    public Builder setTextShadowDy(float textShadowDy) {
		      this.textShadowDy = textShadowDy;
		      return this;
		    }

		    /**
		     * The text appearance resource id for the text.
		     */
		    public Builder setTextAppearance(int textAppearanceResId) {
		      this.textAppearanceResId = textAppearanceResId;
		      return this;
		    }

		    /**
		     * The resource id for the in animation
		     */
		    public Builder setInAnimation(int inAnimationResId) {
		      this.inAnimationResId = inAnimationResId;
		      return this;
		    }

		    /**
		     * The resource id for the out animation
		     */
		    public Builder setOutAnimation(int outAnimationResId) {
		      this.outAnimationResId = outAnimationResId;
		      return this;
		    }

		    /**
		     * The {@link android.widget.ImageView.ScaleType} for the image
		     */
		    public Builder setImageScaleType(ImageView.ScaleType imageScaleType) {
		      this.imageScaleType = imageScaleType;
		      return this;
		    }

		    /**
		     * The padding for the crouton view's content in pixels
		     */
		    public Builder setPaddingInPixels(int padding) {
		      this.paddingInPixels = padding;
		      return this;
		    }

		    /**
		     * The resource id for the padding for the crouton view's content
		     */
		    public Builder setPaddingDimensionResId(int paddingResId) {
		      this.paddingDimensionResId = paddingResId;
		      return this;
		    }

		    /**
		     * @return a configured {@link Style} object.
		     */
		    public Style build() {
		      return new Style(this);
		    }
		  }
		}
}
