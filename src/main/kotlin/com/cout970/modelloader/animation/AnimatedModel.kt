package com.cout970.modelloader.animation

import com.cout970.modelloader.*
import com.cout970.modelloader.api.IRenderCache
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraftforge.client.ForgeHooksClient
import javax.vecmath.Quat4d
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d

data class AnimationKeyframe<T>(
    val time: Float,
    val value: T
)

data class AnimatedNode(
    val index: Int,
    val transform: TRSTransformation,
    val children: List<AnimatedNode>,
    val cache: IRenderCache
)

enum class AnimationChannelType {
    TRANSLATION, ROTATION, SCALE
}

data class AnimationChannel(
    val index: Int,
    val type: AnimationChannelType,
    val keyframes: List<AnimationKeyframe<Any>>
)

class AnimatedModel(val rootNodes: List<AnimatedNode>, val channels: List<AnimationChannel>) {

    /**
     * Total animation time in seconds
     */
    val length: Float by lazy {
        channels.mapNotNull { channel -> channel.keyframes.map { it.time }.max() }.max() ?: 1f
    }

    /**
     * Time is in seconds, to use ticks just divide by 20
     */
    fun render(time: Double) {
        val localTime = (time % length.toDouble()).toFloat()
        rootNodes.forEach { renderNode(it, localTime) }
    }

    fun renderNode(node: AnimatedNode, time: Float) {
        GlStateManager.pushMatrix()
        val matrix = getTransform(node, time).matrixVec.apply { transpose() }
        ForgeHooksClient.multiplyCurrentGlMatrix(matrix)
        node.cache.render()
        node.children.forEach { renderNode(it, time) }
        GlStateManager.popMatrix()
    }

    fun renderUntextured(time: Double) {
        val localTime = (time % length.toDouble()).toFloat()
        rootNodes.forEach { renderUntexturedNode(it, localTime) }
    }

    fun renderUntexturedNode(node: AnimatedNode, time: Float) {
        GlStateManager.pushMatrix()
        val matrix = getTransform(node, time).matrixVec.apply { transpose() }
        ForgeHooksClient.multiplyCurrentGlMatrix(matrix)
        node.cache.renderUntextured()
        node.children.forEach { renderUntexturedNode(it, time) }
        GlStateManager.popMatrix()
    }

    fun getTransform(node: AnimatedNode, time: Float): TRSTransformation {
        val channels = channels.filter { it.index == node.index }

        if (channels.isEmpty())
            return node.transform

        val translation = channels
            .filter { it.type == AnimationChannelType.TRANSLATION }
            .fold<AnimationChannel, Vector3d?>(null) { acc, channel ->
                val value = channel.keyframes.first().value
                check(value is Vector3d) { "Error: translate animation channel must use Vector3d for values, ${value::class.java} found" }
                @Suppress("UNCHECKED_CAST")
                val keyframes = channel.keyframes as List<AnimationKeyframe<Vector3d>>
                val (prev, next) = getPrevAndNext(time, keyframes)
                val new = interpolateVec3(time, prev, next)

                if (acc == null) new else acc + new
            } ?: node.transform.translation

        val rotation = channels
            .filter { it.type == AnimationChannelType.ROTATION }
            .fold<AnimationChannel, Quat4d?>(null) { acc, channel ->
                val value = channel.keyframes.first().value
                check(value is Vector4d) { "Error: rotate animation channel must use Vector4d for values, ${value::class.java} found" }
                @Suppress("UNCHECKED_CAST")
                val keyframes = channel.keyframes as List<AnimationKeyframe<Vector4d>>
                val (prev, next) = getPrevAndNext(time, keyframes)
                val new = interpolateQuat(time, prev, next)

                if (acc == null) new else acc * new
            } ?: node.transform.rotation

        val scale = channels
            .filter { it.type == AnimationChannelType.SCALE }
            .fold<AnimationChannel, Vector3d?>(null) { acc, channel ->
                val value = channel.keyframes.first().value
                check(value is Vector3d) { "Error: scale animation channel must use Vector3d for values, ${value::class.java} found" }
                @Suppress("UNCHECKED_CAST")
                val keyframes = channel.keyframes as List<AnimationKeyframe<Vector3d>>
                val (prev, next) = getPrevAndNext(time, keyframes)
                val new = interpolateVec3(time, prev, next)

                if (acc == null) new else acc + new
            } ?: node.transform.scale

        return TRSTransformation(translation, rotation, scale)
    }

    fun interpolateVec3(time: Float, prev: AnimationKeyframe<Vector3d>, next: AnimationKeyframe<Vector3d>): Vector3d {
        if (next.time == prev.time) return next.value

        val size = next.time - prev.time
        val step = (time - prev.time) / size

        return (prev.value).interpolated(next.value, step.toDouble())
    }

    fun interpolateQuat(time: Float, prev: AnimationKeyframe<Vector4d>, next: AnimationKeyframe<Vector4d>): Quat4d {
        if (next.time == prev.time) return next.value.asQuaternion()

        val size = next.time - prev.time
        val step = (time - prev.time) / size

        return prev.value.asQuaternion().interpolated(next.value.asQuaternion(), step.toDouble())
    }

    fun <T> getPrevAndNext(time: Float, keyframes: List<AnimationKeyframe<T>>): Pair<AnimationKeyframe<T>, AnimationKeyframe<T>> {
        val next = keyframes.firstOrNull { it.time > time } ?: keyframes.first()
        val prev = keyframes.lastOrNull { it.time <= time } ?: keyframes.last()

        return prev to next
    }
}