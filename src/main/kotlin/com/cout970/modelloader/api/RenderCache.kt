package com.cout970.modelloader.api

import com.mojang.blaze3d.platform.GlStateManager
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
     * Performs the render process
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

    /**
     * Clears the state of the cache without performing any GL operation (to avoid thread issues),
     * all necessary GL operations are performed in a callback passed to runner.
     *
     * A common use of this is function is `model.asyncClose(Minecraft.getInstance()::runImmediately)`
     * which will free the resources immediately if we are in the main thread,
     * or it will defer the task to be run in the main thread once the game ends the current tick
     *
     * @runner is a function that takes tasks that must run in the main thread to avoid GL threading issues
     */
    fun asyncClose(runner: (Runnable) -> Unit)
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

    override fun asyncClose(runner: (Runnable) -> Unit) {
        if (id != -1) {
            val idToFree = id
            runner.invoke(Runnable {
                GlStateManager.deleteLists(idToFree, 1)
            })
        }
        id = -1
    }
}

/**
 * RenderCache wrapper that groups all elements that can be rendered with the same texture
 */
class TextureModelCache(val texture: ResourceLocation, vararg val cache: IRenderCache) : IRenderCache {

    override fun render() {
        Utilities.bindTexture(texture)
        cache.forEach { it.render() }
    }

    override fun renderUntextured() {
        cache.forEach { it.renderUntextured() }
    }

    override fun close() {
        cache.forEach { it.close() }
    }

    override fun asyncClose(runner: (Runnable) -> Unit) {
        cache.forEach { it.asyncClose(runner) }
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

    override fun asyncClose(runner: (Runnable) -> Unit) {
        cache.forEach { it.asyncClose(runner) }
    }
}

object EmptyRenderCache : IRenderCache {
    override fun render() = Unit

    override fun close() = Unit

    override fun asyncClose(runner: (Runnable) -> Unit) = Unit
}