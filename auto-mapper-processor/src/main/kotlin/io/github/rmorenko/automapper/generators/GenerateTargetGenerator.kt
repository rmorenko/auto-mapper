package io.github.rmorenko.automapper.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.model.GenerateTargetInfo
import io.github.rmorenko.automapper.resolvers.GenerateTargetInfoResolver

/**
 * Class responsible for generating target classes.
 *
 * @property logger Logger for logging information during code generation.
 */
class GenerateTargetGenerator(private val logger: KSPLogger) {

    private val generateTargetInfoResolver : GenerateTargetInfoResolver = GenerateTargetInfoResolver(logger)

    /**
     * Generates target class with the same properties.
     *
     * @param classDeclaration The class declaration to generate target class.
     * @param codeGenerator The code generator to use for generating target class.
     */
    fun generate(classDeclaration: KSClassDeclaration, codeGenerator: CodeGenerator) {
        val generateTargetInfo = try{
            generateTargetInfoResolver.resolve(classDeclaration)
        } catch (e: AnnotationNotPresent) {
            logger.info(e.message.toString())
            return
        }
        val fileBuilder = FileSpec.builder(generateTargetInfo.packageName, generateTargetInfo.name)
        val constructorBuilder = createConstructor(generateTargetInfo)
        val typeSpecBuilder = TypeSpec
            .classBuilder(generateTargetInfo.name)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(constructorBuilder)
        addProperties(generateTargetInfo, typeSpecBuilder, fileBuilder)
        logger.info("Generating class ${generateTargetInfo.name}")
        val file = fileBuilder.addType(typeSpecBuilder.build()).build()
        file.writeTo(codeGenerator, Dependencies.ALL_FILES)
    }

    private fun addProperties(
        generateTargetInfo: GenerateTargetInfo,
        typeSpecBuilder: TypeSpec.Builder,
        fileBuilder: FileSpec.Builder
    ) {
        generateTargetInfo.properties.forEach { property ->
            typeSpecBuilder.addProperty(
                PropertySpec.builder(property.name, ClassName(property.pkg, property.className))
                    .initializer(property.name)
                    .build()
            )
            if (property.pkg.isNotEmpty()) {
                fileBuilder.addImport(property.pkg, property.className)
            }
        }
    }

    private fun createConstructor(generateTargetInfo: GenerateTargetInfo): FunSpec {
        val constructorBuilder = FunSpec.constructorBuilder()
        generateTargetInfo.properties.forEach { property ->
            constructorBuilder.addParameter(
                property.name,
                ClassName(property.pkg, property.className)
            )
        }
        return constructorBuilder.build()
    }

}
