package com.cout970.modelloader.animation

import com.cout970.modelloader.cross
import com.cout970.modelloader.minus
import com.cout970.modelloader.norm
import com.cout970.modelloader.unaryMinus
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.client.renderer.vertex.VertexFormatElement
import net.minecraft.util.Direction
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad
import javax.vecmath.Vector2d
import javax.vecmath.Vector3d

object VertexUtilities {

    /**
     * Creates a list of baked quads from a list of Vertex
     */
    fun bakedVertices(format: VertexFormat, sprite: TextureAtlasSprite, storage: List<Vertex>): List<BakedQuad> {
        val quads = mutableListOf<BakedQuad>()

        repeat(storage.size / 4) {
            val a = storage[4 * it]
            val b = storage[4 * it + 1]
            val c = storage[4 * it + 2]
            val d = storage[4 * it + 3]

            val unpacked = UnpackedBakedQuad.Builder(format).apply {
                setContractUVs(true)
                setTexture(sprite)
                putVertex(format, a, sprite)
                putVertex(format, b, sprite)
                putVertex(format, c, sprite)
                putVertex(format, d, sprite)
                setQuadOrientation(Direction.getFacingFromVector(a.xn, a.yn, a.zn))
            }.build()

            quads += compact(unpacked)
        }

        return quads
    }

    fun compact(unpacked: UnpackedBakedQuad): BakedQuad {
        return BakedQuad(
            unpacked.vertexData,
            unpacked.tintIndex,
            unpacked.face,
            unpacked.sprite,
            unpacked.shouldApplyDiffuseLighting(),
            unpacked.format
        )
    }

    /**
     * Auxiliary function
     */
    fun UnpackedBakedQuad.Builder.putVertex(format: VertexFormat, vertex: Vertex, sprite: TextureAtlasSprite) {
        repeat(format.elementCount) { e ->
            val elem = format.getElement(e)
            when (elem.usage) {
                VertexFormatElement.Usage.POSITION -> put(e, vertex.x, vertex.y, vertex.z, 1f)
                VertexFormatElement.Usage.COLOR -> put(e, 1f, 1f, 1f, 1f)
                VertexFormatElement.Usage.NORMAL -> put(e, vertex.xn, vertex.yn, vertex.zn, 0f)
                VertexFormatElement.Usage.UV -> if (elem.index == 0) {
                    put(e, sprite.getInterpolatedU(vertex.u * 16.0), sprite.getInterpolatedV(vertex.v * 16.0), 0f, 1f)
                }
                else -> put(e)
            }
        }
    }

    /**
     * Fills a list of vertices from a compact model
     */
    fun collect(model: ICompactModelData, sprite: TextureAtlasSprite?, storage: MutableList<Vertex>) {
        val indices = model.indices

        if (indices != null) {
            if (model.triangles) {
                repeat(model.count / 3) {
                    collectQuadFromTriangleIndexed(it * 3, indices, model.pos, model.tex, sprite, storage)
                }
            } else {
                repeat(model.count / 4) {
                    collectQuadIndexed(it * 4, indices, model.pos, model.tex, sprite, storage)
                }
            }
        } else {
            if (model.triangles) {
                repeat(model.count / 3) {
                    collectQuadFromTriangle(it * 3, model.pos, model.tex, sprite, storage)
                }
            } else {
                repeat(model.count / 4) {
                    collectQuad(it * 4, model.pos, model.tex, sprite, storage)
                }
            }
        }
    }

    /**
     * Fills a list of vertices from a list of baked quads
     */
    fun collect(list: List<BakedQuad>, storage: MutableList<Vertex>) {
        list.forEach {
            storage += Vertex(
                x = Float.fromBits(it.vertexData[0]),
                y = Float.fromBits(it.vertexData[1]),
                z = Float.fromBits(it.vertexData[2]),
                u = Float.fromBits(it.vertexData[3]),
                v = Float.fromBits(it.vertexData[4]),
                xn = Float.fromBits(it.vertexData[5]),
                yn = Float.fromBits(it.vertexData[6]),
                zn = Float.fromBits(it.vertexData[7])
            )
            storage += Vertex(
                x = Float.fromBits(it.vertexData[8]),
                y = Float.fromBits(it.vertexData[9]),
                z = Float.fromBits(it.vertexData[10]),
                u = Float.fromBits(it.vertexData[11]),
                v = Float.fromBits(it.vertexData[12]),
                xn = Float.fromBits(it.vertexData[13]),
                yn = Float.fromBits(it.vertexData[14]),
                zn = Float.fromBits(it.vertexData[15])
            )
            storage += Vertex(
                x = Float.fromBits(it.vertexData[16]),
                y = Float.fromBits(it.vertexData[17]),
                z = Float.fromBits(it.vertexData[18]),
                u = Float.fromBits(it.vertexData[19]),
                v = Float.fromBits(it.vertexData[20]),
                xn = Float.fromBits(it.vertexData[21]),
                yn = Float.fromBits(it.vertexData[22]),
                zn = Float.fromBits(it.vertexData[23])
            )
            storage += Vertex(
                x = Float.fromBits(it.vertexData[24]),
                y = Float.fromBits(it.vertexData[25]),
                z = Float.fromBits(it.vertexData[26]),
                u = Float.fromBits(it.vertexData[27]),
                v = Float.fromBits(it.vertexData[28]),
                xn = Float.fromBits(it.vertexData[29]),
                yn = Float.fromBits(it.vertexData[30]),
                zn = Float.fromBits(it.vertexData[31])
            )
        }
    }

