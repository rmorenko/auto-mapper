package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.exceptions.AnnotationArgumentNotPresent
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.exceptions.MultiplyAnnotationException
import io.github.rmorenko.automapper.getAnnotation
import io.github.rmorenko.automapper.getAnnotationArgumentValue
import io.github.rmorenko.automapper.model.AutoMapperInfo
import io.github.rmorenko.automapper.model.toDefaultInfo

class AutoMapperInfoResolver(private val logger: KSPLogger) {

    fun resolve(classDeclaration: KSClassDeclaration): AutoMapperInfo {
        validate(classDeclaration)
        val arguments = classDeclaration.getAnnotation(AutoMapper::class)?.arguments.orEmpty()
        arguments.ifEmpty {
            throw AnnotationNotPresent(AutoMapper::class)
        }
        logger.info("${AutoMapper::class.simpleName} arguments: $arguments")
        val targetType = arguments.getAnnotationArgumentValue<KSType>("target")
            ?: throw AnnotationArgumentNotPresent(AutoMapper::class, "target")
        val defaults = arguments.getAnnotationArgumentValue<ArrayList<KSAnnotation>>("defaults")
            ?.map(KSAnnotation::toDefaultInfo)
            ?.filter { defaultInfo ->
                defaultInfo.code.isNotEmpty() && defaultInfo.target.isNotEmpty()
            }?.toSet().orEmpty()
        logger.info("Defaults: $defaults")
        val additionalImports = arguments.getAnnotationArgumentValue<ArrayList<String>>("imports")
            ?.toSet().orEmpty()
        logger.info("Additional imports: $additionalImports")
        val excludes = arguments.getAnnotationArgumentValue<ArrayList<String>>("exclude")?.toSet().orEmpty()
        logger.info("Excludes: $excludes")

        val sourcePackage = targetType.declaration.packageName.asString()
        val sourceName = targetType.declaration.simpleName.asString()

        return AutoMapperInfo(
            targetPackage = sourcePackage,
            targetName = sourceName,
            additionalImports = additionalImports,
            defaults = defaults,
            excludes = excludes
        )
    }

    private fun validate(classDeclaration: KSClassDeclaration) {
        if (classDeclaration.annotations.count {
                AutoMapper::class.simpleName.toString() == it.shortName.asString()
            } > 1) throw MultiplyAnnotationException(AutoMapper::class)
    }
}
