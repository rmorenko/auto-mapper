package com.morenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.morenko.automapper.annotations.AutoMapper
import com.morenko.automapper.exceptions.AnnotationArgumentNotPresent
import com.morenko.automapper.exceptions.AnnotationNotPresent
import com.morenko.automapper.getAnnotation
import com.morenko.automapper.getAnnotationArgumentValue
import com.morenko.automapper.model.AutoMapperInfo
import com.morenko.automapper.model.toDefaultInfo

class AutoMapperInfoResolver(private val logger: KSPLogger) {

    fun resolve(classDeclaration: KSClassDeclaration): AutoMapperInfo {
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
}
