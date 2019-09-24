package com.cout970.modelloader.api

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.Direction
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.model.data.EmptyModelData
import org.lwjgl.opengl.GL11
import java.util.*

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

    fun saveMatrix(func: () -> Unit) {
        GlStateManager.pushMatrix()
        func()
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun cacheModel(model: IBakedModel): IRenderCache {
        return TextureModelCache(AtlasTexture.LOCATION_BLOCKS_TEXTURE, ModelCache { renderModelSlow(model) })
    }

    @JvmStatic
    fun renderModelSlow(model: IBakedModel) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        buffer.setTranslation(0.0, 0.0, 0.0)
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM)
        val quads = model.getQuads(null, null, Random(), EmptyModelData.INSTANCE)
        for (quad in quads) {
            buffer.addVertexData(quad.vertexData)
        }
        buffer.setTranslation(0.0, 0.0, 0.0)

        tessellator.draw()
    }

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

    @JvmStatic
    fun customRotate(euler: Vec3d, pivot: Vec3d) {
        customRotate(euler.x, euler.y, euler.z, pivot)
    }

    @JvmStatic
    fun customRotate(x: Double, y: Double, z: Double, pivot: Vec3d) {
        GlStateManager.translated(pivot.x, pivot.y, pivot.z)
        GlStateManager.rotated(x, 1.0, 0.0, 0.0)
        GlStateManager.rotated(y, 0.0, 1.0, 0.0)
        GlStateManager.rotated(z, 0.0, 0.0, 1.0)
        GlStateManager.translated(-pivot.x, -pivot.y, -pivot.z)
    }
}