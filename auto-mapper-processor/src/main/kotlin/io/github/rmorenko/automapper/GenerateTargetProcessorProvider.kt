package io.github.rmorenko.automapper

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider for the GenerateTargetProcessor.
 * This class is responsible for creating instances of the GenerateTargetProcessor.
 */
class GenerateTargetProcessorProvider : SymbolProcessorProvider {

    /**
     * Creates a new instance of the GenerateTargetProcessor.
     *
     * @param environment The environment provided by the Kotlin Symbol Processing API.
     * @return A new instance of GenerateTargetProcessor.
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return GenerateTargetProcessor(environment.codeGenerator, environment.logger)
    }
}
