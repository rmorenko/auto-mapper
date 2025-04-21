package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.exceptions.MultiplyAnnotationException
import io.github.rmorenko.automapper.exceptions.NoAnnotationArguments
import io.github.rmorenko.automapper.getAnnotationArgumentValue
import io.github.rmorenko.automapper.getAnnotations
import io.github.rmorenko.automapper.getRequiredAnnotationArgumentValue
import io.github.rmorenko.automapper.model.AutoMapperInfo
import io.github.rmorenko.automapper.model.toDefaultInfo

/**
 * Class responsible for resolving information from `@AutoMapper` annotation and
 * class declaration.
 *
 * @property logger Logger for logging information during the resolution process.
 */
class AutoMapperInfoResolver(private val logger: KSPLogger) {

    /**
     * Resolves information from `@AutoMapper` annotation.
     * @param classDeclaration The class declaration to generate target class.
     * @return data from `@AutoMapper` annotation.
     */
    fun resolve(classDeclaration: KSClassDeclaration): AutoMapperInfo {
        val annotations = getValidAnnotations(classDeclaration)
        val arguments = annotations.first().arguments.ifEmpty {
            throw NoAnnotationArguments(AutoMapper::class)
        }
        logger.info("${AutoMapper::class.simpleName} arguments: $arguments")
        val targetType = arguments.getAnnotationArgumentValue<KSType>("target")
        val defaults = arguments.getAnnotationArgumentValue<ArrayList<KSAnnotation>>("defaults")
            .orEmpty()
            .map(KSAnnotation::toDefaultInfo)
            .filter { defaultInfo ->
                defaultInfo.code.isNotEmpty() && defaultInfo.target.isNotEmpty()
            }.toSet()
        logger.info("Defaults: $defaults")
        val additionalImports = arguments.getAnnotationArgumentValue<ArrayList<String>>("imports")
            ?.toSet().orEmpty()
        logger.info("Additional imports: $additionalImports")
        val excludes = arguments.getAnnotationArgumentValue<ArrayList<String>>("exclude")?.toSet().orEmpty()
        logger.info("Excludes: $excludes")
        val targetClassInfo = if (targetType!= null && targetType.declaration.qualifiedName?.asString()
            != Unit::class.qualifiedName.toString()
        ) {
            targetType.declaration.packageName.asString() to targetType.declaration.simpleName.asString()
        } else {
            (arguments.getAnnotationArgumentValue<String>("targetPkg")?.ifEmpty {
                classDeclaration.packageName.asString()
            } ?: classDeclaration.packageName.asString()) to
                    arguments.getRequiredAnnotationArgumentValue<String>("targetName")

        }
        val targetPackage = targetClassInfo.first
        val targetName = targetClassInfo.second
        return AutoMapperInfo(
            targetPackage = targetPackage,
            targetName = targetName,
            additionalImports = additionalImports,
            defaults = defaults,
            excludes = excludes
        )
    }

    private fun getValidAnnotations(classDeclaration: KSClassDeclaration): List<KSAnnotation> {
        val annotations = classDeclaration.getAnnotations(AutoMapper::class).ifEmpty {
            throw AnnotationNotPresent(AutoMapper::class)
        }
        if (annotations.size > 1) {
            throw MultiplyAnnotationException(AutoMapper::class)
        }
        return annotations
    }
}
