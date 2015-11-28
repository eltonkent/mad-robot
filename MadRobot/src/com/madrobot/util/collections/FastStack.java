package com.madrobot.util.collections;

/**
 * An array-based stack implementation.
 * 
 */
public final class FastStack {

	private int pointer;
	private Object[] stack;

	public FastStack(int initialCapacity) {
		stack = new Object[initialCapacity];
	}

	public Object get(int i) {
		return stack[i];
	}

	public boolean hasStuff() {
		return pointer > 0;
	}

	public Object peek() {
		return pointer == 0 ? null : stack[pointer - 1];
	}

	public Object pop() {
		final Object result = stack[--pointer];
		stack[pointer] = null;
		return result;
	}

	public void popSilently() {
		stack[--pointer] = null;
	}

	public Object push(Object value) {
		if (pointer + 1 >= stack.length) {
			resizeStack(stack.length * 2);
		}
		stack[pointer++] = value;
		return value;
	}

	public Object replace(Object value) {
		final Object result = stack[pointer - 1];
		stack[pointer - 1] = value;
		return result;
	}

	public void replaceSilently(Object value) {
		stack[pointer - 1] = value;
	}

	private void resizeStack(int newCapacity) {
		Object[] newStack = new Object[newCapacity];
		System.arraycopy(stack, 0, newStack, 0, Math.min(pointer, newCapacity));
		stack = newStack;
	}

	public int size() {
		return pointer;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer("[");
		for (int i = 0; i < pointer; i++) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(stack[i]);
		}
		result.append(']');
		return result.toString();
	}
}
