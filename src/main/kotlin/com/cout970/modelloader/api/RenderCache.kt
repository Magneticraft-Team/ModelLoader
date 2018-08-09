package com.cout970.modelloader.api

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.io.Closeable

/**
 * Created by cout970 on 2017/06/17.
 */

/**
 * Wraps a render process to avoid unnecessary calculations and gpu access overhead
 */
interface IRenderCache : Closeable {

    /**
     * Performs the render process, or use the cache if available
     */
    fun render()

    /**
     * Clear cache and associated resources
     */
    override fun close()
}

/**
 * RenderCache implementation that uses DisplayLists to reduce GPU access overhead
 */
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