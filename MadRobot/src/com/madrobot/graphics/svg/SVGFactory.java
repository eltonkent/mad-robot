package com.madrobot.graphics.svg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Picture;

import com.madrobot.io.CopyInputStream;

/**
 * Entry point for parsing SVG files for Android. Use one of the various static
 * methods for parsing SVGs by resource, asset or input stream. Optionally, a
 * single color can be searched and replaced in the SVG while parsing. You can
 * also parse an svg path directly.
 * 
 * @see #getSVGFromResource(Resources, int, int)
 * @see #getSVGFromAsset(AssetManager, String, int)
 * @see #getSVGFromString(String, int)
 * @see #getSVGFromInputStream(InputStream, int)
 * 
 */
public class SVGFactory {

	/**
	 * Parse SVG data from an input stream.
	 * 
	 * @param svgData
	 *            the input stream, with SVG XML data in UTF-8 character
	 *            encoding.
	 * @param zoomFactor
	 *            The factor by which the SVG should be zoomed. <code>100</code>
	 *            indicates that the SVG will be rendered in its actual size,
	 *            <code>200</code> means twice its size,<code>50</code> means
	 *            half its size and so on.
	 * @return the parsed SVG.
	 * @throws SVGException
	 *             if there is an error while parsing.
	 */
	public static SVG getSVGFromInputStream(InputStream svgData, int zoomFactor)
			throws SVGException {
		return SVGFactory.parse(svgData, 0, 0, false, zoomFactor);
	}

	/**
	 * Parse SVG data from a string.
	 * 
	 * @param svgData
	 *            the string containing SVG XML data.
	 * @param zoomFactor
	 *            The factor by which the SVG should be zoomed. <code>100</code>
	 *            indicates that the SVG will be rendered in its actual size,
	 *            <code>200</code> means twice its size,<code>50</code> means
	 *            half its size and so on.
	 * @return the parsed SVG.
	 * @throws SVGException
	 *             if there is an error while parsing.
	 */
	public static SVG getSVGFromString(String svgData, int zoomFactor) throws SVGException {
		return getSVGFromByteArray(svgData.getBytes(), zoomFactor);
	}

	/**
	 * Parse SVG from a byte array
	 * 
	 * @param svgData
	 * @param zoomFactor
	 *            The factor by which the SVG should be zoomed. <code>100</code>
	 *            indicates that the SVG will be rendered in its actual size,
	 *            <code>200</code> means twice its size,<code>50</code> means
	 *            half its size and so on.
	 * @return
	 * @throws SVGException
	 */
	public static SVG getSVGFromByteArray(byte[] svgData, int zoomFactor) throws SVGException {
		return SVGFactory.parse(new ByteArrayInputStream(svgData), 0, 0, false, zoomFactor);
	}

	/**
	 * Parse SVG data from an Android application resource.
	 * <p>
	 * This is a blocking operation and should be done preferably on a non-UI
	 * thread.
	 * </p>
	 * 
	 * @param resources
	 *            the Android context resources.
	 * @param resId
	 *            the ID of the raw resource SVG.
	 * @param zoomFactor
	 *            The factor by which the SVG should be zoomed. <code>100</code>
	 *            indicates that the SVG will be rendered in its actual size,
	 *            <code>200</code> means twice its size,<code>50</code> means
	 *            half its size and so on.
	 * @return the parsed SVG.
	 * @throws SVGException
	 *             if there is an error while parsing.
	 */
	public static SVG getSVGFromResource(Resources resources, int resId, int zoomFactor)
			throws SVGException {
		return SVGFactory.parse(resources.openRawResource(resId), 0, 0, false, zoomFactor);
	}

	/**
	 * Parse SVG data from an Android application asset.
	 * <p>
	 * This is a blocking operation and should be done preferably on a non-UI
	 * thread.
	 * </p>
	 * 
	 * @param assetMngr
	 *            the Android asset manager.
	 * @param svgPath
	 *            the path to the SVG file in the application's assets.
	 * @param zoomFactor
	 *            The factor by which the SVG should be zoomed. <code>100</code>
	 *            indicates that the SVG will be rendered in its actual size,
	 *            <code>200</code> means twice its size,<code>50</code> means
	 *            half its size and so on.
	 * @return the parsed SVG.
	 * @throws SVGException
	 *             if there is an error while parsing.
	 * @throws IOException
	 *             if there was a problem reading the file.
	 */
	public static SVG getSVGFromAsset(AssetManager assetMngr, String svgPath, int zoomFactor)
			throws SVGException, IOException {
		InputStream inputStream = assetMngr.open(svgPath);
		SVG svg = getSVGFromInputStream(inputStream, zoomFactor);
		inputStream.close();
		return svg;
	}

	/**
	 * Parse SVG data from an input stream, replacing a single color with
	 * another color.
	 * <p>
	 * This is a blocking operation and should be done preferably on a non-UI
	 * thread.
	 * </p>
	 * 
	 * @param svgData
	 *            the input stream, with SVG XML data in UTF-8 character
	 *            encoding.
	 * @param searchColor
	 *            the color in the SVG to replace.
	 * @param replaceColor
	 *            the color with which to replace the search color.
	 * @param zoomFactor
	 *            The factor by which the SVG should be zoomed. <code>100</code>
	 *            indicates that the SVG will be rendered in its actual size,
	 *            <code>200</code> means twice its size,<code>50</code> means
	 *            half its size and so on.
	 * @return the parsed SVG.
	 * @throws SVGException
	 *             if there is an error while parsing.
	 */
	public static SVG getSVGFromInputStream(InputStream svgData, int searchColor,
			int replaceColor, int zoomFactor) throws SVGException {
		return SVGFactory.parse(svgData, searchColor, replaceColor, false, zoomFactor);
	}

