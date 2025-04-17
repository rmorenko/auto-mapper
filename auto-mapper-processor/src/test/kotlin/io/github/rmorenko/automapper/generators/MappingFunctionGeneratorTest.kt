package io.github.rmorenko.automapper.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.resolvers.AutoMapperInfoResolver
import io.github.rmorenko.automapper.resolvers.MappingInfoResolver
import io.github.rmorenko.automapper.setPrivateField
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class MappingFunctionGeneratorTest  : StringSpec({

    val logger = mockk<KSPLogger>(relaxed = true)

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
        mappingFunctionGenerator.generate(ksClassDeclaration, resolver, codeGenerator)
        verify (exactly = 0) {
            mappingInfoResolver.resolve(ksClassDeclaration)
            propertyMappingsGenerator.generate(ksClassDeclaration, any(), any())
        }
    }
})
