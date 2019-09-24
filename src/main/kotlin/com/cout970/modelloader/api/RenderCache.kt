package com.cout970.modelloader.api

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
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
     * Renders the model without binding textures
     */
    fun renderUntextured() = render()

    /**
     * Clear cache and associated resources
     */
    override fun close()
}

/**
 * RenderCache implementation that uses DisplayLists to reduce GPU access overhead
 */
class ModelCache(val renderFunc: () -> Unit) : IRenderCache {
    private var id: Int = -1

    override fun render() {
        if (id == -1) {
            id = GlStateManager.genLists(1)
            GlStateManager.newList(id, GL11.GL_COMPILE)
            renderFunc()
            GlStateManager.endList()
        }
        GlStateManager.callList(id)
    }

    override fun close() {
        if (id != -1) {
            GlStateManager.deleteLists(id, 1)
        }
        id = -1
    }
}

/**
 * RenderCache wrapper that groups all elements that can be rendered with the same texture
 */
class TextureModelCache(val texture: ResourceLocation, vararg val cache: IRenderCache) : IRenderCache {

    var enableTexture: Boolean = true

    override fun render() {
        if (enableTexture) {
            Minecraft.getInstance().textureManager.bindTexture(texture)
        }
        cache.forEach { it.render() }
    }

    override fun renderUntextured() {
        cache.forEach { it.renderUntextured() }
    }

    override fun close() {
        cache.forEach { it.close() }
    }
}

/**
 * RenderCache wrapper that groups other RenderCaches
 */
class ModelGroupCache(vararg val cache: IRenderCache) : IRenderCache {

    override fun render() {
        cache.forEach { it.render() }
    }

    override fun renderUntextured() {
        cache.forEach { it.renderUntextured() }
    }

    override fun close() {
        cache.forEach { it.close() }
    }
}