package io.github.rmorenko.automapper.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.exceptions.MultiplyAnnotationException
import io.github.rmorenko.automapper.model.AutoMapperInfo
import io.github.rmorenko.automapper.model.DefaultInfo
import io.github.rmorenko.automapper.model.MappingInfo
import io.github.rmorenko.automapper.resolvers.AutoMapperInfoResolver
import io.github.rmorenko.automapper.resolvers.MappingInfoResolver
import io.github.rmorenko.automapper.setPrivateField
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset


class MappingFunctionGeneratorTest  : StringSpec({

    val logger = mockk<KSPLogger>(relaxed = true)

    "should not generate code when AutoMapper annotation not present" {
        val autoMapperInfoResolver = mockk<AutoMapperInfoResolver>()
        val mappingInfoResolver = mockk<MappingInfoResolver>()
        val propertyMappingsGenerator = mockk<PropertyMappingsGenerator>()

        val mappingFunctionGenerator = MappingFunctionGenerator(logger)
        mappingFunctionGenerator.setPrivateField("autoMapperInfoResolver", autoMapperInfoResolver)
        val ksClassDeclaration = mockk<KSClassDeclaration> {
            every {
                packageName.asString()
            } returns "com.example"
            every {
                simpleName.asString()
            } returns "SourceClass"
        }
        every {
            autoMapperInfoResolver.resolve(ksClassDeclaration)
        } throws AnnotationNotPresent(String::class)


        val codeGenerator = mockk<CodeGenerator>()
        mappingFunctionGenerator.generate(ksClassDeclaration, codeGenerator)
        verify (exactly = 0) {
            mappingInfoResolver.resolve(ksClassDeclaration)
            propertyMappingsGenerator.generate(ksClassDeclaration, any(), any())
        }
    }

    "should not generate code when duplicate AutoMapper annotation" {

        val autoMapperInfoResolver = mockk<AutoMapperInfoResolver>()
        val mappingFunctionGenerator = MappingFunctionGenerator(logger)
        mappingFunctionGenerator.setPrivateField("autoMapperInfoResolver", autoMapperInfoResolver)
        val ksClassDeclaration = mockk<KSClassDeclaration> {
            every {
                packageName.asString()
            } returns "com.example"
            every {
                simpleName.asString()
            } returns "SourceClass"
        }
        every {
            autoMapperInfoResolver.resolve(ksClassDeclaration)
        } throws MultiplyAnnotationException(String::class)


        val codeGenerator = mockk<CodeGenerator>()
        shouldThrow<MultiplyAnnotationException> {
            mappingFunctionGenerator.generate(ksClassDeclaration, codeGenerator)
        }
    }

    "should generate code when AutoMapper annotation presents" {

        val autoMapperInfoResolver = mockk<AutoMapperInfoResolver>()
        val mappingInfoResolver = mockk<MappingInfoResolver>()
        val propertyMappingsGenerator = mockk<PropertyMappingsGenerator>()

        val mappingFunctionGenerator = MappingFunctionGenerator(logger)
        mappingFunctionGenerator.setPrivateField("autoMapperInfoResolver", autoMapperInfoResolver)
        val ksClassDeclaration = mockk<KSClassDeclaration> {
            every {
                packageName.asString()
            } returns "com.example"
            every {
                simpleName.asString()
            } returns "SourceClass"

            every {
               simpleName
            } returns mockk<KSName>(relaxed = true)

            every {
                parentDeclaration
            } returns mockk<KSClassDeclaration>(relaxed = true)

            every {
                classKind
            } returns ClassKind.CLASS

            every {
                annotations
            } returns emptyList<KSAnnotation>().asSequence()

            every {
                qualifiedName?.asString()
            } returns "com.example.SourceClass"

            every {
                getAllProperties()
            } returns emptyList<KSPropertyDeclaration>().asSequence()

        }

        val autoMapperInfo = AutoMapperInfo(
            targetPackage = "com.example.target",
            targetName = "TargetClass",
            defaults = setOf(
                DefaultInfo("propertyDefault", "default code")
            ),
            additionalImports = setOf("com.example.additional"),
            excludes = setOf("propertyExcludes")
        )

        every {
            autoMapperInfoResolver.resolve(ksClassDeclaration)
        } returns autoMapperInfo

        val mappings = mapOf(
            "propertyOne" to MappingInfo(
                target = "targetPropertyOne",
                functionName = "functionOne",
                isExtension = true
            )
        )
        every {
            mappingInfoResolver.resolve(ksClassDeclaration)
        } returns mappings

        val outputStream = ByteArrayOutputStream()
        val codeGenerator = mockk<CodeGenerator> {
            every {
                createNewFile(Dependencies.ALL_FILES, "com.example", "Mapper", "kt")
            } returns outputStream
        }
        mappingFunctionGenerator.setPrivateField("mappingInfoResolver", mappingInfoResolver)
        every {
            propertyMappingsGenerator.generate(ksClassDeclaration, mappings, autoMapperInfo)
        } returns "generated code"
        mappingFunctionGenerator.setPrivateField("propertyMappingsGenerator", propertyMappingsGenerator)
        mappingFunctionGenerator.generate(ksClassDeclaration, codeGenerator)
        outputStream.toString(Charset.defaultCharset()).trimIndent() shouldBe """
            package com.example

            import com.example.additional
            import com.example.target.TargetClass

            public fun SourceClass.mapToTargetClass(): TargetClass = TargetClass(
            generated code
                )
        """.trimIndent()
    }
})
