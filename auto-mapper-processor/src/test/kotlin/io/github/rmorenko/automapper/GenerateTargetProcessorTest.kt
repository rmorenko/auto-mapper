package io.github.rmorenko.automapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.rmorenko.automapper.annotations.GenerateTarget
import io.github.rmorenko.automapper.generators.GenerateTargetGenerator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.mockk.every
import io.mockk.mockk

class GenerateTargetProcessorTest : StringSpec({
    val logger = mockk<KSPLogger>(relaxed = true)


    "process shouldn't failed when no annotated classes" {
        val codeGenerator = mockk<CodeGenerator>()
        val resolver = mockk<Resolver> {
            every {
                getSymbolsWithAnnotation(GenerateTarget::class.qualifiedName.toString())
            } returns emptyList<KSAnnotated>().asSequence()
        }
        val gGenerateTargetProcessor = GenerateTargetProcessor(codeGenerator, logger)
        gGenerateTargetProcessor.process(resolver).shouldBeEmpty()
    }

    "process shouldn't failed with annotated classes" {
        val codeGenerator = mockk<CodeGenerator>()
        val resolver = mockk<Resolver> {
            every {
                getSymbolsWithAnnotation(GenerateTarget::class.qualifiedName.toString())
            } returns listOf(
                mockk<KSAnnotated> {
                }
            ).asSequence()
        }
        val classDeclaration = mockk<KSClassDeclaration>()
        val gGenerateTargetProcessor = GenerateTargetProcessor(codeGenerator, logger)
        val generateTargetGenerator = mockk<GenerateTargetGenerator>()
        every {
            generateTargetGenerator.generate(classDeclaration, codeGenerator)
        } returns Unit
        gGenerateTargetProcessor.process(resolver).shouldBeEmpty()
    }

})
