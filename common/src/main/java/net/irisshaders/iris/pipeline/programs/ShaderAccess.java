package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;

public class ShaderAccess {
	public static final VertexFormat IE_FORMAT = VertexFormat.builder(0)
		.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, standardFormat(DefaultVertexFormat.ENTITY, DefaultVertexFormat.POSITION_SEMANTIC_NAME))
		.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, standardFormat(DefaultVertexFormat.ENTITY, DefaultVertexFormat.COLOR_SEMANTIC_NAME))
		.addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, standardFormat(DefaultVertexFormat.ENTITY, DefaultVertexFormat.UV0_SEMANTIC_NAME))
		.addAttribute(DefaultVertexFormat.NORMAL_SEMANTIC_NAME, standardFormat(DefaultVertexFormat.ENTITY, DefaultVertexFormat.NORMAL_SEMANTIC_NAME))
		.build();

	private static GpuFormat standardFormat(VertexFormat format, String elementName) {
		return format.getElement(elementName).format();
	}

	// TODO SPS 1.21.2
}
