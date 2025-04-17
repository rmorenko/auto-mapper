package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.rmorenko.automapper.annotations.GenerateTarget
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.exceptions.MultiplyAnnotationException
import io.github.rmorenko.automapper.exceptions.NoAnnotationArguments
import io.github.rmorenko.automapper.getAnnotationArgumentValue
import io.github.rmorenko.automapper.getAnnotations
import io.github.rmorenko.automapper.getRequiredAnnotationArgumentValue
import io.github.rmorenko.automapper.model.GenerateTargetInfo

class GenerateTargetInfoResolver(private val logger: KSPLogger) {

    private val targetPropertiesInfoResolver = TargetPropertiesInfoResolver(logger)

    fun resolve(classDeclaration: KSClassDeclaration): GenerateTargetInfo {
        val annotations = getValidAnnotations(classDeclaration)
        val arguments = annotations.first().arguments.ifEmpty {
            throw NoAnnotationArguments(GenerateTarget::class)
        }
        val pkg = arguments.getAnnotationArgumentValue<String>("pkg").orEmpty().ifEmpty {
            classDeclaration.packageName.asString()
        }
        val name = arguments.getRequiredAnnotationArgumentValue<String>("name")
        val properties = targetPropertiesInfoResolver.resolve(classDeclaration)
        return GenerateTargetInfo(
            properties = properties.toList(),
            name = name,
            packageName = pkg.ifEmpty { classDeclaration.packageName.asString() },
        ).also {
            logger.info("GenerateTargetInfo: $it")
        }
    }

    private fun getValidAnnotations(classDeclaration: KSClassDeclaration): List<KSAnnotation> {
        val annotations = classDeclaration.getAnnotations(GenerateTarget::class).ifEmpty {
            throw AnnotationNotPresent(GenerateTarget::class)
        }
        if (annotations.size > 1) {
            throw MultiplyAnnotationException(GenerateTarget::class)
        }
        return annotations
    }
}
