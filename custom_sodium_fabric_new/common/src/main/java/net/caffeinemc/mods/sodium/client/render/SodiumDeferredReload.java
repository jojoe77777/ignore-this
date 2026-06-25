package net.caffeinemc.mods.sodium.client.render;

import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Latches SodiumWorldRenderer.reload() calls until the next LevelRenderer.render
 * invocation so they run once at the start of a fresh frame with a managed GL
 * command context. Both setupTerrain's mid-frame reload trigger and
 * Config.onRendererReload's GUI-thread reload call route through here.
 */
public final class SodiumDeferredReload {
    private static final AtomicReference<SodiumWorldRenderer> PENDING_RENDERER = new AtomicReference<>();

    private SodiumDeferredReload() {}

    public static void schedule(SodiumWorldRenderer self) {
        PENDING_RENDERER.set(self);
    }

    public static void executePending() {
        SodiumWorldRenderer renderer = PENDING_RENDERER.getAndSet(null);

        if (renderer == null) {
            return;
        }

        RenderDevice.enterManagedCode();
        try {
            renderer.reload();
        } finally {
            RenderDevice.exitManagedCode();
        }
    }
}
