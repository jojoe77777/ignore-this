package net.irisshaders.iris.mixin.vertices.immediate;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.RenderTypeInterface;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderType.class)
public class MixinRenderType {
	@Inject(method = "format", at = @At("RETURN"), cancellable = true)
	private void iris$change(CallbackInfoReturnable<VertexFormat> cir) {
		if (!Iris.isPackInUseQuick() || !ImmediateState.renderWithExtendedVertexFormat || !ImmediateState.isRenderingLevel || ImmediateState.bypass || ImmediateState.temporarilyIgnorePass) {
			return;
		}

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
		if (!(pipeline instanceof IrisRenderingPipeline irisPipeline) || !irisPipeline.shouldOverrideShaders()) {
			return;
		}

		RenderPipeline renderPipeline = ((RenderTypeInterface) this).iris$getPipeline();
		ShaderKey shaderKey = IrisPipelines.getPipeline(irisPipeline, renderPipeline);
		if (shaderKey == null) {
			return;
		}

		VertexFormat vertexFormat = shaderKey.getVertexFormat();
		if (ImmediateState.isImmediateVertexExtensionDebugDisabled(vertexFormat)) {
			return;
		}

		if (vertexFormat == IrisVertexFormats.TERRAIN || vertexFormat == IrisVertexFormats.ENTITY || vertexFormat == IrisVertexFormats.GLYPH) {
			cir.setReturnValue(vertexFormat);
		}
	}
}
