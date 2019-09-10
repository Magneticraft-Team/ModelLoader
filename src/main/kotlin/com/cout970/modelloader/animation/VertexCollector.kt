package com.cout970.modelloader.animation

import com.cout970.modelloader.cross
import com.cout970.modelloader.minus
import com.cout970.modelloader.norm
import com.cout970.modelloader.unaryMinus
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import javax.vecmath.Vector2d
import javax.vecmath.Vector3d

object VertexCollector {

    fun collect(model: CompactModelData, sprite: TextureAtlasSprite?, storage: MutableList<Vertex>) {
        val indices = model.indices

        if (indices != null) {
            if (model.triangles) {
                repeat(model.count) {
                    collectQuadFromTriangleIndexed(it, indices, model.pos, model.tex, sprite, storage)
                }
            } else {
                repeat(model.count) {
                    collectQuadIndexed(it, indices, model.pos, model.tex, sprite, storage)
                }
            }
        } else {
            if (model.triangles) {
                repeat(model.count) {
                    collectQuadFromTriangle(it, model.pos, model.tex, sprite, storage)
                }
            } else {
                repeat(model.count) {
                    collectQuad(it, model.pos, model.tex, sprite, storage)
                }
            }
        }
    }

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

    private fun collectQuadIndexed(index: Int, indices: List<Int>, pos: List<Vector3d>, tex: List<Vector2d>,
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
        val normal = (ac cross bd).norm()

        result += vertexOf(a, at, normal)
        result += vertexOf(b, bt, normal)
        result += vertexOf(c, ct, normal)
        result += vertexOf(d, dt, normal)
    }

    private fun collectQuadFromTriangleIndexed(index: Int, indices: List<Int>, pos: List<Vector3d>, tex: List<Vector2d>,
                                               sprite: TextureAtlasSprite?, result: MutableList<Vertex>) {

        val a = pos[indices[index + 0]]
        val b = pos[indices[index + 1]]
        val c = pos[indices[index + 2]]

        val at = tex.getOrNull(indices[index + 0]).applySprite(sprite)
        val bt = tex.getOrNull(indices[index + 1]).applySprite(sprite)
        val ct = tex.getOrNull(indices[index + 2]).applySprite(sprite)

        val ac = c - a
        val ab = b - a
        val normal = -(ac cross ab).norm()

        result += vertexOf(a, at, normal)
        result += vertexOf(b, bt, normal)
        result += vertexOf(c, ct, normal)
        result += vertexOf(c, ct, normal)
    }

    private fun collectQuad(index: Int, pos: List<Vector3d>, tex: List<Vector2d>,
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
        val normal = (ac cross bd).norm()

        result += vertexOf(a, at, normal)
        result += vertexOf(b, bt, normal)
        result += vertexOf(c, ct, normal)
        result += vertexOf(d, dt, normal)
    }

    private fun collectQuadFromTriangle(index: Int, pos: List<Vector3d>, tex: List<Vector2d>,
                                        sprite: TextureAtlasSprite?, result: MutableList<Vertex>) {

        val a = pos[index + 0]
        val b = pos[index + 1]
        val c = pos[index + 2]

        val at = tex.getOrNull(index + 0).applySprite(sprite)
        val bt = tex.getOrNull(index + 1).applySprite(sprite)
        val ct = tex.getOrNull(index + 2).applySprite(sprite)

        val ac = c - a
        val ab = b - a
        val normal = -(ac cross ab).norm()

        result += vertexOf(a, at, normal)
        result += vertexOf(b, bt, normal)
        result += vertexOf(c, ct, normal)
        result += vertexOf(c, ct, normal)
    }

    private fun vertexOf(pos: Vector3d, uv: Vector2d, normal: Vector3d) = Vertex(
        pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(),
        uv.x.toFloat(), uv.y.toFloat(),
        normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()
    )

    fun Vector2d?.applySprite(sprite: TextureAtlasSprite?): Vector2d {
        this ?: return Vector2d()

        if (sprite != null) {
            return Vector2d(
                sprite.getInterpolatedU(x * 16.0).toDouble(),
                sprite.getInterpolatedV(y * 16.0).toDouble()
            )
        }

        return this
    }
}