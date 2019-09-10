package com.cout970.modelloader

import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ICustomModelLoader

object MLCustomModelLoader : ICustomModelLoader {
    private lateinit var resourceManager: IResourceManager
    private val validDomains = mutableSetOf<String>()

    fun registerDomain(domain: String){
        validDomains += domain
    }

    override fun loadModel(modelLocation: ResourceLocation): IUnbakedModel {
        return ModelFormatRegistry.loadUnbakedModel(resourceManager, modelLocation)
    }

    override fun accepts(modelLocation: ResourceLocation): Boolean {
        val extension = modelLocation.path.substringAfterLast('.')
        return modelLocation.namespace in validDomains && ModelFormatRegistry.supportsExtension(extension)
    }

    override fun onResourceManagerReload(resourceManager: IResourceManager) {
        this.resourceManager = resourceManager
    }
}