package com.cout970.modelloader.api

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.io.Closeable

/**
 * Created by cout970 on 2017/06/17.
 */

interface IRenderCache : Closeable {
    fun render()
    override fun close()
}

class RenderCacheDisplayList(val renderFunc: () -> Unit) : IRenderCache {
    private var id: Int = -1

    override fun render() {
        if (id == -1) {
            id = GlStateManager.glGenLists(1)
            GlStateManager.glNewList(id, GL11.GL_COMPILE)
            renderFunc()
            GlStateManager.glEndList()
        }
        GlStateManager.callList(id)
    }

    override fun close() {
        if (id != -1) {
            GlStateManager.glDeleteLists(id, 1)
        }
        id = -1
    }
}