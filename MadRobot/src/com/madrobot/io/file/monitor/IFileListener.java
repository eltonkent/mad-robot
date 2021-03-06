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
package com.madrobot.io.file.monitor;

import java.io.File;

public interface IFileListener {
	/**
	 * 
	 * 
	 * @param status
	 *            The integer describing the event. As defined in
	 *            {@code android.os.FileObserver}
	 * @param file
	 *            The file which the event has occured
	 */
	public void onFileStatusChanged(int status, File file);
}
