package net.irisshaders.iris.layer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;

public class OuterWrappedRenderType extends RenderType {
	private static final RenderSetup FAKE_SETUP = RenderSetup.builder(RenderPipelines.GUI_TEXTURED).createRenderSetup();
	private static final Map<PreparedRenderType, RenderingWrapper> PREPARED_WRAPPERS = Collections.synchronizedMap(new WeakHashMap<>());
	private static final ThreadLocal<Map<PreparedRenderType, Integer>> ACTIVE_WRAPPER_DEPTH = ThreadLocal.withInitial(IdentityHashMap::new);
	private final RenderingWrapper extra;
	private final RenderType wrapped;

	public OuterWrappedRenderType(String name, RenderType wrapped, RenderingWrapper extra) {
		super(name, FAKE_SETUP);

		this.extra = extra;
		this.wrapped = wrapped;
	}

	public static OuterWrappedRenderType wrapExactlyOnce(String name, RenderType wrapped, RenderingWrapper extra) {
		while (wrapped instanceof OuterWrappedRenderType) {
			wrapped = ((OuterWrappedRenderType) wrapped).unwrap();
		}

		return new OuterWrappedRenderType(name, wrapped, extra);
	}

	private RenderType unwrap() {
		return wrapped;
	}

	@Override
	public boolean hasBlending() {
		return wrapped.hasBlending();
	}

	@Override
	public Optional<RenderType> outline() {
		return this.wrapped.outline();
	}

	@Override
	public boolean isOutline() {
		return this.wrapped.isOutline();
	}

	@Override
	public PreparedRenderType prepare() {
		PreparedRenderType prepared = wrapped.prepare();
		PreparedRenderType wrappedPrepared = new PreparedRenderType(prepared.pipeline(), prepared.outputTarget(), prepared.dynamicTransforms(), prepared.scissorState(), prepared.textures());
		PREPARED_WRAPPERS.put(wrappedPrepared, extra);
		return wrappedPrepared;
	}

	public static void beginDraw(PreparedRenderType preparedRenderType) {
		RenderingWrapper wrapper = PREPARED_WRAPPERS.get(preparedRenderType);
		if (wrapper != null) {
			Map<PreparedRenderType, Integer> activeWrapperDepth = ACTIVE_WRAPPER_DEPTH.get();
			int depth = activeWrapperDepth.getOrDefault(preparedRenderType, 0);
			activeWrapperDepth.put(preparedRenderType, depth + 1);

			if (depth == 0) {
				wrapper.setup();
			}
		}
	}

	public static void endDraw(PreparedRenderType preparedRenderType) {
		RenderingWrapper wrapper = PREPARED_WRAPPERS.get(preparedRenderType);
		if (wrapper != null) {
			Map<PreparedRenderType, Integer> activeWrapperDepth = ACTIVE_WRAPPER_DEPTH.get();
			Integer depth = activeWrapperDepth.get(preparedRenderType);

			if (depth == null) {
				return;
			}

			if (depth <= 1) {
				activeWrapperDepth.remove(preparedRenderType);
				wrapper.clear();
			} else {
				activeWrapperDepth.put(preparedRenderType, depth - 1);
			}
		}
	}

	@Override
	public RenderPipeline pipeline() {
		return wrapped.pipeline();
	}

	@Override
	public boolean sortOnUpload() {
		return wrapped.sortOnUpload();
	}

	@Override
	public RenderPipeline iris$getPipeline() {
		return wrapped.iris$getPipeline();
	}

	@Override
	public RenderTarget iris$getRenderTarget() {
		return wrapped.iris$getRenderTarget();
	}

	@Override
	public boolean canConsolidateConsecutiveGeometry() {
		return wrapped.canConsolidateConsecutiveGeometry();
	}

	@Override
	public boolean affectsCrumbling() {
		return wrapped.affectsCrumbling();
	}

	@Override
	public VertexFormat format() {
		return wrapped.format();
	}

	@Override
	public PrimitiveTopology primitiveTopology() {
		return wrapped.primitiveTopology();
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if (object == null) {
			return false;
		}

		if (object.getClass() != this.getClass()) {
			return false;
		}

		OuterWrappedRenderType other = (OuterWrappedRenderType) object;

		return Objects.equals(this.wrapped, other.wrapped) && Objects.equals(this.extra, other.extra);
	}



	@Override
	public int hashCode() {
		// Add one so that we don't have the exact same hash as the wrapped object.
		// This means that we won't have a guaranteed collision if we're inserted to a map alongside the unwrapped object.
		return this.wrapped.hashCode() + 1;
	}

	@Override
	public String toString() {
		return "iris_wrapped:" + this.wrapped.toString();
	}
}
