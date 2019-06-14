package com.cout970.modelloader.api

import com.google.common.collect.ImmutableMap
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.*
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraftforge.client.model.PerspectiveMapWrapper
import net.minecraftforge.client.model.SimpleModelState
import net.minecraftforge.common.model.TRSRTransformation
import javax.vecmath.Vector3f

/**
 * Created by cout970 on 2017/06/16.
 */
interface IBakedModelDecorator {

    /**
     * Decorate a baked model, for example wrapping the model with an IPerspectiveAwareModel
     */
    fun decorate(model: IBakedModel, modelResourceLocation: ModelResourceLocation): IBakedModel
}

/**
 * Helper class for the decorators that apply the same transformations as the forge blockstate.json loader
 */
abstract class ForgeDecorator : IBakedModelDecorator {

    companion object {
        val flipX = TRSRTransformation(null, null, Vector3f(-1f, 1f, 1f), null)

        fun get(tx: Float, ty: Float, tz: Float, ax: Float, ay: Float, az: Float, s: Float): TRSRTransformation {
            return TRSRTransformation.blockCenterToCorner(TRSRTransformation(
                Vector3f(tx / 16, ty / 16, tz / 16),
                TRSRTransformation.quatFromXYZDegrees(Vector3f(ax, ay, az)),
                Vector3f(s, s, s),
                null))
        }

        fun leftify(transform: TRSRTransformation): TRSRTransformation {
            return TRSRTransformation.blockCenterToCorner(
                flipX.compose(TRSRTransformation.blockCornerToCenter(transform)).compose(flipX))
        }
    }
}

/**
 * Blockstate property: forge:default-block
 */
object DefaultBlockDecorator : ForgeDecorator() {

    val map: ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation>

    init {
        val thirdperson = get(0f, 2.5f, 0f, 75f, 45f, 0f, 0.375f)
        val builder = ImmutableMap.builder<ItemCameraTransforms.TransformType, TRSRTransformation>()
        builder.put(GUI, get(0f, 0f, 0f, 30f, 225f, 0f, 0.625f))
        builder.put(GROUND, get(0f, 3f, 0f, 0f, 0f, 0f, 0.25f))
        builder.put(FIXED, get(0f, 0f, 0f, 0f, 0f, 0f, 0.5f))
        builder.put(THIRD_PERSON_RIGHT_HAND, thirdperson)
        builder.put(THIRD_PERSON_LEFT_HAND, leftify(thirdperson))
        builder.put(FIRST_PERSON_RIGHT_HAND, get(0f, 0f, 0f, 0f, 45f, 0f, 0.4f))
        builder.put(FIRST_PERSON_LEFT_HAND, get(0f, 0f, 0f, 0f, 225f, 0f, 0.4f))
        map = builder.build()
    }

    override fun decorate(model: IBakedModel, modelResourceLocation: ModelResourceLocation): IBakedModel {
        val state = SimpleModelState(map)
        return PerspectiveMapWrapper(model, state)
    }
}

/**
 * Blockstate property: forge:default-item
 */
object DefaultItemDecorator : ForgeDecorator() {

    val map: ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation>

    init {
        val thirdperson = get(0f, 3f, 1f, 0f, 0f, 0f, 0.55f)
        val firstperson = get(1.13f, 3.2f, 1.13f, 0f, -90f, 25f, 0.68f)
        val builder = ImmutableMap.builder<ItemCameraTransforms.TransformType, TRSRTransformation>()
        builder.put(GROUND, get(0f, 2f, 0f, 0f, 0f, 0f, 0.5f))
        builder.put(HEAD, get(0f, 13f, 7f, 0f, 180f, 0f, 1f))
        builder.put(THIRD_PERSON_RIGHT_HAND, thirdperson)
        builder.put(THIRD_PERSON_LEFT_HAND, leftify(thirdperson))
        builder.put(FIRST_PERSON_RIGHT_HAND, firstperson)
        builder.put(FIRST_PERSON_LEFT_HAND, leftify(firstperson))
        builder.put(FIXED, get(0f, 0f, 0f, 0f, 180f, 0f, 1f))
        map = builder.build()
    }

    override fun decorate(model: IBakedModel, modelResourceLocation: ModelResourceLocation): IBakedModel {
        val state = SimpleModelState(map)
        return PerspectiveMapWrapper(model, state)
    }
}

/**
 * Blockstate property: forge:default-tool
 */
object DefaultToolDecorator : ForgeDecorator() {

    val map: ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation>

    init {
        val builder = ImmutableMap.builder<ItemCameraTransforms.TransformType, TRSRTransformation>()
        builder.put(THIRD_PERSON_RIGHT_HAND, get(0f, 4f, 0.5f, 0f, -90f, 55f, 0.85f))
        builder.put(THIRD_PERSON_LEFT_HAND, get(0f, 4f, 0.5f, 0f, 90f, -55f, 0.85f))
        builder.put(FIRST_PERSON_RIGHT_HAND, get(1.13f, 3.2f, 1.13f, 0f, -90f, 25f, 0.68f))
        builder.put(FIRST_PERSON_LEFT_HAND, get(1.13f, 3.2f, 1.13f, 0f, 90f, -25f, 0.68f))
        map = builder.build()
    }

    override fun decorate(model: IBakedModel, modelResourceLocation: ModelResourceLocation): IBakedModel {
        val state = SimpleModelState(map)
        return PerspectiveMapWrapper(model, state)
    }
}