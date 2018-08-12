package com.cout970.modelloader.internal

import com.cout970.modelloader.api.formats.mcx.McxModel
import com.cout970.modelloader.api.formats.mcx.Mesh
import com.cout970.vector.api.IVector2
import com.cout970.vector.api.IVector3
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.minecraft.util.ResourceLocation
import java.io.InputStream
import java.lang.reflect.Type
/**
 * Created by cout970 on 2017/01/26.
 */

private val GSON = GsonBuilder()
        .registerTypeAdapter(Mesh.Indices::class.java, McxModelSerializer.QuadIndicesDeserializer())
        .registerTypeAdapter(IVector3::class.java, Vector3Deserializer())
        .registerTypeAdapter(IVector2::class.java, Vector2Deserializer())
        .registerTypeAdapter(ResourceLocation::class.java, McxModelSerializer.ResourceLocationDeserializer())
        .create()!!

internal object McxModelSerializer {

    fun load(stream: InputStream): McxModel {
        return GSON.fromJson(stream.reader(), McxModel::class.java)
    }

    class ResourceLocationDeserializer : JsonDeserializer<ResourceLocation> {
        override fun deserialize(json: JsonElement, typeOfT: Type?,
                                 context: JsonDeserializationContext?): ResourceLocation {
            return ResourceLocation(json.asString)
        }
    }

    class QuadIndicesDeserializer : JsonDeserializer<Mesh.Indices> {
        override fun deserialize(json: JsonElement, typeOfT: Type?,
                                 context: JsonDeserializationContext?): Mesh.Indices {
            val pos = json.asJsonArray[0].asJsonArray
            val tex = json.asJsonArray[1].asJsonArray
            return Mesh.Indices(pos[0].asInt, pos[1].asInt, pos[2].asInt, pos[3].asInt,
                    tex[0].asInt, tex[1].asInt, tex[2].asInt, tex[3].asInt)
        }
    }
}
