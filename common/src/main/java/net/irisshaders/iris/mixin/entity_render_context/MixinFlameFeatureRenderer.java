package net.irisshaders.iris.mixin.entity_render_context;

import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FlameFeatureRenderer.class)
public class MixinFlameFeatureRenderer {
	@Unique
	private static final NamespacedId flameId = new NamespacedId("minecraft", "entity_flame");

	@Inject(method = "buildGroup", at = @At("HEAD"))
	private void iris$setFlame(FeatureFrameContext featureFrameContext, List<FlameFeatureRenderer.Submit> submits, CallbackInfo ci) {
		if (WorldRenderingSettings.INSTANCE.getEntityIds() != null) {
			CapturedRenderingState.INSTANCE.setCurrentEntity(WorldRenderingSettings.INSTANCE.getEntityIds().applyAsInt(flameId));
		}
	}

	@Inject(method = "buildGroup", at = @At("RETURN"))
	private void iris$setFlame2(FeatureFrameContext featureFrameContext, List<FlameFeatureRenderer.Submit> submits, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);

	}
}
