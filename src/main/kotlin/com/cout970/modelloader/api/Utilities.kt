package com.cout970.modelloader.api

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.model.data.EmptyModelData
import org.lwjgl.opengl.GL11
import java.util.*

/**
 * Several utilities for rendering stuff
 */
object Utilities {
    @JvmField
    val BLOCK_CENTER = Vec3d(0.5, 0.5, 0.5)
    @JvmField
    val DOWN_ROTATION = Vec3d(180.0, 0.0, 0.0)
    @JvmField
    val UP_ROTATION = Vec3d(0.0, 0.0, 0.0)
    @JvmField
    val SOUTH_ROTATION = Vec3d(0.0, 90.0, 90.0)
    @JvmField
    val NORTH_ROTATION = Vec3d(0.0, -90.0, 90.0)
    @JvmField
    val EAST_ROTATION = Vec3d(0.0, 0.0, -90.0)
    @JvmField
    val WEST_ROTATION = Vec3d(0.0, 0.0, 90.0)

    /**
     * Binds a texture to be used in the next models to render
     */
    @JvmStatic
    fun bindTexture(tex: ResourceLocation) {
        Minecraft.getInstance().textureManager.bindTexture(tex)
    }

    /**
     * Gets the time since the last client tick, the value is between 0 and 1
     */
    @JvmStatic
    fun getPartialTicks(): Float {
        return Minecraft.getInstance().renderPartialTicks
    }

    /**
     * Time of the world with partial ticks,
     * it wraps so it can be used for animations and don't generate numbers too big
     *
     * Every 65535 ticks the value gets reset to 0
     */
    @JvmStatic
    fun worldTime(): Float {
        val world = Minecraft.getInstance().world
        return (world.gameTime and 0xFFFF).toInt().toFloat() + getPartialTicks()
    }

    /**
     * Runs code that modifies the OpenGL matrix and restores the original value at the end
     */
    inline fun saveMatrix(func: () -> Unit) {
        GlStateManager.pushMatrix()
        func()
        GlStateManager.popMatrix()
    }

    /**
     * Creates a IRenderCache that can be used to render a model efficiently.
     *
     * To make efficient use of the IRenderCache you need to reuse it as much as possible
     * and only create a new one if the model is different.
     *
     * If you are not going to use it anymore call IRenderCache.close() to free GPU memory
     */
    @JvmStatic
    fun cacheModel(model: IBakedModel): IRenderCache {
        return TextureModelCache(AtlasTexture.LOCATION_BLOCKS_TEXTURE, ModelCache { renderModelSlow(model) })
    }

    /**
     * Renders a model without any cache.
     * This is slow and should't be used directly, you should cache the model to improve performance
     *
     * @see cacheModel
     */
    @JvmStatic
    fun renderModelSlow(model: IBakedModel) {
        val quads = model.getQuads(null, null, Random(), EmptyModelData.INSTANCE)
        renderQuadsSlow(quads)
    }

    /**
     * Renders a list of quads without any cache.
     * This is slow and should't be used directly, you should cache the model to improve performance
     *
     * @see cacheModel
     */
    @JvmStatic
    fun renderQuadsSlow(quads: List<BakedQuad>) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        buffer.setTranslation(0.0, 0.0, 0.0)
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM)
        for (quad in quads) {
            buffer.addVertexData(quad.vertexData)
        }
        buffer.setTranslation(0.0, 0.0, 0.0)

        tessellator.draw()
    }

    /**
     * Rotates a block around the center following a Direction
     */
    @JvmStatic
    fun rotateAroundCenter(facing: Direction) {
        when (facing) {
            Direction.DOWN -> customRotate(DOWN_ROTATION, BLOCK_CENTER)
            Direction.SOUTH -> customRotate(SOUTH_ROTATION, BLOCK_CENTER)
            Direction.NORTH -> customRotate(NORTH_ROTATION, BLOCK_CENTER)
            Direction.EAST -> customRotate(EAST_ROTATION, BLOCK_CENTER)
            Direction.WEST -> customRotate(WEST_ROTATION, BLOCK_CENTER)
            else -> Unit
        }
    }

    /**
     * Rotates around a custom pivot
     */
    @JvmStatic
    fun customRotate(euler: Vec3d, pivot: Vec3d) {
        customRotate(euler.x, euler.y, euler.z, pivot)
    }

    /**
     * Rotates around a custom pivot
     */
    @JvmStatic
    fun customRotate(x: Double, y: Double, z: Double, pivot: Vec3d) {
        GlStateManager.translated(pivot.x, pivot.y, pivot.z)
        GlStateManager.rotated(x, 1.0, 0.0, 0.0)
        GlStateManager.rotated(y, 0.0, 1.0, 0.0)
        GlStateManager.rotated(z, 0.0, 0.0, 1.0)
        GlStateManager.translated(-pivot.x, -pivot.y, -pivot.z)
    }
}