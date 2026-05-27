package net.irisshaders.iris.mixin.rendertype;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PreparedRenderType.class)
public class MixinPreparedRenderType {
	@Inject(method = "drawFromBuffer(Lnet/minecraft/client/renderer/StagedVertexBuffer$ExecuteInfo;)V", at = @At("HEAD"))
	private void iris$beforeDrawFromExecuteInfo(StagedVertexBuffer.ExecuteInfo executeInfo, CallbackInfo ci) {
		OuterWrappedRenderType.beginDraw((PreparedRenderType) (Object) this);
	}

	@Inject(method = "drawFromBuffer(Lnet/minecraft/client/renderer/StagedVertexBuffer$ExecuteInfo;)V", at = @At("RETURN"))
	private void iris$afterDrawFromExecuteInfo(StagedVertexBuffer.ExecuteInfo executeInfo, CallbackInfo ci) {
		OuterWrappedRenderType.endDraw((PreparedRenderType) (Object) this);
	}

	@Inject(method = "drawFromBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/IndexType;III)V", at = @At("HEAD"))
	private void iris$beforeDrawDirect(GpuBuffer vertexBuffer, GpuBuffer indexBuffer, IndexType indexType, int baseVertex, int firstIndex, int indexCount, CallbackInfo ci) {
		OuterWrappedRenderType.beginDraw((PreparedRenderType) (Object) this);
	}

	@Inject(method = "drawFromBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/IndexType;III)V", at = @At("RETURN"))
	private void iris$afterDrawDirect(GpuBuffer vertexBuffer, GpuBuffer indexBuffer, IndexType indexType, int baseVertex, int firstIndex, int indexCount, CallbackInfo ci) {
		OuterWrappedRenderType.endDraw((PreparedRenderType) (Object) this);
	}
}