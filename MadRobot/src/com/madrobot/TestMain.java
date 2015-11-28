package com.madrobot;

import com.madrobot.net.NetUtils;

/**
 * @serial exclude
 * @author ekent4
 * 
 */
public class TestMain {
	public static void main(String[] args) {
		System.out.println("Valid->"
				+ NetUtils.isValidEmailAddress("elton.kent@accenture.com"));
	}

}
