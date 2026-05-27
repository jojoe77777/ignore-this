package net.irisshaders.iris.mixin;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.irisshaders.iris.compat.sodium.IrisServerInstr;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkSender.class)
public abstract class MixinPlayerChunkSender {
	@Shadow @Final private LongSet pendingChunks;
	@Shadow private float desiredChunksPerTick;
	@Shadow private float batchQuota;
	@Shadow private int unacknowledgedBatches;
	@Shadow private int maxUnacknowledgedBatches;

	private static long iris$lastSendLog = 0;
	private static int iris$sendCalls = 0;
	private static int iris$lastPending = -1;
	private static int iris$lastUnack = -1;
	private static float iris$lastDesired = -1f;
	private static float iris$lastQuota = -1f;
	private static int iris$lastMaxUnack = -1;

	@Inject(method = "markChunkPendingToSend", at = @At("HEAD"))
	private void iris$logPcsMark(LevelChunk chunk, CallbackInfo ci) {
		IrisServerInstr.pcsMark();
	}

	@Inject(method = "sendNextChunks", at = @At("HEAD"))
	private void iris$logSendNextChunks(ServerPlayer player, CallbackInfo ci) {
		iris$sendCalls++;
		iris$lastPending = pendingChunks.size();
		iris$lastUnack = unacknowledgedBatches;
		iris$lastDesired = desiredChunksPerTick;
		iris$lastQuota = batchQuota;
		iris$lastMaxUnack = maxUnacknowledgedBatches;
		long now = System.currentTimeMillis();
		if (now - iris$lastSendLog > 2000) {
			iris$lastSendLog = now;
			int calls = iris$sendCalls;
			iris$sendCalls = 0;
			org.slf4j.LoggerFactory.getLogger("SodiumPort").info(
				"SEND: calls={} pending={} unack={}/{} desired={} quota={}",
				calls, iris$lastPending, iris$lastUnack, iris$lastMaxUnack,
				String.format("%.2f", iris$lastDesired),
				String.format("%.2f", iris$lastQuota));
		}
	}

}
