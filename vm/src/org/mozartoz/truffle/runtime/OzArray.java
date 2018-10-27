package org.mozartoz.truffle.runtime;

public class OzArray extends OzValue {

	private final int low;
	private final Object[] store;

	public OzArray(int low, int width, Object initialValue) {
		this.low = low;
		this.store = new Object[width];

		for (int i = 0; i < store.length; i++) {
			store[i] = initialValue;
		}
	}

	public int getLow() {
		return low;
	}

	public int getHigh() {
		return low + store.length - 1;
	}

	public int getWidth() {
		return store.length;
	}

	public Object get(int index) {
		return store[index - low];
	}

	public void set(int index, Object newValue) {
		store[index - low] = newValue;
	}

}
