@file:Suppress("DEPRECATION")

package com.cout970.modelloader.api

import net.minecraft.client.renderer.model.ItemCameraTransforms
import net.minecraft.client.renderer.model.ItemTransformVec3f
import javax.vecmath.Vector3f
import net.minecraft.client.renderer.Vector3f as McVector3

data class ItemTransforms(
    val thirdPersonLeft: ItemTransform,
    val thirdPersonRight: ItemTransform,
    val firstPersonLeft: ItemTransform,
    val firstPersonRight: ItemTransform,
    val head: ItemTransform,
    val gui: ItemTransform,
    val ground: ItemTransform,
    val fixed: ItemTransform
) {

    fun withThirdPersonLeft(it: ItemTransform) = copy(thirdPersonLeft = it)
    fun withThirdPersonRight(it: ItemTransform) = copy(thirdPersonRight = it)
    fun withFirstPersonLeft(it: ItemTransform) = copy(firstPersonLeft = it)
    fun withFirstPersonRight(it: ItemTransform) = copy(firstPersonRight = it)
    fun withHead(it: ItemTransform) = copy(head = it)
    fun withGui(it: ItemTransform) = copy(gui = it)
    fun withGround(it: ItemTransform) = copy(ground = it)
    fun withFixed(it: ItemTransform) = copy(fixed = it)

    companion object {
        @JvmField
        val DEFAULT = ItemTransforms(
            ItemTransform(), ItemTransform(), ItemTransform(), ItemTransform(), ItemTransform(), ItemTransform(), ItemTransform(), ItemTransform()
        )

        @JvmField
        val BLOCK_DEFAULT = ItemTransforms(
            thirdPersonLeft = ItemTransform(Vector3f(0f, 2.5f, 0f), Vector3f(75f, 225f, 0f), Vector3f(0.375f, 0.375f, 0.375f)),
            thirdPersonRight = ItemTransform(Vector3f(0f, 2.5f, 0f), Vector3f(75f, 45f, 0f), Vector3f(0.375f, 0.375f, 0.375f)),
            firstPersonLeft = ItemTransform(Vector3f(0f, 0f, 0f), Vector3f(0f, 225f, 0f), Vector3f(0.4f, 0.4f, 0.4f)),
            firstPersonRight = ItemTransform(Vector3f(0f, 0f, 0f), Vector3f(0f, 45f, 0f), Vector3f(0.4f, 0.4f, 0.4f)),
            head = ItemTransform(),
            gui = ItemTransform(Vector3f(0f, 0f, 0f), Vector3f(30f, 225f, 0f), Vector3f(0.625f, 0.625f, 0.625f)),
            ground = ItemTransform(Vector3f(0f, 3f, 0f), Vector3f(0f, 0f, 0f), Vector3f(0.25f, 0.25f, 0.25f)),
            fixed = ItemTransform(Vector3f(0f, 0f, 0f), Vector3f(0f, 0f, 0f), Vector3f(0.5f, 0.5f, 0.5f))
        )

        @JvmField
        val ITEM_DEFAULT = ItemTransforms(
            thirdPersonLeft = ItemTransform(Vector3f(0f, 3f, 1f), Vector3f(0f, 0f, 0f), Vector3f(0.55f, 0.55f, 0.55f)),
            thirdPersonRight = ItemTransform(Vector3f(0f, 3f, 1f), Vector3f(0f, 0f, 0f), Vector3f(0.55f, 0.55f, 0.55f)),
            firstPersonLeft = ItemTransform(Vector3f(1.13f, 3.2f, 1.13f), Vector3f(1.13f, 0f, -90f), Vector3f(0.68f, 0.68f, 0.68f)),
            firstPersonRight = ItemTransform(Vector3f(1.13f, 3.2f, 1.13f), Vector3f(1.13f, 0f, -90f), Vector3f(0.68f, 0.68f, 0.68f)),
            head = ItemTransform(Vector3f(0f, 13f, 7f), Vector3f(0f, 180f, 0f), Vector3f(1f, 1f, 1f)),
            gui = ItemTransform(),
            ground = ItemTransform(Vector3f(0f, 2f, 0f), Vector3f(0f, 0f, 0f), Vector3f(0.5f, 0.5f, 0.5f)),
            fixed = ItemTransform(Vector3f(0f, 0f, 0f), Vector3f(0f, 180f, 0f), Vector3f(1f, 1f, 1f))
        )
    }

    fun toItemCameraTransforms(): ItemCameraTransforms {
        return ItemCameraTransforms(
            thirdPersonLeft.toItemTransformVec3f(),
            thirdPersonRight.toItemTransformVec3f(),
            firstPersonLeft.toItemTransformVec3f(),
            firstPersonRight.toItemTransformVec3f(),
            head.toItemTransformVec3f(),
            gui.toItemTransformVec3f(),
            ground.toItemTransformVec3f(),
            fixed.toItemTransformVec3f()
        )
    }
}

data class ItemTransform(
    var translation: Vector3f = Vector3f(),
    var rotation: Vector3f = Vector3f(),
    var scale: Vector3f = Vector3f(1f, 1f, 1f)
) {
    fun toItemTransformVec3f() = ItemTransformVec3f(
        McVector3(rotation.x, rotation.y, rotation.z),
        McVector3(translation.x / 16, translation.y / 16, translation.z / 16),
        McVector3(scale.x, scale.y, scale.z)
    )
}