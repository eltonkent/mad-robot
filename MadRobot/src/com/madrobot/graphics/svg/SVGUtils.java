package com.madrobot.graphics.svg;

import java.util.List;

public class SVGUtils {
	/**
	 * Convert an closed polygon to an SVG
	 * 
	 * @param closedPolygons
	 * @param width
	 *            of the SVG
	 * @param height
	 *            of the SVG
	 * @return String representing the SVG of the polygon
	 */
	public static String arraysSVG(List<float[]> closedPolygons, int width, int height) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>").append("\n");
		sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" "
				+ "xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
				+ "xmlns:ev=\"http://www.w3.org/2001/xml-events\" "
				+ "version=\"1.1\" baseProfile=\"full\" width=\"" + width + "\" height=\""
				+ height + "\">");

		for (float[] fs : closedPolygons) {
			sb.append("<polygon points=\"");
			for (float f : fs) {
				sb.append(f).append(" ");
			}
			sb.append("\" />");
		}

		sb.append("</svg>");

		return sb.toString();
	}
}
