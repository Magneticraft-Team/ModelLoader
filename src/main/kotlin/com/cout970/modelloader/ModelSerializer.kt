package com.cout970.modelloader

import com.cout970.vector.api.IVector2
import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.vec2Of
import com.cout970.vector.extensions.vec3Of
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import java.io.InputStream
import java.lang.reflect.Type

/**
 * Created by cout970 on 2017/01/26.
 */
object ModelSerializer {

    val GSON = GsonBuilder()
            .registerTypeAdapter(QuadStorage.QuadIndices::class.java, QuadIndicesDeserializer())
            .registerTypeAdapter(IVector3::class.java, Vector3Deserializer())
            .registerTypeAdapter(IVector2::class.java, Vector2Deserializer())
            .registerTypeAdapter(ResourceLocation::class.java, ResourceLocationDeserializer())
            .create()!!

    fun load(stream: InputStream): ModelData {
        return GSON.fromJson(stream.reader(), ModelData::class.java)
    }

    class ResourceLocationDeserializer : JsonDeserializer<ResourceLocation> {
        override fun deserialize(json: JsonElement, typeOfT: Type?,
                                 context: JsonDeserializationContext?): ResourceLocation {
            return ResourceLocation(json.asString)
        }
    }

    class Vector3Deserializer : JsonDeserializer<Vec3d> {
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): IVector3 {
            val arr = json.asJsonArray
            return vec3Of(arr[0].asNumber, arr[1].asNumber, arr[2].asNumber)
        }
    }

    class Vector2Deserializer : JsonDeserializer<IVector2> {
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): IVector2 {
            val arr = json.asJsonArray
            return vec2Of(arr[0].asNumber, arr[1].asNumber)
        }
    }

    class QuadIndicesDeserializer : JsonDeserializer<QuadStorage.QuadIndices> {
        override fun deserialize(json: JsonElement, typeOfT: Type?,
                                 context: JsonDeserializationContext?): QuadStorage.QuadIndices {
            val pos = json.asJsonArray[0].asJsonArray
            val tex = json.asJsonArray[1].asJsonArray
            return QuadStorage.QuadIndices(pos[0].asInt, pos[1].asInt, pos[2].asInt, pos[3].asInt,
                    tex[0].asInt, tex[1].asInt, tex[2].asInt, tex[3].asInt)
        }
    }
}