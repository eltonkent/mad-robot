package com.madrobot.util;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * Iterator of an Array of any type
 */
public class ArrayIterator implements Iterator {
	private final Object array;
	private int idx;
	private int length;

	public ArrayIterator(Object array) {
		this.array = array;
		length = Array.getLength(array);
	}

	@Override
	public boolean hasNext() {
		return idx < length;
	}

	@Override
	public Object next() {
		return Array.get(array, idx++);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove from array");
	}
}
