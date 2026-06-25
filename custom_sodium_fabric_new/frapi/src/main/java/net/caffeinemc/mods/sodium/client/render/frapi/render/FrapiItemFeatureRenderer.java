package net.caffeinemc.mods.sodium.client.render.frapi.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.MatrixUtil;
import net.caffeinemc.mods.sodium.client.render.frapi.wrapper.MutableQuadViewWrapper;
import net.caffeinemc.mods.sodium.client.render.model.EncodingFormat;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class FrapiItemFeatureRenderer extends RenderTypeFeatureRenderer<FrapiItemRenderSubmit> {
	public static final FeatureRendererType<FrapiItemRenderSubmit> TYPE = FeatureRendererType.create("SodiumFrapiItem");

	private final MutableQuadViewWrapper emitter;
	private FrapiItemRenderSubmit submit;
	private PoseStack.@Nullable Pose foilDecalPose;

	public FrapiItemFeatureRenderer() {
		emitter = new MutableQuadViewWrapper(null);

		var quad = new MutableQuadViewImpl() {
			{
				data = new int[EncodingFormat.TOTAL_STRIDE];
				clear();
			}

			@Override
			public void emitDirectly() {
				bufferQuad(emitter);
			}
		};

		emitter.setDelegate(quad);
	}

	@Override
	protected void buildGroup(FeatureFrameContext context, List<FrapiItemRenderSubmit> submits) {
		for (FrapiItemRenderSubmit submit : submits) {
			this.submit = submit;
			this.foilDecalPose = null;
			bufferQuads(submit.quads(), submit.mesh());
		}

		this.submit = null;
		this.foilDecalPose = null;
	}

	private void bufferQuads(List<BakedQuad> vanillaQuads, MeshView mesh) {
		QuadEmitter emitter = this.emitter;
		emitter.clear();

		for (int i = 0; i < vanillaQuads.size(); i++) {
			BakedQuad quad = vanillaQuads.get(i);
			emitter.fromBakedQuad(quad);
			emitter.emit();
		}

		if (mesh != null) {
			mesh.outputTo(emitter);
		}
	}

	private void bufferQuad(MutableQuadViewWrapper quad) {
		FrapiItemRenderSubmit submit = this.submit;
		RenderType renderType = quad.itemRenderType();

		if (submit.outline()) {
			RenderType outlineRenderType = renderType.outline().orElse(null);

			if (outlineRenderType == null) {
				return;
			}

			for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
				quad.color(vertexIndex, submit.outlineColor());
				quad.lightmap(vertexIndex, LightCoordsUtil.FULL_BRIGHT);
			}

			quad.buffer(OverlayTexture.NO_OVERLAY, submit.pose(), getVertexBuilder(outlineRenderType));
			return;
		}

		if (renderType.hasBlending() != submit.translucent()) {
			return;
		}

		shadeQuad(quad, quad.emissive(), submit.lightCoords());
		tintQuad(quad, submit.tintLayers());

		ItemStackRenderState.FoilType foilType = quad.foilType() == null ? submit.foilType() : quad.foilType();

		if (foilType != ItemStackRenderState.FoilType.NONE) {
			PoseStack.Pose foilDecalPose;

			if (foilType == ItemStackRenderState.FoilType.SPECIAL) {
				if (this.foilDecalPose == null) {
					this.foilDecalPose = computeFoilDecalPose(submit.displayContext(), submit.pose());
				}

				foilDecalPose = this.foilDecalPose;
			} else {
				foilDecalPose = null;
			}

			VertexConsumer foilBuffer = getFoilBuffer(renderType, foilDecalPose);
			quad.buffer(submit.overlayCoords(), submit.pose(), foilBuffer);
		}

		quad.buffer(submit.overlayCoords(), submit.pose(), getVertexBuilder(renderType));
	}

	private void shadeQuad(MutableQuadViewWrapper quad, boolean emissive, int lightCoords) {
		if (emissive) {
			quad.lightmap(LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT);
		} else {
			quad.minLightmap(lightCoords);
		}
	}

	private void tintQuad(MutableQuadViewWrapper quad, int[] tintLayers) {
		int tintIndex = quad.tintIndex();

		if (tintIndex >= 0 && tintIndex < tintLayers.length) {
			quad.multiplyColor(tintLayers[tintIndex]);
		}
	}

	private VertexConsumer getFoilBuffer(RenderType renderType, PoseStack.@Nullable Pose foilDecalPose) {
		RenderType foilRenderType = useTransparentGlint(renderType) ? RenderTypes.glintTranslucent() : RenderTypes.glint();
		VertexConsumer foilBuffer = getVertexBuilder(foilRenderType);

		if (foilDecalPose != null) {
			foilBuffer = new SheetedDecalTextureGenerator(foilBuffer, foilDecalPose, 0.0078125f);
		}

		return foilBuffer;
	}

	private static PoseStack.Pose computeFoilDecalPose(ItemDisplayContext type, PoseStack.Pose pose) {
		PoseStack.Pose foilDecalPose = pose.copy();

		if (type == ItemDisplayContext.GUI) {
			MatrixUtil.mulComponentWise(foilDecalPose.pose(), 0.5f);
		} else if (type.firstPerson()) {
			MatrixUtil.mulComponentWise(foilDecalPose.pose(), 0.75f);
		}

		return foilDecalPose;
	}

	private static boolean useTransparentGlint(RenderType renderType) {
		return Minecraft.getInstance().gameRenderer.gameRenderState().useShaderTransparency() && renderType.outputTarget() == OutputTarget.ITEM_ENTITY_TARGET;
	}
}