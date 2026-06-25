package net.caffeinemc.mods.sodium.mixin.frapi;

import net.caffeinemc.mods.sodium.client.render.frapi.render.FrapiItemFeatureRenderer;
import net.caffeinemc.mods.sodium.client.render.frapi.render.FrapiItemRenderSubmit;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRendererMap;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FeatureRenderDispatcher.class)
abstract class FeatureRenderDispatcherMixin {
	@Shadow
	@Final
	private FeatureRendererMap featureRenderers;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(RenderBuffers renderBuffers, ModelManager modelManager, AtlasManager atlasManager, Font font, GameRenderState gameRenderState, CallbackInfo ci) {
		this.featureRenderers.put(FrapiItemFeatureRenderer.TYPE, new FrapiItemFeatureRenderer());
	}

	@Inject(
			method = "prepareFrameWithContext",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/SubmitNodeStorage;drainPhases(Ljava/util/function/Consumer;)V"
			)
	)
	private void onPrepareFrameWithContext(FeatureFrameContext context, SubmitNodeStorage submitNodeStorage, CallbackInfoReturnable<FeatureRenderDispatcher.PreparedFrame> cir) {
		for (SubmitNodeCollection collection : submitNodeStorage.getSubmitsPerOrder().values()) {
			var extendedItemSubmits = ((FabricSubmitNodeCollection) collection).getExtendedItemSubmits();

			if (extendedItemSubmits.isEmpty()) {
				continue;
			}

			for (var submit : extendedItemSubmits) {
				collection.solid.submit(FrapiItemRenderSubmit.solid(submit));
				collection.translucentBlocksAndItems.submit(FrapiItemRenderSubmit.translucent(submit));

				if (submit.outlineColor() != 0) {
					collection.outline.submit(FrapiItemRenderSubmit.outline(submit));
				}
			}

			extendedItemSubmits.clear();
		}
	}
}