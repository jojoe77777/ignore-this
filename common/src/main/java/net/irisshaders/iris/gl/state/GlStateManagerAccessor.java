package net.irisshaders.iris.gl.state;

import com.mojang.blaze3d.opengl.GlStateManager;

import java.lang.reflect.Field;

public final class GlStateManagerAccessor {
	private static final Field BLEND = resolveField("BLEND");
	private static final Field COLOR_MASK = resolveField("COLOR_MASK");
	private static final Field DEPTH = resolveField("DEPTH");
	private static final Field ACTIVE_TEXTURE = resolveField("activeTexture");
	private static final Field TEXTURES = resolveField("TEXTURES");

	private GlStateManagerAccessor() {
	}

	public static GlStateManager.BlendState[] getBLEND() {
		return getObject(BLEND, GlStateManager.BlendState[].class);
	}

	public static int[] getCOLOR_MASK() {
		return getObject(COLOR_MASK, int[].class);
	}

	public static GlStateManager.DepthState getDEPTH() {
		return getObject(DEPTH, GlStateManager.DepthState.class);
	}

	public static int getActiveTexture() {
		try {
			return ACTIVE_TEXTURE.getInt(null);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to read GlStateManager.activeTexture", e);
		}
	}

	public static GlStateManager.TextureState[] getTEXTURES() {
		return getObject(TEXTURES, GlStateManager.TextureState[].class);
	}

	private static Field resolveField(String name) {
		try {
			Field field = GlStateManager.class.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to access GlStateManager." + name, e);
		}
	}

	private static <T> T getObject(Field field, Class<T> type) {
		try {
			return type.cast(field.get(null));
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to read GlStateManager field " + field.getName(), e);
		}
	}
}