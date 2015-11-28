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
package com.madrobot.geom;

public class Angle {



	private float anglePercent;

	public Angle(float rad) {
		anglePercent = (float) (rad / (2 * Math.PI));
	}

	public Angle add(Angle a) {
		float newAngleRad = (float) ((anglePercent + a.anglePercent) * 2 * Math.PI);
		return new Angle(newAngleRad);
	}

	public float getDegrees() {
		return (anglePercent * 360);
	}

	public float getRadians() {
		return (float) (anglePercent * 2 * Math.PI);
	}

	public Angle subtract(Angle a) {
		float newAngleRad = (float) ((anglePercent - a.anglePercent) * 2 * Math.PI);
		return new Angle(newAngleRad);
	}

}
