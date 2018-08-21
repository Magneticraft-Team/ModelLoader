package com.cout970.modelloader.proxy

import com.cout970.modelloader.ModelLoaderMod
import com.cout970.modelloader.internal.ModelManager
import net.minecraftforge.common.MinecraftForge

/**
 * Created by cout970 on 2017/09/12.
 */
internal class Client : Server() {

    override fun init() {
        super.init()
        ModelLoaderMod.logger.info("${ModelLoaderMod.MOD_ID} starting client init")
        MinecraftForge.EVENT_BUS.register(ModelManager)

        //Debug
//        MinecraftForge.EVENT_BUS.register(this)
//        registerModel(
//                ModelResourceLocation("minecraft:gold_block", "normal"),
//                ResourceLocation("modelloader:model.gltf"), false
//        )
//
//        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBed::class.java, DebugTileEntityRenderer)
    }

//    @Suppress("unused", "UNUSED_PARAMETER")
//    @SubscribeEvent
//    fun onModelRegistryReload(event: ModelBakeEvent) {
//        DebugTileEntityRenderer.initModels()
//    }
}

//
//object DebugTileEntityRenderer : TileEntitySpecialRenderer<TileEntityBed>() {
//
//    private var model: IAnimatedModel? = null
//
//    override fun render(te: TileEntityBed, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
//        te.world = world
//        val time = te.world.totalWorldTime.toDouble()
//
//        if (!te.isHeadPiece) return
//
//        GlStateManager.pushMatrix()
//        GlStateManager.translate(x, y, z)
//        GlStateManager.disableTexture2D()
//        model?.render(time + partialTicks)
//        GlStateManager.enableTexture2D()
//        GlStateManager.popMatrix()
//    }
//
//    fun initModels() {
//        val loc = ModelResourceLocation("minecraft:gold_block", "normal")
//        val entry = ModelLoaderApi.getModelEntry(loc) ?: return
//        val model = entry.raw
//
//        if (model is Model.Gltf) {
//            this.model = GltfAnimationBuilder().buildPlain(model.data)
//        }
//    }
//}