package net.caffeinemc.mods.sodium.mixin.core.render.world;

import net.caffeinemc.mods.sodium.client.render.SodiumDeferredReload;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2: LevelRenderer.allChanged is gone; the reload entry-point is now LevelExtractor.allChanged.
 * Mirror Sodium's old reload hook onto it so iris-driven pipeline reloads (e.g. when the player
 * toggles shaders on/off and the chunk vertex format / block ID map changes) destroy the old chunks
 * and rebuild them in the new format instead of leaving stale GPU buffers around.
 *
 * Defer the actual Sodium reload to the next render frame. Iris can invoke allChanged while it is
 * still preparing the current frame's pipeline state, and tearing Sodium down inline there causes
 * repeated chunk reload loops and render-state reentrancy problems.
 *
 * Only fire when iris has actually requested a reload (WorldRenderingSettings.isReloadRequired()) —
 * otherwise a render distance change in VideoSettings (which also calls allChanged) would
 * gratuitously destroy and rebuild every chunk through the heavy iris shader pipeline. Sodium
 * handles render distance changes itself via setupTerrain's internal check.
 */
@Mixin(LevelExtractor.class)
public abstract class LevelExtractorReloadMixin {
    @Unique
    private static boolean sodium$irisReloadFlagAtHead = false;

    @Inject(method = "allChanged", at = @At("HEAD"))
    private void sodium$snapshotIrisFlag(CallbackInfo ci) {
        sodium$irisReloadFlagAtHead = sodium$isIrisReloadRequired();
    }

    @Inject(method = "allChanged", at = @At("RETURN"))
    private void sodium$onReload(CallbackInfo ci) {
        if (!sodium$irisReloadFlagAtHead) {
            // Not an iris-triggered reload — sodium will handle anything it needs to itself.
            return;
        }

        sodium$irisReloadFlagAtHead = false;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.levelRenderer == null) return;
        SodiumWorldRenderer renderer = ((LevelRendererExtension) mc.levelRenderer).sodium$getWorldRenderer();
        if (renderer == null) return;

        SodiumDeferredReload.schedule(renderer);
    }

    @Unique
    private static boolean sodium$isIrisReloadRequired() {
        try {
            Class<?> cls = Class.forName("net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings");
            Object instance = cls.getField("INSTANCE").get(null);
            return (boolean) cls.getMethod("isReloadRequired").invoke(instance);
        } catch (Throwable t) {
            return false;
        }
    }
}
