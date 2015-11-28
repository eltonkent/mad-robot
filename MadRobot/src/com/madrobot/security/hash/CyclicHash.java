package com.madrobot.security.hash;

import java.util.Random;

public class CyclicHash {
	int hashvalues[] = new int[1 << 16];

	// myn is the length in characters of the blocks you want to hash
	public CyclicHash(int myn) {
		Random r = new Random();
		for (int k = 0; k < hashvalues.length; ++k)
			hashvalues[k] = r.nextInt();
		n = myn;
		if (n > wordsize) {
			throw new IllegalArgumentException();
		}

	}

	private int fastleftshiftn(int x) {
		return (x << n) | (x >>> (wordsize - n));
	}

	private static int fastleftshift1(int x) {
		return (x << 1) | (x >>> (wordsize - 1));
	}

	// add new character (useful to initiate the hasher)
	// to get a strongly universal hash value, you have to ignore the last or
	// first (n-1) bits.
	public int eat(char c) {
		hashvalue = fastleftshift1(hashvalue);
		hashvalue ^= hashvalues[c];
		return hashvalue;
	}

	// remove old character and add new one
	// to get a strongly universal hash value, you have to ignore the last or
	// first (n-1) bits.
	public int update(char outchar, char inchar) {
		int z = fastleftshiftn(hashvalues[outchar]);
		hashvalue = fastleftshift1(hashvalue) ^ z ^ hashvalues[inchar];
		return hashvalue;
	}

	public final static int wordsize = 32;
	public int hashvalue;
	int n;
	int myr;

}
