package net.irisshaders.iris.mixin.entity_render_context;

import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFeatureRenderer.class)
public class MixinItemFeatureRenderer {
	@Inject(method = "prepareSubmit", at = @At("HEAD"))
	private void iris$set(ItemFeatureRenderer.Submit itemSubmit, boolean outline, CallbackInfo ci) {
		ImmediateState.isPreparingItemModels = true;
		((ModelStorage) (Object) itemSubmit).iris$set();
	}

	@Inject(method = "prepareSubmit", at = @At("RETURN"))
	private void iris$unset(ItemFeatureRenderer.Submit itemSubmit, boolean outline, CallbackInfo ci) {
		ImmediateState.isPreparingItemModels = false;
	}

	@Inject(method = "buildGroup", at = @At("RETURN"))
	private void iris$clear(CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
	}
}
