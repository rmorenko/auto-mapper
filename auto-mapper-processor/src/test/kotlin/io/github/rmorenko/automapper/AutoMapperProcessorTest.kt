package io.github.rmorenko.automapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.generators.MappingFunctionGenerator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.mockk.every
import io.mockk.mockk

class AutoMapperProcessorTest : StringSpec({
    val logger = mockk<KSPLogger>(relaxed = true)

    "mappingFunctionGenerator should not execute when AutoMapper not resolved" {
        val codeGenerator = mockk<CodeGenerator>()
        val resolver = mockk<Resolver>()

        every {
            resolver.getSymbolsWithAnnotation(any())
        } returns emptyList<KSAnnotated>().asSequence()

        val autoMapperProcessor = AutoMapperProcessor(codeGenerator, logger)
        autoMapperProcessor.process(resolver).shouldBeEmpty()

        val ksFile = mockk<KSFile>()
        every {
            resolver.getSymbolsWithAnnotation(AutoMapperProcessor::class.qualifiedName.toString())
        } returns listOf<KSAnnotated>(ksFile).asSequence()
        autoMapperProcessor.process(resolver).shouldBeEmpty()

    }


    "mappingFunctionGenerator should execute when AutoMapper annotation resolved" {
        val codeGenerator = mockk<CodeGenerator>()
        val resolver = mockk<Resolver>()
        val ksClassDeclaration = mockk<KSClassDeclaration>()
        val mappingFunctionGenerator = mockk<MappingFunctionGenerator>()
        every {
            ksClassDeclaration.simpleName.asString()
        } returns AutoMapper::class.simpleName.toString()

        every {
            ksClassDeclaration.packageName.asString()
        } returns "com.example"

        every {
            mappingFunctionGenerator.generate(ksClassDeclaration, codeGenerator)
        } returns Unit

        every {
            resolver.getSymbolsWithAnnotation(AutoMapper::class.qualifiedName.toString())
        } returns listOf(ksClassDeclaration).asSequence()

        val autoMapperProcessor = AutoMapperProcessor(codeGenerator, logger)
        autoMapperProcessor
            .setPrivateField("mappingFunctionGenerator", mappingFunctionGenerator)
        autoMapperProcessor.process(resolver).shouldBeEmpty()
    }
})
