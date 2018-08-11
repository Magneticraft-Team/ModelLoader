package com.cout970.modelloader.api

import com.cout970.modelloader.api.formats.gltf.GltfModel
import com.cout970.modelloader.api.formats.mcx.McxModel
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraftforge.client.model.obj.OBJModel

data class ModelEntry(
        val baked: IBakedModel?,
        val raw: Model
)

sealed class Model {
    class Mcx(val data: McxModel) : Model()
    class Gltf(val data: GltfModel) : Model()
    class Obj(val data: OBJModel) : Model()
    object Missing : Model()
}

