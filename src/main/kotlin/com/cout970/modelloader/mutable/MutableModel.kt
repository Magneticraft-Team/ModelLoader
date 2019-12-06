package com.cout970.modelloader.mutable

import com.cout970.modelloader.api.IRenderCache
import com.cout970.modelloader.api.Utilities
import net.minecraft.util.ResourceLocation

/**
 * This model is intended to be modified by custom animations for entities and tile entities,
 * It allows to modify parts of the model, enable/disable parts, add custom transformations,
 * swap textures easily, etc.
 *
 * This has a decent performance for models with a small number of parts,
 * If you model is too complex, you can use an AnimatedModel to improve performance but you will lose the
 * ability to edit parts manually
 */
class MutableModel(
    val root: MutableModelNode
) {

    /**
     * Renders the model binding textures as necesary
     */
    fun render() = root.render()

    /**
     * Renders the model without binding textures
     */
    fun renderUntextured() = root.renderUntextured()
}

/**
 * This represents a node in tree of model parts
 *
 * For performance reasons you shouldn't create new instances of
 * MutableTRSTransformation and mutate them instead.
 */
class MutableModelNode(
    var content: IRenderCache,
    val children: MutableMap<String, MutableModelNode> = mutableMapOf(),
    var transform: MutableTRSTransformation = MutableTRSTransformation(),
    var useTransform: Boolean = false,
    var enable: Boolean = true,
    var texture: ResourceLocation? = null
) {

    /**
     * Renders this node and all its children
     */
    fun render() {
        if (!enable) return
        if (useTransform) transform.glMultiply()
        texture?.let { Utilities.bindTexture(it) }
        content.render()
        children.forEach { it.value.render() }
    }

    /**
     * Renders this node and all its children without binding any texture
     */
    fun renderUntextured() {
        if (!enable) return
        if (useTransform) transform.glMultiply()
        content.renderUntextured()
        children.forEach { it.value.renderUntextured() }
    }
}