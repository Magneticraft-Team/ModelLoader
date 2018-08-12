package com.cout970.modelloader.api.animation

import com.cout970.modelloader.api.IRenderCache
import com.cout970.modelloader.api.util.TRSTransformation
import com.cout970.modelloader.api.util.times
import com.cout970.vector.api.IQuaternion
import com.cout970.vector.api.IVector3
import com.cout970.vector.api.IVector4
import com.cout970.vector.extensions.asQuaternion
import com.cout970.vector.extensions.interpolate
import com.cout970.vector.extensions.plus
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.ForgeHooksClient

typealias Keyframe = Pair<Float, Any>

class AnimatedModel(val rootNodes: List<Node>, val channels: List<Channel>) : IAnimatedModel {

    val length: Float = channels.map { it.keyframes.map { (time) -> time }.max()!! }.max()!!

    data class Node(
            val index: Int,
            val transform: TRSTransformation,
            val children: List<Node>,
            val cache: IRenderCache
    )

    enum class ChannelType {
        TRANSLATION, ROTATION, SCALE
    }

    data class Channel(
            val index: Int,
            val type: ChannelType,
            val keyframes: List<Keyframe>
    )

    override fun render(tickTime: Double) {
        val time = tickTime / 20.0
        val localTime = (time % length.toDouble()).toFloat()
        rootNodes.forEach { renderNode(it, localTime) }
    }

    fun renderNode(node: Node, time: Float) {
        GlStateManager.pushMatrix()
        ForgeHooksClient.multiplyCurrentGlMatrix(getTransform(node, time).matrix.apply { transpose() })
        node.cache.render()
        node.children.forEach { renderNode(it, time) }
        GlStateManager.popMatrix()
    }

    fun getTransform(node: Node, time: Float): TRSTransformation {
        val channels = channels.filter { it.index == node.index }

        if (channels.isEmpty())
            return node.transform

        val translation = channels
                .filter { it.type == ChannelType.TRANSLATION }
                .fold<AnimatedModel.Channel, IVector3?>(null) { acc, channel ->
                    val (prev, next) = getPrevAndNext(time, channel.keyframes)
                    val new = interpolateVec3(time, prev, next)

                    if (acc == null) new else acc + new
                } ?: node.transform.translation

        val rotation = channels
                .filter { it.type == ChannelType.ROTATION }
                .fold<AnimatedModel.Channel, IQuaternion?>(null) { acc, channel ->
                    val (prev, next) = getPrevAndNext(time, channel.keyframes)
                    val new = interpolateQuat(time, prev, next)

                    if (acc == null) new else acc * new
                } ?: node.transform.rotation

        val scale = channels
                .filter { it.type == ChannelType.SCALE }
                .fold<AnimatedModel.Channel, IVector3?>(null) { acc, channel ->
                    val (prev, next) = getPrevAndNext(time, channel.keyframes)
                    val new = interpolateVec3(time, prev, next)

                    if (acc == null) new else acc + new
                } ?: node.transform.scale

        return TRSTransformation(translation, rotation, scale)
    }

    fun interpolateVec3(time: Float, prev: Keyframe, next: Keyframe): IVector3 {
        if (next.first == prev.first) return next.second as IVector3

        val size = next.first - prev.first
        val step = (time - prev.first) / size

        return (prev.second as IVector3).interpolate(next.second as IVector3, step.toDouble())
    }

    fun interpolateQuat(time: Float, prev: Keyframe, next: Keyframe): IQuaternion {
        if (next.first == prev.first) return (next.second as IVector4).asQuaternion()

        val size = next.first - prev.first
        val step = (time - prev.first) / size

        return IQuaternion().apply {
            interpolate((prev.second as IVector4).asQuaternion(), (next.second as IVector4).asQuaternion(), step)
        }
    }

    fun getPrevAndNext(time: Float, keyframes: List<Keyframe>): Pair<Keyframe, Keyframe> {
        val next = keyframes.firstOrNull { it.first > time } ?: keyframes.first()
        val prev = keyframes.lastOrNull { it.first <= time } ?: keyframes.last()

        return prev to next
    }
}