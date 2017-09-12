package com.cout970.modelloader.proxy

import com.cout970.modelloader.ModelLoaderMod

/**
 * Created by cout970 on 2017/09/12.
 */
class Server : IProxy {

    override fun init() {
        ModelLoaderMod.logger.info("Ignoring server side proxy...")
    }
}