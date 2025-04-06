package com.morenko.automapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Processes classes annotated with `@AutoMapper` and generates mapping functions.
 */
class AutoMapperProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {

    private val processedClasses = mutableSetOf<String>()

    private val mappingsCodeGenerator = MappingsCodeGenerator(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(AutoMapper::class.qualifiedName.orEmpty())
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        logger.info("Found annotated classes: ${symbols.map { it.simpleName.asString() }}")
        symbols.forEach { symbol ->
            mappingsCodeGenerator.generateMapping(symbol, resolver, codeGenerator, processedClasses)
        }
        return emptyList()
    }
}
