package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.irisshaders.iris.compat.sodium.IrisServerInstr;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public abstract class MixinChunkMap {

	@Inject(method = "markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;)V",
		at = @At("HEAD"))
	private void iris$logMarkPendingPos(ServerPlayer p, net.minecraft.world.level.ChunkPos pos, CallbackInfo ci) {
		if (IrisServerInstr.ENABLED) {
			IrisServerInstr.markPendingInstance();
		}
	}

	@Inject(method = "markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;)V",
		at = @At("HEAD"))
	private static void iris$logMarkPendingChunk(ServerPlayer p, net.minecraft.world.level.chunk.LevelChunk chunk, CallbackInfo ci) {
		if (IrisServerInstr.ENABLED) {
			IrisServerInstr.markPendingStatic();
		}
	}

	@Inject(method = "move(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"))
	private void iris$logMove(ServerPlayer p, CallbackInfo ci) {
		if (IrisServerInstr.ENABLED) {
			IrisServerInstr.moveCall(p.getX(), p.getY(), p.getZ());
		}
	}

	@Inject(method = "updateChunkTracking", at = @At("HEAD"))
	private void iris$logUpdateChunkTracking(ServerPlayer p, CallbackInfo ci) {
		if (IrisServerInstr.ENABLED) {
			IrisServerInstr.updateChunkTracking();
		}
	}

	@Inject(method = "updatePlayerPos", at = @At("HEAD"))
	private void iris$logUpdatePlayerPos(ServerPlayer p, CallbackInfo ci) {
		if (IrisServerInstr.ENABLED) {
			IrisServerInstr.updatePlayerPos();
		}
	}

	@Inject(method = "updatePlayerStatus", at = @At("HEAD"))
	private void iris$logUpdatePlayerStatus(ServerPlayer p, boolean added, CallbackInfo ci) {
		if (IrisServerInstr.ENABLED) {
			IrisServerInstr.updatePlayerStatus();
		}
	}

	@ModifyExpressionValue(method = "getChunkToSend",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ChunkMap;getVisibleChunkIfPresent(J)Lnet/minecraft/server/level/ChunkHolder;"))
	private ChunkHolder iris$peekHolderInGCTS(ChunkHolder original) {
		if (original == null && IrisServerInstr.ENABLED) {
			IrisServerInstr.gctsVisNull();
		}
		return original;
	}

	@ModifyReturnValue(method = "getChunkToSend", at = @At("RETURN"))
	private LevelChunk iris$logGetChunkToSend(LevelChunk original) {
		if (IrisServerInstr.ENABLED) {
			if (original == null) {
				IrisServerInstr.gctsChunkNull();
			} else {
				IrisServerInstr.gctsOk();
			}
		}
		return original;
	}
}
