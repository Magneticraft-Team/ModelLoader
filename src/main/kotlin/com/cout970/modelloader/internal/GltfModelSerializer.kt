package com.cout970.modelloader.internal

import com.cout970.modelloader.api.formats.gltf.*
import com.cout970.modelloader.api.formats.gltf.GltfStructure.Animation
import com.cout970.modelloader.api.formats.gltf.GltfStructure.Buffer
import com.cout970.modelloader.api.formats.gltf.GltfStructure.Channel
import com.cout970.modelloader.api.formats.gltf.GltfStructure.File
import com.cout970.modelloader.api.formats.gltf.GltfStructure.Mesh
import com.cout970.modelloader.api.formats.gltf.GltfStructure.Node
import com.cout970.modelloader.api.formats.gltf.GltfStructure.Scene
import com.cout970.modelloader.api.util.TRSTransformation
import com.cout970.vector.api.IQuaternion
import com.cout970.vector.api.IVector2
import com.cout970.vector.api.IVector3
import com.cout970.vector.api.IVector4
import com.cout970.vector.extensions.*
import com.google.gson.GsonBuilder
import net.minecraft.client.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.IModel
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

private val GSON = GsonBuilder()
        .registerTypeAdapter(IVector4::class.java, Vector4Deserializer())
        .registerTypeAdapter(IVector3::class.java, Vector3Deserializer())
        .registerTypeAdapter(IVector2::class.java, Vector2Deserializer())
        .registerTypeAdapter(IQuaternion::class.java, QuaternionDeserializer())
        .registerTypeAdapter(IMatrix4::class.java, Matrix4Deserializer())
        .setPrettyPrinting()
        .create()


internal object GltfModelSerializer {

    private val MISSING_TEXTURE = ResourceLocation("minecraft", "missingno")

    private fun parseBuffers(file: GltfFile, folder: (String) -> InputStream): List<ByteArray> {
        return file.buffers.map { buff ->

            val uri = buff.uri ?: error("Found buffer without uri, unable to load, buffer: $buff")
            val bytes = folder(uri).readBytes()

            if (bytes.size != buff.byteLength) {
                error("Buffer byteLength, and resource size doesn't match, buffer: $buff, resource size: ${bytes.size}")
            }

            bytes
        }
    }

    private fun parseBufferViews(file: GltfFile, buffers: List<ByteArray>): List<ByteArray> {
        return file.bufferViews.map { view ->

            val buffer = buffers[view.buffer]
            val offset = view.byteOffset ?: 0
            val size = view.byteLength

            Arrays.copyOfRange(buffer, offset, offset + size)
        }
    }

    private fun parseAccessors(file: GltfFile, bufferViews: List<ByteArray>): List<Buffer> {
        return file.accessors.map { accessor ->

            val viewIndex = accessor.bufferView ?: error("Unsupported Empty BufferView at accessor: $accessor")

            val buffer = bufferViews[viewIndex]

            val offset = accessor.byteOffset ?: 0
            val type = GltfComponentType.fromId(accessor.componentType)

            val buff = ByteBuffer.wrap(buffer, offset, buffer.size - offset).order(ByteOrder.LITTLE_ENDIAN)
            val list: List<Any> = intoList(accessor.type, type, accessor.count, buff)

            Buffer(accessor.type, type, list)
        }
    }

