package net.irisshaders.iris.compat.sodium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IrisServerInstr {
	private static final Logger LOG = LoggerFactory.getLogger("SodiumPort");

	private static long lastLog = 0;
	private static int markPendingInstance = 0;
	private static int markPendingStatic = 0;
	private static int moveCalls = 0;
	private static int updateChunkTracking = 0;
	private static int updatePlayerPos = 0;
	private static int updatePlayerStatus = 0;
	private static int gcts_visNull = 0;
	private static int gcts_chunkNull = 0;
	private static int gcts_ok = 0;
	private static int pcsMark = 0;
	private static double lastMoveX = Double.NaN;
	private static double lastMoveY = Double.NaN;
	private static double lastMoveZ = Double.NaN;

	public static synchronized void markPendingInstance() { markPendingInstance++; maybeLog(); }
	public static synchronized void markPendingStatic() { markPendingStatic++; maybeLog(); }
	public static synchronized void moveCall(double x, double y, double z) {
		moveCalls++;
		lastMoveX = x; lastMoveY = y; lastMoveZ = z;
		maybeLog();
	}
	public static synchronized void updateChunkTracking() { updateChunkTracking++; maybeLog(); }
	public static synchronized void updatePlayerPos() { updatePlayerPos++; maybeLog(); }
	public static synchronized void updatePlayerStatus() { updatePlayerStatus++; maybeLog(); }
	public static synchronized void gctsVisNull() { gcts_visNull++; maybeLog(); }
	public static synchronized void gctsChunkNull() { gcts_chunkNull++; maybeLog(); }
	public static synchronized void gctsOk() { gcts_ok++; maybeLog(); }
	public static synchronized void pcsMark() { pcsMark++; maybeLog(); }

	private static void maybeLog() {
		long now = System.currentTimeMillis();
		if (now - lastLog > 2000) {
			lastLog = now;
			LOG.info("SRV: markPend(inst={},stat={}) pcsMark={} gcts(ok={},visNull={},chunkNull={}) move={} updTrk={} updPos={} updStatus={} lastPos=({},{},{})",
				markPendingInstance, markPendingStatic, pcsMark,
				gcts_ok, gcts_visNull, gcts_chunkNull,
				moveCalls, updateChunkTracking,
				updatePlayerPos, updatePlayerStatus,
				String.format("%.1f", lastMoveX),
				String.format("%.1f", lastMoveY),
				String.format("%.1f", lastMoveZ));
			markPendingInstance = 0;
			markPendingStatic = 0;
			pcsMark = 0;
			gcts_visNull = 0;
			gcts_chunkNull = 0;
			gcts_ok = 0;
			moveCalls = 0;
			updateChunkTracking = 0;
			updatePlayerPos = 0;
			updatePlayerStatus = 0;
		}
	}
}
