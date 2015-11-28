package com.madrobot.app;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;

/**
 * Intent utilities
 * 
 * @author elton.stephen.kent
 * 
 */
public class IntentUtils {

	/**
	 * Check if the given intent is available/can be resolved.
	 * 
	 * @param context
	 * @param action
	 * @return
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (resolveInfo.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * Open the android market page for the given application's context.
	 * 
	 * @param context
	 *            application context
	 */
	public static void openMarketPage(Context context) {
		Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
				+ context.getPackageName()));
		context.startActivity(marketIntent);
	}

	/**
	 * Search android market
	 * 
	 * @param query
	 *            to search
	 * @param context
	 *            application context
	 */
	public static void searchMarket(String query, Context context) {
		Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q="
				+ query));
		context.startActivity(marketIntent);
	}

	/**
	 * Install the APK at the given file path.
	 * <p>
	 * Launches the package installer activity after setting the given APK file
	 * to be installed.
	 * </p>
	 * 
	 * @param context
	 * @param filePath
	 */
	public static void installAPK(Context context, final String filePath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(filePath)),
				"application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.setClassName("com.android.packageinstaller",
				"com.android.packageinstaller.PackageInstallerActivity");
		context.startActivity(intent);
	}

	/**
	 * Get Compatible activities for the given intent
	 * <p>
	 * Similar to <code>intent.getChooser</code>
	 * </p>
	 */
	public static List<ResolveInfo> getCompatibleActivities(Context context, Intent intent) {
		PackageManager packMan = context.getPackageManager();
		List<ResolveInfo> resolved = packMan.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return resolved;
	}

	/**
	 * Print the contents of this Intent
	 * <p>
	 * Print the intent's action,data,type,category and the extras.
	 * </p>
	 * 
	 * @param tag
	 * @param intent
	 */
	public static void logIntent(final String tag, final Intent intent) {
		android.util.Log.d(tag, "========================================================");
		android.util.Log.d(tag, "action=" + intent.getAction());
		android.util.Log.d(tag, "data=" + intent.getData());
		android.util.Log.d(tag, "type=" + intent.getType());
		android.util.Log.d(tag, "categories=" + intent.getCategories());
		// Log.d(tag, "sourceBounds=" + intent.getSourceBounds());
		android.util.Log.d(tag, "extras:");
		final android.os.Bundle extras = intent.getExtras();
		if (extras != null) {
			for (final String key : extras.keySet()) {
				final Object o = intent.getExtras().get(key);
				android.util.Log.d(tag, "  " + key + "=" + (o != null ? o.getClass() : null)
						+ "/" + o);
			}
		}
	}

	/**
	 * Checks if the given activity is started from the launcher.
	 * 
	 * @param a
	 *            activity instance
	 * @return whether the activity was started from launcher
	 */
	public static boolean isStartedFromLauncher(final android.app.Activity a) {
		final Intent intent = a.getIntent();
		final String intentAction = intent.getAction();
		return intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null
				&& intentAction.equals(Intent.ACTION_MAIN);
	}

	public static Drawable getIconForIntent(final Context context, Intent i) {
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> infos = pm.queryIntentActivities(i,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (infos.size() > 0) {
			return infos.get(0).loadIcon(pm);
		}
		return null;
	}
}
