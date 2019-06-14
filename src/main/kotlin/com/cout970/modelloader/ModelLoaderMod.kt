package com.cout970.modelloader

import com.cout970.modelloader.proxy.IProxy
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger

@Mod(
        modid = ModelLoaderMod.MOD_ID,
        name = ModelLoaderMod.MOD_NAME,
        version = "1.1.6",
        acceptedMinecraftVersions = "[1.12]",
        dependencies = "required-after:forgelin", // TODO change on release
        modLanguageAdapter = "com.cout970.modelloader.KotlinAdapter",
        modLanguage = "kotlin",
        clientSideOnly = true
)
object ModelLoaderMod {

    internal const val MOD_ID = "modelloader"
    internal const val MOD_NAME = "ModelLoader"
    lateinit var logger: Logger

    internal var useMultiThreading = false

    @SidedProxy(
            clientSide = "com.cout970.modelloader.proxy.Client",
            serverSide = "com.cout970.modelloader.proxy.Server"
    )
    internal var proxy: IProxy? = null

    @EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        this.logger = event.modLog

        val config = Configuration(event.suggestedConfigurationFile)
        config.load()
        this.useMultiThreading = config["global", "use_multi_threading", true].boolean
        config.save()

        logger.info("$MOD_ID preInit start")
        proxy?.init()
        logger.info("$MOD_ID preInit end")
    }
}
