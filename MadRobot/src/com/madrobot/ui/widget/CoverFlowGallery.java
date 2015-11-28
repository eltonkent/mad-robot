package com.madrobot.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;

import com.madrobot.R;
import com.madrobot.ui.UIUtils;

/**
 * iPhone Style image gallery.
 * <p>
 * <img src="../../../../resources/CoverFlowGallery.png"><br/>
 * The following are the CoverFlowGallery attributes (attrs.xml)
 * <table cellspacing="1" cellpadding="3">
 * <tr>
 * <th><b>Attribute</b></td>
 * <td><b>Type</b></td>
 * <td><b>Default</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td><code>centerViewZoom</code></td>
 * <td>integer</td>
 * <td>120</td>
 * <td>The Zoom level of the ImageView thats in the center of the
 * CoverFlowGallery(currently selected)</td>
 * </tr>
 * <tr>
 * <td><code>rotationAngle</code></td>
 * <td>integer</td>
 * <td>60</td>
 * <td>Angle at which the ImageViews to the left and right of the center item
 * stay rotated</td>
 * </tr>
 * </table>
 * <br/>
 * <b>Demo</b><br/>
 * <center><OBJECT CLASSID="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
 * WIDTH="725" HEIGHT="461" CODEBASE=
 * "http://active.macromedia.com/flash5/cabs/swflash.cab#version=7,0,0,0">
 * <PARAM NAME=movie VALUE="../../../../resources/demos/coverflow.swf"> <PARAM
 * NAME=play VALUE=true> <PARAM NAME=loop VALUE=false> <PARAM NAME=wmode
 * VALUE=transparent> <PARAM NAME=quality VALUE=low> <EMBED
 * SRC="../../../../resources/demos/coverflow.swf" WIDTH=725 HEIGHT=461
 * quality=low loop=false wmode=transparent TYPE="application/x-shockwave-flash"
 * PLUGINSPAGE=
 * "http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"
 * > </EMBED> </OBJECT></center> <SCRIPT
 * src='../../../../resources/demos/pagecurl.js'></script> <br/>
 * 
 * <b>Using the CoverFlowGallery</b><br/>
 * <p>
 * The CoverFlowGallery is an extension of the Gallery widget in android.
 * </p>
 * <br/>
 * 
 * <b>Creating a CoverFlowGallery Adapter</b><br/>
 * <p>
 * Creating an ImageAdapter is simply done by extending BaseAdapter.
 * </p>
 * <code>
 * <pre>
 * public class ImageAdapter extends BaseAdapter {
 * 		private Context mContext;
 * 		<font color="#007F0E">//The list of images to be used by the CoverFlowGallery</font>
 * 		private Integer[] mImageIds = { R.drawable.three, R.drawable.one, R.drawable.two, R.drawable.three,
 * 				R.drawable.four,
 * 		};
 * 
 * 		public ImageAdapter(Context c) {
 * 			mContext = c;
 * 		}
 * 		public int getCount() {
 * 			return mImageIds.length;
 * 		}
 * 		public Object getItem(int position) {
 * 			return position;
 * 		}
 * 
 * 		public long getItemId(int position) {
 * 			return position;
 * 		}
 * 		<font color="#007F0E">//Must return an instance of ImageView only</font>
 * 		public View getView(int position, View convertView, ViewGroup parent) {
 * 
 * 			<font color="#007F0E">//Use this code if you want to load from resources</font>
 * 			ImageView i = new ImageView(mContext);
 * 			i.setImageResource(mImageIds[position]);
 * 			<font color="#007F0E">//The width and height of the imageview. Make sure it does not exceed the height/width of the CoverFlowGallery</font>
 * 			i.setLayoutParams(new CoverFlowGallery.LayoutParams(350, 250));
 * 			i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
 * 
 * 			<font color="#007F0E">// Make sure we enable anti-aliasing.</font>
 * 			BitmapDrawable drawable = (BitmapDrawable) i.getDrawable();
 * 			drawable.setAntiAlias(true);
 * 			return i;
 * 		}
 * 
 * }
 * </pre>
 * </code> </p>
 * 
 * @author elton.stephen.kent
 * 
 */
public class CoverFlowGallery extends Gallery {

	/**
	 * Graphics Camera used for transforming the matrix of ImageViews
	 */
	private Camera mCamera = new Camera();

