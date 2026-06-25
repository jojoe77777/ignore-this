package net.caffeinemc.mods.sodium.mixin.features.textures.animations.tracking;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.UvMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 26.3: wrap(VertexConsumer) was hoisted out of TextureAtlasSprite into a default method on the new
// UvMapping interface (TextureAtlasSprite implements it and does not override wrap). Target the interface
// default method and guard for the sprite implementor.
@Mixin(UvMapping.class)
public interface TextureAtlasSpriteMixin {
    @Inject(method = "wrap", at = @At("HEAD"))
    private void markSpriteAsActive(VertexConsumer consumer, CallbackInfoReturnable<VertexConsumer> cir) {
        if ((Object) this instanceof TextureAtlasSprite sprite) {
            SpriteUtil.INSTANCE.markSpriteActive(sprite);
        }
    }
}
