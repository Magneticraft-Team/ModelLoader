package com.cout970.modelloader.mcx

import com.cout970.modelloader.api.EmptyRenderCache
import com.cout970.modelloader.api.FilterableModel
import com.cout970.modelloader.api.MutableModelConversion
import com.cout970.modelloader.mutable.MutableModel
import com.cout970.modelloader.mutable.MutableModelNode
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
) : IUnbakedModel, FilterableModel, MutableModelConversion {

    override fun bake(bakery: ModelBakery, spriteGetter: Function<ResourceLocation, TextureAtlasSprite>, sprite: ISprite, format: VertexFormat): IBakedModel? {
        return McxBaker(format, spriteGetter).bake(this)
    }

    override fun getTextures(modelGetter: Function<ResourceLocation, IUnbakedModel>, missingTextureErrors: MutableSet<String>): MutableCollection<ResourceLocation> {
        return (parts.map { it.texture } + particleTexture).distinct().toMutableList()
    }

    override fun getDependencies(): MutableCollection<ResourceLocation> = mutableListOf()

    override fun filterParts(filter: (String) -> Boolean): UnbakedMcxModel {
        return UnbakedMcxModel(
            useAmbientOcclusion = this.useAmbientOcclusion,
            use3dInGui = this.use3dInGui,
            particleTexture = this.particleTexture,
            parts = this.parts.filter { filter(it.name) },
            quads = this.quads
        )
    }

    override fun toMutable(sprites: Function<ResourceLocation, TextureAtlasSprite>, format: VertexFormat): MutableModel {
        return McxBaker(format, sprites).bakeMutableModel(this)
    }

    class Part(val name: String, val from: Int, val to: Int, val side: Direction?, val texture: ResourceLocation)
}
