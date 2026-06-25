package net.caffeinemc.mods.sodium.mixin.core.render.world;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.FilterMode;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.SodiumDeferredReload;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.util.GameRendererStorage;
import net.caffeinemc.mods.sodium.client.util.SodiumChunkSection;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements LevelRendererExtension {
    @Unique
    private static final EnumMap<ChunkSectionLayer,Int2ObjectOpenHashMap<List<RenderPass.Draw<GpuBufferSlice[]>>>> STATIC_MAP = new EnumMap<>(ChunkSectionLayer.class);

    static {
        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            STATIC_MAP.put(layer, new Int2ObjectOpenHashMap<>());
        }
    }

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;
    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Unique
    private SodiumWorldRenderer renderer;

    @Unique
    private ChunkRenderMatrices matrices;

    @Override
    public SodiumWorldRenderer sodium$getWorldRenderer() {
        return this.renderer;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void sodium$captureMatrices(com.mojang.blaze3d.resource.GraphicsResourceAllocator alloc, net.minecraft.client.DeltaTracker tracker, boolean renderOutline, net.minecraft.client.renderer.state.level.CameraRenderState cameraState, org.joml.Matrix4fc modelViewMatrix, com.mojang.blaze3d.buffers.GpuBufferSlice fog, org.joml.Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        SodiumDeferredReload.executePending();

        if (this.renderer == null) return;
        org.joml.Matrix4fc proj = ((net.caffeinemc.mods.sodium.client.util.GameRendererStorage) net.minecraft.client.Minecraft.getInstance().gameRenderer).sodium$getProjectionMatrix();
        if (proj == null) return;
        this.matrices = new ChunkRenderMatrices(proj, modelViewMatrix);
        this.renderer.updateFogColor(fogColor);
    }

    @Inject(method = "addMainPass", at = @At("HEAD"))
    private void sodium$wireUpChunkSectionsToRender(com.mojang.blaze3d.framegraph.FrameGraphBuilder builder, net.minecraft.client.renderer.feature.FeatureRenderDispatcher.PreparedFrame frame, com.mojang.blaze3d.buffers.GpuBufferSlice fogBuf, net.minecraft.client.renderer.state.level.LevelRenderState state, net.minecraft.util.profiling.ProfilerFiller profiler, ChunkSectionsToRender sections, CallbackInfo ci) {
        if (this.renderer == null) return;
        ChunkRenderMatrices m = this.matrices;
        if (m == null) return;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        net.minecraft.client.Camera camera = mc.gameRenderer.mainCamera();
        net.minecraft.client.renderer.culling.Frustum frustum = camera != null ? camera.getCullFrustum() : null;
        if (camera != null && frustum != null) {
            RenderDevice.enterManagedCode();
            try {
                this.renderer.setupTerrain(
                    camera,
                    ((net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider) frustum).sodium$createViewport(),
                    ((net.caffeinemc.mods.sodium.client.util.FogStorage) mc.gameRenderer).sodium$getFogParameters(),
                    mc.player != null && mc.player.isSpectator(),
                    net.caffeinemc.mods.sodium.client.util.FlawlessFrames.isActive(),
                    ((net.caffeinemc.mods.sodium.mixin.core.render.world.FrustumAccessor) frustum).sodium$getMatrix());
            } finally {
                RenderDevice.exitManagedCode();
            }
        }
        net.minecraft.client.renderer.state.level.CameraRenderState cam = this.levelRenderState.cameraRenderState;
        ((net.caffeinemc.mods.sodium.client.util.SodiumChunkSection) (Object) sections).sodium$setRendering(
            this.renderer, m, cam.pos.x, cam.pos.y, cam.pos.z);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, net.minecraft.client.resources.model.ModelManager modelManager, net.minecraft.client.renderer.texture.TextureManager textureManager, net.minecraft.client.resources.model.sprite.AtlasManager atlasManager, net.minecraft.client.renderer.ShaderManager shaderManager, net.minecraft.client.renderer.GameRenderer gameRenderer, int i, int j, CallbackInfo ci) {
        this.renderer = new SodiumWorldRenderer(Minecraft.getInstance());
    }

    // 26.2: LevelRenderer.setLevel removed. Sodium's renderer is now told about the level
    // from MinecraftSetLevelMixin instead.

    // hasRenderedAllSections overwrite removed for 26.2: Sodium's renderer has no level
    // (no setLevel hook exists post-refactor) so it always reported incomplete and the
    // chunk-load wait timed out.

    // prepareChunkRenders overwrite removed for 26.2: let vanilla MC build its own ChunkSectionsToRender
    // so terrain actually renders. Sodium's path can't be wired up without porting setBlocksDirty/setSectionDirty/etc.

    // isSectionCompiledAndVisible overwrite removed for 26.2: renderer isn't being initialized
    // through the new lifecycle, so we'd always return false. Let vanilla handle it.

    /**
     * @reason Allow control of the texture filtering mode
     * @author pajic
     */
    @Redirect(method = "lambda$addMainPass$0", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/textures/FilterMode;LINEAR:Lcom/mojang/blaze3d/textures/FilterMode;", opcode = Opcodes.GETSTATIC), require = 0)
    private FilterMode setFilterMode() {
        return SodiumClientMod.options().quality.pixelFilteringMode;
    }

    @Override
    public void sodium$setMatrices(ChunkRenderMatrices matrices) {
        this.matrices = matrices;
    }

    @Override
    public ChunkRenderMatrices sodium$getMatrices() {
        return this.matrices;
    }
}
