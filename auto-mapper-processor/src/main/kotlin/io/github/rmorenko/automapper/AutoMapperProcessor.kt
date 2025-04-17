package io.github.rmorenko.automapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.generators.MappingFunctionGenerator

/**
 * Processes classes annotated with `@AutoMapper` and generates mapping functions.
 */
class AutoMapperProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {

    private val mappingFunctionGenerator = MappingFunctionGenerator(logger)

    /**
     * Processes classes annotated with `@AutoMapper` and generates mapping functions.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val classDeclarations = resolver.getClassDeclarations(AutoMapper::class)
        logger.info("Found annotated AutoMapper classes: ${classDeclarations.map { it.simpleName.asString() }}")
        classDeclarations.forEach { classDeclaration ->
            mappingFunctionGenerator.generate(classDeclaration, resolver, codeGenerator)
        }
        return emptyList()
    }
}

