package net.caffeinemc.mods.sodium.mixin.core.render.world;

import net.caffeinemc.mods.sodium.client.config.structure.Config;
import net.caffeinemc.mods.sodium.client.render.SodiumDeferredReload;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The Sodium video-settings GUI calls Config.onRendererReload from the mouse-click handler,
 * which calls SodiumWorldRenderer.reload() outside any active GL command context. Sodium's
 * GLRenderDevice.checkDeviceActive then throws "Tried to access device from unmanaged context"
 * and crashes the game.
 *
 * Route that call through the same deferred path as setupTerrain's reload trigger, so the
 * reload runs once at the start of the next world-render frame with a managed GL context.
 */
@Mixin(Config.class)
public abstract class SodiumConfigDeferReloadMixin {
    @Redirect(method = "onRendererReload", at = @At(value = "INVOKE",
        target = "Lnet/caffeinemc/mods/sodium/client/render/SodiumWorldRenderer;reload()V"), remap = false)
    private static void sodium$deferReloadFromConfig(SodiumWorldRenderer renderer) {
        SodiumDeferredReload.schedule(renderer);
    }
}