	/**
	 * The Centre of the Coverflow
	 */
	private int mCoveflowCenter;

	/**
	 * The maximum angle the Child ImageView will be rotated by
	 */
	private int mMaxRotationAngle = 60;

	/**
	 * The maximum zoom on the centre Child
	 */
	private int mMaxZoom = -120;

	public CoverFlowGallery(Context context) {
		super(context);
		this.setStaticTransformationsEnabled(true);
	}

	public CoverFlowGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setStaticTransformationsEnabled(true);
		initAttributes(context, attrs);
	}

	public CoverFlowGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setStaticTransformationsEnabled(true);
		initAttributes(context, attrs);
	}

	/**
	 * Get the Centre of the Coverflow
	 * 
	 * @return The centre of this Coverflow.
	 */
	private int getCenterOfCoverflow() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see #setStaticTransformationsEnabled(boolean)
	 */
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		final int childCenter = UIUtils.getCenterOfView(child);
		final int childWidth = child.getWidth();
		int rotationAngle = 0;

		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);

		if (childCenter == mCoveflowCenter) {
			transformImageBitmap((ImageView) child, t, 0);
		} else {
			rotationAngle = (int) (((float) (mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);
			if (Math.abs(rotationAngle) > mMaxRotationAngle) {
				rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle : mMaxRotationAngle;
			}
			transformImageBitmap((ImageView) child, t, rotationAngle);
		}

		return true;
	}

	/**
	 * Get the max rotational angle of the image
	 * 
	 * @return the mMaxRotationAngle
	 */
	public int getMaxRotationAngle() {
		return mMaxRotationAngle;
	}

	/**
	 * Get the Max zoom of the centre image
	 * 
	 * @return the mMaxZoom
	 */
	public int getMaxZoom() {
		return mMaxZoom;
	}

	private void initAttributes(Context context, AttributeSet attrs) {
		TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
				R.styleable.CoverFlowGallery);
		mMaxRotationAngle = styledAttrs.getInt(R.styleable.CoverFlowGallery_rotationAngle, 60);
		mMaxZoom = -styledAttrs.getInt(R.styleable.CoverFlowGallery_centerViewZoom, 120);
	}

	/**
	 * This is called during layout when the size of this view has changed. If
	 * you were just added to the view hierarchy, you're called with the old
	 * values of 0.
	 * 
	 * @param w
	 *            Current width of this view.
	 * @param h
	 *            Current height of this view.
	 * @param oldw
	 *            Old width of this view.
	 * @param oldh
	 *            Old height of this view.
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCoveflowCenter = getCenterOfCoverflow();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * Set the max rotational angle of each image
	 * 
	 * @param maxRotationAngle
	 *            the mMaxRotationAngle to set
	 */
	public void setMaxRotationAngle(int maxRotationAngle) {
		mMaxRotationAngle = maxRotationAngle;
	}

	/**
	 * Set the max zoom of the centre image
	 * 
	 * @param maxZoom
	 *            the mMaxZoom to set
	 */
	public void setMaxZoom(int maxZoom) {
		mMaxZoom = maxZoom;
	}

	/**
	 * Transform the Image Bitmap by the Angle passed
	 * 
	 * @param imageView
	 *            ImageView the ImageView whose bitmap we want to rotate
	 * @param t
	 *            transformation
	 * @param rotationAngle
	 *            the Angle by which to rotate the Bitmap
	 */
	private void transformImageBitmap(ImageView child, Transformation t, int rotationAngle) {
		mCamera.save();
		final Matrix imageMatrix = t.getMatrix();
		;
		final int imageHeight = child.getLayoutParams().height;
		;
		final int imageWidth = child.getLayoutParams().width;
		final int rotation = Math.abs(rotationAngle);

		mCamera.translate(0.0f, 0.0f, 100.0f);

		// As the angle of the view gets less, zoom in
		if (rotation < mMaxRotationAngle) {
			float zoomAmount = (float) (mMaxZoom + (rotation * 1.5));
			mCamera.translate(0.0f, 0.0f, zoomAmount);
		}

		mCamera.rotateY(rotationAngle);
		mCamera.getMatrix(imageMatrix);
		imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
		imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
		mCamera.restore();
	}
}