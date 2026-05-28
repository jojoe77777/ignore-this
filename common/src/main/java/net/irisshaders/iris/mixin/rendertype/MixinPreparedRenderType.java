package net.irisshaders.iris.mixin.rendertype;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PreparedRenderType.class)
public class MixinPreparedRenderType {
	@WrapMethod(method = "drawFromBuffer(Lnet/minecraft/client/renderer/StagedVertexBuffer$ExecuteInfo;)V")
	private void iris$wrapDrawFromExecuteInfo(StagedVertexBuffer.ExecuteInfo executeInfo, Operation<Void> original) {
		PreparedRenderType preparedRenderType = (PreparedRenderType) (Object) this;
		OuterWrappedRenderType.beginDraw(preparedRenderType);

		try {
			original.call(executeInfo);
		} finally {
			OuterWrappedRenderType.endDraw(preparedRenderType);
		}
	}

	@WrapMethod(method = "drawFromBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/IndexType;III)V")
	private void iris$wrapDrawDirect(GpuBuffer vertexBuffer, GpuBuffer indexBuffer, IndexType indexType, int baseVertex, int firstIndex, int indexCount, Operation<Void> original) {
		PreparedRenderType preparedRenderType = (PreparedRenderType) (Object) this;
		OuterWrappedRenderType.beginDraw(preparedRenderType);

		try {
			original.call(vertexBuffer, indexBuffer, indexType, baseVertex, firstIndex, indexCount);
		} finally {
			OuterWrappedRenderType.endDraw(preparedRenderType);
		}
	}
}