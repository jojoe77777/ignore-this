package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(RenderPipeline.class)
public class MixinRenderPipeline {
	@Inject(method = "getVertexFormatBinding", at = @At("RETURN"), cancellable = true)
	private void iris$change(int slot, CallbackInfoReturnable<VertexFormat> cir) {
		if (slot != 0) return;
		if (!Iris.isPackInUseQuick() || !ImmediateState.isRenderingLevel || ImmediateState.bypass || ImmediateState.temporarilyIgnorePass) {
			return;
		}

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
		if (!(pipeline instanceof IrisRenderingPipeline irisPipeline) || !irisPipeline.shouldOverrideShaders()) {
			return;
		}

		ShaderKey shaderKey = IrisPipelines.getPipeline(irisPipeline, (RenderPipeline) (Object) this);
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
