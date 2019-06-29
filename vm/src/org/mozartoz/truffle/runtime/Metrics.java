package org.mozartoz.truffle.runtime;

import org.mozartoz.truffle.Options;

public class Metrics {

	private static long last = System.currentTimeMillis();

	public static void reset() {
		last = System.currentTimeMillis();
	}

	public static void tick(String desc) {
		if (OzLanguage.getOptions().get(Options.MEASURE_STARTUP)) {
			long now = System.currentTimeMillis();
			long duration = now - last;
			if (duration > 5) {
				System.out.println(String.format("%4d", duration) + " " + desc);
			}
			last = now;
		}
	}

}
