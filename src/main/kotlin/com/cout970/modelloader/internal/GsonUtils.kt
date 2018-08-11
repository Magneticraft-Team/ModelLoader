package com.cout970.modelloader.internal

import com.cout970.modelloader.api.formats.gltf.IMatrix4
import com.cout970.vector.api.IQuaternion
import com.cout970.vector.api.IVector2
import com.cout970.vector.api.IVector3
import com.cout970.vector.api.IVector4
import com.cout970.vector.extensions.quatOf
import com.cout970.vector.extensions.vec2Of
import com.cout970.vector.extensions.vec3Of
import com.cout970.vector.extensions.vec4Of
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import javax.vecmath.Matrix4f

class QuaternionDeserializer : JsonDeserializer<IQuaternion> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): IQuaternion {
        val arr = json.asJsonArray
        return quatOf(arr[0].asNumber, arr[1].asNumber, arr[2].asNumber, arr[3].asNumber)
    }
}

class Vector4Deserializer : JsonDeserializer<IVector4> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): IVector4 {
        val arr = json.asJsonArray
        return vec4Of(arr[0].asNumber, arr[1].asNumber, arr[2].asNumber, arr[3].asNumber)
    }
}

class Vector3Deserializer : JsonDeserializer<IVector3> {
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

class Matrix4Deserializer : JsonDeserializer<IMatrix4> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): IMatrix4 {
        val array = json.asJsonArray
        return Matrix4f(
                array[0].asFloat, array[1].asFloat, array[2].asFloat, array[3].asFloat,
                array[4].asFloat, array[5].asFloat, array[6].asFloat, array[7].asFloat,
                array[8].asFloat, array[9].asFloat, array[10].asFloat, array[11].asFloat,
                array[12].asFloat, array[13].asFloat, array[14].asFloat, array[15].asFloat)
    }
}