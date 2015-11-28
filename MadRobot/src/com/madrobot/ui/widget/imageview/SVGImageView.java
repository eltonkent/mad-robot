package com.madrobot.ui.widget.imageview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.madrobot.R;
import com.madrobot.graphics.svg.SVG;
import com.madrobot.graphics.svg.SVGFactory;

/**
 * An ImageView that can also use SVG images as its source.
 * <p>
 * The SVG image set is loaded asynchronously.<br/>
 * Attributes<br/>
 * <table cellspacing="1" cellpadding="3">
 * <tr>
 * <th><b>Attribute</b></td>
 * <td><b>Type</b></td>
 * <td><b>Default</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td><code>svgResource</code></td>
 * <td>integer</td>
 * <td>-1</td>
 * <td>The SVG resource to be used. defined usually with
 * <code>@raw/svg_file</code></td>
 * </tr>
 * <td><code>svgZoomFactor</code></td>
 * <td>integer</td>
 * <td>100</td>
 * <td>The factor by which the SVG should be zoomed. <code>100</code> indicates
 * that the SVG will be rendered in its actual size, <code>200</code> means
 * twice its size,<code>50</code> means half its size and so on.</td> </tr>
 * </table>
 * <br/>
 * <b>Demo</b><br/>
 * This demo uses the <a
 * href="../../../../../resources/giraffe.svg">Giraffe.svg</a> created by <a
 * href="http://openclipart.org/detail/160609/new-cartoon-giraffe-by-ryanlerch">
 * ryan lerch</a>. <center><OBJECT
 * CLASSID="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" WIDTH="725" HEIGHT="461"
 * CODEBASE=
 * "http://active.macromedia.com/flash5/cabs/swflash.cab#version=7,0,0,0">
 * <PARAM NAME=movie VALUE="../../../../../resources/demos/svgimageview.swf">
 * <PARAM NAME=play VALUE=true> <PARAM NAME=loop VALUE=false> <PARAM NAME=wmode
 * VALUE=transparent> <PARAM NAME=quality VALUE=low> <EMBED
 * SRC="../../../../../resources/demos/svgimageview.swf" WIDTH=725 HEIGHT=461
 * quality=low loop=false wmode=transparent TYPE="application/x-shockwave-flash"
 * PLUGINSPAGE=
 * "http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"
 * > </EMBED> </OBJECT></center> <SCRIPT
 * src='../../../../../resources/demos/pagecurl.js'></script> <br/>
 * <b>Using the SVGImageView</b><br/>
 * <code>
 * <pre>
 * 	&lt;com.madrobot.ui.widgets.SVGImageView
 * 	android:id="@+id/image"
 * 	android:layout_width="wrap_content"
 *  android:layout_height="wrap_content"
 *  app:svgZoomFactor="100" 
 *  app:svgResource="@raw/giraffe"
 * 	android:background="#ff0000" /&gt;
 * </pre>
 * </code> <br/>
 * </p>
 * 
 * @see SVGFactory
 * @see SVG
 * @author elton.stephen.kent
 */
public class SVGImageView extends ImageView {

	private class LoadSVGTask extends AsyncTask<Void, Void, Drawable> {
		@Override
		protected Drawable doInBackground(Void... params) {
			InputStream is = null;
			switch (currentSource) {
			case LOAD_ASSET:
				try {
					is = assetManager.open(assetPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case LOAD_FILE:
				try {
					is = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				break;
			case LOAD_IS:
				is = stream;
				break;
			case LOAD_RESOURCE:
				is = getResources().openRawResource(resId);
				break;
			}
			SVG svg = SVGFactory.getSVGFromInputStream(is, zoomFactor);
			if (svg != null) {
				Drawable drawable = svg.createDrawable();
				return drawable;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Drawable result) {
			if (result != null)
				setImageDrawable(result);
		}
	}

	private int zoomFactor = 100;

	private int resId;
	private AssetManager assetManager;
	private String assetPath;
	private InputStream stream;
	private File file;

	private int currentSource = -1;

	private static final int LOAD_RESOURCE = 10;
	private static final int LOAD_ASSET = 20;
	private static final int LOAD_IS = 30;
	private static final int LOAD_FILE = 40;

	public SVGImageView(Context context) {
		super(context);
	}

	public SVGImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttributes(context, attrs);
	}

	public SVGImageView(Context context, AttributeSet attrs, int intt) {
		super(context, attrs, intt);
		initAttributes(context, attrs);
	}

	private void initAttributes(Context context, AttributeSet attrs) {
		TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
				R.styleable.SVGImageView);
		zoomFactor = styledAttrs.getInteger(R.styleable.SVGImageView_svgZoomFactor, 100);
		int resource = styledAttrs.getResourceId(R.styleable.SVGImageView_svgResource, -1);
		if (resource > 0) {
			setSVGFromResource(resource);
		}
	}

	public void setSVGFile(File svg) {
		this.file = svg;
		currentSource = LOAD_FILE;
		load();
	}

	private void load() {
		new LoadSVGTask().execute();
	}

	public void setSVGFromResource(int resId) {
		this.resId = resId;
		currentSource = LOAD_RESOURCE;
		load();
	}

	public void setSVGFromAsset(AssetManager assetMngr, String svgPath) {
		this.assetManager = assetMngr;
		this.assetPath = svgPath;
		currentSource = LOAD_ASSET;
		load();
	}

	public void setSVGFromInputStream(InputStream svgData) {
		this.stream = svgData;
		currentSource = LOAD_IS;
		load();
	}

	/**
	 * Set the Zoom level for the loaded SVG Image.
	 * <p>
	 * This is a Asynchronous call.
	 * </p>
	 * 
	 * @param zoomFactor
	 * @throws IllegalStateException
	 *             if the SVG image has not been set.
	 */
	public void setSVGZoomFactor(int zoomFactor) {
		this.zoomFactor = zoomFactor;
		if (currentSource == -1) {
			throw new IllegalStateException("SVG Image has not been set!");
		}
		new LoadSVGTask().execute();
	}

	public int getSVGZoomFactor() {
		return zoomFactor;
	}
}
