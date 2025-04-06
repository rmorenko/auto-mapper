package com.morenko.automapper

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider for the AutoMapperProcessor.
 * This class is responsible for creating instances of the AutoMapperProcessor.
 */
class AutoMapperProcessorProvider : SymbolProcessorProvider {

    /**
     * Creates a new instance of the AutoMapperProcessor.
     *
     * @param environment The environment provided by the Kotlin Symbol Processing API.
     * @return A new instance of AutoMapperProcessor.
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutoMapperProcessor(environment.codeGenerator, environment.logger)
    }
}
