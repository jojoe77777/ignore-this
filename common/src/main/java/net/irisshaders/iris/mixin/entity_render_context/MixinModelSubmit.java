package net.irisshaders.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelFeatureRenderer.Submit.class)
public class MixinModelSubmit implements ModelStorage {
	@Unique
	private int entityId, beId, itemId;

	@Unique
	private boolean isRenderingBEs;

	@Override
	public void iris$capture() {
		entityId = CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
		beId = CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
		itemId = CapturedRenderingState.INSTANCE.getCurrentRenderedItem();
		isRenderingBEs = ImmediateState.isRenderingBEs;
	}

	@Override
	public void iris$set() {
		CapturedRenderingState.INSTANCE.setCurrentEntity(entityId);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(beId);
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(itemId);
	}

	@Override
	public boolean iris$wasBE() {
		return isRenderingBEs;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private <S> void iris$captureOnInit(RenderType renderType, PoseStack.Pose pose, Model<? super S> model, S state, int lightCoords, int overlayCoords, int tintedColor, UvMapping sprite, PoseStack.Pose sheetedDecalPose, CallbackInfo ci) {
		iris$capture();
	}
}
