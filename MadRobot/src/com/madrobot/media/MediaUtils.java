package com.madrobot.media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore.Images;

public class MediaUtils {

	/**
	 * Get all the images in SDcard/Gallery
	 * 
	 * @param context
	 */
	public List<File> getAllExternalImages(Context context) {
		Cursor c = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, null,
				null, null, null);
		if (c != null) {
			c.moveToFirst();
			List<File> imageList = new ArrayList<File>();
			while (c.isAfterLast() == false) {
				long id = c.getLong(c.getColumnIndex(BaseColumns._ID));
				Uri imageUri = Uri.parse(Images.Media.EXTERNAL_CONTENT_URI + "/" + id);
				String uri = imageUri.toString();
				if (uri.startsWith(ContentResolver.SCHEME_CONTENT)
						|| uri.startsWith(ContentResolver.SCHEME_FILE)) {
					imageList.add(new File(uri));
				}
				c.moveToNext();
			}
			return imageList;

		}
		return null;

	}

}
