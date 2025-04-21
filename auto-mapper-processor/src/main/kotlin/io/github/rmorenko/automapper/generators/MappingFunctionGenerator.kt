package io.github.rmorenko.automapper.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.model.AutoMapperInfo
import io.github.rmorenko.automapper.resolvers.AutoMapperInfoResolver
import io.github.rmorenko.automapper.resolvers.MappingInfoResolver

/**
 * Class responsible for generating mapping function for data classes.
 *
 * @property logger Logger for logging information during code generation.
 */
class MappingFunctionGenerator(private val logger: KSPLogger) {

    private val autoMapperInfoResolver = AutoMapperInfoResolver(logger)
    private val mappingInfoResolver = MappingInfoResolver(logger)
    private val propertyMappingsGenerator = PropertyMappingsGenerator(logger)


    /**
     * Generates a mapping function for the given class declaration.
     *
     * @param classDeclaration The class declaration to generate the mapping function for.
     * @param codeGenerator The code generator to use for generating the mapping function.
     */
    fun generate(
        classDeclaration: KSClassDeclaration,
        codeGenerator: CodeGenerator
    ) {
        val sourcePackageName = classDeclaration.packageName.asString()
        val sourceClassName = classDeclaration.simpleName.asString()

        val autoMapperInfo = try {
            autoMapperInfoResolver.resolve(classDeclaration)
        } catch (e: AnnotationNotPresent) {
            logger.info(e.message.toString())
            return
        }
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
        addImports(fileBuilder, autoMapperInfo)

        val file = fileBuilder.addFunction(functionBuilder.build()).build()
        file.writeTo(codeGenerator, Dependencies.ALL_FILES)
    }

    private fun addImports(
        fileBuilder: FileSpec.Builder,
        autoMapperInfo: AutoMapperInfo
    ) {
        autoMapperInfo.additionalImports.forEach {
            fileBuilder.addImport(it, "")
        }
    }
}
