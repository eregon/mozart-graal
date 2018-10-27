package org.mozartoz.truffle.runtime;

import java.io.InputStream;
import java.io.OutputStream;

public class OzIO extends OzValue {

	private final InputStream inputStream;
	private final OutputStream outputStream;

	public OzIO(InputStream inputStream) {
		this.inputStream = inputStream;
		this.outputStream = null;
	}

	public OzIO(OutputStream outputStream) {
		this.inputStream = null;
		this.outputStream = outputStream;
	}

	public InputStream getInputStream() {
		assert inputStream != null;
		return inputStream;
	}

	public OutputStream getOutputStream() {
		assert outputStream != null;
		return outputStream;
	}

}
