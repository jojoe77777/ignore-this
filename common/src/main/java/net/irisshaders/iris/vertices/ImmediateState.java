package net.irisshaders.iris.vertices;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;

/**
 * Some annoying global state needed for rendering.
 */
public class ImmediateState {
	private static final boolean DEBUG_DISABLE_IMMEDIATE_TERRAIN_EXTENSION = false;
	private static final boolean DEBUG_DISABLE_IMMEDIATE_ENTITY_EXTENSION = true;
	private static final boolean DEBUG_DISABLE_IMMEDIATE_GLYPH_EXTENSION = false;

	public static final ThreadLocal<Boolean> skipExtension = ThreadLocal.withInitial(() -> false);
	public static boolean isRenderingLevel = false;
	public static boolean usingTessellation = false;
	public static boolean renderWithExtendedVertexFormat = true;
	public static boolean bypass;
	public static boolean temporarilyIgnorePass;
	public static boolean safeToMultiply;
	public static boolean isRenderingBEs;
	public static boolean isRenderingEntities;
	public static boolean isRenderingPlayerEntity;
	public static boolean isRenderingItems;
	public static boolean isPreparingEntityModels;
	public static boolean isPreparingItemModels;
	public static boolean isRenderingHand;

	public static boolean isImmediateVertexExtensionDebugDisabled(VertexFormat format) {
		if (format == null) {
			return false;
		}

		if (format.equals(IrisVertexFormats.TERRAIN)) {
			return DEBUG_DISABLE_IMMEDIATE_TERRAIN_EXTENSION;
		}

		if (format.equals(DefaultVertexFormat.ENTITY) || format.equals(IrisVertexFormats.ENTITY)) {
			if (isRenderingHand || isRenderingItems || isRenderingEntities || isRenderingBEs || HandRenderer.INSTANCE.isActive() || isHandPhaseActive()) {
				return false;
			}

			return DEBUG_DISABLE_IMMEDIATE_ENTITY_EXTENSION;
		}

		if (format.equals(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR) || format.equals(IrisVertexFormats.GLYPH)) {
			return DEBUG_DISABLE_IMMEDIATE_GLYPH_EXTENSION;
		}

		return false;
	}

	private static boolean isHandPhaseActive() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
		if (pipeline == null) {
			return false;
		}

		WorldRenderingPhase phase = pipeline.getPhase();
		return phase == WorldRenderingPhase.HAND_SOLID || phase == WorldRenderingPhase.HAND_TRANSLUCENT;
	}
}
