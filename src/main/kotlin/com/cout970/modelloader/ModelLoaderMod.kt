package com.cout970.modelloader

import com.cout970.modelloader.proxy.IProxy
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger

@Mod(modid = ModelLoaderMod.MOD_ID, name = ModelLoaderMod.MOD_NAME, version = "1.0.5",
        modLanguageAdapter = "com.cout970.modelloader.KotlinAdapter", modLanguage = "kotlin")
object ModelLoaderMod {

    internal const val MOD_ID = "modelloader"
    internal const val MOD_NAME = "ModelLoader"
    lateinit var logger: Logger

    @SidedProxy(
            clientSide = "com.cout970.modelloader.proxy.Client",
            serverSide = "com.cout970.modelloader.proxy.Server"
    )
    var proxy: IProxy? = null

    @EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        this.logger = event.modLog
        logger.info("${ModelLoaderMod.MOD_ID} preInit start")
        proxy?.init()
        logger.info("${ModelLoaderMod.MOD_ID} preInit end")
    }
}