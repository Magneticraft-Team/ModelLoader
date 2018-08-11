package com.cout970.modelloader.internal

import net.minecraft.util.ResourceLocation

/**
 * Tries to create a ResourceLocation from a relative path
 * if the path contains ':', it is parsed as a ResourceLocation
 */
fun resourceLocationOf(aux: ResourceLocation, path: String): ResourceLocation {
    if(path.contains(':')){
        return ResourceLocation(path)
    }
    return ResourceLocation(aux.resourceDomain, path)
}