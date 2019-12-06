package com.cout970.modelloader

import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.*
import net.minecraft.client.renderer.texture.ISprite
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.resources.IResourceManager
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ICustomModelLoader
import java.util.*
import java.util.function.Function

/**
 * Allows minecraft to load glTF and mcx model from blockstate json files
 * It also allow to register empty models with "modelloader:empty"
 */
object MLCustomModelLoader : ICustomModelLoader {
    private lateinit var resourceManager: IResourceManager
    private val validDomains = mutableSetOf<String>()

    /**
     * Registers a modId, so this class is allowed to load models from that mod
     */
    @JvmStatic
    fun registerDomain(domain: String) {
        validDomains += domain
    }

    override fun loadModel(modelLocation: ResourceLocation): IUnbakedModel {
        if (modelLocation.namespace == "modelloader" && (modelLocation.path == "empty" || modelLocation.path == "models/empty")) {
            return EmptyUnbakedModel
        }
        return ModelFormatRegistry.loadUnbakedModel(resourceManager, modelLocation)
    }

    override fun accepts(modelLocation: ResourceLocation): Boolean {
        // Allow empty models with keys modelloader:empty or modelloader:models/empty
        if (modelLocation.namespace == "modelloader" &&
            (modelLocation.path == "empty" || modelLocation.path == "models/empty")) {
            return true
        }

        val extension = modelLocation.path.substringAfterLast('.')
        return modelLocation.namespace in validDomains && ModelFormatRegistry.supportsExtension(extension)
    }

    override fun onResourceManagerReload(resourceManager: IResourceManager) {
        this.resourceManager = resourceManager
    }
}

object EmptyUnbakedModel : IUnbakedModel {
    override fun bake(bakery: ModelBakery, spriteGetter: Function<ResourceLocation, TextureAtlasSprite>,
                      sprite: ISprite, format: VertexFormat): IBakedModel? {
        return EmptyBakedModel
    }

    override fun getTextures(modelGetter: Function<ResourceLocation, IUnbakedModel>,
                             missingTextureErrors: MutableSet<String>): MutableCollection<ResourceLocation> {
        return mutableListOf()
    }

    override fun getDependencies(): MutableCollection<ResourceLocation> = mutableListOf()
}

object EmptyBakedModel : IBakedModel {
    override fun getQuads(state: BlockState?, side: Direction?, rand: Random): MutableList<BakedQuad> {
        return mutableListOf()
    }

    override fun isBuiltInRenderer(): Boolean = false

    override fun isAmbientOcclusion(): Boolean = true

    override fun isGui3d(): Boolean = true

    override fun getOverrides(): ItemOverrideList = ItemOverrideList.EMPTY

    override fun getParticleTexture(): TextureAtlasSprite {
        val rl = ResourceLocation("modelloader", "empty")
        return Minecraft.getInstance().textureMap.getSprite(rl)
    }
}