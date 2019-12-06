package com.cout970.modelloader.animation

import com.cout970.modelloader.api.IRenderCache
import com.cout970.modelloader.api.TRSTransformation
import com.cout970.modelloader.asQuaternion
import com.cout970.modelloader.interpolated
import com.cout970.modelloader.plus
import com.cout970.modelloader.times
import com.mojang.blaze3d.platform.GlStateManager
import javax.vecmath.Quat4d
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d

/**
 * A node in the animated model.
 * The more nodes a model has the more expensive it is to render the model
 */
data class AnimatedNode(
    val index: Int,
    var transform: TRSTransformation,
    val children: List<AnimatedNode>,
    val cache: IRenderCache
) {
    fun close() {
        children.forEach(AnimatedNode::close)
        cache.close()
    }

    fun asyncClose(runner: (Runnable) -> Unit) {
        children.forEach { it.asyncClose(runner) }
        cache.asyncClose(runner)
    }
}

/**
 * A channel of the animation
 * Stores changes to a property of the model, the value of the property is calculated using the keyframes
 * - index is the id of the node to modify
 * - type is the property to modify
 * - keyframes have the values to put in the property and the time where to put them
 */
data class AnimationChannel(
    val index: Int,
    val type: AnimationChannelType,
    val keyframes: List<AnimationKeyframe<Any>>
)

/**
 * Type of an AnimationChannel
 */
enum class AnimationChannelType {
    TRANSLATION, ROTATION, SCALE
}

/**
 * A single keyframe of the animation,
 * Stores the value of a property of the model in a concrete animation time
 */
data class AnimationKeyframe<T>(
    val time: Float,
    val value: T
)

/**
 * An animated model
 */
class AnimatedModel(val rootNodes: List<AnimatedNode>, val channels: List<AnimationChannel>) {

    /**
     * Total animation time in seconds
     */
    val length: Float by lazy {
        channels.mapNotNull { channel -> channel.keyframes.map { it.time }.max() }.max() ?: 1f
    }

    /**
     * Renders the model with an specified animation time
     * Time is in seconds, to use ticks just divide by 20
     */
    fun render(time: Float) {
        val localTime = time % length
        rootNodes.forEach { renderNode(it, localTime) }
    }

    /**
     * Same but with Double as input, avoids hidden casts at call location
     */
    fun render(time: Double) {
        render(time.toFloat())
    }

    /**
     * Renders a single node tree of the model
     */
    fun renderNode(node: AnimatedNode, time: Float) {
        GlStateManager.pushMatrix()
        getTransform(node, time).glMultiply()
        node.cache.render()
        node.children.forEach { renderNode(it, time) }
        GlStateManager.popMatrix()
    }

    /**
     *  Renders the model without texture with an specified animation time
     * Time is in seconds, to use ticks just divide by 20
     */
    fun renderUntextured(time: Double) {
        val localTime = (time % length.toDouble()).toFloat()
        rootNodes.forEach { renderUntexturedNode(it, localTime) }
    }

    /**
     * Renders a single node tree of the model without textures
     */
    fun renderUntexturedNode(node: AnimatedNode, time: Float) {
        GlStateManager.pushMatrix()
        getTransform(node, time).glMultiply()
        node.cache.renderUntextured()
        node.children.forEach { renderUntexturedNode(it, time) }
        GlStateManager.popMatrix()
    }

    /**
     * Gets the Transformation of a node given an animation time
     */
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

    /**
     * Interpolate animation keyframes with Vectors
     */
    fun interpolateVec3(time: Float, prev: AnimationKeyframe<Vector3d>, next: AnimationKeyframe<Vector3d>): Vector3d {
        if (next.time == prev.time) return next.value

        val size = next.time - prev.time
        val step = (time - prev.time) / size

        return (prev.value).interpolated(next.value, step.toDouble())
    }

    /**
     * Interpolate animation keyframes with Quaternions
     */
    fun interpolateQuat(time: Float, prev: AnimationKeyframe<Vector4d>, next: AnimationKeyframe<Vector4d>): Quat4d {
        if (next.time == prev.time) return next.value.asQuaternion()

        val size = next.time - prev.time
        val step = (time - prev.time) / size

        return prev.value.asQuaternion().interpolated(next.value.asQuaternion(), step.toDouble())
    }

    /**
     * Returns a pair of keyframes to interpolate
     */
    fun <T> getPrevAndNext(time: Float, keyframes: List<AnimationKeyframe<T>>): Pair<AnimationKeyframe<T>, AnimationKeyframe<T>> {
        val next = keyframes.firstOrNull { it.time > time } ?: keyframes.first()
        val prev = keyframes.lastOrNull { it.time <= time } ?: keyframes.last()

        return prev to next
    }

    /**
     * Frees the GPU memory used by the model
     */
    fun close() {
        rootNodes.forEach { it.close() }
    }

    fun asyncClose(runner: (Runnable) -> Unit) {
        rootNodes.forEach { it.asyncClose(runner) }
    }
}