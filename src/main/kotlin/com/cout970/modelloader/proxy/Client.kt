package com.cout970.modelloader.proxy

import com.cout970.modelloader.ModelLoaderMod
import com.cout970.modelloader.internal.ModelManager
import net.minecraftforge.common.MinecraftForge

/**
 * Created by cout970 on 2017/09/12.
 */
class Client : IProxy {

    override fun init() {
        ModelLoaderMod.logger.info("${ModelLoaderMod.MOD_ID} starting client init")
        MinecraftForge.EVENT_BUS.register(ModelManager)
    }
}