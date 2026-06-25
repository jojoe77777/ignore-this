package net.caffeinemc.mods.sodium.client.render.frapi.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;

public record FrapiItemRenderSubmit(
		PoseStack.Pose pose,
		ItemDisplayContext displayContext,
		int lightCoords,
		int overlayCoords,
		int outlineColor,
		int[] tintLayers,
		java.util.List<net.minecraft.client.resources.model.geometry.BakedQuad> quads,
		MeshView mesh,
		ItemStackRenderState.FoilType foilType,
		boolean translucent,
		boolean outline,
		float distanceToCameraSq) implements TranslucentSubmit {
	public static FrapiItemRenderSubmit solid(FabricSubmitNodeCollection.ExtendedItemSubmit submit) {
		return new FrapiItemRenderSubmit(
				submit.pose(),
				submit.displayContext(),
				submit.lightCoords(),
				submit.overlayCoords(),
				submit.outlineColor(),
				submit.tintLayers(),
				submit.quads(),
				submit.mesh(),
				submit.foilType(),
				false,
				false,
				TranslucentSubmit.computeDistanceToCameraSq(submit.pose().pose()));
	}

	public static FrapiItemRenderSubmit translucent(FabricSubmitNodeCollection.ExtendedItemSubmit submit) {
		return new FrapiItemRenderSubmit(
				submit.pose(),
				submit.displayContext(),
				submit.lightCoords(),
				submit.overlayCoords(),
				submit.outlineColor(),
				submit.tintLayers(),
				submit.quads(),
				submit.mesh(),
				submit.foilType(),
				true,
				false,
				TranslucentSubmit.computeDistanceToCameraSq(submit.pose().pose()));
	}

	public static FrapiItemRenderSubmit outline(FabricSubmitNodeCollection.ExtendedItemSubmit submit) {
		return new FrapiItemRenderSubmit(
				submit.pose(),
				submit.displayContext(),
				submit.lightCoords(),
				submit.overlayCoords(),
				submit.outlineColor(),
				submit.tintLayers(),
				submit.quads(),
				submit.mesh(),
				submit.foilType(),
				false,
				true,
				TranslucentSubmit.computeDistanceToCameraSq(submit.pose().pose()));
	}

	@Override
	public net.minecraft.client.renderer.feature.FeatureRendererType<FrapiItemRenderSubmit> featureType() {
		return FrapiItemFeatureRenderer.TYPE;
	}
}