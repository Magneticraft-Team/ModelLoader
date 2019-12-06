package com.cout970.modelloader.gltf

import com.cout970.modelloader.animation.AnimatedModel
import com.cout970.modelloader.api.AnimatableModel
import com.cout970.modelloader.api.FilterableModel
import com.cout970.modelloader.api.MutableModelConversion
import com.cout970.modelloader.mutable.MutableModel
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.model.ModelRotation
import net.minecraft.client.renderer.texture.ISprite
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.ResourceLocation
import java.util.function.Function

class UnbakedGltfModel(val tree: GltfTree.DefinitionTree) : IUnbakedModel, FilterableModel, AnimatableModel, MutableModelConversion {
    override fun getDependencies(): MutableCollection<ResourceLocation> = mutableListOf()

    override fun bake(bakery: ModelBakery, spriteGetter: Function<ResourceLocation, TextureAtlasSprite>, sprite: ISprite, format: VertexFormat): IBakedModel? {
        return GltfBaker(format, spriteGetter, sprite as? ModelRotation ?: ModelRotation.X0_Y0).bake(this)
    }

    override fun getTextures(modelGetter: Function<ResourceLocation, IUnbakedModel>, missingTextureErrors: MutableSet<String>): MutableCollection<ResourceLocation> {
        return tree.textures.toMutableList()
    }

    override fun filterParts(filter: (String) -> Boolean): UnbakedGltfModel {
        // Filter nodes from scenes
        val newScenes = tree.scenes.map { GltfTree.Scene(filterNodes(it.nodes, filter)) }
        // Keep all the tree except the scenes
        val newTree = tree.copy(scenes = newScenes)

        return UnbakedGltfModel(newTree)
    }

    private fun filterNodes(nodes: List<GltfTree.Node>, filter: (String) -> Boolean): List<GltfTree.Node> {
        return nodes.mapNotNull { node ->
            if (node.name == null || filter(node.name)) {
                // Create a new node with filtered children
                node.copy(children = filterNodes(node.children, filter))
            } else {
                // Discard the node
                null
            }
        }
    }

    override fun toMutable(sprites: Function<ResourceLocation, TextureAtlasSprite>, format: VertexFormat): MutableModel {
        return GltfBaker(format, sprites, ModelRotation.X0_Y0).bakeMutableModel(this)
    }

    override fun getAnimations(): Map<String, AnimatedModel> {
        var count = 0
        return tree.animations.map {
            val name = it.name ?: "Animation${count++}"
            name to GltfAnimator(tree).animate(it)
        }.toMap()
    }
}