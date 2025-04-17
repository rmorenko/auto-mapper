package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.rmorenko.automapper.annotations.Mapping
import io.github.rmorenko.automapper.exceptions.MultiplyAnnotationException
import io.github.rmorenko.automapper.getAnnotationProperty
import io.github.rmorenko.automapper.model.MappingInfo

private const val DOT = "."

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
                    it.shortName.asString() == Mapping::class.simpleName.toString()
                            && it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                            Mapping::class.qualifiedName
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
                val transform = arguments.getAnnotationProperty("transform").orEmpty()
                val target = arguments.getAnnotationProperty("target").orEmpty()
                logger.info("Transform annotation value for :$prop is $transform")
                val isExtension = transform.contains(DOT)
                val functionName = if(isExtension) {
                    transform.split(DOT).last()
                } else {
                    transform
                }
                propName to MappingInfo(
                    code = code,
                    functionName = functionName,
                    isExtension = isExtension,
                    target = target
                )
            }
    }
}

