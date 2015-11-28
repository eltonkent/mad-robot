package com.madrobot.util.collections;

/**
 * A simple pool implementation.
 * 
 */
public class Pool {

	public interface Factory {
		public Object newInstance();
	}

	private final Factory factory;
	private final int initialPoolSize;
	private final int maxPoolSize;
	private transient Object mutex = new Object();
	private transient int nextAvailable;
	private transient Object[] pool;

	public Pool(int initialPoolSize, int maxPoolSize, Factory factory) {
		this.initialPoolSize = initialPoolSize;
		this.maxPoolSize = maxPoolSize;
		this.factory = factory;
	}

	public Object fetchFromPool() {
		Object result;
		synchronized (mutex) {
			if (pool == null) {
				pool = new Object[maxPoolSize];
				for (nextAvailable = initialPoolSize; nextAvailable > 0;) {
					putInPool(factory.newInstance());
				}
			}
			while (nextAvailable == maxPoolSize) {
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					throw new RuntimeException("Interrupted whilst waiting "
							+ "for a free item in the pool : " + e.getMessage());
				}
			}
			result = pool[nextAvailable++];
			if (result == null) {
				result = factory.newInstance();
				putInPool(result);
				++nextAvailable;
			}
		}
		return result;
	}

	public void putInPool(Object object) {
		synchronized (mutex) {
			pool[--nextAvailable] = object;
			mutex.notify();
		}
	}

	private Object readResolve() {
		mutex = new Object();
		return this;
	}
}
