package net.caffeinemc.mods.sodium.mixin.core.render.world;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2: block-dirty tracking moved from LevelRenderer to ClientLevel. Forward to
 * SodiumWorldRenderer so its render section manager keeps in sync with block changes.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelDirtyMixin {
    @Inject(method = "setSectionDirtyWithNeighbors", at = @At("HEAD"))
    private void sodium$setSectionDirtyWithNeighbors(int x, int y, int z, CallbackInfo ci) {
        SodiumWorldRenderer r = sodium$getRenderer();
        if (r != null) {
            r.scheduleRebuildForChunks(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1, false);
        }
    }

    @Inject(method = "setBlocksDirty", at = @At("HEAD"))
    private void sodium$setBlocksDirty(BlockPos pos, net.minecraft.world.level.block.state.BlockState oldState, net.minecraft.world.level.block.state.BlockState newState, CallbackInfo ci) {
        SodiumWorldRenderer r = sodium$getRenderer();
        if (r != null) {
            r.scheduleRebuildForBlockArea(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, false);
        }
    }

    @Inject(method = "setSectionRangeDirty", at = @At("HEAD"))
    private void sodium$setSectionRangeDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, CallbackInfo ci) {
        SodiumWorldRenderer r = sodium$getRenderer();
        if (r != null) {
            r.scheduleRebuildForChunks(minX, minY, minZ, maxX, maxY, maxZ, false);
        }
    }

    private static SodiumWorldRenderer sodium$getRenderer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.levelRenderer == null) return null;
        return ((LevelRendererExtension) mc.levelRenderer).sodium$getWorldRenderer();
    }
}
