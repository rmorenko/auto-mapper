package com.morenko.automapper

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Class responsible for resolving mappings for properties in a class declaration.
 *
 * @property logger Logger for logging information during the resolution process.
 */
class MappingResolver(val logger: KSPLogger) {

    /**
     * Processes the properties of the given class declaration to resolve their mappings.
     *
     * @param classDeclaration The class declaration to process properties for.
     * @return A map of property names to their corresponding mapping information.
     */
    fun processProperties(classDeclaration: KSClassDeclaration): Map<String, MappingInfo> {
        return classDeclaration.getAllProperties()
            .map { prop ->
                val annotation = prop.annotations.firstOrNull {
                    it.shortName.asString() ==
                            Mapping::class.simpleName.toString()
                }
                val arguments = annotation?.arguments.orEmpty()
                Triple(prop, annotation, arguments)
            }
            .filter { (_, _, arguments) ->
                arguments.isNotEmpty()
            }.associate { (prop, annotation, arguments) ->
                val propName = prop.simpleName.asString()
                logger.info("Function annotation arguments: $arguments")
                val code = annotation?.getAnnotationProperty("code").orEmpty()
                val type = annotation?.getAnnotationProperty("type")
                    ?.replace(".", "*")?.split("*")?.lastOrNull() ?: "CODE"
                val target = annotation?.getAnnotationProperty("target")
                propName to MappingInfo(code = code, type = MappingType.valueOf(type), target = target)
            }
    }
}

