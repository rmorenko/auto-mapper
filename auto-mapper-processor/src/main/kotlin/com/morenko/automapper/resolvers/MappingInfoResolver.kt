package com.morenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.morenko.automapper.annotations.Mapping
import com.morenko.automapper.exceptions.MultiplyAnnotationException
import com.morenko.automapper.getAnnotationProperty
import com.morenko.automapper.model.MappingInfo

/**
 * Class responsible for resolving mappings for properties in a class declaration.
 *
 * @property logger Logger for logging information during the resolution process.
 */
class MappingInfoResolver(private val logger: KSPLogger) {

    /**
     * Processes the properties of the given class declaration to resolve their mappings.
     *
     * @param classDeclaration The class declaration to process properties for.
     * @return A map of property names to their corresponding mapping information.
     */
    fun resolve(classDeclaration: KSClassDeclaration): Map<String, MappingInfo> {
        return classDeclaration.getAllProperties()
            .map { prop ->
                if (prop.annotations.count() > 1) {
                    throw MultiplyAnnotationException(Mapping::class)
                }
                val annotation = prop.annotations.firstOrNull {
                    it.shortName.asString() ==
                            Mapping::class.simpleName.toString()
                }
                val arguments = annotation?.arguments.orEmpty()
                prop to arguments
            }
            .filter { (_, arguments) ->
                arguments.isNotEmpty()
            }.associate { (prop, arguments) ->
                val propName = prop.simpleName.asString()
                logger.info("Function annotation arguments: $arguments")
                val code = arguments.getAnnotationProperty("code").orEmpty()
                val mapFn = arguments.getAnnotationProperty("mapFn").orEmpty()
                val invokeFn = arguments.getAnnotationProperty("invokeFn").orEmpty()
                val target = arguments.getAnnotationProperty("target").orEmpty()
                val mapFirst = arguments.getAnnotationProperty("mapFirst").toBoolean()
                propName to MappingInfo(
                    code = code,
                    mapFn = mapFn,
                    invokeFn = invokeFn,
                    mapFirst = mapFirst,
                    target = target
                )
            }
    }
}

