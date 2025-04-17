package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.rmorenko.automapper.annotations.AddTargetProperty
import io.github.rmorenko.automapper.annotations.GenerateTargetPropertyType
import io.github.rmorenko.automapper.exceptions.MultiplyAnnotationException
import io.github.rmorenko.automapper.getAnnotations
import io.github.rmorenko.automapper.getRequiredAnnotationArgumentValue
import io.github.rmorenko.automapper.model.TargetPropertyInfo

class TargetPropertiesInfoResolver(private val logger: KSPLogger) {

    private val excludeResolver: TargetPropertyExcludeResolver = TargetPropertyExcludeResolver(logger)

    fun resolve(classDeclaration: KSClassDeclaration): List<TargetPropertyInfo> {
        val properties = classDeclaration.getAllProperties().filter {
            !excludeResolver.resolve(it)
        }.map { propertyDeclaration ->
            logger.info("Processing declaration type: ${propertyDeclaration.type.resolve()}")
            val generateTargetPropertyTypeAnnotations = propertyDeclaration.getAnnotations(
                GenerateTargetPropertyType::class
            )
                .toList()
            if (generateTargetPropertyTypeAnnotations.size > 1) {
                throw MultiplyAnnotationException(GenerateTargetPropertyType::class)
            }
            generateTargetPropertyTypeAnnotations.firstOrNull()?.let { ksAnnotation ->
                TargetPropertyInfo(
                    name = propertyDeclaration.simpleName.asString(),
                    pkg = ksAnnotation.arguments.getRequiredAnnotationArgumentValue<String>("pkg"),
                    className = ksAnnotation.arguments.getRequiredAnnotationArgumentValue<String>("className")
                )

            } ?: TargetPropertyInfo(
                name = propertyDeclaration.simpleName.asString(),
                pkg = propertyDeclaration.type.resolve().declaration.packageName.asString(),
                className = propertyDeclaration.type.resolve().declaration.simpleName.asString()
            )
        }.toMutableList()
        properties.addAll(resolveAdditionalProperties(classDeclaration))
        return properties.toList()
    }

    private fun resolveAdditionalProperties(classDeclaration: KSClassDeclaration): List<TargetPropertyInfo> {
        return classDeclaration.getAllProperties().map { property ->
            val name = property.simpleName.asString()
            val addTargetPropertyAnnotations = property.getAnnotations(AddTargetProperty::class)
                .toList()
            if (addTargetPropertyAnnotations.size > 1) {
                throw MultiplyAnnotationException(AddTargetProperty::class)
            }
            addTargetPropertyAnnotations.firstOrNull()?.arguments?.let {
                name to it
            }
        }.filterNotNull()
            .map { pair ->
            val name = pair.second.getRequiredAnnotationArgumentValue<String>("name")
            val packageName = pair.second.getRequiredAnnotationArgumentValue<String>("pkg")
            val className = pair.second.getRequiredAnnotationArgumentValue<String>("className")
            TargetPropertyInfo(
                name,
                packageName,
                className,
            ).also {
                logger.info("Resolved targetInfoProperty $it")
            }
        }.toList()
    }
}
