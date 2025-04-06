package com.morenko.automapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Class responsible for generating mapping functions for data classes.
 *
 * @property logger Logger for logging information during code generation.
 */

class MappingsCodeGenerator(private val logger: KSPLogger) {

    private val mappingCodeGenerator = MappingCodeGenerator(logger)
    private val mappingResolver = MappingResolver(logger)

    /**
     * Generates a mapping function for the given class declaration.
     *
     * @param classDeclaration The class declaration to generate the mapping function for.
     * @param resolver The resolver to use for resolving symbols.
     * @param codeGenerator The code generator to use for generating the mapping function.
     * @param processedClasses A set of class names that have already been processed.
     */
    @SuppressWarnings("MagicNumber")
    fun generateMapping(
        classDeclaration: KSClassDeclaration,
        resolver: Resolver,
        codeGenerator: CodeGenerator,
        processedClasses: MutableSet<String>
    ) {
        val sourcePackageName = classDeclaration.packageName.asString()
        val sourceClassName = classDeclaration.simpleName.asString()

        if (processedClasses.contains(sourceClassName)) return
        processedClasses.add(sourceClassName)
        val arguments = classDeclaration.getAnnotation(AutoMapper::class)?.arguments.orEmpty()
        logger.info("${AutoMapper::class.simpleName} arguments: $arguments")
        val annotationName = AutoMapper::class.simpleName.toString()
        val targetType = arguments.getAnnotationArgumentValue<KSType>("target")
            ?: throw AnnotationArgumentNotPresent(annotationName, "target")
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

        val functionName = "map${sourceClassName}To${sourceName}"
        val fileName = "${sourceClassName}Mapper"

        val functionBuilder = FunSpec.builder(functionName)
            .receiver(classDeclaration.toClassName())
            .returns(ClassName(sourcePackage, sourceName))

        val mappings = mappingResolver.processProperties(classDeclaration)
        logger.info("MappingFunctions = $mappings")
        val propertyMappings = mappingCodeGenerator.resolveMappings(
            classDeclaration, mappings, defaults,
            excludes
        )
        logger.info("PropertyMappings = $propertyMappings")

        functionBuilder.addStatement("return $sourceName(\n$propertyMappings\n    )")

        val fileBuilder = FileSpec.builder(sourcePackageName, fileName)
        val extensionPackages = resolver.getSymbolsWithAnnotation("MappingFunction")
            .filterIsInstance<KSClassDeclaration>()
            .map { it.packageName.asString() }
            .toSet()

        mappings.keys.forEach { funcName ->
            extensionPackages.forEach { extPackage ->
                fileBuilder.addImport(extPackage, funcName)
            }
        }

        additionalImports.forEach {
            fileBuilder.addImport(it, "")
        }

        val file = fileBuilder.addFunction(functionBuilder.build()).build()
        file.writeTo(codeGenerator, Dependencies.ALL_FILES)
    }
}
