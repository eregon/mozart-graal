package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import java.util.HashMap;

public class OzDict extends HashMap<Object, Object> {

	private static final long serialVersionUID = -768886564649072349L;

	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}

	@TruffleBoundary
	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(key);
	}

	@TruffleBoundary
	@Override
	public Object get(Object key) {
		return super.get(key);
	}

	@TruffleBoundary
	@Override
	public Object put(Object key, Object value) {
		return super.put(key, value);
	}

	@TruffleBoundary
	@Override
	public Object remove(Object key) {
		return super.remove(key);
	}

	@TruffleBoundary
	@Override
	public void clear() {
		super.clear();
	}

}
