package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
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
		if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel) {
			VertexFormat vf = cir.getReturnValue();
			if (vf == null) return;
			if (vf.equals(DefaultVertexFormat.BLOCK)) {
				cir.setReturnValue(IrisVertexFormats.TERRAIN);
			} else if (vf.equals(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)) {
				cir.setReturnValue(IrisVertexFormats.GLYPH);
			} else if (vf.equals(DefaultVertexFormat.ENTITY)) {
				cir.setReturnValue(IrisVertexFormats.ENTITY);
			}
		}
	}
}
