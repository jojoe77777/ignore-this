package net.irisshaders.iris.mixin.entity_render_context;

import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelFeatureRenderer.class)
public class MixinModelFeatureRenderer {
	@Inject(method = "prepareModel", at = @At("HEAD"))
	private <S> void iris$set(ModelFeatureRenderer.Submit<S> modelSubmit, CallbackInfo ci) {
		ImmediateState.isPreparingEntityModels = true;
		((ModelStorage) (Object) modelSubmit).iris$set();
	}

	@Inject(method = "prepareModel", at = @At("RETURN"))
	private <S> void iris$unset(ModelFeatureRenderer.Submit<S> modelSubmit, CallbackInfo ci) {
		ImmediateState.isPreparingEntityModels = false;
	}

	@Inject(method = "buildGroup", at = @At("RETURN"))
	private void iris$clear(CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
	}
}
