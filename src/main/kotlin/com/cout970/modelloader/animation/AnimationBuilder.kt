package com.cout970.modelloader.animation

import com.cout970.modelloader.api.TRSTransformation
import com.cout970.modelloader.mutable.MutableTRSTransformation
import net.minecraft.util.ResourceLocation
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d

/**
 * Builder that accepts sub-AnimationNodeBuilders
 */
interface IAnimationBuilder {
    fun add(node: AnimationNodeBuilder)
}

/**
 * Allows to create animation models
 */
class AnimationBuilder : IAnimationBuilder {
    private val rootNodes = mutableListOf<AnimationNodeBuilder>()
    private val channels = mutableListOf<AnimationChannel>()

    fun clear(): AnimationBuilder {
        rootNodes.clear()
        channels.clear()
        return this
    }

    fun addTranslationChannel(target: Int, keyframes: List<AnimationKeyframe<Vector3d>>): AnimationBuilder {
        @Suppress("UNCHECKED_CAST")
        channels += AnimationChannel(target, AnimationChannelType.TRANSLATION, keyframes as List<AnimationKeyframe<Any>>)
        return this
    }

    fun addRotationChannel(target: Int, keyframes: List<AnimationKeyframe<Vector4d>>): AnimationBuilder {
        @Suppress("UNCHECKED_CAST")
        channels += AnimationChannel(target, AnimationChannelType.ROTATION, keyframes as List<AnimationKeyframe<Any>>)
        return this
    }

    fun addScaleChannel(target: Int, keyframes: List<AnimationKeyframe<Vector3d>>): AnimationBuilder {
        @Suppress("UNCHECKED_CAST")
        channels += AnimationChannel(target, AnimationChannelType.SCALE, keyframes as List<AnimationKeyframe<Any>>)
        return this
    }

    @JvmOverloads
    fun createNode(id: Int = -1): AnimationNodeBuilder {
        return AnimationNodeBuilder(id, this)
    }

    @JvmOverloads
    fun createNode(id: Int = -1, func: AnimationNodeBuilder.() -> Unit): AnimationBuilder {
        AnimationNodeBuilder(id, this).apply(func).finish()
        return this
    }

    fun build(): AnimatedModel {
        val animationPoints = channels.map { it.index }.toSet()
        val optimized = AnimationOptimizer.optimize(rootNodes, animationPoints)
        return AnimatedModel(optimized, channels)
    }

    override fun add(node: AnimationNodeBuilder) {
        rootNodes += node
    }
}

/**
 * Builds a sinlge node for an animated model
 */
class AnimationNodeBuilder(val id: Int, val parent: IAnimationBuilder) : IAnimationBuilder {
    internal val children = mutableListOf<AnimationNodeBuilder>()
    internal val vertices: MutableMap<ResourceLocation, MutableList<Vertex>> = mutableMapOf()
    var transform = MutableTRSTransformation()
        private set

    fun withTransform(t: TRSTransformation): AnimationNodeBuilder {
        transform = t.toMutable()
        return this
    }

    fun withTransform(t: MutableTRSTransformation): AnimationNodeBuilder {
        transform = t
        return this
    }

    fun withTransform(t: (MutableTRSTransformation) -> Unit): AnimationNodeBuilder {
        transform.apply(t)
        return this
    }

    fun withVertices(newVertices: List<VertexGroup>): AnimationNodeBuilder {
        newVertices.forEach { (tex, list) ->
            var storage = vertices[tex]
            if (storage == null) {
                storage = mutableListOf()
                vertices[tex] = storage
            }

            storage.addAll(list)
        }
        return this
    }

    fun withQuads(quads: List<QuadGroup>): AnimationNodeBuilder {
        quads.forEach { (tex, list) ->
            var storage = vertices[tex]
            if (storage == null) {
                storage = mutableListOf()
                vertices[tex] = storage
            }

            VertexUtilities.collect(list, storage)
        }
        return this
    }

    @JvmOverloads
    fun createChildren(id: Int = -1): AnimationNodeBuilder {
        return AnimationNodeBuilder(id, this)
    }

    @JvmOverloads
    fun createChildren(id: Int = -1, func: AnimationNodeBuilder.() -> Unit): AnimationNodeBuilder {
        AnimationNodeBuilder(id, this).apply(func).finish()
        return this
    }

    fun finish() {
        parent.add(this)
    }

    override fun add(node: AnimationNodeBuilder) {
        children += node
    }
}

// Example usege from Kotlin
//fun usageExample() {
//    AnimationBuilder()
//        .createNode(0) {
//            transform.translate(0f, 1f, 0f)
//            withVertices(listOf())
//        }
//        .createNode() {
//            transform.translate(1f, 0f, 0f)
//            withVertices(listOf())
//
//            createChildren(1) {
//                transform.translate(1f, 0f, 0f)
//                withVertices(listOf())
//                finish()
//            }
//        }
//        .addScaleChannel(0, listOf())
//        .addTranslationChannel(1, listOf())
//        .build()
//}