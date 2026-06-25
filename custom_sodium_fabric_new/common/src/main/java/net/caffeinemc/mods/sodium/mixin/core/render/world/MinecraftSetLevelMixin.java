package net.caffeinemc.mods.sodium.mixin.core.render.world;

import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftSetLevelMixin {
    @Shadow
    public net.minecraft.client.renderer.LevelRenderer levelRenderer;

    @Inject(method = "setLevel", at = @At("RETURN"))
    private void sodium$onSetLevel(@Nullable ClientLevel level, CallbackInfo ci) {
        var ext = (LevelRendererExtension) this.levelRenderer;
        var renderer = ext.sodium$getWorldRenderer();
        if (renderer == null) return;
        RenderDevice.enterManagedCode();
        try {
            renderer.setLevel(level);
        } finally {
            RenderDevice.exitManagedCode();
        }
    }

    @Inject(method = "clearClientLevel", at = @At("HEAD"))
    private void sodium$onClearLevel(net.minecraft.client.gui.screens.Screen screen, CallbackInfo ci) {
        var ext = (LevelRendererExtension) this.levelRenderer;
        var renderer = ext.sodium$getWorldRenderer();
        if (renderer == null) return;
        RenderDevice.enterManagedCode();
        try {
            renderer.setLevel(null);
        } finally {
            RenderDevice.exitManagedCode();
        }
    }
}