	/**
	 * Parse SVG data from a string.
	 * <p>
	 * This is a blocking operation and should be done preferably on a non-UI
	 * thread.
	 * </p>
	 * 
	 * @param svgData
	 *            the string containing SVG XML data.
	 * @param searchColor
	 *            the color in the SVG to replace.
	 * @param replaceColor
	 *            the color with which to replace the search color.
	 * @param zoomFactor
	 *            The factor by which the SVG should be zoomed. <code>100</code>
	 *            indicates that the SVG will be rendered in its actual size,
	 *            <code>200</code> means twice its size,<code>50</code> means
	 *            half its size and so on.
	 * @return the parsed SVG.
	 * @throws SVGException
	 *             if there is an error while parsing.
	 */
	public static SVG getSVGFromString(String svgData, int searchColor, int replaceColor,
			int zoomFactor) throws SVGException {
		return SVGFactory.parse(new ByteArrayInputStream(svgData.getBytes()), searchColor,
				replaceColor, false, zoomFactor);
	}

	/**
	 * Parse SVG data from an Android application resource.
	 * <p>
	 * This is a blocking operation and should be done preferably on a non-UI
	 * thread.
	 * </p>
	 * 
	 * @param resources
	 *            the Android context
	 * @param resId
	 *            the ID of the raw resource SVG.
	 * @param searchColor
	 *            the color in the SVG to replace.
	 * @param replaceColor
	 *            the color with which to replace the search color.
	 * @param zoomFactor
	 *            The factor by which the SVG should be zoomed. <code>100</code>
	 *            indicates that the SVG will be rendered in its actual size,
	 *            <code>200</code> means twice its size,<code>50</code> means
	 *            half its size and so on.
	 * @return the parsed SVG.
	 * @throws SVGException
	 *             if there is an error while parsing.
	 */
	public static SVG getSVGFromResource(Resources resources, int resId, int searchColor,
			int replaceColor, int zoomFactor) throws SVGException {
		return SVGFactory.parse(resources.openRawResource(resId), searchColor, replaceColor,
				false, zoomFactor);
	}

	/**
	 * Parse SVG data from an Android application asset.
	 * <p>
	 * This is a blocking operation and should be done preferably on a non-UI
	 * thread.
	 * </p>
	 * 
	 * @param assetMngr
	 *            the Android asset manager.
	 * @param svgPath
	 *            the path to the SVG file in the application's assets.
	 * @param searchColor
	 *            the color in the SVG to replace.
	 * @param replaceColor
	 *            the color with which to replace the search color.
	 * @param zoomFactor
	 *            The factor by which the SVG should be zoomed. <code>100</code>
	 *            indicates that the SVG will be rendered in its actual size,
	 *            <code>200</code> means twice its size,<code>50</code> means
	 *            half its size and so on.
	 * @return the parsed SVG.
	 * @throws SVGException
	 *             if there is an error while parsing.
	 * @throws IOException
	 *             if there was a problem reading the file.
	 */
	public static SVG getSVGFromAsset(AssetManager assetMngr, String svgPath, int searchColor,
			int replaceColor, int zoomFactor) throws SVGException, IOException {
		InputStream inputStream = assetMngr.open(svgPath);
		SVG svg = getSVGFromInputStream(inputStream, searchColor, replaceColor, zoomFactor);
		inputStream.close();
		return svg;
	}

	private static SVG parse(InputStream in, Integer searchColor, Integer replaceColor,
			boolean whiteMode, int zoomFactor) throws SVGException {
		if (zoomFactor < 0) {
			throw new IllegalArgumentException("Zoom factor should be > 0");
		}
		// Util.debug("Parsing SVG...");
		SVGHandler svgHandler = null;
		try {
			// long start = System.currentTimeMillis();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			final Picture picture = new Picture();
			svgHandler = new SVGHandler(picture, zoomFactor);
			svgHandler.setColorSwap(searchColor, replaceColor);
			svgHandler.setWhiteMode(whiteMode);

			CopyInputStream cin = new CopyInputStream(in);

			IDHandler idHandler = new IDHandler();
			xr.setContentHandler(idHandler);
			xr.parse(new InputSource(cin.getCopy()));
			svgHandler.idXml = idHandler.idXml;

			xr.setContentHandler(svgHandler);
			xr.parse(new InputSource(cin.getCopy()));
			// Util.debug("Parsing complete in " + (System.currentTimeMillis() -
			// start) + " millis.");
			SVG result = new SVG(picture, svgHandler.getMetaData(), svgHandler.bounds);
			// Skip bounds if it was an empty pic
			if (!Float.isInfinite(svgHandler.limits.top)) {
				result.setLimits(svgHandler.limits);
			}
			return result;
		} catch (Exception e) {
			throw new SVGException(e);
		}
	}

}