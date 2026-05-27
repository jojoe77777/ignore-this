package net.irisshaders.iris.vertices;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class IrisVertexFormats {
	public static final String ENTITY_ELEMENT = "mc_Entity";
	public static final String ENTITY_ID_ELEMENT = "iris_Entity";
	public static final String MID_TEXTURE_ELEMENT = "mc_midTexCoord";
	public static final String TANGENT_ELEMENT = "at_tangent";
	public static final String MID_BLOCK_ELEMENT = "at_midBlock";

	public static final VertexFormat TERRAIN;
	public static final VertexFormat ENTITY;
	public static final VertexFormat GLYPH;
	public static final VertexFormat CLOUDS;

	static {
		TERRAIN = extend(DefaultVertexFormat.BLOCK,
			new ExtraAttribute(DefaultVertexFormat.NORMAL_SEMANTIC_NAME,
				getRequiredElement(DefaultVertexFormat.ENTITY, DefaultVertexFormat.NORMAL_SEMANTIC_NAME).format()),
			new ExtraAttribute(ENTITY_ELEMENT, GpuFormat.RG16_SINT),
			new ExtraAttribute(MID_TEXTURE_ELEMENT, GpuFormat.RG32_FLOAT),
			new ExtraAttribute(TANGENT_ELEMENT, GpuFormat.RGBA8_SNORM),
			new ExtraAttribute(MID_BLOCK_ELEMENT, GpuFormat.RGBA8_SINT));

		ENTITY = extend(DefaultVertexFormat.ENTITY,
			new ExtraAttribute(ENTITY_ID_ELEMENT, GpuFormat.RGBA16_UINT),
			new ExtraAttribute(MID_TEXTURE_ELEMENT, GpuFormat.RG32_FLOAT),
			new ExtraAttribute(TANGENT_ELEMENT, GpuFormat.RGBA8_SNORM));

		GLYPH = VertexFormat.builder(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR.getStepRate())
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, getRequiredElement(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, DefaultVertexFormat.POSITION_SEMANTIC_NAME).format())
			.addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, getRequiredElement(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, DefaultVertexFormat.UV0_SEMANTIC_NAME).format())
			.addAttribute(DefaultVertexFormat.UV2_SEMANTIC_NAME, getRequiredElement(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, DefaultVertexFormat.UV2_SEMANTIC_NAME).format())
			.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, getRequiredElement(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, DefaultVertexFormat.COLOR_SEMANTIC_NAME).format())
			.addAttribute(DefaultVertexFormat.NORMAL_SEMANTIC_NAME, getRequiredElement(DefaultVertexFormat.ENTITY, DefaultVertexFormat.NORMAL_SEMANTIC_NAME).format())
			.addAttribute(ENTITY_ID_ELEMENT, GpuFormat.RGBA16_UINT)
			.addAttribute(MID_TEXTURE_ELEMENT, GpuFormat.RG32_FLOAT)
			.addAttribute(TANGENT_ELEMENT, GpuFormat.RGBA8_SNORM)
			.build();

		CLOUDS = DefaultVertexFormat.POSITION_COLOR_NORMAL;
	}

	public static int getOffset(VertexFormat format, String elementName) {
		VertexFormatElement element = format.getElement(elementName);
		if (element == null) {
			throw new IllegalArgumentException("Missing element '" + elementName + "' in " + format);
		}
		return element.offset();
	}

	private static VertexFormat extend(VertexFormat base, ExtraAttribute... extras) {
		VertexFormat.Builder builder = VertexFormat.builder(base.getStepRate());
		for (VertexFormatElement element : base.getElements()) {
			builder.addAttribute(element.name(), element.format());
		}
		for (ExtraAttribute extra : extras) {
			builder.addAttribute(extra.name(), extra.format());
		}
		return builder.build();
	}

	private static VertexFormatElement getRequiredElement(VertexFormat format, String name) {
		VertexFormatElement element = format.getElement(name);
		if (element == null) {
			throw new IllegalStateException("Missing required element '" + name + "' in " + format);
		}
		return element;
	}

	private record ExtraAttribute(String name, GpuFormat format) {
	}
}
