package net.caffeinemc.mods.sodium.client.render.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;

public interface VertexConsumerIntrinsicsExtension {
    boolean sodium$canAccept(VertexFormat format);
}