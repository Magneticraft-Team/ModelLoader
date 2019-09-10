package com.cout970.modelloader.animation

import com.cout970.modelloader.TRSTransformation
import net.minecraft.util.ResourceLocation
import javax.vecmath.AxisAngle4d
import javax.vecmath.Quat4d
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d

interface IAnimationBuilder {
    fun add(node: AnimationNodeBuilder)
}

class AnimationBuilder : IAnimationBuilder {
    private val rootNodes = mutableListOf<AnimationNodeBuilder>()
    private val channels = mutableListOf<AnimationChannel>()

    fun clear(): AnimationBuilder {
        rootNodes.clear()
        channels.clear()
        return this
    }

    fun addTranlationChannel(target: Int, keyframes: List<AnimationKeyframe<Vector3d>>): AnimationBuilder {
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

class AnimationNodeBuilder(val id: Int, val parent: IAnimationBuilder) : IAnimationBuilder {
    internal val children = mutableListOf<AnimationNodeBuilder>()
    internal val transform = TRSTransformation()
    internal val vertices: MutableMap<ResourceLocation, MutableList<Vertex>> = mutableMapOf()

    fun withTranslation(x: Float, y: Float, z: Float): AnimationNodeBuilder {
        transform.translation.set(x.toDouble(), y.toDouble(), z.toDouble())
        return this
    }

    fun withTranslation(translation: Vector3d): AnimationNodeBuilder {
        transform.translation.set(translation)
        return this
    }

    fun withRotation(rotation: Quat4d): AnimationNodeBuilder {
        transform.rotation.set(rotation)
        return this
    }

    fun withEulerRotation(rotation: Vector3d): AnimationNodeBuilder {
        transform.rotation.mul(Quat4d().apply { set(AxisAngle4d(1.0, 0.0, 0.0, rotation.x)) })
        transform.rotation.mul(Quat4d().apply { set(AxisAngle4d(0.0, 1.0, 0.0, rotation.y)) })
        transform.rotation.mul(Quat4d().apply { set(AxisAngle4d(0.0, 0.0, 1.0, rotation.z)) })
        return this
    }

    fun withAxisRotation(rotation: AxisAngle4d): AnimationNodeBuilder {
        transform.rotation.set(rotation)
        return this
    }

    fun withAxisRotation(angle: Float, x: Float, y: Float, z: Float): AnimationNodeBuilder {
        transform.rotation.set(AxisAngle4d(x.toDouble(), y.toDouble(), z.toDouble(), angle.toDouble()))
        return this
    }

    fun withScale(scale: Vector3d): AnimationNodeBuilder {
        transform.scale.set(scale)
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

            VertexCollector.collect(list, storage)
        }
        return this
    }

    @JvmOverloads
    fun createChildren(id: Int = -1): AnimationNodeBuilder {
        return AnimationNodeBuilder(id, parent)
    }

    @JvmOverloads
    fun createChildren(id: Int = -1, func: AnimationNodeBuilder.() -> Unit): AnimationNodeBuilder {
        AnimationNodeBuilder(id, parent).apply(func).finish()
        return this
    }

    fun finish() {
        parent.add(this)
    }

    override fun add(node: AnimationNodeBuilder) {
        children += node
    }
}

fun usageExample() {
    AnimationBuilder()
        .createNode(0) {
            withTranslation(0f, 1f, 0f)
            withVertices(listOf())
        }
        .createNode() {
            withTranslation(1f, 0f, 0f)
            withVertices(listOf())

            createChildren(1) {
                withTranslation(1f, 0f, 0f)
                withVertices(listOf())
                finish()
            }
        }
        .addScaleChannel(0, listOf())
        .addTranlationChannel(1, listOf())
        .build()
}