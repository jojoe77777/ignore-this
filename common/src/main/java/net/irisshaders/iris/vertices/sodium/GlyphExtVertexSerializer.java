package net.irisshaders.iris.vertices.sodium;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.MemoryAccess;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class GlyphExtVertexSerializer implements VertexSerializer {
	private static final int OFFSET_POSITION = 0;

	private static final int OFFSET_MID_TEXTURE = IrisVertexFormats.getOffset(IrisVertexFormats.GLYPH, IrisVertexFormats.MID_TEXTURE_ELEMENT);
	private static final int OFFSET_COLOR = DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR.getElement(DefaultVertexFormat.COLOR_SEMANTIC_NAME).offset();
	private static final int OFFSET_TEXTURE = DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR.getElement(DefaultVertexFormat.UV0_SEMANTIC_NAME).offset();
	private static final int OFFSET_LIGHT = DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR.getElement(DefaultVertexFormat.UV2_SEMANTIC_NAME).offset();
	private static final int OFFSET_NORMAL = IrisVertexFormats.getOffset(IrisVertexFormats.GLYPH, DefaultVertexFormat.NORMAL_SEMANTIC_NAME);
	private static final int OFFSET_TANGENT = IrisVertexFormats.getOffset(IrisVertexFormats.GLYPH, IrisVertexFormats.TANGENT_ELEMENT);
	private static final QuadViewEntity quad = new QuadViewEntity();
	private static final Vector3f saveNormal = new Vector3f();
	private static final int STRIDE = IrisVertexFormats.GLYPH.getVertexSize();
	private static final int SOURCE_STRIDE = DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR.getVertexSize();
	private static final int DEFAULT_NORMAL = NormI8.pack(0.0f, 0.0f, 1.0f, 0.0f);

	private static void endQuad(float uSum, float vSum, long dst) {
		uSum *= 0.25f;
		vSum *= 0.25f;

		quad.setup(dst, IrisVertexFormats.GLYPH.getVertexSize());

		float normalX, normalY, normalZ;

		NormalHelper.computeFaceNormal(saveNormal, quad);
		normalX = saveNormal.x;
		normalY = saveNormal.y;
		normalZ = saveNormal.z;
		int normal = NormI8.pack(saveNormal);

		int tangent = NormalHelper.computeTangent(normalX, normalY, normalZ, quad);

		for (long vertex = 0; vertex < 4; vertex++) {
			MemoryAccess.setFloat(dst + OFFSET_MID_TEXTURE - STRIDE * vertex, uSum);
			MemoryAccess.setFloat(dst + (OFFSET_MID_TEXTURE + 4) - STRIDE * vertex, vSum);
			MemoryAccess.setInt(dst + OFFSET_NORMAL - STRIDE * vertex, normal);
			MemoryAccess.setInt(dst + OFFSET_TANGENT - STRIDE * vertex, tangent);
		}
	}

	@Override
	public void serialize(long src, long dst, int vertexCount) {
		final short entity = (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
		final short blockEntity = (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
		final short item = (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem();

		int vertexIndex = 0;

		while (vertexIndex < vertexCount) {
			int batchSize = Math.min(4, vertexCount - vertexIndex);
			float uSum = 0.0f;
			float vSum = 0.0f;
			long batchStartDst = dst;
			long lastVertexDst = dst;

			for (int batchVertex = 0; batchVertex < batchSize; batchVertex++) {
				float u = MemoryAccess.getFloat(src + OFFSET_TEXTURE);
				float v = MemoryAccess.getFloat(src + OFFSET_TEXTURE + 4);

				uSum += u;
				vSum += v;

				MemoryIntrinsics.copyMemory(src, dst, 28);

				MemoryAccess.setShort(dst + 32, entity);
				MemoryAccess.setShort(dst + 34, blockEntity);
				MemoryAccess.setShort(dst + 36, item);
				MemoryAccess.setFloat(dst + OFFSET_MID_TEXTURE, u);
				MemoryAccess.setFloat(dst + OFFSET_MID_TEXTURE + 4, v);
				MemoryAccess.setInt(dst + OFFSET_NORMAL, DEFAULT_NORMAL);
				MemoryAccess.setInt(dst + OFFSET_TANGENT, 0);

				lastVertexDst = dst;
				src += SOURCE_STRIDE;
				dst += STRIDE;
			}

			if (batchSize == 4) {
				endQuad(uSum, vSum, lastVertexDst);
			} else {
				float midU = uSum / batchSize;
				float midV = vSum / batchSize;
				for (int batchVertex = 0; batchVertex < batchSize; batchVertex++) {
					long vertexDst = batchStartDst + (long) batchVertex * STRIDE;
					MemoryAccess.setFloat(vertexDst + OFFSET_MID_TEXTURE, midU);
					MemoryAccess.setFloat(vertexDst + OFFSET_MID_TEXTURE + 4, midV);
				}
			}

			vertexIndex += batchSize;
		}
	}
}