    /**
     * Collect vertices from indexed buffers of quads
     */
    fun collectQuadIndexed(index: Int, indices: List<Int>, pos: List<Vector3d>, tex: List<Vector2d>,
                           sprite: TextureAtlasSprite?, result: MutableList<Vertex>) {

        val a = pos[indices[index + 0]]
        val b = pos[indices[index + 1]]
        val c = pos[indices[index + 2]]
        val d = pos[indices[index + 3]]

        val at = tex.getOrNull(indices[index + 0]).applySprite(sprite)
        val bt = tex.getOrNull(indices[index + 1]).applySprite(sprite)
        val ct = tex.getOrNull(indices[index + 2]).applySprite(sprite)
        val dt = tex.getOrNull(indices[index + 3]).applySprite(sprite)

        val ac = c - a
        val bd = d - b
        val rawNorm = (ac cross bd).norm()
        val normal = if (rawNorm.x.isNaN() || rawNorm.y.isNaN() || rawNorm.z.isNaN()) {
            Vector3d(0.0, 1.0, 0.0)
        } else rawNorm

        result += vertexOf(a, at, normal)
        result += vertexOf(b, bt, normal)
        result += vertexOf(c, ct, normal)
        result += vertexOf(d, dt, normal)
    }

    /**
     * Collect vertices from indexed buffers of triangles
     */
    fun collectQuadFromTriangleIndexed(index: Int, indices: List<Int>, pos: List<Vector3d>, tex: List<Vector2d>,
                                       sprite: TextureAtlasSprite?, result: MutableList<Vertex>) {

        val a = pos[indices[index + 0]]
        val b = pos[indices[index + 1]]
        val c = pos[indices[index + 2]]

        val at = tex.getOrNull(indices[index + 0]).applySprite(sprite)
        val bt = tex.getOrNull(indices[index + 1]).applySprite(sprite)
        val ct = tex.getOrNull(indices[index + 2]).applySprite(sprite)

        val ac = c - a
        val ab = b - a
        val rawNorm = -(ac cross ab).norm()
        val normal = if (rawNorm.x.isNaN() || rawNorm.y.isNaN() || rawNorm.z.isNaN()) {
            Vector3d(0.0, 1.0, 0.0)
        } else rawNorm

        result += vertexOf(a, at, normal)
        result += vertexOf(b, bt, normal)
        result += vertexOf(c, ct, normal)
        result += vertexOf(c, ct, normal)
    }

    /**
     * Collect vertices from buffers of quads
     */
    fun collectQuad(index: Int, pos: List<Vector3d>, tex: List<Vector2d>,
                    sprite: TextureAtlasSprite?, result: MutableList<Vertex>) {

        val a = pos[index + 0]
        val b = pos[index + 1]
        val c = pos[index + 2]
        val d = pos[index + 3]

        val at = tex.getOrNull(index + 0).applySprite(sprite)
        val bt = tex.getOrNull(index + 1).applySprite(sprite)
        val ct = tex.getOrNull(index + 2).applySprite(sprite)
        val dt = tex.getOrNull(index + 3).applySprite(sprite)

        val ac = c - a
        val bd = d - b
        val rawNorm = (ac cross bd).norm()
        val normal = if (rawNorm.x.isNaN() || rawNorm.y.isNaN() || rawNorm.z.isNaN()) {
            Vector3d(0.0, 1.0, 0.0)
        } else rawNorm

        result += vertexOf(a, at, normal)
        result += vertexOf(b, bt, normal)
        result += vertexOf(c, ct, normal)
        result += vertexOf(d, dt, normal)
    }

    /**
     * Collect vertices from buffers of triangles
     */
    fun collectQuadFromTriangle(index: Int, pos: List<Vector3d>, tex: List<Vector2d>,
                                sprite: TextureAtlasSprite?, result: MutableList<Vertex>) {

        val a = pos[index + 0]
        val b = pos[index + 1]
        val c = pos[index + 2]

        val at = tex.getOrNull(index + 0).applySprite(sprite)
        val bt = tex.getOrNull(index + 1).applySprite(sprite)
        val ct = tex.getOrNull(index + 2).applySprite(sprite)

        val ac = c - a
        val ab = b - a
        val rawNorm = -(ac cross ab).norm()
        val normal = if (rawNorm.x.isNaN() || rawNorm.y.isNaN() || rawNorm.z.isNaN()) {
            Vector3d(0.0, 1.0, 0.0)
        } else rawNorm

        result += vertexOf(a, at, normal)
        result += vertexOf(b, bt, normal)
        result += vertexOf(c, ct, normal)
        result += vertexOf(c, ct, normal)
    }

    /**
     * Creates a vertex from data in vectors
     */
    fun vertexOf(pos: Vector3d, uv: Vector2d, normal: Vector3d) = Vertex(
        pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(),
        uv.x.toFloat(), uv.y.toFloat(),
        normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()
    )

    /**
     * Maps a UV coordinate into a sprite
     * If the sprite is null, the original coordinate is returned
     */
    fun Vector2d?.applySprite(sprite: TextureAtlasSprite?): Vector2d {
        this ?: return Vector2d()

        return when {
            sprite != null -> {
                val u = sprite.getInterpolatedU(this.x * 16.0).toDouble()
                val v = sprite.getInterpolatedV(this.y * 16.0).toDouble()
                Vector2d(u, v)
            }
            else -> this
        }
    }
}