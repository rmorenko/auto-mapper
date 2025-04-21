package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.github.rmorenko.automapper.annotations.AddTargetProperty
import io.github.rmorenko.automapper.annotations.GenerateTarget
import io.github.rmorenko.automapper.annotations.GenerateTargetPropertyType
import io.github.rmorenko.automapper.exceptions.AnnotationArgumentNotPresent
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.exceptions.MultiplyAnnotationException
import io.github.rmorenko.automapper.getAnnotationArgumentValue
import io.github.rmorenko.automapper.getAnnotationProperty
import io.github.rmorenko.automapper.getAnnotations
import io.github.rmorenko.automapper.getRequiredAnnotationArgumentValue
import io.github.rmorenko.automapper.model.TargetPropertyInfo

/**
 * Class responsible for resolving information about properties for generated target class.
 *
 * @property logger Logger for logging information during the resolution process.
 */
class TargetPropertiesInfoResolver(private val logger: KSPLogger) {

    private val excludeResolver: TargetPropertyExcludeResolver = TargetPropertyExcludeResolver(logger)

    /**
     * Resolves information about properties for generated target class
     * @param classDeclaration The class declaration of source class.
     * @return list of TargetPropertyInfo
     */
    fun resolve(classDeclaration: KSClassDeclaration): List<TargetPropertyInfo> {
        val properties = classDeclaration.getAllProperties().filter {
            !excludeResolver.resolve(it)
        }.map { propertyDeclaration ->
            resolveTargetPropertyInfo(propertyDeclaration) ?: TargetPropertyInfo(
                name = propertyDeclaration.simpleName.asString(),
                pkg = propertyDeclaration.type.resolve().declaration.packageName.asString(),
                className = propertyDeclaration.type.resolve().declaration.simpleName.asString()
            )
        }.toMutableList()
        val generateTargetAnnotations = classDeclaration.getAnnotations(GenerateTarget::class)
        validate(generateTargetAnnotations)
        properties.addAll(resolveAdditionalTargetPropertyInfos(classDeclaration, generateTargetAnnotations))
        return properties.toList()
    }

    private fun resolveTargetPropertyInfo(propertyDeclaration: KSPropertyDeclaration): TargetPropertyInfo? {
        logger.info("Processing declaration type: ${propertyDeclaration.type.resolve()}")
        val generateTargetPropertyTypeAnnotations = propertyDeclaration.getAnnotations(
            GenerateTargetPropertyType::class
        )
            .toList()
        if (generateTargetPropertyTypeAnnotations.size > 1) {
            throw MultiplyAnnotationException(GenerateTargetPropertyType::class)
        }
        return generateTargetPropertyTypeAnnotations.firstOrNull()?.let { ksAnnotation ->
            TargetPropertyInfo(
                name = propertyDeclaration.simpleName.asString(),
                pkg = ksAnnotation.arguments.getRequiredAnnotationArgumentValue<String>("pkg"),
                className = ksAnnotation.arguments.getRequiredAnnotationArgumentValue<String>("className")
            )
        }
    }

    private fun resolveAdditionalTargetPropertyInfos(
        classDeclaration: KSClassDeclaration,
        generateTargetAnnotations: List<KSAnnotation>
    ): List<TargetPropertyInfo> {
        return generateTargetAnnotations.first().arguments
            .getAnnotationArgumentValue<ArrayList<KSAnnotation>>("props").orEmpty()
            .map { annotation ->
                val name = annotation.getAnnotationProperty("name")
                    ?: throw AnnotationArgumentNotPresent(AddTargetProperty::class, "name")
                val packageName = annotation.getAnnotationProperty("pkg") ?: classDeclaration.packageName.asString()
                val className = annotation.getAnnotationProperty("className")
                    ?: throw AnnotationArgumentNotPresent(AddTargetProperty::class, "className")
            TargetPropertyInfo(
                name,
                packageName,
                className,
            ).also {
                logger.info("Resolved targetInfoProperty $it")
            }
        }.toList()
    }

    private fun validate(generateTargetAnnotations: List<KSAnnotation>) {
        if (generateTargetAnnotations.isEmpty()) {
            throw AnnotationNotPresent(GenerateTarget::class)
        }
        if (generateTargetAnnotations.size > 1) {
            throw MultiplyAnnotationException(GenerateTarget::class)
        }
    }
}
