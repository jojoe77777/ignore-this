package net.irisshaders.iris.compat.sodium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IrisSodiumDebugLog {
	private static final Logger LOG = LoggerFactory.getLogger("SodiumPort");
	private static long lastWrapLog = 0L;

	private IrisSodiumDebugLog() {}

	public static void maybeLog(int frame, boolean isShadow, boolean stateBefore, boolean stateAfter, boolean needsShadowUpd) {
		long now = System.currentTimeMillis();
		if (now - lastWrapLog > 2000) {
			lastWrapLog = now;
			LOG.info("WRAP: frame={} isShadow={} stateBefore={} stateAfter={} shadowNeedsUpd={}",
				frame, isShadow, stateBefore, stateAfter, needsShadowUpd);
		}
	}
}
