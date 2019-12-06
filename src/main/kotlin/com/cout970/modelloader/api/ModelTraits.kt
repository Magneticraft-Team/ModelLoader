package com.cout970.modelloader.api

import com.cout970.modelloader.animation.AnimatedModel
import com.cout970.modelloader.mutable.MutableModel
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.ResourceLocation
import java.util.function.Function

interface FilterableModel {
    fun filterParts(filter: (String) -> Boolean): IUnbakedModel
}

interface AnimatableModel {
    fun getAnimations(): Map<String, AnimatedModel>
}

interface MutableModelConversion {
    fun toMutable(sprites: Function<ResourceLocation, TextureAtlasSprite>, format: VertexFormat): MutableModel
}