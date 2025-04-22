package io.github.rmorenko.automapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
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
        val generateTargetArguments = listOf(
            mockk<KSValueArgument> {
                every {
                    name?.getShortName()
                } returns "name"

                every {
                    value
                } returns "TargetClass"
            },

            mockk<KSValueArgument> {
                every {
                    name?.getShortName()
                } returns "pkg"

                every {
                    value
                } returns "com.example"
            })
        val classDeclaration = mockk<KSClassDeclaration> {
            every {
                simpleName.asString()
            } returns "TestClass"
            every {
                getAllProperties()
            } returns emptyList<KSPropertyDeclaration>().asSequence()
            every {
                annotations
            } returns listOf(
                mockk<KSAnnotation> {
                    every {
                        shortName.asString()
                    } returns GenerateTarget::class.simpleName.toString()

                    every {
                        simpleName.asString()
                    } returns GenerateTarget::class.simpleName.toString()

                    every {
                        annotationType.resolve().declaration.qualifiedName?.asString()
                    } returns GenerateTarget::class.qualifiedName.toString()

                    every {
                        arguments
                    } returns generateTargetArguments
                }

            ).asSequence()
        }
        val resolver = mockk<Resolver> {
            every {
                getSymbolsWithAnnotation(GenerateTarget::class.qualifiedName.toString())
            } returns listOf(
                classDeclaration
            ).asSequence()
        }

        val gGenerateTargetProcessor = GenerateTargetProcessor(codeGenerator, logger)
        val generateTargetGenerator = mockk<GenerateTargetGenerator>()
        every {
            generateTargetGenerator.generate(classDeclaration, codeGenerator)
        } returns Unit
        gGenerateTargetProcessor.setPrivateField("generateTargetGenerator", generateTargetGenerator)
        gGenerateTargetProcessor.process(resolver).shouldBeEmpty()
    }

})
