package com.cout970.modelloader.api

import com.cout970.modelloader.internal.mcx.McxModel
import net.minecraft.client.renderer.block.model.IBakedModel

data class ModelEntry(
        val baked: IBakedModel?,
        val raw: Model
)

sealed class Model {
    class Mcx(val data: McxModel) : Model()
    class Gltf(val data: Any) : Model()
}

