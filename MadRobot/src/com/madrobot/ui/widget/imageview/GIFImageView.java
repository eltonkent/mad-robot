package com.madrobot.ui.widget.imageview;

import java.io.InputStream;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.madrobot.R;
import com.madrobot.graphics.AnimatedGIFDecoder;

/**
 * Image view that displays animated GIF files.
 * <p>
 * Its a good practice to call {@link #stopAnimating()} when the activity is paused and {@link #startAnimating()} when it is resumed.
 * Attributes<br/>
 * <table cellspacing="1" cellpadding="3">
 * <tr>
 * <th><b>Attribute</b></td>
 * <td><b>Type</b></td>
 * <td><b>Default</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td><code>gifImage</code></td>
 * <td>integer</td>
 * <td>01</td>
 * <td>The GIF resource to be used. defined usually with
 * <code>@drawable/gif_file</code></td>
 * </tr>
 * <tr>
 * <td><code>startAnimating</code></td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Auto start GIF animation</td>
 * </tr>
 * <br/>
 * <b>Demo</b><br/>
 * <center><OBJECT CLASSID="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
 * WIDTH="725" HEIGHT="461" CODEBASE=
 * "http://active.macromedia.com/flash5/cabs/swflash.cab#version=7,0,0,0">
 * <PARAM NAME=movie VALUE="../../../../../resources/demos/gifimageview.swf">
 * <PARAM NAME=play VALUE=true> <PARAM NAME=loop VALUE=false> <PARAM NAME=wmode
 * VALUE=transparent> <PARAM NAME=quality VALUE=low> <EMBED
 * SRC="../../../../../resources/demos/gifimageview.swf" WIDTH=725 HEIGHT=461
 * quality=low loop=false wmode=transparent TYPE="application/x-shockwave-flash"
 * PLUGINSPAGE=
 * "http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"
 * > </EMBED> </OBJECT></center> <SCRIPT
 * src='../../../../../resources/demos/pagecurl.js'></script>
 * </p>
 * 
 * @author Elton.kent
 * 
 */
public class GIFImageView extends ImageView {
	private boolean autostart;

	public GIFImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
				R.styleable.GIFImageView);
		int resource = styledAttrs.getResourceId(R.styleable.GIFImageView_gifImage, -1);
		setAutoStartAnimation(styledAttrs.getBoolean(R.styleable.GIFImageView_startAnimating,
				true));
		if (resource > 0) {
			setGIFFromResource(resource);
		}
	}

	private boolean mIsPlayingGif = false;

	private AnimatedGIFDecoder mGifDecoder;

	private Bitmap mTmpBitmap;

	private final Handler mHandler = new Handler();

	private final Runnable mUpdateResults = new Runnable() {
		@Override
		public void run() {
			if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
				GIFImageView.this.setImageBitmap(mTmpBitmap);
			}
		}
	};

	public void setAutoStartAnimation(boolean autostart) {
		this.autostart = autostart;
	}

	/**
	 * Stop Animating the GIF
	 */
	public void stopAnimating() {
		mIsPlayingGif = false;
	}

	public boolean isAnimating() {
		return mIsPlayingGif;
	}

	public void resetAnimation() {
		mTmpBitmap = mGifDecoder.getFrame(0);
		mHandler.post(mUpdateResults);
	}

	public void startAnimating() {
		if (isDecoding)
			throw new IllegalStateException(
					"Cannot start animation while gif is decoding. Set Autostart = true to start animation right after decoding.");
		if (!mIsPlayingGif)
			startGIFAnimation();
	}

	public void setGIFFromResource(int resId) {
		InputStream is = getResources().openRawResource(resId);
		setGIFFromStream(is);
	}

	@Override
	public void setImageResource(int resId) {
		setGIFFromResource(resId);
	}

	private boolean isDecoding;

	public synchronized void setGIFFromStream(final InputStream stream) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mIsPlayingGif = false;
				isDecoding = true;
				mGifDecoder = new AnimatedGIFDecoder();
				int read = mGifDecoder.read(stream);
				if (read != 0) {
					throw new RuntimeException(
							"Could not process GIF file. This could be an invalid GIF file");
				}
				isDecoding = false;
				if (autostart) {
					startAnimating();
				} else {
					Log.d("Autostart", "not animating");
					mTmpBitmap = mGifDecoder.getFrame(0);
					mHandler.post(mUpdateResults);
				}
			}

		}).start();

	}

	private void startGIFAnimation() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mIsPlayingGif = true;
				final int n = mGifDecoder.getFrameCount();
				System.out.println("GIF  Frame count->" + n);
				final int ntimes = mGifDecoder.getLoopCount();
				int repetitionCounter = 0;
				int t = 0;
				do {
					for (int i = 0; i < n; i++) {
						mTmpBitmap = mGifDecoder.getFrame(i);
						t = mGifDecoder.getDelay(i);
						mHandler.post(mUpdateResults);
						try {
							if (mIsPlayingGif)
								Thread.sleep(t);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (ntimes != 0) {
						repetitionCounter++;
					}
				} while (mIsPlayingGif && (repetitionCounter <= ntimes));
				mIsPlayingGif = false;
			}
		}).start();
	}
	
	/**
	 * Deallocated the GIF resources assigned to this view.
	 * <p>
	 * the first frame may be still visible after deallocation
	 * </p>
	 */
	public void deallocate(){
		stopAnimating();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mGifDecoder=null;
		mTmpBitmap=null;
		
		
	}
}