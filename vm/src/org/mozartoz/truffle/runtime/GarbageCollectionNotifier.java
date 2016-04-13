package org.mozartoz.truffle.runtime;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.InstanceNotFoundException;

public class GarbageCollectionNotifier {

	private static long lastActiveSize = 0L;

	public static long getLastActiveSize() {
		return lastActiveSize;
	}

	private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

	public static void register() {
		for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
			try {
				ManagementFactory.getPlatformMBeanServer().addNotificationListener(gcBean.getObjectName(), (notification, handback) -> {
					lastActiveSize = MEMORY_MX_BEAN.getHeapMemoryUsage().getUsed();
				}, null, null);
			} catch (InstanceNotFoundException e) {
				System.err.println("GC monitoring disabled");
				e.printStackTrace();
			}
		}
	}

}
