package com.morenko.automapper.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.morenko.automapper.annotations.Mapping
import com.morenko.automapper.model.MappingInfo
import com.morenko.automapper.resolvers.AutoMapperInfoResolver
import com.morenko.automapper.resolvers.MappingInfoResolver
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Class responsible for generating mapping function for data classes.
 *
 * @property logger Logger for logging information during code generation.
 */

class MappingFunctionGenerator(private val logger: KSPLogger) {

    private val mappingInfoResolver = MappingInfoResolver(logger)
    private val propertyMappingsGenerator = PropertyMappingsGenerator(logger)
    private val autoMaResolver = AutoMapperInfoResolver(logger)

    /**
     * Generates a mapping function for the given class declaration.
     *
     * @param classDeclaration The class declaration to generate the mapping function for.
     * @param resolver The resolver to use for resolving symbols.
     * @param codeGenerator The code generator to use for generating the mapping function.
     * @param processedClasses A set of class names that have already been processed.
     */
    fun generate(
        classDeclaration: KSClassDeclaration,
        resolver: Resolver,
        codeGenerator: CodeGenerator,
        processedClasses: MutableSet<String>
    ) {
        val sourcePackageName = classDeclaration.packageName.asString()
        val sourceClassName = classDeclaration.simpleName.asString()

        if (processedClasses.contains(sourceClassName)) return
        processedClasses.add(sourceClassName)
        val autoMapperInfo = autoMaResolver.resolve(classDeclaration)

        val functionName = "map${sourceClassName}To${autoMapperInfo.targetName}"
        val fileName = "${sourceClassName}Mapper"

        val functionBuilder = FunSpec.builder(functionName)
            .receiver(classDeclaration.toClassName())
            .returns(ClassName(autoMapperInfo.targetPackage, autoMapperInfo.targetName))

        val mappings = mappingInfoResolver.resolve(classDeclaration)
        logger.info("Resolved mappings information:  $mappings")
        val propertyMappings = propertyMappingsGenerator.generate(
            classDeclaration, mappings,
            autoMapperInfo
        )
        logger.info("Generated property mappings: $propertyMappings")

        functionBuilder.addStatement("return ${autoMapperInfo.targetName}(\n$propertyMappings\n    )")

        val fileBuilder = FileSpec.builder(sourcePackageName, fileName)
        addImports(mappings, resolver, fileBuilder, autoMapperInfo.additionalImports)

        val file = fileBuilder.addFunction(functionBuilder.build()).build()
        file.writeTo(codeGenerator, Dependencies.ALL_FILES)
    }

    private fun addImports(
        mappings: Map<String, MappingInfo>,
        resolver: Resolver,
        fileBuilder: FileSpec.Builder,
        additionalImports: Set<String>
    ) {
        val extensionPackages = resolver.getSymbolsWithAnnotation(Mapping::class.simpleName.toString())
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
    }
}
