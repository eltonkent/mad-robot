package com.madrobot.app;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public final class ApplicationUtils {

	/**
	 * Get the Dex
	 * 
	 * @param context
	 * @return
	 */
	public File getDexFileDirectory(Context context) {
		return context.getDir("dex", 0);
	}

	/**
	 * Utility method to test if a package is installed or not.
	 * 
	 * @param context
	 * @param packagName
	 * @return
	 */
	public static boolean isPackageInstalled(Context context, String packagName) {
		boolean rv = true;

		try {
			PackageManager pm = context.getPackageManager();
			pm.getPackageInfo(packagName, 0);
		} catch (NameNotFoundException e) {
			rv = false;
		}

		return rv;
	}

	public static int getAPINumber() {
		if (isAPILevelLower4())
			return 3;
		int version = 3;
		try {
			Class buildClass = Build.VERSION.class;
			Field sdkint = buildClass.getField("SDK_INT");
			version = sdkint.getInt(null);
		} catch (Exception ignore) {
		}
		return version;
	}

	/**
	 * Returns true when platform version is lower or equal to 1.5 Since prior
	 * to 1.5 there was no Build.VERSION.SDK_INT available.
	 * 
	 * @return
	 */
	public static boolean isAPILevelLower4() {
		return "1.5".compareTo(Build.VERSION.RELEASE) >= 0;
	}

	/**
	 * Check if the application with the given package name is running.
	 * <p>
	 * Eg: To check if the browser is running.<br/>
	 * <code>
	 * isApplicationRunning(getApplicationContext(),"com.android.browser");
	 * </code>
	 * </p>
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isApplicationRunning(Context context, String packageName) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> procInfos = manager
				.getRunningAppProcesses();
		for (int i = 0; i < procInfos.size(); i++) {
			if (procInfos.get(i).processName.equals(packageName)) {
				return true;
			}
		}
		return false;

	}
}
