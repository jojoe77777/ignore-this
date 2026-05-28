package net.irisshaders.iris.mixin.vertices;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.BufferBuilderPolygonView;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.MemoryAccess;
import net.irisshaders.iris.vertices.MojangBufferAccessor;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

/**
 * Dynamically and transparently extends the vanilla vertex formats with additional data
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements VertexConsumer, BlockSensitiveBufferBuilder {
	@Unique
	private final BufferBuilderPolygonView polygon = new BufferBuilderPolygonView();
	@Unique
	private final Vector3f normal = new Vector3f();
	@Unique
	private final long[] vertexOffsets = new long[4];
	@Shadow
	private int elementsToFill;
	@Unique
	private boolean skipEndVertexOnce;
	@Shadow
	@Final
	private PrimitiveTopology primitiveTopology;
	@Shadow
	@Final
	private VertexFormat format;
	@Shadow
	@Final
	private boolean blockFormat;
	@Shadow
	private long vertexPointer;
	@Shadow
	private int vertices;
	@Unique
	private boolean extending;
	@Unique
	private boolean injectNormalAndUV1;
	@Unique
	private int iris$vertexCount;
	@Unique
	private int currentBlock = -1;
	@Unique
	private byte currentRenderType = -1;
	@Unique
	private int currentLocalPosX;
	@Unique
	private int currentLocalPosY;
	@Unique
	private int currentLocalPosZ;
	@Shadow
	@Final
	private ByteBufferBuilder buffer;

	@Shadow
	public abstract VertexConsumer setNormal(float f, float g, float h);

	@ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
	private static VertexFormat iris$extendFormat(VertexFormat format) {
		if (ImmediateState.isImmediateVertexExtensionDebugDisabled(format) || ImmediateState.skipExtension.get() || !ImmediateState.isRenderingLevel || !Iris.isPackInUseQuick()) {
			return format;
		}

		if (format.equals(IrisVertexFormats.TERRAIN)) {
			return IrisVertexFormats.TERRAIN;
		} else if (format.equals(DefaultVertexFormat.ENTITY) || format.equals(IrisVertexFormats.ENTITY)) {
			return IrisVertexFormats.ENTITY;
		} else if (format.equals(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR) || format.equals(IrisVertexFormats.GLYPH)) {
			return IrisVertexFormats.GLYPH;
		}

		return format;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$captureExtensionState(ByteBufferBuilder bb, PrimitiveTopology topo, VertexFormat fmt, CallbackInfo ci) {
		injectNormalAndUV1 = false;
		extending = false;
		if (ImmediateState.isImmediateVertexExtensionDebugDisabled(this.format)) {
			return;
		}

		if (this.format == IrisVertexFormats.TERRAIN || this.format == IrisVertexFormats.ENTITY) {
			extending = true;
		} else if (this.format == IrisVertexFormats.GLYPH) {
			extending = true;
			injectNormalAndUV1 = true;
		}
	}

	@Redirect(method = "addVertex(FFFIFFIIFFF)V", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;blockFormat:Z"))
	private boolean fastFormat(BufferBuilder instance) {
		return this.blockFormat && !extending;
	}

	@Inject(method = "addVertex(FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;", at = @At("RETURN"))
	private void injectMidBlock(float x, float y, float z, CallbackInfoReturnable<VertexConsumer> cir) {
		if (!extending) {
			return;
		}

		long midBlockOffset = iris$getCustomElementPointer(IrisVertexFormats.MID_BLOCK_ELEMENT);
		if (midBlockOffset != -1L) {
			MemoryAccess.setInt(midBlockOffset, ExtendedDataHelper.computeMidBlock(x, y, z, currentLocalPosX, currentLocalPosY, currentLocalPosZ));
			byte currentBlockEmission = -1;
			MemoryAccess.setByte(midBlockOffset + 3, currentBlockEmission);
		}

		long offset = iris$getCustomElementPointer(IrisVertexFormats.ENTITY_ELEMENT);
		if (offset != -1L) {
			// ENTITY_ELEMENT
			MemoryAccess.setShort(offset, (short) currentBlock);
			MemoryAccess.setShort(offset + 2, currentRenderType);
		} else {
			offset = iris$getCustomElementPointer(IrisVertexFormats.ENTITY_ID_ELEMENT);
		}

		if (offset != -1L && format.contains(IrisVertexFormats.ENTITY_ID_ELEMENT)) {
			// ENTITY_ID_ELEMENT
			MemoryAccess.setShort(offset, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
			MemoryAccess.setShort(offset + 2, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
			MemoryAccess.setShort(offset + 4, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
		}
	}

	@Dynamic("Used to skip endLastVertex if the last push was made by Sodium")
	@Inject(method = "push", at = @At("TAIL"), remap = false, require = 0)
	private void iris$skipSodiumChange(CallbackInfo ci) {
		skipEndVertexOnce = true;
	}

	@Inject(method = "endLastVertex", at = @At("HEAD"))
	private void iris$beforeNext(CallbackInfo ci) {
		if (this.vertices == 0 || !extending) {
			return;
		}

		if (injectNormalAndUV1 && iris$hasPendingNormal()) {
			this.setNormal(0, 1, 0);
		}

		if (skipEndVertexOnce) {
			skipEndVertexOnce = false;
			return;
		}

		if (primitiveTopology != PrimitiveTopology.QUADS && primitiveTopology != PrimitiveTopology.TRIANGLES) {
			return;
		}

		vertexOffsets[iris$vertexCount] = vertexPointer - ((MojangBufferAccessor) buffer).getPointer();

		iris$vertexCount++;

		if (primitiveTopology == PrimitiveTopology.QUADS && iris$vertexCount == 4 || primitiveTopology == PrimitiveTopology.TRIANGLES && iris$vertexCount == 3) {
			fillExtendedData(iris$vertexCount);
		}
	}

	@Override
	public void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {
		this.currentBlock = block;
		this.currentRenderType = renderType;
		this.currentLocalPosX = localPosX;
		this.currentLocalPosY = localPosY;
		this.currentLocalPosZ = localPosZ;
	}

	@Override
	public void endBlock() {
		this.currentBlock = -1;
		this.currentRenderType = -1;
		this.currentLocalPosX = 0;
		this.currentLocalPosY = 0;
		this.currentLocalPosZ = 0;
	}

	@Unique
	private void fillExtendedData(int vertexAmount) {
		iris$vertexCount = 0;

		int stride = format.getVertexSize();

		polygon.setup(((MojangBufferAccessor) buffer).getPointer(), vertexOffsets, stride, vertexAmount);

		float midU = 0;
		float midV = 0;

		for (int vertex = 0; vertex < vertexAmount; vertex++) {
			midU += polygon.u(vertex);
			midV += polygon.v(vertex);
		}

		midU /= vertexAmount;
		midV /= vertexAmount;

		int midTexOffset = IrisVertexFormats.getOffset(format, IrisVertexFormats.MID_TEXTURE_ELEMENT);
		int normalOffset = IrisVertexFormats.getOffset(format, DefaultVertexFormat.NORMAL_SEMANTIC_NAME);
		int tangentOffset = IrisVertexFormats.getOffset(format, IrisVertexFormats.TANGENT_ELEMENT);
		if (vertexAmount == 3) {
			// NormalHelper.computeFaceNormalTri(normal, polygon);	// Removed to enable smooth shaded triangles. Mods rendering triangles with bad normals need to recalculate their normals manually or otherwise shading might be inconsistent.

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				long newPointer = ((MojangBufferAccessor) buffer).getPointer() + vertexOffsets[vertex];
				int vertexNormal = MemoryAccess.getInt(newPointer + normalOffset); // retrieve per-vertex normal

				int tangent = NormalHelper.computeTangentSmooth(NormI8.unpackX(vertexNormal), NormI8.unpackY(vertexNormal), NormI8.unpackZ(vertexNormal), polygon);

				MemoryAccess.setFloat(newPointer + midTexOffset, midU);
				MemoryAccess.setFloat(newPointer + midTexOffset + 4, midV);
				MemoryAccess.setInt(newPointer + tangentOffset, tangent);
			}
		} else {
			// TODO: Temporary fix for EMI item batching
			boolean recalculateNormal = ImmediateState.isRenderingLevel;
			NormalHelper.computeFaceNormal(normal, polygon);
			int packedNormal = 0;
			if (recalculateNormal) {
				packedNormal = NormI8.pack(normal.x, normal.y, normal.z, 0.0f);
			}
			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, polygon);

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				long newPointer = ((MojangBufferAccessor) buffer).getPointer() + vertexOffsets[vertex];

				MemoryAccess.setFloat(newPointer + midTexOffset, midU);
				MemoryAccess.setFloat(newPointer + midTexOffset + 4, midV);
				if (recalculateNormal) {
					MemoryAccess.setInt(newPointer + normalOffset, packedNormal);
				}
				MemoryAccess.setInt(newPointer + tangentOffset, tangent);
			}
		}

		Arrays.fill(vertexOffsets, 0);
	}

	@Unique
	private long iris$getCustomElementPointer(String elementName) {
		VertexFormatElement element = format.getElement(elementName);
		if (element == null) {
			return -1L;
		}
		return vertexPointer + element.offset();
	}

	@Unique
	private boolean iris$hasPendingNormal() {
		return (this.elementsToFill & (1 << 5)) != 0;
	}
}
