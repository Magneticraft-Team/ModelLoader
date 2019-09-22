package com.cout970.modelloader.mcx

import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.texture.ISprite
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import java.util.function.Function

class UnbakedMcxModel(
    var useAmbientOcclusion: Boolean,
    var use3dInGui: Boolean,
    val particleTexture: ResourceLocation,
    val parts: List<Part>,
    val quads: Mesh
) : IUnbakedModel {

    override fun bake(bakery: ModelBakery, spriteGetter: Function<ResourceLocation, TextureAtlasSprite>, sprite: ISprite, format: VertexFormat): IBakedModel? {
        return McxBaker(format, spriteGetter).bake(this)
    }

    override fun getTextures(modelGetter: Function<ResourceLocation, IUnbakedModel>, missingTextureErrors: MutableSet<String>): MutableCollection<ResourceLocation> {
        return (parts.map { it.texture } + particleTexture).distinct().toMutableList()
    }

    override fun getDependencies(): MutableCollection<ResourceLocation> = mutableListOf()

    class Part(val name: String, val from: Int, val to: Int, val side: Direction?, val texture: ResourceLocation)
}

