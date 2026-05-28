package net.irisshaders.iris.mixin;

import net.irisshaders.iris.Iris;
import net.minecraft.client.multiplayer.ChunkBatchSizeCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC measures wall-clock time between ClientboundChunkBatchStart and
 * ClientboundChunkBatchFinished to compute "nanos per chunk", which the
 * client then reports to the server to size future batches. When iris is
 * rendering a shadow pass plus a heavy main pass, each tick can take 20-30ms,
 * which inflates the measurement and causes the integrated server to throttle
 * chunk delivery to almost zero. The result: terrain stops loading as the
 * player flies.
 *
 * Skip the timing update only while iris has a pipeline active so the calculator
 * keeps its default ~2ms/chunk estimate, and clamp getDesiredChunksPerTick
 * to never report below the unthrottled rate.
 */
@Mixin(ChunkBatchSizeCalculator.class)
public abstract class MixinChunkBatchSizeCalculator {
	@Inject(method = "onBatchFinished", at = @At("HEAD"), cancellable = true)
	private void iris$skipMeasurementWithShaders(int batchSize, CallbackInfo ci) {
		if (!Iris.isPackInUseQuick()) {
			return;
		}

		ci.cancel();
	}

	@Inject(method = "getDesiredChunksPerTick", at = @At("HEAD"), cancellable = true)
	private void iris$floorChunkRate(CallbackInfoReturnable<Float> cir) {
		if (!Iris.isPackInUseQuick()) {
			return;
		}

		cir.setReturnValue(20.0f);
	}
}
