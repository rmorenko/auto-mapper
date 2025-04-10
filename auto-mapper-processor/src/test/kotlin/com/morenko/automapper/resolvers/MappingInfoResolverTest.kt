package com.morenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument

import com.morenko.automapper.annotations.Mapping
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

private val annotationSimpleName =  Mapping::class.simpleName.toString()

class MappingInfoResolverTest : StringSpec({
    val logger = mockk<KSPLogger>(relaxed = true)
    val resolver = MappingInfoResolver(logger)

    "resolve should return empty result without annotation" {
        val classDeclaration = mockk<KSClassDeclaration>()

        every {
            classDeclaration.annotations
        } returns listOf<KSAnnotation>().asSequence()


        val propertyDeclarations = listOf(
            mockk<KSPropertyDeclaration>().apply {
                every { annotations } returns listOf<KSAnnotation>().asSequence()
            }
        ).asSequence()

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        val actualResult = resolver.resolve(classDeclaration)
        actualResult.size shouldBe 0

    }

    "resolve should return empty result with empty Mapping annotation" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val mappingAnnotation = mockk<KSAnnotation>()
        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotation).asSequence()


        val propertyDeclarations = listOf(
            mockk<KSPropertyDeclaration>().apply {
                every { annotations } returns listOf(mappingAnnotation).asSequence()
            }
        ).asSequence()

        val mappingAnnotationName = mockk<KSName>()

        every {
            mappingAnnotationName.asString()
        } returns Mapping::class.toString()

        every {
            mappingAnnotation.shortName.asString()
        } returns annotationSimpleName


        every {
            mappingAnnotation.shortName
        } returns mappingAnnotationName

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        every {
            mappingAnnotation.arguments
        } returns emptyList()

        val actualResult = resolver.resolve(classDeclaration)
        actualResult.size shouldBe 0

    }

    "resolve should return correct answer" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val mappingAnnotation = mockk<KSAnnotation>()
        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotation).asSequence()

        val propertyDeclaration = mockk<KSPropertyDeclaration>().apply {
            every { annotations } returns listOf(mappingAnnotation).asSequence()
            every { simpleName.asString() } returns "sourceProperty"
        }


        val propertyDeclarations = listOf(propertyDeclaration
        ).asSequence()

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        val mappingAnnotationName = mockk<KSName>()

        every {
            mappingAnnotationName.asString()
        } returns annotationSimpleName

        every {
            mappingAnnotation.shortName.asString()
        } returns Mapping::class.simpleName.toString()


        every {
            mappingAnnotation.shortName
        } returns mappingAnnotationName

        val arguments = listOf(
            mockk<KSValueArgument>().apply {
                 every {
                     name?.getShortName()
                 } returns "target"

                every {
                    value
                } returns "targetProperty"
            },
            mockk<KSValueArgument>().apply {
                every {
                    name?.getShortName()
                } returns "mapFn"

                every {
                    value
                } returns "map"
            },
            mockk<KSValueArgument>().apply {
                every {
                    name?.getShortName()
                } returns "invokeFn"

                every {
                    value
                } returns "invoke"
            },
            mockk<KSValueArgument>().apply {
                every {
                    name?.getShortName()
                } returns "code"

                every {
                    value
                } returns "someCode"
            },
            mockk<KSValueArgument>().apply {
                every {
                    name?.getShortName()
                } returns "mapFirst"

                every {
                    value
                } returns false
            }
        )

        every {
            mappingAnnotation.arguments
        } returns arguments

        val actualResult = resolver.resolve(classDeclaration)
        val mappingInfo = actualResult["sourceProperty"]
        mappingInfo.shouldNotBeNull()
        mappingInfo.mapFirst shouldBe false
        mappingInfo.code shouldBe "someCode"
        mappingInfo.mapFn shouldBe "map"
        mappingInfo.invokeFn shouldBe "invoke"
    }

})
