/*******************************************************************************
 * Copyright (c) 2011 MadRobot.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *  Elton Kent - initial API and implementation
 ******************************************************************************/
package com.madrobot.graphics.svg;

/**
 * Runtime exception thrown when there is a problem parsing an SVG.
 * 
 */
public class SVGException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SVGException(String s) {
		super(s);
	}

	public SVGException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public SVGException(Throwable throwable) {
		super(throwable);
	}
}
