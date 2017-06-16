package com.cout970.modelloader

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger

@Mod(modid = ModelLoaderMod.MOD_ID, name = ModelLoaderMod.MOD_NAME, version = ModelLoaderMod.VERSION,
        modLanguageAdapter = "com.cout970.modelloader.KotlinAdapter", modLanguage = "kotlin",
        clientSideOnly = true)
object ModelLoaderMod {

    internal const val MOD_ID = "modelloader"
    internal const val MOD_NAME = "ModelLoader"
    internal const val VERSION = "@VERSION@"
    lateinit var logger: Logger

    @EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        this.logger = event.modLog
        logger.info("$MOD_ID preinit start")
        MinecraftForge.EVENT_BUS.register(ModelManager)
        logger.info("$MOD_ID preinit end")
    }
}