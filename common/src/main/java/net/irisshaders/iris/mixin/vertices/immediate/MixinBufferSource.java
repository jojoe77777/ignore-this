package net.irisshaders.iris.mixin.vertices.immediate;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.StagedVertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Quick optimization to disable the extended vertex format outside of level rendering if we're using a BufferSource.
 * This is a heuristic that should hopefully work almost always because of how people use BufferSource.
 */
@Mixin(StagedVertexBuffer.class)
public class MixinBufferSource {
	@Inject(method = "getVertexBuilder", at = @At("HEAD"))
	private void iris$beforeGetVertexBuilder(StagedVertexBuffer.Draw draw, CallbackInfoReturnable<VertexConsumer> cir) {
		ImmediateState.skipExtension.set(iris$notRenderingLevel());
	}

	@Inject(method = "getVertexBuilder", at = @At("RETURN"))
	private void iris$afterGetVertexBuilder(StagedVertexBuffer.Draw draw, CallbackInfoReturnable<VertexConsumer> cir) {
		ImmediateState.skipExtension.set(false);
	}

	@Inject(method = "upload", at = @At("HEAD"))
	private void iris$beforeFlushBuffer(CallbackInfo ci) {
		if (iris$notRenderingLevel()) {
			ImmediateState.renderWithExtendedVertexFormat = false;
		}
	}

	@Inject(method = "upload", at = @At("RETURN"))
	private void iris$afterFlushBuffer(CallbackInfo ci) {
		if (iris$notRenderingLevel()) {
			ImmediateState.renderWithExtendedVertexFormat = true;
		}
	}

	@Unique
	private boolean iris$notRenderingLevel() {
		return !ImmediateState.isRenderingLevel;
	}
}
