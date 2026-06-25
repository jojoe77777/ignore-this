package net.caffeinemc.mods.sodium.mixin.core.render.world;

import net.caffeinemc.mods.sodium.client.render.SodiumDeferredReload;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Sodium's setupTerrain calls reload() synchronously mid-frame whenever the player's effective
 * render distance changes. Under iris with a heavy shader pack this rebuilds every chunk through
 * the iris-extended encoder, and the GPU driver locks up because draw calls from the prior frame
 * are still in flight against resources we're about to delete.
 *
 * Fix: defer the reload one frame so it runs cleanly between renders, and update the cached
 * render distance immediately so the check doesn't fire again until the deferred reload finishes
 * and resets it.
 */
@Mixin(SodiumWorldRenderer.class)
public abstract class SodiumWorldRendererDeferReloadMixin {
    @Shadow
    private int renderDistance;

    @Redirect(method = "setupTerrain", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/SodiumWorldRenderer;reload()V"), remap = false)
    private void sodium$deferReload(SodiumWorldRenderer self) {
        SodiumDeferredReload.schedule(self);
        // Eagerly update the cached value so the if-check stops firing in subsequent frames.
        this.renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
    }
}
