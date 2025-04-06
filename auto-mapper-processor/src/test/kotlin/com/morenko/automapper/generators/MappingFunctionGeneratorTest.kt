package com.morenko.automapper.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.morenko.automapper.annotations.Mapping
import com.morenko.automapper.exceptions.AnnotationNotPresent
import com.morenko.automapper.model.AutoMapperInfo
import com.morenko.automapper.model.DefaultInfo
import com.morenko.automapper.model.MappingInfo
import com.morenko.automapper.resolvers.AutoMapperInfoResolver
import com.morenko.automapper.resolvers.MappingInfoResolver
import com.morenko.automapper.setPrivateField
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
import io.mockk.declaringKotlinFile
import java.io.ByteArrayOutputStream

class MappingFunctionGeneratorTest  : StringSpec({

    val logger = mockk<KSPLogger>(relaxed = true)

    "should not generate code when class already processed" {

        val autoMapperInfoResolver = mockk<AutoMapperInfoResolver>()
        val mappingInfoResolver = mockk<MappingInfoResolver>()
        val propertyMappingsGenerator = mockk<PropertyMappingsGenerator>()

        val mappingFunctionGenerator = MappingFunctionGenerator(logger)
        mappingFunctionGenerator.setPrivateField("autoMapperInfoResolver", autoMapperInfoResolver)
        mappingFunctionGenerator.setPrivateField("mappingInfoResolver", mappingInfoResolver)
        mappingFunctionGenerator.setPrivateField("propertyMappingsGenerator", propertyMappingsGenerator)

        val ksClassDeclaration = mockk<KSClassDeclaration> {
            every {
                packageName.asString()
            } returns "com.example"
            every {
                simpleName.asString()
            } returns "SourceClass"
        }

        val resolver = mockk<Resolver>()
        val codeGenerator = mockk<CodeGenerator>()
        val processedClasses = mutableSetOf("com.example.SourceClass")
        mappingFunctionGenerator.generate(ksClassDeclaration, resolver, codeGenerator, processedClasses)
        verify (exactly = 0) {
            autoMapperInfoResolver.resolve(ksClassDeclaration)
            mappingInfoResolver.resolve(ksClassDeclaration)
            propertyMappingsGenerator.generate(ksClassDeclaration, any(), any())
        }
        processedClasses.size shouldBe 1
    }


    "should not generate code when class not processed and AutoMapper annotation not present" {

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

        val resolver = mockk<Resolver>()
        val codeGenerator = mockk<CodeGenerator>()
        val processedClasses = mutableSetOf<String>()
        mappingFunctionGenerator.generate(ksClassDeclaration, resolver, codeGenerator, processedClasses)
        verify (exactly = 0) {
            mappingInfoResolver.resolve(ksClassDeclaration)
            propertyMappingsGenerator.generate(ksClassDeclaration, any(), any())
        }
        processedClasses.size shouldBe 1
    }

    "should generate code when class not processed and AutoMapper annotation present" {

        val autoMapperInfoResolver = mockk<AutoMapperInfoResolver>()
        val mappingInfoResolver = mockk<MappingInfoResolver>()
        val propertyMappingsGenerator = mockk<PropertyMappingsGenerator>()

        val mappingFunctionGenerator = MappingFunctionGenerator(logger)



        val ksClassDeclaration = mockk<KSClassDeclaration> {
            every {
                packageName.asString()
            } returns "com.example"
            every {
                simpleName.asString()
            } returns "SourceClass"

        }
        mockkStatic(KSClassDeclaration::toClassName.declaringKotlinFile.qualifiedName!!)
        val className =  mockk<ClassName>(relaxed = true) {
            every { simpleName } returns "SourceClass"
            every { isAnnotated } returns true
        }

        every {
            ksClassDeclaration.toClassName()
        } returns className

        val resolver = mockk<Resolver>()
        val codeGenerator = mockk<CodeGenerator>()
        val processedClasses = mutableSetOf<String>()
        val autoMapperInfo = AutoMapperInfo(
            targetPackage = "com.target",
            targetName = "TargetClass",
            defaults = setOf(
                DefaultInfo ("defaultTarget1", "defaultCode1"),
                DefaultInfo ("defaultTarget2", "defaultCode2")
            ),
            additionalImports = setOf("com.import.one", "com.import.two"),
            excludes = setOf("exclude1", "exclude2")
        )
        every {
            autoMapperInfoResolver.resolve(ksClassDeclaration)
        } returns autoMapperInfo

        val mappings  = mapOf(
            "prop1" to MappingInfo(),
            "prop2" to MappingInfo(target = "NestedTargetClass"),
            "prop2" to MappingInfo(code = "some code")
        )
        every {
            mappingInfoResolver.resolve(ksClassDeclaration)
        } returns mappings

        every {
            propertyMappingsGenerator.generate(ksClassDeclaration, mappings, autoMapperInfo)
        } returns "Some mappings generated code"

        val otherKsClassDeclaration = mockk<KSClassDeclaration>()

        every {
            otherKsClassDeclaration.packageName.asString()
        } returns "com.import.three"

        every {
            resolver.getSymbolsWithAnnotation(Mapping::class.simpleName.toString())
        } returns listOf(otherKsClassDeclaration).asSequence()

        val dependencies = slot<Dependencies>()
        val packageName = slot<String>()
        val fileName = slot<String>()

        val outputStream = ByteArrayOutputStream()

        every {
            codeGenerator.createNewFile(capture(dependencies),
                capture(packageName), capture(fileName), "kt")
        } returns  outputStream

        mappingFunctionGenerator.setPrivateField("autoMapperInfoResolver", autoMapperInfoResolver)
        mappingFunctionGenerator.setPrivateField("mappingInfoResolver", mappingInfoResolver)
        mappingFunctionGenerator.setPrivateField("propertyMappingsGenerator", propertyMappingsGenerator)
        mappingFunctionGenerator.generate(ksClassDeclaration, resolver, codeGenerator, processedClasses)
        fileName.captured shouldBe "SourceClassMapper"
        packageName.captured shouldBe "com.example"
        processedClasses.size shouldBe 1
        processedClasses.first() shouldBe "com.example.SourceClass"
        outputStream.toByteArray().toString(Charsets.UTF_8) shouldContain """
            package com.example

            import com.`import`.one
            import com.`import`.three.prop1
            import com.`import`.three.prop2
            import com.`import`.two
            import com.target.TargetClass

            public fun .mapSourceClassToTargetClass(): TargetClass = TargetClass(
            Some mappings generated code
                )

        """.trimIndent()
    }

})
