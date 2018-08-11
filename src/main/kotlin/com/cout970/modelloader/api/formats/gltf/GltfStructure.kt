package com.cout970.modelloader.api.formats.gltf

import com.cout970.modelloader.api.util.TRSTransformation
import net.minecraft.util.ResourceLocation

object GltfStructure {

    class File(
            val scenes: List<Scene>,
            val animations: List<Animation>
    )

    class Scene(
            val nodes: List<Node>
    )

    class Node(
            val index: Int,
            val children: List<Node>,
            val transform: TRSTransformation,
            val mesh: Mesh? = null
    )

    class Mesh(
            val primitives: List<Primitive>
    )

    class Primitive(
            val attributes: Map<GltfAttribute, Buffer>,
            val indices: Buffer? = null,
            val mode: GltfMode,
            val material: ResourceLocation
    )

    data class Buffer(
            val type: GltfType,
            val componentType: GltfComponentType,
            val data: List<Any>
    )

    class Animation(
            val channels: List<Channel>
    )

    class Channel(
            val node: Int,
            val path: GltfChannelPath,
            val times: List<Float>,
            val interpolation: GltfInterpolation,
            val values: List<Any>
    )
}