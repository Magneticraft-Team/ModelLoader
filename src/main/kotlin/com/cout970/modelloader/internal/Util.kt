package com.cout970.modelloader.internal

import net.minecraft.util.ResourceLocation

/**
 * Tries to create a ResourceLocation from a relative path
 * if the path contains ':', it is parsed as a ResourceLocation
 */
internal fun resourceLocationOf(aux: ResourceLocation, path: String): ResourceLocation {
    if (path.contains(':')) {
        return ResourceLocation(path)
    }
    if (path.contains(".")) {
        val newPath = aux.resourcePath.substringBeforeLast('/', "") + path.substringBeforeLast(".", path)
        return ResourceLocation(aux.resourceDomain, newPath)
    }
    return ResourceLocation(aux.resourceDomain, path)
}