package com.madrobot.graphics.bitmap;

class Kernel implements Cloneable {
	/** The kernel width. */
	private final int width;

	/** The kernel height. */
	private final int height;

	/** Internal storage for the kernel's values. */
	private final float[] data;

	/**
	 * Creates a new <code>Kernel</code> instance with the specified dimensions
	 * and values. The first <code>width * height</code> values in the specified
	 * <code>data</code> array are copied to internal storage.
	 * 
	 * @param width
	 *            the kernel width.
	 * @param height
	 *            the kernel height.
	 * @param data
	 *            the source data array (<code>null</code> not permitted).
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>data.length</code> is less than
	 *             <code>width * height</code>.
	 * @throws IllegalArgumentException
	 *             if <code>width</code> or <code>height</code> is less than
	 *             zero.
	 * @throws NullPointerException
	 *             if <code>data</code> is <code>null</code>.
	 */
	Kernel(int width, int height, float[] data) throws IllegalArgumentException {
		this.width = width;
		this.height = height;
		if (data.length < width * height || width < 0 || height < 0)
			throw new IllegalArgumentException();
		this.data = new float[width * height];
		System.arraycopy(data, 0, this.data, 0, width * height);
	}

	/**
	 * Returns the x-origin for the kernel, which is calculated as
	 * <code>(width - 1) / 2</code>.
	 * 
	 * @return The x-origin for the kernel.
	 */
	final int getXOrigin() {
		return (width - 1) / 2;
	}

	/**
	 * Returns the y-origin for the kernel, which is calculated as
	 * <code>(height - 1) / 2</code>.
	 * 
	 * @return The y-origin for the kernel.
	 */
	final int getYOrigin() {
		return (height - 1) / 2;
	}

	/**
	 * Returns the kernel width (as supplied to the constructor).
	 * 
	 * @return The kernel width.
	 */
	final int getWidth() {
		return width;
	}

	/**
	 * Returns the kernel height (as supplied to the constructor).
	 * 
	 * @return The kernel height.
	 */
	final int getHeight() {
		return height;
	}

	/**
	 * Returns an array containing a copy of the kernel data. If the
	 * <code>data</code> argument is non-<code>null</code>, the kernel values
	 * are copied into it and then <code>data</code> is returned as the result.
	 * If the <code>data</code> argument is <code>null</code>, this method
	 * allocates a new array then populates and returns it.
	 * 
	 * @param data
	 *            an array to copy the return values into (if <code>null</code>,
	 *            a new array is allocated).
	 * 
	 * @return The array with copied values.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>data.length</code> is less than the kernel's
	 *             <code>width * height</code>.
	 */
	final float[] getKernelData(float[] data) throws IllegalArgumentException {
		if (data == null)
			return this.data.clone();

		if (data.length < this.data.length)
			throw new IllegalArgumentException();

		System.arraycopy(this.data, 0, data, 0, this.data.length);
		return data;
	}

	/**
	 * Returns a clone of this kernel.
	 * 
	 * @return a clone of this Kernel.
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw (Error) new InternalError().initCause(e); // Impossible
		}
	}
}