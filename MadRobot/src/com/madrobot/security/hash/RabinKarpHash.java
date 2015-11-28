package com.madrobot.security.hash;

import java.util.Random;

public class RabinKarpHash {

	private int hashvalues[] = new int[1 << 16];

	// myn is the length in characters of the blocks you want to hash
	public RabinKarpHash(int myn) {
		Random r = new Random();
		for (int k = 0; k < hashvalues.length; ++k)
			hashvalues[k] = r.nextInt();
		n = myn;
		BtoN = 1;
		for (int i = 0; i < n; ++i) {
			BtoN *= B;
		}
	}

	// add new character (useful to initiate the hasher)
	// return 32 bits (not even universal)
	public int eat(char c) {
		hashvalue = B * hashvalue + hashvalues[c];
		return hashvalue;
	}

	// remove old character and add new one
	// return 32 bits (not even universal)
	public int update(char outchar, char inchar) {
		hashvalue = B * hashvalue + hashvalues[inchar] - BtoN * hashvalues[outchar];
		return hashvalue;
	}

	// this is purely for testing purposes

	public int hashvalue;
	int n;
	int BtoN;
	final static int B = 31;

}
