package io.github.rmorenko.automapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.rmorenko.automapper.annotations.GenerateTarget
import io.github.rmorenko.automapper.generators.GenerateTargetGenerator

/**
 * Processes classes annotated with `@GenerateTarget` and generates target classes.
 */
class GenerateTargetProcessor(private val codeGenerator: CodeGenerator,
                              private val logger: KSPLogger) : SymbolProcessor {

    private val generateTargetGenerator = GenerateTargetGenerator(logger)

    /**
     * Processes classes annotated with `@GenerateTarget` and generates new target classes.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val classDeclarations = resolver.getClassDeclarations(GenerateTarget::class)
        logger.info("Found annotated GenerateTarget classes: ${classDeclarations.map { 
            it.simpleName.asString() 
        }}")
        classDeclarations.forEach { classDeclaration ->
            generateTargetGenerator.generate(classDeclaration, codeGenerator)
        }
        return emptyList()
    }
}
