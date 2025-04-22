package io.github.rmorenko.automapper.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.rmorenko.automapper.annotations.GenerateTarget
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.model.GenerateTargetInfo
import io.github.rmorenko.automapper.model.TargetPropertyInfo
import io.github.rmorenko.automapper.resolvers.GenerateTargetInfoResolver
import io.github.rmorenko.automapper.setPrivateField
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

class GenerateTargetGeneratorTest : StringSpec({
    val logger = mockk<KSPLogger>(relaxed = true)


    "generate shouldn not failed for class without any GenerateTarget annotations" {
        val codeGenerator = mockk<CodeGenerator>()
        val generateTargetGenerator = GenerateTargetGenerator(logger)
        val ksClassDeclaration = mockk<KSClassDeclaration>()
        val generateTargetInfoResolver = mockk<GenerateTargetInfoResolver> {
            every {
                resolve(ksClassDeclaration)
            } throws AnnotationNotPresent(GenerateTarget::class)
        }
        generateTargetGenerator.setPrivateField("generateTargetInfoResolver", generateTargetInfoResolver)
        generateTargetGenerator.generate(ksClassDeclaration, codeGenerator)
    }

    "generate should create target class for class with  GenerateTarget annotation and without any properties" {
        val generateTargetGenerator = GenerateTargetGenerator(logger)
        val ksClassDeclaration = mockk<KSClassDeclaration>()
        val generateTargetInfoResolver = mockk<GenerateTargetInfoResolver> {
            every {
                resolve(ksClassDeclaration)
            } returns GenerateTargetInfo(
                properties = emptyList(),
                packageName = "com.example",
                name = "SomeClass"
            )
        }
        val codeGenerator = mockk<CodeGenerator>()
        val stream = ByteArrayOutputStream()
        every {
            codeGenerator.createNewFile(Dependencies.ALL_FILES, "com.example",
                "SomeClass", "kt")
        } returns stream
        generateTargetGenerator.setPrivateField("generateTargetInfoResolver", generateTargetInfoResolver)
        generateTargetGenerator.generate(ksClassDeclaration, codeGenerator)
        stream.toByteArray().toString(Charset.defaultCharset()).trimIndent() shouldBe """
            package com.example

            public data class SomeClass()""".trimIndent()
    }


    "generate should create target class for class with  GenerateTarget annotation and properties" {
        val generateTargetGenerator = GenerateTargetGenerator(logger)
        val ksClassDeclaration = mockk<KSClassDeclaration>()
        val generateTargetInfoResolver = mockk<GenerateTargetInfoResolver> {
            every {
                resolve(ksClassDeclaration)
            } returns GenerateTargetInfo(
                properties = listOf(
                    TargetPropertyInfo(
                        name = "propertyOne",
                        pkg = "com.example.one",
                        className = "OneClass"
                    )
                ),
                packageName = "com.example",
                name = "SomeClass"
            )
        }
        val codeGenerator = mockk<CodeGenerator>()
        val stream = ByteArrayOutputStream()
        every {
            codeGenerator.createNewFile(Dependencies.ALL_FILES, "com.example",
                "SomeClass", "kt")
        } returns stream
        generateTargetGenerator.setPrivateField("generateTargetInfoResolver", generateTargetInfoResolver)
        generateTargetGenerator.generate(ksClassDeclaration, codeGenerator)
        stream.toByteArray().toString(Charset.defaultCharset()).trimIndent() shouldBe """
            package com.example

            import com.example.one.OneClass

            public data class SomeClass(
              public val propertyOne: OneClass,
            )""".trimIndent()
    }

})
