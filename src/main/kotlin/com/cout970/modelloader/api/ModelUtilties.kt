package com.cout970.modelloader.api

import com.cout970.modelloader.api.formats.mcx.McxModel
import com.cout970.modelloader.api.formats.mcx.Mesh
import com.cout970.vector.extensions.*
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11

/**
 * Created by cout970 on 2017/06/17.
 */
object ModelUtilties {

    /**
     * Renders the model data, this renders the model *directly*,
     * so this is not suitable for continuous render, for example in a TileEntitySpecialRenderer,
     * this should be cached with a IRenderCache or other method
     *
     * example of correct use:
     * kotlin:
     *  val cache = RenderCacheDisplayList { renderModel(model) }
     *  cache.render()
     * java:
     *  IRenderCache cache = new RenderCacheDisplayList(() -> renderModel(model));
     *  cache.render()
     *
     *  Note: as any cache, makes no sense create it every time you need to use it,
     *        store it and call the render method any time you want to render the model
     */
    fun renderModel(model: McxModel) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        val storage = model.quads

        buffer.apply {
            begin(GL11.GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL)
            model.parts.forEach { part ->
                putPartInBuffer(part, storage, buffer)
            }
            setTranslation(0.0, 0.0, 0.0)
            tessellator.draw()
        }
    }

    /**
     * Same as renderModel but selection the parts you want to render
     */
    fun renderModelParts(model: McxModel, parts: List<McxModel.Part>) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        val storage = model.quads

        buffer.apply {
            begin(GL11.GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL)
            parts.forEach { part ->
                putPartInBuffer(part, storage, buffer)
            }
            setTranslation(0.0, 0.0, 0.0)
            tessellator.draw()
        }
    }

    /**
     * Helper function that inserts all the quads inside the part into the BufferBuilder
     */
    fun putPartInBuffer(part: McxModel.Part, storage: Mesh, buffer: BufferBuilder) {
        val indices = storage.indices.subList(part.from, part.to)
        indices.forEach { index ->
            val pos0 = storage.pos[index.a]
            val pos1 = storage.pos[index.b]
            val pos2 = storage.pos[index.c]
            val pos3 = storage.pos[index.d]

            val tex0 = storage.tex[index.at]
            val tex1 = storage.tex[index.bt]
            val tex2 = storage.tex[index.ct]
            val tex3 = storage.tex[index.dt]

            val normal = ((pos2 - pos0) cross (pos3 - pos1)).normalize()

            buffer.apply {
                pos(pos0.xd, pos0.yd, pos0.zd)
                        .tex(tex0.xd, tex0.yd)
                        .normal(normal.xf, normal.yf, normal.zf)
                        .endVertex()

                pos(pos1.xd, pos1.yd, pos1.zd)
                        .tex(tex1.xd, tex1.yd)
                        .normal(normal.xf, normal.yf, normal.zf)
                        .endVertex()

                pos(pos2.xd, pos2.yd, pos2.zd)
                        .tex(tex2.xd, tex2.yd)
                        .normal(normal.xf, normal.yf, normal.zf)
                        .endVertex()

                pos(pos3.xd, pos3.yd, pos3.zd)
                        .tex(tex3.xd, tex3.yd)
                        .normal(normal.xf, normal.yf, normal.zf)
                        .endVertex()
            }
        }
    }
}
