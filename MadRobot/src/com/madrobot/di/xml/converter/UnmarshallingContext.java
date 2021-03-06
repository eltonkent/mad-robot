/*******************************************************************************
 * Copyright (c) 2012 MadRobot.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the GNU Lesser Public License v2.1
 *  which accompanies this distribution, and is available at
 *  http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *  
 *  Contributors:
 *  Elton Kent - initial API and implementation
 ******************************************************************************/

package com.madrobot.di.xml.converter;

public interface UnmarshallingContext extends DataHolder {

	void addCompletionCallback(Runnable work, int priority);

	Object convertAnother(Object current, Class type);

	/**
	 * @since 1.2
	 */
	Object convertAnother(Object current, Class type, Converter converter);

	Object currentObject();

	Class getRequiredType();

}
