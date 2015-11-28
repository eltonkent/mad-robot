package com.madrobot.ui.widget.imageview;

/**
 * Helps notifying the loading of SVG,GIF imageviews
 * 
 * @author ekent4
 * 
 */
public interface LoadingCallback {

	public void onResourceAssigned();

	public void onResourceLoaded();

}