    @Suppress("UnnecessaryVariable")
    private fun intoList(listType: GltfType, componentType: GltfComponentType, count: Int, buffer: ByteBuffer): List<Any> {
        val t = componentType
        val b = buffer
        return when (listType) {
            GltfType.SCALAR -> List(count) { b.next(t) }
            GltfType.VEC2 -> List(count) { vec2Of(b.next(t), b.next(t)) }
            GltfType.VEC3 -> List(count) { vec3Of(b.next(t), b.next(t), b.next(t)) }
            GltfType.VEC4 -> List(count) { vec4Of(b.next(t), b.next(t), b.next(t), b.next(t)) }
            GltfType.MAT2 -> error("Unsupported")
            GltfType.MAT3 -> error("Unsupported")
            GltfType.MAT4 -> error("Unsupported")
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun ByteBuffer.next(type: GltfComponentType): Number {
        return when (type) {
            GltfComponentType.BYTE, GltfComponentType.UNSIGNED_BYTE -> get()
            GltfComponentType.SHORT, GltfComponentType.UNSIGNED_SHORT -> short
            GltfComponentType.UNSIGNED_INT -> int
            GltfComponentType.FLOAT -> float
        }
    }

    private fun parseMeshes(file: GltfFile, accessors: List<Buffer>, location: ResourceLocation): List<Mesh> {
        return file.meshes.map { mesh ->
            val primitives = mesh.primitives.map { prim ->

                val attr = prim.attributes.map { (k, v) ->
                    Pair(GltfAttribute.valueOf(k), accessors[v])
                }.toMap()

                val indices = prim.indices?.let { accessors[it] }
                val mode = GltfMode.fromId(prim.mode)

                val material = getMaterial(file, prim.material, location)

                GltfStructure.Primitive(attr, indices, mode, material)
            }

            Mesh(primitives)
        }
    }

    private fun getMaterial(file: GltfFile, mat: Int?, location: ResourceLocation): ResourceLocation {
        if (mat == null) return MISSING_TEXTURE
        val material = file.materials[mat]
        val texture = material.pbrMetallicRoughness?.baseColorTexture?.index ?: return MISSING_TEXTURE
        val image = file.textures[texture].source ?: return MISSING_TEXTURE
        val path = file.images[image].uri ?: return MISSING_TEXTURE

        return resourceLocationOf(location, path)
    }

    private fun parseScenes(file: GltfFile, meshes: List<Mesh>): List<Scene> {
        return file.scenes.map { scene ->
            val nodes = scene.nodes ?: emptyList()
            val parsedNodes = nodes.map { parseNode(file, it, file.nodes[it], meshes) }

            Scene(parsedNodes)
        }
    }

    private fun parseNode(file: GltfFile, nodeIndex: Int, node: GltfNode, meshes: List<Mesh>): Node {
        val children = node.children.map { parseNode(file, it, file.nodes[it], meshes) }
        val mesh = node.mesh?.let { meshes[it] }

        val transform = TRSTransformation(
                translation = node.translation ?: Vector3.ORIGIN,
                rotation = node.rotation ?: Quaternion.IDENTITY,
                scale = node.scale ?: Vector3.ONE
        )

        return Node(nodeIndex, children, transform, mesh)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseChannel(channel: GltfAnimationChannel, samplers: List<GltfAnimationSampler>,
                             accessors: List<Buffer>): Channel {

        val sampler = samplers[channel.sampler]
        val timeValues = accessors[sampler.input].data.map { (it as Number).toFloat() }

        return Channel(
                node = channel.target.node,
                path = GltfChannelPath.valueOf(channel.target.path),
                times = timeValues,
                interpolation = sampler.interpolation,
                values = accessors[sampler.output].data
        )
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun parseAnimations(file: GltfFile, accessors: List<Buffer>): List<Animation> {
        return file.animations.filter { it.channels != null }.map { animation ->
            val channels = animation.channels.map { parseChannel(it, animation.samplers, accessors) }
            Animation(animation.name, channels)
        }
    }

    fun parse(file: GltfFile, location: ResourceLocation, folder: (String) -> InputStream): File {

        val buffers = parseBuffers(file, folder)
        val bufferViews = parseBufferViews(file, buffers)
        val accessors = parseAccessors(file, bufferViews)
        val meshes = parseMeshes(file, accessors, location)
        val scenes = parseScenes(file, meshes)
        val animations = parseAnimations(file, accessors)

        return File(scenes, animations)
    }

    fun load(resourceManager: IResourceManager, location: ResourceLocation, fileStream: InputStream): IModel {
        val file = GSON.fromJson(fileStream.reader(), GltfFile::class.java)

        val extraData = parse(file, location) { path ->
            val basePath = location.resourcePath.substringBeforeLast('/', "")
            val loc = ResourceLocation(location.resourceDomain, "$basePath/$path")

            resourceManager.getResource(loc).inputStream
        }
        return GltfModel(location, file, extraData)
    }
}