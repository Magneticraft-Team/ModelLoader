package com.cout970.modelloader.gltf

import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.model.ModelRotation
import net.minecraft.client.renderer.texture.ISprite
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.ResourceLocation
import java.util.function.Function

class UnbakedGltfModel(val tree: GltfTree.DefinitionTree) : IUnbakedModel {
    override fun getDependencies(): MutableCollection<ResourceLocation> = mutableListOf()

    override fun bake(bakery: ModelBakery, spriteGetter: Function<ResourceLocation, TextureAtlasSprite>, sprite: ISprite, format: VertexFormat): IBakedModel? {
        return GltfBaker(format, spriteGetter, sprite as? ModelRotation ?: ModelRotation.X0_Y0).bake(this)
    }

    override fun getTextures(modelGetter: Function<ResourceLocation, IUnbakedModel>, missingTextureErrors: MutableSet<String>): MutableCollection<ResourceLocation> {
        return tree.textures.toMutableList()
    }
}