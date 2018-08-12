package com.cout970.modelloader.internal

import net.minecraft.client.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ICustomModelLoader
import net.minecraftforge.client.model.IModel

internal object CustomModelLoader : ICustomModelLoader {

    private lateinit var resourceManager: IResourceManager
    internal val validDomains = mutableSetOf<String>()

    override fun loadModel(modelLocation: ResourceLocation): IModel {
        return ModelManager.loadModel(resourceManager, modelLocation)
    }

    override fun accepts(modelLocation: ResourceLocation): Boolean {
        return modelLocation.resourceDomain in validDomains && (modelLocation.resourcePath.endsWith(".mcx") || modelLocation.resourcePath.endsWith(".gltf"))
    }

    override fun onResourceManagerReload(resourceManager: IResourceManager) {
        this.resourceManager = resourceManager
    }
}