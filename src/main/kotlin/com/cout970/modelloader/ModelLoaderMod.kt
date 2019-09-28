package com.cout970.modelloader

import com.cout970.modelloader.gltf.GltfFormatHandler
import com.cout970.modelloader.mcx.McxFormatHandler
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

lateinit var ModelLoaderMod: ModelLoaderModImpl

/**
 * Main mod class
 */
@Mod("modelloader")
class ModelLoaderModImpl {
    /**
     * Location of the default texture used for model particles,
     * its used when a model doesn't has any texture
     */
    val defaultParticleTexture = ResourceLocation("modelloader:default_particle")
    /**
     * Location of the default texture used for entire models,
     * its used when a model has a mesh without texture
     */
    val defaultModelTexture = ResourceLocation("modelloader:default_model")
    val logger: Logger = LogManager.getLogger()

    init {
        ModelLoaderMod = this
        FMLJavaModLoadingContext.get()?.apply {
            // Event before textures are loaded
            modEventBus.addListener<TextureStitchEvent.Pre> { onTextureStitchEvent(it) }
            // Event when models are baked
            modEventBus.addListener<ModelBakeEvent>(EventPriority.HIGH) { onModelBakeEvent(it) }
        }

        // Allow blockstate json files to use gltf or mcx models
        ModelLoaderRegistry.registerLoader(MLCustomModelLoader)
        // Register the default formats
        ModelFormatRegistry.registerHandler("mcx", McxFormatHandler)
        ModelFormatRegistry.registerHandler("gltf", GltfFormatHandler)

        // Register config options
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.spec)
    }

    fun onTextureStitchEvent(event: TextureStitchEvent.Pre) {
        ModelManager.loadModelFiles(Minecraft.getInstance().resourceManager)
        ModelManager.onTextureStitchEvent(event)
    }

    fun onModelBakeEvent(event: ModelBakeEvent) {
        ModelManager.onModelBakeEvent(event)
    }
}

internal object Config {
    val spec: ForgeConfigSpec
    // Allows the mod to use multiple thread to load models in parallel
    val useMultithreading: ForgeConfigSpec.BooleanValue

    init {
        val builder = ForgeConfigSpec.Builder()
        builder.push("General")

        builder.apply {
            comment("Use multithreading to load models faster")
            translation("modelloader.enable.multithreading")
            useMultithreading = define("useMultithreading", true)
        }

        builder.pop()
        spec = builder.build()
    }
}